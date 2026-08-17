package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.JoAcceptanceDetailDto;

import java.util.List;

/**
 * Created by Personal on 7/7/2015.
 */
public interface JoAcceptanceDetailService {
    public List<JoAcceptanceDetailDto> getJoaDetails(Integer joaId);
}
