package com.loopers.domain.like;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LikeTest {

    @DisplayName("성공 - 좋아요 객체 생성")
    @Test
    void likeCreateSucceed(){

        Like like = new Like("aaa", "utlee", true);

        assert like.getLikeYn().equals(true);
        assert like.getProductId().equals("aaa");
        assert like.getUserId().equals("utlee");
    }

    @DisplayName("실패 - 유저 ID 누락")
    @Test
    void likeCreateFail_whenUserIdIsNull(){
        CoreException response = assertThrows(CoreException.class, () -> {
            Like like = new Like("aaa", null, true);
        });
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("회원ID는 필수값 입니다");
    }

    @DisplayName("실패 - 상품코드 누락")
    @Test
    void likeCreateFail_whenProductIdIsNull(){
        CoreException response = assertThrows(CoreException.class, () -> {
            Like like = new Like(null, "utlee", true);
        });
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("물품 코드는 필수값 입니다");
    }

    @DisplayName("실패 - 좋아요 플래그 누락")
    @Test
    void likeCreateFail_when_like_flag_dIsNull(){
        CoreException response = assertThrows(CoreException.class, () -> {
            Like like = new Like("A0001", "utlee", null);
        });
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("좋아요 플래그는 필수값 입니다");
    }
}
