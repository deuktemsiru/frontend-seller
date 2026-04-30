package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

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
