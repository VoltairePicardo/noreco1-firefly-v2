package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.CanvassDetailDto;

import java.util.List;

/**
 * Created by Personal on 5/14/2015.
 */
public interface CanvassDetailService {
    public List<CanvassDetailDto> getCanvassDetails(Integer canvassId);
}
