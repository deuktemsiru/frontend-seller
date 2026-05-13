# 득템시루 판매자 앱

득템시루 판매자용 Android 앱입니다. 판매자는 카카오로 로그인한 뒤 매장 정보, 메뉴, 판매 상품, 주문, 픽업 검증, 매출, 알림을 관리할 수 있습니다.

## 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 프로젝트 | `deuktemsiru_seller` |
| 플랫폼 | Android |
| 패키지 | `com.example.deuktemsiru_seller` |
| 앱 버전 | `1.0` |
| minSdk | 29 |
| targetSdk | 36 |
| compileSdk | 36.1 |
| 백엔드 기본 주소 | `http://10.0.2.2:8080/` |
| 인증 방식 | `Authorization: Bearer {accessToken}` |

## 주요 기능

| 기능 | 설명 |
| --- | --- |
| 카카오 로그인 | Kakao SDK로 로그인하고 백엔드 카카오 인증 API와 연동합니다. |
| 홈 대시보드 | 매장 운영 상태, 오늘 매출, 주문 현황, 활성 판매 상품을 확인합니다. |
| 판매 상품 관리 | 등록된 메뉴를 기반으로 할인율, 수량, 픽업 시간을 설정해 판매 상품을 등록합니다. |
| 메뉴 등록 | 단계별 입력 화면에서 메뉴명, 가격, 할인율, 수량, 픽업 시간, 이미지를 등록합니다. |
| 주문 관리 | 신규, 준비 중, 픽업 대기, 완료 주문을 탭으로 관리하고 상태를 변경합니다. |
| 픽업 검증 | 픽업 코드를 직접 입력하거나 QR 스캔 화면으로 검증 흐름을 시작합니다. |
| 매출 분석 | 주간, 월간, 연간 매출 차트와 인기 메뉴를 확인합니다. |
| 가게 정보 | 매장 설명, 연락처, 운영 정보와 등록 메뉴를 조회/수정/삭제합니다. |
| 알림 | 구매자에게 알림을 발송하고 발송 내역을 조회합니다. |
| 마이페이지 | 계정 정보, 알림 설정, 로그아웃 진입점을 제공합니다. |

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin |
| UI | XML View, ViewBinding, Material Components 1.13.0 |
| 구조 | MainActivity + Fragment, 별도 Activity 기반 등록/인증/검증 화면 |
| 네트워크 | Retrofit 2.11.0, Gson Converter, OkHttp Interceptor, Multipart |
| 비동기 | Kotlin Coroutines Android 1.8.1 |
| 소셜 로그인 | Kakao SDK User 2.21.0 |
| 기타 | Custom `BarChartView`, 로컬 알림 채널 |
| 빌드 | Gradle Kotlin DSL, Android Gradle Plugin 9.0.0 |

## 실행 방법

### 1. 백엔드 실행

Android Emulator에서 로컬 PC의 백엔드에 접근하기 위해 `10.0.2.2`를 사용합니다.

```bash
cd ../deuktemsiru_backend
./gradlew bootRun
```

### 2. 로컬 키 설정

프로젝트 루트의 `local.properties`에 카카오 네이티브 앱 키를 설정합니다.

```properties
KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key
```

릴리스 빌드를 만들 때는 같은 파일에 서명 정보도 추가합니다.

```properties
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

### 3. 앱 실행

Android Studio에서 `deuktemsiru_seller` 프로젝트를 열고 `app` 실행 구성을 실행합니다. 터미널에서는 디버그 APK를 빌드할 수 있습니다.

```bash
./gradlew assembleDebug
```

### 4. 서버 주소 변경

실기기나 원격 서버에 연결하려면 `RetrofitClient.kt`의 `BASE_URL`을 변경합니다.

```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"
```

## 프로젝트 구조

```text
app/src/main/java/com/example/deuktemsiru_seller/
├── DeuktemsiruSellerApp.kt
├── MainActivity.kt
├── data/             # 세션 관리
├── network/          # Retrofit, API 인터페이스, DTO
├── util/             # 로컬 알림 헬퍼
└── ui/
    ├── auth/         # 로그인, 회원가입
    ├── home/         # 홈 대시보드
    ├── product/      # 판매 상품 목록, 판매 상품 등록
    ├── registration/ # 메뉴 등록
    ├── order/        # 주문 관리, 상세 바텀시트, 픽업 검증
    ├── sales/        # 매출 분석, 차트
    ├── store/        # 가게 정보, 메뉴 관리
    ├── notification/ # 알림 발송/내역
    ├── settings/     # 알림 설정
    └── mypage/       # 마이페이지, 계정 정보
```

## 연동 API

`RetrofitClient`는 로그인 후 저장된 Access Token을 모든 요청의 `Authorization` 헤더에 자동으로 첨부합니다.

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/kakao/login` | 카카오 로그인/자동 회원가입 |
| `POST` | `/api/v1/auth/refresh` | Access Token 갱신 |
| `POST` | `/api/v1/auth/logout` | 로그아웃 |
| `GET` | `/api/v1/seller/products` | 판매 상품 목록 조회 |
| `POST` | `/api/v1/seller/products` | 판매 상품 등록 |
| `PATCH` | `/api/v1/seller/products/{id}` | 판매 상품 상태 변경 |
| `DELETE` | `/api/v1/seller/products/{id}` | 판매 상품 취소 |
| `GET` | `/api/v1/seller/menus` | 메뉴 목록 조회 |
| `POST` | `/api/v1/seller/menus` | 메뉴 등록, JSON 또는 multipart |
| `PATCH` | `/api/v1/seller/menus/{menuItemId}` | 메뉴 수정 |
| `DELETE` | `/api/v1/seller/menus/{menuItemId}` | 메뉴 삭제 |
| `GET` | `/api/v1/seller/orders` | 주문 목록 조회 |
| `PATCH` | `/api/v1/seller/orders/{orderId}` | 주문 상태 변경 |
| `GET` | `/api/v1/seller/pickup/verify?code={code}` | 픽업 코드 검증 |
| `GET` | `/api/v1/seller/store` | 내 매장 조회 |
| `PATCH` | `/api/v1/seller/store` | 내 매장 수정 |
| `POST` | `/api/v1/seller/notifications` | 알림 발송 |
| `GET` | `/api/v1/seller/notifications` | 알림 내역 조회 |
| `GET` | `/api/v1/seller/sales?period={period}&offset={offset}` | 매출 통계 조회 |

## 화면 흐름

```text
LoginActivity
└── MainActivity
    ├── 홈
    ├── 판매 상품
    │   └── ProductListingActivity
    ├── 주문
    │   ├── OrderDetailBottomSheet
    │   └── PickupVerifyActivity
    ├── 매출
    ├── 가게
    │   └── MenuRegistrationActivity
    ├── 알림
    └── 마이페이지
        ├── AccountInfoActivity
        └── NotificationSettingsActivity
```
