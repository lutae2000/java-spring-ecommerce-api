package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@Slf4j
public class RedisConfig {

    private static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        log.info("Redis Master 설정: {}:{}", master.getHost(), master.getPort());
        if (replicas != null && !replicas.isEmpty()) {
            log.info("Redis Replica 설정: {}개", replicas.size());
            replicas.forEach(replica ->
                log.info("  - {}:{}", replica.getHost(), replica.getPort()));
        } else {
            log.info("Redis Replica 설정: 없음 (단일 Redis 사용)");
        }

        return lettuceConnectionFactory(
            database, master, replicas,
            b -> b.readFrom(ReadFrom.REPLICA_PREFERRED)
        );
    }

    @Bean
    @Qualifier(CONNECTION_MASTER)
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        return lettuceConnectionFactory(
            database, master, replicas,
            b -> b.readFrom(ReadFrom.MASTER)
        );
    }

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return defaultRedisTemplate(template, lettuceConnectionFactory);
    }

    @Bean
    @Qualifier(REDIS_TEMPLATE_MASTER)
    public RedisTemplate<String, String> masterRedisTemplate(
        @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return defaultRedisTemplate(template, lettuceConnectionFactory);
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
        int database,
        RedisNodeInfo master,
        List<RedisNodeInfo> replicas,
        Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer
    ) {
        try {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
            if (customizer != null) {
                customizer.accept(builder);
            }
            LettuceClientConfiguration clientConfig = builder.build();

            RedisStaticMasterReplicaConfiguration masterReplica =
                new RedisStaticMasterReplicaConfiguration(master.getHost(), master.getPort());
            masterReplica.setDatabase(database);

            if (replicas != null && !replicas.isEmpty()) {
                for (RedisNodeInfo replica : replicas) {
                    if (replica.getHost() != null && replica.getPort() != null) {
                        masterReplica.addNode(replica.getHost(), replica.getPort());
                        log.info("Replica 노드 추가: {}:{}", replica.getHost(), replica.getPort());
                    } else {
                        log.warn("Replica 노드 설정 누락: host={}, port={}", replica.getHost(), replica.getPort());
                    }
                }
            }

            LettuceConnectionFactory factory = new LettuceConnectionFactory(masterReplica, clientConfig);
            factory.afterPropertiesSet();

            log.info("Redis 연결 팩토리 생성 완료 - Master: {}:{}, Replicas: {}개",
                master.getHost(), master.getPort(),
                replicas != null ? replicas.size() : 0);

            return factory;

        } catch (Exception e) {
            log.error("Redis 연결 팩토리 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Redis connection factory creation failed", e);
        }
    }

    private <K, V> RedisTemplate<K, V> defaultRedisTemplate(
        RedisTemplate<K, V> template,
        LettuceConnectionFactory connectionFactory
    ) {
        StringRedisSerializer string = new StringRedisSerializer();
        template.setKeySerializer(string);
        template.setValueSerializer(string);
        template.setHashKeySerializer(string);
        template.setHashValueSerializer(string);
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
