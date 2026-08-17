package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherRr;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckVoucherRrRepo extends JpaRepository<CheckVoucherRr, Integer> {
    void deleteByCheckVoucherId(Integer id);
    CheckVoucherRr findByCheckVoucherId(Integer id);
}
