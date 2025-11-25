# AquaLife - Fish Marketplace App

Ứng dụng điện thoại cho phép xem thông tin, mua bán các loại cá từ cá biển tới cá cảnh.

## 🏗️ Architecture

Ứng dụng sử dụng kiến trúc **MVVM (Model-View-ViewModel)** với các thành phần:

- **Jetpack Compose** cho giao diện người dùng
- **Room Database** cho lưu trữ dữ liệu cục bộ (Offline-First)
- **Firebase** cho xác thực và đồng bộ hóa thời gian thực
- **Retrofit** cho các lời gọi REST API
- **Hilt** cho Dependency Injection
- **Coroutines & Flow** cho các thao tác bất đồng bộ
- **DataStore** cho lưu trữ preferences

### 📐 Sơ đồ Kiến trúc Ứng dụng

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        🎨 PRESENTATION LAYER (UI)                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    MainActivity (Single Activity)                 │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                           │
│        ┌─────────────────────┼─────────────────────┐                   │
│        │                     │                     │                   │
│   ┌────▼────┐          ┌────▼────┐          ┌────▼────┐              │
│   │Welcome  │          │  Auth   │          │  Main   │              │
│   │ Screen  │          │ Screens │          │ Screen  │              │
│   └────┬────┘          └────┬────┘          └────┬────┘              │
│        │                     │                     │                   │
│        │                     │         ┌───────────┼───────────┐       │
│        │                     │         │           │           │       │
│        │                     │    ┌────▼───┐ ┌────▼───┐ ┌────▼───┐   │
│        │                     │    │  Home  │ │ Search │ │  Cart  │   │
│        │                     │    └────┬───┘ └────┬───┘ └────┬───┘   │
│        │                     │         │           │           │       │
│        │                     │    ┌────▼───┐ ┌────▼───┐ ┌────▼───┐   │
│        │                     │    │Profile │ │Favorite│ │Detail  │   │
│        │                     │    └────────┘ └────────┘ └────────┘   │
│        │                     │                                         │
│   ┌────▼────┐          ┌─────▼─────┐                                 │
│   │Payment │          │Notification│                                 │
│   │ Screen │          │   Screen   │                                 │
│   └────────┘          └────────────┘                                 │
│                                                                         │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
                                │ (StateFlow/Events)
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      🧠 VIEWMODEL LAYER                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │ HomeViewModel│  │AuthViewModel │  │CartViewModel │                  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                  │
│         │                 │                  │                          │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐                  │
│  │Favorite      │  │Payment       │  │Notification  │                  │
│  │ViewModel     │  │ViewModel     │  │ViewModel     │                  │
│  └──────────────┘  └──────────────┘  └──────────────┘                  │
│                                                                           │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
                                │ (Repository Pattern)
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      📦 REPOSITORY LAYER                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────┐      ┌──────────────────────┐              │
│  │   FishRepository      │      │  PaymentRepository   │              │
│  │  (Single Source of   │      │  (MoMo/VNPay API)    │              │
│  │   Truth - Offline-    │      │                      │              │
│  │   First Strategy)     │      │                      │              │
│  └───────────┬────────────┘      └──────────┬───────────┘              │
│              │                               │                          │
└──────────────┼───────────────────────────────┼──────────────────────────┘
               │                               │
               │                               │
    ┌──────────▼──────────┐       ┌───────────▼──────────┐
    │                     │       │                     │
┌───▼─────────────────────▼───┐ ┌─▼─────────────────────▼───┐
│    💾 DATA LAYER             │ │  ☁️ REMOTE SERVICES        │
├───────────────────────────────┤ ├───────────────────────────┤
│                               │ │                           │
│  ┌─────────────────────┐    │ │  ┌─────────────────────┐ │
│  │   Room Database      │    │ │  │ Firebase Firestore  │ │
│  │                      │    │ │  │ (Real-time Sync)     │ │
│  │ • FishEntity (80)    │◄───┼─┼──┤                     │ │
│  │ • CartEntity         │    │ │  └─────────────────────┘ │
│  │ • FavoriteEntity     │    │ │                           │
│  │ • UserEntity         │    │ │  ┌─────────────────────┐ │
│  │ • OrderEntity        │    │ │  │  Firebase Auth      │ │
│  │ • NotificationEntity │    │ │  │  (Authentication)    │ │
│  └─────────────────────┘    │ │  └─────────────────────┘ │
│                               │ │                           │
│  ┌─────────────────────┐    │ │  ┌─────────────────────┐ │
│  │  DataStore           │    │ │  │  MoMo Payment API    │ │
│  │  (Preferences)      │    │ │  │  (QR Code Generation)│ │
│  │                      │    │ │  └─────────────────────┘ │
│  │ • Theme              │    │ │                           │
│  │ • Search History     │    │ │                           │
│  │ • Session            │    │ │                           │
│  └─────────────────────┘    │ │                           │
│                               │ │                           │
└───────────────────────────────┘ └───────────────────────────┘
```

### 📊 Luồng Dữ liệu (Data Flow)

```
┌──────────┐
│  👤 User │
└────┬─────┘
     │ 1. Tương tác (Click, Search, Add to Cart...)
     ▼
┌─────────────────────────────────────────────────────────────┐
│  🎨 UI SCREEN (Jetpack Compose)                             │
│  • HomeScreenContent                                        │
│  • SearchScreen                                             │
│  • CartScreen                                               │
└────┬────────────────────────────────────────────────────────┘
     │ 2. Gọi hàm ViewModel
     ▼
┌─────────────────────────────────────────────────────────────┐
│  🧠 VIEWMODEL                                               │
│  • Quản lý StateFlow                                        │
│  • Xử lý logic nghiệp vụ                                    │
│  • Gọi Repository để lấy dữ liệu                            │
└────┬────────────────────────────────────────────────────────┘
     │ 3. Yêu cầu dữ liệu
     ▼
┌─────────────────────────────────────────────────────────────┐
│  📦 REPOSITORY                                              │
│  • Offline-First: Ưu tiên đọc từ Room DB                   │
│  • Đồng bộ nền với Firebase                                 │
└────┬────────────────────────────────────────────────────────┘
     │
     ├─────────────────┐                    ┌──────────────────┐
     │ 4a. Đọc từ DB   │                    │ 4b. Sync Firebase│
     ▼                 │                    ▼                  │
┌──────────────┐       │            ┌──────────────┐         │
│ 💾 Room DB   │       │            │ ☁️ Firebase  │         │
│              │       │            │  Firestore   │         │
│ • Fish (80)  │       │            │              │         │
│ • Cart       │       │            │ Real-time    │         │
│ • Favorites  │       │            │ Updates      │         │
└──────┬───────┘       │            └──────┬───────┘         │
       │               │                   │                 │
       │ 5. Trả dữ liệu │                   │ 6. Cập nhật DB  │
       └───────────────┼───────────────────┘                 │
                       │                                     │
                       │ 7. Flow tự động cập nhật UI         │
                       ▼                                     │
              ┌─────────────────┐                           │
              │ UI tự động refresh│                          │
              │ (Reactive Flow)  │                           │
              └─────────────────┘                           │
```

**Đặc điểm chính:**
- ✅ **Offline-First**: Luôn ưu tiên dữ liệu từ Room DB
- ✅ **Real-time Sync**: Firebase tự động cập nhật khi có thay đổi
- ✅ **Reactive UI**: UI tự động cập nhật khi dữ liệu thay đổi (Flow)
- ✅ **Single Source of Truth**: Room DB là nguồn dữ liệu chính

### 🗂️ Cấu trúc Thư mục

```
app/src/main/java/com/example/aqualife/
├── 🎨 ui/
│   ├── screen/          # Các màn hình chính
│   │   ├── MainScreen.kt          # Home, Search, Cart, Profile
│   │   ├── AuthScreens.kt          # Login, Register, OTP
│   │   ├── PaymentScreen.kt         # Thanh toán MoMo/Bank
│   │   ├── WelcomeScreen.kt        # Splash & Onboarding
│   │   └── NotificationsScreen.kt  # Thông báo
│   ├── viewmodel/      # Quản lý trạng thái UI
│   │   ├── HomeViewModel.kt
│   │   ├── AuthViewModel.kt
│   │   ├── CartViewModel.kt
│   │   ├── FavoriteViewModel.kt
│   │   ├── PaymentViewModel.kt
│   │   └── NotificationViewModel.kt
│   ├── components/     # Component tái sử dụng
│   │   ├── SkeletonLoader.kt
│   │   └── PermissionRationaleDialog.kt
│   └── theme/          # Material Design 3 Theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── 📦 data/
│   ├── local/          # Room Database
│   │   ├── AquaLifeDatabase.kt
│   │   ├── dao/        # Data Access Objects
│   │   ├── entity/     # Database Entities
│   │   └── FishSeedData.kt
│   ├── remote/         # API & Firebase
│   │   ├── AquaLifeApiService.kt
│   │   ├── FirebaseSyncService.kt
│   │   └── payment/    # MoMo Payment API
│   ├── repository/     # Repository Pattern
│   │   ├── FishRepository.kt
│   │   └── PaymentRepository.kt
│   └── preferences/    # DataStore
│       ├── ThemePreferences.kt
│       ├── SearchHistoryPreferences.kt
│       └── SessionPreferences.kt
│
├── 🔧 di/              # Dependency Injection (Hilt)
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── FirebaseModule.kt
│   ├── PreferencesModule.kt
│   └── RepositoryModule.kt
│
└── 🛠️ utils/           # Tiện ích
    ├── FormatUtils.kt
    ├── MomoQrGenerator.kt
    └── PaymentManager.kt
```

### 🔄 Mô tả Chi tiết các Layer

#### 1. **Presentation Layer (UI Layer)**
- **Chức năng**: Hiển thị giao diện người dùng và xử lý tương tác
- **Công nghệ**: Jetpack Compose, Material Design 3
- **Thành phần**:
  - `MainActivity`: Single Activity Architecture, quản lý Navigation
  - Các Screen: Home, Search, Cart, Profile, Payment, Auth, etc.
  - Components: SkeletonLoader, PermissionDialog, etc.

#### 2. **ViewModel Layer**
- **Chức năng**: Quản lý trạng thái UI, xử lý logic nghiệp vụ
- **Công nghệ**: Android ViewModel, StateFlow, Coroutines
- **Thành phần**:
  - `HomeViewModel`: Quản lý danh sách cá, tìm kiếm, lọc
  - `CartViewModel`: Quản lý giỏ hàng
  - `AuthViewModel`: Xác thực người dùng
  - `FavoriteViewModel`: Quản lý yêu thích
  - `PaymentViewModel`: Xử lý thanh toán

#### 3. **Repository Layer**
- **Chức năng**: Trung gian giữa ViewModel và Data Layer, quản lý nguồn dữ liệu
- **Công nghệ**: Repository Pattern, Flow
- **Thành phần**:
  - `FishRepository`: Quản lý dữ liệu cá (Room + Firebase)
  - `PaymentRepository`: Xử lý thanh toán MoMo/VNPay

#### 4. **Data Layer**
- **Local Storage (Room Database)**:
  - `FishEntity`: 80 loại cá với đầy đủ thông tin
  - `CartEntity`: Giỏ hàng của người dùng
  - `FavoriteEntity`: Sản phẩm yêu thích
  - `UserEntity`: Thông tin người dùng
  - `OrderEntity`: Lịch sử đơn hàng
  - `NotificationEntity`: Thông báo trong app
  
- **Remote Services**:
  - **Firebase Firestore**: Đồng bộ dữ liệu thời gian thực
  - **Firebase Auth**: Xác thực người dùng
  - **MoMo Payment API**: Thanh toán qua MoMo

#### 5. **Dependency Injection (Hilt)**
- **Chức năng**: Quản lý dependencies, giảm coupling
- **Modules**:
  - `DatabaseModule`: Cung cấp Room Database
  - `NetworkModule`: Cung cấp Retrofit, OkHttp
  - `FirebaseModule`: Cung cấp Firebase services
  - `PreferencesModule`: Cung cấp DataStore
  - `RepositoryModule`: Cung cấp Repositories

### 🎯 Nguyên tắc Thiết kế

1. **Offline-First**: Ưu tiên dữ liệu cục bộ, đồng bộ nền với Firebase
2. **Single Source of Truth**: Room Database là nguồn dữ liệu chính
3. **Reactive Programming**: Sử dụng Flow để tự động cập nhật UI
4. **Separation of Concerns**: Tách biệt rõ ràng giữa các layer
5. **Dependency Injection**: Sử dụng Hilt để quản lý dependencies
6. **Unidirectional Data Flow**: UI → ViewModel → Repository → Data

## 📱 Features

### ✅ Implemented
- **Authentication**: Firebase Auth with email verification + Admin bypass (admin123/admin123)
- **Product Catalog**: **80 real Vietnamese fish** with authentic names and prices
- **Smart Database**: Auto-initializes with real data, syncs with Firebase
- **Advanced Search**: Search Vietnamese fish names (e.g., "Cá Lóc", "Cá Rồng")
- **Category Filtering**: Filter by Cá biển, Cá sông, Cá nước lợ, Cá cảnh
- **Shopping Cart**: Persistent cart with Room database
- **Favorites**: Save favorite products with database persistence
- **Real-time Sync**: Price updates sync from admin dashboard in < 1 second
- **Offline Support**: App works offline, shows cached data (80 fish)
- **Rating System**: Star ratings (3.9-5.0) and best seller tracking
- **Discount System**: Sale prices and discount badges
- **Payment**: MoMo & Bank payment (Test mode)
- **Dark/Light Mode**: Theme toggle with persistence
- **Skeleton Loading**: Beautiful loading animations
- **Notifications**: In-app notification system

### 🚧 In Progress
- Enhanced Profile Page
- Admin Dashboard (Web)
- Camera/Image permissions
- Order History

## 🛠️ Tech Stack

### Core
- **Kotlin** 2.0.21
- **Android Gradle Plugin** 8.13.1
- **Jetpack Compose** BOM 2024.09.00
- **Material Design 3**

### Architecture Components
- **Hilt** 2.51.1 - Dependency Injection
- **Room** 2.6.1 - Local Database
- **ViewModel** - State management
- **LiveData/StateFlow** - Reactive data

### Networking
- **Retrofit** 2.9.0 - REST API
- **OkHttp** 4.12.0 - HTTP client
- **Firebase Firestore** - Real-time database
- **Firebase Auth** - Authentication

### UI/UX
- **Coil** 2.6.0 - Image loading
- **Navigation Compose** 2.7.7
- **Accompanist Permissions** - Runtime permissions

## 📦 Setup Instructions

### 1. Clone Repository
```bash
git clone https://github.com/ToriToriisme/AquaLife-mobileApp.git
cd AquaLife-mobileApp
```

### 2. Firebase Setup

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package name: `com.example.aqualife`
3. Download `google-services.json`
4. Place it in `app/` directory
5. Enable Authentication (Email/Password)
6. Enable Firestore Database
7. Create collection `products` in Firestore

### 3. Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### 4. APK Location
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 🔐 Permissions

The app requires:
- **INTERNET** - Network access
- **ACCESS_NETWORK_STATE** - Check connectivity
- **CAMERA** - Take photos for products
- **READ_EXTERNAL_STORAGE** - Access images
- **WRITE_EXTERNAL_STORAGE** - Save images (Android ≤ 12)
- **READ_MEDIA_IMAGES** - Access images (Android 13+)
- **RECORD_AUDIO** - Voice search (future feature)

## 📊 Database Structure

### Room Database Tables (Version 2)
- **fish_table**: Product catalog (**80 real Vietnamese fish** with ratings, discounts)
- **cart_table**: Shopping cart items
- **user_table**: User profiles (synced with Firebase Auth)
- **order_table**: Order history
- **favorite_table**: Favorite products (by String ID)
- **notification_table**: App notifications

### Fish Database Details
- **Total Fish**: 80 (20 per category)
- **Categories**: Cá biển, Cá sông, Cá nước lợ, Cá cảnh
- **Price Range**: 5,000đ - 25,000,000đ
- **Features**: Rating (4.5★ avg), Best seller tracking, Discount system
- **Auto-init**: Loads real data on first launch
- **Sync**: Firebase Firestore real-time updates

## 🔄 Real-time Synchronization

The app uses **Firebase Firestore** for real-time data sync:

1. Admin updates product price on Web Dashboard
2. Firebase sends change event to app
3. App updates local Room database
4. UI automatically updates via Flow
5. **Sync time: < 1 second**

## 💳 Payment Integration

### Test Mode
- **MoMo**: Simulated payment flow
- **Bank Transfer**: Simulated payment flow
- All payments show "PAYMENT TEST MODE" watermark
- Transaction codes generated: `TRX_[timestamp]`

### Production Setup (Future)
- Integrate real MoMo SDK
- Integrate bank payment gateway
- Remove test mode indicators

## 🌐 External APIs Used

### Firebase Services
- **Firebase Authentication**: User login/registration
- **Firebase Firestore**: Real-time database
- **Firebase Cloud Messaging**: Push notifications (future)
- **Terms**: https://firebase.google.com/terms

### Image Sources
- **Unsplash API**: Product images
  - Terms: https://unsplash.com/api-terms
  - Attribution: Photos from Unsplash contributors

### Weather API (Future)
- **OpenWeatherMap**: Weather-based fish recommendations
  - Terms: https://openweathermap.org/terms

## 📱 Screens

### Authentication Flow
1. Welcome Screen (Splash + Onboarding)
2. Login Screen
3. Register Screen
4. OTP Verification Screen
5. Social Login (Google/Facebook)

### Main App
1. Home Screen - Product catalog with banners
2. Search Screen - Product search
3. Cart Screen - Shopping cart
4. Profile Screen - User profile
5. Fish Detail Screen - Product details
6. Favorites Screen - Favorite products
7. Payment Screen - Checkout flow
8. Notifications Screen - App notifications

## 🎨 UI/UX Features

- **Material Design 3** components
- **Dark/Light Mode** toggle
- **Skeleton Loading** animations
- **Smooth transitions** and animations
- **Responsive layouts** for all screen sizes
- **Accessibility** support

## 🧪 Testing

### Manual Testing Checklist
- [ ] App installs and launches
- [ ] Registration with email verification
- [ ] Login/logout functionality
- [ ] Product browsing (offline & online)
- [ ] Add to cart and checkout
- [ ] Payment test mode
- [ ] Favorites functionality
- [ ] Dark/Light mode toggle
- [ ] Real-time price sync (requires Firebase setup)

## 📈 Admin Dashboard

Web dashboard for managing:
- Product inventory
- Price updates (syncs to app in real-time)
- Order management
- Profit analytics
- Sales charts

**Status**: In development (React/Next.js)

## 🐛 Known Issues

1. Firebase configuration required (`google-services.json`)
2. API base URL needs configuration
3. Default fish data uses placeholder names (should use real fish names)
4. Some screens still use global state (needs refactoring)

## 🔧 Configuration

### API Base URL
Update in `NetworkModule.kt`:
```kotlin
.baseUrl("https://api.aqualife.example.com/")
```

### Firebase Configuration
1. Add `google-services.json` to `app/` directory
2. Configure Firestore collections:
   - `products` - Product catalog
   - `orders` - Order data

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Note**: This is a showcase/demo app. Payment features are in test mode and do not process real transactions.
