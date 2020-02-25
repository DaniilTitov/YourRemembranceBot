package technology.bear.database

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import technology.bear.constans.DB_PASSWORD
import technology.bear.constans.DB_USER
import technology.bear.constans.JDBC_DRIVER
import technology.bear.constans.JDBC_URL
import technology.bear.database.dao.Events
import technology.bear.database.dao.Tasks

fun initDatabase(config: ConfigurationProperties) {
    connectToDatabase(config)
    createTables()
}

fun connectToDatabase(config: ConfigurationProperties) {
    Database.connect(
        url = config[Key(JDBC_URL, stringType)],
        driver = config[Key(JDBC_DRIVER, stringType)],
        user = config[Key(DB_USER, stringType)],
        password = config[Key(DB_PASSWORD, stringType)]
    )
}

fun createTables() {
    transaction {
        create(
            Tasks,
            Events
        )
    }
}
