package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJPARepository productJPARepository;

    @Override
    public Product findProductForUpdate(String productId) {
        return productJPARepository.findProductByCodeForUpdate(productId);
    }

    @Override
    public Product findProduct(String productId) {
        return productJPARepository.findByCode(productId);
    }

    @Override
    public Page<Product> findProductListByBrandCode(String brandCode, Pageable pageable) {
        return productJPARepository.findProductListByBrandCode(brandCode, pageable);
    }

    @Override
    public Product save(Product product) {
        Product savedProduct = productJPARepository.save(product);
        return savedProduct;
    }

    @Override
    public void orderProduct(String productId, Long quantity) {

        Product product = productJPARepository.findProductByCodeForUpdate(productId);
        product.setQuantity(product.getQuantity() - quantity);
        productJPARepository.save(product);
    }


    @Override
    public void saveAll(List<Product> products) {
        productJPARepository.saveAll(products);
    }

    @Override
    public int count() {
        int count = productJPARepository.countAllByCode("p1");
        return count;
    }
}
