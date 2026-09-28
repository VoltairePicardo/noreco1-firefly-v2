package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Meter;
import com.noreco1.fireflyv2.service.InitialReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/initial-reading")
public class InitialReadingController {

    private final InitialReadingService initialReadingService;
    private final MessageSource messageSource;

    @GetMapping("/list")
    public Page<Meter> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return initialReadingService.list(q, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meter> getById(@PathVariable Integer id) {
        Meter meter = initialReadingService.getById(id);
        return meter != null ? ResponseEntity.ok(meter) : ResponseEntity.notFound().build();
    }

    @GetMapping("/meter/{serialNo}")
    public Meter getMeterBySerialNo(@PathVariable String serialNo) {
        return initialReadingService.getMeterBySerialNo(serialNo);
    }

    @PostMapping("/save")
    public PostResponse save(@RequestBody Meter meter) {
        return initialReadingService.saveReading(meter, messageSource);
    }

}
