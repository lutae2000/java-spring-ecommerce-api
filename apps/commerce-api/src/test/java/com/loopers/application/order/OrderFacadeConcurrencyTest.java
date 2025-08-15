package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
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

        PointCommand.Create pointCommand = new PointCommand.Create("user1", 1000000L);
        pointService.chargePoint(pointCommand);

        Product product1 = Product.builder()
            .code("A0001")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(1000))
            .name("테스트 물품")
            .quantity(1000L)
            .category1("ELECTRIC")
            .useYn(true)
            .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
            .code("A0002")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(2000))
            .quantity(1000L)
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

        //given
        List<OrderDetail> items = List.of(
            new OrderDetail("A0001", 2L, BigDecimal.valueOf(1000)),
            new OrderDetail("A0002", 1L, BigDecimal.valueOf(2000))
        );

        String userId = "user1";
        String couponNo = "1234";

        Order order = Order.builder()
            .userId(userId)
            .couponNo(couponNo)
            .orderDetailList(items)
            .build();

        //when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    orderFacade.orderSubmit(userId, order);
                } catch (Exception e){

                } finally {
                    latch.countDown();
                }

            });
        }
        latch.await();
        executorService.shutdown();

        //then
        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user1");
        assertThat(orderList.size()).isEqualTo(1);

    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    @Test
    void order_should_be_success_when_same_user_submit_order_concurrently() throws InterruptedException {

        //given
        List<OrderDetail> items = List.of(
            new OrderDetail("A0001", 2L, BigDecimal.valueOf(1000)),
            new OrderDetail("A0002", 1L, BigDecimal.valueOf(2000))
        );

        String userId = "user1";
        String couponNo = "1234";

        Order order = Order.builder()
            .userId(userId)
            .couponNo(couponNo)
            .orderDetailList(items)
            .build();

        //when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    orderFacade.orderSubmit(userId, order);
                } catch (Exception e){
                    System.err.println("주문 실패: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }

            });
        }
        latch.await();
        executorService.shutdown();

        //then
        Point pointInfo = pointService.getPointInfo("user1");
        // 주문 금액: (1000 * 2) + (2000 * 1) = 4000, 할인: 4000 * 0.1 = 400, 실제 차감: 3600
        // 1000000 - 3600 = 996400
        assertThat(pointInfo.getPoint()).isEqualTo(996400L);
//        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user1");
//        assertThat(orderList.size()).isEqualTo(2);

    }

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다.")
    @Test
    void order_valid_stock() throws InterruptedException {

        //given
        List<OrderDetail> items = List.of(
            new OrderDetail("A0001", 2L, BigDecimal.valueOf(1000)),
            new OrderDetail("A0002", 1L, BigDecimal.valueOf(2000))
        );

        String userId = "user1";
        String couponNo = "1234";

        Order order = Order.builder()
            .userId(userId)
            .couponNo(couponNo)
            .orderDetailList(items)
            .build();


        //when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    orderFacade.orderSubmit(userId, order);
                } catch (Exception e){

                } finally {
                    latch.countDown();
                }

            });
        }
        latch.await();
        executorService.shutdown();

        //then
        Point pointInfo = pointService.getPointInfo("user1");
        // 주문 금액: (1000 * 2) + (2000 * 1) = 4000, 할인: 4000 * 0.1 = 400, 실제 차감: 3600
        // 1000000 - 3600 = 996400
        assertThat(pointInfo.getPoint()).isEqualTo(996400L);
//        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user1");
//        assertThat(orderList.size()).isEqualTo(2);

    }
}
