package com.loopers.application.like;

import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeFacade {
    private final LikeService likeService;
    private final UserService  userService;
    private final ProductService productService;

    /**
     * 좋아요
     * @param userId
     * @param productId
     * @return
     */
    public LikeResult like(String userId, String productId){
        UserInfo userInfo = userService.getUserInfo(userId);
        ProductInfo productInfo = productService.findProduct(productId);
        LikeInfo likeInfo = likeService.like(userInfo.getUserId(), productInfo.getCode());
        Long likesCount = likeService.countLike(productId);
        return LikeResult.of(productId, likesCount);
    }

    /**
     * 좋아요 취소
     * @param userId
     * @param productId
     * @return
     */
    public LikeResult likeCancel(String userId, String productId){
        UserInfo userInfo = userService.getUserInfo(userId);
        LikeInfo likeInfo = likeService.likeCancel(userInfo.getUserId(), productId);
        Long likesCount = likeService.countLike(productId);
        return LikeResult.of(productId, likesCount);
    }
}
