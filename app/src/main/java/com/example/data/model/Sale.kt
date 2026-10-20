package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CartItem(
  val product: Product,
  val quantity: Int
) {
  val lineTotal: Double get() = product.price * quantity
}

data class SaleItem(
  val productId: Long,
  val productName: String,
  val unitPrice: Double,
  val quantity: Int
) {
  val lineTotal: Double get() = unitPrice * quantity
}

@Entity(tableName = "sales")
data class Sale(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val totalAmount: Double,
  val customerName: String = "",
  val paymentMethod: String = "Cash", // Cash, Card, Mobile, Transfer, Other
  val itemsJson: String = "", // serialized items list
  val notes: String = ""
)
