package org.mozilla.fenix

import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class DateTest {

    @Test
    fun `what is the date`() {
        println(Date())
        // Define the date
        val date: LocalDate = LocalDate.of(2025, 12, 1)
        val epochMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        println("POLLY $epochMillis")
    }

}