package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherIEMOPBilling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by TSI on 11/17/2023.
 */
public interface CheckVoucherIEMOPBillingRepo extends JpaRepository<CheckVoucherIEMOPBilling, Integer> {

    List<CheckVoucherIEMOPBilling> findAllByCheckVoucherId(Integer cvId);

    Long deleteByCheckVoucherId(Integer cvId);

}
