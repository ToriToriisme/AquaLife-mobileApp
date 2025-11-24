# AquaLife App - Pages & Structure Analysis

## 📱 Total Pages/Screens: **13 Screens**

### **1 Activity:** `MainActivity` (Single Activity Architecture)
All screens are implemented as Jetpack Compose composables within one Activity.

---

## 🔐 Authentication Flow (6 Screens)

### 1. **WelcomeScreen** (`"welcome"`)
- **Sub-screens:**
  - `StartScreen` - Logo splash screen with "Bắt đầu" button
  - `OnboardingContent` - Auto-scrolling onboarding slides (3 slides)
- **Features:**
  - Logo display
  - Auto-scrolling banner (3 seconds per slide)
  - Navigation to Login/Register
- **Navigation:** → Login or Register

### 2. **LoginScreen** (`"login"`)
- **Features:**
  - Email/Phone + Password input
  - Google login button
  - Facebook login button
  - Link to Register screen
- **Validation:** Checks against UserManager database
- **Navigation:** → Home (on success) or Register

### 3. **RegisterScreen** (`"register"`)
- **Features:**
  - Email/Phone input
  - Password + Confirm Password
  - Email/Phone format validation
  - OTP verification flow
- **Navigation:** → OTP Screen

### 4. **OTPScreen** (`"otp"`)
- **Features:**
  - 4-digit OTP input
  - Default OTP: "0000"
  - OTP verification
- **Navigation:** → Login (on success)

### 5. **FacebookLoginScreen** (`"facebook_login"`)
- **Features:**
  - Simulated Facebook login
  - Email/Phone + Password validation
- **Navigation:** → Home (on success)

### 6. **GoogleLoginScreen** (`"google_login"`)
- **Features:**
  - Account selection (3 pre-defined accounts)
  - Simulated Google login
- **Navigation:** → Home (on selection)

---

## 🏠 Main App Flow (7 Screens)

### 7. **MainScreen** (`"home"`) - Container Screen
- **Bottom Navigation:** 5 tabs
  - Home (index 0)
  - Khám phá / Search (index 1)
  - Giỏ hàng / Cart (index 2)
  - Thông báo / Notifications (index 3) - **Placeholder**
  - Tôi / Profile (index 4)

### 8. **HomeScreenContent** (Tab 0)
- **Features:**
  - Search bar
  - Quick menu tabs (Yêu Thích, Lịch sử, Theo dõi, Orders)
  - Auto-scrolling category banners (4 categories)
  - Category icons (Cá biển, Cá sông, Cá nước lợ, Cá cảnh)
  - Auto-scrolling fish recommendations (LazyRow)
- **Navigation:** → Fish List, Fish Detail, Favorites, Cart

### 9. **SearchScreen** (`"Khám phá"`) (Tab 1)
- **Features:**
  - Search input field
  - Real-time search filtering
  - Search results display
  - Empty state when no search query
- **Navigation:** → Fish Detail

### 10. **CartScreen** (`"Giỏ hàng"`) (Tab 2)
- **Features:**
  - Cart items list
  - Quantity adjustment (+/-)
  - Remove item functionality
  - Total price calculation
  - Checkout button (UI only)
  - Empty cart state
- **Badge:** Shows item count on bottom nav icon

### 11. **ProfileScreen** (`"Tôi"`) (Tab 4)
- **Features:**
  - Profile header (avatar, stats: Posts, Followers, Following)
  - Profile name and bio
  - Edit profile dialog
  - Account menu (Add account, Logout)
  - Settings menu (Privacy toggle, Dark mode, Logout)
  - Posts grid (3 columns)
  - Create post functionality
  - Empty state for no posts
- **Navigation:** → Post Detail, Login (on logout)

### 12. **FishListScreen** (`"fish_list/{category}"`)
- **Features:**
  - Category filtering (Cá biển, Cá sông, Cá nước lợ, Cá cảnh, or "all")
  - Filtered fish list display
  - Empty state for no results
- **Navigation:** → Fish Detail

### 13. **FishDetailScreen** (`"fish_detail/{fishId}"`)
- **Features:**
  - Large fish image
  - Fish name, price, category
  - Details: Habitat, Max weight, Diet
  - Favorite toggle
  - Add to cart button
- **Navigation:** Back to previous screen

### 14. **FavoritesScreen** (`"favorites"`)
- **Features:**
  - List of favorited fish
  - Empty state message
- **Navigation:** → Fish Detail

### 15. **PostDetailScreen** (`"post_detail/{postId}"`)
- **Features:**
  - Post image display
  - Like functionality
  - Comment section
  - Add comment input
  - Delete post option
  - User profile header
- **Navigation:** Back to Profile

---

## 📊 Screen Summary

| Category | Screen Count | Screens |
|----------|-------------|---------|
| **Authentication** | 6 | Welcome, Login, Register, OTP, Facebook Login, Google Login |
| **Main App** | 7 | Main (Container), Home, Search, Cart, Profile, Fish List, Fish Detail, Favorites, Post Detail |
| **Placeholder** | 1 | Notifications (Tab 3) |
| **Total** | **13** | Unique screens |

---

## 🗂️ Data Models

### **FishProduct**
- id, name, price, priceInt, imageUrl
- category, habitat, maxWeight, diet

### **BannerItem**
- title, imageUrl, categoryKey

### **UserPost**
- id, imageUrl, isLiked, comments

### **Comment**
- userName, content

### **CartItem**
- fish, quantity

---

## 🔄 Navigation Flow

```
Welcome → Login/Register
  ↓
Login → Home
Register → OTP → Login → Home
Facebook/Google Login → Home

Home (MainScreen)
  ├─ Tab 0: HomeScreenContent
  │   ├─ → Fish List (by category)
  │   ├─ → Fish Detail
  │   └─ → Favorites
  ├─ Tab 1: SearchScreen
  │   └─ → Fish Detail
  ├─ Tab 2: CartScreen
  ├─ Tab 3: Notifications (Placeholder)
  └─ Tab 4: ProfileScreen
      ├─ → Post Detail
      └─ → Login (on logout)
```

---

## 📦 Global State Management

- `largeFishList` - All fish products (30 items)
- `favoriteFishIds` - List of favorited fish IDs
- `globalMyPosts` - User's posts
- `globalProfileName` - Current user's name
- `globalProfileBio` - Current user's bio
- `globalCartItems` - Shopping cart items

**Note:** Currently using in-memory state (not persisted)

