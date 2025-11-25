# Script to generate QR code for APK file
# Usage: .\scripts\generate-apk-qr.ps1

param(
    [string]$ApkPath = "",
    [string]$OutputDir = "apk-release"
)

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# Find APK file
if ([string]::IsNullOrEmpty($ApkPath)) {
    $apkFiles = @(
        "app\build\outputs\apk\debug\app-debug.apk",
        "app\build\outputs\apk\release\app-release.apk"
    )
    
    foreach ($path in $apkFiles) {
        if (Test-Path $path) {
            $ApkPath = $path
            break
        }
    }
    
    # If not found, search recursively
    if ([string]::IsNullOrEmpty($ApkPath)) {
        $found = Get-ChildItem -Path "app\build" -Recurse -Filter "*.apk" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $ApkPath = $found.FullName
        }
    }
}

if ([string]::IsNullOrEmpty($ApkPath) -or -not (Test-Path $ApkPath)) {
    Write-Host "ERROR: APK file not found. Please build the APK first:" -ForegroundColor Red
    Write-Host "  ./gradlew :app:assembleDebug" -ForegroundColor Yellow
    exit 1
}

Write-Host "Found APK: $ApkPath" -ForegroundColor Green

# Get APK file info
$apkFile = Get-Item $ApkPath
$apkName = $apkFile.Name
$apkSize = [math]::Round($apkFile.Length / 1MB, 2)
$apkDate = $apkFile.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")

Write-Host "APK Name: $apkName" -ForegroundColor Cyan
Write-Host "APK Size: $apkSize MB" -ForegroundColor Cyan
Write-Host "Build Date: $apkDate" -ForegroundColor Cyan

# Copy APK to output directory
$outputApkPath = Join-Path $OutputDir $apkName
Copy-Item $ApkPath $outputApkPath -Force
Write-Host "Copied APK to: $outputApkPath" -ForegroundColor Green

# Generate QR code using online API
# Note: For production, you should upload APK to a hosting service (Google Drive, GitHub Releases, etc.)
# and use that URL in the QR code

# Option 1: QR code with file path (for local sharing)
$localPath = Resolve-Path $outputApkPath
$qrDataLocal = "file:///$($localPath -replace '\\', '/')"

# Option 2: QR code with instructions (recommended)
$instructions = @"
AquaLife Mobile App APK
File: $apkName
Size: $apkSize MB
Build Date: $apkDate

To install:
1. Transfer this APK to your Android device
2. Enable "Install from Unknown Sources" in Settings
3. Open the APK file and install

Or scan this QR code after uploading to:
- Google Drive (share link)
- GitHub Releases
- Firebase Hosting
- Any file hosting service
"@

# Generate QR code URL using qrserver.com API
$encodedInstructions = [System.Web.HttpUtility]::UrlEncode($instructions)
$qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=$encodedInstructions"

# Create HTML file with QR code
$htmlContent = @"
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AquaLife APK - QR Code</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            text-align: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
            color: #333;
        }
        h1 {
            color: #667eea;
            margin-bottom: 10px;
        }
        .info {
            background: #f5f5f5;
            padding: 15px;
            border-radius: 10px;
            margin: 20px 0;
            text-align: left;
        }
        .info-item {
            margin: 8px 0;
            font-size: 14px;
        }
        .info-label {
            font-weight: bold;
            color: #667eea;
        }
        .qr-code {
            margin: 30px 0;
            padding: 20px;
            background: white;
            border-radius: 10px;
        }
        .qr-code img {
            max-width: 100%;
            height: auto;
            border: 3px solid #667eea;
            border-radius: 10px;
        }
        .instructions {
            background: #fff3cd;
            border: 1px solid #ffc107;
            padding: 15px;
            border-radius: 10px;
            margin: 20px 0;
            text-align: left;
            font-size: 14px;
        }
        .instructions h3 {
            margin-top: 0;
            color: #856404;
        }
        .instructions ol {
            margin: 10px 0;
            padding-left: 20px;
        }
        .instructions li {
            margin: 5px 0;
        }
        .download-btn {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 15px 30px;
            text-decoration: none;
            border-radius: 25px;
            font-weight: bold;
            margin: 20px 0;
            transition: background 0.3s;
        }
        .download-btn:hover {
            background: #764ba2;
        }
        .note {
            background: #d1ecf1;
            border: 1px solid #bee5eb;
            padding: 15px;
            border-radius: 10px;
            margin: 20px 0;
            font-size: 13px;
            color: #0c5460;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🐟 AquaLife Mobile App</h1>
        <p style="color: #666; margin-bottom: 30px;">Ứng dụng mua bán cá trực tuyến</p>
        
        <div class="info">
            <div class="info-item">
                <span class="info-label">Tên file:</span> $apkName
            </div>
            <div class="info-item">
                <span class="info-label">Kích thước:</span> $apkSize MB
            </div>
            <div class="info-item">
                <span class="info-label">Ngày build:</span> $apkDate
            </div>
        </div>
        
        <div class="qr-code">
            <h3 style="color: #667eea; margin-bottom: 15px;">📱 QR Code để tải APK</h3>
            <img src="$qrCodeUrl" alt="QR Code for APK" />
            <p style="color: #666; margin-top: 15px; font-size: 12px;">
                Quét mã QR này để xem thông tin APK<br/>
                (Cần upload APK lên hosting để tải trực tiếp)
            </p>
        </div>
        
        <a href="$apkName" class="download-btn" download="$apkName">
            ⬇️ Tải APK ngay
        </a>
        
        <div class="instructions">
            <h3>📋 Hướng dẫn cài đặt:</h3>
            <ol>
                <li>Tải file APK về thiết bị Android</li>
                <li>Vào <strong>Cài đặt → Bảo mật</strong></li>
                <li>Bật <strong>"Cho phép cài đặt từ nguồn không xác định"</strong></li>
                <li>Mở file APK đã tải và nhấn <strong>"Cài đặt"</strong></li>
                <li>Chờ quá trình cài đặt hoàn tất</li>
            </ol>
        </div>
        
        <div class="note">
            <strong>💡 Lưu ý:</strong><br/>
            Để chia sẻ APK qua QR code, bạn cần upload file lên một dịch vụ hosting như:
            <ul style="text-align: left; margin: 10px 0;">
                <li>Google Drive (chia sẻ công khai)</li>
                <li>GitHub Releases</li>
                <li>Firebase Hosting</li>
                <li>Dropbox hoặc các dịch vụ tương tự</li>
            </ul>
            Sau đó thay thế URL trong QR code bằng link download trực tiếp.
        </div>
    </div>
</body>
</html>
"@

$htmlPath = Join-Path $OutputDir "apk-qr-code.html"
$htmlContent | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "Created HTML file: $htmlPath" -ForegroundColor Green

# Also create a simple text file with instructions
$textContent = @"
========================================
AquaLife Mobile App - APK Information
========================================

File Name: $apkName
File Size: $apkSize MB
Build Date: $apkDate
Location: $outputApkPath

========================================
Hướng dẫn chia sẻ APK qua QR Code:
========================================

1. Upload APK lên một dịch vụ hosting:
   - Google Drive: Upload file, chia sẻ công khai, lấy link
   - GitHub Releases: Tạo release mới và upload APK
   - Firebase Hosting: Upload và lấy URL
   - Dropbox: Upload và lấy link chia sẻ

2. Tạo QR code từ link download:
   - Truy cập: https://www.qr-code-generator.com/
   - Dán link download APK
   - Tải QR code về

3. Hoặc sử dụng script này với URL:
   - Sửa biến `$downloadUrl` trong script
   - Chạy lại script để tạo QR code mới

========================================
QR Code API (để tạo QR từ URL):
========================================

https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=YOUR_URL_HERE

Thay YOUR_URL_HERE bằng link download APK của bạn.

========================================
"@

$textPath = Join-Path $OutputDir "APK_INFO.txt"
$textContent | Out-File -FilePath $textPath -Encoding UTF8
Write-Host "Created info file: $textPath" -ForegroundColor Green

Write-Host "`n✅ Hoàn tất!" -ForegroundColor Green
Write-Host "📁 Tất cả files đã được tạo trong thư mục: $OutputDir" -ForegroundColor Cyan
Write-Host "`n📱 Để chia sẻ APK:" -ForegroundColor Yellow
Write-Host "   1. Mở file: $htmlPath" -ForegroundColor White
Write-Host "   2. Upload APK lên Google Drive/GitHub và lấy link" -ForegroundColor White
Write-Host "   3. Tạo QR code từ link đó tại: https://www.qr-code-generator.com/" -ForegroundColor White

