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

    /**
     * 주문한 수량만큼 재고 차감
     * @param productId
     * @param quantity
     */
    void orderProduct(String productId, Long quantity);
}
