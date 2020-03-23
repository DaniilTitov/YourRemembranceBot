package technology.bear.bot.message

import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.KeyboardButton
import me.ivmg.telegram.entities.KeyboardReplyMarkup
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SizedIterable
import org.joda.time.DateTime
import org.joda.time.Period
import technology.bear.constans.CallbackData
import technology.bear.constans.MainCommands
import technology.bear.constans.TaskFrequency
import technology.bear.database.dao.Task
import technology.bear.database.dsl.Events
import technology.bear.database.dsl.Statistics
import technology.bear.database.dsl.Tasks

fun generatePeriodicalTaskInfo(taskData: Query): String {
    if (taskData.empty()) {
        return "У вас пока нет целей, но вы можете их добавить"
    }

    val answer = StringBuilder("Ваши цели:\n\n")
    for (data in taskData) {
        val period = Period(DateTime.now(), data[Events.taskTime])
        answer.append(
            "Название: *${data[Tasks.taskName]}*\n" +
                    "Периодичность: *${data[Tasks.taskFrequency]}*\n" +
                    "Получилось выполнить раз - *${data[Statistics.completedCount]}*\n" +
                    "Не получилось выполнить раз - *${data[Statistics.uncompletedCount]}*\n" +
                    "Следующее оповещение через ${ if (period.days > 0) {period.days.toString() + "дней, " } else ""} ${period.hours} часов, ${period.minutes} минут\n\n"
        )
    }
    return answer.toString()
}

val taskFrequencyMarkup = KeyboardReplyMarkup(
    keyboard = listOf(TaskFrequency.values().map {
        KeyboardButton(text = it.frequencyMessage)
    }.toList()),
    resizeKeyboard = true
)


fun generateInlineTaskAnswersMarkup(id: EntityID<Int>): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(CallbackData.values().map {
        InlineKeyboardButton(
            text = it.description,
            callbackData = "${it.name} $id"
        )
    }.toList()))
}

val mainMenuMarkup = KeyboardReplyMarkup(
    keyboard = listOf(MainCommands.values().map {
        KeyboardButton(it.command)
    }).toList(),
    resizeKeyboard = true
)

fun generateRemoveMenuMarkup(tasks: SizedIterable<Task>): KeyboardReplyMarkup {
    val buttons = mutableListOf(tasks.map { KeyboardButton(it.taskName) })
    buttons.add(listOf(KeyboardButton("Отмена")))

    return KeyboardReplyMarkup(
        keyboard = buttons,
        resizeKeyboard = true
    )
}