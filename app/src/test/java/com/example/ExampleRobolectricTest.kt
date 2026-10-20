package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SaleItem
import com.example.data.model.SaleItemParser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SaleSnap", appName)
  }

  @Test
  fun `sale item parser round trip`() {
    val items = listOf(
      SaleItem(productId = 1, productName = "Espresso", unitPrice = 3.50, quantity = 2),
      SaleItem(productId = 2, productName = "Croissant", unitPrice = 3.25, quantity = 1)
    )
    val json = SaleItemParser.toJson(items)
    val restored = SaleItemParser.fromJson(json)

    assertEquals(2, restored.size)
    assertEquals("Espresso", restored[0].productName)
    assertEquals(7.00, restored[0].lineTotal, 0.001)
    assertEquals(3.25, restored[1].lineTotal, 0.001)
  }
}
