package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ── 인증 ──────────────────────────────────────────────────
    @POST("api/v1/auth/kakao/login")
    suspend fun kakaoLogin(@Body req: KakaoLoginRequest): ApiResponse<LoginData>

    @POST("api/v1/auth/debug/login")
    suspend fun debugLogin(@Body req: DebugLoginRequest): ApiResponse<LoginData>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body req: TokenRefreshRequest): ApiResponse<TokenData>

    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    // ── 멤버 ──────────────────────────────────────────────────
    @GET("api/v1/members/me")
    suspend fun getMyInfo(): ApiResponse<MemberMeResponse>

    // ── 판매 상품 ──────────────────────────────────────────────
    @GET("api/v1/sellers/products")
    suspend fun getSaleItems(): ApiResponse<List<SaleItemApiResponse>>

    @POST("api/v1/sellers/products")
    suspend fun createSaleItem(@Body req: SaleItemCreateRequest): ApiResponse<SaleItemApiResponse>

    @Multipart
    @POST("api/v1/sellers/products")
    suspend fun createSaleItemWithImage(
        @Part("name") name: RequestBody,
        @Part("discountPrice") discountPrice: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("quantityTotal") quantityTotal: RequestBody,
        @Part("pickupStart") pickupStart: RequestBody,
        @Part("pickupEnd") pickupEnd: RequestBody,
        @Part("availableDate") availableDate: RequestBody,
        @Part("allergenInfo") allergenInfo: RequestBody?,
        @Part images: List<MultipartBody.Part>?,
    ): ApiResponse<SaleItemApiResponse>

    @PATCH("api/v1/sellers/products/{productId}/status")
    suspend fun updateSaleStatus(
        @Path("productId") id: Long,
        @Body req: UpdateSaleStatusRequest,
    ): ApiResponse<SaleItemApiResponse>

    @DELETE("api/v1/sellers/products/{productId}")
    suspend fun cancelSaleItem(@Path("productId") id: Long): ApiResponse<Unit>

    // ── 메뉴 마스터 ────────────────────────────────────────────
    @GET("api/v1/sellers/menu-items")
    suspend fun getMenus(): ApiResponse<List<MenuItemApiResponse>>

    @POST("api/v1/sellers/menu-items")
    suspend fun addMenu(@Body req: MenuItemRequest): ApiResponse<MenuItemApiResponse>

    @Multipart
    @POST("api/v1/sellers/menu-items")
    suspend fun addMenuWithImage(
        @Part("name") name: RequestBody,
        @Part("emoji") emoji: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("discountRate") discountRate: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("pickupTimeSlot") pickupTimeSlot: RequestBody,
        @Part("allergyInfo") allergyInfo: RequestBody?,
        @Part image: MultipartBody.Part?,
    ): ApiResponse<MenuItemApiResponse>

    @PATCH("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun updateMenu(
        @Path("menuItemId") menuItemId: Long,
        @Body req: MenuItemUpdateRequest,
    ): ApiResponse<MenuItemApiResponse>

    @DELETE("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun deleteMenu(@Path("menuItemId") menuItemId: Long): ApiResponse<Unit>

    // ── 주문 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/orders")
    suspend fun getOrders(): ApiResponse<List<OrderApiResponse>>

    @PATCH("api/v1/sellers/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Body req: UpdateOrderStatusRequest,
    ): ApiResponse<OrderApiResponse>

    @PATCH("api/v1/sellers/orders/{orderId}/confirm")
    suspend fun confirmPickup(
        @Path("orderId") orderId: Long,
        @Body req: ConfirmPickupRequest,
    ): ApiResponse<OrderApiResponse>

    @GET("api/v1/sellers/pickup/verify")
    suspend fun verifyPickupCode(@Query("code") code: String): ApiResponse<OrderApiResponse>

    // ── 가게 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/stores/my")
    suspend fun getMyStore(): ApiResponse<StoreApiResponse>

    @PUT("api/v1/sellers/stores/my")
    suspend fun updateStore(@Body req: UpdateStoreRequest): ApiResponse<StoreApiResponse>

    // ── 알림 ──────────────────────────────────────────────────
    @POST("api/v1/sellers/notifications")
    suspend fun sendNotification(@Body req: SendNotificationRequest): ApiResponse<NotificationApiResponse>

    @GET("api/v1/sellers/notifications")
    suspend fun getNotifications(): ApiResponse<List<NotificationApiResponse>>

    // ── 정산 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/settlements")
    suspend fun getSettlements(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): ApiResponse<SettlementListResponse>

    @POST("api/v1/sellers/settlements/withdrawals")
    suspend fun requestWithdrawal(@Body req: SettlementWithdrawRequest): ApiResponse<SettlementItem>

    // ── 매출 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/sales/summary")
    suspend fun getSales(
        @Query("period") period: String = "DAY",
        @Query("date") date: String? = null,
    ): ApiResponse<SalesApiResponse>
}
