package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody

object MockApiService : ApiService {

    private var _store = StoreApiResponse(
        id = 1L,
        name = "남산 베이커리",
        category = "BAKERY",
        address = "서울시 용산구 남산동 2가 123-4",
        phone = "02-1234-5678",
        closingTime = "21:00",
    )

    private val menus = mutableListOf(
        MenuItemApiResponse(id = 1L, name = "크루아상", emoji = "🥐", originalPrice = 3800),
        MenuItemApiResponse(id = 2L, name = "소금빵", emoji = "🍞", originalPrice = 2500),
        MenuItemApiResponse(id = 3L, name = "바게트", emoji = "🥖", originalPrice = 4500),
        MenuItemApiResponse(id = 4L, name = "시나몬롤", emoji = "🌀", originalPrice = 3200),
        MenuItemApiResponse(id = 5L, name = "스콘", emoji = "🧁", originalPrice = 2800),
    )

    private val saleItems = mutableListOf(
        SaleItemApiResponse(
            id = 1L, menuItemId = 1L, name = "크루아상", emoji = "🥐",
            originalPrice = 3800, discountedPrice = 2660, discountRate = 30,
            remainingItems = 3, totalItems = 5, status = "AVAILABLE", pickupTimeSlot = "19:00~20:00",
        ),
        SaleItemApiResponse(
            id = 2L, menuItemId = 2L, name = "소금빵", emoji = "🍞",
            originalPrice = 2500, discountedPrice = 2000, discountRate = 20,
            remainingItems = 5, totalItems = 8, status = "AVAILABLE", pickupTimeSlot = "19:30~20:30",
        ),
        SaleItemApiResponse(
            id = 3L, menuItemId = 3L, name = "바게트", emoji = "🥖",
            originalPrice = 4500, discountedPrice = 3375, discountRate = 25,
            remainingItems = 0, totalItems = 3, status = "SOLD_OUT", pickupTimeSlot = "18:00~19:00",
        ),
        SaleItemApiResponse(
            id = 4L, menuItemId = 4L, name = "시나몬롤", emoji = "🌀",
            originalPrice = 3200, discountedPrice = 2240, discountRate = 30,
            remainingItems = 2, totalItems = 4, status = "AVAILABLE", pickupTimeSlot = "20:00~21:00",
        ),
    )

    private val orders = mutableListOf(
        OrderApiResponse(
            id = 1L, orderNumber = "ORD-2405-001", storeId = 1L, storeName = "남산 베이커리",
            customerName = "김민준", status = "PENDING", pickupCode = "2847", pickupTime = "19:30",
            totalAmount = 5320, createdAt = "2024-05-15T18:45:00",
            items = listOf(OrderItemApiResponse(1L, 1L, "크루아상", "🥐", 2, 2660)),
        ),
        OrderApiResponse(
            id = 2L, orderNumber = "ORD-2405-002", storeId = 1L, storeName = "남산 베이커리",
            customerName = "이서연", status = "PENDING", pickupCode = "1593", pickupTime = "19:00",
            totalAmount = 6000, createdAt = "2024-05-15T18:50:00",
            items = listOf(OrderItemApiResponse(2L, 2L, "소금빵", "🍞", 3, 2000)),
        ),
        OrderApiResponse(
            id = 3L, orderNumber = "ORD-2405-003", storeId = 1L, storeName = "남산 베이커리",
            customerName = "박지호", status = "CONFIRMED", pickupCode = "7621", pickupTime = "20:00",
            totalAmount = 3375, createdAt = "2024-05-15T18:30:00",
            items = listOf(OrderItemApiResponse(3L, 3L, "바게트", "🥖", 1, 3375)),
        ),
        OrderApiResponse(
            id = 4L, orderNumber = "ORD-2405-004", storeId = 1L, storeName = "남산 베이커리",
            customerName = "최수아", status = "CONFIRMED", pickupCode = "0000", pickupTime = "19:00",
            totalAmount = 4480, createdAt = "2024-05-15T18:20:00",
            items = listOf(OrderItemApiResponse(4L, 4L, "시나몬롤", "🌀", 2, 2240)),
        ),
        OrderApiResponse(
            id = 5L, orderNumber = "ORD-2405-005", storeId = 1L, storeName = "남산 베이커리",
            customerName = "정우진", status = "PICKED_UP", pickupCode = "3948", pickupTime = "18:30",
            totalAmount = 2660, createdAt = "2024-05-15T17:30:00",
            items = listOf(OrderItemApiResponse(1L, 1L, "크루아상", "🥐", 1, 2660)),
        ),
        OrderApiResponse(
            id = 6L, orderNumber = "ORD-2405-006", storeId = 1L, storeName = "남산 베이커리",
            customerName = "강하은", status = "PICKED_UP", pickupCode = "5731", pickupTime = "18:00",
            totalAmount = 6750, createdAt = "2024-05-15T17:00:00",
            items = listOf(OrderItemApiResponse(3L, 3L, "바게트", "🥖", 2, 3375)),
        ),
    )

    private val notifications = mutableListOf(
        NotificationApiResponse(
            id = 1L, storeId = 1L, storeName = "남산 베이커리",
            message = "오늘 베이커리 마감 1시간 전! 크루아상과 소금빵이 할인 중이에요 🥐",
            sentAt = "2024-05-15T18:00:00", recipientCount = 42,
        ),
        NotificationApiResponse(
            id = 2L, storeId = 1L, storeName = "남산 베이커리",
            message = "오늘의 마감 특가! 바게트 25% 할인, 수량 한정입니다 🥖",
            sentAt = "2024-05-14T19:30:00", recipientCount = 38,
        ),
    )

    // ── 인증 ─────────────────────────────────────────────────────
    override suspend fun kakaoLogin(req: KakaoLoginRequest) = ApiResponse<LoginData>(400, "mock", null)
    override suspend fun debugLogin(req: DebugLoginRequest) = ApiResponse<LoginData>(400, "mock", null)
    override suspend fun refresh(req: TokenRefreshRequest) = ApiResponse<TokenData>(400, "mock", null)
    override suspend fun logout() = ApiResponse(200, "ok", Unit)

    // ── 멤버 ─────────────────────────────────────────────────────
    override suspend fun getMyInfo() = ApiResponse(200, "ok", MemberMeResponse(
        memberId = 1L, nickname = "테스트판매자", role = "SELLER"
    ))

    // ── 판매 상품 ─────────────────────────────────────────────────
    override suspend fun getSaleItems() = ApiResponse(200, "ok", saleItems.toList())

    override suspend fun createSaleItem(req: SaleItemCreateRequest): ApiResponse<SaleItemApiResponse> {
        val newId = (saleItems.maxOfOrNull { it.id } ?: 0L) + 1L
        val item = SaleItemApiResponse(
            id = newId,
            menuItemId = req.menuItemId,
            name = req.name,
            emoji = menus.firstOrNull { it.id == req.menuItemId }?.emoji,
            originalPrice = req.originalPrice,
            discountedPrice = req.discountPrice,
            discountRate = null,
            remainingItems = req.quantityTotal,
            totalItems = req.quantityTotal,
            status = "AVAILABLE",
            pickupStart = req.pickupStart,
            pickupEnd = req.pickupEnd,
        )
        saleItems.add(item)
        return ApiResponse(200, "ok", item)
    }

    override suspend fun createSaleItemWithImage(
        name: RequestBody, discountPrice: RequestBody, originalPrice: RequestBody,
        quantityTotal: RequestBody, pickupStart: RequestBody, pickupEnd: RequestBody,
        availableDate: RequestBody, allergenInfo: RequestBody?, images: List<MultipartBody.Part>?,
    ): ApiResponse<SaleItemApiResponse> = ApiResponse(400, "not supported in mock", null)

    override suspend fun updateSaleStatus(id: Long, req: UpdateSaleStatusRequest): ApiResponse<SaleItemApiResponse> {
        val idx = saleItems.indexOfFirst { it.id == id }
        if (idx < 0) return ApiResponse(404, "not found", null)
        saleItems[idx] = saleItems[idx].copy(status = req.status)
        return ApiResponse(200, "ok", saleItems[idx])
    }

    override suspend fun updateSaleItem(id: Long, req: UpdateSaleItemRequest): ApiResponse<SaleItemApiResponse> {
        val idx = saleItems.indexOfFirst { it.id == id }
        if (idx < 0) return ApiResponse(404, "not found", null)
        saleItems[idx] = saleItems[idx].copy(
            discountedPrice = req.discountPrice,
            remainingItems = req.quantityRemaining,
        )
        return ApiResponse(200, "ok", saleItems[idx])
    }

    override suspend fun cancelSaleItem(id: Long): ApiResponse<Unit> {
        val idx = saleItems.indexOfFirst { it.id == id }
        if (idx >= 0) saleItems[idx] = saleItems[idx].copy(status = "CANCELLED")
        return ApiResponse(200, "ok", Unit)
    }

    // ── 메뉴 ─────────────────────────────────────────────────────
    override suspend fun getMenus() = ApiResponse(200, "ok", menus.toList())

    override suspend fun addMenu(req: MenuItemRequest): ApiResponse<MenuItemApiResponse> {
        val menu = MenuItemApiResponse(
            id = (menus.maxOfOrNull { it.id } ?: 0L) + 1L,
            name = req.name,
            emoji = req.emoji,
            originalPrice = req.originalPrice,
        )
        menus.add(menu)
        return ApiResponse(200, "ok", menu)
    }

    override suspend fun addMenuWithImage(
        name: RequestBody,
        originalPrice: RequestBody,
        description: RequestBody?,
        allergenInfo: RequestBody?,
        image: MultipartBody.Part?,
    ): ApiResponse<MenuItemApiResponse> = ApiResponse(400, "not supported in mock", null)

    override suspend fun updateMenu(menuItemId: Long, req: MenuItemUpdateRequest): ApiResponse<MenuItemApiResponse> {
        val idx = menus.indexOfFirst { it.id == menuItemId }
        if (idx < 0) return ApiResponse(404, "not found", null)
        menus[idx] = menus[idx].copy(
            name = req.name ?: menus[idx].name,
            originalPrice = req.originalPrice ?: menus[idx].originalPrice,
        )
        return ApiResponse(200, "ok", menus[idx])
    }

    override suspend fun deleteMenu(menuItemId: Long): ApiResponse<Unit> {
        menus.removeIf { it.id == menuItemId }
        return ApiResponse(200, "ok", Unit)
    }

    // ── 주문 ─────────────────────────────────────────────────────
    override suspend fun getOrders() = ApiResponse(200, "ok", orders.toList())

    override suspend fun updateOrderStatus(orderId: Long, req: UpdateOrderStatusRequest): ApiResponse<OrderApiResponse> {
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx < 0) return ApiResponse(404, "not found", null)
        orders[idx] = orders[idx].copy(status = req.status)
        return ApiResponse(200, "ok", orders[idx])
    }

    override suspend fun confirmPickup(orderId: Long, req: ConfirmPickupRequest): ApiResponse<OrderApiResponse> {
        val idx = orders.indexOfFirst { it.id == orderId && it.pickupCode == req.pickupCode }
        if (idx < 0) return ApiResponse(400, "invalid pickup code", null)
        orders[idx] = orders[idx].copy(status = "PICKED_UP")
        return ApiResponse(200, "ok", orders[idx])
    }

    override suspend fun verifyPickupCode(code: String): ApiResponse<OrderApiResponse> {
        val order = orders.firstOrNull { it.pickupCode == code && it.status == "CONFIRMED" }
        return ApiResponse(200, if (order != null) "ok" else "not found", order)
    }

    // ── 가게 ─────────────────────────────────────────────────────
    override suspend fun getMyStore() = ApiResponse(200, "ok", _store)

    override suspend fun updateStore(req: UpdateStoreRequest): ApiResponse<StoreApiResponse> {
        _store = _store.copy(
            address = req.address ?: _store.address,
            phone = req.phone ?: _store.phone,
            closingTime = req.closingTime ?: _store.closingTime,
        )
        return ApiResponse(200, "ok", _store)
    }

    // ── 알림 ─────────────────────────────────────────────────────
    override suspend fun sendNotification(req: SendNotificationRequest): ApiResponse<NotificationApiResponse> {
        val notif = NotificationApiResponse(
            id = (notifications.maxOfOrNull { it.id } ?: 0L) + 1L,
            storeId = 1L,
            storeName = "남산 베이커리",
            message = req.message,
            sentAt = java.time.LocalDateTime.now().toString().substring(0, 19),
            recipientCount = 35,
        )
        notifications.add(0, notif)
        return ApiResponse(200, "ok", notif)
    }

    override suspend fun getNotifications() = ApiResponse(200, "ok", notifications.toList())

    override suspend fun getSettlements(year: Int, month: Int): ApiResponse<SettlementListResponse> {
        val total = orders.filter { it.status == "PICKED_UP" || it.status == "COMPLETED" }.sumOf { it.totalAmount }
        val fee = (total * 0.03).toInt()
        return ApiResponse(
            200,
            "ok",
            SettlementListResponse(
                listOf(
                    SettlementItem(
                        settlementId = 0,
                        periodStart = "%04d-%02d-01".format(year, month),
                        periodEnd = "%04d-%02d-%02d".format(year, month, java.time.YearMonth.of(year, month).lengthOfMonth()),
                        totalSales = total,
                        platformFee = fee,
                        settlementAmount = total - fee,
                        status = "PENDING",
                        settledAt = null,
                    )
                )
            )
        )
    }

    override suspend fun requestWithdrawal(req: SettlementWithdrawRequest): ApiResponse<SettlementItem> {
        val item = getSettlements(req.year, req.month).data?.settlements?.firstOrNull()
            ?: SettlementItem(0, "${req.year}-${req.month}-01", "${req.year}-${req.month}-${java.time.YearMonth.of(req.year, req.month).lengthOfMonth()}", 0, 0, 0, "PENDING", null)
        return ApiResponse(200, "ok", item)
    }

    // ── 매출 ─────────────────────────────────────────────────────
    override suspend fun getSales(period: String, date: String?): ApiResponse<SalesApiResponse> {
        val weeklyBase = listOf(
            DailySales("05.09", 32500), DailySales("05.10", 28000),
            DailySales("05.11", 0),     DailySales("05.12", 41000),
            DailySales("05.13", 35500), DailySales("05.14", 52000),
            DailySales("05.15", 18000),
        )
        val salesData = when (period) {
            "DAY" -> listOf(DailySales("05.15", 18000))
            "MONTH" -> (1..15).map { d ->
                DailySales("05.%02d".format(d), if (d % 7 == 0) 0 else 12000 + d * 1300)
            }
            else -> weeklyBase
        }

        val topMenus = listOf(
            TopMenu("크루아상", "🥐", 42),
            TopMenu("소금빵", "🍞", 31),
            TopMenu("시나몬롤", "🌀", 24),
        )

        return ApiResponse(
            200, "ok",
            SalesApiResponse(
                todaySales = 18000,
                todayOrderCount = orders.count { it.status == "PICKED_UP" },
                salesData = salesData,
                topMenus = topMenus,
            ),
        )
    }
}
