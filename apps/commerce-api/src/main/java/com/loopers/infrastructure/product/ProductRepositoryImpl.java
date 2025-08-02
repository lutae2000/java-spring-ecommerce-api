package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJPARepository productJPARepository;

    @Override
    public Product findProduct(String productId) {
        return productJPARepository.findProductByCode(productId);
    }

    @Override
    public void deleteProduct(String productId) {
//        productJPARepository.delete(productId);
    }

    @Override
    public Product save(Product product) {
        Product savedProduct = productJPARepository.save(product);
        return savedProduct;
    }
}
