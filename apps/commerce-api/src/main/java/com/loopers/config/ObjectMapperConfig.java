package com.loopers.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ObjectMapper 전역 설정
 * LocalDateTime 등의 Java 8 시간 API 지원을 위한 설정
 */
@Configuration
public class ObjectMapperConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 시간 API 지원
        mapper.registerModule(new JavaTimeModule());
        
        // 타임스탬프를 ISO 문자열로 직렬화 (숫자가 아닌)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 알 수 없는 프로퍼티 무시 (JSON 스키마 변경에 유연하게 대응)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // JSON 중첩 깊이 제한 조정 (순환 참조 방지)
        mapper.getFactory().setStreamReadConstraints(
            StreamReadConstraints.builder()
                .maxNestingDepth(2000)  // 기본값 1000에서 2000으로 증가
                .build()
        );
        
        mapper.getFactory().setStreamWriteConstraints(
            StreamWriteConstraints.builder()
                .maxNestingDepth(2000)  // 기본값 1000에서 2000으로 증가
                .build()
        );
        
        return mapper;
    }
}
