package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.Town;
import com.noreco1.fireflyv2.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/town")
public class TownController {

    @Autowired
    private TownService townService;

    @GetMapping("/list")
    public List<Town> list() {
        return townService.list();
    }
}
