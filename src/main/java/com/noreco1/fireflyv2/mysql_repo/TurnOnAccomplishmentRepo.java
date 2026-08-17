package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.TurnOnAccomplishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TurnOnAccomplishmentRepo extends JpaRepository<TurnOnAccomplishment, Integer> {

    List<TurnOnAccomplishment> findAllByTurnOnOrderId(Integer TurnOnOrderId);

    @Query(value = "select TOP 1 * " +
            "FROM TurnOnAccomplishment t " +
            "INNER JOIN meter m ON m.id = t.meterID " +
            "WHERE m.metersn = :meterSN " +
            "ORDER BY t.installDate DESC", nativeQuery = true)
    TurnOnAccomplishment findByMeterSerialNumberOrderByInstallDateDesc(@Param("meterSN") String meterSN);
}
