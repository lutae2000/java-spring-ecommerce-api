package org.example.commercebatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class CommerceBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceBatchApplication.class, args);
    }

}
