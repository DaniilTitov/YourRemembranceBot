package technology.bear.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import technology.bear.database.dao.Cities
import technology.bear.database.dao.Tasks

fun initDatabase() {
    connectToDatabase()
    createTables()
}

fun connectToDatabase() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/schema",
        driver = "org.postgresql.Driver",
        user = "",
        password = ""
    )
}

fun createTables() {
    transaction {
        SchemaUtils.create(
            Tasks
        )
    }
}