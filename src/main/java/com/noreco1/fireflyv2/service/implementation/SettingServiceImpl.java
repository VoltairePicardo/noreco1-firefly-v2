package com.noreco1.fireflyv2.service.implementation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.Setting;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.SettingRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.SettingService;
import com.noreco1.fireflyv2.validator.SettingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    SettingRepo settingRepo;

    @Autowired
    AuthenticationFacade authFacade;

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        Setting setting = (Setting) entity;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {
            SettingValidator validator = new SettingValidator();
            validator.setService(this);
            validator.validate(setting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                Setting exSetting = settingRepo.findById(setting.getId()).orElse(null);
                if (exSetting != null) {
                    Setting s = settingRepo.save(setting);
                    if (s != null) {
                        response.setSuccessMessage("Setting successfully saved");
                    } else {
                        response.setFailureMessage("Failed to update setting");
                    }
                } else {
                    response.setFailureMessage(setting.getDescription() + " is not available");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        Setting setting = (Setting) entity;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {

            SettingValidator validator = new SettingValidator();
            validator.setService(this);
            validator.validate(setting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                User user = authFacade.getLoggedIn();
                setting.setCreatedBy(user);

                Setting newSetting = settingRepo.save(setting);
                if (newSetting != null) {
                    response.setSuccessMessage("Setting successfully saved");
                } else {
                    response.setFailureMessage("Failed to save setting successfully");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Override
    public PostResponse delete(Integer settingId) {
        PostResponse response = new PostResponse();

        try {
            Setting exSetting = settingRepo.findById(settingId).orElse(null);
            if (exSetting != null) {
                settingRepo.delete(exSetting);
                response.setSuccessMessage("Setting successfully deleted");
            } else {
                response.setFailureMessage("Setting is not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Override
    public Map findByCode(String code) {
        return settingFacade.getByCode(code);
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

                    StringBuilder htmlBuilder = new StringBuilder();
                    htmlBuilder.append("<html>");
                    htmlBuilder.append("<ul>");

                    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                        String key = entry.getKey();
                        Object jsonValue = entry.getValue();
                        String valueStr = jsonValue.toString();

                        htmlBuilder.append("<li>");

                        if (jsonValue instanceof Map) { // for nested json: level only
                            htmlBuilder.append("<b>"+key+"</b>: ");

                            Map<String,Object> subValueMap = (Map)jsonValue;

                            htmlBuilder.append("<ul>");
                            for (Map.Entry<String, Object> subEntry : subValueMap.entrySet()) {
                                String subKey = subEntry.getKey();
                                Object subValue = subEntry.getValue();

                                htmlBuilder.append("<li>");
                                htmlBuilder.append("<b>"+subKey+"</b>: " + ": " + subValue.toString());
                                htmlBuilder.append("</li>");
                            }
                            htmlBuilder.append("</ul>");
                        } else {
                            htmlBuilder.append("<b>"+key+"</b>: " + valueStr);
                        }

                        htmlBuilder.append("</li>");

                    }
                    htmlBuilder.append("</ul>");
                    htmlBuilder.append("</html>");

                    row.put("id", s.getId());
                    row.put("code", s.getCode());
                    row.put("description", s.getDescription());
                    row.put("createdBy", s.getCreatedBy());
                    row.put("updatedAt", s.getUpdatedAt());
                    row.put("valueHtml", htmlBuilder.toString());
                    row.put("valueJson", valueMap);

                    settingsMap.add(row);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException(e);
        }
        return  settingsMap;
    }

    @Override
    public Setting findById(Integer id) {
        return settingRepo.findById(id).orElse(null);
    }
}
