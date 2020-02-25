package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import technology.bear.constans.EventStatus

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
