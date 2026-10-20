package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Sale
import com.example.data.model.SaleItemParser
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.DateFilter
import com.example.ui.viewmodel.SaleSnapViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SalesListScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val sales by viewModel.sales.collectAsState()
  val searchQuery by viewModel.salesSearchQuery.collectAsState()
  val dateFilter by viewModel.salesDateFilter.collectAsState()
  val selectedSale by viewModel.selectedSaleDetail.collectAsState()

  var saleToEdit by remember { mutableStateOf<Sale?>(null) }
  var saleToDelete by remember { mutableStateOf<Sale?>(null) }

  // Filter sales based on search query and date filter
  val filteredSales = remember(sales, searchQuery, dateFilter) {
    val now = Calendar.getInstance()

    val todayStart = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val weekStart = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthStart = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    sales.filter { sale ->
      // Date filter
      val matchesDate = when (dateFilter) {
        DateFilter.ALL -> true
        DateFilter.TODAY -> sale.timestamp >= todayStart
        DateFilter.THIS_WEEK -> sale.timestamp >= weekStart
        DateFilter.THIS_MONTH -> sale.timestamp >= monthStart
      }

      // Search filter
      val matchesSearch = if (searchQuery.isBlank()) true else {
        val q = searchQuery.trim().lowercase()
        sale.customerName.lowercase().contains(q) ||
          sale.paymentMethod.lowercase().contains(q) ||
          sale.itemsJson.lowercase().contains(q) ||
          sale.id.toString().contains(q)
      }

      matchesDate && matchesSearch
    }
  }

  val totalFilteredRevenue = remember(filteredSales) {
    filteredSales.sumOf { it.totalAmount }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Sales Transactions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${filteredSales.size} recorded",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSalesSearchQuery(it) },
          placeholder = { Text("Search by customer, item, or ID...") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSalesSearchQuery("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sales_search_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Emerald500,
            unfocusedBorderColor = Color(0xFFE5E7EB)
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Date Filter Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (df in DateFilter.values()) {
            val isSelected = dateFilter == df
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setSalesDateFilter(df) },
              label = { Text(df.label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Emerald500,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("date_filter_${df.name}")
            )
          }
        }
      }
    }

    // Filtered Total Banner
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Emerald100.copy(alpha = 0.5f)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Filtered Total (${filteredSales.size} sales):",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Emerald700)
        )
        Text(
          text = "${settings.currencySymbol}%.2f".format(Locale.US, totalFilteredRevenue),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Amber700)
        )
      }
    }

    // Chronological Transactions List
    if (filteredSales.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No sales match your search or filter.",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredSales, key = { it.id }) { sale ->
          SalesListItemCard(
            sale = sale,
            currencySymbol = settings.currencySymbol,
            onClick = { viewModel.selectSaleForDetail(sale) }
          )
        }

        item {
          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }

  // View / Edit / Delete Sale Modal Dialog
  selectedSale?.let { sale ->
    SaleDetailModal(
      sale = sale,
      currencySymbol = settings.currencySymbol,
      onDismiss = { viewModel.selectSaleForDetail(null) },
      onEdit = {
        saleToEdit = sale
        viewModel.selectSaleForDetail(null)
      },
      onDelete = {
        saleToDelete = sale
        viewModel.selectSaleForDetail(null)
      }
    )
  }

  // Edit Sale Dialog
  saleToEdit?.let { sale ->
    EditSaleDialog(
      sale = sale,
      onDismiss = { saleToEdit = null },
      onSave = { updatedSale ->
        viewModel.updateSale(updatedSale)
        saleToEdit = null
      }
    )
  }

  // Delete Confirmation Alert
  saleToDelete?.let { sale ->
    AlertDialog(
      onDismissRequest = { saleToDelete = null },
      title = { Text("Delete Sale #${sale.id}?") },
      text = {
        Text("Are you sure you want to delete this sale for ${settings.currencySymbol}%.2f? This action cannot be undone.".format(Locale.US, sale.totalAmount))
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteSale(sale)
            saleToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_sale_button")
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { saleToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun SalesListItemCard(
  sale: Sale,
  currencySymbol: String,
  onClick: () -> Unit
) {
  val items = remember(sale.itemsJson) { SaleItemParser.fromJson(sale.itemsJson) }
  val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()) }
  val formattedDate = dateFormat.format(Date(sale.timestamp))
  val itemsSummary = remember(items) {
    items.joinToString(", ") { "${it.productName} (x${it.quantity})" }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("sale_item_card_${sale.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = when (sale.paymentMethod) {
            "Cash" -> Emerald100
            "Card" -> Color(0xFFDBEAFE)
            else -> Amber100
          },
          modifier = Modifier.size(42.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = when (sale.paymentMethod) {
                "Cash" -> Icons.Default.Payments
                "Card" -> Icons.Default.CreditCard
                else -> Icons.Default.ShoppingBag
              },
              contentDescription = null,
              tint = when (sale.paymentMethod) {
                "Cash" -> Emerald700
                "Card" -> Color(0xFF1D4ED8)
                else -> Amber700
              },
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFF3F4F6)
            ) {
              Text(
                text = "#${sale.id}",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = itemsSummary,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "$formattedDate • ${sale.paymentMethod}",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "$currencySymbol%.2f".format(Locale.US, sale.totalAmount),
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          color = Amber700
        )
      )
    }
  }
}

@Composable
fun SaleDetailModal(
  sale: Sale,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val items = remember(sale.itemsJson) { SaleItemParser.fromJson(sale.itemsJson) }
  val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy  hh:mm a", Locale.getDefault())
  val dateFormatted = dateFormat.format(Date(sale.timestamp))

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("sale_detail_modal"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transaction Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Emerald100
          ) {
            Text(
              text = "SALE #${sale.id}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Emerald700),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = dateFormatted, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        if (sale.customerName.isNotBlank()) {
          Text(text = "Customer: ${sale.customerName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
        Text(text = "Payment: ${sale.paymentMethod}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray))

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFE5E7EB))
        Spacer(modifier = Modifier.height(10.dp))

        // Items list
        for (item in items) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${item.productName} (x${item.quantity})",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "$currencySymbol%.2f".format(Locale.US, item.lineTotal),
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFE5E7EB))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Total Amount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Text(
            text = "$currencySymbol%.2f".format(Locale.US, sale.totalAmount),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Amber700)
          )
        }

        if (sale.notes.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Note: ${sale.notes}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Edit, Delete, Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onDelete,
            modifier = Modifier
              .weight(1f)
              .testTag("delete_sale_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
          }

          OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
              .weight(1f)
              .testTag("edit_sale_button")
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
          ) {
            Text("Close")
          }
        }
      }
    }
  }
}

@Composable
fun EditSaleDialog(
  sale: Sale,
  onDismiss: () -> Unit,
  onSave: (Sale) -> Unit
) {
  var customerName by remember { mutableStateOf(sale.customerName) }
  var paymentMethod by remember { mutableStateOf(sale.paymentMethod) }
  var notes by remember { mutableStateOf(sale.notes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Sale #${sale.id}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = customerName,
          onValueChange = { customerName = it },
          label = { Text("Customer Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Payment Method", style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          for (pm in listOf("Cash", "Card", "Mobile", "Transfer")) {
            FilterChip(
              selected = paymentMethod == pm,
              onClick = { paymentMethod = pm },
              label = { Text(pm) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            sale.copy(
              customerName = customerName.trim(),
              paymentMethod = paymentMethod,
              notes = notes.trim()
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
        modifier = Modifier.testTag("save_edited_sale_button")
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
