package technology.bear.bot

import me.ivmg.telegram.dispatcher.Dispatcher
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.KeyboardReplyMarkup
import me.ivmg.telegram.entities.ParseMode
import me.ivmg.telegram.entities.ReplyKeyboardRemove
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import technology.bear.bot.message.*
import technology.bear.constans.EventStatus
import technology.bear.constans.UserState
import technology.bear.constans.UserState.ADDING_TASK_FREQUENCY
import technology.bear.constans.UserState.ADDING_TASK_NAME
import technology.bear.database.dao.Event
import technology.bear.database.dao.Task
import technology.bear.database.dao.Tasks

fun Dispatcher.handleStartCommand() {
    command("start") { bot, update ->
        val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateMainMenuButtons(), resizeKeyboard = true)
        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = "Дратути, выберите что хотите сделать",
            replyMarkup = keyboardMarkup
        )
    }
}

fun Dispatcher.handleAddingTask(userStates: HashMap<Long, UserState>) {
    message(newTaskFilter(userStates)) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message
        userStates[chatId] = ADDING_TASK_NAME
        bot.sendMessage(chatId = chatId, text = "Напишите название цели", replyMarkup = ReplyKeyboardRemove())
    }
}

fun Dispatcher.handleAddingTaskFrequency(
    userStates: HashMap<Long, UserState>,
    currentUserTask: HashMap<Long, Task.Builder>
) {
    message(taskNameFilter(userStates)) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message
        if (userStates[chatId] == ADDING_TASK_NAME) {

            currentUserTask[chatId] = Task.Builder()
                .userId(chatId)
                .taskName(update.message?.text!!)

            userStates[chatId] = ADDING_TASK_FREQUENCY

            val keyboardMarkup =
                KeyboardReplyMarkup(keyboard = generateTaskFrequencyButtons(), resizeKeyboard = true)

            bot.sendMessage(
                chatId = chatId,
                text = "Выберите периодичность цели",
                replyMarkup = keyboardMarkup
            )
        }
    }
}

fun Dispatcher.handleSavingTask(
    userStates: HashMap<Long, UserState>,
    currentUserTask: HashMap<Long, Task.Builder>
) {
    message(taskFrequencyFilter(userStates)) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message

        currentUserTask[chatId]!!.apply {
            taskFrequency(update.message?.text!!)
            transaction {
                val task = newTask()
                when (task.taskFrequency) {
                    "Раз в минуту" -> {
                        Event.new {
                            this.task = task
                            taskTime = DateTime.now().plusMinutes(1)
                            status = EventStatus.ACTIVE.toString()
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

        val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateMainMenuButtons(), resizeKeyboard = true)

        bot.sendMessage(
            chatId = chatId,
            text = "Цель сохранена",
            replyMarkup = keyboardMarkup
        )
    }
}

fun Dispatcher.handleShowingTasks() {
    message(showAllTasksFilter) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message

        transaction {
            val userTasks = Task.find { Tasks.userId eq chatId }
            bot.sendMessage(
                chatId = chatId,
                text = generatePeriodicalTaskInfo(userTasks),
                parseMode = ParseMode.HTML
            )
        }
    }
}