package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BusinessSettings
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.SaleSnapViewModel

@Composable
fun SettingsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()

  var businessName by remember(settings) { mutableStateOf(settings.businessName) }
  var currencySymbol by remember(settings) { mutableStateOf(settings.currencySymbol) }
  var phone by remember(settings) { mutableStateOf(settings.phoneNumber) }
  var address by remember(settings) { mutableStateOf(settings.address) }
  var receiptNote by remember(settings) { mutableStateOf(settings.receiptNote) }

  var showPinDialog by remember { mutableStateOf(false) }
  var showResetDemoDialog by remember { mutableStateOf(false) }
  var showClearSalesDialog by remember { mutableStateOf(false) }

  val currencies = listOf(
    "$" to "USD / CAD ($)",
    "€" to "Euro (€)",
    "£" to "British Pound (£)",
    "₦" to "Nigerian Naira (₦)",
    "¥" to "Japanese Yen (¥)",
    "₹" to "Indian Rupee (₹)",
    "R" to "South African Rand (R)",
    "A$" to "Australian Dollar (A$)"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Settings",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "Configure business receipts, currency & owner passcode",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
      )
    }

    // Business Name & Logo for Receipts
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("business_info_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = Emerald600)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Business Name & Logo for Receipts",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Logo Preview & Brand Badge
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
              .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Emerald500,
              modifier = Modifier.size(54.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.PointOfSale,
                  contentDescription = "Logo",
                  tint = Color.White,
                  modifier = Modifier.size(32.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Official Receipt Logo",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Included on digital and printed customer receipts",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name *") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_business_name_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald500)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Store Address / Location") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald500)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Business Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald500)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = receiptNote,
            onValueChange = { receiptNote = it },
            label = { Text("Receipt Footer Note") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald500)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              viewModel.updateSettings(
                settings.copy(
                  businessName = businessName.trim().ifBlank { "SaleSnap Store" },
                  address = address.trim(),
                  phoneNumber = phone.trim(),
                  receiptNote = receiptNote.trim()
                )
              )
              Toast.makeText(context, "Business details saved!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_business_settings_button")
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Receipt Info")
          }
        }
      }
    }

    // Currency Selector
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("currency_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = Amber500)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Currency Symbol",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Active Currency: ${settings.currencySymbol}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Amber700)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Grid of Currency choices
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((symbol, label) in currencies) {
              val isSelected = currencySymbol == symbol
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Amber100 else Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) Amber500 else Color(0xFFE5E7EB)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    currencySymbol = symbol
                    viewModel.updateSettings(settings.copy(currencySymbol = symbol))
                    Toast.makeText(context, "Currency updated to $symbol", Toast.LENGTH_SHORT).show()
                  }
                  .testTag("currency_option_$symbol")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  )
                  if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Amber700, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    // Security & Owner PIN
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("security_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Emerald600)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Owner Passcode & Lock",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Protect sales totals and business settings with your 4-digit PIN.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showPinDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("change_pin_button")
            ) {
              Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Change PIN")
            }

            Button(
              onClick = { viewModel.logout() },
              colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
              modifier = Modifier
                .weight(1f)
                .testTag("lock_app_now_button")
            ) {
              Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lock App")
            }
          }
        }
      }
    }

    // Demo Data & Maintenance
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("maintenance_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showResetDemoDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("reset_demo_data_button")
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reset Demo Data")
            }

            OutlinedButton(
              onClick = { showClearSalesDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("clear_sales_button"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear Sales")
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  // Change PIN Dialog
  if (showPinDialog) {
    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showPinDialog = false },
      title = { Text("Change Owner Passcode") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Enter new 4-digit PIN for business owner login:")
          OutlinedTextField(
            value = newPin,
            onValueChange = {
              if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                newPin = it
                pinError = null
              }
            },
            label = { Text("4-Digit PIN") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("new_pin_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
          )
          pinError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPin.length == 4) {
              viewModel.updateSettings(settings.copy(ownerPin = newPin))
              Toast.makeText(context, "PIN successfully updated!", Toast.LENGTH_SHORT).show()
              showPinDialog = false
            } else {
              pinError = "PIN must be exactly 4 digits."
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
          modifier = Modifier.testTag("save_pin_button")
        ) {
          Text("Save PIN")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPinDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Reset Demo Dialog
  if (showResetDemoDialog) {
    AlertDialog(
      onDismissRequest = { showResetDemoDialog = false },
      title = { Text("Reset Demo Data?") },
      text = { Text("This will reload the initial sample products and 7-day sales records.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetDemoData()
            Toast.makeText(context, "Demo data refreshed!", Toast.LENGTH_SHORT).show()
            showResetDemoDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
        ) {
          Text("Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDemoDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Clear All Sales Dialog
  if (showClearSalesDialog) {
    AlertDialog(
      onDismissRequest = { showClearSalesDialog = false },
      title = { Text("Clear All Sales History?") },
      text = { Text("This will delete all sales transactions. Your product catalog will be kept.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllSales()
            Toast.makeText(context, "All sales cleared.", Toast.LENGTH_SHORT).show()
            showClearSalesDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearSalesDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
