package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_details")
@NoArgsConstructor
@Getter
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order orderNo;

    private String productId;

    private Long quantity;

    private BigDecimal unitPrice;

    public OrderDetail(Long id, Order orderNo, String productId, Long quantity, BigDecimal unitPrice) {
        this.id = id;
        this.orderNo = orderNo;
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
        if(unitPrice.compareTo(BigDecimal.ZERO) <= 0){  //수량 체크(수량은 반드시 1이상)
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 주문수량 입니다");
        }
    }
}
