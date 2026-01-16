# Create all Java files with proper UTF-8 encoding (no BOM)
$basePath = "src\main\java\com\babyshop\api"
# Function to create Java file
function CreateJavaFile($path, $content) {
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($path, $content, $utf8NoBom)
    Write-Host "Created: $path"
}
# ProductStatus enum
$content = @"
package com.babyshop.api.product.entity;
public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISABLED
}
"@
CreateJavaFile "$basePath\product\entity\ProductStatus.java" $content
Write-Host "File creation script ready. Run with: .\create_all_files.ps1"
