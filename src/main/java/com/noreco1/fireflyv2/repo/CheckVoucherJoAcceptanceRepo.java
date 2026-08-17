package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherJoAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckVoucherJoAcceptanceRepo extends JpaRepository<CheckVoucherJoAcceptance, Integer>{
    void deleteByCheckVoucherId(Integer id);
    CheckVoucherJoAcceptance findByCheckVoucherId(Integer id);
}
