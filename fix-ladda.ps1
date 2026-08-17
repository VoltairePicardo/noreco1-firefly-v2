$pages = "C:/Users/TSI-KEN04/Documents/GitHub/noreco1-firefly-v2/frontend/src/app/pages"

Get-ChildItem -Path $pages -Recurse -Filter "*.html" | ForEach-Object {
    $path = $_.FullName
    $content = Get-Content $path -Raw

    $changed = $false

    # Fix literal `n artifact from previous run (appears as backtick-n in file)
    if ($content -match '`n\s*<ng-icon') {
        $content = $content -replace '`n\s*(<ng-icon)', "`n                                    `$1"
        $changed = $true
    }

    # Fix any remaining [ladda]
    if ($content -match '\[ladda\]') {
        $content = $content -replace 'ladda-button ', ''
        $content = $content -replace '\[ladda\]="formSubmit"', '[disabled]="formSubmit"'
        $spinner = '<span *ngIf="formSubmit" class="spinner-border spinner-border-sm me-2" role="status"></span>'
        $content = $content -replace '(<ng-icon \*ngIf="!formSubmit")', ($spinner + "`n                                    " + '$1')
        $changed = $true
    }

    if ($changed) {
        Set-Content $path $content -NoNewline
        Write-Host "Updated: $($_.Name)"
    }
}
