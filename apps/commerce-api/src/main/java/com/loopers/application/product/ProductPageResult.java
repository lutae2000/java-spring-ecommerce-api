package com.loopers.application.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopers.domain.product.ProductInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductPageResult {
    private List<ProductInfo> products;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    public static ProductPageResult from(Page<ProductInfo> page) {
        return ProductPageResult.builder()
            .products(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .build();
    }
}
