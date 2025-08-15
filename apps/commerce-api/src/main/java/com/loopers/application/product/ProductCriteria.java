package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.SortBy;
import java.math.BigDecimal;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Builder
public record ProductCriteria(
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
) {
    public static ProductCommand toCommand(ProductCriteria criteria){
        return new ProductCommand(
            criteria.code(),
            criteria.name(),
            criteria.price(),
            criteria.quantity(),
            criteria.imgURL(),
            criteria.description(),
            criteria.brandCode(),
            criteria.category1(),
            criteria.category2(),
            criteria.category3(),
            criteria.sortBy,
            criteria.page,
            criteria.size
        );
    }

    public static Pageable toPageable(ProductCriteria criteria){
        return PageRequest.of(criteria.page, criteria.size);
    }
}
