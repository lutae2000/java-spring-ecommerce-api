package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.SortBy;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJPARepository extends JpaRepository<Product, Long> {

    /**
     * 물품조회(비관락)
     * @param productId
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.code = :productId")
    Product findProductByCodeForUpdate(@Param("productId") String productId);

    /**
     * 물품 저장
     * @param product
     * @return
     */
    Product save(Product product);

    /**
     * 물품 상세
     * @param code
     * @return
     */
    @Query("select p from Product p where p.code = :code")
    Product findByCode(@Param("code") String code);


    int countAllByCode(String code);

    /**
     * 삼품목록 조회
     * @param brandCode
     * @param pageable
     * @return
     */
    @Query("SELECT p, COALESCE(ls.likesCount, 0) as likesCount FROM Product p " +
        "LEFT JOIN LikeSummary ls ON p.code = ls.productId " +
        "WHERE (:brandCode IS NULL OR p.brand = :brandCode) " +
        "AND p.useYn = true " +
        "ORDER BY " +
        "CASE WHEN :sortBy = 'LIKE_ASC' THEN COALESCE(ls.likesCount, 0) END ASC, " +
        "CASE WHEN :sortBy = 'LIKE_DESC' THEN COALESCE(ls.likesCount, 0) END DESC, " +
        "CASE WHEN :sortBy = 'PRICE_ASC' THEN p.price END ASC, " +
        "CASE WHEN :sortBy = 'PRICE_DESC' THEN p.price END DESC, " +
        "CASE WHEN :sortBy = 'LATEST' THEN p.createdAt END DESC, " +
        "p.code ASC")
    Page<Object[]> findProductListByBrandCode(String brandCode, @Param("sortBy") String sortBy, Pageable pageable);

    /**
     * 재고 조정
     * @param productId
     * @param quantity
     */
    @Query("update Product p set p.quantity = :quantity where p.code = :productId")
    void updateProductQuantity(String productId, Long quantity);
}
