package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccountSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface AccountSettingRepo extends JpaRepository<AccountSetting, Integer> {

    List<AccountSetting> findAllByDateBetweenAndCreatedByIdOrderByDate(Date from, Date to, Integer userId);

}
