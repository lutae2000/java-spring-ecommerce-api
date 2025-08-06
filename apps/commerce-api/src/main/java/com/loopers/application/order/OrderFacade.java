package com.loopers.application.order;

import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
    private final UserService userService;
    private final PointService pointService;
    private final ProductService productService;


    public OrderResult orderSubmit(String userId, BigDecimal totalAmount, List<orderItem> orderItems){
        UserInfo userInfo = userService.getUserInfo(userId);

        if(ObjectUtils.isEmpty(userInfo)){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 계정이 아닙니다");
        }

        PointInfo pointInfo = pointService.getPointInfo(userInfo.getUserId());

        if(pointInfo.getPoint() < totalAmount.intValue()){
            throw new CoreException(ErrorType.BAD_REQUEST, "가지고 있는 잔액이 부족합니다");
        }

        OrderInfo orderInfo = orderService.placeOrder(userId, totalAmount, orderItems);

        for(orderItem item : orderItems){   //재고 차감
            productService.orderedStock(item.productId(), item.quantity());
        }

        return OrderResult.of(orderInfo);
    }
}
