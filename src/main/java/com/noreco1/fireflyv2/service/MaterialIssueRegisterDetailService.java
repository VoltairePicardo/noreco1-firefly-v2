package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.MaterialIssueRegisterDetailDto;

import java.util.List;

/**
 * Created by nsutgio2015 on 4/30/2015.
 */
public interface MaterialIssueRegisterDetailService {

    public List<MaterialIssueRegisterDetailDto> findByMaterialIssueRegisterId(Integer id);
}
