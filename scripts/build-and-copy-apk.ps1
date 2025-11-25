# Script tu dong build APK va copy vao thu muc release
# Usage: .\scripts\build-and-copy-apk.ps1

param(
    [switch]$SkipBuild = $false
)

Write-Host ""
Write-Host "Script tu dong build APK AquaLife" -ForegroundColor Cyan
Write-Host ("=" * 50) -ForegroundColor Gray

# Thiet lap Java tu Android Studio neu chua co
if (-not $env:JAVA_HOME -or (Test-Path "C:\Program Files\Android\Android Studio\jbr\bin\java.exe")) {
    $androidStudioJava = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path "$androidStudioJava\bin\java.exe") {
        $env:JAVA_HOME = $androidStudioJava
        $env:PATH = "$androidStudioJava\bin;$env:PATH"
        Write-Host "[OK] Da thiet lap Java tu Android Studio" -ForegroundColor Green
    }
}

# Tao thu muc apk-release neu chua co
if (-not (Test-Path "apk-release")) {
    New-Item -ItemType Directory -Path "apk-release" | Out-Null
    Write-Host "[OK] Da tao thu muc apk-release" -ForegroundColor Green
}

# Buoc 1: Build APK
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "Buoc 1: Building APK..." -ForegroundColor Yellow
    Write-Host "   Cau hinh: minSdk=24, targetSdk=34" -ForegroundColor Gray
    Write-Host "   Dang build, vui long doi..." -ForegroundColor Gray
    Write-Host ""
    
    $buildResult = ./gradlew :app:assembleDebug --no-daemon 2>&1
    
    # Kiem tra ket qua build
    if ($LASTEXITCODE -eq 0 -or $buildResult -match "BUILD SUCCESSFUL") {
        Write-Host "[OK] Build thanh cong!" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Build that bai!" -ForegroundColor Red
        $buildResult | Select-String -Pattern "BUILD|FAILED|error|Error" | Select-Object -Last 10
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "[SKIP] Bo qua buoc build (SkipBuild=true)" -ForegroundColor Yellow
}

# Buoc 2: Kiem tra APK da build
$sourceApk = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $sourceApk)) {
    Write-Host ""
    Write-Host "[ERROR] Khong tim thay APK tai: $sourceApk" -ForegroundColor Red
    Write-Host "   Vui long chay build truoc: ./gradlew :app:assembleDebug" -ForegroundColor Yellow
    exit 1
}

# Buoc 3: Lay thong tin APK
$apkFile = Get-Item $sourceApk
$apkSize = [math]::Round($apkFile.Length / 1MB, 2)
$apkDate = $apkFile.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")

Write-Host ""
Write-Host "Thong tin APK:" -ForegroundColor Cyan
Write-Host "   Duong dan: $sourceApk" -ForegroundColor White
Write-Host "   Kich thuoc: $apkSize MB" -ForegroundColor White
Write-Host "   Ngay tao: $apkDate" -ForegroundColor White

# Buoc 4: Copy APK vao thu muc release
Write-Host ""
Write-Host "Buoc 2: Copying APK to apk-release folder..." -ForegroundColor Yellow
$destApk = "apk-release\app-debug.apk"

try {
    Copy-Item $sourceApk $destApk -Force
    Write-Host "[OK] Da copy APK thanh cong!" -ForegroundColor Green
    Write-Host "   Dich: $destApk" -ForegroundColor White
    
    # Kiem tra file da copy
    $copiedFile = Get-Item $destApk
    Write-Host ""
    Write-Host "APK trong apk-release:" -ForegroundColor Cyan
    Write-Host "   Kich thuoc: $([math]::Round($copiedFile.Length / 1MB, 2)) MB" -ForegroundColor White
    Write-Host "   Ngay: $($copiedFile.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor White
    
} catch {
    Write-Host "[ERROR] Loi khi copy APK: $_" -ForegroundColor Red
    exit 1
}

# Buoc 5: Tom tat
Write-Host ""
Write-Host ("=" * 50) -ForegroundColor Gray
Write-Host "[OK] Hoan thanh!" -ForegroundColor Green
Write-Host ""
Write-Host "Buoc tiep theo:" -ForegroundColor Cyan
Write-Host "   1. Upload APK len Google Drive/GitHub" -ForegroundColor White
Write-Host "   2. Lay link download truc tiep" -ForegroundColor White
Write-Host "   3. Tao QR code voi script:" -ForegroundColor White
Write-Host "      .\scripts\create-apk-qr-with-url.ps1 -DownloadUrl 'YOUR_LINK'" -ForegroundColor Yellow
Write-Host ""
Write-Host "File APK: apk-release\app-debug.apk" -ForegroundColor Green
