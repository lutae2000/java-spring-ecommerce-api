package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.StringUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    private String userId;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    @Builder
    public Order(String orderNo, String userId, OrderStatus orderStatus, BigDecimal totalAmount, List<OrderDetail> orderDetailList) {
        this.orderNo = ObjectUtils.isEmpty(orderNo) ? StringUtil.generateCode(7) : orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderDetailList = orderDetailList;
        orderUserIdValid(userId);
        orderDetailValid(orderDetailList);
        orderAmountPrice();
    }

    public void orderUserIdValid(String userId){
        if(StringUtils.isEmpty(userId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문자 계정은 필수입니다");
        }
    }

    public void orderDetailValid(List<OrderDetail> orderItems){
        if(ObjectUtils.isEmpty(orderItems)){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문하려는 물품은 필수입니다");
        }
    }

    public void orderAmountPrice(){
        if(!totalAmount.equals(totalAmount())){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문하려는 총금액과 상품가격이 상이합니다");
        }
    }
    /**
     * 주문 총액 계산
     */
    public BigDecimal totalAmount() {
        return orderDetailList.stream()
            .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
