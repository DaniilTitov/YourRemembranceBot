package technology.bear.database.dsl

import org.jetbrains.exposed.dao.IntIdTable
import technology.bear.constans.EventStatus


object Events : IntIdTable() {
    val task = reference("taskId", Tasks)
    val taskTime = datetime("taskTime")
    val status = enumerationByName("status", 8, EventStatus::class)
}
