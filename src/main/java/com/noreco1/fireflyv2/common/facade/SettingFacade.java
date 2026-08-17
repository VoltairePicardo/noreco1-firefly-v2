package com.noreco1.fireflyv2.common.facade;


import java.util.List;
import java.util.Map;

public interface SettingFacade {
    Map getByCode(String code);
    List<Map> findAll();
    List<Integer> approvedVouchersPurchasingRoles();
}
