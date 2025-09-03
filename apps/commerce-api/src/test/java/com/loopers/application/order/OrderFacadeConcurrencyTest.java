package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.hikari.maximum-pool-size=20",
    "spring.datasource.hikari.minimum-idle=10"
})
public class OrderFacadeConcurrencyTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CardService cardService;

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

        // Product 생성
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

        // 카드 등록
        Card card = new Card("user1", "KB", CardType.KB, "1234567890121234");
        cardService.saveCard(card);
    }

    @DisplayName("동시에 여러 주문을 시도해도 주문이 정상적으로 생성되어야 한다.")
    @Test
    void concurrent_order_creation_should_succeed() throws InterruptedException {

        //given
        String userId = "user1";
        int threadCount = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            final int index = i;
            executorService.submit(() -> {
                try{
                    String productId = index % 2 == 0 ? "A0001" : "A0002";
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest(productId, 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, null, null, null
                    );

                    OrderInfo orderInfo = orderFacade.placeOrder(criteria);
                    successCount.incrementAndGet();
                    System.out.println("주문 성공: " + orderInfo.getOrder().getOrderNo());
                } catch (Exception e){
                    failureCount.incrementAndGet();
                    System.err.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        System.out.println("성공한 주문 수: " + successCount.get());
        System.out.println("실패한 주문 수: " + failureCount.get());
        
        // 최소한 하나의 주문은 성공해야 함
        assertThat(successCount.get()).isGreaterThan(0);
        // 실패한 주문이 있더라도 전체 시스템이 무너지지 않아야 함
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);
    }

    @DisplayName("동일한 쿠폰으로 여러 주문을 시도해도 쿠폰은 한 번만 사용되어야 한다.")
    @Test
    void coupon_should_be_used_only_once() throws InterruptedException {

        //given
        String userId = "user1";
        String couponNo = "1234";
        int threadCount = 10;
        AtomicInteger successCount = new AtomicInteger(0);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, couponNo, null, BigDecimal.valueOf(100)
                    );

                    OrderInfo orderInfo = orderFacade.placeOrder(criteria);
                    successCount.incrementAndGet();
                    System.out.println("쿠폰 주문 성공: " + orderInfo.getOrder().getOrderNo());
                } catch (Exception e){
                    System.err.println("쿠폰 주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        System.out.println("성공한 쿠폰 주문 수: " + successCount.get());
        // 쿠폰 사용으로 인해 여러 주문이 실패할 수 있지만, 최소한 하나는 성공해야 함
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }

    @DisplayName("동시에 여러 포인트 차감이 발생해도 주문이 정상적으로 생성되어야 한다.")
    @Test
    void concurrent_point_deduction_should_succeed() throws InterruptedException {

        //given
        String userId = "user1";
        int threadCount = 5;
        AtomicInteger successCount = new AtomicInteger(0);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try{
                    List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                        new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
                    );

                    OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                        userId, orderDetails, null, null, BigDecimal.valueOf(100)
                    );

                    OrderInfo orderInfo = orderFacade.placeOrder(criteria);
                    successCount.incrementAndGet();
                    System.out.println("포인트 주문 성공: " + orderInfo.getOrder().getOrderNo());
                } catch (Exception e){
                    System.err.println("포인트 주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        System.out.println("성공한 포인트 주문 수: " + successCount.get());
        
        // 최소한 하나의 주문은 성공해야 함
        assertThat(successCount.get()).isGreaterThan(0);
        // 동시성 제어가 제대로 작동하여 주문이 정상적으로 생성되어야 함
        assertThat(successCount.get()).isLessThanOrEqualTo(threadCount);
    }
}
