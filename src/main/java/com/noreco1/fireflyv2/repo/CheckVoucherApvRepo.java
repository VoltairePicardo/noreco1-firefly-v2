package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherApv;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import com.noreco1.fireflyv2.model.SlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CheckVoucherApvRepo extends JpaRepository<CheckVoucherApv, Integer> {
    public Long deleteByCheckVoucherId(Integer cid);

//    @Transactional(readOnly = true)
//    @Query(value = "SELECT " +
//            "FK_accountsPayableVoucherId, " +
//            "FK_checkVoucherId " +
//            "FROM CheckVoucherApv " +
//            "WHERE FK_checkVoucherId = :FK_checkVoucherId", nativeQuery = true) // take one only for now
//    public List<Object[]> findByCheckVoucherId(@Param("FK_checkVoucherId") Integer cvId);

    CheckVoucherApv findByCheckVoucherId(Integer id);

    @Transactional
    @Query(value = "SELECT " +
            "cv.`code` " +
            "FROM CheckVoucherApv cva " +
            "INNER JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "INNER JOIN AccountsPayableVoucher apv ON apv.id = cva.FK_accountsPayableVoucherId " +
            "INNER JOIN AccountsPayableVoucherLink apvl ON apvl.FK_accountsPayableVoucherId = apv.id " +
            "WHERE apvl.FK_documentTypeId = :documentTypeId AND apvl.FK_linkedDocumentId = :receivingReportId ", nativeQuery = true)
    List<String> findAllByReceivingReportId(@Param("documentTypeId") Integer documentTypeId, @Param("receivingReportId") Integer receivingReportId);

}
