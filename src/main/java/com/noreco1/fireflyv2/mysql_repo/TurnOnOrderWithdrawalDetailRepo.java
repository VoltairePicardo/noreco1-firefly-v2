package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.TurnOnOrderWithdrawalDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnOnOrderWithdrawalDetailRepo extends JpaRepository<TurnOnOrderWithdrawalDetail, Integer> {

    List<TurnOnOrderWithdrawalDetail> findAllByTurnOnOrderWithdrawalId(Integer id);

}
