package technology.bear.scheduler

import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.ParseMode.MARKDOWN
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import technology.bear.bot.message.inlineTaskAnswersMarkup
import technology.bear.constans.EventStatus.ACTIVE
import technology.bear.constans.EventStatus.SENT
import technology.bear.database.dao.Event
import technology.bear.database.dao.Events
import technology.bear.database.dao.createNewEvent
import kotlin.concurrent.fixedRateTimer

fun runScheduler(bot: Bot) {
    fixedRateTimer(period = 5000, action = {
        transaction {
            Event.find { Events.taskTime.less(DateTime.now()) and Events.status.eq(ACTIVE) }.forEach {

                bot.sendMessage(
                    chatId = it.task.userId,
                    text = "Напоминаю о *" + it.task.taskName + "*\nПолучилось выполнить?",
                    replyMarkup = inlineTaskAnswersMarkup,
                    parseMode = MARKDOWN
                )

                it.status = SENT

                createNewEvent(it.task)
            }
        }
    })
}
