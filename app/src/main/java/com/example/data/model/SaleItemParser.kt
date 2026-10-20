package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object SaleItemParser {
  fun toJson(items: List<SaleItem>): String {
    val array = JSONArray()
    for (item in items) {
      val obj = JSONObject().apply {
        put("productId", item.productId)
        put("productName", item.productName)
        put("unitPrice", item.unitPrice)
        put("quantity", item.quantity)
      }
      array.put(obj)
    }
    return array.toString()
  }

  fun fromJson(jsonStr: String): List<SaleItem> {
    if (jsonStr.isBlank()) return emptyList()
    val list = mutableListOf<SaleItem>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          SaleItem(
            productId = obj.optLong("productId", 0L),
            productName = obj.optString("productName", "Item"),
            unitPrice = obj.optDouble("unitPrice", 0.0),
            quantity = obj.optInt("quantity", 1)
          )
        )
      }
    } catch (e: Exception) {
      // In case of older or custom format
    }
    return list
  }
}
