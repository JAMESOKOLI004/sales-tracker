package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.RecordSaleScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.SaleSnapTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SaleSnapViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: SaleSnapViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      SaleSnapTheme {
        SaleSnapApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun SaleSnapApp(viewModel: SaleSnapViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val isLoggedIn by viewModel.isLoggedIn.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val cart by viewModel.cart.collectAsState()
  val showReceiptDialog by viewModel.showReceiptDialog.collectAsState()
  val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()

  // Handle Android hardware/gesture Back navigation
  BackHandler(enabled = isLoggedIn && currentScreen != AppScreen.DASHBOARD) {
    viewModel.navigateTo(AppScreen.DASHBOARD)
  }

  if (!isLoggedIn || currentScreen == AppScreen.LOGIN) {
    LoginScreen(viewModel = viewModel)
  } else {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        // Slim Top Bar with Brand, Settings & Lock shortcuts
        Surface(
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PointOfSale,
              contentDescription = null,
              tint = Emerald600,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SaleSnap",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Emerald700,
                letterSpacing = 0.5.sp
              )
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
              onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
              modifier = Modifier.testTag("top_bar_settings_button")
            ) {
              Icon(
                imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = if (currentScreen == AppScreen.SETTINGS) Emerald600 else Color.Gray
              )
            }

            IconButton(
              onClick = { viewModel.logout() },
              modifier = Modifier.testTag("top_bar_lock_button")
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock POS",
                tint = Color.Gray
              )
            }
          }
        }
      },
      bottomBar = {
        SaleSnapBottomNavigationBar(
          currentScreen = currentScreen,
          cartCount = cart.values.sum(),
          onNavigate = { viewModel.navigateTo(it) }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
          when (screen) {
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
            AppScreen.RECORD_SALE -> RecordSaleScreen(viewModel = viewModel)
            AppScreen.SALES_LIST -> com.example.ui.screens.SalesListScreen(viewModel = viewModel)
            AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
            AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
          }
        }

        // Receipt Dialog when a sale is completed
        if (showReceiptDialog && lastCompletedSale != null) {
          ReceiptDialog(
            sale = lastCompletedSale!!,
            settings = settings,
            onDismiss = { viewModel.dismissReceiptDialog() }
          )
        }
      }
    }
  }
}

@Composable
fun SaleSnapBottomNavigationBar(
  currentScreen: AppScreen,
  cartCount: Int,
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier.testTag("main_bottom_nav"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    // 1. Dashboard
    NavigationBarItem(
      selected = currentScreen == AppScreen.DASHBOARD,
      onClick = { onNavigate(AppScreen.DASHBOARD) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
          contentDescription = "Dashboard"
        )
      },
      label = { Text("Home") },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = Emerald100,
        selectedIconColor = Emerald700,
        selectedTextColor = Emerald700
      ),
      modifier = Modifier.testTag("nav_dashboard")
    )

    // 2. Record Sale (Quick-Entry)
    NavigationBarItem(
      selected = currentScreen == AppScreen.RECORD_SALE,
      onClick = { onNavigate(AppScreen.RECORD_SALE) },
      icon = {
        if (cartCount > 0) {
          BadgedBox(
            badge = {
              Badge(containerColor = Amber500) {
                Text(text = "$cartCount", color = Color.White)
              }
            }
          ) {
            Icon(
              imageVector = if (currentScreen == AppScreen.RECORD_SALE) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale,
              contentDescription = "Record Sale"
            )
          }
        } else {
          Icon(
            imageVector = if (currentScreen == AppScreen.RECORD_SALE) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale,
            contentDescription = "Record Sale"
          )
        }
      },
      label = { Text("Record") },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = Emerald100,
        selectedIconColor = Emerald700,
        selectedTextColor = Emerald700
      ),
      modifier = Modifier.testTag("nav_record_sale")
    )

    // 3. Sales List
    NavigationBarItem(
      selected = currentScreen == AppScreen.SALES_LIST,
      onClick = { onNavigate(AppScreen.SALES_LIST) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.SALES_LIST) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
          contentDescription = "Sales"
        )
      },
      label = { Text("Sales") },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = Emerald100,
        selectedIconColor = Emerald700,
        selectedTextColor = Emerald700
      ),
      modifier = Modifier.testTag("nav_sales")
    )

    // 4. Products / Services
    NavigationBarItem(
      selected = currentScreen == AppScreen.PRODUCTS,
      onClick = { onNavigate(AppScreen.PRODUCTS) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.PRODUCTS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
          contentDescription = "Products"
        )
      },
      label = { Text("Products") },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = Emerald100,
        selectedIconColor = Emerald700,
        selectedTextColor = Emerald700
      ),
      modifier = Modifier.testTag("nav_products")
    )

    // 5. Reports
    NavigationBarItem(
      selected = currentScreen == AppScreen.REPORTS,
      onClick = { onNavigate(AppScreen.REPORTS) },
      icon = {
        Icon(
          imageVector = if (currentScreen == AppScreen.REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
          contentDescription = "Reports"
        )
      },
      label = { Text("Reports") },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = Emerald100,
        selectedIconColor = Emerald700,
        selectedTextColor = Emerald700
      ),
      modifier = Modifier.testTag("nav_reports")
    )
  }
}
