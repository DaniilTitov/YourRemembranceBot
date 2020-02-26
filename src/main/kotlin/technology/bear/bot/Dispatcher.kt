package technology.bear.bot

import me.ivmg.telegram.dispatcher.Dispatcher
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.ParseMode.MARKDOWN
import me.ivmg.telegram.entities.ReplyKeyboardRemove
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import technology.bear.bot.message.*
import technology.bear.constans.CallbackData.SUCCESSFULLY
import technology.bear.constans.CallbackData.UNSUCCESSFUL
import technology.bear.constans.UserState
import technology.bear.constans.UserState.ADDING_TASK_FREQUENCY
import technology.bear.constans.UserState.ADDING_TASK_NAME
import technology.bear.constans.happySmiles
import technology.bear.constans.sadSmiles
import technology.bear.database.dao.Task
import technology.bear.database.dsl.Statistics
import technology.bear.database.dsl.Tasks

fun Dispatcher.handleStartCommand() {
    command("start") { bot, update ->
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
        if (userStates[chatId] == ADDING_TASK_NAME) {

            currentUserTask[chatId] = Task.Builder()
                .userId(chatId)
                .taskName(update.message?.text!!)

            userStates[chatId] = ADDING_TASK_FREQUENCY

            bot.sendMessage(
                chatId = chatId,
                text = "Выберите периодичность цели",
                replyMarkup = taskFrequencyMarkup
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
            val tasksData = (Tasks innerJoin Statistics).slice(
                Tasks.taskName,
                Tasks.taskFrequency,
                Statistics.completedCount,
                Statistics.uncompletedCount
            ).select { Tasks.userId eq chatId }

            bot.sendMessage(
                chatId = chatId,
                text = generatePeriodicalTaskInfo(tasksData),
                parseMode = MARKDOWN
            )
        }
    }
}

fun Dispatcher.handleRemovingTask() {
    message(removeTasksFilter) { bot, update ->

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
