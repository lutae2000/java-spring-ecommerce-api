package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
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

        Optional<User> user = userRepository.selectUserByLoginId(command.loginId());
        if(user.isEmpty()){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        Long totalPoint;
        PointEntity pointEntity = pointRepository.findByLoginId(user.get().getLoginId());
        if(ObjectUtils.isEmpty(pointEntity)) {  //값이 없으면 최초 적립
            totalPoint = command.point();
        } else {
            totalPoint = pointEntity.getPoint() + command.point();
        }

        pointRepository.save(user.get().getLoginId(), totalPoint);


        PointEntity result = pointRepository.findByLoginId(user.get().getLoginId());

        return PointInfo.from(result);
    }


    public PointInfo getPointInfo(String loginId) {

        Optional<User> user = userRepository.selectUserByLoginId(loginId);

        if(ObjectUtils.isEmpty(user)){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        PointEntity pointEntity = pointRepository.findByLoginId(user.get().getLoginId());

        if(ObjectUtils.isEmpty(pointEntity)) {
            return new PointInfo(0L, loginId); //NPE 발생해서 초기값 생성
        } else {
            return new PointInfo(pointEntity.getPoint(), loginId);
        }
    }
}
