package com.loopers.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopers.domain.brand.Brand;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfo {

    private String code;
    private String name;
    private BigDecimal price;
    private Long quantity;
    private String imgURL;
    private String description;
    private String brandCode;
    private String category1;
    private String category2;
    private String category3;
    private Long likeCount;

    public static ProductInfo from(Product product){
        return new ProductInfo(
            product.getCode(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getImgURL(),
            product.getDescription(),
            product.getBrand() != null ? product.getBrand() : null,
            product.getCategory1(),
            product.getCategory2(),
            product.getCategory3(),
            0L
        );
    }
}
