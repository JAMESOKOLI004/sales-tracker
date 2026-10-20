package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.model.BusinessSettings
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleItemParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SaleSnapRepository(private val context: Context) {
  private val database = AppDatabase.getDatabase(context)
  private val productDao = database.productDao()
  private val saleDao = database.saleDao()
  private val prefs: SharedPreferences = context.getSharedPreferences("salesnap_prefs", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

  val allProducts: Flow<List<Product>> = productDao.getAllProducts()
  val allSales: Flow<List<Sale>> = saleDao.getAllSales()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      seedInitialDataIfNeeded()
    }
  }

  private fun loadSettings(): BusinessSettings {
    return BusinessSettings(
      businessName = prefs.getString("business_name", "SaleSnap Cafe & Market") ?: "SaleSnap Cafe & Market",
      currencySymbol = prefs.getString("currency_symbol", "$") ?: "$",
      ownerPin = prefs.getString("owner_pin", "1234") ?: "1234",
      receiptNote = prefs.getString("receipt_note", "Thank you for your business! Please visit us again.") ?: "Thank you for your business! Please visit us again.",
      phoneNumber = prefs.getString("phone_number", "+1 (555) 019-2834") ?: "+1 (555) 019-2834",
      address = prefs.getString("business_address", "104 Market St, Suite B") ?: "104 Market St, Suite B"
    )
  }

  fun updateSettings(newSettings: BusinessSettings) {
    prefs.edit()
      .putString("business_name", newSettings.businessName)
      .putString("currency_symbol", newSettings.currencySymbol)
      .putString("owner_pin", newSettings.ownerPin)
      .putString("receipt_note", newSettings.receiptNote)
      .putString("phone_number", newSettings.phoneNumber)
      .putString("business_address", newSettings.address)
      .apply()
    _settings.value = newSettings
  }

  suspend fun addProduct(product: Product): Long = withContext(Dispatchers.IO) {
    productDao.insertProduct(product)
  }

  suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
    productDao.updateProduct(product)
  }

  suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
    productDao.deleteProduct(product)
  }

  suspend fun recordSale(
    items: List<SaleItem>,
    customerName: String,
    paymentMethod: String,
    notes: String = "",
    customTimestamp: Long? = null
  ): Long = withContext(Dispatchers.IO) {
    val total = items.sumOf { it.lineTotal }
    val sale = Sale(
      timestamp = customTimestamp ?: System.currentTimeMillis(),
      totalAmount = total,
      customerName = customerName.trim(),
      paymentMethod = paymentMethod,
      itemsJson = SaleItemParser.toJson(items),
      notes = notes.trim()
    )
    saleDao.insertSale(sale)
  }

  suspend fun updateSale(sale: Sale) = withContext(Dispatchers.IO) {
    saleDao.updateSale(sale)
  }

  suspend fun deleteSale(sale: Sale) = withContext(Dispatchers.IO) {
    saleDao.deleteSale(sale)
  }

  suspend fun clearAllSales() = withContext(Dispatchers.IO) {
    saleDao.deleteAllSales()
  }

  suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
    if (productDao.countProducts() == 0) {
      seedDefaultProducts()
    }
    if (saleDao.countSales() == 0) {
      seedSampleSales()
    }
  }

  suspend fun resetDemoData() = withContext(Dispatchers.IO) {
    productDao.deleteAllProducts()
    saleDao.deleteAllSales()
    seedDefaultProducts()
    seedSampleSales()
  }

  private suspend fun seedDefaultProducts() {
    val sampleProducts = listOf(
      Product(name = "Espresso", price = 3.50, category = "Drinks", colorHex = "#10B981"),
      Product(name = "Iced Latte", price = 4.75, category = "Drinks", colorHex = "#10B981"),
      Product(name = "Matcha Green Tea", price = 5.00, category = "Drinks", colorHex = "#059669"),
      Product(name = "Cold Brew", price = 4.25, category = "Drinks", colorHex = "#047857"),
      Product(name = "Butter Croissant", price = 3.25, category = "Bakery", colorHex = "#F59E0B"),
      Product(name = "Chocolate Muffin", price = 3.75, category = "Bakery", colorHex = "#F59E0B"),
      Product(name = "Artisan Panini", price = 8.50, category = "Food", colorHex = "#D97706"),
      Product(name = "Avocado Toast", price = 7.00, category = "Food", colorHex = "#10B981"),
      Product(name = "Fresh Orange Juice", price = 4.50, category = "Drinks", colorHex = "#F59E0B"),
      Product(name = "Consulting (15m)", price = 25.00, category = "Services", colorHex = "#3B82F6"),
      Product(name = "Local Delivery", price = 5.00, category = "Services", colorHex = "#8B5CF6")
    )
    productDao.insertProducts(sampleProducts)
  }

  private suspend fun seedSampleSales() {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis
    val sales = mutableListOf<Sale>()

    fun dayAgo(days: Int, hour: Int, minute: Int): Long {
      val c = Calendar.getInstance()
      c.timeInMillis = now
      c.add(Calendar.DAY_OF_YEAR, -days)
      c.set(Calendar.HOUR_OF_DAY, hour)
      c.set(Calendar.MINUTE, minute)
      return c.timeInMillis
    }

    // Today's sales
    sales.add(
      Sale(
        timestamp = dayAgo(0, 8, 30),
        totalAmount = 8.25,
        customerName = "Sarah Jenkins",
        paymentMethod = "Card",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(1, "Espresso", 3.50, 1),
            SaleItem(5, "Iced Latte", 4.75, 1)
          )
        )
      )
    )
    sales.add(
      Sale(
        timestamp = dayAgo(0, 9, 45),
        totalAmount = 15.50,
        customerName = "David Chen",
        paymentMethod = "Cash",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(7, "Artisan Panini", 8.50, 1),
            SaleItem(8, "Avocado Toast", 7.00, 1)
          )
        )
      )
    )
    sales.add(
      Sale(
        timestamp = dayAgo(0, 11, 15),
        totalAmount = 29.50,
        customerName = "Alex Rivera",
        paymentMethod = "Card",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(10, "Consulting (15m)", 25.00, 1),
            SaleItem(9, "Fresh Orange Juice", 4.50, 1)
          )
        )
      )
    )
    sales.add(
      Sale(
        timestamp = dayAgo(0, 12, 40),
        totalAmount = 11.75,
        customerName = "Emma Watson",
        paymentMethod = "Mobile",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(2, "Iced Latte", 4.75, 1),
            SaleItem(6, "Chocolate Muffin", 3.75, 1),
            SaleItem(4, "Butter Croissant", 3.25, 1)
          )
        )
      )
    )

    // Yesterday's sales (Day 1 ago)
    sales.add(
      Sale(
        timestamp = dayAgo(1, 9, 10),
        totalAmount = 24.00,
        customerName = "Marcus Vance",
        paymentMethod = "Card",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(7, "Artisan Panini", 8.50, 2),
            SaleItem(8, "Avocado Toast", 7.00, 1)
          )
        )
      )
    )
    sales.add(
      Sale(
        timestamp = dayAgo(1, 14, 20),
        totalAmount = 18.25,
        customerName = "Rachel Green",
        paymentMethod = "Cash",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(3, "Matcha Green Tea", 5.00, 2),
            SaleItem(6, "Chocolate Muffin", 3.75, 1),
            SaleItem(11, "Local Delivery", 5.00, 1)
          )
        )
      )
    )

    // Day 2 ago
    sales.add(
      Sale(
        timestamp = dayAgo(2, 10, 0),
        totalAmount = 42.50,
        customerName = "Global Corp Catering",
        paymentMethod = "Transfer",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(5, "Butter Croissant", 3.25, 6),
            SaleItem(2, "Iced Latte", 4.75, 4),
            SaleItem(11, "Local Delivery", 5.00, 1)
          )
        )
      )
    )

    // Day 3 ago
    sales.add(
      Sale(
        timestamp = dayAgo(3, 11, 30),
        totalAmount = 31.00,
        customerName = "Liam Miller",
        paymentMethod = "Card",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(10, "Consulting (15m)", 25.00, 1),
            SaleItem(1, "Espresso", 3.50, 1),
            SaleItem(5, "Butter Croissant", 3.25, 1)
          )
        )
      )
    )

    // Day 4 ago
    sales.add(
      Sale(
        timestamp = dayAgo(4, 8, 50),
        totalAmount = 16.50,
        customerName = "Sophia Lee",
        paymentMethod = "Mobile",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(8, "Avocado Toast", 7.00, 1),
            SaleItem(2, "Iced Latte", 4.75, 2)
          )
        )
      )
    )

    // Day 5 ago
    sales.add(
      Sale(
        timestamp = dayAgo(5, 13, 15),
        totalAmount = 22.00,
        customerName = "Oliver Bennett",
        paymentMethod = "Cash",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(7, "Artisan Panini", 8.50, 2),
            SaleItem(11, "Local Delivery", 5.00, 1)
          )
        )
      )
    )

    // Day 6 ago
    sales.add(
      Sale(
        timestamp = dayAgo(6, 9, 30),
        totalAmount = 27.50,
        customerName = "Tech Startups Lab",
        paymentMethod = "Card",
        itemsJson = SaleItemParser.toJson(
          listOf(
            SaleItem(3, "Matcha Green Tea", 5.00, 3),
            SaleItem(6, "Chocolate Muffin", 3.75, 2),
            SaleItem(11, "Local Delivery", 5.00, 1)
          )
        )
      )
    )

    saleDao.insertSales(sales)
  }
}
