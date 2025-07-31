package com.loopers.domain.like;

import com.loopers.domain.brand.BrandService;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        Like like = new Like("aaa", null, true);
    }

    @DisplayName("실패 - 상품코드 누락")
    @Test
    void likeCreateFail_whenProductIdIsNull(){
        Like like = new Like(null, "utlee", true);
    }


}
