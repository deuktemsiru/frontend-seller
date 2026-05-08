package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer

class MockApiService(sellerId: Long) : ApiService {

    private val account = SampleData.findById(sellerId)
        ?: throw IllegalArgumentException("샘플 계정을 찾을 수 없습니다: $sellerId")

    private var storeData = account.store
    private val menuList = account.store.menus.toMutableList()
    private val saleItems: MutableList<SaleItemApiResponse> = account.store.menus.mapIndexed { i, m ->
        SaleItemApiResponse(
            id = (sellerId * 1000 + i).toLong(),
            menuItemId = m.id,
            name = m.name,
            emoji = m.emoji,
            originalPrice = m.originalPrice,
            discountedPrice = m.discountedPrice,
            discountRate = m.discountRate,
            remainingItems = m.remainingItems,
            totalItems = m.remainingItems,
            status = "AVAILABLE",
            pickupTimeSlot = m.pickupTimeSlot,
        )
    }.toMutableList()

    override suspend fun login(req: LoginRequest): LoginResponse =
        throw UnsupportedOperationException()

    override suspend fun register(req: RegisterRequest): RegisterResponse =
        throw UnsupportedOperationException()

    override suspend fun verifyBusiness(number: String): BusinessVerifyResponse =
        BusinessVerifyResponse(verified = true, businessName = "샘플 사업자")

    override suspend fun getNotices(): List<NoticeApiResponse> = SampleData.notices

    override suspend fun getSaleItems(sellerId: Long): List<SaleItemApiResponse> =
        saleItems.toList()

    override suspend fun createSaleItem(sellerId: Long, req: SaleItemRequest): SaleItemApiResponse {
        val menu = menuList.find { it.id == req.menuItemId }
            ?: throw Exception("메뉴를 찾을 수 없어요")
        val discounted = (menu.originalPrice * (1.0 - req.discountRate / 100.0)).toInt()
        val item = SaleItemApiResponse(
            id = System.currentTimeMillis(),
            menuItemId = req.menuItemId,
            name = menu.name,
            emoji = menu.emoji,
            originalPrice = menu.originalPrice,
            discountedPrice = discounted,
            discountRate = req.discountRate,
            remainingItems = req.quantity,
            totalItems = req.quantity,
            status = "AVAILABLE",
            pickupTimeSlot = req.pickupTimeSlot,
        )
        saleItems.add(item)
        return item
    }

    override suspend fun updateSaleStatus(id: Long, sellerId: Long, req: UpdateSaleStatusRequest): SaleItemApiResponse {
        val idx = saleItems.indexOfFirst { it.id == id }
        if (idx < 0) throw Exception("상품을 찾을 수 없어요")
        val updated = saleItems[idx].copy(status = req.status)
        saleItems[idx] = updated
        return updated
    }

    override suspend fun cancelSaleItem(id: Long, sellerId: Long) {
        saleItems.removeAll { it.id == id }
    }

    override suspend fun verifyPickupCode(sellerId: Long, code: String): OrderApiResponse =
        throw Exception("샘플 모드에서는 픽업 코드를 확인할 수 없어요")

    override suspend fun getMyStore(sellerId: Long): StoreApiResponse =
        storeData.copy(menus = menuList.toList())

    override suspend fun updateStore(sellerId: Long, req: UpdateStoreRequest): StoreApiResponse {
        storeData = storeData.copy(
            address = req.address ?: storeData.address,
            phone = req.phone ?: storeData.phone,
            closingTime = req.closingTime ?: storeData.closingTime,
        )
        return storeData.copy(menus = menuList.toList())
    }

    override suspend fun addMenu(sellerId: Long, req: MenuItemRequest): MenuItemApiResponse {
        val discounted = (req.originalPrice * (1.0 - req.discountRate / 100.0)).toInt()
        val menu = MenuItemApiResponse(
            id = System.currentTimeMillis(),
            name = req.name,
            emoji = req.emoji,
            originalPrice = req.originalPrice,
            discountedPrice = discounted,
            discountRate = req.discountRate,
            remainingItems = req.quantity,
            isSoldOut = false,
            pickupTimeSlot = req.pickupTimeSlot,
        )
        menuList.add(menu)
        return menu
    }

    override suspend fun addMenuWithImage(
        sellerId: Long,
        name: RequestBody,
        emoji: RequestBody,
        originalPrice: RequestBody,
        discountRate: RequestBody,
        quantity: RequestBody,
        pickupTimeSlot: RequestBody,
        image: MultipartBody.Part?,
    ): MenuItemApiResponse {
        val nameStr = name.readText()
        val emojiStr = emoji.readText()
        val originalPriceInt = originalPrice.readText().toIntOrNull() ?: 0
        val discountRateInt = discountRate.readText().toIntOrNull() ?: 0
        val quantityInt = quantity.readText().toIntOrNull() ?: 0
        val pickupStr = pickupTimeSlot.readText()
        val discounted = (originalPriceInt * (1.0 - discountRateInt / 100.0)).toInt()
        val menu = MenuItemApiResponse(
            id = System.currentTimeMillis(),
            name = nameStr,
            emoji = emojiStr,
            originalPrice = originalPriceInt,
            discountedPrice = discounted,
            discountRate = discountRateInt,
            remainingItems = quantityInt,
            isSoldOut = false,
            pickupTimeSlot = pickupStr,
        )
        menuList.add(menu)
        return menu
    }

    override suspend fun updateMenu(menuItemId: Long, sellerId: Long, req: MenuItemUpdateRequest): MenuItemApiResponse {
        val idx = menuList.indexOfFirst { it.id == menuItemId }
        val base = menuList.getOrNull(idx) ?: MenuItemApiResponse(
            id = menuItemId, name = "", emoji = "", originalPrice = 0, discountedPrice = 0,
            discountRate = 0, remainingItems = 0, isSoldOut = false, pickupTimeSlot = "",
        )
        val updated = base.copy(
            remainingItems = req.remainingItems ?: base.remainingItems,
            isSoldOut = req.isSoldOut ?: base.isSoldOut,
            discountRate = req.discountRate ?: base.discountRate,
            pickupTimeSlot = req.pickupTimeSlot ?: base.pickupTimeSlot,
        )
        if (idx >= 0) menuList[idx] = updated
        return updated
    }

    override suspend fun deleteMenu(menuItemId: Long, sellerId: Long) {
        menuList.removeAll { it.id == menuItemId }
    }

    override suspend fun getOrders(sellerId: Long): List<OrderApiResponse> = emptyList()

    override suspend fun updateOrderStatus(orderId: Long, sellerId: Long, req: UpdateOrderStatusRequest): OrderApiResponse =
        OrderApiResponse(
            id = orderId,
            orderNumber = "ORD-SAMPLE",
            storeId = sellerId,
            storeName = account.storeName,
            status = req.status,
            pickupCode = "0000",
            pickupTime = "18:00",
            totalAmount = 0,
            createdAt = "",
            items = emptyList(),
        )

    override suspend fun getSales(sellerId: Long, period: String, offset: Int): SalesApiResponse =
        SalesApiResponse(todaySales = 0, todayOrderCount = 0, salesData = emptyList(), topMenus = emptyList())

    override suspend fun sendNotification(sellerId: Long, req: SendNotificationRequest): NotificationApiResponse =
        NotificationApiResponse(
            id = System.currentTimeMillis(),
            storeId = sellerId,
            storeName = account.storeName,
            message = req.message,
            sentAt = java.time.LocalDateTime.now().toString(),
            recipientCount = 0,
        )

    override suspend fun getNotifications(sellerId: Long): List<NotificationApiResponse> = emptyList()

    private fun RequestBody.readText(): String {
        val buffer = Buffer()
        return try {
            writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            ""
        }
    }
}