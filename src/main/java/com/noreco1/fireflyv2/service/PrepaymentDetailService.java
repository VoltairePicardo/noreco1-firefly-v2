package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PrepaymentDetailDto;

import java.util.List;

/**
 * Created by Personal on 11/9/2015.
 */
public interface PrepaymentDetailService {
    public List<PrepaymentDetailDto> getPrepaymentDetails(Integer ppId);
}
