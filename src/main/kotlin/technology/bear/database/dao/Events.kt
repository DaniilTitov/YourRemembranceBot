package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime.now
import technology.bear.constans.EventStatus
import technology.bear.constans.EventStatus.ACTIVE
import technology.bear.constans.TaskFrequency

object Events : IntIdTable() {
    val task = reference("taskId", Tasks)
    val taskTime = Events.datetime("taskTime")
    val status = enumerationByName("status", 8, EventStatus::class)
}

class Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Event>(Events)

    var task by Task referencedOn Events.task
    var taskTime by Events.taskTime
    var status by Events.status
}

fun createNewEvent(task: Task) {
    Event.new {
        this.task = task
        this.status = ACTIVE
        this.taskTime = when (TaskFrequency.parseTaskFrequency(task.taskFrequency)) {
            TaskFrequency.ONCE_A_MINUTE -> now().plusMinutes(1)
            TaskFrequency.TWICE_A_MINUTE -> now().plusMinutes(2)
            TaskFrequency.THREE_TIMES_A_MINUTE -> now().plusMinutes(3)
            null -> TODO()
        }
    }
}
