# 득템시루 판매자 앱

마감 할인 상품을 등록하고 주문, 매출, 알림을 관리하는 득템시루 판매자용 Android 앱입니다.

## 개요

| 항목 | 내용 |
| --- | --- |
| 프로젝트 | `deuktemsiru_seller` |
| 플랫폼 | Android |
| 패키지 | `com.example.deuktemsiru_seller` |
| 백엔드 기본 주소 | `http://10.0.2.2:8080/` |
| 인증 | 로그인 후 발급된 JWT를 `Authorization: Bearer {token}` 헤더로 전송 |

## 주요 기능

| 기능 | 설명 |
| --- | --- |
| 로그인 | 샘플 판매자 계정으로 로그인하고 사용자 세션을 관리합니다. |
| 홈 | 오늘의 주문, 매출, 매장 운영 현황을 확인합니다. |
| 주문 관리 | 신규 주문을 확인하고 주문 상태를 변경합니다. |
| 메뉴 등록 | 단계별 입력 화면에서 할인 메뉴와 메뉴 이미지를 등록합니다. |
| 매출 분석 | 기간별 매출과 인기 메뉴를 차트로 확인합니다. |
| 가게 정보 | 매장 기본 정보와 등록된 메뉴 목록을 조회, 수정, 삭제합니다. |
| 알림 | 구매자에게 알림을 발송하고 발송 내역을 조회합니다. |

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin 2.1.21 |
| UI | XML View, ViewBinding, Material Components 1.13.0 |
| 아키텍처 | Single Activity, Fragment, 별도 메뉴 등록 Activity |
| 네트워크 | Retrofit 2.11.0, Gson Converter, OkHttp Interceptor, Multipart |
| 비동기 | Kotlin Coroutines Android 1.8.1 |
| Android | minSdk 29, targetSdk 36, compileSdk 36.1 |
| 빌드 | Gradle Kotlin DSL, Android Gradle Plugin 9.0.1 |

## 실행 방법

### 1. 백엔드 실행

앱은 Android Emulator에서 로컬 백엔드에 접근하기 위해 `http://10.0.2.2:8080/`을 사용합니다. 먼저 백엔드를 실행합니다.

```bash
cd ../deuktemsiru_backend
./gradlew bootRun
```

### 2. 앱 실행

Android Studio에서 `deuktemsiru_seller` 프로젝트를 열고 `app` 실행 구성을 사용합니다. 터미널에서는 디버그 APK를 빌드할 수 있습니다.

```bash
./gradlew assembleDebug
```

### 3. 서버 주소 변경

실기기 또는 원격 서버에 연결할 때는 `RetrofitClient.kt`의 `BASE_URL`을 변경합니다.

```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"
```

## 샘플 계정

| 매장 | 이메일 | 비밀번호 |
| --- | --- | --- |
| 영희네 베이커리 | `bakery@test.com` | `1234` |
| 맛있는 도시락 | `lunchbox@test.com` | `1234` |
| 그린 샐러드 | `salad@test.com` | `1234` |
| 커피향기 | `cafe1@test.com` | `1234` |
| 달콤카페 | `cafe2@test.com` | `1234` |
| 파리크라상 | `bakery2@test.com` | `1234` |

## 프로젝트 구조

```text
app/src/main/java/com/example/deuktemsiru_seller/
├── MainActivity.kt
├── data/             # 세션 관리
├── network/          # Retrofit 클라이언트, API 인터페이스, DTO
└── ui/
    ├── home/         # 홈 대시보드
    ├── order/        # 주문 관리
    ├── registration/ # 메뉴 등록
    ├── sales/        # 매출 분석
    ├── store/        # 가게 정보, 메뉴 관리
    └── notification/ # 알림 발송/내역
```

## 연동 API

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 로그인 |
| `GET` | `/api/seller/store?sellerId={sellerId}` | 내 가게 정보 조회 |
| `PATCH` | `/api/seller/store?sellerId={sellerId}` | 내 가게 정보 수정 |
| `POST` | `/api/seller/menus?sellerId={sellerId}` | 메뉴 등록(JSON 또는 multipart/form-data) |
| `PATCH` | `/api/seller/menus/{menuItemId}?sellerId={sellerId}` | 메뉴 수정 |
| `DELETE` | `/api/seller/menus/{menuItemId}?sellerId={sellerId}` | 메뉴 삭제 |
| `GET` | `/api/seller/orders?sellerId={sellerId}` | 주문 목록 조회 |
| `PATCH` | `/api/seller/orders/{orderId}?sellerId={sellerId}` | 주문 상태 변경 |
| `GET` | `/api/seller/sales?sellerId={sellerId}&period={period}&offset={offset}` | 매출 통계 조회 |
| `POST` | `/api/seller/notifications?sellerId={sellerId}` | 알림 발송 |
| `GET` | `/api/seller/notifications?sellerId={sellerId}` | 알림 내역 조회 |
