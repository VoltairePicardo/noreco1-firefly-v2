package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.JoDetailDto;

import java.util.List;

/**
 * Created by Personal on 6/18/2015.
 */
public interface JoDetailService {
    public List<JoDetailDto> getJoDetails(Integer joId);
    public List<JoDetailDto> getJoDetailsForJoa(Integer joId);
}
