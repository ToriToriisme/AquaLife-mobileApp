# 🚀 AquaLife - Quick Start Guide

## 1️⃣ Build & Run (5 minutes)

```bash
# Navigate to project
cd "C:\Users\Vy Hao\Desktop\Clone-Aqualife"

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug
```

**Or use Android Studio:**
1. Open project in Android Studio
2. Click **Build** → **Rebuild Project**
3. Click **Run** (green play button)

---

## 2️⃣ First Launch

### Expected Behavior
1. **Welcome Screen** appears
2. Click "Đăng nhập" or "Đăng ký"
3. **Database auto-initializes:**
   - Loads 80 Vietnamese fish
   - Saves to Room database
   - Pushes to Firebase

**Check Logcat:**
```
D/AquaLife: Database empty. Checking Firebase...
D/AquaLife: Loaded 80 fish to local database
D/AquaLife: Pushed 80 fish to Firebase successfully!
```

---

## 3️⃣ Login Options

### Admin Account (Instant)
- **Username:** `admin123`
- **Password:** `admin123`
- **Bypasses:** Firebase, email verification
- **Goes to:** Home screen immediately

### Normal User
1. Click "Đăng ký ngay"
2. Enter email, name, password
3. Check email for verification link
4. Click link in email
5. Return to app
6. Click "Tôi đã xác thực xong" in dialog
7. Login with email & password

---

## 4️⃣ Test Features

### Home Screen
- ✅ See 80 Vietnamese fish
- ✅ Auto-scrolling banner
- ✅ Category buttons (4 types)
- ✅ Tap fish to see details

### Search (Khám Phá)
- ✅ Type "Cá Lóc" → See river fish
- ✅ Type "Cá Rồng" → See Arowana (25M đ)
- ✅ Type "Nemo" → See clownfish
- ✅ Shows result count

### Categories
- ✅ Tap "Cá biển" → See 20 sea fish
- ✅ Tap "Cá sông" → See 20 river fish
- ✅ Tap "Cá nước lợ" → See 20 brackish fish
- ✅ Tap "Cá cảnh" → See 20 aquarium fish

### Fish Details
- ✅ Tap any fish card
- ✅ See full information
- ✅ Price, habitat, diet, weight
- ✅ Add to cart button
- ✅ Favorite toggle

---

## 5️⃣ Verify Database

### Check Fish Data
1. Android Studio → **App Inspection** → **Database Inspector**
2. Select `aqualife_database`
3. Open `fish_table`
4. Should see **80 rows**
5. Check columns: name, price, category, rating, soldCount

### Sample Queries
```sql
-- All fish
SELECT COUNT(*) FROM fish_table;  -- Returns 80

-- Sea fish only
SELECT * FROM fish_table WHERE category = 'Cá biển';  -- 20 rows

-- Search by name
SELECT * FROM fish_table WHERE name LIKE '%Lóc%';  -- Cá Lóc Đồng

-- Discounted items
SELECT * FROM fish_table WHERE hasDiscount = 1;  -- 12 rows

-- Best sellers
SELECT * FROM fish_table ORDER BY soldCount DESC LIMIT 10;

-- High rated fish
SELECT * FROM fish_table WHERE rating >= 4.8;
```

---

## 6️⃣ Check Firebase

### Firebase Console
1. Go to https://console.firebase.google.com
2. Select your project
3. **Firestore Database** → `products` collection
4. Should see 80 documents (sea_01, sea_02, river_01, etc.)
5. Each document has all fish data

### Real-time Sync Test
1. Open Firebase Console
2. Edit a fish price (e.g., change `sea_01` price to 300000)
3. Save in Firebase
4. **App updates within 1 second** (no refresh needed)
5. Verify new price appears in app

---

## 7️⃣ Common Issues

### Build Fails
**Error:** Firebase dependencies not found  
**Fix:** 
```bash
./gradlew --refresh-dependencies clean
./gradlew assembleDebug
```

### App Crashes on Login
**Error:** Layout issue in home screen  
**Fix:** Already fixed (SkeletonGrid has fixed height)  
**Verify:** Check `SkeletonLoader.kt` line 91 has `modifier = Modifier.height(600.dp)`

### Database Empty
**Check:** Logcat for init messages  
**Fix:** HomeViewModel.init() should call repository.initializeData()  
**Manual:** Clear app data and reinstall

### Search Returns Nothing
**Check:** Database has 80 fish (use Database Inspector)  
**Verify:** SearchScreen uses `viewModel.searchFish()`  
**Test:** Search "Cá" (should return all 80)

---

## 8️⃣ Project Structure

```
Clone-Aqualife/
├── app/
│   └── src/main/java/com/example/aqualife/
│       ├── data/local/
│       │   ├── FishSeedData.kt ⭐ 80 fish
│       │   ├── AquaLifeDatabase.kt
│       │   ├── dao/ (6 DAOs)
│       │   └── entity/ (6 entities)
│       ├── ui/
│       │   ├── screen/ (Auth, Home, Search, etc.)
│       │   ├── viewmodel/ (5 ViewModels)
│       │   └── components/
│       └── di/ (Dependency injection)
├── docs/ ⭐ All documentation (14 files)
└── README.md
```

---

## 9️⃣ Documentation

**Main Guides:**
- `docs/PROJECT_STATUS.md` - Current status
- `docs/COMPLETE_IMPLEMENTATION_SUMMARY.md` - Full details
- `docs/DATABASE_IMPLEMENTATION_COMPLETE.md` - Database guide
- `docs/DATABASE_MIGRATION_GUIDE.md` - Migration guide
- `docs/NEED UPDATE.md` - Requirements checklist

---

## 🎓 Learning Resources

### Understand the Code
1. **Start here:** `docs/APP_STRUCTURE.md`
2. **Database:** `docs/DATABASE_IMPLEMENTATION_COMPLETE.md`
3. **Auth:** `docs/AUTH_FIXES_SUMMARY.md`
4. **MVVM:** `docs/VIEWMODEL_INTEGRATION_GUIDE.md`

### Key Patterns Used
- **Offline-First**: Room as source of truth
- **Repository Pattern**: Data access abstraction
- **Dependency Injection**: Hilt for scalability
- **Reactive Programming**: Flow + StateFlow
- **Sealed Classes**: Type-safe state management

---

## 📞 Quick Commands

```bash
# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Clean
./gradlew clean

# Check dependencies
./gradlew app:dependencies

# View database (requires device/emulator running)
adb shell
cd /data/data/com.example.aqualife/databases/
ls -la
```

---

## ✅ Success Checklist

After building, verify:
- [ ] App installs without errors
- [ ] Welcome screen shows
- [ ] Login with admin123/admin123 works
- [ ] Home screen shows Vietnamese fish names
- [ ] Search finds "Cá Lóc"
- [ ] Category filtering works
- [ ] Fish detail screen opens
- [ ] No crashes
- [ ] Database has 80 fish (check App Inspection)
- [ ] Firebase has 80 documents in `products` collection

---

**Ready to deploy!** 🚀

**Location:** `C:\Users\Vy Hao\Desktop\Clone-Aqualife`

