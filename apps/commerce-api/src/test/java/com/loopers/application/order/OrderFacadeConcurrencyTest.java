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

        // Product 생성 방식 수정
        Product product1 = Product.create(
            "A0001",
            "테스트 물품1",
            BigDecimal.valueOf(1000),
            1000L,
            "image1.jpg",
            "테스트 물품1 설명",
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );
        productRepository.save(product1);

        Product product2 = Product.create(
            "A0002",
            "테스트 물품2",
            BigDecimal.valueOf(2000),
            1000L,
            "image2.jpg",
            "테스트 물품2 설명",
            "ELECTRIC",
            "컴퓨터",
            "데스크톱"
        );
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
        String userId = "user1";
        String couponNo = "1234";

        //when
        int threadCount = 10; // 테스트용으로 줄임
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            final int index = i;
            executorService.submit(() -> {
                try{
                    // 각 스레드마다 다른 주문번호를 가진 주문 생성
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                        new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, couponNo, BigDecimal.valueOf(400) // 10% 할인
                    );

                    orderFacade.placeOrder(criteria);
                } catch (Exception e){
                    System.err.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user1");
        // 쿠폰은 한 번만 사용되어야 하므로 성공한 주문은 1개여야 함
        assertThat(orderList.size()).isEqualTo(1);
    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    @Test
    void order_should_be_success_when_same_user_submit_order_concurrently() throws InterruptedException {

        //given
        String userId = "user1";

        //when
        int threadCount = 5; // 테스트용으로 줄임
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            final int index = i;
            executorService.submit(() -> {
                try{
                    // 각 스레드마다 다른 상품으로 주문
                    String productId = index % 2 == 0 ? "A0001" : "A0002";
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest(productId, 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, null, null
                    );

                    orderFacade.placeOrder(criteria);
                } catch (Exception e){
                    System.err.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        Point pointInfo = pointService.getPointInfo("user1");
        // 각 주문당 1000원씩 5번 주문 = 5000원 차감
        // 1000000 - 5000 = 995000
        assertThat(pointInfo.getPoint()).isEqualTo(995000L);
    }

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다.")
    @Test
    void order_valid_stock() throws InterruptedException {

        //given
        String userId = "user1";

        //when
        int threadCount = 10; // 테스트용으로 줄임
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    // 모든 스레드가 동일한 상품 주문
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, null, null
                    );

                    orderFacade.placeOrder(criteria);
                } catch (Exception e){
                    System.err.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        // 재고가 정상적으로 차감되었는지 확인
        Product product = productRepository.findProduct("A0001");
        // 초기 재고 1000개에서 10개 주문 = 990개 남아야 함
        assertThat(product.getQuantity()).isEqualTo(990L);
    }

    @DisplayName("동시성 제어 테스트 - 쿠폰 사용 시 중복 사용 방지")
    @Test
    void concurrency_control_coupon_usage() throws InterruptedException {

        //given
        String userId = "user1";
        String couponNo = "1234";

        //when
        int threadCount = 20; // 더 많은 스레드로 테스트
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, couponNo, BigDecimal.valueOf(100) // 10% 할인
                    );

                    orderFacade.placeOrder(criteria);
                } catch (Exception e){
                    // 예외가 발생하는 것은 정상 (쿠폰 중복 사용 방지)
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        // 쿠폰은 한 번만 사용되어야 함
        List<OrderInfo> orderList = orderService.findAllOrderByUserId("user1");
        assertThat(orderList.size()).isEqualTo(1);
        
        // 쿠폰 상태 확인
        Coupon usedCoupon = couponService.getCouponByCouponNo(couponNo);
        assertThat(usedCoupon.getUseYn()).isTrue();
    }
}
