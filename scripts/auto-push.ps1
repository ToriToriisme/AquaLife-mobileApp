# Script tu dong push code len GitHub
# Usage: .\scripts\auto-push.ps1

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  AUTO PUSH TO GITHUB" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Kiem tra git status
Write-Host "[1/4] Kiem tra git status..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "   Co thay doi chua commit, dang add..." -ForegroundColor Gray
    git add .
    $commitMsg = "Update: Auto commit from script - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    git commit -m $commitMsg
    Write-Host "   [OK] Da commit: $commitMsg" -ForegroundColor Green
} else {
    Write-Host "   [OK] Khong co thay doi" -ForegroundColor Green
}

# Kiem tra branch
Write-Host "`n[2/4] Kiem tra branch..." -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host "   Current branch: $currentBranch" -ForegroundColor White

# Kiem tra remote
Write-Host "`n[3/4] Kiem tra remote..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin
Write-Host "   Remote: $remoteUrl" -ForegroundColor White

# Push len GitHub
Write-Host "`n[4/4] Dang push len GitHub..." -ForegroundColor Yellow
Write-Host "   Branch: $currentBranch" -ForegroundColor Gray
Write-Host "   Dang push, vui long doi..." -ForegroundColor Gray

try {
    $pushResult = git push origin $currentBranch 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[OK] Push thanh cong!" -ForegroundColor Green
        Write-Host "   Code da duoc push len: $remoteUrl" -ForegroundColor White
        
        # Kiem tra lai
        Start-Sleep -Seconds 2
        $remoteCommit = git ls-remote --heads origin $currentBranch | ForEach-Object { $_.Split()[0] }
        $localCommit = git log --oneline -1 --format="%H"
        
        if ($remoteCommit -eq $localCommit) {
            Write-Host "   [OK] Remote da cap nhat thanh cong!" -ForegroundColor Green
        } else {
            Write-Host "   [WARNING] Remote commit khac local, co the can doi them" -ForegroundColor Yellow
        }
    } else {
        Write-Host "`n[ERROR] Push that bai!" -ForegroundColor Red
        Write-Host "   Chi tiet loi:" -ForegroundColor Yellow
        $pushResult | ForEach-Object { Write-Host "   $_" -ForegroundColor Red }
        Write-Host "`n   Giai phap:" -ForegroundColor Yellow
        Write-Host "   1. Kiem tra GitHub credentials" -ForegroundColor White
        Write-Host "   2. Su dung Personal Access Token thay vi password" -ForegroundColor White
        Write-Host "   3. Hoac push bang GitHub Desktop/Android Studio" -ForegroundColor White
        exit 1
    }
} catch {
    Write-Host "`n[ERROR] Loi khi push: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  HOAN THANH!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

