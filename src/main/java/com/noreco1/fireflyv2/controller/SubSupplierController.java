package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.SubSupplier;
import com.noreco1.fireflyv2.service.SubSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/sub-supplier")
public class SubSupplierController {

    @Autowired
    private SubSupplierService subSupplierService;

    @GetMapping("/list")
    public Page<SubSupplier> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer suppId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return subSupplierService.findAllForListing(q, suppId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubSupplier> getById(@PathVariable Integer id) {
        SubSupplier subSupplier = subSupplierService.findById(id);
        return subSupplier != null ? ResponseEntity.ok(subSupplier) : ResponseEntity.notFound().build();
    }

    @PostMapping("/upload")
    public PostResponse upload(@RequestParam("file") MultipartFile file,
                               @RequestParam("supplierId") Integer supplierId) {
        return subSupplierService.processUpload(file, supplierId);
    }
}
