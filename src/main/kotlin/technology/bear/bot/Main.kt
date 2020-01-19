package technology.bear.bot


import com.natpryce.konfig.ConfigurationProperties.Companion.fromResource
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.dispatcher.message
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.KeyboardButton
import me.ivmg.telegram.extensions.filters.Filter
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import technology.bear.constans.CONFIG_FILE
import technology.bear.constans.TOKEN_KEY
import kotlin.concurrent.fixedRateTimer

fun main() {

    val bot = bot {
        token = fromResource(CONFIG_FILE)[Key(TOKEN_KEY, stringType)]
        logLevel = BASIC

        dispatch {

            //            command("start") { bot, update ->
//                val chatId = update.message?.chat?.id ?: return@command
//                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
//                bot.sendMessage(
//                    chatId = update.message!!.chat.id,
//                    text = "Дратути, выберите что хотите сделать",
//                    replyMarkup = keyboardMarkup
//                )
//            }
//
//            command("end") { bot, update ->
//                val chatId = update.message?.chat?.id ?: return@command
//                val inlineKeyboardMarkup = InlineKeyboardMarkup(generateButtons())
//                bot.sendMessage(chatId = chatId, text = "Hello, inline buttons!", replyMarkup = inlineKeyboardMarkup)
//            }

            message(Filter.Text) { bot, update ->
                val chatId = update.message?.chat?.id ?: return@message
                bot.sendMessage(chatId = chatId, text = "Записал!")
            }

            command("get") { bot, update ->

            }


            message(Filter.Custom { text.isNullOrBlank() }) { bot, update ->

            }


        }
    }
    bot.startPolling()

    fixedRateTimer(name = "timer", period = 5000, action = { bot.sendMessage(1L, "5 sec") })
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Request location (not supported on desktop)", requestLocation = true)),
        listOf(KeyboardButton("Request contact", requestContact = true)),
        listOf(KeyboardButton("Request contact", requestContact = true)),
        listOf(KeyboardButton("Request contact", requestContact = true)),
        listOf(KeyboardButton("Request contact", requestContact = true))
    )
}

fun generateButtons(): List<List<InlineKeyboardButton>> {
    return listOf(
        listOf(InlineKeyboardButton(text = "Test Inline Button", callbackData = "testButton")),
        listOf(InlineKeyboardButton(text = "Show alert", callbackData = "showAlert"))
    )
}
