package com.noreco1.fireflyv2.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.noreco1.fireflyv2.mssql_repo",
        entityManagerFactoryRef = "mssqlEntityManagerFactory",
        transactionManagerRef = "mssqlTransactionManager"
)
public class MSSQLDataSourceConfig {

    private final Environment environment;

    public MSSQLDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "mssqlDataSource")
    public DataSource mssqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(this.environment.getProperty("app.mssql.driver")));
        dataSource.setUrl(this.environment.getProperty("app.mssql.url"));
        dataSource.setUsername(this.environment.getProperty("app.mssql.username"));
        dataSource.setPassword(this.environment.getProperty("app.mssql.password"));
        return dataSource;
    }

    @Bean(name = "mssqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mssqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mssqlDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = builder
                .dataSource(dataSource)
                .packages("com.noreco1.fireflyv2.mssql_model")
                .persistenceUnit("mssql")
                .build();

        // Set Hibernate Dialect manually
        factoryBean.getJpaPropertyMap().put("hibernate.dialect", this.environment.getProperty("app.mssql.hibernate.dialect"));
        return factoryBean;
    }

    @Bean(name = "mssqlTransactionManager")
    public PlatformTransactionManager mssqlTransactionManager(@Qualifier("mssqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}