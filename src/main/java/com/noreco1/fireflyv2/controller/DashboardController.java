package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    DocumentDtoer documentDtoer;

    @GetMapping("/documents")
    public List<Map> documents() {
        return documentDtoer.getForMainDashboard();
    }

    @GetMapping("/other-approved-documents")
    public List<Map> otherApprovedDocuments() {
        return documentDtoer.getApprovedForMainDashboard();
    }
}
