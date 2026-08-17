package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherCashAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Tri-Nvent on 1/7/2020.
 */
public interface CheckVoucherCashAdvanceRepo extends JpaRepository<CheckVoucherCashAdvance, Integer> {
    public Long deleteByCheckVoucherId(Integer id);

    CheckVoucherCashAdvance findByCheckVoucherId(Integer id);
    List<CheckVoucherCashAdvance> findAllByCheckVoucherId(Integer id);

}
