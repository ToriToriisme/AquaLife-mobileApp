# Script to verify APK file integrity
# Usage: .\scripts\verify-apk.ps1 -ApkPath "path\to\app.apk"

param(
    [Parameter(Mandatory=$false)]
    [string]$ApkPath = "apk-release\app-debug.apk"
)

Write-Host "🔍 Verifying APK file..." -ForegroundColor Cyan
Write-Host "Path: $ApkPath`n" -ForegroundColor Yellow

if (-not (Test-Path $ApkPath)) {
    Write-Host "❌ ERROR: APK file not found at: $ApkPath" -ForegroundColor Red
    exit 1
}

$file = Get-Item $ApkPath
Write-Host "File Information:" -ForegroundColor Cyan
Write-Host "  Name: $($file.Name)"
Write-Host "  Size: $([math]::Round($file.Length/1MB, 2)) MB"
Write-Host "  Date: $($file.LastWriteTime)"
Write-Host ""

# Check ZIP header
Write-Host "Checking APK format..." -ForegroundColor Cyan
$header = [System.IO.File]::ReadAllBytes($apkPath)[0..3]
$isZip = ($header[0] -eq 0x50 -and $header[1] -eq 0x4B -and $header[2] -eq 0x03 -and $header[3] -eq 0x04)

if ($isZip) {
    Write-Host "  ✅ ZIP header: Valid" -ForegroundColor Green
} else {
    Write-Host "  ❌ ZIP header: INVALID!" -ForegroundColor Red
    Write-Host "  This file is not a valid APK/ZIP file." -ForegroundColor Red
    exit 1
}

# Check APK structure
Write-Host "`nChecking APK structure..." -ForegroundColor Cyan
Add-Type -AssemblyName System.IO.Compression.FileSystem

try {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($apkPath)
    
    # Check AndroidManifest.xml
    $manifest = $zip.Entries | Where-Object { $_.FullName -eq "AndroidManifest.xml" }
    if ($manifest) {
        Write-Host "  ✅ AndroidManifest.xml: Found" -ForegroundColor Green
    } else {
        Write-Host "  ❌ AndroidManifest.xml: NOT FOUND!" -ForegroundColor Red
    }
    
    # Check DEX files
    $dexFiles = $zip.Entries | Where-Object { $_.FullName -like "classes*.dex" }
    if ($dexFiles.Count -gt 0) {
        Write-Host "  ✅ DEX files: Found $($dexFiles.Count) file(s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ DEX files: NOT FOUND!" -ForegroundColor Red
    }
    
    # Check resources.arsc
    $resources = $zip.Entries | Where-Object { $_.FullName -eq "resources.arsc" }
    if ($resources) {
        Write-Host "  ✅ resources.arsc: Found" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  resources.arsc: Not found (may be in AAB format)" -ForegroundColor Yellow
    }
    
    # Check META-INF (signing)
    $metaInf = $zip.Entries | Where-Object { $_.FullName -like "META-INF/*" }
    if ($metaInf.Count -gt 0) {
        Write-Host "  ✅ META-INF: Found $($metaInf.Count) file(s) (APK is signed)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  META-INF: Not found (APK may be unsigned)" -ForegroundColor Yellow
    }
    
    # Check res folder
    $resFiles = $zip.Entries | Where-Object { $_.FullName -like "res/*" }
    if ($resFiles.Count -gt 0) {
        Write-Host "  ✅ Resources: Found $($resFiles.Count) file(s)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Resources: Not found" -ForegroundColor Yellow
    }
    
    $zip.Dispose()
    
    Write-Host "`n✅ APK structure verification complete!" -ForegroundColor Green
    Write-Host "`n📱 Installation Tips:" -ForegroundColor Cyan
    Write-Host "  1. Ensure 'Install from Unknown Sources' is enabled" -ForegroundColor White
    Write-Host "  2. Try installing via ADB: adb install $ApkPath" -ForegroundColor White
    Write-Host "  3. Check device storage space (APK is $([math]::Round($file.Length/1MB, 2)) MB)" -ForegroundColor White
    Write-Host "  4. Ensure Android version >= 10 (API 29)" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ ERROR reading APK: $_" -ForegroundColor Red
    Write-Host "The APK file may be corrupted." -ForegroundColor Red
    exit 1
}


