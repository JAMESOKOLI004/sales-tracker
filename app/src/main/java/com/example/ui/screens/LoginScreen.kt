package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.SaleSnapViewModel

@Composable
fun LoginScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val loginError by viewModel.loginError.collectAsState()
  var pinInput by remember { mutableStateOf("") }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 420.dp)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Brand Logo Header
      Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = Emerald500,
        shadowElevation = 6.dp
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.PointOfSale,
            contentDescription = "SaleSnap Icon",
            tint = Color.White,
            modifier = Modifier.size(46.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "SaleSnap",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          color = Emerald700
        )
      )

      Text(
        text = settings.businessName,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = Amber500,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Business Owner Passcode",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // PIN Indicators (4 dots)
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 4) {
          val isFilled = i < pinInput.length
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(if (isFilled) Emerald500 else Color.Transparent)
              .border(
                2.dp,
                if (isFilled) Emerald500 else Color(0xFFD1D5DB),
                CircleShape
              )
          )
        }
      }

      // Error message
      AnimatedVisibility(visible = loginError != null) {
        Text(
          text = loginError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 12.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Numeric Keypad
      val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
      )

      for (row in keys) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          for (key in row) {
            KeypadButton(
              text = key,
              onClick = {
                when (key) {
                  "C" -> {
                    pinInput = ""
                  }
                  "DEL" -> {
                    if (pinInput.isNotEmpty()) {
                      pinInput = pinInput.dropLast(1)
                    }
                  }
                  else -> {
                    if (pinInput.length < 4) {
                      val updated = pinInput + key
                      pinInput = updated
                      if (updated.length == 4) {
                        viewModel.loginWithPin(updated)
                      }
                    }
                  }
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Quick 1-Tap Demo Login Button for instant access
      Button(
        onClick = { viewModel.quickDemoLogin() },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("quick_owner_login_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Emerald500,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Quick Owner Login (PIN: 1234)",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "SaleSnap Point of Sale • Secure Owner Mode",
        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun KeypadButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .size(68.dp)
      .testTag("keypad_btn_$text"),
    shape = CircleShape,
    color = when (text) {
      "C", "DEL" -> MaterialTheme.colorScheme.surfaceVariant
      else -> MaterialTheme.colorScheme.surface
    },
    shadowElevation = 2.dp,
    onClick = onClick
  ) {
    Box(contentAlignment = Alignment.Center) {
      if (text == "DEL") {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Backspace,
          contentDescription = "Backspace",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(24.dp)
        )
      } else {
        Text(
          text = text,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = if (text == "C") 18.sp else 22.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }
    }
  }
}
