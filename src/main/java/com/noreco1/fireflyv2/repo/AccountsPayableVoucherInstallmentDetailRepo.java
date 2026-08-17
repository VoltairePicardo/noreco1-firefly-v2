package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccountsPayableVoucherInstallmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountsPayableVoucherInstallmentDetailRepo extends JpaRepository<AccountsPayableVoucherInstallmentDetail, Integer> {

    void deleteAllByAccountsPayableVoucherId(Integer apvId);

    List<AccountsPayableVoucherInstallmentDetail> findAllByAccountsPayableVoucherId(Integer apvId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT apvid.* FROM AccountsPayableVoucherInstallmentDetail apvid " +
            "WHERE apvid.FK_accountsPayableVoucherId = :apvId " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM CheckVoucherApvPaidInstallment cvapi " +
            "    WHERE cvapi.FK_accountsPayableVoucherInstallmentDetailId = apvid.id " +
            ") ORDER BY apvid.dueDate", nativeQuery = true)
    List<AccountsPayableVoucherInstallmentDetail> findUnpaidInstallmentDetailsByApvId(@Param("apvId") Integer apvId);

}