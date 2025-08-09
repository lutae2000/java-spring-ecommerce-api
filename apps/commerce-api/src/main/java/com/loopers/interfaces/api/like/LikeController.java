package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeCriteria;
import com.loopers.application.like.LikeFacade;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LikeController {
    private final LikeFacade likeFacade;

    @PostMapping(value = "/like/{productId}")
    public void like(
        @RequestHeader(value = CustomHeader.USER_ID, required = true) String userId,
        @PathVariable String productId
    ){
        LikeCriteria criteria = new LikeCriteria(userId, productId);
        likeFacade.like(criteria);
    }

    @DeleteMapping(value = "/like/{productId}")
    public void likeCancel(
        @RequestHeader(value = CustomHeader.USER_ID, required = true) String userId,
        @PathVariable String productId
    ){
        LikeCriteria criteria = new LikeCriteria(userId, productId);
        likeFacade.likeCancel(criteria);
    }
}
