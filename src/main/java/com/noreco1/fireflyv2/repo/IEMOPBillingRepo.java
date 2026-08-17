package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.IEMOPBilling;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by TSI on 10/20/2023.
 */
public interface IEMOPBillingRepo extends JpaRepository<IEMOPBilling, Integer> {

    Page<IEMOPBilling> findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrderByStlIdAscBillingIdAsc(String q1, String q2, Pageable pageable);

    @Query(value = "SELECT * FROM IEMOPBilling ib " +
            "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
            "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
            "WHERE cvib.id is null AND apvib.id is null " +
            "AND ib.stlId like :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM IEMOPBilling ib " +
                    "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
                    "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
                    "WHERE cvib.id is null AND apvib.id is null " +
                    "AND ib.stlId like :query ",
            nativeQuery = true)
    Page<IEMOPBilling> findIEMOPBillingNotInCVByQuery(String query, Pageable pageable);

    @Query(value = "SELECT * FROM IEMOPBilling ib " +
            "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
            "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
            "WHERE cvib.id is null AND apvib.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM IEMOPBilling ib " +
                    "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
                    "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
                    "WHERE cvib.id is null AND apvib.id is null",
            nativeQuery = true)
    Page<IEMOPBilling> findIEMOPBillingNotInCV(Pageable pageable);

    @Query(value = "SELECT * FROM IEMOPBilling ib " +
            "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
            "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
            "WHERE cvib.id is null AND apvib.id is null " +
            "AND ib.stlId like :query ",
            countQuery = "SELECT COUNT(*) FROM IEMOPBilling ib " +
                    "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
                    "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
                    "WHERE cvib.id is null AND apvib.id is null " +
                    "AND ib.stlId like :query ",
            nativeQuery = true)
    List<IEMOPBilling> findIEMOPBillingNotInCVByQuery(String query);

    @Query(value = "SELECT * FROM IEMOPBilling ib " +
            "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
            "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
            "WHERE cvib.id is null AND apvib.id is null ",
            countQuery = "SELECT COUNT(*) FROM IEMOPBilling ib " +
                    "LEFT JOIN CheckVoucherIEMOPBilling cvib ON ib.id = cvib.FK_iemopBillingId " +
                    "LEFT JOIN AccountsPayableVoucherIEMOPBilling apvib ON ib.id = apvib.FK_iemopBillingId " +
                    "WHERE cvib.id is null AND apvib.id is null ",
            nativeQuery = true)
    List<IEMOPBilling> findIEMOPBillingNotInCV();

}
