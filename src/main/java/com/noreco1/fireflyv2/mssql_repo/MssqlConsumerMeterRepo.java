package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.ConsumerMeter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MssqlConsumerMeterRepo extends JpaRepository<ConsumerMeter, Integer> {

    @Query("SELECT cm FROM ConsumerMeter cm " +
            "WHERE LOWER(cm.consumer.accountName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(cm.consumer.accountNo AS string) LIKE CONCAT('%', :query, '%') " +
            "OR LOWER(REPLACE(cm.consumer.oldAccountNo, '-', '')) LIKE LOWER(CONCAT('%', REPLACE(:query, '-', ''), '%'))")
    Page<ConsumerMeter> findAllByQuery(@Param("query") String query, Pageable pageable);
}
