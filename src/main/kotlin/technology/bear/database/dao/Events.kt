package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Events : IntIdTable() {
    val task = reference("taskId", Tasks)
    val taskTime = Events.datetime("taskTime")
    val userId = Events.long("userId")
    val status = Events.varchar("status", 16)
}

class Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Event>(Events)

    var task by Task referencedOn Events.task
    var userId by Events.userId
    var taskTime by Events.taskTime
    var status by Events.status
}
