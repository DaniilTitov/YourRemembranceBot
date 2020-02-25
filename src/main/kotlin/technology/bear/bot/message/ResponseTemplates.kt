package technology.bear.bot.message

import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.KeyboardButton
import me.ivmg.telegram.entities.KeyboardReplyMarkup
import org.jetbrains.exposed.sql.SizedIterable
import technology.bear.constans.CallbackData
import technology.bear.constans.MainCommands
import technology.bear.constans.TaskFrequency
import technology.bear.database.dao.Task

fun generatePeriodicalTaskInfo(userTasks: SizedIterable<Task>): String {
    val answer = StringBuilder("Ваши цели:\n\n")
    for (userTask in userTasks) {
        answer.append("Название: *${userTask.taskName}*\nПериодичность: *${userTask.taskFrequency}*\n\n")
    }
    return answer.toString()
}

val taskFrequencyMarkup = KeyboardReplyMarkup(
    keyboard = listOf(TaskFrequency.values().map {
        KeyboardButton(text = it.frequencyMessage)
    }.toList()),
    resizeKeyboard = true
)

val inlineTaskAnswersMarkup = InlineKeyboardMarkup(listOf(CallbackData.values().map {
    InlineKeyboardButton(
        text = it.description,
        callbackData = it.name
    )
}.toList()))

val mainMenuMarkup = KeyboardReplyMarkup(
    keyboard = listOf(MainCommands.values().map {
        KeyboardButton(it.command)
    }).toList(),
    resizeKeyboard = true
)
