package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): RegisterResponse

    @GET("api/auth/verify-business")
    suspend fun verifyBusiness(@Query("number") number: String): BusinessVerifyResponse

    @GET("api/notices")
    suspend fun getNotices(): List<NoticeApiResponse>

    @GET("api/seller/products")
    suspend fun getSaleItems(@Query("sellerId") sellerId: Long): List<SaleItemApiResponse>

    @POST("api/seller/products")
    suspend fun createSaleItem(
        @Query("sellerId") sellerId: Long,
        @Body req: SaleItemRequest,
    ): SaleItemApiResponse

    @PATCH("api/seller/products/{id}")
    suspend fun updateSaleStatus(
        @Path("id") id: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateSaleStatusRequest,
    ): SaleItemApiResponse

    @DELETE("api/seller/products/{id}")
    suspend fun cancelSaleItem(
        @Path("id") id: Long,
        @Query("sellerId") sellerId: Long,
    )

    @GET("api/seller/pickup/verify")
    suspend fun verifyPickupCode(
        @Query("sellerId") sellerId: Long,
        @Query("code") code: String,
    ): OrderApiResponse

    @GET("api/seller/store")
    suspend fun getMyStore(@Query("sellerId") sellerId: Long): StoreApiResponse

    @PATCH("api/seller/store")
    suspend fun updateStore(
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateStoreRequest,
    ): StoreApiResponse

    @POST("api/seller/menus")
    suspend fun addMenu(
        @Query("sellerId") sellerId: Long,
        @Body req: MenuItemRequest,
    ): MenuItemApiResponse

    @Multipart
    @POST("api/seller/menus")
    suspend fun addMenuWithImage(
        @Query("sellerId") sellerId: Long,
        @Part("name") name: RequestBody,
        @Part("emoji") emoji: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("discountRate") discountRate: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("pickupTimeSlot") pickupTimeSlot: RequestBody,
        @Part image: MultipartBody.Part?,
    ): MenuItemApiResponse

    @PATCH("api/seller/menus/{menuItemId}")
    suspend fun updateMenu(
        @Path("menuItemId") menuItemId: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: MenuItemUpdateRequest,
    ): MenuItemApiResponse

    @DELETE("api/seller/menus/{menuItemId}")
    suspend fun deleteMenu(
        @Path("menuItemId") menuItemId: Long,
        @Query("sellerId") sellerId: Long,
    )

    @GET("api/seller/orders")
    suspend fun getOrders(@Query("sellerId") sellerId: Long): List<OrderApiResponse>

    @PATCH("api/seller/orders/{orderId}")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateOrderStatusRequest,
    ): OrderApiResponse

    @GET("api/seller/sales")
    suspend fun getSales(
        @Query("sellerId") sellerId: Long,
        @Query("period") period: String = "weekly",
        @Query("offset") offset: Int = 0,
    ): SalesApiResponse

    @POST("api/seller/notifications")
    suspend fun sendNotification(
        @Query("sellerId") sellerId: Long,
        @Body req: SendNotificationRequest,
    ): NotificationApiResponse

    @GET("api/seller/notifications")
    suspend fun getNotifications(@Query("sellerId") sellerId: Long): List<NotificationApiResponse>
}
