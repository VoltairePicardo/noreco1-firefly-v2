package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PaymentRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PaymentRequestDetailRepo extends JpaRepository<PaymentRequestDetail, Integer> {
    @Transactional
    void deleteByPaymentRequestId(Integer transId);

    ArrayList<PaymentRequestDetail> findAllByPaymentRequestId(Integer id);
}
