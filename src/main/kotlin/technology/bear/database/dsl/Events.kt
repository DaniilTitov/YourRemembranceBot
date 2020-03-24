package technology.bear.database.dsl

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import technology.bear.constans.EventStatus

object Events : IdTable<Int>() {
    override val id: Column<EntityID<Int>> = reference("taskId", Tasks).primaryKey().uniqueIndex()
    val taskTime = datetime("taskTime")
    val status = enumerationByName("status", 8, EventStatus::class)
}
