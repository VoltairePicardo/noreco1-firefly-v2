package com.noreco1.fireflyv2.config;

import jakarta.persistence.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Profile("mysql2")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.noreco1.fireflyv2.mysql_repo",
        entityManagerFactoryRef = "mysql2EntityManagerFactory",
        transactionManagerRef = "mysql2TransactionManager"
)
public class MySQL2DataSourceConfig {

    private final Environment environment;

    public MySQL2DataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "mysql2DataSource")
    public DataSource mysql2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("app.mysql2.driver"));
        dataSource.setUrl(environment.getProperty("app.mysql2.url"));
        dataSource.setUsername(environment.getProperty("app.mysql2.username"));
        dataSource.setPassword(environment.getProperty("app.mysql2.password"));
        return dataSource;
    }

    @Bean(name = "mysql2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mysql2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysql2DataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = builder
                .dataSource(dataSource)
                .packages("com.noreco1.fireflyv2.mysql_model")
                .persistenceUnit("mysql2")
                .build();
        factoryBean.getJpaPropertyMap().put("hibernate.dialect", environment.getProperty("app.mysql2.hibernate.dialect"));
        return factoryBean;
    }

    @Bean(name = "mysql2TransactionManager")
    public PlatformTransactionManager mysql2TransactionManager(
            @Qualifier("mysql2EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
