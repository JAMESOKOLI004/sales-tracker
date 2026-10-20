package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<Product>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: Product): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<Product>): List<Long>

  @Update
  suspend fun updateProduct(product: Product)

  @Delete
  suspend fun deleteProduct(product: Product)

  @Query("SELECT COUNT(*) FROM products")
  suspend fun countProducts(): Int

  @Query("DELETE FROM products")
  suspend fun deleteAllProducts()
}
