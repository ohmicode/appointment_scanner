package my.tool.ind.scanner

import java.lang.Long.parseLong
import java.time.LocalDate

fun main(vararg args: String) {
    if (args.size > 1) {
        val date = LocalDate.parse(args[0])
        val interval = parseLong(args[1])
        println("Start searching IND for appointment before date: $date with interval $interval ms")
        Scanner(date, interval).start()
    } else {
        printHelp()
    }
}

private fun printHelp() =
    println(
        """
        usage: java -jar ind_scanner.jar <iso_date> <interval_in_ms>
        example: java -jar ind_scanner.jar 2022-07-25 5000
        """.trimIndent()
    )
