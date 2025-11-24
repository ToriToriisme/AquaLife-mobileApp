# AquaLife - Fish Marketplace App

Ứng dụng điện thoại cho phép xem thông tin, mua bán các loại cá từ cá biển tới cá cảnh.

## 🏗️ Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture with:

- **Jetpack Compose** for UI
- **Room Database** for local data persistence (Offline-First)
- **Firebase** for authentication and real-time synchronization
- **Retrofit** for REST API calls
- **Hilt** for Dependency Injection
- **Coroutines & Flow** for asynchronous operations
- **DataStore** for preferences storage

## 📱 Features

### ✅ Implemented
- **Authentication**: Firebase Auth with email verification
- **Product Catalog**: 40-50 fish types per category (Cá biển, Cá sông, Cá nước lợ, Cá cảnh)
- **Shopping Cart**: Persistent cart with Room database
- **Favorites**: Save favorite products
- **Real-time Sync**: Price updates sync from admin dashboard in < 1 second
- **Offline Support**: App works offline, shows cached data
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
git clone <repository-url>
cd AquaLife
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

### Room Database Tables
- **fish_table**: Product catalog (177+ fish types)
- **cart_table**: Shopping cart items
- **user_table**: User profiles
- **order_table**: Order history
- **favorite_table**: Favorite products
- **notification_table**: App notifications

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
