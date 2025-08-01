package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductInfo {


    private String code;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String imgURL;
    private String description;
    @Transient
    private String brand;
    private String category1;
    private String category2;
    private String category3;
    private boolean useYn;
    private Long like;

    public static ProductInfo from(Product product){
        return new ProductInfo(
            product.getCode(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getImgURL(),
            product.getDescription(),
            product.getBrandCode(),
            product.getCategory1(),
            product.getCategory2(),
            product.getCategory3(),
            product.isUseYn(),
            product.getLikes()
        );
    }
}
