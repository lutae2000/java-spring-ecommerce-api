package com.loopers.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CouponTest {

    @DisplayName("쿠폰 객체 생성")
    @Nested
    class Create{

        @DisplayName("성공 - 정률 쿠폰 ")
        @ParameterizedTest
        @CsvSource({
            "123456, 0.2, 10000, true",
            "123456, 0.1, 20000, false",
        })
        void rate_coupon_when_valid_succeed(String couponNo, Double discountRate, BigDecimal discountRateLimitPrice, Boolean useYn){
            Coupon coupon = Coupon.builder()
                .eventId("ABCV")
                .couponNo(couponNo)
                .userId("utlee")
                .couponName("테스트 쿠폰 입니다")
                .useYn(useYn)
                .discountType(DiscountType.RATIO_DISCOUNT)
                .discountRate(BigDecimal.valueOf(discountRate))
                .discountRateLimitPrice(discountRateLimitPrice)
                .build();

            assertAll(
                () -> assertThat(coupon.getCouponNo()).isEqualTo(couponNo)
            );
        }

        @DisplayName("성공 - 정액 쿠폰")
        @ParameterizedTest
        @CsvSource({
            "123456, 10000, true",
            "12345, 100000, true"
        })
        void fixed_coupon_when_valid_succeed(String couponNo, Long discountAmount, Boolean useYn){
            Coupon coupon = Coupon.builder()
                .eventId("ABCV")
                .couponNo(couponNo)
                .userId("utlee")
                .couponName("테스트 쿠폰 입니다")
                .useYn(useYn)
                .discountType(DiscountType.AMOUNT_DISCOUNT)
                .discountAmount(BigDecimal.valueOf(discountAmount))
                .build();

            assertAll(
                () -> assertThat(coupon.getCouponNo()).isEqualTo(couponNo)
            );
        }

        @Nested
        @DisplayName("실패")
        class Fail{

            @DisplayName("400에러 - 정률쿠폰 할인금액 비정상")
            @ParameterizedTest
            @CsvSource({
                "10000, 0",
                "-10000, 0",
                "0, 0",
                "0, -0.3"
            })
            void rate_coupon_when_invalid_discountAmount(BigDecimal discountRateLimitPrice, BigDecimal discountRate){
                CoreException response = assertThrows(CoreException.class, () -> {
                    Coupon coupon = Coupon.builder()
                        .eventId("ABCV")
                        .couponNo("123456")
                        .userId("utlee")
                        .couponName("테스트 쿠폰 입니다")
                        .useYn(true)
                        .discountType(DiscountType.RATIO_DISCOUNT)
                        .discountRateLimitPrice(discountRateLimitPrice)
                        .discountRate(discountRate)
                        .build();
                });

                assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            }

            @DisplayName("400에러 - 정액쿠폰 할인금액 비정상")
            @ParameterizedTest
            @CsvSource({
                "0",
                "-10000"
            })
            void fixed_coupon_when_invalid_discountAmount(BigDecimal discountAmount){
                CoreException response = assertThrows(CoreException.class, () -> {
                    Coupon coupon = Coupon.builder()
                        .eventId("ABCV")
                        .couponNo("123456")
                        .userId("utlee")
                        .couponName("테스트 쿠폰 입니다")
                        .useYn(true)
                        .discountType(DiscountType.AMOUNT_DISCOUNT)
                        .discountAmount(discountAmount)
                        .build();
                });

                assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            }
        }
    }
}
