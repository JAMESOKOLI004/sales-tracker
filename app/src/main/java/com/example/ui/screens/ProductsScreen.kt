package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.SaleSnapViewModel
import java.util.Locale

@Composable
fun ProductsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val products by viewModel.products.collectAsState()
  val searchQuery by viewModel.productSearchQuery.collectAsState()
  val selectedCategory by viewModel.selectedProductCategory.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  var productToEdit by remember { mutableStateOf<Product?>(null) }
  var productToDelete by remember { mutableStateOf<Product?>(null) }

  val categories = remember(products) {
    listOf("All") + products.map { it.category }.distinct()
  }

  val filteredProducts = remember(products, searchQuery, selectedCategory) {
    products.filter { p ->
      val matchesCat = selectedCategory == "All" || p.category == selectedCategory
      val matchesSearch = if (searchQuery.isBlank()) true else {
        p.name.lowercase().contains(searchQuery.trim().lowercase()) ||
          p.category.lowercase().contains(searchQuery.trim().lowercase())
      }
      matchesCat && matchesSearch
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = Emerald500,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_product_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      // Header
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
            Column {
              Text(
                text = "Products & Services",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${products.size} items in catalog",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
              )
            }

            // "+ Add Item" prominent header button
            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("add_item_top_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ Add Item", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Search Bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setProductSearchQuery(it) },
            placeholder = { Text("Search products or services...") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.setProductSearchQuery("") }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("product_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Emerald500,
              unfocusedBorderColor = Color(0xFFE5E7EB)
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Category Chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            for (cat in categories) {
              val isSelected = cat == selectedCategory
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setSelectedProductCategory(cat) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Emerald500,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }

      // Products List
      if (filteredProducts.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              tint = Color.LightGray,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No items found.",
              style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
              Text("Add Your First Item")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredProducts, key = { it.id }) { product ->
            ProductListItemCard(
              product = product,
              currencySymbol = settings.currencySymbol,
              onEdit = { productToEdit = product },
              onDelete = { productToDelete = product }
            )
          }

          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    }
  }

  // Add Item Dialog
  if (showAddDialog) {
    AddEditProductDialog(
      initialProduct = null,
      currencySymbol = settings.currencySymbol,
      onDismiss = { showAddDialog = false },
      onSave = { name, price, category, colorHex ->
        viewModel.addProduct(name, price, category, colorHex)
        showAddDialog = false
      }
    )
  }

  // Edit Item Dialog
  productToEdit?.let { product ->
    AddEditProductDialog(
      initialProduct = product,
      currencySymbol = settings.currencySymbol,
      onDismiss = { productToEdit = null },
      onSave = { name, price, category, colorHex ->
        viewModel.updateProduct(
          product.copy(
            name = name,
            price = price,
            category = category,
            colorHex = colorHex
          )
        )
        productToEdit = null
      }
    )
  }

  // Delete Item Confirmation
  productToDelete?.let { product ->
    AlertDialog(
      onDismissRequest = { productToDelete = null },
      title = { Text("Delete '${product.name}'?") },
      text = { Text("Are you sure you want to remove this item from your catalog?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteProduct(product)
            productToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { productToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ProductListItemCard(
  product: Product,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("product_list_row_${product.id}"),
    shape = RoundedCornerShape(12.dp),
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
          color = Emerald100,
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = product.name.take(1).uppercase(),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald700)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = product.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
          )
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFF3F4F6)
          ) {
            Text(
              text = product.category,
              style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "$currencySymbol%.2f".format(Locale.US, product.price),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Emerald700
          )
        )

        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun AddEditProductDialog(
  initialProduct: Product?,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onSave: (name: String, price: Double, category: String, colorHex: String) -> Unit
) {
  var name by remember { mutableStateOf(initialProduct?.name ?: "") }
  var priceStr by remember { mutableStateOf(if (initialProduct != null) "%.2f".format(Locale.US, initialProduct.price) else "") }
  var category by remember { mutableStateOf(initialProduct?.category ?: "Drinks") }
  val colorHex = initialProduct?.colorHex ?: "#10B981"
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val defaultCategories = listOf("Drinks", "Bakery", "Food", "Services", "Retail", "General")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initialProduct == null) "+ Add New Item" else "Edit Item") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorMessage = null
          },
          label = { Text("Item Name *") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("item_name_input"),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        OutlinedTextField(
          value = priceStr,
          onValueChange = {
            priceStr = it
            errorMessage = null
          },
          label = { Text("Price ($currencySymbol) *") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("item_price_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Text("Category", style = MaterialTheme.typography.labelSmall)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          for (cat in defaultCategories) {
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat) }
            )
          }
        }

        errorMessage?.let {
          Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmedName = name.trim()
          val parsedPrice = priceStr.toDoubleOrNull()
          if (trimmedName.isBlank()) {
            errorMessage = "Please enter an item name."
            return@Button
          }
          if (parsedPrice == null || parsedPrice <= 0) {
            errorMessage = "Please enter a valid price."
            return@Button
          }
          onSave(trimmedName, parsedPrice, category, colorHex)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
        modifier = Modifier.testTag("save_item_button")
      ) {
        Text("Save Item")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
