package com.thepriceisright.domain.model

import java.time.LocalDateTime

data class ShoppingList(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val items: List<ShoppingListItem> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val shareCode: String? = null,
    val isShared: Boolean = false
) {
    val completedCount: Int get() = items.count { it.isChecked }
    val totalCount: Int get() = items.size
    val progress: Float get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
}

data class ShoppingListItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int = 1,
    val unit: String? = null,
    val isChecked: Boolean = false,
    val category: String? = null,
    val barcode: String? = null,
    val sortOrder: Int = 0
)
