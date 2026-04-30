package com.example.deuktemsiru_seller.network

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val userId: Long,
    val nickname: String,
    val role: String,
    val token: String,
)

data class MenuItemApiResponse(
    val id: Long,
    val name: String,
    val emoji: String,
    val imageUrl: String? = null,
    val originalPrice: Int,
    val discountedPrice: Int,
    val discountRate: Int,
    val remainingItems: Int,
    val isSoldOut: Boolean,
    val pickupTimeSlot: String,
)

data class StoreApiResponse(
    val id: Long,
    val name: String,
    val category: String,
    val emoji: String,
    val rating: Float,
    val address: String,
    val phone: String,
    val closingTime: String,
    val menus: List<MenuItemApiResponse>,
)

data class OrderItemApiResponse(
    val menuItemId: Long,
    val name: String,
    val emoji: String,
    val quantity: Int,
    val price: Int,
)

data class OrderApiResponse(
    val id: Long,
    val orderNumber: String,
    val storeId: Long,
    val storeName: String,
    val status: String,
    val pickupCode: String,
    val pickupTime: String,
    val totalAmount: Int,
    val createdAt: String,
    val items: List<OrderItemApiResponse>,
)

data class MenuItemRequest(
    val name: String,
    val emoji: String,
    val originalPrice: Int,
    val discountRate: Int,
    val quantity: Int,
    val pickupTimeSlot: String,
)

data class MenuItemUpdateRequest(
    val remainingItems: Int? = null,
    val isSoldOut: Boolean? = null,
    val discountRate: Int? = null,
    val pickupTimeSlot: String? = null,
)

data class UpdateOrderStatusRequest(val status: String)

data class SendNotificationRequest(val message: String)

data class NotificationApiResponse(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val message: String,
    val sentAt: String,
    val recipientCount: Int,
)

data class DailySales(val date: String, val amount: Int)
data class TopMenu(val name: String, val emoji: String, val count: Int)

data class SalesApiResponse(
    val todaySales: Int,
    val todayOrderCount: Int,
    val salesData: List<DailySales>,
    val topMenus: List<TopMenu>,
)

data class UpdateStoreRequest(
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
)
