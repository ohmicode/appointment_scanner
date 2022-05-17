package my.tool.ind.scanner

import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder

const val TOKEN = "5199070249:AAEWs5nHL7b21tA2U5ae-YGgmg7YBYRmgWY"
const val CHAT_ID = "156336064"

// HOWTO get token & chatId
// Inside Telegram go to bot @BotFather
// create a bot with /newbot (get token here)
// start new chat with that bot
// curl 'https://api.telegram.org/bot$token/getUpdates'
// Chat id will be in response JSON
class TelegramBot {

    fun sendMessage(message: String) {
        val urlEncodedMessage = URLEncoder.encode(message, "UTF-8")
        val url = "https://api.telegram.org/bot$TOKEN/sendMessage?chat_id=$CHAT_ID&text=$urlEncodedMessage"
        val input = URL(url).openStream()
        val reader = InputStreamReader(input, "UTF-8")
        reader.readText()
        reader.close()
        input.close()
    }
}
