package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.repo.WorkOrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    private final String PARTIALS_PATH = "work-order/partials/";

    @GetMapping("/list")
    public Page<WorkOrder> list(
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Use findAll(Pageable) from JpaRepository - filters are applied post-query if needed
        // For basic operation, return all with pagination
        List<WorkOrder> all = workOrderRepo.findAll(Sort.by("id").descending());

        // Apply simple filters in memory
        if (statusId != null) {
            boolean closed = statusId == 1;
            all = all.stream().filter(w -> Boolean.TRUE.equals(w.getIsClosed()) == closed).toList();
        }
        if (year != null) {
            try {
                int y = Integer.parseInt(year);
                all = all.stream().filter(w -> w.getYear() != null && w.getYear() == y).toList();
            } catch (NumberFormatException ignored) {}
        }
        if (month != null) {
            try {
                int m = Integer.parseInt(month);
                all = all.stream().filter(w -> w.getMonth() != null && w.getMonth() == m).toList();
            } catch (NumberFormatException ignored) {}
        }

        int total = all.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<WorkOrder> content = start > total ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(content, pageable, total);
    }

    @GetMapping("/{id}")
    public WorkOrder getById(@PathVariable Integer id) {
        return workOrderRepo.findById(id).orElse(null);
    }

    @GetMapping("/document-statuses")
    public List<?> documentStatuses() {
        return Collections.emptyList();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            WorkOrder wo = buildWorkOrder(payload, null);
            WorkOrder saved = workOrderRepo.save(wo);
            if (saved != null) {
                response.setSuccessMessage("Work Order successfully created.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to create Work Order.");
            }
        } catch (Exception e) {
            response.setFailureMessage("Failed to create Work Order: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) {
            response.setFailureMessage("ID is required for update.");
            return response;
        }
        Integer id = ((Number) idObj).intValue();
        WorkOrder existing = workOrderRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Work Order not found.");
            return response;
        }

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try {
                Date d = java.sql.Date.valueOf(dateStr);
                existing.setDate(d);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                existing.setYear(cal.get(Calendar.YEAR));
                existing.setMonth(cal.get(Calendar.MONTH));
            } catch (Exception ignored) {}
        }

        if (payload.get("description") != null) existing.setDescription((String) payload.get("description"));

        String typeStr = (String) payload.get("type");
        if (typeStr != null) existing.setType(mapWorkOrderType(typeStr));

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            existing.setProject(p);
        }

        existing.setUpdatedAt(new Date());
        WorkOrder saved = workOrderRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Work Order successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Work Order.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Work Order processing is not supported in this version.");
        return response;
    }

    private WorkOrder buildWorkOrder(Map<String, Object> payload, Integer id) {
        WorkOrder wo = new WorkOrder();
        if (id != null) wo.setId(id);

        Date now = new Date();

        Object dateObj = payload.get("date");
        Date woDate = now;
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try {
                woDate = java.sql.Date.valueOf(dateStr);
            } catch (Exception ignored) {}
        }
        wo.setDate(woDate);
        wo.setTargetDate(woDate);  // required NOT NULL

        Calendar cal = Calendar.getInstance();
        cal.setTime(woDate);
        wo.setYear(cal.get(Calendar.YEAR));
        wo.setMonth(cal.get(Calendar.MONTH));

        if (payload.get("description") != null) wo.setDescription((String) payload.get("description"));

        String typeStr = (String) payload.get("type");
        wo.setType(mapWorkOrderType(typeStr));

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            wo.setProject(p);
        }

        // Generate code
        Object latestCode = workOrderRepo.findLatestVvCodeByYear(cal.get(Calendar.YEAR));
        String code = generatorFacade.voucherCodeNoOffice(
                "WO", latestCode == null ? "" : String.valueOf(latestCode),
                woDate, GlobalConstant.COUNTER_PAD_4);
        wo.setCode(code);

        // Account number
        wo.setAccountNumber(generatorFacade.entityAccountNumber());

        // Required audit fields
        wo.setCreatedAt(now);
        wo.setUpdatedAt(now);
        wo.setCreatedBy(authenticationFacade.getLoggedIn());
        wo.setIsClosed(false);

        // SLEntityClassification required
        SLEntityClassification slClass = new SLEntityClassification();
        slClass.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.WORK_ORDER.getId());
        wo.setSlEntityClassification(slClass);

        return wo;
    }

    private Integer mapWorkOrderType(String typeStr) {
        if (typeStr == null) return null;
        return switch (typeStr.toUpperCase()) {
            case "LABOR" -> 1;
            case "MATERIALS" -> 2;
            case "BOTH" -> 3;
            default -> null;
        };
    }
}
