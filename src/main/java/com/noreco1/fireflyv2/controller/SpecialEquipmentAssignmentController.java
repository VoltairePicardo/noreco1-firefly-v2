package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import com.noreco1.fireflyv2.repo.SpecialEquipmentAssignmentRepo;
import com.noreco1.fireflyv2.service.SpecialEquipmentAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/special-equipment-assignment")
public class SpecialEquipmentAssignmentController {

    @Autowired
    @Qualifier("specialEquipmentAssignmentServiceImpl")
    private SpecialEquipmentAssignmentService seaService;

    @Autowired
    private SpecialEquipmentAssignmentRepo seaRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @GetMapping("/list")
    public List<SpecialEquipmentAssignment> list() {
        Page<SpecialEquipmentAssignment> page = seaService.findAll("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31",
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/list/{from}/{to}")
    public List<SpecialEquipmentAssignment> listByDateRange(@PathVariable String from, @PathVariable String to) {
        Page<SpecialEquipmentAssignment> page = seaService.findAll(from, to,
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/{id}")
    public SpecialEquipmentAssignment getById(@PathVariable Integer id) {
        return seaService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            SpecialEquipmentAssignment sea = new SpecialEquipmentAssignment();
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                sea.setDate(java.sql.Date.valueOf(dateStr));
            }
            Date now = new Date();
            sea.setCreatedAt(now);
            sea.setUpdatedAt(now);
            sea.setCreatedBy(authenticationFacade.getLoggedIn());
            SpecialEquipmentAssignment saved = seaRepo.save(sea);
            response.setSuccessMessage("Special Equipment Assignment saved.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to save: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) { response.setFailureMessage("ID is required."); return response; }
        Integer id = ((Number) idObj).intValue();
        SpecialEquipmentAssignment sea = seaRepo.findById(id).orElse(null);
        if (sea == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                sea.setDate(java.sql.Date.valueOf(dateStr));
            }
            sea.setUpdatedAt(new Date());
            SpecialEquipmentAssignment saved = seaRepo.save(sea);
            response.setSuccessMessage("Special Equipment Assignment updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Process not supported for Special Equipment Assignment.");
        return response;
    }
}
