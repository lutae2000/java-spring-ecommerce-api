package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * product 생성(upsert)
     */
    public Product createProduct(Product product){
        return productRepository.save(product);
    }

    /**
     * product 조회
     */
    public Product findProduct(String productId){
        return productRepository.findProduct(productId);
    }

    /**
     * product 삭제
     */
    public void deleteProduct(String productId){
        if(findProduct(productId).getId() == null){
            throw new CoreException(ErrorType.NOT_FOUND, "삭제하려는 물품코드가 없습니다");
        }
        productRepository.deleteProduct(productId);
    }
}
