package com.noreco1.fireflyv2.common.facade;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class SettingFacadeImpl implements SettingFacade {

    private final String INVENTORY_LOCATION_IDS = "INVENTORY_LOCATION_IDS";
    private final String CA_FOR_CV_DEFAULT_ACCOUNTS = "CA_FOR_CV_DEFAULT_ACCOUNTS";

    @Autowired
    private SettingRepo settingRepo;

    @Autowired
    private WorkflowRepo workflowRepo;

    @Override
    public Map getByCode(String code) {
        Setting setting = settingRepo.findOneByCode(code);

        try {
            if (setting != null) {
                String json = setting.getValue();
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);

                TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                HashMap<String,Object> o = mapper.readValue(json, typeRef);

                return o;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map> findAll() {
        List<Map> settingsMap = new ArrayList<>();
        try {

            List<Setting> settings = settingRepo.findAll();
            if (!Checker.collectionIsEmpty(settings)) {

                for(Setting s:settings) {

                    Map row = new HashMap();


                    String json = s.getValue();
                    JsonFactory factory = new JsonFactory();
                    ObjectMapper mapper = new ObjectMapper(factory);

                    TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                    Map<String,Object> valueMap = mapper.readValue(json, typeRef);

                    row.put("id", s.getId());
                    row.put("code", s.getCode());
                    row.put("createdBy", s.getCreatedBy());
                    row.put("updatedAt", s.getUpdatedAt());
                    row.put("value", valueMap);
                }
                return settingsMap;
            }

        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException(e);
        }
        return  settingsMap;
    }

    @Override
    public List<Integer> approvedVouchersPurchasingRoles() {
        Map rolesMap = this.getByCode(SettingCode.APPROVED_VOUCHERS_USER_ROLES.name());
        return this.transformRoles((String) rolesMap.get("purchasingRoles"));
    }

    private List<Integer> transformRoles(String rolesStr) {

        List<Integer> ids = new ArrayList<>();

        if(rolesStr != null) {
            String[] roleIdsStr = rolesStr.split("\\s*,\\s*");
            ids = ServiceUtil.strArrayToList(roleIdsStr);
        }

        return ids;
    }
}
