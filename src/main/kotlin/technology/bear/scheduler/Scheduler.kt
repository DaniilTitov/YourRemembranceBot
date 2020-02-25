package technology.bear.scheduler

import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import me.ivmg.telegram.entities.ParseMode.MARKDOWN
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import technology.bear.bot.message.generateAnswerButtons
import technology.bear.constans.EventStatus.ACTIVE
import technology.bear.constans.EventStatus.SENT
import technology.bear.constans.TaskFrequency.*
import technology.bear.constans.TaskFrequency.Companion.parseTaskFrequency
import technology.bear.database.dao.Event
import technology.bear.database.dao.Events
import kotlin.concurrent.fixedRateTimer

fun runScheduler(bot: Bot) {
    fixedRateTimer(period = 5000, action = {
        transaction {
            Event.find { Events.taskTime.less(DateTime.now()) and Events.status.eq(ACTIVE) }
                .forEach {
                    val inlineKeyboardMarkup = InlineKeyboardMarkup(generateAnswerButtons())

                    bot.sendMessage(
                        chatId = it.task.userId,
                        text = "Напоминаю о *" + it.task.taskName + "*\nПолучилось выполнить?",
                        replyMarkup = inlineKeyboardMarkup,
                        parseMode = MARKDOWN
                    )

                    it.status = SENT

                    when (parseTaskFrequency(it.task.taskFrequency)) {
                        ONCE_A_MINUTE -> {
                            Event.new {
                                this.task = it.task
                                this.taskTime = it.taskTime.plusMinutes(1)
                                this.status = ACTIVE
                            }
                        }
                        TWICE_A_MINUTE -> {
                            Event.new {
                                this.task = it.task
                                this.taskTime = it.taskTime.plusMinutes(2)
                                this.status = ACTIVE
                            }
                        }
                        THREE_TIMES_A_MINUTE -> {
                            Event.new {
                                this.task = it.task
                                this.taskTime = it.taskTime.plusMinutes(3)
                                this.status = ACTIVE
                            }
                        }
                    }

                }
        }
    })
}
