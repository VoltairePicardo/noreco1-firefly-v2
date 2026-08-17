package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Setting;
import com.noreco1.fireflyv2.controller.response.PostResponse;

import java.util.List;
import java.util.Map;

public interface SettingService extends DataManagementService {
    PostResponse delete(Integer settingId);
    Map findByCode(String code);
    List<Map> findAll();
    Setting findById(Integer id);
}
