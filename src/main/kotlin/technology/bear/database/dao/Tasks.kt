package technology.bear.database.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import kotlin.properties.Delegates.notNull

object Tasks : IntIdTable() {
    val taskName = Tasks.varchar("taskName", length = 256)
    val taskFrequency = Tasks.varchar("taskFrequency", length = 32)
    val userId = Tasks.long("userId")
}

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
}
