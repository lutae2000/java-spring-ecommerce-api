package com.loopers.domain.product;

import com.loopers.application.product.ProductCriteria;
import com.loopers.domain.brand.Brand;
import java.math.BigDecimal;
import lombok.Builder;

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
    String category3
){
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
            product.getCategory3()
        );
    }

    public static Product toProduct(ProductCommand productCommand){
        return new Product(
            productCommand.code(),
            productCommand.name(),
            productCommand.price(),
            productCommand.quantity(),
            productCommand.imgURL(),
            productCommand.description(),
            productCommand.brandCode(),
            productCommand.category1(),
            productCommand.category2(),
            productCommand.category3()
        );
    }
    
    public static ProductCommand toProduct(Product product){
        return new ProductCommand(
            product.getCode(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.getImgURL(),
            product.getDescription(),
            product.getBrandCode(),
            product.getCategory1(),
            product.getCategory2(),
            product.getCategory3()
        );
    }

    @Builder
    public record Create(String code, String name, String description, String imgURL, Boolean useYn){
        public Brand toEntity(){
            return new Brand(code, name, description, imgURL, useYn);
        }
    }

    public record Search(
        SortBy sortBy,
        int page,
        int size
    ){}
}
