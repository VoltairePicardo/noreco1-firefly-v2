package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface WorkOrderRepo extends JpaRepository<WorkOrder, Integer> {
    WorkOrder findByCode(String code);
    WorkOrder findByIdAndIsClosed(Integer id, Boolean isClosed);

    List<WorkOrder> findByYear(Integer year);
    List<WorkOrder> findByMonth(Integer month);
    List<WorkOrder> findByYearAndMonth(Integer year, Integer month);

    List<WorkOrder> findByIsClosed(Boolean isClosed);
    List<WorkOrder> findByIsClosedAndMonth(Boolean isClosed, Integer month);
    List<WorkOrder> findByIsClosedAndYear(Boolean isClosed, Integer year);
    List<WorkOrder> findByIsClosedAndYearAndMonth(Boolean isClosed, Integer year, Integer month);

    List<WorkOrder> findByDateBetweenOrderByCode(Date from, Date to);
    List<WorkOrder> findByDateBetweenAndIsClosedFalseOrderByCode(Date from, Date to);
    List<WorkOrder> findByDateBetweenAndIsClosedTrueOrderByCode(Date from, Date to);

    // no place to put
    @Query(value = "select " +
            "JournalVoucher.code as code, " +
            "JournalVoucher.explanation as description, " +
            "SubLedger.FK_transactionId as trans_id,  " +
            "SUM(COALESCE(debit,0)) as total_dr, " +
            "SUM(COALESCE(credit,0)) as total_cr " +
            "FROM SubLedger " +
            "JOIN JournalVoucher ON SubLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE SubLedger.FK_accountNo = :accountNo " +
            "AND SubLedger.FK_transactionId NOT IN (SELECT FK_transactionId FROM WorkOrderDetail)  " +
            "AND FK_documentStatusId = :status " +
            "GROUP BY JournalVoucher.FK_transactionId " +
            "HAVING code is NOT NULL " +
            " " +
            "UNION " +
            " " +
            "select " +
            "CheckVoucher.code as code, " +
            "CheckVoucher.particulars as description, " +
            "SubLedger.FK_transactionId as trans_id, " +
            "SUM(COALESCE(debit,0)) as total_dr, " +
            "SUM(COALESCE(credit,0)) as total_cr " +
            "FROM SubLedger " +
            "JOIN CheckVoucher ON SubLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE SubLedger.FK_accountNo = :accountNo " +
            "AND SubLedger.FK_transactionId NOT IN (SELECT FK_transactionId FROM WorkOrderDetail)  " +
            "AND FK_documentStatusId = :status " +
            "GROUP BY CheckVoucher.FK_transactionId " +
            "HAVING code is NOT NULL " +
            " " +
            "UNION " +
            " " +
            "select " +
            "MaterialIssueRegister.code as code, " +
                "MaterialIssueRegister.particulars as description, " +
            "SubLedger.FK_transactionId as trans_id, " +
            "SUM(COALESCE(debit,0)) as total_dr, " +
            "SUM(COALESCE(credit,0)) as total_cr " +
            "FROM SubLedger " +
            "JOIN MaterialIssueRegister ON SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId " +
            "WHERE SubLedger.FK_accountNo = :accountNo " +
            "AND SubLedger.FK_transactionId NOT IN (SELECT FK_transactionId FROM WorkOrderDetail)  " +
            "AND FK_documentStatusId = :status " +
            "GROUP BY MaterialIssueRegister.FK_transactionId " +
            "HAVING code is NOT NULL " +
            " " +
            "UNION " +
            " " +
            "select " +
            "AccountsPayableVoucher.code as code, " +
                "AccountsPayableVoucher.particulars as description, " +
            "SubLedger.FK_transactionId as trans_id, " +
            "SUM(COALESCE(debit,0)) as total_dr, " +
            "SUM(COALESCE(credit,0)) as total_cr " +
            "FROM SubLedger " +
            "JOIN AccountsPayableVoucher ON SubLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE SubLedger.FK_accountNo = :accountNo " +
            "AND SubLedger.FK_transactionId NOT IN (SELECT FK_transactionId FROM WorkOrderDetail)  " +
            "AND FK_documentStatusId = :status " +
            "GROUP BY AccountsPayableVoucher.FK_transactionId " +
            "HAVING code is NOT NULL", nativeQuery = true)
    List<Object[]> findVouchersForPosting(@Param("accountNo") Integer accountNo, @Param("status") Integer status);

    @Query(value = "SELECT " +
            "* " +
            "FROM (SELECT  " +
            "JournalVoucher.id as id,  " +
            "JournalVoucher.code as code,  " +
            "JournalVoucher.voucherDate as 'date',  " +
            "JournalVoucher.amount as amount,  " +
            "JournalVoucher.explanation as description  " +
            "FROM WorkOrderDetail  " +
            "JOIN JournalVoucher ON WorkOrderDetail.FK_transactionId = JournalVoucher.FK_transactionId  " +
            "WHERE WorkOrderDetail.FK_workOrderId = :workOrderId " +
            " " +
            "UNION " +
            " " +
            "SELECT  " +
            "CheckVoucher.id as id,  " +
            "CheckVoucher.code as code,  " +
            "CheckVoucher.voucherDate as 'date',  " +
            "CheckVoucher.amount as amount,  " +
            "CheckVoucher.particulars as description  " +
            "FROM WorkOrderDetail  " +
            "JOIN CheckVoucher ON WorkOrderDetail.FK_transactionId = CheckVoucher.FK_transactionId  " +
            "WHERE WorkOrderDetail.FK_workOrderId = :workOrderId  " +
            " " +
            "UNION " +
            " " +
            "SELECT  " +
            "MaterialIssueRegister.id as id,  " +
            "MaterialIssueRegister.code as code,  " +
            "MaterialIssueRegister.voucherDate as 'date',  " +
            "MaterialIssueRegister.amount as amount,  " +
            "MaterialIssueRegister.particulars as description  " +
            "FROM WorkOrderDetail  " +
            "JOIN MaterialIssueRegister ON WorkOrderDetail.FK_transactionId = MaterialIssueRegister.FK_transactionId  " +
            "WHERE WorkOrderDetail.FK_workOrderId = :workOrderId) vouchers " +
            " " +
            "ORDER BY vouchers.`date` DESC ", nativeQuery = true)
    List<Object[]> findPostedVouchers(@Param("workOrderId") Integer workOrderId);

    @Query(value = "SELECT " +
            "WorkOrder.code, " +
            "WorkOrder.description, " +
            "WorkOrder.date, " +
            "SUM(WorkOrderDetail.materials), " +
            "SUM(WorkOrderDetail.labor), " +
            "SUM(WorkOrderDetail.overhead), " +
            "SUM(WorkOrderDetail.materials+WorkOrderDetail.labor+WorkOrderDetail.overhead+WorkOrderDetail.houseConnection), " +
            "SUM(WorkOrderDetail.houseConnection), "+
            "WorkOrder.targetDate "+
            "FROM WorkOrder LEFT JOIN WorkOrderDetail ON WorkOrderDetail.FK_workOrderId = WorkOrder.id " +
            "WHERE WorkOrder.date <= :asOfDate AND (DATE(WorkOrder.closedDateTime) > :asOfDate OR WorkOrder.isClosed = 0) " +
            "GROUP BY WorkOrder.code, WorkOrder.date, WorkOrder.description ORDER BY WorkOrder.code", nativeQuery = true)
    List<Object[]> findWorkOrderAsOfTheSpecifiedDate(@Param("asOfDate") java.util.Date asOfDate);

    WorkOrder findByProjectId(Integer id);
    WorkOrder findTop1ByProjectId(Integer id);

    @Query(value = "SELECT e.code FROM WorkOrder e WHERE year = :year AND code LIKE '%WO%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestVvCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT " +
            "ist.quantity AS stockQty, " +
            "ist.id AS itemStockId, " +
            "i.id AS itemId, " +
            "i.code, " +
            "u.code AS unitCode, " +
            "u.id AS unitId, " +
            "ist.unitCost, " +
            "i.description, " +
            "ist.FK_inventoryLocationId, " +
            "ced.quantity " +
            "FROM CostEstimateDetail ced " +
            "INNER JOIN CostEstimate c ON c.id = ced.FK_costEstimateId " +
            "INNER JOIN Project p ON p.id = c.FK_projectId " +
            "INNER JOIN WorkOrder w ON w.FK_projectId = p.id " +
            "INNER JOIN ItemStock ist ON ist.FK_itemId = ced.FK_itemId " +
            "INNER JOIN Item i ON ist.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "WHERE w.id = :workOrderId AND ist.FK_inventoryLocationId = :inventoryLocationId AND i.FK_inventoryCategoryId = :inventoryCategoryId", nativeQuery = true)
    List<Object[]> findProjectCostEstimateDetailById(@Param("workOrderId") Integer workOrderId,
                                                     @Param("inventoryLocationId") Integer inventoryLocationId,
                                                     @Param("inventoryCategoryId") Integer inventoryCategoryId);

    @Query(value = "SELECT " +
            "ist.quantity AS stockQty, " +
            "ist.id AS itemStockId, " +
            "i.id AS itemId, " +
            "i.code, " +
            "u.code AS unitCode, " +
            "u.id AS unitId, " +
            "ist.unitCost, " +
            "i.description, " +
            "ist.FK_inventoryLocationId, " +
            "ced.quantity," +
            "i.FK_inventoryCategoryId " +
            "FROM CostEstimateDetail ced " +
            "INNER JOIN CostEstimate c ON c.id = ced.FK_costEstimateId " +
            "INNER JOIN Project p ON p.id = c.FK_projectId " +
            "INNER JOIN WorkOrder w ON w.FK_projectId = p.id " +
            "INNER JOIN ItemStock ist ON ist.FK_itemId = ced.FK_itemId " +
            "INNER JOIN Item i ON ist.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "WHERE w.id = :workOrderId", nativeQuery = true)
    List<Object[]> findProjectCostEstimateDetailById(@Param("workOrderId") Integer workOrderId);
}
