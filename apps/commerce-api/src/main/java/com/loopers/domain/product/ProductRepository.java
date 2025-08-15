package com.loopers.domain.product;


import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    /**
     * 비관락 Product 조회
     */
    public Product findProductForUpdate(String productId);

    /**
     * Product 조회
     */
    public Product findProduct(String productId);

    /**
     * 삼품목록 조회
     * @param brandCode
     * @param pageable
     * @return
     */
    Page<Product> findProductListByBrandCode(String brandCode, Pageable pageable);

    /**
     * Product 생성
     */
    Product save(Product product);

    /**
     * 주문한 수량만큼 재고 차감
     * @param productId
     * @param quantity
     */
    void orderProduct(String productId, Long quantity);

    void saveAll(List<Product> products);

    public int count();
}
