package com.example.aqualife.data.local

import com.example.aqualife.data.local.entity.FishEntity

/**
 * Fish Seed Data - 80 real Vietnamese fish (20 per category)
 * Auto-populated on first app launch
 */
object FishSeedData {

    fun generateRealFishData(): List<FishEntity> {
        return getSeaFish() + getRiverFish() + getBrackishFish() + getAquariumFish()
    }

    // Cá Biển (20 loại)
    private fun getSeaFish(): List<FishEntity> = listOf(
        FishEntity("sea_01", "Cá Thu Phấn", 250000.0, 250000, "Cá biển", "Biển khơi", "15 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?mackerel,fish", "Thịt thơm, ngọt, ít xương, rất giàu Omega-3.", System.currentTimeMillis(), 4.8f, 1200, false, null),
        FishEntity("sea_02", "Cá Ngừ Đại Dương", 180000.0, 180000, "Cá biển", "Đại dương", "200 kg", "Mực, tôm", "https://source.unsplash.com/featured/?tuna,fish", "Thích hợp làm Sashimi hoặc áp chảo.", System.currentTimeMillis(), 4.9f, 3500, true, 162000.0),
        FishEntity("sea_03", "Cá Bớp Biển", 320000.0, 320000, "Cá biển", "Rạn san hô", "30 kg", "Cua, ghẹ", "https://source.unsplash.com/featured/?cobia,fish", "Thịt trắng, dai ngon, da dày béo ngậy.", System.currentTimeMillis(), 4.7f, 850, false, null),
        FishEntity("sea_04", "Cá Nục Suôn", 60000.0, 60000, "Cá biển", "Vùng vịnh", "0.5 kg", "Phù du", "https://source.unsplash.com/featured/?scad,fish", "Cá bình dân, kho tiêu hoặc hấp cuốn bánh tráng cực ngon.", System.currentTimeMillis(), 4.2f, 5000, false, null),
        FishEntity("sea_05", "Cá Chim Trắng", 190000.0, 190000, "Cá biển", "Biển nông", "3 kg", "Sứa, tôm", "https://source.unsplash.com/featured/?pomfret,fish", "Thịt mềm, ngọt, chiên giòn hoặc nấu ngót.", System.currentTimeMillis(), 4.6f, 900, true, 171000.0),
        FishEntity("sea_06", "Cá Mú Đỏ", 450000.0, 450000, "Cá biển", "Rạn san hô sâu", "10 kg", "Cá con", "https://source.unsplash.com/featured/?grouper,fish", "Loại cá quý hiếm, thịt đỏ, rất ngọt và thơm.", System.currentTimeMillis(), 5.0f, 300, false, null),
        FishEntity("sea_07", "Cá Hố Rồng", 220000.0, 220000, "Cá biển", "Biển sâu", "5 kg", "Mực nhỏ", "https://source.unsplash.com/featured/?hairtail,fish", "Thân dài, bạc óng, nướng muối ớt hoặc kho nghệ.", System.currentTimeMillis(), 4.5f, 600, false, null),
        FishEntity("sea_08", "Cá Phèn Hồng", 120000.0, 120000, "Cá biển", "Đáy cát", "0.8 kg", "Giáp xác", "https://source.unsplash.com/featured/?redmullet,fish", "Thịt nạc, ít xương dăm, chiên sả ớt rất đưa cơm.", System.currentTimeMillis(), 4.4f, 1500, true, 108000.0),
        FishEntity("sea_09", "Cá Bạc Má", 80000.0, 80000, "Cá biển", "Vùng khơi", "0.4 kg", "Sinh vật phù du", "https://source.unsplash.com/featured/?indianmackerel,fish", "Cá béo, nhiều thịt, hấp gừng hành.", System.currentTimeMillis(), 4.1f, 4200, false, null),
        FishEntity("sea_10", "Cá Cam", 160000.0, 160000, "Cá biển", "Vùng khơi", "10 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?yellowtail,fish", "Thịt chắc, ngọt, nấu lẩu hoặc nướng giấy bạc.", System.currentTimeMillis(), 4.6f, 1100, false, null),
        FishEntity("sea_11", "Cá Đuối Nghệ", 150000.0, 150000, "Cá biển", "Đáy bùn", "15 kg", "Giáp xác", "https://source.unsplash.com/featured/?stingray,fish", "Xương sụn giòn, nấu canh chua bắp chuối hoặc nướng mỡ hành.", System.currentTimeMillis(), 4.3f, 2000, true, 135000.0),
        FishEntity("sea_12", "Cá Nhám", 200000.0, 200000, "Cá biển", "Đại dương", "50 kg", "Cá, mực", "https://source.unsplash.com/featured/?shark,fish", "Thịt dai như gà, làm gỏi hoặc nấu lẩu.", System.currentTimeMillis(), 4.5f, 450, false, null),
        FishEntity("sea_13", "Cá Trích Tròn", 50000.0, 50000, "Cá biển", "Vùng vịnh", "0.2 kg", "Phù du", "https://source.unsplash.com/featured/?herring,fish", "Làm gỏi cá trích đặc sản Phú Quốc.", System.currentTimeMillis(), 4.0f, 6000, true, 40000.0),
        FishEntity("sea_14", "Cá Chuồn Cồ", 110000.0, 110000, "Cá biển", "Mặt nước", "0.5 kg", "Phù du", "https://source.unsplash.com/featured/?flyingfish,fish", "Cá có cánh, chiên nghệ hoặc kho mít.", System.currentTimeMillis(), 4.4f, 1200, false, null),
        FishEntity("sea_15", "Cá Dìa Biển", 250000.0, 250000, "Cá biển", "Rạn đá", "1.5 kg", "Tảo biển", "https://source.unsplash.com/featured/?rabbitfish,fish", "Thịt trắng phau, nấu lá giang.", System.currentTimeMillis(), 4.7f, 800, false, null),
        FishEntity("sea_16", "Cá Mai", 90000.0, 90000, "Cá biển", "Ven bờ", "0.05 kg", "Phù du", "https://source.unsplash.com/featured/?whitebait,fish", "Nhỏ, trong suốt, chuyên làm gỏi thấu.", System.currentTimeMillis(), 4.3f, 3000, false, null),
        FishEntity("sea_17", "Cá Bò Da", 180000.0, 180000, "Cá biển", "Rạn san hô", "2 kg", "Giáp xác", "https://source.unsplash.com/featured/?leatherjacketfish,fish", "Nướng muối ớt nguyên con, thịt dai như thịt gà.", System.currentTimeMillis(), 4.6f, 700, true, 162000.0),
        FishEntity("sea_18", "Cá Sơn Đá", 130000.0, 130000, "Cá biển", "Hốc đá", "0.3 kg", "Tôm nhỏ", "https://source.unsplash.com/featured/?cardinalfish,fish", "Thịt trắng, thơm, chiên giòn.", System.currentTimeMillis(), 4.2f, 500, false, null),
        FishEntity("sea_19", "Cá Mòi Dầu", 45000.0, 45000, "Cá biển", "Ven bờ", "0.1 kg", "Tảo", "https://source.unsplash.com/featured/?sardine,fish", "Kho cà chua, đóng hộp.", System.currentTimeMillis(), 3.9f, 8000, true, 35000.0),
        FishEntity("sea_20", "Cá Hồi Nauy (Phi lê)", 550000.0, 550000, "Cá biển", "Vùng lạnh", "8 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?salmon,fish", "Nhập khẩu tươi sống, giàu dinh dưỡng nhất.", System.currentTimeMillis(), 5.0f, 5000, false, null)
    ).withFishImages()

    // Cá Sông (20 loại)
    private fun getRiverFish(): List<FishEntity> = listOf(
        FishEntity("river_01", "Cá Lóc Đồng", 120000.0, 120000, "Cá sông", "Đầm lầy, ruộng", "3 kg", "Cá, ếch nhái", "https://source.unsplash.com/featured/?snakehead,fish", "Thịt chắc, ngọt, nướng trui hoặc nấu canh chua.", System.currentTimeMillis(), 4.8f, 5000, false, null),
        FishEntity("river_02", "Cá Trê Vàng", 90000.0, 90000, "Cá sông", "Bùn lầy", "1 kg", "Tạp ăn", "https://source.unsplash.com/featured/?catfish,river", "Thịt vàng ươm, kho gừng hoặc chiên mắm gừng.", System.currentTimeMillis(), 4.5f, 3200, true, 81000.0),
        FishEntity("river_03", "Cá Rô Đồng", 80000.0, 80000, "Cá sông", "Ruộng lúa", "0.3 kg", "Tạp ăn", "https://source.unsplash.com/featured/?perch,fish", "Xương cứng nhưng thịt rất thơm, kho tộ hoặc nấu canh cải.", System.currentTimeMillis(), 4.6f, 4100, false, null),
        FishEntity("river_04", "Cá Chép Giòn", 180000.0, 180000, "Cá sông", "Sông lớn", "5 kg", "Đậu tằm", "https://source.unsplash.com/featured/?carp,fish", "Lai tạo đặc biệt, thịt giòn sần sật, nhúng lẩu.", System.currentTimeMillis(), 4.7f, 1500, false, null),
        FishEntity("river_05", "Cá Trắm Cỏ", 70000.0, 70000, "Cá sông", "Hồ, sông", "10 kg", "Cỏ, rong", "https://source.unsplash.com/featured/?grasscarp,fish", "Cá to, thịt nạc, kho riềng.", System.currentTimeMillis(), 4.3f, 6000, true, 63000.0),
        FishEntity("river_06", "Cá Mè Hoa", 40000.0, 40000, "Cá sông", "Tầng mặt", "15 kg", "Phù du", "https://source.unsplash.com/featured/?silvercarp,fish", "Đầu cá nấu canh chua rất ngon, thân hơi nhiều xương.", System.currentTimeMillis(), 3.8f, 2000, false, null),
        FishEntity("river_07", "Cá Lăng Nha", 150000.0, 150000, "Cá sông", "Sông nước chảy", "8 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?bagridcatfish,fish", "Da trơn, thịt vàng, không xương dăm, chuyên nấu lẩu măng chua.", System.currentTimeMillis(), 4.9f, 2200, false, null),
        FishEntity("river_08", "Cá Tra Dầu", 90000.0, 90000, "Cá sông", "Sông Mekong", "300 kg", "Tạp ăn", "https://source.unsplash.com/featured/?pangasius,fish", "Thịt trắng, xuất khẩu, chiên xù hoặc kho tộ.", System.currentTimeMillis(), 4.2f, 8000, true, 80000.0),
        FishEntity("river_09", "Cá Basa", 60000.0, 60000, "Cá sông", "Sông", "5 kg", "Tạp ăn", "https://source.unsplash.com/featured/?basa,fish", "Nhiều mỡ, thịt mềm, dùng làm chả cá hoặc kho.", System.currentTimeMillis(), 4.4f, 9500, false, null),
        FishEntity("river_10", "Cá Heo Nước Ngọt", 250000.0, 250000, "Cá sông", "Sông Hậu", "0.1 kg", "Giáp xác", "https://source.unsplash.com/featured/?freshwaterfish,river", "Đặc sản miền Tây, da xanh, nướng muối ớt.", System.currentTimeMillis(), 4.8f, 600, false, null),
        FishEntity("river_11", "Cá Linh Non", 200000.0, 200000, "Cá sông", "Đồng lũ", "0.02 kg", "Tảo", "https://source.unsplash.com/featured/?mekongfish,small", "Đặc sản mùa nước nổi, ăn cả xương, nhúng lẩu bông điên điển.", System.currentTimeMillis(), 4.9f, 1000, false, null),
        FishEntity("river_12", "Cá Chạch Lấu", 400000.0, 400000, "Cá sông", "Đáy sông", "1 kg", "Tôm tép", "https://source.unsplash.com/featured/?spinyel,fish", "Nhân sâm dưới nước, thịt dai ngọt đặc biệt.", System.currentTimeMillis(), 5.0f, 400, true, 360000.0),
        FishEntity("river_13", "Cá Bống Tượng", 350000.0, 350000, "Cá sông", "Đáy bùn", "1.5 kg", "Tép", "https://source.unsplash.com/featured/?goby,fish", "Vua của các loài cá bống, thịt trắng như gà.", System.currentTimeMillis(), 4.9f, 550, false, null),
        FishEntity("river_14", "Cá Thát Lát Cườm", 140000.0, 140000, "Cá sông", "Hồ, sông", "1 kg", "Côn trùng", "https://source.unsplash.com/featured/?knifefish,fish", "Chuyên dùng để nạo làm chả cá dai ngon.", System.currentTimeMillis(), 4.5f, 3000, false, null),
        FishEntity("river_15", "Cá Hô", 500000.0, 500000, "Cá sông", "Sông sâu", "100 kg", "Thực vật", "https://source.unsplash.com/featured/?giantbarb,fish", "Loài cá khổng lồ của sông Mekong (Dạng nuôi bè).", System.currentTimeMillis(), 4.8f, 100, false, null),
        FishEntity("river_16", "Cá Chày Mắt Đỏ", 110000.0, 110000, "Cá sông", "Suối, sông", "2 kg", "Rong rêu", "https://source.unsplash.com/featured/?barb,fish", "Thịt thơm nhưng hơi nhiều xương dăm, nướng mọi.", System.currentTimeMillis(), 4.2f, 800, true, 99000.0),
        FishEntity("river_17", "Cá Ngạnh Sông", 130000.0, 130000, "Cá sông", "Đáy sông", "1 kg", "Tạp ăn", "https://source.unsplash.com/featured/?cranoglanis,fish", "Nấu canh chua cơm mẻ ngon tuyệt.", System.currentTimeMillis(), 4.4f, 1200, false, null),
        FishEntity("river_18", "Cá Diếc", 50000.0, 50000, "Cá sông", "Ao hồ", "0.3 kg", "Mùn bã", "https://source.unsplash.com/featured/?cruciancarp,fish", "Kho tương bần, xương mềm.", System.currentTimeMillis(), 4.0f, 2500, false, null),
        FishEntity("river_19", "Cá Rô Phi Đơn Tính", 45000.0, 45000, "Cá sông", "Nước ngọt", "2 kg", "Tạp ăn", "https://source.unsplash.com/featured/?tilapia,fish", "Thịt dày, ít xương, chiên xù.", System.currentTimeMillis(), 4.1f, 9000, true, 40000.0),
        FishEntity("river_20", "Cá Trôi Ấn Độ", 35000.0, 35000, "Cá sông", "Ao nuôi", "4 kg", "Thực vật", "https://source.unsplash.com/featured/?rohu,fish", "Giá rẻ, thịt ngọt, kho dưa.", System.currentTimeMillis(), 3.9f, 1800, false, null)
    ).withFishImages()

    // Cá Nước Lợ (20 loại)
    private fun getBrackishFish(): List<FishEntity> = listOf(
        FishEntity("brackish_01", "Cá Chẽm (Vược)", 160000.0, 160000, "Cá nước lợ", "Cửa sông", "20 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?barramundi,fish", "Thịt trắng phau, ít xương, hấp xì dầu số 1.", System.currentTimeMillis(), 4.8f, 4000, false, null),
        FishEntity("brackish_02", "Cá Kèo", 120000.0, 120000, "Cá nước lợ", "Bãi bùn", "0.05 kg", "Tảo", "https://source.unsplash.com/featured/?goby,mud", "Đặc sản miền Tây, lẩu cá kèo lá giang hoặc nướng muối ớt.", System.currentTimeMillis(), 4.6f, 5500, true, 108000.0),
        FishEntity("brackish_03", "Cá Đối Mục", 140000.0, 140000, "Cá nước lợ", "Cửa sông", "1 kg", "Mùn bã", "https://source.unsplash.com/featured/?mullet,fish", "Trứng cá đối rất béo, kho dưa cải.", System.currentTimeMillis(), 4.5f, 2300, false, null),
        FishEntity("brackish_04", "Cá Nâu", 220000.0, 220000, "Cá nước lợ", "Rừng ngập mặn", "0.5 kg", "Rong rêu", "https://source.unsplash.com/featured/?scat,fish", "Thịt ngọt thanh, nấu canh chua trái giác.", System.currentTimeMillis(), 4.7f, 1200, false, null),
        FishEntity("brackish_05", "Cá Dìa Công", 300000.0, 300000, "Cá nước lợ", "Đầm phá", "1 kg", "Tảo", "https://source.unsplash.com/featured/?spottedscat,fish", "Thịt thơm mùi tảo biển, hấp mồng tơi.", System.currentTimeMillis(), 4.9f, 450, false, null),
        FishEntity("brackish_06", "Cá Măng", 180000.0, 180000, "Cá nước lợ", "Đầm nuôi", "5 kg", "Tảo", "https://source.unsplash.com/featured/?milkfish,fish", "Cá quốc dân của Philippines, thịt trắng, chiên.", System.currentTimeMillis(), 4.4f, 1500, true, 160000.0),
        FishEntity("brackish_07", "Cá Bớp Nước Lợ", 280000.0, 280000, "Cá nước lợ", "Lồng bè", "15 kg", "Cá tạp", "https://source.unsplash.com/featured/?cobia,brackish", "Nuôi lồng bè cửa biển, thịt béo.", System.currentTimeMillis(), 4.7f, 900, false, null),
        FishEntity("brackish_08", "Cá Chim Vây Vàng", 150000.0, 150000, "Cá nước lợ", "Đầm nước lợ", "2 kg", "Giáp xác", "https://source.unsplash.com/featured/?pompano,fish", "Vây vàng óng, thịt mềm, hấp gừng.", System.currentTimeMillis(), 4.5f, 3000, false, null),
        FishEntity("brackish_09", "Cá Chạch Lấu (Nuôi)", 380000.0, 380000, "Cá nước lợ", "Ao đất", "0.8 kg", "Tôm", "https://source.unsplash.com/featured/?eel,fish", "Nuôi trong môi trường lợ nhẹ, thịt vẫn rất dai ngon.", System.currentTimeMillis(), 4.8f, 600, false, null),
        FishEntity("brackish_10", "Cá Bống Mú Trân Châu", 320000.0, 320000, "Cá nước lợ", "Lồng bè", "5 kg", "Cá con", "https://source.unsplash.com/featured/?grouper,spotted", "Da có đốm như ngọc trai, thịt rất ngọt.", System.currentTimeMillis(), 4.9f, 750, true, 288000.0),
        FishEntity("brackish_11", "Cá Đù Sóc", 90000.0, 90000, "Cá nước lợ", "Cửa sông", "0.3 kg", "Giáp xác", "https://source.unsplash.com/featured/?croaker,fish", "Thường phơi một nắng chiên giòn.", System.currentTimeMillis(), 4.2f, 4000, false, null),
        FishEntity("brackish_12", "Cá Khoai", 200000.0, 200000, "Cá nước lợ", "Cửa biển", "0.2 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?bombayduck,fish", "Thân mềm như cháo, nấu lẩu rau tần ô.", System.currentTimeMillis(), 4.6f, 1800, false, null),
        FishEntity("brackish_13", "Cá Dứa Cần Giờ", 350000.0, 350000, "Cá nước lợ", "Rừng sác", "5 kg", "Trái mắm", "https://source.unsplash.com/featured/?catfish,brackish", "Đặc sản nổi tiếng, kho tộ hoặc phơi một nắng.", System.currentTimeMillis(), 5.0f, 1200, false, null),
        FishEntity("brackish_14", "Cá Bè Trang", 170000.0, 170000, "Cá nước lợ", "Cửa vịnh", "4 kg", "Cá nhỏ", "https://source.unsplash.com/featured/?queenfish,fish", "Thịt dai, nạo làm chả hoặc chiên tươi.", System.currentTimeMillis(), 4.3f, 1100, false, null),
        FishEntity("brackish_15", "Cá Sủ Đất", 250000.0, 250000, "Cá nước lợ", "Đáy bùn", "10 kg", "Tôm", "https://source.unsplash.com/featured/?croaker,giant", "Bong bóng cá sủ rất quý, thịt ngọt.", System.currentTimeMillis(), 4.7f, 300, false, null),
        FishEntity("brackish_16", "Cá Hồng Mỹ", 140000.0, 140000, "Cá nước lợ", "Ao đầm", "3 kg", "Cua ốc", "https://source.unsplash.com/featured/?reddrum,fish", "Dễ nuôi, thịt đỏ hồng, nấu ngót.", System.currentTimeMillis(), 4.4f, 2000, true, 120000.0),
        FishEntity("brackish_17", "Cá Dìa Bông", 280000.0, 280000, "Cá nước lợ", "Đầm phá", "1 kg", "Tảo", "https://source.unsplash.com/featured/?rabbitfish,spotted", "Da có hoa văn bông, thịt thơm hơn dìa thường.", System.currentTimeMillis(), 4.8f, 500, false, null),
        FishEntity("brackish_18", "Cá Kình", 200000.0, 200000, "Cá nước lợ", "Đầm phá", "0.2 kg", "Rong", "https://source.unsplash.com/featured/?rabbitfish,gold", "Thường dùng để đổ bánh khoái cá kình ở Huế.", System.currentTimeMillis(), 4.5f, 900, false, null),
        FishEntity("brackish_19", "Cá Bống Dừa", 110000.0, 110000, "Cá nước lợ", "Rạch dừa", "0.1 kg", "Tạp ăn", "https://source.unsplash.com/featured/?goby,coconut", "Kho tiêu nước dừa sệt sệt.", System.currentTimeMillis(), 4.3f, 2500, false, null),
        FishEntity("brackish_20", "Cá Bống Sao", 130000.0, 130000, "Cá nước lợ", "Bãi bồi", "0.05 kg", "Mùn bã", "https://source.unsplash.com/featured/?mudskipper,fish", "Da có đốm sao xanh, kho rau răm.", System.currentTimeMillis(), 4.4f, 1800, false, null)
    ).withFishImages()

    // Cá Cảnh (20 loại) - Prices per fish, not per kg
    private fun getAquariumFish(): List<FishEntity> = listOf(
        FishEntity("pet_01", "Cá Rồng Huyết Long", 25000000.0, 25000000, "Cá cảnh", "Bể lớn", "60 cm", "Côn trùng, nhái", "https://source.unsplash.com/featured/?arowana,red", "Vua của các loài cá cảnh, mang lại tài lộc, màu đỏ huyết tuyệt đẹp.", System.currentTimeMillis(), 5.0f, 50, false, null),
        FishEntity("pet_02", "Cá Koi Kohaku", 5000000.0, 5000000, "Cá cảnh", "Hồ sân vườn", "80 cm", "Cám Koi", "https://source.unsplash.com/featured/?koi,fish", "Dòng Koi cơ bản trắng đỏ, nhập khẩu Nhật Bản.", System.currentTimeMillis(), 4.9f, 200, true, 4500000.0),
        FishEntity("pet_03", "Cá Betta Halfmoon", 80000.0, 80000, "Cá cảnh", "Hũ nhỏ", "5 cm", "Trùng chỉ", "https://source.unsplash.com/featured/?betta,fish", "Đuôi xòe 180 độ như nửa vầng trăng, rất hung dữ.", System.currentTimeMillis(), 4.6f, 5000, false, null),
        FishEntity("pet_04", "Cá Hề Nemo", 150000.0, 150000, "Cá cảnh", "Bể nước mặn", "8 cm", "Tép", "https://source.unsplash.com/featured/?clownfish,fish", "Cá biển cảnh nổi tiếng, bơi cùng hải quỳ.", System.currentTimeMillis(), 4.8f, 3000, false, null),
        FishEntity("pet_05", "Cá La Hán King Kamfa", 3000000.0, 3000000, "Cá cảnh", "Bể kính", "30 cm", "Tôm đông lạnh", "https://source.unsplash.com/featured/?flowerhorn,fish", "Đầu gù to, châu sáng lấp lánh, rất khôn.", System.currentTimeMillis(), 4.7f, 100, false, null),
        FishEntity("pet_06", "Cá Dĩa (Discus)", 400000.0, 400000, "Cá cảnh", "Bể thủy sinh", "15 cm", "Tim bò", "https://source.unsplash.com/featured/?discus,fish", "Được mệnh danh là nhất đại mỹ ngư, thân tròn dẹt.", System.currentTimeMillis(), 4.8f, 800, true, 350000.0),
        FishEntity("pet_07", "Cá Bảy Màu Rồng Đỏ", 20000.0, 20000, "Cá cảnh", "Bể nhỏ", "3 cm", "Cám mịn", "https://source.unsplash.com/featured/?guppy,fish", "Dễ nuôi, sinh sản nhanh, màu sắc rực rỡ.", System.currentTimeMillis(), 4.5f, 10000, false, null),
        FishEntity("pet_08", "Cá Ba Đuôi Đầu Lân", 60000.0, 60000, "Cá cảnh", "Bể kính", "15 cm", "Cám viên", "https://source.unsplash.com/featured/?goldfish,fish", "Đầu có bướu như lân, bơi uyển chuyển.", System.currentTimeMillis(), 4.4f, 4000, false, null),
        FishEntity("pet_09", "Cá Neon Vua", 15000.0, 15000, "Cá cảnh", "Bể thủy sinh", "2 cm", "Cám mịn", "https://source.unsplash.com/featured/?neontetra,fish", "Phát sáng lấp lánh dưới đèn, bơi theo đàn.", System.currentTimeMillis(), 4.6f, 8000, false, null),
        FishEntity("pet_10", "Cá Phượng Hoàng Lam", 50000.0, 50000, "Cá cảnh", "Bể thủy sinh", "4 cm", "Trùng chỉ", "https://source.unsplash.com/featured/?ramcichlid,fish", "Màu xanh lam óng ánh, tính hiền lành.", System.currentTimeMillis(), 4.5f, 2000, false, null),
        FishEntity("pet_11", "Cá Ông Tiên", 40000.0, 40000, "Cá cảnh", "Bể cao", "10 cm", "Cám", "https://source.unsplash.com/featured/?angelfish,freshwater", "Dáng bơi khoan thai, vây dài thướt tha.", System.currentTimeMillis(), 4.4f, 3000, false, null),
        FishEntity("pet_12", "Cá Hồng Két", 120000.0, 120000, "Cá cảnh", "Bể lớn", "20 cm", "Cám", "https://source.unsplash.com/featured/?parrotfish,blood", "Màu đỏ rực, mỏ két, thường nuôi chung cá rồng.", System.currentTimeMillis(), 4.6f, 1500, true, 100000.0),
        FishEntity("pet_13", "Cá Thần Tiên Ai Cập", 500000.0, 500000, "Cá cảnh", "Bể Biotop", "25 cm", "Côn trùng", "https://source.unsplash.com/featured/?altumangelfish,fish", "Loài cá thần tiên hoang dã, kích thước lớn, rất sang trọng.", System.currentTimeMillis(), 4.9f, 200, false, null),
        FishEntity("pet_14", "Cá Ali Thái", 35000.0, 35000, "Cá cảnh", "Bể đá", "12 cm", "Cám", "https://source.unsplash.com/featured/?cichlid,fish", "Màu sắc sặc sỡ, thích hợp bể xếp đá.", System.currentTimeMillis(), 4.3f, 1800, false, null),
        FishEntity("pet_15", "Cá Sọc Ngựa Dạ Quang", 5000.0, 5000, "Cá cảnh", "Bể nhỏ", "3 cm", "Cám", "https://source.unsplash.com/featured/?danio,fish", "Biến đổi gen phát sáng, cực khỏe.", System.currentTimeMillis(), 4.2f, 12000, false, null),
        FishEntity("pet_16", "Cá Bình Tích", 10000.0, 10000, "Cá cảnh", "Hồ ngoài trời", "5 cm", "Rêu hại", "https://source.unsplash.com/featured/?molly,fish", "Bụng bự, dễ sinh sản, dọn rêu tốt.", System.currentTimeMillis(), 4.1f, 5000, false, null),
        FishEntity("pet_17", "Cá Mún Đỏ", 8000.0, 8000, "Cá cảnh", "Hồ sen", "4 cm", "Lăng quăng", "https://source.unsplash.com/featured/?platy,fish", "Màu đỏ cam, rất khỏe, không cần sủi oxy.", System.currentTimeMillis(), 4.2f, 6000, false, null),
        FishEntity("pet_18", "Cá Kiếm", 10000.0, 10000, "Cá cảnh", "Bể kính", "6 cm", "Cám", "https://source.unsplash.com/featured/?swordtail,fish", "Đuôi có kiếm dài (con đực), màu đỏ đẹp.", System.currentTimeMillis(), 4.3f, 4000, false, null),
        FishEntity("pet_19", "Cá Pleco (Lau kiếng)", 20000.0, 20000, "Cá cảnh", "Đáy bể", "30 cm", "Rêu", "https://source.unsplash.com/featured/?pleco,fish", "Chuyên dọn bể, ăn rêu và thức ăn thừa.", System.currentTimeMillis(), 4.0f, 3500, false, null),
        FishEntity("pet_20", "Cá Chuột Panda", 25000.0, 25000, "Cá cảnh", "Đáy bể", "4 cm", "Cám chìm", "https://source.unsplash.com/featured/?corydoras,panda", "Dễ thương như gấu trúc, dọn thức ăn đáy.", System.currentTimeMillis(), 4.7f, 2200, false, null)
    ).withFishImages()

    private fun List<FishEntity>.withFishImages(): List<FishEntity> {
        return map { fish ->
            val keyword = fish.id.replace("_", "")
            val imageUrl = "https://source.unsplash.com/800x600/?fish,$keyword"
            fish.copy(imageUrl = imageUrl)
        }
    }
}