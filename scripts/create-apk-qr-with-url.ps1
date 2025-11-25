# Script to create QR code for APK download URL
# Usage: .\scripts\create-apk-qr-with-url.ps1 -DownloadUrl "https://your-link.com/app.apk"

param(
    [Parameter(Mandatory=$true)]
    [string]$DownloadUrl,
    
    [string]$OutputDir = "apk-release"
)

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

Write-Host "Creating QR code for APK download URL..." -ForegroundColor Cyan
Write-Host "URL: $DownloadUrl" -ForegroundColor Yellow

# Generate QR code URL
$encodedUrl = [System.Web.HttpUtility]::UrlEncode($DownloadUrl)
$qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=$encodedUrl"

# Create HTML file with QR code
$htmlContent = @"
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AquaLife APK - Download QR Code</title>
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            text-align: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: white;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
            color: #333;
        }
        h1 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 32px;
        }
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 16px;
        }
        .qr-code {
            margin: 30px 0;
            padding: 30px;
            background: #f8f9fa;
            border-radius: 15px;
        }
        .qr-code img {
            max-width: 100%;
            height: auto;
            border: 4px solid #667eea;
            border-radius: 15px;
            padding: 10px;
            background: white;
        }
        .download-url {
            background: #e9ecef;
            padding: 15px;
            border-radius: 10px;
            margin: 20px 0;
            word-break: break-all;
            font-size: 12px;
            color: #495057;
        }
        .download-btn {
            display: inline-block;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 18px 40px;
            text-decoration: none;
            border-radius: 30px;
            font-weight: bold;
            margin: 20px 0;
            font-size: 16px;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }
        .download-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }
        .instructions {
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 20px;
            border-radius: 10px;
            margin: 30px 0;
            text-align: left;
            font-size: 14px;
        }
        .instructions h3 {
            margin-top: 0;
            color: #856404;
            font-size: 18px;
        }
        .instructions ol {
            margin: 15px 0;
            padding-left: 25px;
        }
        .instructions li {
            margin: 10px 0;
            line-height: 1.6;
        }
        .info-box {
            background: #d1ecf1;
            border-left: 4px solid #0c5460;
            padding: 15px;
            border-radius: 10px;
            margin: 20px 0;
            text-align: left;
            font-size: 13px;
            color: #0c5460;
        }
        .qr-instruction {
            background: #e7f3ff;
            border: 2px dashed #667eea;
            padding: 20px;
            border-radius: 10px;
            margin: 20px 0;
            color: #004085;
        }
        .qr-instruction h3 {
            margin-top: 0;
            color: #667eea;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🐟 AquaLife Mobile App</h1>
        <p class="subtitle">Ứng dụng mua bán cá trực tuyến</p>
        
        <div class="qr-instruction">
            <h3>📱 Quét mã QR để tải APK</h3>
            <p>Mở camera trên điện thoại và quét mã QR bên dưới để tải ứng dụng</p>
        </div>
        
        <div class="qr-code">
            <img src="$qrCodeUrl" alt="QR Code for APK Download" />
        </div>
        
        <div class="download-url">
            <strong>Link tải:</strong><br/>
            <a href="$DownloadUrl" target="_blank" style="color: #667eea;">$DownloadUrl</a>
        </div>
        
        <a href="$DownloadUrl" class="download-btn" target="_blank">
            ⬇️ Tải APK ngay
        </a>
        
        <div class="instructions">
            <h3>📋 Hướng dẫn cài đặt:</h3>
            <ol>
                <li><strong>Quét mã QR</strong> hoặc nhấn nút "Tải APK ngay" ở trên</li>
                <li>Chờ file APK tải về hoàn tất</li>
                <li>Mở file APK đã tải (thường ở thư mục Downloads)</li>
                <li>Nếu có cảnh báo "Cài đặt từ nguồn không xác định":
                    <ul style="margin-top: 5px;">
                        <li>Vào <strong>Cài đặt → Bảo mật</strong></li>
                        <li>Bật <strong>"Cho phép cài đặt từ nguồn không xác định"</strong></li>
                        <li>Quay lại và nhấn <strong>"Cài đặt"</strong></li>
                    </ul>
                </li>
                <li>Chờ quá trình cài đặt hoàn tất và mở ứng dụng</li>
            </ol>
        </div>
        
        <div class="info-box">
            <strong>💡 Lưu ý:</strong><br/>
            • Đảm bảo thiết bị của bạn có kết nối Internet để tải APK<br/>
            • File APK chỉ dành cho thiết bị Android<br/>
            • Nếu gặp lỗi, vui lòng kiểm tra lại cài đặt bảo mật trên thiết bị
        </div>
    </div>
</body>
</html>
"@

$htmlPath = Join-Path $OutputDir "apk-download-qr.html"
$htmlContent | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "✅ Created QR code HTML: $htmlPath" -ForegroundColor Green

# Also save QR code image directly
$qrImagePath = Join-Path $OutputDir "apk-qr-code.png"
try {
    Invoke-WebRequest -Uri $qrCodeUrl -OutFile $qrImagePath
    Write-Host "✅ Downloaded QR code image: $qrImagePath" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Could not download QR image, but HTML file contains the QR code" -ForegroundColor Yellow
}

Write-Host "`n✅ Hoàn tất!" -ForegroundColor Green
Write-Host "📁 Files đã được tạo trong: $OutputDir" -ForegroundColor Cyan
Write-Host "`n📱 Cách sử dụng:" -ForegroundColor Yellow
Write-Host "   1. Mở file: $htmlPath" -ForegroundColor White
Write-Host "   2. In hoặc hiển thị QR code trên màn hình" -ForegroundColor White
Write-Host "   3. Thầy/cô có thể quét mã QR bằng điện thoại để tải APK" -ForegroundColor White

