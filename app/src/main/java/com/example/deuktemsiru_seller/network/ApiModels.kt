package com.example.deuktemsiru_seller.network

import com.google.gson.annotations.SerializedName

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

// ── 멤버 ──────────────────────────────────────────────────────
data class MemberMeResponse(
    val memberId: Long,
    val email: String? = null,
    val nickname: String,
    val role: String,
)

// ── 판매 상품(SaleItem) ───────────────────────────────────────
data class SaleItemApiResponse(
    @SerializedName("productId") val id: Long,
    val menuItemId: Long? = null,
    val name: String,
    val emoji: String? = null,
    val originalPrice: Int,
    @SerializedName("discountPrice") val discountedPrice: Int,
    val discountRate: Int? = null,
    @SerializedName("quantityRemaining") val remainingItems: Int,
    @SerializedName("quantityTotal") val totalItems: Int,
    val status: String,
    // 명세서: pickupStart + pickupEnd 분리. 기존 pickupTimeSlot도 허용(mock 호환)
    val pickupStart: String? = null,
    val pickupEnd: String? = null,
    val pickupTimeSlot: String? = null,
) {
    val displayPickupTime: String
        get() = pickupTimeSlot
            ?: if (pickupStart != null && pickupEnd != null) "$pickupStart~$pickupEnd"
            else pickupStart ?: pickupEnd ?: ""
}

// 상품 목록 래퍼 (GET /sellers/products → data.products)
data class ProductListData(
    val products: List<SaleItemApiResponse> = emptyList(),
)

data class SaleItemCreateRequest(
    val menuItemId: Long? = null,
    val name: String,
    val discountPrice: Int,
    val originalPrice: Int,
    val quantityTotal: Int,
    val madeAt: String? = null,
    val pickupStart: String,
    val pickupEnd: String,
    val availableDate: String,
    val allergenInfo: String? = null,
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
    val productId: Long? = null,
    val menuItemId: Long? = null,
    @SerializedName("productName") val name: String,
    val emoji: String? = null,
    val quantity: Int,
    @SerializedName("unitPrice") val price: Int,
)

data class OrderApiResponse(
    @SerializedName("orderId") val id: Long,
    val orderNumber: String? = null,
    val storeId: Long? = null,
    val storeName: String? = null,
    val customerName: String? = null,
    val status: String,
    val pickupCode: String,
    val pickupTime: String? = null,
    @SerializedName("totalPrice") val totalAmount: Int,
    val createdAt: String,
    val items: List<OrderItemApiResponse>,
)

// 주문 목록 래퍼 (GET /sellers/orders → data.orders)
data class OrderListData(
    val orders: List<OrderApiResponse> = emptyList(),
)

data class UpdateOrderStatusRequest(val status: String)

data class ConfirmPickupRequest(val pickupCode: String)

// ── 가게 ──────────────────────────────────────────────────────
data class StoreApiResponse(
    // 명세서 v2 필드
    val storeId: Long? = null,
    val name: String,
    val isActive: Int? = null,
    val isVerified: Int? = null,
    val todayProductCount: Int? = null,
    val pendingOrderCount: Int? = null,
    val ratingAvg: Float? = null,
    val reviewCount: Int? = null,
    // 확장 필드 (가게 상세/수정에서 추가 반환될 수 있음)
    val id: Long? = null,
    val category: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
    val description: String? = null,
) {
    val actualId: Long get() = storeId ?: id ?: 0L
}

data class UpdateStoreRequest(
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
    val description: String? = null,
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

data class TopMenu(
    @SerializedName("productName") val name: String,
    val emoji: String? = null,
    @SerializedName("soldCount") val count: Int,
)

data class SalesApiResponse(
    @SerializedName("totalAmount") val todaySales: Int = 0,
    @SerializedName("totalOrders") val todayOrderCount: Int = 0,
    @SerializedName("chartData") val salesData: List<DailySales> = emptyList(),
    @SerializedName("topProducts") val topMenus: List<TopMenu> = emptyList(),
    val carbonSavedKg: Float? = null,
)
