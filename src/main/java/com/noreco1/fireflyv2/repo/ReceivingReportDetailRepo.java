package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ReceivingReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReceivingReportDetailRepo extends JpaRepository<ReceivingReportDetail, Integer> {

    @Query(value = "SELECT e.deliveryNumber FROM ReceivingReportDetail e WHERE e.FK_receivingReportId = :rrId LIMIT 1", nativeQuery = true)
    String findOneByReceivingReportId(@Param("rrId") Integer rrId);

    List<ReceivingReportDetail> findByReceivingReportId(Integer rrId);
    Long deleteByReceivingReportId(Integer rrId);

    ReceivingReportDetail findByPoDetailIdAndReceivingReportDocumentStatusIdNotIn(Integer id, List<Integer> integers);

    List<ReceivingReportDetail> findAllByPoDetailPurchaseRequestDetailPurchaseRequestIdOrderByReceivingReportCode(Integer purchaseRequestId);
}
