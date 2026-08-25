package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.CanvassDetailDto;
import com.noreco1.fireflyv2.service.CanvassDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Personal on 5/14/2015.
 */
@RestController
@RequestMapping("/api/canvass-detail")
public class CanvassDetailController {

    @Autowired
    CanvassDetailService canvassDetailService;

    @GetMapping(value = "/cnvsd/{canvassId}")
    
    public List<CanvassDetailDto> getCanvassDetails(@PathVariable Integer canvassId, HttpServletRequest request) {
        return canvassDetailService.getCanvassDetails(canvassId);
    }
}