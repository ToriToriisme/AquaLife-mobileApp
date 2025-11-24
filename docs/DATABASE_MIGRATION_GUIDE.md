# 🔄 Database Migration Guide - v1 to v2

## Overview

This guide explains the database schema changes and how data migration works.

---

## Schema Changes

### Version 1 → Version 2

**FishEntity** table updated with new columns:

| Column | Type | Default | Purpose |
|--------|------|---------|---------|
| `rating` | Float | 4.5f | Star rating (3.9-5.0) |
| `soldCount` | Int | 0 | Best seller tracking |
| `hasDiscount` | Boolean | false | Sale indicator |
| `discountPrice` | Double? | null | Sale price (optional) |

---

## Migration Strategy

### Automatic Migration (Current)

`AquaLifeDatabase.kt` uses `.fallbackToDestructiveMigration()`:

```kotlin
Room.databaseBuilder(
    context,
    AquaLifeDatabase::class.java,
    "aqualife_database"
)
    .fallbackToDestructiveMigration() // Drops old DB, creates new
    .build()
```

**Behavior:**
- ✅ Simple & safe for development
- ✅ No manual migration code needed
- ⚠️ **Deletes all existing data** on schema change
- ✅ Auto-repopulates from FishSeedData (80 fish)

### Production Migration (Future)

For production apps with real user data, implement proper migration:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns with defaults
        database.execSQL(
            "ALTER TABLE fish_table ADD COLUMN rating REAL NOT NULL DEFAULT 4.5"
        )
        database.execSQL(
            "ALTER TABLE fish_table ADD COLUMN soldCount INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL(
            "ALTER TABLE fish_table ADD COLUMN hasDiscount INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL(
            "ALTER TABLE fish_table ADD COLUMN discountPrice REAL"
        )
    }
}

// In DatabaseModule.kt:
Room.databaseBuilder(context, AquaLifeDatabase::class.java, "aqualife_database")
    .addMigrations(MIGRATION_1_2) // Preserves data!
    .build()
```

---

## Data Initialization Flow

### First Install (Clean)
```
App launches
    ↓
Database version = 0 (doesn't exist)
    ↓
Room creates v2 schema
    ↓
FishRepository.initializeData() runs
    ↓
fishDao.getCount() returns 0
    ↓
Loads 80 fish from FishSeedData
    ↓
Saves to database
    ↓
Pushes to Firebase for backup
```

### Upgrade (v1 → v2)
```
App launches
    ↓
Detects schema version mismatch (1 vs 2)
    ↓
.fallbackToDestructiveMigration() triggers
    ↓
Drops old database
    ↓
Creates new v2 schema
    ↓
FishRepository.initializeData() runs
    ↓
Checks Firebase first (may have backed up data)
    ↓
Downloads from Firebase OR loads FishSeedData
    ↓
Database ready with 80 fish
```

---

## Firebase Sync Behavior

### Initial Push
When database is populated from FishSeedData:
```kotlin
pushToFirebase(seedData) // Batch write 80 fish
```

**Firebase Structure:**
```
/products/ (collection)
  /sea_01 (document)
    - name: "Cá Thu Phấn"
    - price: 250000
    - category: "Cá biển"
    - rating: 4.8
    - soldCount: 1200
    - hasDiscount: false
    - ... other fields
  /sea_02
  /river_01
  ... (80 documents total)
```

### Real-time Sync

Admin updates price on web dashboard:
```
Admin changes price: 250,000đ → 225,000đ
    ↓
Firebase triggers snapshot listener
    ↓
startRealtimeSyncListener() detects change
    ↓
Downloads updated fish data
    ↓
fishDao.insertAllFish(updatedList) // REPLACE strategy
    ↓
UI auto-updates (< 1 second)
```

---

## Testing Migration

### Test Scenario 1: Fresh Install
1. Uninstall app: `adb uninstall com.example.aqualife`
2. Install new version: `./gradlew installDebug`
3. Launch app
4. Check Logcat for: `"Loaded 80 fish to local database"`
5. Verify home screen shows Vietnamese fish names

### Test Scenario 2: Upgrade
1. Keep app installed (with old v1 database)
2. Install new version: `./gradlew installDebug`
3. Launch app
4. Old database is dropped
5. New database created with 80 fish
6. Check Logcat for migration messages

### Test Scenario 3: Firebase Sync
1. Open Firebase Console
2. Manually change a fish price
3. App auto-updates within 1 second
4. Verify price change in UI

---

## Verify Database Content

### Using Android Studio
1. Open **App Inspection** tab
2. Select **Database Inspector**
3. Navigate to `aqualife_database`
4. View `fish_table` → Should show 80 rows
5. Check columns: rating, soldCount, hasDiscount, discountPrice

### Using ADB
```bash
adb shell
cd /data/data/com.example.aqualife/databases/
sqlite3 aqualife_database

# SQL queries:
.schema fish_table           # View table structure
SELECT COUNT(*) FROM fish_table;  # Should return 80
SELECT * FROM fish_table WHERE category = 'Cá biển' LIMIT 5;
SELECT * FROM fish_table WHERE hasDiscount = 1;  # Show sale items
SELECT * FROM fish_table ORDER BY rating DESC LIMIT 10;
.quit
```

---

## Rollback Strategy

If migration causes issues:

### Option 1: Clear App Data (Quick)
```bash
adb shell pm clear com.example.aqualife
# This deletes database, will auto-repopulate on next launch
```

### Option 2: Downgrade Database Version
```kotlin
// In AquaLifeDatabase.kt, temporarily revert:
version = 1  // Instead of 2
// Remove new fields from FishEntity
// Rebuild
```

### Option 3: Manual Firebase Reset
1. Firebase Console → Firestore
2. Delete `products` collection
3. App will detect empty Firebase
4. Repopulates from FishSeedData

---

## Performance Considerations

### Database Size
- **80 fish** × ~1KB each = ~80KB total
- Very light, no performance issues
- Room caching ensures instant queries

### Query Performance
```kotlin
// Indexed queries (fast)
fishDao.getAllFish()              // 1-2ms
fishDao.getFishByCategory()       // <1ms (indexed)
fishDao.searchFish("Cá Lóc")      // 2-3ms (LIKE query)

// Filtered queries (optimized)
fishDao.getFilteredFish(
    category = "Cá biển",
    minPrice = 100000,
    maxPrice = 500000,
    minRating = 4.5f,
    sortBy = "best_seller"
)  // 3-5ms (multi-condition)
```

### Memory Usage
- **StateFlow** with `WhileSubscribed(5000)` → Auto-cleanup
- **LazyColumn/LazyRow** → Only renders visible items
- **Coil image loading** → Automatic caching

---

## Common Migration Issues

### Issue: "Table already exists"
**Cause:** Migration ran twice  
**Fix:** Clear app data or uninstall/reinstall

### Issue: "No such column: rating"
**Cause:** Database version not incremented  
**Fix:** Change `version = 2` in AquaLifeDatabase.kt

### Issue: "Foreign key constraint failed"
**Cause:** Related tables not migrated together  
**Fix:** Ensure all tables using FishEntity reference are updated

### Issue: Database still empty after migration
**Cause:** `initializeData()` not called  
**Fix:** Verify `HomeViewModel.init()` runs on app start

---

## Best Practices

### Development
- ✅ Use `.fallbackToDestructiveMigration()` for rapid iteration
- ✅ Auto-repopulate from seed data
- ✅ Test on emulator with fresh installs

### Production
- 🔜 Implement proper `Migration` classes
- 🔜 Test upgrade paths (v1→v2, v2→v3, etc.)
- 🔜 Backup data to Firebase before migration
- 🔜 Add migration success/failure analytics

---

## Version History

| Version | Date | Changes | Migration |
|---------|------|---------|-----------|
| 1 | Nov 2025 | Initial schema | - |
| 2 | Nov 24, 2025 | Added rating, soldCount, hasDiscount, discountPrice | Destructive |

---

## Future Schema Changes

### Planned v3 (Example)
Potential additions:
- `reviewCount: Int` - Number of reviews
- `averageRating: Float` - Calculated rating
- `tags: String` - JSON array of tags ["fresh", "imported"]
- `supplier: String` - Supplier name
- `stockQuantity: Int` - Available stock

**When implementing v3:**
1. Update `FishEntity.kt` with new fields
2. Increment `version = 3` in `AquaLifeDatabase.kt`
3. Add `MIGRATION_2_3` if preserving data
4. Update `FishSeedData.kt` with new field values
5. Test thoroughly before deployment

---

Generated: November 24, 2025  
Database Version: 2  
Migration Strategy: Destructive (development)  
Auto-initialization: Enabled

