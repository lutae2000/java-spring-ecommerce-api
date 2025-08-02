package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order extends BaseEntity {

    private Long orderNo;  // String → Long 변경 (auto-generated용)

    @OneToMany(mappedBy = "orderNo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    /**
     * 주문 총액 계산
     */
    public BigDecimal totalAmount() {
        return orderDetailList.stream()
            .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
