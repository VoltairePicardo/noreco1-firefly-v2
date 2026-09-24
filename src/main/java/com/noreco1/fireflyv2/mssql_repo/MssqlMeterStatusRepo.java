package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.MeterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MssqlMeterStatusRepo extends JpaRepository<MeterStatus, Integer> {

}
