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
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
@Setter
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    private String userId;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    private String couponNo;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    @Builder
    public Order(String orderNo, String userId, OrderStatus orderStatus, BigDecimal totalAmount, List<OrderDetail> orderDetailList, String couponNo, BigDecimal discountPrice) {
        this.orderNo = ObjectUtils.isEmpty(orderNo) ? StringUtil.generateCode(10) : orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        this.orderDetailList = orderDetailList;
        this.couponNo = couponNo;
        this.discountAmount = discountPrice == null ? BigDecimal.ZERO : discountPrice;
    }

    // Factory Method
    public static Order createOrder(String userId, List<OrderDetail> orderDetailList, String couponNo, BigDecimal discountPrice){
        validateOrderCreation(userId, orderDetailList, couponNo, discountPrice);
        String orderNo = StringUtil.generateCode(10);
        BigDecimal totalPrice = calculateTotalAmount(orderDetailList);
        BigDecimal safeDiscountPrice = getDiscountPrice(discountPrice);
        BigDecimal totalAmount = applyDiscountPrice(totalPrice, safeDiscountPrice);

        Order order = new Order(orderNo, userId, OrderStatus.ORDER_PLACED, totalAmount, orderDetailList, couponNo, safeDiscountPrice);

        // 양방향 관계 설정
        if (orderDetailList != null) {
            orderDetailList.forEach(order::addOrderDetail); // 반드시 편의 메서드 사용

        }

        return order;
    }

    // 연관관계 편의 메서드 - 양방향 세팅 보장
    public void addOrderDetail(OrderDetail detail) {
        if (detail == null) return;
        orderDetailList.add(detail);
        detail.setOrder(this);
    }

    public void removeOrderDetail(OrderDetail detail) {
        if (detail == null) return;
        orderDetailList.remove(detail);
        detail.setOrder(null);
    }


    /**
     * 할인후 최종금액 계산
     * @param totalPrice
     * @param safeDiscountPrice
     * @return
     */
    private static BigDecimal applyDiscountPrice(BigDecimal totalPrice, BigDecimal safeDiscountPrice) {
        if(safeDiscountPrice.compareTo(totalPrice) > 0){
            return totalPrice;
        }
        return totalPrice.subtract(safeDiscountPrice);
    }

    private static void validateOrderCreation(String userId, List<OrderDetail> orderDetails, String couponNo, BigDecimal discountPrice) {
        if (StringUtils.isEmpty(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문자 계정은 필수입니다");
        }
        if (ObjectUtils.isEmpty(orderDetails)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문하려는 물품은 필수입니다");
        }
/*        if(StringUtils.isEmpty(couponNo) && discountPrice.compareTo(BigDecimal.ZERO) <= 0
            || StringUtils.isNotEmpty(couponNo) && discountPrice.compareTo(BigDecimal.ZERO) > 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 정상 적용되지 않았습니다");
        }*/
    }

    /**
     * 할인금액 계산
     */
    private static BigDecimal getDiscountPrice(BigDecimal discountPrice) {
        if (discountPrice == null) {
            return BigDecimal.ZERO;
        }
        if(discountPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인금액은 마이너스일 수 없습니다");
        }
        return discountPrice;
    }

    /**
     * 주문 총액 계산
     */
    private static BigDecimal calculateTotalAmount(List<OrderDetail> orderDetailList) {
         BigDecimal res = orderDetailList.stream()
            .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return res;
    }

}
