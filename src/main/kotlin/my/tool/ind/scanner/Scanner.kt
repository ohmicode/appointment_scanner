package my.tool.ind.scanner

import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate

const val IND_URL = "https://oap.ind.nl/oap/api/desks/AM/slots/?productKey=BIO"
const val TRASH_PREFIX = ")]}',"
const val MAX_PERSONS = 4
const val MIN_PERSONS = 1

class Scanner(private val desiredDate: LocalDate, private val intervalMs: Long) {

    var stopped: Boolean = false

    private val knownWindows = (MIN_PERSONS..MAX_PERSONS).associateWith { mutableSetOf<AppointmentWindow>() }

    fun start() {
        var persons = MAX_PERSONS
        var starCount = 0

        while (!stopped) {
            val spots = getSpots(persons)
            printStar(spots.data.isNotEmpty(), starCount++)

            val windows = spots.data.map { it.transform() }.filter { it.date < desiredDate.toString() }

            removeClosedWindows(knownWindows[persons]!!, windows)
            findNewWindows(knownWindows[persons]!!, windows)

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

    private fun removeClosedWindows(known: MutableSet<AppointmentWindow>, windows: List<AppointmentWindow>) {
        val closed = known.filterNot { windows.contains(it) }
        closed.map { buildClosedMessage(it) }.forEach { message ->
            TelegramBot().sendMessage(message).also { println(message) }
        }
        known.removeAll(closed)
    }

    private fun findNewWindows(known: MutableSet<AppointmentWindow>, windows: List<AppointmentWindow>) {
        val found = windows.filterNot { known.contains(it) }
        found.map { buildSuccessMessage(it) }.forEach { message ->
            TelegramBot().sendMessage(message).also { println(message) }
        }
        known.addAll(found)
    }

    private fun buildClosedMessage(window: AppointmentWindow) = """
        Window is closed for ${window.date} ${window.startTime} for ${window.parts} persons
    """.trimIndent()

    private fun buildSuccessMessage(window: AppointmentWindow) = """
        Found new free spot for ${window.parts} persons on ${window.date} ${window.startTime}, 
        do it fast https://oap.ind.nl/oap/en/#/BIO
    """.trimIndent()

    private fun printStar(hasData: Boolean, position: Int) {
        if (position % 80 == 0) println("")
        print(if (hasData) "*" else ".")
    }
}

fun String.prune() = if (startsWith(TRASH_PREFIX)) { drop(TRASH_PREFIX.length) } else { this }

data class IndResponse(val status: String, val data: List<IndData>)

data class IndData(val key: String, val date: String, val startTime: String, val endTime: String, val parts: Int)

data class AppointmentWindow(val date: String, val startTime: String, val parts: Int)

fun IndData.transform() = AppointmentWindow(date = date, startTime = startTime, parts = parts)
