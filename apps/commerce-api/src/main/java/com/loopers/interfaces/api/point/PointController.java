package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointCommand.Create;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.header.CustomHeader;
import io.micrometer.common.util.StringUtils;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 포인트 조회
     * @param loginId
     * @return
     */
    @GetMapping("")
    public ApiResponse<PointDto.Response> getPointInfo(@RequestHeader(value = CustomHeader.USER_ID, required = false) String loginId) {

        if(StringUtils.isBlank(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더값은 필수 입니다");
        }

        log.debug("::: getPointInfo ::: loginId: {}", loginId);

        PointInfo pointInfo = pointService.getPointInfo(loginId);

        return ApiResponse.success(PointDto.Response.from(pointInfo));
    }

    /**
     * 포인트 중전
     */
    @PostMapping("/charge")
    public ApiResponse<PointDto.Response> chargePoint(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String loginId,
        @RequestBody PointDto.Request request) {

        if(StringUtils.isBlank(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더값은 필수 입니다");
        }


        log.debug("::: chargePoint ::: loginId: {}, request: {}", loginId, request);

        PointCommand.Create command = new Create(loginId, request.point);

        PointInfo pointInfo = pointService.chargePoint(command);
        return ApiResponse.success(PointDto.Response.from(pointInfo));
    }
}
