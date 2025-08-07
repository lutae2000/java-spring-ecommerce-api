package com.loopers.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class CouponServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    CouponService couponService;

    @BeforeAll
    void setup(){
        Coupon coupon = Coupon.builder()
            .eventId("ABC")
            .couponNo("1234")
            .userId("utlee")
            .couponName("테스트 쿠폰 입니다")
            .useYn(false)
            .discountType(DiscountType.RATIO_DISCOUNT)
            .discountRate(BigDecimal.valueOf(0.1))
            .discountRateLimitPrice(BigDecimal.valueOf(10000))
            .build();

        couponService.save(coupon);

        Coupon coupon1 = Coupon.builder()
            .eventId("ABCV")
            .couponNo("1234567")
            .userId("utlee")
            .couponName("테스트 쿠폰 입니다")
            .useYn(false)
            .discountType(DiscountType.RATIO_DISCOUNT)
            .discountRate(BigDecimal.valueOf(0.1))
            .discountRateLimitPrice(BigDecimal.valueOf(2000))
            .build();

        couponService.save(coupon1);

        Coupon coupon2 = Coupon.builder()
            .eventId("ABCV")
            .couponNo("123456789")
            .userId("utlee")
            .couponName("테스트 쿠폰 입니다")
            .useYn(false)
            .discountType(DiscountType.AMOUNT_DISCOUNT)
            .discountAmount(BigDecimal.valueOf(10000))
            .build();

        couponService.save(coupon2);
    }

    @AfterAll
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCoupon{

        @DisplayName("실패 - 회원한테 없는 쿠폰")
        @ParameterizedTest
        @CsvSource({
            "utlee, 1111",
            "utlee, 123",
        })
        void useCoupon_whenFailed(String userId, String couponNo){

            CouponCommand couponCommand = CouponCommand.builder()
                .userId(userId)
                .couponNo(couponNo)
                .build();

            CoreException response = assertThrows(CoreException.class, () -> {
                couponService.updateCouponUseYn(couponCommand);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("성공")
        @ParameterizedTest
        @CsvSource({
            "utlee, 1234",
            "utlee, 1234567",
        })
        void useCoupon_whenSucceed(String userId, String couponNo){

            CouponCommand couponCommand = CouponCommand.builder()
                .userId(userId)
                .couponNo(couponNo)
                .build();
            couponService.updateCouponUseYn(couponCommand);
        }

        @DisplayName("실패 - 이미 사용한 쿠폰으로 시도")
        @ParameterizedTest
        @CsvSource({
            "utlee, 1234",
            "utlee, 1234567",
        })
        void useCoupon_alreadyUsedCoupon(String userId, String couponNo){

            CouponCommand couponCommand = CouponCommand.builder()
                .userId(userId)
                .couponNo(couponNo)
                .build();

            CoreException response = assertThrows(CoreException.class, () -> {
                couponService.updateCouponUseYn(couponCommand);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("회원에게 할당된 쿠폰 조회")
    class GetCoupons{

        @DisplayName("성공")
        @ParameterizedTest
        @CsvSource({
            "utlee"
        })
        void getCoupon(String userId){

            Optional<List<Coupon>> list=  couponService.getCoupons(userId);

            assertThat(list.get().size()).isEqualTo(3);
        }


        @DisplayName("실패")
        @ParameterizedTest
        @CsvSource({
            "unknown",
            "cat"
        })
        void getCoupon_no_coupon_list(String userId){

            Optional<List<Coupon>> list=  couponService.getCoupons(userId);

            assertThat(list.get().size()).isEqualTo(0);
        }
    }
}
