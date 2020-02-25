package technology.bear.bot.message

import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.KeyboardButton
import org.jetbrains.exposed.sql.SizedIterable
import technology.bear.database.dao.Task

fun generatePeriodicalTaskInfo(userTasks: SizedIterable<Task>): String {
    val answer = StringBuilder().append("Ваши цели:\n\n")
    for (userTask in userTasks) {
        answer.append("Название: *${userTask.taskName}*\nПериодичность: *${userTask.taskFrequency}*\n\n")
    }
    return answer.toString()
}

fun generateTaskFrequencyButtons(): List<List<KeyboardButton>> {
    return listOf(
        listOf(
            KeyboardButton("Раз в минуту"),
            KeyboardButton("Раз в две минуты"),
            KeyboardButton("Раз в три минуты")
        )
    )
}

fun generateMainMenuButtons(): List<List<KeyboardButton>> {
    return listOf(
        listOf(
            KeyboardButton("Добавь цель"),
            KeyboardButton("Покажи все мои цели")
        )
    )
}

fun generateAnswerButtons(): List<List<InlineKeyboardButton>> {
    return listOf(
        listOf(
            InlineKeyboardButton("Да", callbackData = "yes"),
            InlineKeyboardButton("Нет", callbackData = "no")
        )
    )
}
