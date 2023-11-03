package com.jerry.pilipala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
@EnableNeo4jRepositories(
        basePackages = {"com.jerry.pilipala.domain"},
        transactionManagerRef = "neo4jTransactionManager"
)
@EntityScan(basePackages = {"com.jerry.pilipala.domain.model.neo4j"})
public class PiliPalaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PiliPalaApplication.class, args);
    }

}
