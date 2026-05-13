package com.example.deuktemsiru_seller.network

// ── 공통 응답 래퍼 ────────────────────────────────────────────
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
)

// ── 인증 ──────────────────────────────────────────────────────
data class KakaoLoginRequest(
    val kakaoAccessToken: String,
    val role: String = "SELLER",
)

data class DebugLoginRequest(
    val role: String = "SELLER",
)

data class TokenRefreshRequest(val refreshToken: String)

data class MemberSummary(
    val memberId: Long,
    val nickname: String,
    val role: String,
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val member: MemberSummary,
)

data class TokenData(val accessToken: String)

// ── 판매 상품(SaleItem) ───────────────────────────────────────
data class SaleItemApiResponse(
    val id: Long,
    val menuItemId: Long,
    val name: String,
    val emoji: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val discountRate: Int,
    val remainingItems: Int,
    val totalItems: Int,
    val status: String,
    val pickupTimeSlot: String,
)

data class SaleItemRequest(
    val menuItemId: Long,
    val discountRate: Int,
    val quantity: Int,
    val pickupTimeSlot: String,
)

data class UpdateSaleStatusRequest(val status: String)

// ── 메뉴 ──────────────────────────────────────────────────────
data class MenuItemApiResponse(
    val id: Long,
    val name: String,
    val emoji: String,
    val imageUrl: String? = null,
    val originalPrice: Int,
)

data class MenuItemRequest(
    val name: String,
    val emoji: String,
    val originalPrice: Int,
    val costPrice: Int? = null,
    val allergyInfo: String? = null,
)

data class MenuItemUpdateRequest(
    val name: String? = null,
    val originalPrice: Int? = null,
)

// ── 주문 ──────────────────────────────────────────────────────
data class OrderItemApiResponse(
    val productId: Long,
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

data class UpdateOrderStatusRequest(val status: String)

// ── 가게 ──────────────────────────────────────────────────────
data class StoreApiResponse(
    val id: Long,
    val name: String,
    val category: String,
    val address: String,
    val phone: String,
    val closingTime: String,
)

data class UpdateStoreRequest(
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
)

// ── 알림 ──────────────────────────────────────────────────────
data class SendNotificationRequest(val message: String)

data class NotificationApiResponse(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val message: String,
    val sentAt: String,
    val recipientCount: Int,
)

// ── 매출 ──────────────────────────────────────────────────────
data class DailySales(val date: String, val amount: Int)
data class TopMenu(val name: String, val emoji: String, val count: Int)

data class SalesApiResponse(
    val todaySales: Int,
    val todayOrderCount: Int,
    val salesData: List<DailySales>,
    val topMenus: List<TopMenu>,
)
