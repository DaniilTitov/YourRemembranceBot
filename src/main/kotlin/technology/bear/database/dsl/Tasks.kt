package technology.bear.database.dsl

import org.jetbrains.exposed.dao.IntIdTable

object Tasks : IntIdTable() {
    val taskName = Tasks.varchar("taskName", length = 256)
    val taskFrequency = Tasks.varchar("taskFrequency", length = 32)
    val userId = Tasks.long("userId")
}
