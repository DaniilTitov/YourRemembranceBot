package technology.bear.database.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import technology.bear.database.dsl.Statistics

class Statistic(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Statistic>(Statistics)

    var task by Task referencedOn Statistics.id
    var completedCount by Statistics.completedCount
    var uncompletedCount by Statistics.uncompletedCount
}
