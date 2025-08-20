package com.loopers.domain.product;


import com.loopers.application.product.ProductPageResult;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
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
    Page<Object[]> findProductListByBrandCode(String brandCode, SortBy sortBy, Pageable pageable);

    /**
     * 캐시에서 Product 조회 (캐시 미스 시 supplier 호출)
     */
    Product findProductWithCache(String productId, Duration ttl);

    /**
     * 캐시에서 Product Page 조회 (캐시 미스 시 supplier 호출)
     */
    ProductPageResult findProductListByBrandCodeWithCache(String brandCode, SortBy sortBy, Pageable pageable, Duration ttl);

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

    /**
     * 상품 리스트 저장
     * @param products
     */
    void saveAll(List<Product> products);

    /**
     * 상품 사운팅
     * @return
     */
    public int count();

    /**
     * 캐시 무효화
     */
    void invalidateProductCache(String productId);
}
