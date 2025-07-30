package com.loopers.domain.product;

public interface ProductRepository {


    /**
     * Product 조회
     */
    public Product findProduct(String productId);

    /**
     * Product 삭제
     */
    public void deleteProduct(String productId);

    /**
     * Product 생성
     */
    Product save(Product product);
}
