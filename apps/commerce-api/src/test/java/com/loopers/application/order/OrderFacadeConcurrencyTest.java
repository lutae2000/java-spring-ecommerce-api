package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetailCommand;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderFacadeConcurrencyTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponService couponService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @BeforeEach
    public void setup(){
        User userBuilder = User.builder()
            .userId("user1")
            .email("user1@naver.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();

        userRepository.save(userBuilder);

        PointCommand.Create pointCommand = new PointCommand.Create("user1", 10000L);
        pointService.chargePoint(pointCommand);

        Product product1 = Product.builder()
            .code("A0001")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(1000))
            .name("테스트 물품")
            .quantity(10L)
            .category1("ELECTRIC")
            .useYn(true)
            .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
            .code("A0002")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(2000))
            .quantity(10L)
            .name("테스트 물품")
            .category1("ELECTRIC")
            .useYn(true)
            .build();
        productRepository.save(product2);

        Coupon coupon = Coupon.builder()
            .eventId("ABC")
            .couponNo("1234")
            .userId("user1")
            .couponName("테스트 쿠폰 입니다")
            .useYn(false)
            .discountType(DiscountType.RATIO_DISCOUNT)
            .discountRate(BigDecimal.valueOf(0.1))
            .discountRateLimitPrice(BigDecimal.valueOf(10000))
            .build();

        couponService.save(coupon);
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void coupon_should_be_used_only_once_per_order() throws InterruptedException {
        // given
        List<orderItem> items = List.of(
            new orderItem("A0001", 2L, BigDecimal.valueOf(1000)),
            new orderItem("A0002", 1L, BigDecimal.valueOf(2000))
        );

        String userId = "user1";
        BigDecimal totalAmount = BigDecimal.valueOf(4000);
        String couponNo = "1234";


        //when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    orderFacade.orderSubmit(userId, couponNo, totalAmount, items);
                } catch (Exception e){

                } finally {
                    latch.countDown();
                }

            });
            latch.await();
            executorService.shutdown();
        }

        //then
        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user");
        assertThat(orderList.size()).isEqualTo(1);

    }
}
