package com.noreco1.fireflyv2.repo;

import jakarta.persistence.*;

import com.noreco1.fireflyv2.model.MaterialChargeTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Temporal;

import java.util.Date;

public interface MaterialChargeTicketRepo extends JpaRepository<MaterialChargeTicket, Integer> {
    Page<MaterialChargeTicket> findAllByVoucherDateBetweenOrderByVoucherDateAscCodeAsc(@Temporal(TemporalType.DATE) Date start, @Temporal(TemporalType.DATE) Date end, Pageable paging);
    Page<MaterialChargeTicket> findAllByVoucherDateBetweenAndCodeOrderByVoucherDateAscCodeAsc(@Temporal(TemporalType.DATE) Date start, @Temporal(TemporalType.DATE) Date end, String code, Pageable paging);

    Page<MaterialChargeTicket> findByCodeContainingIgnoreCaseAndPurposeContainingIgnoreCaseOrderByCodeAsc(String query, String query1, Pageable pageable);
}
