package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucherJv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckVoucherJvRepo extends JpaRepository<CheckVoucherJv, Integer> {

    void deleteByCheckVoucherId(Integer id);

    CheckVoucherJv findByCheckVoucherId(Integer id);

}
