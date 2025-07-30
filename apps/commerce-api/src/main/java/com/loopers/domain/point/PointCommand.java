package com.loopers.domain.point;

public class PointCommand {

    private String loginId;
    private Long point;

    public record Create(String loginId, Long point) {
        public PointEntity toPointEntity() {
            return PointEntity.builder()
                .userId(this.loginId)
                .point(this.point)
                .build();
        }
    }

}
