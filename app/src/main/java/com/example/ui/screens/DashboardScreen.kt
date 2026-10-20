package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.Emerald900
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SaleSnapViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val sales by viewModel.sales.collectAsState()

  // Compute 7-day stats and today's sales
  val cal = Calendar.getInstance()
  val todayStart = remember(sales) {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.timeInMillis
  }

  val todaySales = remember(sales) {
    sales.filter { it.timestamp >= todayStart }
  }

  val todayTotal = remember(todaySales) {
    todaySales.sumOf { it.totalAmount }
  }

  val todayCount = todaySales.size
  val avgSaleValue = if (todayCount > 0) todayTotal / todayCount else 0.0

  // 7-day sales breakdown for mini bar chart
  val last7DaysData = remember(sales) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val list = mutableListOf<DayBarData>()
    for (i in 6 downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_YEAR, -i)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      val start = c.timeInMillis
      val end = start + 86400000L - 1L

      val daySales = sales.filter { it.timestamp in start..end }
      val total = daySales.sumOf { it.totalAmount }
      list.add(
        DayBarData(
          label = if (i == 0) "Today" else dayFormat.format(Date(start)),
          amount = total,
          isToday = (i == 0)
        )
      )
    }
    list
  }

  val max7DayAmount = remember(last7DaysData) {
    (last7DaysData.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(50.0)
  }

  val recentSales = remember(sales) {
    sales.take(4)
  }

  val dateHeader = remember {
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
  }

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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = settings.businessName,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )
          Text(
            text = dateHeader,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )
        }

        IconButton(
          onClick = { viewModel.logout() },
          modifier = Modifier.testTag("lock_app_button")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock App",
            tint = Emerald600
          )
        }
      }
    }

    // Today's Total Sales Card (Large Bold Text)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("today_total_sales_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald600),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(Emerald600, Emerald700, Emerald900)
              )
            )
            .padding(20.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TODAY'S TOTAL SALES",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = Emerald100
                )
              )
              Surface(
                shape = CircleShape,
                color = Amber500
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$todayCount Sales",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Bold Sales Amount
            Text(
              text = "${settings.currencySymbol}%.2f".format(Locale.US, todayTotal),
              style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 42.sp
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Avg per ticket: ${settings.currencySymbol}%.2f".format(Locale.US, avgSaleValue),
                style = MaterialTheme.typography.bodySmall.copy(color = Emerald100)
              )
              Text(
                text = "Tap to Record Sale ➜",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = Amber100
                ),
                modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.RECORD_SALE) }
              )
            }
          }
        }
      }
    }

    // Prominent "+ Record Sale" Button
    item {
      Button(
        onClick = { viewModel.navigateTo(AppScreen.RECORD_SALE) },
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("record_sale_prominent_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Emerald500,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "+ Record Sale",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      }
    }

    // Mini Bar Chart of Last 7 Days
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("seven_days_bar_chart_card"),
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
              text = "Last 7 Days Sales",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Total: ${settings.currencySymbol}%.2f".format(Locale.US, last7DaysData.sumOf { it.amount }),
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Emerald700
              )
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Canvas Bar Chart
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val canvasWidth = size.width
              val canvasHeight = size.height - 24.dp.toPx()
              val barCount = last7DaysData.size
              val totalSpacing = canvasWidth * 0.25f
              val barWidth = (canvasWidth - totalSpacing) / barCount
              val barGap = totalSpacing / (barCount + 1)

              for (i in 0 until barCount) {
                val data = last7DaysData[i]
                val fraction = (data.amount / max7DayAmount).toFloat().coerceIn(0.04f, 1f)
                val barHeight = canvasHeight * fraction
                val x = barGap + i * (barWidth + barGap)
                val y = canvasHeight - barHeight

                val barColor = if (data.isToday) Amber500 else Emerald500

                // Draw Bar
                drawRoundRect(
                  color = barColor,
                  topLeft = Offset(x, y),
                  size = Size(barWidth, barHeight),
                  cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
              }
            }

            // Day labels under bars
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              for (data in last7DaysData) {
                Text(
                  text = data.label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (data.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (data.isToday) Amber700 else Color.Gray
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

    // Quick Stats Row: Transactions Today & Avg Sale Value
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Transactions Today
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("quick_stat_transactions"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Emerald100,
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.ReceiptLong,
                  contentDescription = null,
                  tint = Emerald700,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Transactions",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
              )
              Text(
                text = "$todayCount Today",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }

        // Avg Sale Value
        Card(
          modifier = Modifier
            .weight(1f)
            .testTag("quick_stat_avg_sale"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Amber100,
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Payments,
                  contentDescription = null,
                  tint = Amber700,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Avg Value",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
              )
              Text(
                text = "${settings.currencySymbol}%.2f".format(Locale.US, avgSaleValue),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Amber700
                )
              )
            }
          }
        }
      }
    }

    // Recent Sales Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Sales",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(
          onClick = { viewModel.navigateTo(AppScreen.SALES_LIST) },
          modifier = Modifier.testTag("view_all_sales_button")
        ) {
          Text(text = "View All", color = Emerald600, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Emerald600,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Recent Sales Items
    if (recentSales.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No sales recorded yet. Tap '+ Record Sale' to make your first sale!",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      items(recentSales) { sale ->
        DashboardSaleItemCard(
          sale = sale,
          currencySymbol = settings.currencySymbol,
          onClick = { viewModel.selectSaleForDetail(sale) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

data class DayBarData(
  val label: String,
  val amount: Double,
  val isToday: Boolean
)

@Composable
fun DashboardSaleItemCard(
  sale: Sale,
  currencySymbol: String,
  onClick: () -> Unit
) {
  val items = remember(sale.itemsJson) { SaleItemParser.fromJson(sale.itemsJson) }
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val timeStr = timeFormat.format(Date(sale.timestamp))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("dashboard_sale_card_${sale.id}"),
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Surface(
          shape = CircleShape,
          color = when (sale.paymentMethod) {
            "Cash" -> Emerald100
            "Card" -> Color(0xFFDBEAFE)
            else -> Amber100
          },
          modifier = Modifier.size(40.dp)
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
          Text(
            text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "$timeStr • ${items.sumOf { it.quantity }} items (${sale.paymentMethod})",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            maxLines = 1
          )
        }
      }

      Text(
        text = "$currencySymbol%.2f".format(Locale.US, sale.totalAmount),
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Amber700
        )
      )
    }
  }
}
