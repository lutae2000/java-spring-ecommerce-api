package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.Builder;

public class ProductCommand {
    private String code;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String imgURL;
    private String description;
    @Transient
    private String brandCode;
    private String category1;
    private String category2;
    private String category3;
    private boolean useYn;
    private Long likes;

    public static ProductInfo from(Product product){
        return new ProductInfo(
            product.getCode(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getImgURL(),
            product.getBrandCode(),
            product.getCategory1(),
            product.getCategory2(),
            product.getCategory3(),
            product.getDescription(),
            product.isUseYn(),
            product.getLikes()
        );
    }

    @Builder
    public record Create(String code, String name, String description, String imgURL, Boolean useYn){
        public Brand toEntity(){
            return new Brand(code, name, description, imgURL, useYn);
        }
    }
}
