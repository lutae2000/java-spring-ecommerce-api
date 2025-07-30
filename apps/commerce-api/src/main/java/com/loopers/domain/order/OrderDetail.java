package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_detail")
@Embeddable
public class OrderDetail {
    @Id
    private String orderNo;
    private String productId;
    private Long quantity;
    private BigDecimal unitPrice;

    /**
     * 각 상품 총 가격
     * @return
     */
    public BigDecimal price(){

        if(unitPrice.compareTo(BigDecimal.ZERO) < 0){   //가격 체크(가격은 무료일수도 있으니 음수값만 체크)
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 주문가격 입니다");
        }

        if(unitPrice.compareTo(BigDecimal.ZERO) <= 0){  //수량 체크(수량은 반드시 1이상)
            throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 주문수량 입니다");
        }

        return BigDecimal.valueOf(quantity).multiply(unitPrice);
    }
}
