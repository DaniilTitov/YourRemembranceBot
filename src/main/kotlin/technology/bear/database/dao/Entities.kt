package technology.bear.database.dao

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = Users.integer("id").autoIncrement().primaryKey()
    val name = Users.varchar("name", length = 50)
    val cityId = (Users.integer("city_id") references Cities.id).nullable()
}

object Cities : Table() {
    val id = Cities.integer("id").autoIncrement().primaryKey()
    val name = Cities.varchar("name", 50)
}
