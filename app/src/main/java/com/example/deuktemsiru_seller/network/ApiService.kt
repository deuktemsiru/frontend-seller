package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ── 인증 ──────────────────────────────────────────────────
    @POST("api/v1/auth/kakao/login")
    suspend fun kakaoLogin(@Body req: KakaoLoginRequest): ApiResponse<LoginData>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body req: TokenRefreshRequest): ApiResponse<TokenData>

    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    // ── 판매 상품 ──────────────────────────────────────────────
    @GET("api/v1/seller/products")
    suspend fun getSaleItems(): ApiResponse<List<SaleItemApiResponse>>

    @POST("api/v1/seller/products")
    suspend fun createSaleItem(@Body req: SaleItemRequest): ApiResponse<SaleItemApiResponse>

    @PATCH("api/v1/seller/products/{id}")
    suspend fun updateSaleStatus(
        @Path("id") id: Long,
        @Body req: UpdateSaleStatusRequest,
    ): ApiResponse<SaleItemApiResponse>

    @DELETE("api/v1/seller/products/{id}")
    suspend fun cancelSaleItem(@Path("id") id: Long): ApiResponse<Unit>

    // ── 메뉴 ──────────────────────────────────────────────────
    @GET("api/v1/seller/menus")
    suspend fun getMenus(): ApiResponse<List<MenuItemApiResponse>>

    @POST("api/v1/seller/menus")
    suspend fun addMenu(@Body req: MenuItemRequest): ApiResponse<MenuItemApiResponse>

    @Multipart
    @POST("api/v1/seller/menus")
    suspend fun addMenuWithImage(
        @Part("name") name: RequestBody,
        @Part("emoji") emoji: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("discountRate") discountRate: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("pickupTimeSlot") pickupTimeSlot: RequestBody,
        @Part image: MultipartBody.Part?,
    ): ApiResponse<MenuItemApiResponse>

    @PATCH("api/v1/seller/menus/{menuItemId}")
    suspend fun updateMenu(
        @Path("menuItemId") menuItemId: Long,
        @Body req: MenuItemUpdateRequest,
    ): ApiResponse<MenuItemApiResponse>

    @DELETE("api/v1/seller/menus/{menuItemId}")
    suspend fun deleteMenu(@Path("menuItemId") menuItemId: Long): ApiResponse<Unit>

    // ── 주문 ──────────────────────────────────────────────────
    @GET("api/v1/seller/orders")
    suspend fun getOrders(): ApiResponse<List<OrderApiResponse>>

    @PATCH("api/v1/seller/orders/{orderId}")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Body req: UpdateOrderStatusRequest,
    ): ApiResponse<OrderApiResponse>

    @GET("api/v1/seller/pickup/verify")
    suspend fun verifyPickupCode(@Query("code") code: String): ApiResponse<OrderApiResponse>

    // ── 가게 ──────────────────────────────────────────────────
    @GET("api/v1/seller/store")
    suspend fun getMyStore(): ApiResponse<StoreApiResponse>

    @PATCH("api/v1/seller/store")
    suspend fun updateStore(@Body req: UpdateStoreRequest): ApiResponse<StoreApiResponse>

    // ── 알림 ──────────────────────────────────────────────────
    @POST("api/v1/seller/notifications")
    suspend fun sendNotification(@Body req: SendNotificationRequest): ApiResponse<NotificationApiResponse>

    @GET("api/v1/seller/notifications")
    suspend fun getNotifications(): ApiResponse<List<NotificationApiResponse>>

    // ── 매출 ──────────────────────────────────────────────────
    @GET("api/v1/seller/sales")
    suspend fun getSales(
        @Query("period") period: String = "weekly",
        @Query("offset") offset: Int = 0,
    ): ApiResponse<SalesApiResponse>
}
