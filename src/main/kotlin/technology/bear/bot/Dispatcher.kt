package technology.bear.bot

import me.ivmg.telegram.dispatcher.Dispatcher
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.ParseMode.MARKDOWN
import me.ivmg.telegram.entities.ReplyKeyboardRemove
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import technology.bear.bot.message.*
import technology.bear.constans.*
import technology.bear.constans.CallbackData.SUCCESSFULLY
import technology.bear.constans.CallbackData.UNSUCCESSFUL
import technology.bear.constans.EventStatus.*
import technology.bear.constans.UserState.*
import technology.bear.database.dao.Task
import technology.bear.database.dsl.Events
import technology.bear.database.dsl.Statistics
import technology.bear.database.dsl.Tasks

fun Dispatcher.handleStartCommand(userStates: HashMap<Long, UserState>, currentUserTask: HashMap<Long, Task.Builder>) {
    command("start") { bot, update ->
        val chatId = update.message?.chat?.id ?: return@command

        userStates.remove(chatId)
        currentUserTask.remove(chatId)

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = "Дратути, выберите что хотите сделать",
            replyMarkup = mainMenuMarkup
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
        val text = update.message?.text!!

        if (text.length > 256) {
            bot.sendMessage(chatId = chatId, text = "Название цели не должно содержать больше 256 символов")
            return@message
        }

        currentUserTask[chatId] = Task.Builder()
            .userId(chatId)
            .taskName(text)

        userStates[chatId] = ADDING_TASK_FREQUENCY

        bot.sendMessage(
            chatId = chatId,
            text = "Выберите периодичность цели",
            replyMarkup = taskFrequencyMarkup
        )
    }
}

fun Dispatcher.handleRemovingTaskName(userStates: HashMap<Long, UserState>) {
    message(removingTaskNameFilter(userStates)) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message
        val text = update.message?.text!!

        if (text == "Отмена") {
            userStates.remove(chatId)

            bot.sendMessage(
                chatId = chatId,
                text = "Дратути, выберите что хотите сделать",
                replyMarkup = mainMenuMarkup
            )

            return@message
        }

        transaction {
            val tasks = Task.find { (Tasks.userId eq chatId) and (Tasks.taskName eq text) }.map { it.id }.toList()
            Events.deleteWhere { Events.id inList tasks }
            Statistics.deleteWhere { Statistics.id inList tasks }
            Tasks.deleteWhere { (Tasks.userId eq chatId) and (Tasks.taskName.eq(text)) }
        }

        userStates.remove(chatId)

        bot.sendMessage(
            chatId = chatId,
            text = "Цель удалена",
            replyMarkup = mainMenuMarkup
        )
    }
}

fun Dispatcher.handleSavingTask(
    userStates: HashMap<Long, UserState>,
    currentUserTask: HashMap<Long, Task.Builder>
) {
    message(taskFrequencyFilter(userStates)) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message
        val taskFrequency = update.message?.text!!

        // todo: remove this shit
        if (!TaskFrequency.values().map { it.frequencyMessage }.contains(taskFrequency)) {
            bot.sendMessage(
                chatId = chatId,
                text = "Выберите значение из списка"
            )
            return@message
        }

        currentUserTask[chatId]!!.apply {
            taskFrequency(taskFrequency)
            transaction {
                newTask().apply {
                    createNewEvent()
                    createNewStatistic()
                }
                commit()
            }
        }

        userStates.remove(chatId)
        currentUserTask.remove(chatId)

        bot.sendMessage(
            chatId = chatId,
            text = "Цель сохранена",
            replyMarkup = mainMenuMarkup
        )
    }
}

fun Dispatcher.handleShowingTasks() {
    message(showAllTasksFilter) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message

        transaction {
            val tasksData = (Tasks innerJoin Statistics innerJoin Events).slice(
                Tasks.taskName,
                Tasks.taskFrequency,
                Statistics.completedCount,
                Statistics.uncompletedCount,
                Events.taskTime
            ).select { (Tasks.userId eq chatId) and (Events.status eq ACTIVE) }

            bot.sendMessage(
                chatId = chatId,
                text = generatePeriodicalTaskInfo(tasksData),
                parseMode = MARKDOWN
            )
        }
    }
}

fun Dispatcher.handleRemovingTask(userStates: HashMap<Long, UserState>) {
    message(removeTasksFilter) { bot, update ->
        val chatId = update.message?.chat?.id ?: return@message

        transaction {
            bot.sendMessage(
                chatId = chatId,
                text = "Какую задачу удалить?",
                parseMode = MARKDOWN,
                replyMarkup = generateRemoveMenuMarkup(Task.find { Tasks.userId eq chatId })
            )
        }

        userStates[chatId] = REMOVING_TASK
    }
}

fun Dispatcher.handleCallback() {
    callbackQuery(SUCCESSFULLY.name) { bot, update ->

        val callbackQuery = update.callbackQuery!!
        val markdown = callbackQuery.message?.entities?.get(0)!!
        val taskName = callbackQuery.message?.text!!.substring(markdown.offset, markdown.offset + markdown.length)
        val smile = happySmiles.random()

        transaction {
            Statistics.update({ Statistics.id eq callbackQuery.data.split(" ").last().toInt() }) {
                with(SqlExpressionBuilder) {
                    it.update(completedCount, completedCount + 1)
                }
            }
        }

        bot.editMessageText(
            chatId = callbackQuery.from.id,
            messageId = callbackQuery.message?.messageId,
            parseMode = MARKDOWN,
            text = "$smile Поздравляю! У тебя получилось выполнить цель *$taskName* $smile"
        )
    }

    callbackQuery(UNSUCCESSFUL.name) { bot, update ->

        val callbackQuery = update.callbackQuery!!
        val markdown = callbackQuery.message?.entities?.get(0)!!
        val taskName = callbackQuery.message?.text!!.substring(markdown.offset, markdown.offset + markdown.length)
        val smile = sadSmiles.random()

        transaction {
            Statistics.update({ Statistics.id eq callbackQuery.data.split(" ").last().toInt() }) {
                with(SqlExpressionBuilder) {
                    it.update(uncompletedCount, uncompletedCount + 1)
                }
            }
        }

        bot.editMessageText(
            chatId = callbackQuery.from.id,
            messageId = callbackQuery.message?.messageId,
            parseMode = MARKDOWN,
            text = "$smile Ничего, в следующий раз *$taskName* обязательно получится $smile"
        )
    }
}
