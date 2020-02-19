package technology.bear.bot


import com.natpryce.konfig.ConfigurationProperties.Companion.fromResource
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.KeyboardButton
import me.ivmg.telegram.entities.KeyboardReplyMarkup
import me.ivmg.telegram.entities.ParseMode
import me.ivmg.telegram.entities.ReplyKeyboardRemove
import me.ivmg.telegram.extensions.filters.Filter.Custom
import me.ivmg.telegram.extensions.filters.Filter.Text
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import technology.bear.constans.CONFIG_FILE
import technology.bear.constans.TOKEN_KEY
import technology.bear.constans.UserState
import technology.bear.constans.UserState.ADDING_TASK_FREQUENCY
import technology.bear.constans.UserState.ADDING_TASK_NAME
import technology.bear.database.dao.Event
import technology.bear.database.dao.Events
import technology.bear.database.dao.Task
import technology.bear.database.dao.Tasks
import technology.bear.database.initDatabase
import kotlin.concurrent.fixedRateTimer


fun main() {

    val config = fromResource(CONFIG_FILE)
    initDatabase(config)

    val userStates = hashMapOf<Long, UserState>()
    val currentUserTask = hashMapOf<Long, Task.Builder>()


    val bot = bot {
        token = config[Key(TOKEN_KEY, stringType)]
        logLevel = BASIC

        dispatch {

            command("start") { bot, update ->
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    text = "Дратути, выберите что хотите сделать",
                    replyMarkup = keyboardMarkup
                )
            }

            message(Custom {
                userStates[chat.id] != ADDING_TASK_NAME
                        && userStates[chat.id] != ADDING_TASK_FREQUENCY
                        && text.equals("Добавь цель")
            }) { bot, update ->
                val chatId = update.message?.chat?.id ?: return@message
                userStates[chatId] = ADDING_TASK_NAME
                bot.sendMessage(chatId = chatId, text = "Напишите название цели", replyMarkup = ReplyKeyboardRemove())
            }

            message(Text and Custom { userStates[chat.id] == ADDING_TASK_NAME }) { bot, update ->
                val chatId = update.message?.chat?.id ?: return@message
                if (userStates[chatId] == ADDING_TASK_NAME) {

                    currentUserTask[chatId] = Task.Builder()
                        .userId(chatId)
                        .taskName(update.message?.text!!)

                    userStates[chatId] = ADDING_TASK_FREQUENCY

                    val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton1(), resizeKeyboard = true)

                    bot.sendMessage(
                        chatId = chatId,
                        text = "Выберите периодичность цели",
                        replyMarkup = keyboardMarkup
                    )
                }
            }

            message(Text and Custom { userStates[chat.id] == ADDING_TASK_FREQUENCY }) { bot, update ->
                val chatId = update.message?.chat?.id ?: return@message

                currentUserTask[chatId]!!.apply {
                    taskFrequency(update.message?.text!!)
                    transaction {
                        val task = newTask()
                        when (task.taskFrequency) {
                            "Раз в минуту" -> {
                                Event.new {
                                    taskId = task
                                    taskName = task.taskName
                                    userId = chatId
                                    taskTime = DateTime.now().plusMinutes(1)
                                }
                            }
                            "Раз в две минуты" -> {

                            }
                            "Раз в три минуты" -> {

                            }

                        }

                        commit()

                    }
                }

                userStates.remove(chatId)
                currentUserTask.remove(chatId)

                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)

                bot.sendMessage(
                    chatId = chatId,
                    text = "Цель сохранена",
                    replyMarkup = keyboardMarkup
                )
            }

            message(Custom { text.equals("Покажи все мои цели") }) { bot, update ->
                val chatId = update.message?.chat?.id ?: return@message

                val answer = StringBuilder().append("Ваши цели:\n\n")
                transaction {
                    val userTasks = Task.find { Tasks.userId eq chatId }
                    generateUserTasksMarkup(userTasks, answer)
                }

                bot.sendMessage(chatId = chatId, text = answer.toString(), parseMode = ParseMode.HTML)
            }
        }
    }

    fixedRateTimer(period = 5000, action = {
        transaction {
            Event.find { Events.taskTime.less(DateTime.now()) }.forEach {
                bot.sendMessage(chatId = it.userId, text = "Напоминаю о " + it.taskName)
            }
        }
    })

    bot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(
            KeyboardButton("Добавь цель"),
            KeyboardButton("Покажи все мои цели")
        )
    )
}

fun generateUsersButton1(): List<List<KeyboardButton>> {
    return listOf(
        listOf(
            KeyboardButton("Раз в минуту"),
            KeyboardButton("Раз в две минуты"),
            KeyboardButton("Раз в три минуты")
        )
    )
}

fun generateUserTasksMarkup(
    userTasks: SizedIterable<Task>,
    answer: StringBuilder
) {
    for (userTask in userTasks) {
        answer.append("Название: " + userTask.taskName + "\n" + "Периодичность: " + userTask.taskFrequency + "\n\n")
    }
}
