package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.TurnOnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TurnOnOrderRepo extends JpaRepository<TurnOnOrder, Integer> {

    @Query(value = "select * from ( " +
            " " +
            "select " +
            "jo.joDate AS historyDate, " +
            "'Installed(JOA)' AS transactionType, " +
            "jo.jobOrderNo AS referenceNumber, " +
            "c.CrewName AS crewName, " +
            "joa.remarks AS remark, " +
            "u.fullName AS TransactBy, " +
            "joa.dateAccomplished AS transactionDate " +
            "from JobOrder jo " +
            "inner join JobOrderAccomplishment joa on joa.FK_jobOrderId = jo.id " +
            "inner join meter m on m.id = joa.FK_newMeterId " +
            "inner join Crew c on c.CrewID = jo.FK_crewId " +
            "inner join Users u on u.id = joa.FK_createdByUserId " +
            "where m.metersn = :serialNumber " +
            " " +
            "union  " +
            " " +
            "select " +
            "pmr.createdAt AS historyDate, " +
            "'Installed(Massive Meter Replacement)' AS transactionType, " +
            "null AS referenceNumber, " +
            "c.CrewName AS crewName, " +
            "null AS remark, " +
            "u.fullName AS TransactBy, " +
            "pmr.createdAt AS transactionDate " +
            "from PendingMeterReplacement pmr " +
            "inner join crew c on c.CrewID = pmr.FK_crewId " +
            "inner join Users u on u.id = pmr.FK_createdByUserId " +
            "where pmr.meterSN = :serialNumber " +
            "and pmr.FK_documentStatusId = 7 " +
            " " +
            ") as ItemHistory " +
            "order by ItemHistory.historyDate ", nativeQuery = true)
    List<Object[]> getItemHistory(@Param("serialNumber") String serialNumber);

}
