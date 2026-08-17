package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.repo.ItemTestingRepo;
import com.noreco1.fireflyv2.service.ItemTestingService;
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
@RequestMapping("/api/item-testing")
public class ItemTestingController {

    @Autowired
    @Qualifier("itemTestingServiceImpl")
    private ItemTestingService itemTestingService;

    @Autowired
    private ItemTestingRepo itemTestingRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @GetMapping("/list")
    public List<ItemTesting> list() {
        Page<ItemTesting> page = itemTestingService.findAll("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31",
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/list/{from}/{to}")
    public List<ItemTesting> listByDateRange(@PathVariable String from, @PathVariable String to) {
        Page<ItemTesting> page = itemTestingService.findAll(from, to,
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/{id}")
    public ItemTestingDto getById(@PathVariable Integer id) {
        return itemTestingService.findById(id);
    }

    @GetMapping("/details/{id}")
    public List<Map> getDetails(@PathVariable Integer id) {
        return itemTestingService.getItemTestingDetails(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ItemTesting it = new ItemTesting();
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                it.setDate(java.sql.Date.valueOf(dateStr));
            }
            Date now = new Date();
            it.setCreatedAt(now);
            it.setUpdatedAt(now);
            it.setCreatedBy(authenticationFacade.getLoggedIn());
            ItemTesting saved = itemTestingRepo.save(it);
            response.setSuccessMessage("Item Testing saved.");
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
        ItemTesting it = itemTestingRepo.findById(id).orElse(null);
        if (it == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                it.setDate(java.sql.Date.valueOf(dateStr));
            }
            it.setUpdatedAt(new Date());
            ItemTesting saved = itemTestingRepo.save(it);
            response.setSuccessMessage("Item Testing updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Process not supported for Item Testing.");
        return response;
    }

}
