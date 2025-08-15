package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * product 생성(upsert)
     */
    @Transactional
    public void createProduct(ProductCommand productCommand){
        Product product = ProductCommand.toProduct(productCommand);
        productRepository.save(product);
    }

    /**
     * 물품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductInfo findProduct(String productId){

        Product product = productRepository.findProduct(productId);
        if(ObjectUtils.isEmpty(product)){
            throw new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다");
        }
        return ProductInfo.from(product);
    }


    @Transactional
    public void orderedStock(String productId, Long quantity){
        ProductInfo productInfo = findProduct(productId);

        if(ObjectUtils.isEmpty(productInfo)){
            throw new CoreException(ErrorType.NOT_FOUND, "주문하려는 물품코드가 없습니다");
        }
        if(productInfo.getQuantity() < quantity){
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다");
        }
        productRepository.orderProduct(productId, quantity);
    }
}
