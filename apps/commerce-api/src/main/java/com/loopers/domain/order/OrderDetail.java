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
import org.apache.commons.lang3.StringUtils;

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
        validateOrderDetail(productId, quantity, unitPrice);
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    //Factory Method
    public static OrderDetail CreateOrderDetail(String productId, Long quantity, BigDecimal unitPrice){
        return new OrderDetail(productId, quantity, unitPrice);
    }

    private void validateOrderDetail(String productId, Long quantity, BigDecimal unitPrice){
        if(StringUtils.isEmpty(productId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품코드는 필수입니다");
        }
        if(unitPrice.compareTo(BigDecimal.ZERO) < 0){   //가격 체크(가격은 무료일수도 있으니 음수값만 체크)
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 주문가격 입니다");
        }
        if(quantity.compareTo(0L) <= 0){  //수량 체크(수량은 반드시 1이상)
            throw new CoreException(ErrorType.BAD_REQUEST, "주문수량은 1개 이상이어야 합니다");
        }
    }
}
