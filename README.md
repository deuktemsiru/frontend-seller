# 득템시루 판매자 앱

마감 임박 할인 상품을 등록하고 주문을 처리하는 **득템시루** 판매자용 Android 앱입니다. 메뉴 등록, 판매 상품 관리, 주문 접수 및 픽업 검증, 매출 분석, 고객 알림 발송을 한 앱에서 처리할 수 있습니다.

## 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 프로젝트 | `deuktemsiru_seller` |
| 플랫폼 | Android |
| 패키지 | `com.example.deuktemsiru_seller` |
| 앱 버전 | 1.0 |
| minSdk | 29 (Android 10) |
| targetSdk | 36 |
| compileSdk | 36.1 |
| 백엔드 기본 주소 | `http://10.0.2.2:8080/` (에뮬레이터 → 로컬 PC) |
| 인증 방식 | `Authorization: Bearer {accessToken}` |

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Kotlin |
| UI | XML View, ViewBinding, Material Components 1.13.0, Google Flexbox 3.0.0 |
| 아키텍처 | LoginActivity 진입 + MainActivity(하단 탭) + 복잡한 흐름은 별도 Activity |
| 네트워크 | Retrofit 2.11.0, Gson Converter, OkHttp Interceptor, Multipart 업로드 |
| 비동기 | Kotlin Coroutines 1.8.1 |
| QR 스캔 | ZXing Android Embedded 4.3.0 |
| 소셜 로그인 | Kakao SDK User 2.21.0 |
| 커스텀 UI | `BarChartView`, `PieChartView`, `RadiusMapView` (자체 구현) |
| 알림 | `LocalNotificationHelper` (주문 접수 알림 채널) |
| 빌드 | Gradle Kotlin DSL, Android Gradle Plugin 9.0.0 |

## 주요 기능

| 화면 | 설명 |
| --- | --- |
| 로그인 | Kakao SDK 소셜 로그인, 백엔드 JWT 발급 |
| 홈 대시보드 | 매장 운영 상태, 오늘 매출, 주문 현황, 활성 판매 상품 수 |
| 메뉴 등록 | 단계별 입력 (이름·가격·할인율·수량·픽업 시간·알레르기 정보·이미지) |
| 판매 상품 관리 | 등록된 메뉴를 기반으로 당일 판매 상품 생성, 상태 변경, 삭제 |
| 주문 관리 | 신규 → 준비 중 → 픽업 대기 → 완료 탭 전환, 주문 상세 바텀시트 |
| 픽업 검증 | QR 스캔 또는 코드 직접 입력으로 픽업 완료 처리 |
| 매출 분석 | 주간 / 월간 / 연간 Bar·Pie 차트, 인기 메뉴 순위 |
| 가게 정보 | 매장 설명·연락처 수정, 등록 메뉴 목록 관리 |
| 알림 발송 | 반경 내 구매자에게 푸시 알림 발송, 발송 내역 조회 (`RadiusMapView`) |
| 정산 | 월별 정산 내역, 출금 신청 |
| 마이페이지 | 계정 정보, 알림 수신 설정, 로그아웃 |

## 화면 흐름

```
LoginActivity
└── MainActivity (하단 탭)
    ├── 홈 대시보드
    ├── 판매 상품
    │   └── ProductListingActivity     # 메뉴 선택 → 상품 등록
    ├── 주문
    │   ├── OrderDetailBottomSheet     # 주문 상세, 상태 변경
    │   └── PickupVerifyActivity       # QR 스캔 / 코드 입력
    ├── 매출                           # 차트, 인기 메뉴
    ├── 가게
    │   └── MenuRegistrationActivity   # 단계별 메뉴 등록
    ├── 알림                           # 발송 / 내역
    └── 마이페이지
        ├── AccountInfoActivity        # 계정 정보
        ├── SettlementActivity         # 정산 내역, 출금 신청
        └── NotificationSettingsActivity
```

## 시작하기

### 사전 준비

- Android Studio 최신 버전
- Kakao Developers 앱 등록 후 네이티브 앱 키 발급

### 1. 백엔드 실행

에뮬레이터에서 로컬 PC 백엔드에 연결하려면 기본 주소(`10.0.2.2:8080`)를 사용합니다.

```bash
cd ../deuktemsiru_backend
./gradlew bootRun
```

### 2. API 키 설정

프로젝트 루트의 `local.properties`에 카카오 네이티브 앱 키를 추가합니다.

```properties
KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key
```

`local.properties`는 `.gitignore`에 포함되어 있으므로 키가 외부에 노출되지 않습니다.

### 3. (선택) 원격 백엔드 연결

실기기 테스트나 운영 서버에 연결할 때는 `local.properties`에 아래 항목을 추가하고 앱을 다시 빌드합니다.

```properties
BACKEND_BASE_URL=http://your-backend-host:8080/
```

### 4. 앱 빌드 및 실행

Android Studio에서 `app` 실행 구성을 실행하거나 터미널에서 빌드합니다.

```bash
./gradlew assembleDebug
```

릴리스 빌드 시 `local.properties`에 서명 정보를 추가합니다.

```properties
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

## 프로젝트 구조

```
app/src/main/java/com/example/deuktemsiru_seller/
├── DeuktemsiruSellerApp.kt          # Application 클래스, Kakao SDK 초기화
├── MainActivity.kt                  # 하단 탭 네비게이션 호스트
├── data/
│   └── SessionManager.kt            # Access / Refresh Token 로컬 저장
├── network/
│   ├── RetrofitClient.kt            # OkHttp + 인증 인터셉터 설정
│   ├── ApiService.kt                # Retrofit 인터페이스 (백엔드 API)
│   ├── ApiModels.kt                 # 요청 / 응답 DTO
│   ├── MockApiService.kt            # 개발용 Mock 서비스
│   └── SampleData.kt               # Mock 샘플 데이터
├── util/
│   └── LocalNotificationHelper.kt  # 로컬 푸시 알림 채널 관리
└── ui/
    ├── auth/                        # LoginActivity, RegisterActivity
    ├── home/                        # HomeFragment (대시보드)
    ├── product/                     # ProductFragment, ProductListingActivity
    ├── registration/                # MenuRegistrationActivity (단계별 메뉴 등록)
    ├── order/                       # OrderFragment, OrderDetailBottomSheet, PickupVerifyActivity
    ├── sales/                       # SalesFragment, BarChartView, PieChartView
    ├── store/                       # StoreFragment (가게 정보, 메뉴 관리)
    ├── notification/                # NotificationFragment, RadiusMapView
    ├── settings/                    # NotificationSettingsActivity
    └── mypage/                      # MyPageFragment, AccountInfoActivity, SettlementActivity
```

## 연동 API

`RetrofitClient`는 `SessionManager`에서 읽은 Access Token을 모든 요청의 `Authorization` 헤더에 자동으로 첨부합니다.

### 인증

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/kakao/login` | 카카오 로그인 / 자동 회원가입 |
| `POST` | `/api/v1/auth/refresh` | Access Token 갱신 |
| `POST` | `/api/v1/auth/logout` | 로그아웃 |

### 메뉴 / 판매 상품

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/menu-items` | 메뉴 목록 조회 |
| `POST` | `/api/v1/sellers/menu-items` | 메뉴 등록 (JSON 또는 multipart) |
| `PATCH` | `/api/v1/sellers/menu-items/{menuItemId}` | 메뉴 수정 |
| `DELETE` | `/api/v1/sellers/menu-items/{menuItemId}` | 메뉴 삭제 |
| `GET` | `/api/v1/sellers/products` | 판매 상품 목록 조회 |
| `POST` | `/api/v1/sellers/products` | 판매 상품 등록 |
| `PATCH` | `/api/v1/sellers/products/{productId}/status` | 판매 상품 상태 변경 |
| `DELETE` | `/api/v1/sellers/products/{productId}` | 판매 상품 삭제 |

### 주문 / 픽업

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/orders` | 주문 목록 조회 |
| `PATCH` | `/api/v1/sellers/orders/{orderId}/status` | 주문 상태 변경 |
| `GET` | `/api/v1/sellers/pickup/verify?code={code}` | 픽업 코드 검증 |

### 매장 / 알림 / 매출 / 정산

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/stores/my` | 내 매장 정보 조회 |
| `PUT` | `/api/v1/sellers/stores/my` | 내 매장 정보 수정 |
| `POST` | `/api/v1/sellers/notifications` | 고객 알림 발송 |
| `GET` | `/api/v1/sellers/notifications` | 알림 발송 내역 조회 |
| `GET` | `/api/v1/sellers/sales/summary?period={period}&date={date}` | 매출 통계 조회 |
| `GET` | `/api/v1/sellers/settlements?year={year}&month={month}` | 월별 정산 내역 조회 |
| `POST` | `/api/v1/sellers/settlements/withdrawals` | 출금 신청 |

## Android 권한

앱이 요청하는 주요 권한은 다음과 같습니다.

| 권한 | 용도 |
| --- | --- |
| `CAMERA` | QR 코드 스캔 (픽업 검증) |
| `INTERNET` | 백엔드 API 통신 |
| `POST_NOTIFICATIONS` | 주문 접수 로컬 알림 (Android 13 이상) |
