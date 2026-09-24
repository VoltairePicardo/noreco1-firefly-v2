package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.MeterTestingResultDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MeterTesting;
import com.noreco1.fireflyv2.service.MeterTestingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meter-testing")
public class MeterTestingController {

    private final MeterTestingService meterTestingService;
    private final MessageSource messageSource;

    @PostMapping("/upload")
    public MeterTestingResultDto upload(@RequestParam("file") MultipartFile file) {
        return meterTestingService.extractMeterTestingData(file);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MeterTesting meterTesting, BindingResult bindingResult) {
        return meterTestingService.create(meterTesting, bindingResult, messageSource);
    }

    @GetMapping("/list")
    public Page<MeterTesting> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return meterTestingService.findAll(pageable);
        }
        return meterTestingService.find(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeterTesting> getById(@PathVariable Integer id) {
        MeterTesting meterTesting = meterTestingService.findById(id);
        return meterTesting != null ? ResponseEntity.ok(meterTesting) : ResponseEntity.notFound().build();
    }

}
