package technology.bear.database.dsl

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object Statistics : IdTable<Int>() {
    override val id: Column<EntityID<Int>> = reference("taskId", Tasks).primaryKey().uniqueIndex()
    val completedCount = long("completedCount")
    val uncompletedCount = long("uncompletedCount")
}
