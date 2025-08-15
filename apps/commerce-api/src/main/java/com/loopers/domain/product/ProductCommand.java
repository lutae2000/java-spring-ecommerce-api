package com.loopers.domain.product;

import com.loopers.application.product.ProductCriteria;
import com.loopers.domain.brand.Brand;
import java.math.BigDecimal;
import lombok.Builder;
import org.hibernate.query.Page;

public record ProductCommand (
    String code,
    String name,
    BigDecimal price,
    Long quantity,
    String imgURL,
    String description,
    String brandCode,
    String category1,
    String category2,
    String category3,
    SortBy sortBy,
    int page,
    int size
){
    public static Product toProduct(ProductCommand productCommand){
        return Product.from(productCommand);
    }

    // Brand 객체가 필요한 경우를 위한 메서드 추가
    public static Product toProductWithBrand(ProductCommand productCommand){
        return Product.from(productCommand);
    }

    @Builder
    public record Create(String code, String name, String description, String imgURL, Boolean useYn){
        public Brand toEntity(){
            return new Brand(code, name, description, imgURL, useYn);
        }
    }

}
