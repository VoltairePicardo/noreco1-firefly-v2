package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.mssql_model.ConsumerMeter;
import com.noreco1.fireflyv2.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

    @GetMapping("/list")
    public Page<ConsumerMeter> getAllConsumerFromIBCMS(@RequestParam(required = false, defaultValue = "") String query,
                                                       Pageable pageable) {
        return consumerService.findAllConsumerFromIBCMS(query, pageable);
    }
}
