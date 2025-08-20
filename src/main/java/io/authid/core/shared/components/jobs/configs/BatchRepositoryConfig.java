package io.authid.core.shared.components.jobs.configs;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchRepositoryConfig {
    // =================================================================
    // == KONFIGURASI JOB REPOSITORY
    // =================================================================

    @Bean
    public JobRepository jobRepository(
            DataSource dataSource, PlatformTransactionManager transactionManager
    ) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();

        // Menentukan DataSource mana yang akan dipakai oleh JobRepository
        factory.setDataSource(dataSource);

        // Menentukan TransactionManager mana yang akan dipakai
        factory.setTransactionManager(transactionManager);

        // Menggunakan skema tabel default (BATCH_*)
        factory.setTablePrefix("APP_BATCH_");

        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
