package com.example.data.model

data class BusinessSettings(
  val businessName: String = "SaleSnap Mart",
  val currencySymbol: String = "$",
  val ownerPin: String = "1234",
  val receiptNote: String = "Thank you for shopping with us! Have a wonderful day.",
  val phoneNumber: String = "+1 (555) 234-5678",
  val address: String = "104 Market Street, Suite B"
)
