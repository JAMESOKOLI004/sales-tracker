package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val price: Double,
  val category: String = "General",
  val colorHex: String = "#10B981"
)
