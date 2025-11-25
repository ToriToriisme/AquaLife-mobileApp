# 📚 Nội Dung Tổng Hợp - Ứng Dụng AquaLife

## 🎯 Tổng Quan Ứng Dụng

AquaLife là ứng dụng thương mại điện tử chuyên về cá cảnh và hải sản, được xây dựng bằng **Android Jetpack Compose** với kiến trúc **MVVM** (Model-View-ViewModel). Ứng dụng hỗ trợ đầy đủ chức năng mua sắm, quản lý giỏ hàng, thanh toán và quản lý tài khoản người dùng.

---

## 🛠️ Kiến Thức Kỹ Thuật Đã Sử Dụng

### 1. **Kiến Trúc & Pattern**

#### MVVM (Model-View-ViewModel)
- **Model**: Entity classes (Room Database), DTOs, Repository
- **View**: Jetpack Compose UI Components
- **ViewModel**: Quản lý state và business logic, sử dụng StateFlow/LiveData
- **Lợi ích**: Tách biệt logic khỏi UI, dễ test, maintainable

#### Dependency Injection (Hilt)
- Sử dụng **Hilt** để quản lý dependencies
- Modules: `DatabaseModule`, `NetworkModule`, `RepositoryModule`, `FirebaseModule`
- `@HiltAndroidApp`, `@AndroidEntryPoint`, `@Inject`, `@Provides`
- **Lợi ích**: Giảm coupling, dễ test, quản lý lifecycle dependencies

#### Repository Pattern
- Trung gian giữa Data Source (Room, Firebase) và ViewModel
- `FishRepository`, `PaymentRepository`
- **Lợi ích**: Single source of truth, dễ thay đổi data source

### 2. **Jetpack Compose**

#### UI Components
- **Material Design 3**: `MaterialTheme`, `Surface`, `Card`, `Button`, `TextField`
- **Layout**: `Column`, `Row`, `Box`, `LazyColumn`, `LazyRow`, `LazyVerticalGrid`
- **Navigation**: `NavController`, `NavHost`, `composable()`
- **State Management**: `remember`, `mutableStateOf`, `collectAsState`, `LaunchedEffect`

#### State Hoisting
- State được nâng lên component cha
- Unidirectional data flow: UI → ViewModel → Repository → Data
- **Lợi ích**: Dễ debug, predictable state

#### Recomposition
- Compose tự động recompose khi state thay đổi
- Sử dụng `remember` để cache expensive calculations
- **Lợi ích**: Performance tốt, code đơn giản

### 3. **Room Database (Local Storage)**

#### Entity Classes
- `@Entity` với `@PrimaryKey`
- Tables: `fish_table`, `cart_table`, `user_table`, `order_table`, `favorite_table`, `notification_table`
- **Lợi ích**: Type-safe, compile-time checking

#### DAO (Data Access Object)
- `@Dao` interface với `@Query`, `@Insert`, `@Update`, `@Delete`
- Return `Flow<List<T>>` cho reactive updates
- **Lợi ích**: Tự động cập nhật UI khi data thay đổi

#### Database Migration
- Version management: `@Database(version = 6)`
- Migration strategies: destructive migration hoặc custom migration
- **Lợi ích**: Cập nhật schema mà không mất data

### 4. **Firebase Integration**

#### Firebase Authentication
- Email/Password authentication
- Google Sign-In, Facebook Login
- OTP verification
- Session management với DataStore
- **Lợi ích**: Secure, scalable authentication

#### Cloud Firestore
- Real-time database synchronization
- Collection: `products` (fish data)
- Offline persistence
- **Lợi ích**: Real-time updates, offline support

#### Firebase Analytics & Messaging
- User analytics tracking
- Push notifications (FCM)
- **Lợi ích**: Insights, user engagement

### 5. **Coroutines & Flow**

#### Coroutines
- `suspend` functions cho async operations
- `CoroutineScope`, `viewModelScope`, `Dispatchers.IO/Main`
- `launch`, `async`, `await`
- **Lợi ích**: Non-blocking, readable async code

#### Flow
- Cold streams cho reactive data
- `StateFlow`, `SharedFlow`
- Operators: `map`, `filter`, `collect`, `stateIn`
- **Lợi ích**: Reactive programming, automatic UI updates

### 6. **Networking**

#### Retrofit
- REST API client
- `@GET`, `@POST` annotations
- Gson converter
- **Lợi ích**: Type-safe API calls

#### OkHttp
- HTTP client với logging interceptor
- Request/Response interceptors
- **Lợi ích**: Debugging, request modification

### 7. **Image Loading**

#### Coil
- Async image loading library
- `AsyncImage` composable
- Placeholder, error handling
- **Lợi ích**: Efficient caching, memory management

### 8. **Permissions**

#### Runtime Permissions
- `Accompanist Permissions` library
- Camera, Storage, Microphone permissions
- Version-specific handling (Android 13+ vs older)
- **Lợi ích**: User privacy, security

### 9. **Data Persistence**

#### DataStore
- `PreferencesDataStore` cho key-value storage
- Session management, theme preferences, search history
- **Lợi ích**: Type-safe, async, replacement for SharedPreferences

### 10. **Format Utilities**

#### FormatUtils (`FormatUtils.kt`)
**Chức năng**: Format và parse giá tiền theo định dạng Việt Nam

**Các function:**
1. **`formatCurrency(amount: Int)`**: Chuyển số tiền thành chuỗi định dạng VN
   - Ví dụ: `100000` → `"100.000 đ"`
   - Sử dụng `NumberFormat` với Locale Việt Nam
   - Tự động thêm dấu chấm phân cách hàng nghìn

2. **`formatCurrency(amount: Double)`**: Overload cho Double
   - Chuyển Double thành Int rồi format

3. **`parseCurrency(priceStr: String)`**: Chuyển chuỗi giá về số
   - Ví dụ: `"100.000 đ"` → `100000`
   - Loại bỏ dấu chấm, dấu phẩy, và " đ"
   - Trả về `Int` hoặc `0` nếu không parse được

**Nơi sử dụng:**
- Hiển thị giá sản phẩm trong `FishDetailScreen`, `CartScreen`
- Hiển thị tổng tiền trong `PaymentScreen`
- Format giá khi convert từ `FishEntity` sang `FishProduct`

**Lợi ích**: 
- Định dạng tiền tệ nhất quán trong toàn bộ app
- Dễ đọc với người dùng Việt Nam
- Dễ maintain và thay đổi format sau này

### 11. **Payment Integration**

#### MoMo Payment
- QR code generation
- Deep linking (`momo://`)
- Payment link handling
- **Lợi ích**: Native payment experience

---

## 📱 Giới Thiệu Các Screen

### 1. **WelcomeScreen** (`WelcomeScreen.kt`)
**Chức năng**: Màn hình chào mừng khi mở ứng dụng
- Hiển thị logo và thông tin ứng dụng
- Nút "Đăng nhập" và "Đăng ký"
- Navigation đến Login/Register screens
- **Khi nào dùng**: Lần đầu mở app hoặc khi chưa đăng nhập

### 2. **LoginScreen** (`AuthScreens.kt`)
**Chức năng**: Đăng nhập vào tài khoản
- Form đăng nhập với email và password
- Validation input
- Firebase Authentication
- Quên mật khẩu
- Đăng nhập bằng Google/Facebook
- **Khi nào dùng**: Người dùng đã có tài khoản

### 3. **RegisterScreen** (`AuthScreens.kt`)
**Chức năng**: Đăng ký tài khoản mới
- Form đăng ký với email, password, confirm password
- Validation đầy đủ
- Tạo tài khoản Firebase
- Email verification
- **Khi nào dùng**: Người dùng mới chưa có tài khoản

### 4. **OTPScreen** (`AuthScreens.kt`)
**Chức năng**: Xác thực OTP (One-Time Password)
- Nhập mã OTP 6 số
- Verify OTP với Firebase
- Resend OTP
- **Khi nào dùng**: Sau khi đăng ký hoặc đăng nhập cần xác thực

### 5. **FacebookLoginScreen** (`AuthScreens.kt`)
**Chức năng**: Đăng nhập bằng Facebook
- Facebook SDK integration
- OAuth flow
- **Khi nào dùng**: Người dùng chọn đăng nhập bằng Facebook

### 6. **GoogleLoginScreen** (`AuthScreens.kt`)
**Chức năng**: Đăng nhập bằng Google
- Google Sign-In SDK
- OAuth flow
- **Khi nào dùng**: Người dùng chọn đăng nhập bằng Google

### 7. **MainScreen** (`MainScreen.kt`)
**Chức năng**: Màn hình chính với Bottom Navigation
- **Home Tab**: Hiển thị danh sách cá nổi bật, categories
- **Search Tab**: Tìm kiếm cá theo tên, category, giá
- **Cart Tab**: Giỏ hàng, quản lý sản phẩm đã chọn
- **Profile Tab**: Thông tin người dùng, cài đặt
- Bottom Navigation Bar để chuyển đổi giữa các tab
- **Khi nào dùng**: Sau khi đăng nhập thành công

### 8. **HomeScreen** (trong `MainScreen.kt`)
**Chức năng**: Trang chủ hiển thị sản phẩm
- Banner/carousel
- Categories: Cá biển, Cá sông, Cá nước lợ, Cá cảnh
- Danh sách cá nổi bật (best seller, discount)
- Quick filters
- **Khi nào dùng**: Tab đầu tiên trong MainScreen

### 9. **FishListScreen** (`MainScreen.kt`)
**Chức năng**: Danh sách cá theo category
- Hiển thị tất cả cá hoặc lọc theo category
- Grid layout với images
- Click vào cá để xem chi tiết
- **Khi nào dùng**: Khi chọn category từ HomeScreen

### 10. **FishDetailScreen** (`MainScreen.kt`)
**Chức năng**: Chi tiết sản phẩm cá
- Hiển thị hình ảnh, tên, giá, mô tả
- Thông tin: habitat, max weight, diet
- Nút "Thêm vào giỏ hàng"
- Nút "Yêu thích" (favorite)
- **Khi nào dùng**: Khi click vào một con cá từ danh sách

### 11. **SearchScreen** (`MainScreen.kt`)
**Chức năng**: Tìm kiếm sản phẩm
- Search bar với Vietnamese text input
- Real-time search results
- Filters: category, price range, rating, discount
- Sort options: name, price, rating
- **Khi nào dùng**: Tab Search trong MainScreen

### 12. **CartScreen** (`MainScreen.kt`)
**Chức năng**: Giỏ hàng
- Danh sách sản phẩm đã thêm vào giỏ
- Tăng/giảm số lượng
- Xóa sản phẩm
- Tính tổng tiền
- Nút "Thanh toán"
- **Khi nào dùng**: Tab Cart trong MainScreen

### 13. **FavoritesScreen** (`MainScreen.kt`)
**Chức năng**: Danh sách sản phẩm yêu thích
- Hiển thị tất cả cá đã được đánh dấu yêu thích
- Chip filters theo category
- Click để xem chi tiết
- Bỏ yêu thích
- **Khi nào dùng**: Khi click "Yêu thích" từ navigation hoặc profile

### 14. **ProfileScreen** (`MainScreen.kt`)
**Chức năng**: Hồ sơ người dùng
- Avatar, tên, bio
- Số lượng posts, followers, following
- Edit profile
- Dark/Light mode toggle
- Đăng xuất
- **Khi nào dùng**: Tab Profile trong MainScreen

### 15. **PostDetailScreen** (`MainScreen.kt`)
**Chức năng**: Chi tiết bài viết (social feature)
- Hiển thị ảnh, caption
- Like, comment, share
- Danh sách comments
- **Khi nào dùng**: Khi click vào một post từ ProfileScreen

### 16. **PaymentScreen** (`PaymentScreen.kt`)
**Chức năng**: Thanh toán đơn hàng
- Form nhập thông tin: tên, số điện thoại, địa chỉ
- Chọn phương thức thanh toán: MoMo, Bank transfer
- Hiển thị QR code thanh toán
- Copy payment link
- Xác nhận đã chuyển khoản
- Tạo notification sau khi thanh toán thành công
- **Khi nào dùng**: Khi click "Thanh toán" từ CartScreen

### 17. **NotificationsScreen** (`NotificationsScreen.kt`)
**Chức năng**: Thông báo trong ứng dụng
- Danh sách notifications: ORDER, PROMOTION, SYSTEM
- Hiển thị hình ảnh nếu có (ví dụ: hình cá sau khi đặt hàng)
- Đánh dấu đã đọc
- Xóa notification
- **Khi nào dùng**: Khi click vào icon notification

---

## 🔄 Luồng Dữ Liệu (Data Flow)

### 1. **Offline-First Architecture**
```
UI (Compose) 
  ↓ collectAsState()
ViewModel (StateFlow)
  ↓ getData()
Repository
  ↓
Room Database (Local) ← Sync → Firebase Firestore (Remote)
```

### 2. **User Authentication Flow**
```
WelcomeScreen → LoginScreen → Firebase Auth → HomeScreen
                ↓
            RegisterScreen → OTP Verification → HomeScreen
```

### 3. **Shopping Flow**
```
HomeScreen → FishDetailScreen → Add to Cart → CartScreen → PaymentScreen → Order Confirmation
```

### 4. **Search Flow**
```
SearchScreen → Query → Repository → Filter → Display Results → FishDetailScreen
```

---

## 📊 Database Schema

### Room Database Tables

1. **fish_table**: 80 loại cá với thông tin đầy đủ
2. **cart_table**: Giỏ hàng của người dùng
3. **user_table**: Thông tin người dùng
4. **order_table**: Lịch sử đơn hàng
5. **favorite_table**: Sản phẩm yêu thích
6. **notification_table**: Thông báo trong app

---

## 🎨 UI/UX Features

- **Material Design 3**: Modern, beautiful UI
- **Dark/Light Mode**: Theme switching với persistence
- **Skeleton Loading**: Loading animations
- **Smooth Animations**: Transitions, state changes
- **Responsive Layout**: Adapts to different screen sizes
- **Vietnamese Language**: Full Vietnamese support

---

## 🔐 Security & Permissions

- **Firebase Authentication**: Secure user authentication
- **Runtime Permissions**: Camera, Storage, Microphone
- **Data Encryption**: Room database encryption (optional)
- **Secure Payment**: MoMo payment integration

---

## 📦 Build Configuration

- **minSdk**: 24 (Android 7.0+)
- **targetSdk**: 34
- **compileSdk**: 36
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.1
- **ABI Filters**: armeabi-v7a, arm64-v8a, x86, x86_64

---

## 🚀 Performance Optimizations

- **Lazy Loading**: LazyColumn, LazyRow cho lists
- **Image Caching**: Coil library
- **Database Indexing**: Room database indexes
- **Coroutine Scopes**: Proper scope management
- **State Management**: Efficient recomposition

---

## 🛠️ Utility Classes

### FormatUtils.kt
**Mục đích**: Format và parse giá tiền theo chuẩn Việt Nam

**Các function chính:**
- `formatCurrency(Int)`: Format số thành chuỗi "100.000 đ"
- `formatCurrency(Double)`: Format số thực thành chuỗi
- `parseCurrency(String)`: Parse chuỗi giá về số nguyên

**Ví dụ sử dụng:**
```kotlin
// Format giá để hiển thị
val price = 150000
val formatted = FormatUtils.formatCurrency(price) // "150.000 đ"

// Parse giá từ chuỗi
val priceStr = "150.000 đ"
val amount = FormatUtils.parseCurrency(priceStr) // 150000
```

**Nơi sử dụng trong app:**
- `FishDetailScreen`: Hiển thị giá cá
- `CartScreen`: Hiển thị giá từng item và tổng tiền
- `PaymentScreen`: Hiển thị tổng tiền đơn hàng
- `MainScreen`: Format giá khi convert Entity sang Product

---

## 📝 Kết Luận

Ứng dụng AquaLife sử dụng các công nghệ hiện đại của Android để tạo ra một ứng dụng thương mại điện tử hoàn chỉnh với:
- ✅ Kiến trúc MVVM rõ ràng
- ✅ Offline-first với sync real-time
- ✅ UI/UX đẹp với Material Design 3
- ✅ Đầy đủ chức năng: Authentication, Shopping, Payment, Notifications
- ✅ Performance tốt với optimizations
- ✅ Code maintainable và scalable

