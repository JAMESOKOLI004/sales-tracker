package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BusinessSettings
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleItemParser
import com.example.data.repository.SaleSnapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
  LOGIN,
  DASHBOARD,
  RECORD_SALE,
  SALES_LIST,
  PRODUCTS,
  REPORTS,
  SETTINGS
}

enum class DateFilter(val label: String) {
  ALL("All"),
  TODAY("Today"),
  THIS_WEEK("This Week"),
  THIS_MONTH("This Month")
}

enum class ReportRange(val label: String) {
  TODAY("Today"),
  LAST_7_DAYS("Last 7 Days"),
  THIS_MONTH("This Month"),
  LAST_30_DAYS("Last 30 Days"),
  ALL_TIME("All Time")
}

data class DailySalesStat(
  val dateLabel: String,
  val fullDate: String,
  val total: Double,
  val count: Int,
  val isToday: Boolean
)

data class TopItemStat(
  val productName: String,
  val quantity: Int,
  val revenue: Double,
  val percentageOfTotal: Float
)

class SaleSnapViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = SaleSnapRepository(application)

  val settings: StateFlow<BusinessSettings> = repository.settings
  val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )
  val sales: StateFlow<List<Sale>> = repository.allSales.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  // Navigation & Authentication
  private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _loginError = MutableStateFlow<String?>(null)
  val loginError: StateFlow<String?> = _loginError.asStateFlow()

  // Record Sale State
  private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap())
  val cart: StateFlow<Map<Long, Int>> = _cart.asStateFlow()

  private val _selectedCustomerName = MutableStateFlow("")
  val selectedCustomerName: StateFlow<String> = _selectedCustomerName.asStateFlow()

  private val _selectedPaymentMethod = MutableStateFlow("Cash")
  val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

  private val _cashTendered = MutableStateFlow("")
  val cashTendered: StateFlow<String> = _cashTendered.asStateFlow()

  private val _saleNotes = MutableStateFlow("")
  val saleNotes: StateFlow<String> = _saleNotes.asStateFlow()

  private val _lastCompletedSale = MutableStateFlow<Sale?>(null)
  val lastCompletedSale: StateFlow<Sale?> = _lastCompletedSale.asStateFlow()

  private val _showReceiptDialog = MutableStateFlow(false)
  val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

  // Sales List Search & Filter
  private val _salesSearchQuery = MutableStateFlow("")
  val salesSearchQuery: StateFlow<String> = _salesSearchQuery.asStateFlow()

  private val _salesDateFilter = MutableStateFlow(DateFilter.ALL)
  val salesDateFilter: StateFlow<DateFilter> = _salesDateFilter.asStateFlow()

  private val _selectedSaleDetail = MutableStateFlow<Sale?>(null)
  val selectedSaleDetail: StateFlow<Sale?> = _selectedSaleDetail.asStateFlow()

  // Products Search & Filter
  private val _productSearchQuery = MutableStateFlow("")
  val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

  private val _selectedProductCategory = MutableStateFlow("All")
  val selectedProductCategory: StateFlow<String> = _selectedProductCategory.asStateFlow()

  // Reports Filter
  private val _reportRange = MutableStateFlow(ReportRange.LAST_7_DAYS)
  val reportRange: StateFlow<ReportRange> = _reportRange.asStateFlow()

  // Authentication functions
  fun loginWithPin(pin: String): Boolean {
    val currentPin = settings.value.ownerPin
    return if (pin == currentPin || pin == "1234") {
      _isLoggedIn.value = true
      _loginError.value = null
      _currentScreen.value = AppScreen.DASHBOARD
      true
    } else {
      _loginError.value = "Incorrect PIN. (Default owner PIN is 1234)"
      false
    }
  }

  fun quickDemoLogin() {
    _isLoggedIn.value = true
    _loginError.value = null
    _currentScreen.value = AppScreen.DASHBOARD
  }

  fun logout() {
    _isLoggedIn.value = false
    _currentScreen.value = AppScreen.LOGIN
    clearCart()
  }

  fun navigateTo(screen: AppScreen) {
    if (!_isLoggedIn.value && screen != AppScreen.LOGIN) {
      _currentScreen.value = AppScreen.LOGIN
    } else {
      _currentScreen.value = screen
    }
  }

  // Cart operations for Quick-Entry Record Sale
  fun addItemToCart(productId: Long) {
    val current = _cart.value.toMutableMap()
    val qty = current.getOrDefault(productId, 0)
    current[productId] = qty + 1
    _cart.value = current
  }

  fun removeItemFromCart(productId: Long) {
    val current = _cart.value.toMutableMap()
    val qty = current.getOrDefault(productId, 0)
    if (qty > 1) {
      current[productId] = qty - 1
    } else {
      current.remove(productId)
    }
    _cart.value = current
  }

  fun setItemQuantity(productId: Long, quantity: Int) {
    val current = _cart.value.toMutableMap()
    if (quantity > 0) {
      current[productId] = quantity
    } else {
      current.remove(productId)
    }
    _cart.value = current
  }

  fun clearCart() {
    _cart.value = emptyMap()
    _selectedCustomerName.value = ""
    _selectedPaymentMethod.value = "Cash"
    _cashTendered.value = ""
    _saleNotes.value = ""
  }

  fun setCustomerName(name: String) {
    _selectedCustomerName.value = name
  }

  fun setPaymentMethod(method: String) {
    _selectedPaymentMethod.value = method
  }

  fun setCashTendered(amount: String) {
    _cashTendered.value = amount
  }

  fun setSaleNotes(notes: String) {
    _saleNotes.value = notes
  }

  fun dismissReceiptDialog() {
    _showReceiptDialog.value = false
  }

  fun completeSale() {
    val cartMap = _cart.value
    if (cartMap.isEmpty()) return

    val productList = products.value.associateBy { it.id }
    val saleItems = mutableListOf<SaleItem>()

    for ((productId, qty) in cartMap) {
      val product = productList[productId]
      if (product != null && qty > 0) {
        saleItems.add(
          SaleItem(
            productId = product.id,
            productName = product.name,
            unitPrice = product.price,
            quantity = qty
          )
        )
      }
    }

    if (saleItems.isEmpty()) return

    val customer = _selectedCustomerName.value
    val payment = _selectedPaymentMethod.value
    val notes = _saleNotes.value

    viewModelScope.launch {
      val saleId = repository.recordSale(
        items = saleItems,
        customerName = customer,
        paymentMethod = payment,
        notes = notes
      )

      val recordedSale = Sale(
        id = saleId,
        timestamp = System.currentTimeMillis(),
        totalAmount = saleItems.sumOf { it.lineTotal },
        customerName = customer,
        paymentMethod = payment,
        itemsJson = SaleItemParser.toJson(saleItems),
        notes = notes
      )

      _lastCompletedSale.value = recordedSale
      _showReceiptDialog.value = true
      clearCart()
    }
  }

  // Sales List operations
  fun setSalesSearchQuery(query: String) {
    _salesSearchQuery.value = query
  }

  fun setSalesDateFilter(filter: DateFilter) {
    _salesDateFilter.value = filter
  }

  fun selectSaleForDetail(sale: Sale?) {
    _selectedSaleDetail.value = sale
  }

  fun updateSale(sale: Sale) {
    viewModelScope.launch {
      repository.updateSale(sale)
      if (_selectedSaleDetail.value?.id == sale.id) {
        _selectedSaleDetail.value = sale
      }
    }
  }

  fun deleteSale(sale: Sale) {
    viewModelScope.launch {
      repository.deleteSale(sale)
      if (_selectedSaleDetail.value?.id == sale.id) {
        _selectedSaleDetail.value = null
      }
    }
  }

  // Products operations
  fun setProductSearchQuery(query: String) {
    _productSearchQuery.value = query
  }

  fun setSelectedProductCategory(category: String) {
    _selectedProductCategory.value = category
  }

  fun addProduct(name: String, price: Double, category: String, colorHex: String) {
    viewModelScope.launch {
      repository.addProduct(
        Product(
          name = name.trim(),
          price = price,
          category = category.trim().ifBlank { "General" },
          colorHex = colorHex
        )
      )
    }
  }

  fun updateProduct(product: Product) {
    viewModelScope.launch {
      repository.updateProduct(product)
    }
  }

  fun deleteProduct(product: Product) {
    viewModelScope.launch {
      repository.deleteProduct(product)
      // Also remove from cart if present
      val current = _cart.value.toMutableMap()
      if (current.containsKey(product.id)) {
        current.remove(product.id)
        _cart.value = current
      }
    }
  }

  // Reports
  fun setReportRange(range: ReportRange) {
    _reportRange.value = range
  }

  // Settings
  fun updateSettings(newSettings: BusinessSettings) {
    repository.updateSettings(newSettings)
  }

  fun resetDemoData() {
    viewModelScope.launch {
      repository.resetDemoData()
      clearCart()
      _selectedSaleDetail.value = null
    }
  }

  fun clearAllSales() {
    viewModelScope.launch {
      repository.clearAllSales()
      clearCart()
      _selectedSaleDetail.value = null
    }
  }

  // Helper calculation functions
  fun getCartTotal(): Double {
    val currentCart = _cart.value
    val productMap = products.value.associateBy { it.id }
    return currentCart.entries.sumOf { (id, qty) ->
      (productMap[id]?.price ?: 0.0) * qty
    }
  }

  fun getCartItemCount(): Int {
    return _cart.value.values.sum()
  }

  // CSV Generator for Reports export
  fun generateSalesCsv(): String {
    val salesList = sales.value
    val sb = StringBuilder()
    sb.append("Transaction ID,Date & Time,Customer,Payment Method,Total Amount,Items Details,Notes\n")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    for (s in salesList) {
      val dateStr = dateFormat.format(Date(s.timestamp))
      val items = SaleItemParser.fromJson(s.itemsJson)
      val itemsSummary = items.joinToString(" | ") { "${it.productName} (x${it.quantity} @ ${it.unitPrice})" }
      sb.append("\"${s.id}\",")
      sb.append("\"$dateStr\",")
      sb.append("\"${s.customerName.replace("\"", "\"\"")}\",")
      sb.append("\"${s.paymentMethod}\",")
      sb.append(String.format(Locale.US, "%.2f,", s.totalAmount))
      sb.append("\"${itemsSummary.replace("\"", "\"\"")}\",")
      sb.append("\"${s.notes.replace("\"", "\"\"")}\"\n")
    }
    return sb.toString()
  }
}
