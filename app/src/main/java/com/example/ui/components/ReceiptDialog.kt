package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BusinessSettings
import com.example.data.model.Sale
import com.example.data.model.SaleItemParser
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
  sale: Sale,
  settings: BusinessSettings,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val items = SaleItemParser.fromJson(sale.itemsJson)
  val dateFormat = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(sale.timestamp))

  val receiptText = buildString {
    appendLine("======= ${settings.businessName.uppercase()} =======")
    if (settings.address.isNotBlank()) appendLine(settings.address)
    if (settings.phoneNumber.isNotBlank()) appendLine(settings.phoneNumber)
    appendLine("Date: $formattedDate")
    appendLine("Receipt #: SALE-${sale.id}")
    if (sale.customerName.isNotBlank()) appendLine("Customer: ${sale.customerName}")
    appendLine("Payment: ${sale.paymentMethod}")
    appendLine("----------------------------------------")
    for (item in items) {
      appendLine("${item.productName} x${item.quantity}  ${settings.currencySymbol}%.2f".format(Locale.US, item.lineTotal))
    }
    appendLine("----------------------------------------")
    appendLine("TOTAL: ${settings.currencySymbol}%.2f".format(Locale.US, sale.totalAmount))
    appendLine("----------------------------------------")
    appendLine(settings.receiptNote)
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("receipt_dialog_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top status
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Success",
              tint = Emerald500
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Sale Completed!",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Emerald600
              )
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_receipt_button")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Receipt Card (Paper Styled)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
          color = Color(0xFFFAFAFA),
          shape = RoundedCornerShape(8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = settings.businessName,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              textAlign = TextAlign.Center
            )
            if (settings.address.isNotBlank()) {
              Text(
                text = settings.address,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                textAlign = TextAlign.Center
              )
            }
            if (settings.phoneNumber.isNotBlank()) {
              Text(
                text = settings.phoneNumber,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                textAlign = TextAlign.Center
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Receipt #:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
              Text("SALE-${sale.id}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Date:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
              Text(formattedDate, style = MaterialTheme.typography.labelSmall)
            }
            if (sale.customerName.isNotBlank()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Customer:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(sale.customerName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Payment:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
              Text(sale.paymentMethod, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Items list
            for (item in items) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                  )
                  Text(
                    text = "${item.quantity} x ${settings.currencySymbol}%.2f".format(Locale.US, item.unitPrice),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                  )
                }
                Text(
                  text = "${settings.currencySymbol}%.2f".format(Locale.US, item.lineTotal),
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TOTAL",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${settings.currencySymbol}%.2f".format(Locale.US, sale.totalAmount),
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Amber500
                )
              )
            }

            if (settings.receiptNote.isNotBlank()) {
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = settings.receiptNote,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                  color = Color.Gray
                ),
                textAlign = TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, receiptText)
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Receipt")
              context.startActivity(shareIntent)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_receipt_button")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("Share")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("new_sale_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
          ) {
            Text("Done")
          }
        }
      }
    }
  }
}
