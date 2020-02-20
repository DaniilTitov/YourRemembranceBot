package technology.bear.scheduler

import me.ivmg.telegram.Bot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import technology.bear.constans.EventStatus
import technology.bear.database.dao.Event
import technology.bear.database.dao.Events
import kotlin.concurrent.fixedRateTimer

fun runScheduler(bot: Bot) {
    fixedRateTimer(period = 5000, action = {
        transaction {
            Event.find { Events.taskTime.less(DateTime.now()) and Events.status.eq(EventStatus.ACTIVE.toString()) }
                .forEach {
                    bot.sendMessage(chatId = it.task.userId, text = "Напоминаю о " + it.task.taskName)
                    it.status = EventStatus.SENT.toString()
                    Event.new {
                        this.task = it.task
                        this.taskTime = it.taskTime.plusMinutes(1)
                        this.status = EventStatus.ACTIVE.toString()
                    }
                }
        }
    })
}
