package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.interfaces.api.like.LikeDto;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductFacade productFacade;

    @PostMapping(value = "/product")
    public void like(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestBody ProductDto productDto
    ){
        log.debug("::: inquiry loginId ::: {}", userId);
        log.debug("::: inquiry productDto ::: {}", productDto);

        ProductCriteria criteria = ProductDto.toCriteria(productDto);
        productFacade.createProduct(criteria);
    }

    @DeleteMapping(value = "/product/{productId}")
    public void likeCancel(
        @RequestHeader(value = CustomHeader.USER_ID, required = true) String userId,
        @PathVariable String productId
    ){
//        productFacade.likeCancel(LikeDto.toCriteria(userId, productId));
    }
}
