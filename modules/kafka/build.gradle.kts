plugins {
    `java-library`
    `java-test-fixtures`
}

sourceSets {
    named("main") {
        resources.srcDir("src/resources")
    }
}

dependencies {
    // Spring for Apache Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Tests
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // Test Fixtures
    testFixturesImplementation("org.springframework.kafka:spring-kafka")
    testFixturesImplementation("org.apache.kafka:kafka-clients")
}
