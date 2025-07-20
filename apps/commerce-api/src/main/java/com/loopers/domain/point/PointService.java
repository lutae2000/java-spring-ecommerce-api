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
    public PointInfo chargePoint(PointCommand.Create command) {

        log.debug("::: chargePoint ::: command: {}", command);

        User user = userRepository.selectUserByLoginId(command.loginId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다"));

        if(command.point() <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "충전하려는 포인트가 0이하가 될 수 없습니다");
        }

        Long totalPoint = 0L;
        PointEntity pointEntity = pointRepository.findByLoginId(user.getLoginId()).orElse(null);

        if(ObjectUtils.isEmpty(pointEntity)) {  //값이 없으면 회원가입 후 최초 포인트 적립
            totalPoint = command.point();
            pointEntity = PointEntity.builder()
                .loginId(user.getLoginId())
                .point(totalPoint)
                .build();
        } else {    //조회해온 포인트 최종값 + 충전할 포인트
            totalPoint = pointEntity.getPoint() + command.point();
            pointEntity.setPoint(totalPoint);
        }

        pointRepository.save(pointEntity);

        return PointInfo.builder()
            .loginId(pointEntity.getLoginId())
            .point(pointEntity.getPoint())
            .build();
    }


    public PointInfo getPointInfo(String loginId) {

        Optional<User> user = userRepository.selectUserByLoginId(loginId);

        if(ObjectUtils.isEmpty(user)){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        PointEntity pointEntity = pointRepository.findByLoginId(user.get().getLoginId()).orElse(null);


        PointInfo pointInfo = PointInfo.builder()
            .loginId(user.get().getLoginId())
            .point(ObjectUtils.isEmpty(pointEntity) ? 0L : pointEntity.getPoint())
            .build();

        return pointInfo;
    }

}
