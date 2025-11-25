/**
 * Firebase Image Migration Script (Node.js)
 * 
 * This script migrates all image URLs in Firebase from Unsplash to Picsum
 * 
 * Prerequisites:
 * 1. Install Firebase Admin SDK: npm install firebase-admin
 * 2. Get your Firebase service account key from Firebase Console
 * 3. Place serviceAccountKey.json in this directory
 * 
 * Usage:
 * node migrate-firebase-images.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // Download from Firebase Console

// Initialize Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Mapping from FishSeedData.kt - Update this if seed data changes
const fishImageMap = {
  // Cá Biển (20 loại)
  "Cá Thu Phấn": "https://picsum.photos/seed/sea01/640/400",
  "Cá Ngừ Đại Dương": "https://picsum.photos/seed/sea02/640/400",
  "Cá Bớp Biển": "https://picsum.photos/seed/sea03/640/400",
  "Cá Nục Suôn": "https://picsum.photos/seed/sea04/640/400",
  "Cá Chim Trắng": "https://picsum.photos/seed/sea05/640/400",
  "Cá Mú Đỏ": "https://picsum.photos/seed/sea06/640/400",
  "Cá Hố Rồng": "https://picsum.photos/seed/sea07/640/400",
  "Cá Phèn Hồng": "https://picsum.photos/seed/sea08/640/400",
  "Cá Bạc Má": "https://picsum.photos/seed/sea09/640/400",
  "Cá Cam": "https://picsum.photos/seed/sea10/640/400",
  "Cá Đuối Nghệ": "https://picsum.photos/seed/sea11/640/400",
  "Cá Nhám": "https://picsum.photos/seed/sea12/640/400",
  "Cá Trích Tròn": "https://picsum.photos/seed/sea13/640/400",
  "Cá Chuồn Cồ": "https://picsum.photos/seed/sea14/640/400",
  "Cá Dìa Biển": "https://picsum.photos/seed/sea15/640/400",
  "Cá Mai": "https://picsum.photos/seed/sea16/640/400",
  "Cá Bò Da": "https://picsum.photos/seed/sea17/640/400",
  "Cá Sơn Đá": "https://picsum.photos/seed/sea18/640/400",
  "Cá Mòi Dầu": "https://picsum.photos/seed/sea19/640/400",
  "Cá Hồi Nauy": "https://picsum.photos/seed/sea20/640/400",
  
  // Cá Sông (20 loại)
  "Cá Lóc Đồng": "https://picsum.photos/seed/river01/640/400",
  "Cá Trê Vàng": "https://picsum.photos/seed/river02/640/400",
  "Cá Rô Đồng": "https://picsum.photos/seed/river03/640/400",
  "Cá Chép Giòn": "https://picsum.photos/seed/river04/640/400",
  "Cá Trắm Cỏ": "https://picsum.photos/seed/river05/640/400",
  "Cá Mè Hoa": "https://picsum.photos/seed/river06/640/400",
  "Cá Lăng Nha": "https://picsum.photos/seed/river07/640/400",
  "Cá Tra Dầu": "https://picsum.photos/seed/river08/640/400",
  "Cá Basa": "https://picsum.photos/seed/river09/640/400",
  "Cá Heo Nước Ngọt": "https://picsum.photos/seed/river10/640/400",
  "Cá Linh Non": "https://picsum.photos/seed/river11/640/400",
  "Cá Chạch Lấu": "https://picsum.photos/seed/river12/640/400",
  "Cá Bống Tượng": "https://picsum.photos/seed/river13/640/400",
  "Cá Thát Lát": "https://picsum.photos/seed/river14/640/400",
  "Cá Hô": "https://picsum.photos/seed/river15/640/400",
  "Cá Chày": "https://picsum.photos/seed/river16/640/400",
  "Cá Ngạnh": "https://picsum.photos/seed/river17/640/400",
  "Cá Diếc": "https://picsum.photos/seed/river18/640/400",
  "Cá Rô Phi": "https://picsum.photos/seed/river19/640/400",
  "Cá Trôi": "https://picsum.photos/seed/river20/640/400",
  
  // Cá Nước Lợ (20 loại)
  "Cá Chẽm": "https://picsum.photos/seed/brackish01/640/400",
  "Cá Kèo": "https://picsum.photos/seed/brackish02/640/400",
  "Cá Đối Mục": "https://picsum.photos/seed/brackish03/640/400",
  "Cá Nâu": "https://picsum.photos/seed/brackish04/640/400",
  "Cá Dìa Công": "https://picsum.photos/seed/brackish05/640/400",
  "Cá Măng": "https://picsum.photos/seed/brackish06/640/400",
  "Cá Bớp Lợ": "https://picsum.photos/seed/brackish07/640/400",
  "Cá Chim Vàng": "https://picsum.photos/seed/brackish08/640/400",
  "Cá Chạch Lấu": "https://picsum.photos/seed/brackish09/640/400",
  "Cá Mú Trân Châu": "https://picsum.photos/seed/brackish10/640/400",
  "Cá Đù Sóc": "https://picsum.photos/seed/brackish11/640/400",
  "Cá Khoai": "https://picsum.photos/seed/brackish12/640/400",
  "Cá Dứa": "https://picsum.photos/seed/brackish13/640/400",
  "Cá Bè Trang": "https://picsum.photos/seed/brackish14/640/400",
  "Cá Sủ Đất": "https://picsum.photos/seed/brackish15/640/400",
  "Cá Hồng Mỹ": "https://picsum.photos/seed/brackish16/640/400",
  "Cá Dìa Bông": "https://picsum.photos/seed/brackish17/640/400",
  "Cá Kình": "https://picsum.photos/seed/brackish18/640/400",
  "Cá Bống Dừa": "https://picsum.photos/seed/brackish19/640/400",
  "Cá Bống Sao": "https://picsum.photos/seed/brackish20/640/400",
  
  // Cá Cảnh (20 loại)
  "Cá Rồng Huyết Long": "https://picsum.photos/seed/pet01/640/400",
  "Cá Koi Kohaku": "https://picsum.photos/seed/pet02/640/400",
  "Cá Betta Halfmoon": "https://picsum.photos/seed/pet03/640/400",
  "Cá Hề Nemo": "https://picsum.photos/seed/pet04/640/400",
  "Cá La Hán": "https://picsum.photos/seed/pet05/640/400",
  "Cá Dĩa (Discus)": "https://picsum.photos/seed/pet06/640/400",
  "Cá Bảy Màu": "https://picsum.photos/seed/pet07/640/400",
  "Cá Ba Đuôi": "https://picsum.photos/seed/pet08/640/400",
  "Cá Neon Vua": "https://picsum.photos/seed/pet09/640/400",
  "Cá Phượng Hoàng": "https://picsum.photos/seed/pet10/640/400",
  "Cá Ông Tiên": "https://picsum.photos/seed/pet11/640/400",
  "Cá Hồng Két": "https://picsum.photos/seed/pet12/640/400",
  "Cá Thần Tiên": "https://picsum.photos/seed/pet13/640/400",
  "Cá Ali Thái": "https://picsum.photos/seed/pet14/640/400",
  "Cá Sọc Ngựa": "https://picsum.photos/seed/pet15/640/400",
  "Cá Bình Tích": "https://picsum.photos/seed/pet16/640/400",
  "Cá Mún Đỏ": "https://picsum.photos/seed/pet17/640/400",
  "Cá Kiếm": "https://picsum.photos/seed/pet18/640/400",
  "Cá Pleco": "https://picsum.photos/seed/pet19/640/400",
  "Cá Chuột Panda": "https://picsum.photos/seed/pet20/640/400"
};

async function migrateFirebaseImages() {
  try {
    console.log('🚀 Starting Firebase image migration...\n');
    
    // Get all documents from products collection
    const snapshot = await db.collection('products').get();
    
    if (snapshot.empty) {
      console.log('⚠️  Firebase collection is empty. No migration needed.');
      return;
    }
    
    console.log(`📊 Found ${snapshot.size} documents in Firebase\n`);
    
    const batch = db.batch();
    let updateCount = 0;
    let skipCount = 0;
    let notFoundCount = 0;
    
    snapshot.forEach(doc => {
      const data = doc.data();
      const name = data.name || '';
      const currentImageUrl = data.imageUrl || '';
      
      // Check if URL needs migration (contains unsplash)
      if (currentImageUrl.includes('unsplash.com')) {
        // Find matching new URL
        const newImageUrl = fishImageMap[name];
        
        if (newImageUrl && newImageUrl !== currentImageUrl) {
          console.log(`✅ Updating: ${name}`);
          console.log(`   Old: ${currentImageUrl.substring(0, 60)}...`);
          console.log(`   New: ${newImageUrl}\n`);
          
          batch.update(doc.ref, { imageUrl: newImageUrl });
          updateCount++;
        } else {
          console.log(`⚠️  No mapping found for: ${name}\n`);
          notFoundCount++;
        }
      } else {
        console.log(`⏭️  Skipping ${name} (already using Picsum or other URL)\n`);
        skipCount++;
      }
    });
    
    if (updateCount > 0) {
      await batch.commit();
      console.log('\n✅ Migration completed successfully!');
      console.log(`   Updated: ${updateCount} documents`);
      console.log(`   Skipped: ${skipCount} documents`);
      if (notFoundCount > 0) {
        console.log(`   Not found in mapping: ${notFoundCount} documents`);
      }
    } else {
      console.log('\n✅ No documents needed migration');
      console.log(`   All documents already use Picsum URLs`);
    }
    
  } catch (error) {
    console.error('❌ Migration failed:', error);
    process.exit(1);
  }
}

// Run migration
migrateFirebaseImages()
  .then(() => {
    console.log('\n🎉 Done!');
    process.exit(0);
  })
  .catch(error => {
    console.error('❌ Error:', error);
    process.exit(1);
  });

