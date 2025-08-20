package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductPageResult;
import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.SortBy;
import com.loopers.interfaces.api.like.LikeDto;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductFacade productFacade;

    /**
     * 상품 생성
     * @param userId
     * @param productDto
     */
    @PostMapping(value = "/product")
    public void createProduct(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestBody ProductDto productDto
    ){
        log.debug("::: inquiry loginId ::: {}", userId);
        log.debug("::: inquiry productDto ::: {}", productDto);

        ProductCriteria criteria = ProductDto.toCriteria(productDto);
        productFacade.createProduct(criteria);
    }

    @DeleteMapping(value = "/product/{productId}")
    public void deleteProduct(
        @RequestHeader(value = CustomHeader.USER_ID, required = true) String userId,
        @PathVariable String productId
    ){
//        productFacade.likeCancel(LikeDto.toCriteria(userId, productId));
    }

    /**
     * 검색 조건으로 상품 조회 (캐시 적용)
     */
    @GetMapping("/products/{productId}")
    public ProductResult getProductList(@PathVariable String productId) {
        log.debug("::: inquiry product list criteria ::: {}", productId);
        return productFacade.getProduct(productId);
    }

    /**
     * 브랜드별 상품 목록 조회 (캐시 적용)
     */
    @GetMapping("/products/brand/{brandCode}")
    public ProductPageResult getProductListByBrand(
        @PathVariable String brandCode,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size,
        @RequestParam(required = false) SortBy sortBy) {

        log.debug("::: inquiry brandCode ::: {}", brandCode);
        log.debug("::: inquiry page ::: {}", page);
        log.debug("::: inquiry size ::: {}", size);
        log.debug("::: inquiry sortBy ::: {}", sortBy);

        ProductCriteria criteria = ProductCriteria.builder()
            .brandCode(brandCode)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .build();

        return productFacade.getProductList(criteria);
    }
}
