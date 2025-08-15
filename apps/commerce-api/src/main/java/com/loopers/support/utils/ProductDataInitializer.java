package com.loopers.support.utils;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() == 0) { // 데이터 없는 경우만
            List<Product> products = new ArrayList<>();

            for (int i = 1; i <= 1_000_000; i++) {
                Product product = Product.builder()
                    .code("P" + i)
                    .name("Product " + i)
                    .price(BigDecimal.valueOf((i % 100) * 1000L + 1000))
                    .quantity((long) (i % 500))
                    .imgURL("https://example.com/image" + i + ".jpg")
                    .description("Description for product " + i)
                    .brand("B" + (i % 50))
                    .category1("Category1_" + (i % 10))
                    .category2("Category2_" + (i % 20))
                    .category3("Category3_" + (i % 30))
                    .useYn(true)
                    .build();

                products.add(product);

                // 5000개씩 나눠서 saveAll() 호출 → 메모리 절약
                if (products.size() % 5000 == 0) {
                    productRepository.saveAll(products);
                    products.clear();
                }
            }

            // 남은 데이터 저장
            if (!products.isEmpty()) {
                productRepository.saveAll(products);
            }

            System.out.println("✅ 100만 건 데이터 생성 완료");
        }
    }
}
