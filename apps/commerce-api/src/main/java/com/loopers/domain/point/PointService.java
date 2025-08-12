package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    private final UserRepository userRepository;

    /**
     * 충전
     * @param loginId
     * @param point
     * @return
     */
    @Transactional
    public PointInfo chargePoint(PointCommand.Create command) {

        log.debug("::: chargePoint ::: command: {}", command);

        User user = userRepository.selectUserByUserId(command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다"));

        if(command.point() <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "충전하려는 포인트가 0이하가 될 수 없습니다");
        }

        Long totalPoint = 0L;
        Point Point = pointRepository.findByUserId(user.getUserId()).orElse(null);

        if(ObjectUtils.isEmpty(Point)) {  //값이 없으면 회원가입 후 최초 포인트 적립
            totalPoint = command.point();
            Point = Point.builder()
                .userId(user.getUserId())
                .point(totalPoint)
                .build();
        } else {    //조회해온 포인트 최종값 + 충전할 포인트
            totalPoint = Point.getPoint() + command.point();
            Point.setPoint(totalPoint);
        }

        pointRepository.save(Point);

        return PointInfo.builder()
            .userId(Point.getUserId())
            .point(Point.getPoint())
            .build();
    }


    /**
     * 회원의 포인트 조회
     * @param loginId
     * @return
     */
    @Transactional
    public Point getPointInfo(String loginId) {

        User user = userRepository.selectUserByUserId(loginId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다"));

        Point point = pointRepository.findByUserId(loginId)
            .orElseGet(() -> Point.builder().userId(loginId).point(0L).build());

/*        PointInfo pointInfo = PointInfo.builder()
            .userId(user.getUserId())
            .point(ObjectUtils.isEmpty(Point) ? 0L : Point.getPoint())
            .build();*/

        return point;
    }

    @Transactional
    public void updatePoint(String loginId, Long cost) {
        Point point = this.getPointInfo(loginId);
        pointRepository.updatePoint(loginId, point.getPoint() - cost);
    }

}
