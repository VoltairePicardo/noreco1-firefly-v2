package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.SubLedgerEntity;
import com.noreco1.fireflyv2.repo.SLEntityClassificationRepo;
import com.noreco1.fireflyv2.repo.SubLedgerEntityRepo;
import com.noreco1.fireflyv2.service.SlEntityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sl-entity")
public class SubLedgerEntityController {

    @Autowired
    SlEntityService slEntityService;

    @Autowired
    SubLedgerEntityRepo subLedgerEntityRepo;

    @Autowired
    SLEntityClassificationRepo slEntityClassificationRepo;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public Page<SubLedgerEntity> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String classification,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (classification != null && !classification.isEmpty()) {
            return slEntityService.findStrongByClassificationQuery(classification, q.isEmpty() ? null : q, pageable);
        }
        if (q == null || q.isEmpty()) {
            return slEntityService.findAllStrong(pageable);
        }
        return slEntityService.findStrongByQuery(q, pageable);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        PostResponse response = new PostResponse();
        try {
            SubLedgerEntity entity = subLedgerEntityRepo.findById(id).orElse(null);
            if (entity == null) {
                response.setFailureMessage("SL Entity not found.");
                return response;
            }
            subLedgerEntityRepo.deleteById(id);
            response.setSuccessMessage("SL Entity successfully deleted.");
        } catch (Exception e) {
            response.setFailureMessage("Unable to delete SL Entity. It may be referenced by other records.");
        }
        return response;
    }

    @GetMapping("/{id}")
    public SubLedgerEntity getById(@PathVariable Integer id) {
        return subLedgerEntityRepo.findById(id).orElse(null);
    }

    @GetMapping("/classifications")
    public List<SLEntityClassification> classifications() {
        return slEntityClassificationRepo.findAll();
    }

    @PostMapping("/create")
    public PostResponse create(@Valid @RequestBody SubLedgerEntity entity, BindingResult bindingResult) {
        return slEntityService.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@Valid @RequestBody SubLedgerEntity entity, BindingResult bindingResult) {
        return slEntityService.processUpdate(entity, bindingResult, messageSource);
    }
}
