package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SettingRepo extends JpaRepository<Setting, Integer> {
    public Setting findOneByCode(String code);
    public List<Setting> findByCodeIn(Collection<String> codes);
}
