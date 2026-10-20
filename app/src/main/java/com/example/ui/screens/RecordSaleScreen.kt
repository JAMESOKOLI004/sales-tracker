package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.SaleSnapViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordSaleScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val products by viewModel.products.collectAsState()
  val cart by viewModel.cart.collectAsState()
  val customerName by viewModel.selectedCustomerName.collectAsState()
  val paymentMethod by viewModel.selectedPaymentMethod.collectAsState()
  val cashTendered by viewModel.cashTendered.collectAsState()
  val saleNotes by viewModel.saleNotes.collectAsState()

  var selectedCategory by remember { mutableStateOf("All") }
  var isCartTrayExpanded by remember { mutableStateOf(false) }

  val categories = remember(products) {
    listOf("All") + products.map { it.category }.distinct()
  }

  val filteredProducts = remember(products, selectedCategory) {
    if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
  }

  val cartTotal = remember(cart, products) { viewModel.getCartTotal() }
  val cartCount = remember(cart) { viewModel.getCartItemCount() }

  val productMap = remember(products) { products.associateBy { it.id } }

  val tenderedAmount = cashTendered.toDoubleOrNull() ?: 0.0
  val changeDue = if (tenderedAmount >= cartTotal && cartTotal > 0) tenderedAmount - cartTotal else 0.0

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
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Record Sale",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Tap item to add to sale",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )
        }

        if (cart.isNotEmpty()) {
          TextButton(
            onClick = { viewModel.clearCart() },
            modifier = Modifier.testTag("clear_cart_button")
          ) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear", color = Color.Gray)
          }
        }
      }
    }

    // Category Filter Chips (Horizontal Scroll)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      for (cat in categories) {
        val isSelected = cat == selectedCategory
        FilterChip(
          selected = isSelected,
          onClick = { selectedCategory = cat },
          label = { Text(cat) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Emerald500,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("filter_cat_$cat")
        )
      }
    }

    // Main Grid of Products/Services
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (filteredProducts.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No products found in this category. You can add items in the Products tab.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
          contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredProducts, key = { it.id }) { product ->
            val qty = cart.getOrDefault(product.id, 0)
            QuickEntryProductCard(
              product = product,
              currencySymbol = settings.currencySymbol,
              cartQuantity = qty,
              onAdd = { viewModel.addItemToCart(product.id) },
              onRemove = { viewModel.removeItemFromCart(product.id) }
            )
          }
        }
      }
    }

    // Running Total & Complete Sale Bottom Sheet / Drawer
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("cart_bottom_tray"),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 16.dp,
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // Handle bar & Toggle Expand
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isCartTrayExpanded = !isCartTrayExpanded }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ShoppingCart,
              contentDescription = null,
              tint = if (cartCount > 0) Emerald600 else Color.Gray,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "$cartCount items selected",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (isCartTrayExpanded) "Hide details" else "Order details",
              style = MaterialTheme.typography.bodySmall.copy(color = Emerald600)
            )
            Icon(
              imageVector = if (isCartTrayExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
              contentDescription = null,
              tint = Emerald600
            )
          }
        }

        // Expanded Cart Details (Customer, Payment Method, Item Steppers, Notes)
        AnimatedVisibility(visible = isCartTrayExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .verticalScroll(rememberScrollState())
              .padding(top = 8.dp, bottom = 8.dp)
          ) {
            // Item list with steppers
            Text(
              text = "Cart Items",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (cart.isEmpty()) {
              Text(
                text = "Cart is empty. Tap products above to add them.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.padding(vertical = 8.dp)
              )
            } else {
              for ((productId, qty) in cart) {
                val prod = productMap[productId]
                if (prod != null) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = prod.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${settings.currencySymbol}%.2f each".format(Locale.US, prod.price),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                      )
                    }

                    // Stepper: - / qty / +
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.size(32.dp),
                        onClick = { viewModel.removeItemFromCart(prod.id) }
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
                        }
                      }

                      Text(
                        text = "$qty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp)
                      )

                      Surface(
                        shape = CircleShape,
                        color = Emerald100,
                        modifier = Modifier.size(32.dp),
                        onClick = { viewModel.addItemToCart(prod.id) }
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(imageVector = Icons.Default.Add, contentDescription = "Increment", tint = Emerald700, modifier = Modifier.size(16.dp))
                        }
                      }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                      text = "${settings.currencySymbol}%.2f".format(Locale.US, prod.price * qty),
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))

            // Customer Name
            OutlinedTextField(
              value = customerName,
              onValueChange = { viewModel.setCustomerName(it) },
              label = { Text("Customer Name (Optional)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_name_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald500,
                focusedLabelColor = Emerald500
              ),
              shape = RoundedCornerShape(10.dp),
              keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Method
            Text(
              text = "Payment Method",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val paymentMethods = listOf("Cash", "Card", "Mobile", "Transfer")
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              for (method in paymentMethods) {
                val isSelected = paymentMethod == method
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) Emerald500 else Color(0xFFF3F4F6),
                  modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { viewModel.setPaymentMethod(method) }
                    .testTag("payment_method_$method")
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = method,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.Black
                      )
                    )
                  }
                }
              }
            }

            // Quick Cash Tender Calculator (if Cash is selected)
            if (paymentMethod == "Cash" && cartTotal > 0) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Quick Cash Tendered",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
              )
              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val tenderPresets = listOf(
                  "Exact" to cartTotal,
                  "${settings.currencySymbol}10" to 10.0,
                  "${settings.currencySymbol}20" to 20.0,
                  "${settings.currencySymbol}50" to 50.0
                )
                for ((label, amt) in tenderPresets) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Amber100,
                    modifier = Modifier
                      .weight(1f)
                      .height(34.dp)
                      .clickable { viewModel.setCashTendered("%.2f".format(Locale.US, amt)) }
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Amber700)
                      )
                    }
                  }
                }
              }

              if (changeDue > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Change to Return:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Emerald700)
                  )
                  Text(
                    text = "${settings.currencySymbol}%.2f".format(Locale.US, changeDue),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Emerald600)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
          }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Running Total & "Complete Sale" Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "RUNNING TOTAL",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color.Gray
              )
            )
            // Accent Amber for totals
            Text(
              text = "${settings.currencySymbol}%.2f".format(Locale.US, cartTotal),
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Amber500
              ),
              modifier = Modifier.testTag("running_total_text")
            )
          }

          Button(
            onClick = { viewModel.completeSale() },
            enabled = cart.isNotEmpty(),
            modifier = Modifier
              .height(54.dp)
              .testTag("complete_sale_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Emerald500,
              disabledContainerColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Complete Sale",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickEntryProductCard(
  product: Product,
  currencySymbol: String,
  cartQuantity: Int,
  onAdd: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isInCart = cartQuantity > 0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onAdd)
      .border(
        width = if (isInCart) 2.dp else 1.dp,
        color = if (isInCart) Emerald500 else Color(0xFFE5E7EB),
        shape = RoundedCornerShape(16.dp)
      )
      .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isInCart) Emerald100.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 3.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        // Category Pill
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFF3F4F6)
        ) {
          Text(
            text = product.category,
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.Gray,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        // Quantity Badge (if in cart)
        if (isInCart) {
          Surface(
            shape = CircleShape,
            color = Emerald500,
            modifier = Modifier.size(26.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$cartQuantity",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "$currencySymbol%.2f".format(Locale.US, product.price),
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Emerald700
          )
        )

        // Mini plus or stepper controls
        if (isInCart) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = Color.White,
              modifier = Modifier.size(28.dp),
              onClick = onRemove
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Remove,
                  contentDescription = "Remove",
                  tint = Emerald700,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Surface(
              shape = CircleShape,
              color = Emerald500,
              modifier = Modifier.size(28.dp),
              onClick = onAdd
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        } else {
          Surface(
            shape = CircleShape,
            color = Emerald100,
            modifier = Modifier.size(28.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add to cart",
                tint = Emerald700,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
