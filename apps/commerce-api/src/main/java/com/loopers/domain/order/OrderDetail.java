package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_no")
    private Order order;

    private String productId;

    private Long quantity;

    private BigDecimal unitPrice;

    public OrderDetail(String productId, Long quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        validUnitPrice();
        validQuantity();
    }


    /**
     * 각 상품 가격
     * @return
     */
    public void validUnitPrice(){

        if(unitPrice.compareTo(BigDecimal.ZERO) < 0){   //가격 체크(가격은 무료일수도 있으니 음수값만 체크)
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 주문가격 입니다");
        }
    }

    /**
     * 수량 체크
     */
    public void validQuantity(){
        if(quantity.compareTo(0L) <= 0){  //수량 체크(수량은 반드시 1이상)
            throw new CoreException(ErrorType.BAD_REQUEST, "주문수량은 1개 이상이어야 합니다");
        }
    }
}
