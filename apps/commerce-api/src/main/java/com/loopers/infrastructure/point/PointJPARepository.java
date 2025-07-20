package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJPARepository extends JpaRepository<PointEntity, Long> {

    PointEntity findByLoginId(String loginId);
}
