package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.IEMOPBilling;
import com.noreco1.fireflyv2.service.IEMOPBillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/iemop-billing")
public class IEMOPBillingController {

    @Autowired
    private IEMOPBillingService iemopBillingService;

    @GetMapping("/list")
    public Page<IEMOPBilling> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return iemopBillingService.findAllForListing(q, pageable);
    }

    @GetMapping("/{id}")
    public IEMOPBilling getData(@PathVariable Integer id) {
        return iemopBillingService.findById(id);
    }

    @PostMapping("/upload")
    public PostResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String date,
            @RequestParam String referenceNumber) {
        return iemopBillingService.processUpload(file, date, referenceNumber);
    }
}
