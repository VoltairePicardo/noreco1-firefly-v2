package com.noreco1.fireflyv2.mysql_repo;

import jakarta.persistence.*;

import com.noreco1.fireflyv2.mysql_model.IomasSOAHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Temporal;

import java.util.Date;
import java.util.List;

public interface IomasSOAHeaderRepo extends JpaRepository<IomasSOAHeader, Integer> {
    List<IomasSOAHeader> findAllByDateBetweenOrderByDateAscNumberAsc(@Temporal(TemporalType.DATE) Date start, @Temporal(TemporalType.DATE) Date end);
    Page<IomasSOAHeader> findAllByDateBetweenOrderByDateAscNumberAsc(@Temporal(TemporalType.DATE) Date start, @Temporal(TemporalType.DATE) Date end, Pageable paging);
}

