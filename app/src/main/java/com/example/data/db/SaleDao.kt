package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
  @Query("SELECT * FROM sales ORDER BY timestamp DESC")
  fun getAllSales(): Flow<List<Sale>>

  @Query("SELECT * FROM sales WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
  fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<Sale>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSale(sale: Sale): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSales(sales: List<Sale>): List<Long>

  @Update
  suspend fun updateSale(sale: Sale)

  @Delete
  suspend fun deleteSale(sale: Sale)

  @Query("DELETE FROM sales")
  suspend fun deleteAllSales()

  @Query("SELECT COUNT(*) FROM sales")
  suspend fun countSales(): Int
}
