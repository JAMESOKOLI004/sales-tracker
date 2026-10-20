package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sale
import com.example.data.model.SaleItemParser
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.DailySalesStat
import com.example.ui.viewmodel.ReportRange
import com.example.ui.viewmodel.SaleSnapViewModel
import com.example.ui.viewmodel.TopItemStat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val sales by viewModel.sales.collectAsState()
  val selectedRange by viewModel.reportRange.collectAsState()

  var showExportDialog by remember { mutableStateOf(false) }

  // Filter sales based on selected range
  val rangeStart = remember(selectedRange) {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    when (selectedRange) {
      ReportRange.TODAY -> c.timeInMillis
      ReportRange.LAST_7_DAYS -> {
        c.add(Calendar.DAY_OF_YEAR, -6)
        c.timeInMillis
      }
      ReportRange.THIS_MONTH -> {
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.timeInMillis
      }
      ReportRange.LAST_30_DAYS -> {
        c.add(Calendar.DAY_OF_YEAR, -29)
        c.timeInMillis
      }
      ReportRange.ALL_TIME -> 0L
    }
  }

  val filteredSales = remember(sales, rangeStart) {
    sales.filter { it.timestamp >= rangeStart }
  }

  val totalRevenue = remember(filteredSales) { filteredSales.sumOf { it.totalAmount } }
  val totalOrders = filteredSales.size
  val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

  // Top Selling Items
  val topItems = remember(filteredSales) {
    val mapQty = mutableMapOf<String, Int>()
    val mapRev = mutableMapOf<String, Double>()

    for (s in filteredSales) {
      val items = SaleItemParser.fromJson(s.itemsJson)
      for (item in items) {
        val q = mapQty.getOrDefault(item.productName, 0) + item.quantity
        mapQty[item.productName] = q
        val r = mapRev.getOrDefault(item.productName, 0.0) + item.lineTotal
        mapRev[item.productName] = r
      }
    }

    val maxRev = mapRev.values.maxOrNull() ?: 1.0
    mapQty.entries.map { (name, qty) ->
      val rev = mapRev[name] ?: 0.0
      TopItemStat(
        productName = name,
        quantity = qty,
        revenue = rev,
        percentageOfTotal = (rev / maxRev).toFloat().coerceIn(0.05f, 1f)
      )
    }.sortedByDescending { it.revenue }.take(5)
  }

  // Sales by Day (last 7 days or matching days)
  val salesByDay = remember(filteredSales, selectedRange) {
    val dayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val shortDay = SimpleDateFormat("EEE", Locale.getDefault())
    val daysCount = when (selectedRange) {
      ReportRange.TODAY -> 1
      ReportRange.LAST_7_DAYS -> 7
      ReportRange.THIS_MONTH, ReportRange.LAST_30_DAYS -> 14
      ReportRange.ALL_TIME -> 7
    }

    val list = mutableListOf<DailySalesStat>()
    val now = Calendar.getInstance()

    for (i in (daysCount - 1) downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_YEAR, -i)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      val start = c.timeInMillis
      val end = start + 86400000L - 1L

      val daySales = filteredSales.filter { it.timestamp in start..end }
      list.add(
        DailySalesStat(
          dateLabel = if (daysCount <= 7) shortDay.format(Date(start)) else "${c.get(Calendar.DAY_OF_MONTH)}",
          fullDate = dayFormat.format(Date(start)),
          total = daySales.sumOf { it.totalAmount },
          count = daySales.size,
          isToday = (i == 0)
        )
      )
    }
    list
  }

  val maxDayAmount = remember(salesByDay) {
    (salesByDay.maxOfOrNull { it.total } ?: 100.0).coerceAtLeast(30.0)
  }

  // Payment Breakdown
  val paymentBreakdown = remember(filteredSales) {
    val map = mutableMapOf<String, Double>()
    for (s in filteredSales) {
      val current = map.getOrDefault(s.paymentMethod, 0.0)
      map[s.paymentMethod] = current + s.totalAmount
    }
    map
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Export Button
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Summary Reports",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Sales performance & top items",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )
        }

        // Export CSV button
        Button(
          onClick = { showExportDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("export_csv_button")
        ) {
          Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export CSV", fontWeight = FontWeight.Bold)
        }
      }
    }

    // Date Range Selector Chips
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        for (range in ReportRange.values()) {
          val isSelected = selectedRange == range
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setReportRange(range) },
            label = { Text(range.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Emerald500,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("report_range_${range.name}")
          )
        }
      }
    }

    // Total Revenue KPI Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("report_revenue_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "TOTAL REVENUE (${selectedRange.label.uppercase()})",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color.Gray
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Accent Amber for totals
          Text(
            text = "${settings.currencySymbol}%.2f".format(Locale.US, totalRevenue),
            style = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = Amber500
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Color(0xFFE5E7EB))
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "Total Orders", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
              Text(
                text = "$totalOrders sales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Column {
              Text(text = "Average Ticket", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
              Text(
                text = "${settings.currencySymbol}%.2f".format(Locale.US, avgOrderValue),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald700)
              )
            }

            Column {
              Text(text = "Period", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
              Text(
                text = selectedRange.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }

    // Sales-by-Day Chart
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sales_by_day_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Sales-by-Day",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${salesByDay.size} Days Tracked",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val canvasWidth = size.width
              val canvasHeight = size.height - 24.dp.toPx()
              val count = salesByDay.size
              val spacing = canvasWidth * 0.2f
              val barWidth = (canvasWidth - spacing) / count
              val gap = spacing / (count + 1)

              for (i in 0 until count) {
                val stat = salesByDay[i]
                val fraction = (stat.total / maxDayAmount).toFloat().coerceIn(0.04f, 1f)
                val barHeight = canvasHeight * fraction
                val x = gap + i * (barWidth + gap)
                val y = canvasHeight - barHeight

                val barColor = if (stat.isToday) Amber500 else Emerald500

                drawRoundRect(
                  color = barColor,
                  topLeft = Offset(x, y),
                  size = Size(barWidth, barHeight),
                  cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
              }
            }

            // Labels
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              for (stat in salesByDay) {
                Text(
                  text = stat.dateLabel,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = if (stat.isToday) Amber700 else Color.Gray,
                    fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Normal
                  ),
                  modifier = Modifier.weight(1f),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }
      }
    }

    // Top-Selling Items Bar Chart
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("top_selling_items_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Top-Selling Items",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Ranked by sales revenue",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          Spacer(modifier = Modifier.height(14.dp))

          if (topItems.isEmpty()) {
            Text(
              text = "No items sold in this period.",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
              modifier = Modifier.padding(vertical = 12.dp)
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              for ((idx, item) in topItems.withIndex()) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "${idx + 1}. ${item.productName} (${item.quantity} sold)",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                      text = "${settings.currencySymbol}%.2f".format(Locale.US, item.revenue),
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Amber700
                      )
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  // Horizontal progress bar
                  LinearProgressIndicator(
                    progress = { item.percentageOfTotal },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(8.dp)
                      .clip(CircleShape),
                    color = if (idx == 0) Amber500 else Emerald500,
                    trackColor = Color(0xFFF3F4F6)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Payment Methods Breakdown
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("payment_methods_breakdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Payment Methods Breakdown",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          Spacer(modifier = Modifier.height(12.dp))

          if (paymentBreakdown.isEmpty()) {
            Text(text = "No payments recorded yet.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              for ((method, amt) in paymentBreakdown) {
                val pct = if (totalRevenue > 0) ((amt / totalRevenue) * 100).toInt() else 0
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      shape = CircleShape,
                      color = when (method) {
                        "Cash" -> Emerald100
                        "Card" -> Color(0xFFDBEAFE)
                        else -> Amber100
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.Payments,
                          contentDescription = null,
                          tint = when (method) {
                            "Cash" -> Emerald700
                            "Card" -> Color(0xFF1D4ED8)
                            else -> Amber700
                          },
                          modifier = Modifier.size(14.dp)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = method, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "$pct%",
                      style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                      modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                      text = "${settings.currencySymbol}%.2f".format(Locale.US, amt),
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  // Export CSV Dialog
  if (showExportDialog) {
    val csvContent = remember { viewModel.generateSalesCsv() }
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Sales CSV") },
      text = {
        Column {
          Text(
            text = "Export ${filteredSales.size} sales records formatted as standard CSV for spreadsheet apps (Excel, Google Sheets).",
            style = MaterialTheme.typography.bodyMedium
          )
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            color = Color(0xFFF3F4F6),
            shape = RoundedCornerShape(8.dp)
          ) {
            Box(modifier = Modifier.padding(8.dp)) {
              Text(
                text = csvContent.take(280) + if (csvContent.length > 280) "..." else "",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                color = Color.DarkGray
              )
            }
          }
        }
      },
      confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("SaleSnap Sales CSV", csvContent))
              Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
              showExportDialog = false
            },
            modifier = Modifier.testTag("copy_csv_button")
          ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy")
          }

          Button(
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_SUBJECT, "SaleSnap Sales Report - ${settings.businessName}")
                type = "text/csv"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Export Sales Report")
              context.startActivity(shareIntent)
              showExportDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            modifier = Modifier.testTag("share_csv_button")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share CSV")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}
