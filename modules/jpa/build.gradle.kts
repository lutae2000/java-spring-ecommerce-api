plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Spring Data JPA (exposed to consumers)
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL for JPA (Jakarta) exposed to consumers
    api("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // JDBC driver (runtime)
    runtimeOnly("com.mysql:mysql-connector-j")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Test Fixtures (used by other modules' tests)
    testFixturesImplementation("org.springframework.boot:spring-boot-starter")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation("org.testcontainers:mysql")
    testFixturesImplementation("org.testcontainers:testcontainers")
}
