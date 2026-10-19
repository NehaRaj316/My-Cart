package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun currencyFormatting_formatsInRupees() {
    val formatted = com.example.util.CurrencyUtil.formatRupees(3499.0)
    assertTrue("Should start with rupee symbol", formatted.startsWith("₹"))
    assertEquals("₹3,499", formatted)

    val largeAmount = com.example.util.CurrencyUtil.formatRupees(68999.0)
    assertEquals("₹68,999", largeAmount)
  }
}
