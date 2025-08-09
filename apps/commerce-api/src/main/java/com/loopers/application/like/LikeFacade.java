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

        LikeSummary likeSummary = new LikeSummary(likeCriteria.productId(), 0L);
        likeSummary.decreaseLikesCount();
        likeSummaryRepository.updateLikeSummary(likeSummary);
    }

    /**
     * 좋아요 행으로 카운팅
     */
    @Transactional(readOnly = true)
    public Long likeCount(String productId){
        List<Like> likeList = likeService.getLikeByProductId(productId);
        return (long) likeList.size();
    }

    /**
     * 좋아요 컬럼 값으로 카운팅
     */
    @Transactional(readOnly = true)
    public Long likeSummaryCount(String productId){
        LikeSummary likeSummary = likeService.likeSummaryByProductId(productId);
        return likeSummary.getLikesCount();
    }
}
