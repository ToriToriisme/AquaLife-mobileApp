package com.example.aqualife.data.local

import com.example.aqualife.data.local.entity.FishEntity
import kotlin.random.Random

object FishSeedData {

    // Hàm chính để tạo dữ liệu
    fun generateRealFishData(): List<FishEntity> {
        val list = mutableListOf<FishEntity>()

        // 1. Cá Biển (20 loại)
        list.addAll(createListFromMap("Cá biển", caBienMap, 150000.0, 500000.0, true))

        // 2. Cá Sông (20 loại)
        list.addAll(createListFromMap("Cá sông", caSongMap, 50000.0, 200000.0, true))

        // 3. Cá Nước Lợ (20 loại)
        list.addAll(createListFromMap("Cá nước lợ", caNuocLoMap, 100000.0, 300000.0, true))

        // 4. Cá Cảnh (20 loại)
        list.addAll(createListFromMap("Cá cảnh", caCanhMap, 50000.0, 5000000.0, false))

        return list
    }

    // --- HÀM MAP DỮ LIỆU ---
    private fun createListFromMap(
        category: String,
        dataMap: Map<String, String>, // Map<Tên Cá, Link Ảnh>
        minPrice: Double,
        maxPrice: Double,
        isPerKg: Boolean
    ): List<FishEntity> {
        val list = mutableListOf<FishEntity>()
        var index = 0

        for ((name, imageUrl) in dataMap) {
            val rawPrice = Random.nextDouble(minPrice, maxPrice)
            val price = (rawPrice / 1000).toInt() * 1000.0 // Làm tròn giá

            // Logic Random phụ (Giảm giá, Rating)
            val hasDiscount = Random.nextDouble() < 0.3
            val discountPrice = if (hasDiscount) price * 0.8 else null
            val rating = String.format("%.1f", Random.nextDouble(3.8, 5.0)).toFloat()
            val soldCount = Random.nextInt(10, 5000)
            val unit = if (isPerKg) "kg" else "con"

            val fishId = when (category) {
                "Cá biển" -> "sea_${String.format("%02d", index + 1)}"
                "Cá sông" -> "river_${String.format("%02d", index + 1)}"
                "Cá nước lợ" -> "brackish_${String.format("%02d", index + 1)}"
                "Cá cảnh" -> "pet_${String.format("%02d", index + 1)}"
                else -> "${category.hashCode()}_$index"
            }

            list.add(
                FishEntity(
                    id = fishId,
                    name = name,
                    price = price,
                    priceInt = price.toInt(),
                    category = category,
                    imageUrl = imageUrl,
                    rating = rating,
                    soldCount = soldCount,
                    hasDiscount = hasDiscount,
                    discountPrice = discountPrice,
                    description = "Đặc sản $category tươi ngon. Nguồn gốc tự nhiên, đảm bảo chất lượng. Đơn vị tính: 1 $unit.",
                    habitat = if(category == "Cá cảnh") "Bể kính/Thủy sinh" else "Tự nhiên/Ao hồ",
                    diet = if(category == "Cá cảnh") "Cám chuyên dụng" else "Tạp ăn",
                    maxWeight = "${Random.nextInt(1, 5)} $unit",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            index++
        }
        return list
    }

    // ==================================================================
    // DỮ LIỆU CỐ ĐỊNH (80 LOẠI CÁ - KÈM ẢNH CHUẨN)
    // ==================================================================

    // 1. MAP CÁ BIỂN (20 loại) - Using picsum.photos for stable images
    private val caBienMap = mapOf(
        "Cá Thu Phấn" to "https://picsum.photos/seed/sea01/640/400",
        "Cá Ngừ Đại Dương" to "https://picsum.photos/seed/sea02/640/400",
        "Cá Bớp Biển" to "https://picsum.photos/seed/sea03/640/400",
        "Cá Nục Suôn" to "https://picsum.photos/seed/sea04/640/400",
        "Cá Chim Trắng" to "https://picsum.photos/seed/sea05/640/400",
        "Cá Mú Đỏ" to "https://picsum.photos/seed/sea06/640/400",
        "Cá Hố Rồng" to "https://picsum.photos/seed/sea07/640/400",
        "Cá Phèn Hồng" to "https://picsum.photos/seed/sea08/640/400",
        "Cá Bạc Má" to "https://picsum.photos/seed/sea09/640/400",
        "Cá Cam" to "https://picsum.photos/seed/sea10/640/400",
        "Cá Đuối Nghệ" to "https://picsum.photos/seed/sea11/640/400",
        "Cá Nhám" to "https://picsum.photos/seed/sea12/640/400",
        "Cá Trích Tròn" to "https://picsum.photos/seed/sea13/640/400",
        "Cá Chuồn Cồ" to "https://picsum.photos/seed/sea14/640/400",
        "Cá Dìa Biển" to "https://picsum.photos/seed/sea15/640/400",
        "Cá Mai" to "https://picsum.photos/seed/sea16/640/400",
        "Cá Bò Da" to "https://picsum.photos/seed/sea17/640/400",
        "Cá Sơn Đá" to "https://picsum.photos/seed/sea18/640/400",
        "Cá Mòi Dầu" to "https://picsum.photos/seed/sea19/640/400",
        "Cá Hồi Nauy" to "https://picsum.photos/seed/sea20/640/400"
    )

    // 2. MAP CÁ SÔNG (20 loại) - Using picsum.photos for stable images
    private val caSongMap = mapOf(
        "Cá Lóc Đồng" to "https://picsum.photos/seed/river01/640/400",
        "Cá Trê Vàng" to "https://picsum.photos/seed/river02/640/400",
        "Cá Rô Đồng" to "https://picsum.photos/seed/river03/640/400",
        "Cá Chép Giòn" to "https://picsum.photos/seed/river04/640/400",
        "Cá Trắm Cỏ" to "https://picsum.photos/seed/river05/640/400",
        "Cá Mè Hoa" to "https://picsum.photos/seed/river06/640/400",
        "Cá Lăng Nha" to "https://picsum.photos/seed/river07/640/400",
        "Cá Tra Dầu" to "https://picsum.photos/seed/river08/640/400",
        "Cá Basa" to "https://picsum.photos/seed/river09/640/400",
        "Cá Heo Nước Ngọt" to "https://picsum.photos/seed/river10/640/400",
        "Cá Linh Non" to "https://picsum.photos/seed/river11/640/400",
        "Cá Chạch Lấu" to "https://picsum.photos/seed/river12/640/400",
        "Cá Bống Tượng" to "https://picsum.photos/seed/river13/640/400",
        "Cá Thát Lát" to "https://picsum.photos/seed/river14/640/400",
        "Cá Hô" to "https://picsum.photos/seed/river15/640/400",
        "Cá Chày" to "https://picsum.photos/seed/river16/640/400",
        "Cá Ngạnh" to "https://picsum.photos/seed/river17/640/400",
        "Cá Diếc" to "https://picsum.photos/seed/river18/640/400",
        "Cá Rô Phi" to "https://picsum.photos/seed/river19/640/400",
        "Cá Trôi" to "https://picsum.photos/seed/river20/640/400"
    )

    // 3. MAP CÁ NƯỚC LỢ (20 loại) - Using picsum.photos for stable images
    private val caNuocLoMap = mapOf(
        "Cá Chẽm" to "https://picsum.photos/seed/brackish01/640/400",
        "Cá Kèo" to "https://picsum.photos/seed/brackish02/640/400",
        "Cá Đối Mục" to "https://picsum.photos/seed/brackish03/640/400",
        "Cá Nâu" to "https://picsum.photos/seed/brackish04/640/400",
        "Cá Dìa Công" to "https://picsum.photos/seed/brackish05/640/400",
        "Cá Măng" to "https://picsum.photos/seed/brackish06/640/400",
        "Cá Bớp Lợ" to "https://picsum.photos/seed/brackish07/640/400",
        "Cá Chim Vàng" to "https://picsum.photos/seed/brackish08/640/400",
        "Cá Chạch Lấu" to "https://picsum.photos/seed/brackish09/640/400",
        "Cá Mú Trân Châu" to "https://picsum.photos/seed/brackish10/640/400",
        "Cá Đù Sóc" to "https://picsum.photos/seed/brackish11/640/400",
        "Cá Khoai" to "https://picsum.photos/seed/brackish12/640/400",
        "Cá Dứa" to "https://picsum.photos/seed/brackish13/640/400",
        "Cá Bè Trang" to "https://picsum.photos/seed/brackish14/640/400",
        "Cá Sủ Đất" to "https://picsum.photos/seed/brackish15/640/400",
        "Cá Hồng Mỹ" to "https://picsum.photos/seed/brackish16/640/400",
        "Cá Dìa Bông" to "https://picsum.photos/seed/brackish17/640/400",
        "Cá Kình" to "https://picsum.photos/seed/brackish18/640/400",
        "Cá Bống Dừa" to "https://picsum.photos/seed/brackish19/640/400",
        "Cá Bống Sao" to "https://picsum.photos/seed/brackish20/640/400"
    )

    // 4. MAP CÁ CẢNH (20 loại) - Using picsum.photos for stable images
    private val caCanhMap = mapOf(
        "Cá Rồng Huyết Long" to "https://picsum.photos/seed/pet01/640/400",
        "Cá Koi Kohaku" to "https://picsum.photos/seed/pet02/640/400",
        "Cá Betta Halfmoon" to "https://picsum.photos/seed/pet03/640/400",
        "Cá Hề Nemo" to "https://picsum.photos/seed/pet04/640/400",
        "Cá La Hán" to "https://picsum.photos/seed/pet05/640/400",
        "Cá Dĩa (Discus)" to "https://picsum.photos/seed/pet06/640/400",
        "Cá Bảy Màu" to "https://picsum.photos/seed/pet07/640/400",
        "Cá Ba Đuôi" to "https://picsum.photos/seed/pet08/640/400",
        "Cá Neon Vua" to "https://picsum.photos/seed/pet09/640/400",
        "Cá Phượng Hoàng" to "https://picsum.photos/seed/pet10/640/400",
        "Cá Ông Tiên" to "https://picsum.photos/seed/pet11/640/400",
        "Cá Hồng Két" to "https://picsum.photos/seed/pet12/640/400",
        "Cá Thần Tiên" to "https://picsum.photos/seed/pet13/640/400",
        "Cá Ali Thái" to "https://picsum.photos/seed/pet14/640/400",
        "Cá Sọc Ngựa" to "https://picsum.photos/seed/pet15/640/400",
        "Cá Bình Tích" to "https://picsum.photos/seed/pet16/640/400",
        "Cá Mún Đỏ" to "https://picsum.photos/seed/pet17/640/400",
        "Cá Kiếm" to "https://picsum.photos/seed/pet18/640/400",
        "Cá Pleco" to "https://picsum.photos/seed/pet19/640/400",
        "Cá Chuột Panda" to "https://picsum.photos/seed/pet20/640/400"
    )
}
