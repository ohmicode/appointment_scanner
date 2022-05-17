package my.tool.ind.scanner

import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate

const val IND_URL = "https://oap.ind.nl/oap/api/desks/AM/slots/?productKey=BIO"
const val TRASH_PREFIX = ")]}',"
const val MAX_PERSONS = 4
const val MIN_PERSONS = 2

class Scanner(private val date: LocalDate, private val intervalMs: Long) {

    var stopped: Boolean = false

    fun start() {
        var persons = MAX_PERSONS
        while (!stopped) {
            print("*")

            val spots = getSpots(persons)
            val minDate = extractMinDate(spots.data)
            if (minDate < date.toString()) {
                TelegramBot().sendMessage(buildSuccessMessage(minDate, persons))
                println("\nFound new free spot for $persons persons on $minDate, message sent")
                stopped = true
            }

            persons = if (persons <= MIN_PERSONS) MAX_PERSONS else persons - 1
            Thread.sleep(intervalMs)
        }
    }

    private fun getSpots(persons: Int): IndResponse {
        val input = URL("$IND_URL&persons=$persons").openStream()
        val reader = InputStreamReader(input, "UTF-8")
        val json = reader.readText().prune()
        reader.close()
        input.close()
        return Gson().fromJson(json, IndResponse::class.java)
    }

    private fun extractMinDate(spots: List<IndData>) = spots.minOf { it.date }

    private fun buildSuccessMessage(date: String, persons: Int) = """
        Found new free spot for $persons persons on $date, 
        do it fast https://oap.ind.nl/oap/en/#/BIO
    """.trimIndent()
}

fun String.prune() = if (startsWith(TRASH_PREFIX)) { drop(TRASH_PREFIX.length) } else { this }

data class IndResponse(val status: String, val data: List<IndData>)

data class IndData(val key: String, val date: String, val startTime: String, val endTime: String, val parts: Int)
