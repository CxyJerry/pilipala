package com.jerry.pilipala.infrastructure.config;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionManagerConfig {

    @Primary
    @Bean("mongoTransactionManager")
    public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }

    @Bean("neo4jTransactionManager")
    public Neo4jTransactionManager neo4jTransactionManager(Driver driver,
                                                           DatabaseSelectionProvider databaseNameProvider,
                                                           ObjectProvider<TransactionManagerCustomizers> optionalCustomizers) {
        final Neo4jTransactionManager transactionManager = new Neo4jTransactionManager(driver, databaseNameProvider);
        optionalCustomizers.ifAvailable((customizer) -> customizer.customize(transactionManager));
        return transactionManager;
    }

    @Autowired
    @Bean("multiTransactionManager")
    public PlatformTransactionManager multiTransactionManager(
            Neo4jTransactionManager neo4jTransactionManager,
            MongoTransactionManager mongoTransactionManager) {
        return new ChainedTransactionManager(neo4jTransactionManager, mongoTransactionManager);
    }
}
