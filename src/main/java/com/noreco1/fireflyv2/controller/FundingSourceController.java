package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.FundingSource;
import com.noreco1.fireflyv2.service.FundingSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/funding-source")
public class FundingSourceController {

    @Autowired
    private FundingSourceService fundingSourceService;

    @GetMapping("/list")
    public Page<FundingSource> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return fundingSourceService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FundingSource> getById(@PathVariable Integer id) {
        FundingSource fs = fundingSourceService.findById(id);
        return fs != null ? ResponseEntity.ok(fs) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody FundingSource fundingSource) {
        return fundingSourceService.create(fundingSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody FundingSource fundingSource) {
        return fundingSourceService.update(fundingSource);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return fundingSourceService.deleteById(id);
    }
}
