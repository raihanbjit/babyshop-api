# PowerShell script to create all Product and Order module files

$basePath = "D:\baby-shop-ecommerce\babyshop-api\src\main\java\com\babyshop\api"

# Product Status Enum
@"
package com.babyshop.api.product.entity;

public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISABLED
}
"@ | Out-File -FilePath "$basePath\product\entity\ProductStatus.java" -Encoding UTF8

# Category Entity
@"
package com.babyshop.api.product.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;
}
"@ | Out-File -FilePath "$basePath\product\entity\Category.java" -Encoding UTF8

Write-Host "Created Product entities" -ForegroundColor Green
"@ | Out-File -FilePath "D:\baby-shop-ecommerce\babyshop-api\create_modules.ps1" -Encoding UTF8

