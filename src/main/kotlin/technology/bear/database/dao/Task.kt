package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.joda.time.DateTime
import technology.bear.constans.EventStatus.ACTIVE
import technology.bear.constans.TaskFrequency
import technology.bear.constans.TaskFrequency.*
import technology.bear.database.dsl.Tasks
import kotlin.properties.Delegates.notNull

class Task(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Task>(Tasks)

    var taskName by Tasks.taskName
    var taskFrequency by Tasks.taskFrequency
    var userId by Tasks.userId

    class Builder {
        private lateinit var taskName: String
        private lateinit var taskFrequency: String
        private var userId: Long by notNull()

        fun taskName(taskName: String) = apply { this.taskName = taskName }
        fun taskFrequency(taskFrequency: String) = apply { this.taskFrequency = taskFrequency }
        fun userId(userId: Long) = apply { this.userId = userId }
        fun newTask() = Task.new {
            taskName = this@Builder.taskName
            taskFrequency = this@Builder.taskFrequency
            userId = this@Builder.userId
        }
    }

    fun createNewEvent(now: DateTime) {
        Event.new {
            this.task = this@Task
            this.status = ACTIVE
            this.taskTime = when (TaskFrequency.parseTaskFrequency(this@Task.taskFrequency)) {
                EVERY_DAY -> now.plusDays(1)
                EVERY_TWO_DAYS -> now.plusDays(2)
                EVERY_WEEK -> now.plusWeeks(1)
                EVERY_MONTH -> now.plusMonths(1)
                null -> now
            }
        }
    }

    fun createNewStatistic() {
        Statistic.new {
            this.task = this@Task
            this.completedCount = 0
            this.uncompletedCount = 0
        }
    }
}
