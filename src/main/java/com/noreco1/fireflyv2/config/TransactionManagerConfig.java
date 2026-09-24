package com.noreco1.fireflyv2.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionManagerConfig {

    @Bean(name = "chainedTransactionManager")
    public ChainedTransactionManager chainedTransactionManager(
            @Qualifier("mssqlTransactionManager") PlatformTransactionManager mssqlTransactionManager,
            @Qualifier("mysqlTransactionManager") PlatformTransactionManager mysqlTransactionManager) {
        return new ChainedTransactionManager(mssqlTransactionManager, mysqlTransactionManager);
    }

}
