package technology.bear.bot


import com.natpryce.konfig.ConfigurationProperties.Companion.fromResource
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import technology.bear.constans.CONFIG_FILE
import technology.bear.constans.TOKEN_KEY
import technology.bear.constans.UserState
import technology.bear.database.dao.Task
import technology.bear.database.initDatabase
import technology.bear.scheduler.runScheduler


fun main() {

    val config = fromResource(CONFIG_FILE)
    initDatabase(config)

    val userStates = hashMapOf<Long, UserState>()
    val currentUserTask = hashMapOf<Long, Task.Builder>()

    val bot = bot {
        token = config[Key(TOKEN_KEY, stringType)]
        logLevel = BASIC

        dispatch {
            handleStartCommand()
            handleAddingTask(userStates)
            handleAddingTaskFrequency(userStates, currentUserTask)
            handleSavingTask(userStates, currentUserTask)
            handleShowingTasks()
            handleRemovingTask()
            handleCallback()
        }
    }

    runScheduler(bot)

    bot.startPolling()
}
