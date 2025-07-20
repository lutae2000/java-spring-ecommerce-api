package com.loopers.domain.domainEnum;


import com.loopers.support.annotation.RequireLoginHeader;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public enum Gender {

    M("남성"),
    F("여성");

    private final String label;

}
