package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Events : IntIdTable() {
    val taskId = reference("taskId", Tasks)
    val taskName = Events.varchar("taskName", length = 256)
    val taskTime = Events.datetime("taskTime")
    val userId = Events.long("userId")
}

class Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Event>(Events)

    var taskId by Task referencedOn Events.taskId
    var taskName by Events.taskName
    var userId by Events.userId
    var taskTime by Events.taskTime
}
