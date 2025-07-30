package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String orderNo;

    @OneToMany(cascade = CascadeType.ALL)//연결된 객체와 같이 저장/삭제
    private List<OrderDetail> orderDetailList;

    /**
     * 주문번호의 총 금액
     * @return
     */
    public BigDecimal totalAmount(){
        return orderDetailList.stream().map(OrderDetail::price).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
