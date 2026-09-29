package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MssqlConsumerRepo extends JpaRepository<Consumer, Integer> {

    Consumer findByAccountNo(Integer accountNo);
}
