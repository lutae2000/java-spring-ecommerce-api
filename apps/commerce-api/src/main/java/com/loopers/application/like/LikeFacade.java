package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeFacade {
    private final LikeService likeService;
    private final UserService  userService;
    private final ProductService productService;
    private final LikeSummaryRepository likeSummaryRepository;
    /**
     * 좋아요
     * @param userId
     * @param productId
     * @return
     */
    @Transactional
    public void like(LikeCriteria likeCriteria){
        UserInfo userInfo = userService.getUserInfo(likeCriteria.userId());
        ProductInfo productInfo = productService.findProduct(likeCriteria.productId());
        likeService.like(userInfo.getUserId(), productInfo.getCode());
    }

    /**
     * 좋아요 취소
     * @param userId
     * @param productId
     * @return
     */
    @Transactional
    public void likeCancel(LikeCriteria likeCriteria){
        UserInfo userInfo = userService.getUserInfo(likeCriteria.userId());
        likeService.likeCancel(userInfo.getUserId(), likeCriteria.productId());
    }

    /**
     * 좋아요 행으로 카운팅
     */
    @Transactional
    public Boolean likeExist(String userId, String productId){
        Boolean like = likeService.likeExist(userId, productId);
        return like;
    }

    /**
     * 좋아요 컬럼 값으로 카운팅
     */
    @Transactional(readOnly = true)
    public Long likeSummaryCount(String productId){
        Long likeSummaryCount = likeService.LikeSummaryCountByProductId(productId);
        return likeSummaryCount;
    }


}
