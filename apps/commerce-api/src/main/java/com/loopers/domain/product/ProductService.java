package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * product 생성(upsert)
     */
    public void createProduct(ProductCommand productCommand){
        Product product = productCommand.toProduct(productCommand);
        productRepository.save(product);
    }

    /**
     * product 조회
     */
    public ProductInfo findProduct(String productId){

        Product product = productRepository.findProduct(productId);
        if(ObjectUtils.isEmpty(product)){
            throw new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다");
        }
        return ProductInfo.from(product);
    }

    /**
     * product 삭제
     */
    public void deleteProduct(String productId){
        if(findProduct(productId) == null){
            throw new CoreException(ErrorType.NOT_FOUND, "삭제하려는 물품코드가 없습니다");
        }
        productRepository.deleteProduct(productId);
    }

    public void orderedStock(String productId, Long quantity){
        ProductInfo productInfo = findProduct(productId);

        if(ObjectUtils.isEmpty(productInfo)){
            throw new CoreException(ErrorType.NOT_FOUND, "주문하려는 물품코드가 없습니다");
        }
        if(productInfo.getQuantity() < quantity){
            throw new CoreException(ErrorType.NOT_FOUND, "재고가 부족합니다");
        }
        productRepository.orderProduct(productId, quantity);
    }
}
