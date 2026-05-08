package com.example.deuktemsiru_seller.network

object SampleData {

    data class Account(
        val email: String,
        val password: String,
        val sellerId: Long,
        val storeName: String,
        val store: StoreApiResponse,
    ) {
        val token = "sample_$sellerId"
    }

    val accounts = listOf(
        Account(
            email = "bakery@test.com",
            password = "1234",
            sellerId = 1L,
            storeName = "영희네 베이커리",
            store = StoreApiResponse(
                id = 1L,
                name = "영희네 베이커리",
                category = "베이커리",
                emoji = "🥐",
                rating = 4.5f,
                address = "서울시 종로구 종로1길 10",
                phone = "02-1234-5678",
                closingTime = "21:00",
                menus = listOf(
                    MenuItemApiResponse(id = 101L, name = "크루아상", emoji = "🥐", originalPrice = 4000, discountedPrice = 2800, discountRate = 30, remainingItems = 5, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                    MenuItemApiResponse(id = 102L, name = "식빵", emoji = "🍞", originalPrice = 5000, discountedPrice = 3500, discountRate = 30, remainingItems = 3, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                ),
            ),
        ),
        Account(
            email = "lunchbox@test.com",
            password = "1234",
            sellerId = 2L,
            storeName = "맛있는 도시락",
            store = StoreApiResponse(
                id = 2L,
                name = "맛있는 도시락",
                category = "도시락",
                emoji = "🍱",
                rating = 4.3f,
                address = "서울시 마포구 홍대입구로 25",
                phone = "02-2345-6789",
                closingTime = "20:00",
                menus = listOf(
                    MenuItemApiResponse(id = 201L, name = "제육볶음 도시락", emoji = "🍱", originalPrice = 8000, discountedPrice = 5600, discountRate = 30, remainingItems = 4, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                    MenuItemApiResponse(id = 202L, name = "연어 도시락", emoji = "🐟", originalPrice = 10000, discountedPrice = 7000, discountRate = 30, remainingItems = 2, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                ),
            ),
        ),
        Account(
            email = "salad@test.com",
            password = "1234",
            sellerId = 3L,
            storeName = "그린 샐러드",
            store = StoreApiResponse(
                id = 3L,
                name = "그린 샐러드",
                category = "샐러드",
                emoji = "🥗",
                rating = 4.7f,
                address = "서울시 강남구 테헤란로 30",
                phone = "02-3456-7890",
                closingTime = "20:30",
                menus = listOf(
                    MenuItemApiResponse(id = 301L, name = "닭가슴살 샐러드", emoji = "🥗", originalPrice = 9000, discountedPrice = 6300, discountRate = 30, remainingItems = 6, isSoldOut = false, pickupTimeSlot = "17:00-18:00"),
                    MenuItemApiResponse(id = 302L, name = "그린 샐러드", emoji = "🥬", originalPrice = 7000, discountedPrice = 4900, discountRate = 30, remainingItems = 4, isSoldOut = false, pickupTimeSlot = "17:00-18:00"),
                ),
            ),
        ),
        Account(
            email = "cafe1@test.com",
            password = "1234",
            sellerId = 4L,
            storeName = "커피향기",
            store = StoreApiResponse(
                id = 4L,
                name = "커피향기",
                category = "카페",
                emoji = "☕",
                rating = 4.4f,
                address = "서울시 서초구 서초대로 15",
                phone = "02-4567-8901",
                closingTime = "22:00",
                menus = listOf(
                    MenuItemApiResponse(id = 401L, name = "아메리카노", emoji = "☕", originalPrice = 4500, discountedPrice = 3150, discountRate = 30, remainingItems = 10, isSoldOut = false, pickupTimeSlot = "19:00-20:00"),
                    MenuItemApiResponse(id = 402L, name = "카페라떼", emoji = "🥛", originalPrice = 5500, discountedPrice = 3850, discountRate = 30, remainingItems = 8, isSoldOut = false, pickupTimeSlot = "19:00-20:00"),
                ),
            ),
        ),
        Account(
            email = "cafe2@test.com",
            password = "1234",
            sellerId = 5L,
            storeName = "달콤카페",
            store = StoreApiResponse(
                id = 5L,
                name = "달콤카페",
                category = "카페",
                emoji = "🧁",
                rating = 4.6f,
                address = "서울시 용산구 이태원로 55",
                phone = "02-5678-9012",
                closingTime = "21:30",
                menus = listOf(
                    MenuItemApiResponse(id = 501L, name = "딸기 케이크", emoji = "🍓", originalPrice = 7000, discountedPrice = 4900, discountRate = 30, remainingItems = 3, isSoldOut = false, pickupTimeSlot = "19:00-20:00"),
                    MenuItemApiResponse(id = 502L, name = "초코 쿠키", emoji = "🍪", originalPrice = 3000, discountedPrice = 2100, discountRate = 30, remainingItems = 7, isSoldOut = false, pickupTimeSlot = "19:00-20:00"),
                ),
            ),
        ),
        Account(
            email = "bakery2@test.com",
            password = "1234",
            sellerId = 6L,
            storeName = "파리크라상",
            store = StoreApiResponse(
                id = 6L,
                name = "파리크라상",
                category = "베이커리",
                emoji = "🥖",
                rating = 4.2f,
                address = "서울시 성동구 왕십리로 200",
                phone = "02-6789-0123",
                closingTime = "20:00",
                menus = listOf(
                    MenuItemApiResponse(id = 601L, name = "바게트", emoji = "🥖", originalPrice = 6000, discountedPrice = 4200, discountRate = 30, remainingItems = 5, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                    MenuItemApiResponse(id = 602L, name = "마카롱", emoji = "🎂", originalPrice = 3500, discountedPrice = 2450, discountRate = 30, remainingItems = 6, isSoldOut = false, pickupTimeSlot = "18:00-19:00"),
                ),
            ),
        ),
    )

    val notices = listOf(
        NoticeApiResponse(
            id = 1L,
            title = "🎉 득템시루 런칭 이벤트",
            content = "5월 한 달 판매자 수수료 0% 이벤트 진행 중!",
            isImportant = true,
            createdAt = "2026-05-01",
        ),
        NoticeApiResponse(
            id = 2L,
            title = "픽업 코드 시스템 개선",
            content = "고객 픽업 코드가 4자리 숫자로 변경됐습니다.",
            isImportant = false,
            createdAt = "2026-04-28",
        ),
    )

    fun findByCredentials(email: String, password: String): Account? =
        accounts.find { it.email == email && it.password == password }

    fun findById(sellerId: Long): Account? =
        accounts.find { it.sellerId == sellerId }
}