package technology.bear.database.dao

import org.jetbrains.exposed.sql.Table

object Tasks : Table() {
    val id = Tasks.integer("id").autoIncrement().primaryKey()
    val taskName = Tasks.varchar("taskName", length = 256)
    val taskFrequency = Tasks.varchar("taskFrequency", length = 32)
    val userId = Tasks.integer("userId")
}
