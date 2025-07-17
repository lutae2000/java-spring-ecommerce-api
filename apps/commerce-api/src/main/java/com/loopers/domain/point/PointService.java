package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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


        pointRepository.save(user.get().getLoginId(), command.point());

        PointEntity pointEntity = pointRepository.findByLoginId(user.get().getLoginId());

        return PointInfo.from(pointEntity);
    }


    public PointInfo getPointInfo(String loginId) {

        PointEntity pointEntity = pointRepository.findByLoginId(loginId);

        if(ObjectUtils.isEmpty(pointEntity)){
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다");

        } else {
            pointEntity = new PointEntity(loginId, 0L);
        }
        return PointInfo.from(pointEntity);
    }
}
