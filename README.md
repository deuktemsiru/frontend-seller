<h1 align="center">득템시루 Seller App</h1>

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/img_siheung_bi.png" width="180" alt="시흥시 BI"/>
  &nbsp;&nbsp;
  <img src="app/src/main/res/drawable-nodpi/img_siheung_character.png" width="96" alt="시흥시 캐릭터"/>
</p>

<p align="center">
  <b>마감 임박 상품을 빠르게 등록하고, 주문부터 픽업까지 한 번에 관리합니다</b><br/>
  시흥시 지역화폐 시루 기반 마감 할인 서비스를 위한 판매자용 Android 앱
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Retrofit 2-48B983?style=for-the-badge&logo=square&logoColor=white" alt="Retrofit"/>
  <img src="https://img.shields.io/badge/Kakao SDK-FFCD00?style=for-the-badge&logo=kakao&logoColor=black" alt="Kakao SDK"/>
  <img src="https://img.shields.io/badge/ZXing QR-000000?style=for-the-badge&logoColor=white" alt="ZXing QR"/>
</p>

---

## 프로젝트 소개

> **"판매자는 남는 상품을 폐기하지 않고, 구매자는 가까운 할인 상품을 시루로 픽업합니다."**

**득템시루 Seller App**은 지역 소상공인이 마감 임박 상품을 등록하고, 구매자 주문을 접수한 뒤, 픽업 코드 또는 QR 스캔으로 수령을 확정할 수 있는 판매자용 Android 앱입니다. 상품 등록, 주문 처리, 매출 확인, 고객 알림 발송, 정산 신청까지 매장 운영에 필요한 흐름을 하나의 앱으로 연결합니다.

## 프로젝트 요약

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | `deuktemsiru_seller` |
| 앱 역할 | 판매자 앱 |
| 플랫폼 | Android Native |
| 패키지 | `com.example.deuktemsiru_seller` |
| 개발 언어 | Kotlin |
| UI 방식 | XML View + ViewBinding |
| 아키텍처 | LoginActivity + MainActivity 하단 탭 + 기능별 Activity / Fragment |
| 백엔드 기본 주소 | `http://10.0.2.2:8080/` |
| 인증 방식 | JWT Bearer Token |
| minSdk / targetSdk | 29 / 36 |
| compileSdk | 36.1 |
| 버전 | 1.0 |

## 문제 정의

마감 시간이 가까운 상품은 아직 판매 가능한 상태여도 폐기되기 쉽고, 소상공인은 실시간 할인 판매를 운영할 전용 도구가 부족합니다. 득템시루 seller 앱은 이 문제를 다음 흐름으로 해결합니다.

1. 매장의 기본 메뉴를 등록하고 반복 입력을 줄입니다.
2. 당일 판매할 마감 할인 상품을 수량, 가격, 픽업 시간과 함께 빠르게 등록합니다.
3. 들어온 주문을 상태별로 확인하고 준비 상황을 갱신합니다.
4. 픽업 코드 또는 QR 스캔으로 현장 수령을 검증합니다.
5. 매출, 인기 메뉴, 정산 내역을 확인해 운영 의사결정에 활용합니다.
6. 반경 내 구매자에게 알림을 발송해 재고 소진 가능성을 높입니다.

## 주요 기능

| 기능 | 구현 내용 |
| --- | --- |
| 로그인 | Debug 로그인, Mock 로그인, Kakao 로그인, 판매자 권한 검증, JWT 저장 |
| 홈 대시보드 | 영업 상태, 오늘 매출, 주문 현황, 활성 판매 상품 수 요약 |
| 메뉴 등록 | 메뉴명, 원가, 설명, 알레르기 정보, 이미지 등록 |
| 판매 상품 등록 | 메뉴 기반 빠른 등록, 할인 가격, 수량, 제조 시간, 픽업 시간 설정 |
| 판매 상품 관리 | 판매중 / 품절 / 판매 종료 상태 변경, 상세 BottomSheet, 수정, 삭제 |
| 주문 관리 | 신규, 준비 중, 픽업 대기, 완료 상태별 목록과 주문 상세 처리 |
| 픽업 검증 | ZXing QR 스캔 또는 픽업 코드 직접 입력으로 수령 확정 |
| 매출 분석 | 주간 / 월간 / 연간 매출 차트, 결제 수단 비율, 인기 메뉴 순위 |
| 가게 정보 | 내 매장 정보 조회와 수정, 등록 메뉴 목록 관리 |
| 고객 알림 | 반경 기반 알림 발송, 미리보기, 발송 내역, `RadiusMapView` 시각화 |
| 정산 | 월별 정산 내역 조회, 출금 신청 |
| 마이페이지 | 계정 정보, 알림 설정, 로그아웃 |

## 사용자 플로우

```text
로그인
└─ 메인
   ├─ 홈 대시보드
   ├─ 판매 상품
   │  ├─ 상품 상세
   │  └─ 상품 등록
   │     ├─ 메뉴 선택
   │     ├─ 가격 / 할인 설정
   │     └─ 수량 / 픽업 시간 설정
   ├─ 주문
   │  ├─ 주문 상세
   │  └─ 픽업 검증
   ├─ 매출
   ├─ 가게
   │  └─ 메뉴 등록
   ├─ 알림
   └─ 마이페이지
      ├─ 계정 정보
      ├─ 알림 설정
      └─ 정산
```

## 기술 스택

### Android

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Android Gradle Plugin 9.0.0-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Gradle Plugin"/>
  <img src="https://img.shields.io/badge/XML View-3DDC84?style=flat-square&logo=android&logoColor=white" alt="XML View"/>
  <img src="https://img.shields.io/badge/ViewBinding-757575?style=flat-square&logo=android&logoColor=white" alt="ViewBinding"/>
  <img src="https://img.shields.io/badge/Material Components 1.13.0-757575?style=flat-square&logo=materialdesign&logoColor=white" alt="Material Components"/>
  <img src="https://img.shields.io/badge/Flexbox 3.0.0-4285F4?style=flat-square&logoColor=white" alt="Google Flexbox"/>
</p>

### Network & Auth

<p>
  <img src="https://img.shields.io/badge/Retrofit 2.11.0-48B983?style=flat-square&logo=square&logoColor=white" alt="Retrofit"/>
  <img src="https://img.shields.io/badge/Gson Converter-4285F4?style=flat-square&logo=google&logoColor=white" alt="Gson"/>
  <img src="https://img.shields.io/badge/OkHttp Authenticator-000000?style=flat-square&logoColor=white" alt="OkHttp Authenticator"/>
  <img src="https://img.shields.io/badge/Kotlin Coroutines 1.8.1-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Coroutines"/>
  <img src="https://img.shields.io/badge/EncryptedSharedPreferences-3DDC84?style=flat-square&logo=android&logoColor=white" alt="EncryptedSharedPreferences"/>
</p>

### External SDK & Custom UI

<p>
  <img src="https://img.shields.io/badge/Kakao SDK 2.21.0-FFCD00?style=flat-square&logo=kakao&logoColor=black" alt="Kakao SDK"/>
  <img src="https://img.shields.io/badge/ZXing Android Embedded 4.3.0-000000?style=flat-square&logoColor=white" alt="ZXing"/>
  <img src="https://img.shields.io/badge/Custom Charts-FF8A00?style=flat-square&logoColor=white" alt="Custom Charts"/>
  <img src="https://img.shields.io/badge/Local Notifications-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Local Notifications"/>
</p>

## 구현 포인트

### 인증과 세션

- `SessionManager`가 회원 ID, 닉네임, Access Token, Refresh Token, Mock 세션 여부를 저장합니다.
- 가능한 경우 `EncryptedSharedPreferences`를 사용하고, 실패하면 일반 `SharedPreferences`로 fallback합니다.
- `RetrofitClient`는 모든 인증 요청에 `Authorization: Bearer {accessToken}` 헤더를 자동으로 추가합니다.
- 401 응답이 발생하면 OkHttp `Authenticator`가 Refresh Token으로 Access Token을 재발급하고 원 요청을 재시도합니다.
- Debug 빌드에서는 `POST /api/v1/auth/debug/login`과 로컬 Mock 세션으로 빠르게 시연할 수 있습니다.

### 판매 상품 등록

- `MenuRegistrationActivity`는 매장에서 반복적으로 사용하는 메뉴 마스터를 관리합니다.
- `ProductListingActivity`는 메뉴 선택, 가격/할인 설정, 수량/픽업 시간 설정을 단계별 화면으로 분리했습니다.
- 상품과 메뉴 등록 모두 이미지 multipart 업로드를 지원합니다.
- 할인율, 판매 수량, 픽업 가능 시간을 입력 즉시 미리보기로 확인할 수 있습니다.

### 주문과 픽업

- `OrderFragment`는 주문을 신규, 준비 중, 픽업 대기, 완료 탭으로 나눠 작업 우선순위를 드러냅니다.
- `OrderDetailBottomSheet`에서 주문 상세 확인과 상태 변경을 처리합니다.
- `PickupVerifyActivity`는 카메라 QR 스캔과 코드 직접 입력을 모두 지원합니다.
- 백엔드 검증 후 `confirmPickup` API로 주문을 픽업 완료 상태로 전환합니다.

### 매출과 알림

- `SalesFragment`는 `BarChartView`, `PieChartView`를 직접 구현해 기간별 매출과 결제 비중을 보여줍니다.
- 인기 메뉴 순위로 어떤 상품이 잘 팔리는지 확인할 수 있습니다.
- `NotificationFragment`는 반경 기반 고객 알림 발송과 발송 이력을 제공합니다.
- `RadiusMapView`는 알림 도달 반경을 판매자가 직관적으로 이해할 수 있게 시각화합니다.

## 프로젝트 구조

```text
app/src/main/java/com/example/deuktemsiru_seller/
├── DeuktemsiruSellerApp.kt       # Application, Kakao SDK 초기화
├── MainActivity.kt               # BottomNavigation 기반 메인 호스트
├── data/
│   └── SessionManager.kt         # 로그인 세션과 토큰 저장
├── network/
│   ├── ApiModels.kt              # 백엔드 요청 / 응답 DTO
│   ├── ApiService.kt             # Retrofit API 인터페이스
│   ├── AppEnums.kt               # 주문 / 상품 상태 enum
│   ├── MockApiService.kt         # 로컬 시연용 Mock API
│   └── RetrofitClient.kt         # Retrofit, OkHttp, 토큰 재발급
├── ui/
│   ├── auth/                     # 로그인
│   ├── home/                     # 홈 대시보드
│   ├── product/                  # 판매 상품 목록, 상품 등록, 상세
│   ├── registration/             # 메뉴 등록
│   ├── order/                    # 주문 목록, 주문 상세, 픽업 검증
│   ├── sales/                    # 매출 분석, 차트 View
│   ├── store/                    # 가게 정보, 메뉴 관리
│   ├── notification/             # 알림 발송, 발송 내역, 반경 View
│   ├── settings/                 # 알림 설정
│   └── mypage/                   # 마이페이지, 계정 정보, 정산
└── util/
    ├── DpExtension.kt
    ├── FormatExtensions.kt       # 가격 / 시간 포맷
    ├── LocalNotificationHelper.kt
    ├── PickupTimeState.kt
    └── TextWatchers.kt
```

## 실행 방법

### 1. 사전 준비

- Android Studio 최신 안정 버전
- JDK 17 이상
- Android SDK 36
- Kakao Developers 네이티브 앱 키
- 로컬 또는 원격 `deuktemsiru_backend`

### 2. 백엔드 실행

에뮬레이터에서 로컬 PC의 Spring Boot 서버에 접근할 때는 `10.0.2.2`를 사용합니다.

```bash
cd ../deuktemsiru_backend
./gradlew bootRun
```

### 3. API 키 설정

프로젝트 루트에 `local.properties`를 만들거나 기존 파일에 아래 값을 추가합니다.

```properties
KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key
```

실기기나 원격 서버를 사용할 경우 백엔드 주소도 함께 설정합니다.

```properties
BACKEND_BASE_URL=http://your-backend-host:8080/
```

`local.properties`는 Git에 커밋하지 않습니다.

### 4. 앱 빌드

```bash
./gradlew assembleDebug
```

Android Studio에서는 `app` 실행 구성을 선택한 뒤 에뮬레이터 또는 실기기에서 실행합니다.

### 5. 로그인 시연

Debug 빌드에서는 로그인 화면의 디버그 입력창을 사용할 수 있습니다.

| 입력값 | 동작 |
| --- | --- |
| `mock` | 백엔드 없이 앱 내부 Mock 데이터로 로그인 |
| `bakery`, `oido`, `오이도` | `bakery@test.com` 판매자 계정으로 Debug 로그인 |
| `cafe`, `baegot`, `배곧` | `cafe@siheung.test` 판매자 계정으로 Debug 로그인 |
| `bunsik`, `정왕` | `bunsik@siheung.test` 판매자 계정으로 Debug 로그인 |
| `dosirak`, `은행` | `dosirak@siheung.test` 판매자 계정으로 Debug 로그인 |
| `mart`, `목감` | `mart@siheung.test` 판매자 계정으로 Debug 로그인 |
| 이메일 직접 입력 | 입력한 이메일로 Debug 로그인 API 호출 |

### 6. 릴리스 빌드

릴리스 빌드에서 `10.0.2.2` 기본 주소를 사용하면 앱이 실행되지 않도록 방어 로직이 들어 있습니다. 릴리스 테스트 전 `BACKEND_BASE_URL`을 반드시 설정합니다.

서명 빌드가 필요하면 프로젝트 루트에 `release.keystore`를 두고 `local.properties`에 아래 값을 추가합니다.

```properties
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

## 주요 API

### 인증 / 회원

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/kakao/login` | 카카오 로그인 / 판매자 자동 회원가입 |
| `POST` | `/api/v1/auth/debug/login` | 개발용 Debug 로그인 |
| `POST` | `/api/v1/auth/refresh` | Access Token 갱신 |
| `POST` | `/api/v1/auth/logout` | 로그아웃 |
| `GET` | `/api/v1/members/me` | 내 회원 정보 조회 |

### 메뉴 / 판매 상품

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/menu-items` | 메뉴 목록 조회 |
| `POST` | `/api/v1/sellers/menu-items` | 메뉴 등록 |
| `PATCH` | `/api/v1/sellers/menu-items/{menuItemId}` | 메뉴 수정 |
| `DELETE` | `/api/v1/sellers/menu-items/{menuItemId}` | 메뉴 삭제 |
| `GET` | `/api/v1/sellers/products` | 판매 상품 목록 조회 |
| `POST` | `/api/v1/sellers/products` | 판매 상품 등록 |
| `PATCH` | `/api/v1/sellers/products/{productId}` | 판매 상품 수정 |
| `PATCH` | `/api/v1/sellers/products/{productId}/status` | 판매 상품 상태 변경 |
| `DELETE` | `/api/v1/sellers/products/{productId}` | 판매 상품 삭제 |

### 주문 / 픽업

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/sellers/orders` | 주문 목록 조회 |
| `PATCH` | `/api/v1/sellers/orders/{orderId}/status` | 주문 상태 변경 |
| `GET` | `/api/v1/sellers/pickup/verify?code={code}` | 픽업 코드 검증 |
| `PATCH` | `/api/v1/sellers/orders/{orderId}/confirm` | 픽업 완료 확정 |

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

| 권한 | 용도 |
| --- | --- |
| `INTERNET` | 백엔드 API 통신 |
| `CAMERA` | 픽업 QR 코드 스캔 |
| `POST_NOTIFICATIONS` | 주문 접수 로컬 알림 |

## 트러블슈팅

| 증상 | 확인할 내용 |
| --- | --- |
| 서버 연결 실패 | 에뮬레이터는 `http://10.0.2.2:8080/`, 실기기는 PC의 같은 네트워크 IP를 `BACKEND_BASE_URL`로 설정 |
| 카카오 로그인 실패 | `local.properties`의 `KAKAO_NATIVE_APP_KEY`와 Kakao Developers의 Android 플랫폼 패키지명 확인 |
| 릴리스 실행 중 URL 오류 | 릴리스 빌드는 `BACKEND_BASE_URL` 설정 필수 |
| 디버그 로그인이 실패 | 백엔드가 실행 중인지, 시드 데이터에 판매자 계정이 있는지 확인 |
| QR 스캔 화면이 열리지 않음 | 카메라 권한 허용 여부와 기기 카메라 사용 가능 여부 확인 |
| 푸시 알림이 보이지 않음 | Android 13 이상에서 알림 권한 허용 여부 확인 |

## 협업 규칙

- 민감한 값은 `local.properties`에만 저장하고 Git에 커밋하지 않습니다.
- API 경로가 변경되면 `ApiService.kt`, `ApiModels.kt`, README의 API 표를 함께 갱신합니다.
- UI 문자열, 화면 흐름, 상태 enum이 바뀌면 관련 XML, Fragment / Activity, Mock 데이터도 같이 확인합니다.
- 시연 전에는 `mock` 로그인과 실제 백엔드 Debug 로그인을 모두 확인합니다.
