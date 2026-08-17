package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccountsPayableVoucherLink;
import com.noreco1.fireflyv2.model.PoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountsPayableVoucherLinkRepo extends JpaRepository<AccountsPayableVoucherLink, Integer> {
    Integer deleteByAccountsPayableVoucherId(Integer id);
    List<AccountsPayableVoucherLink> findByAccountsPayableVoucherIdAndDocumentTypeId(Integer apvId, Integer docTypeId);
    AccountsPayableVoucherLink findByAccountsPayableVoucherId(Integer apvId);
    AccountsPayableVoucherLink findFirstByAccountsPayableVoucherId(Integer apvId);

    @Transactional
    @Query(value = "SELECT " +
            "apv.`code` " +
            "FROM AccountsPayableVoucherLink apvl " +
            "INNER JOIN AccountsPayableVoucher apv ON apv.id = apvl.FK_accountsPayableVoucherId " +
            "WHERE apvl.FK_documentTypeId = :documentTypeId AND apvl.FK_linkedDocumentId = :receivingReportId ", nativeQuery = true)
    List<String> findAllByReceivingReportId(@Param("documentTypeId") Integer documentTypeId, @Param("receivingReportId") Integer receivingReportId);
}
