package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Meter;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InitialReadingService {
    Page<Meter> list(String q, Pageable pageable);
    Meter getById(Integer id);
    Meter getMeterBySerialNo(String serialNo);
    PostResponse saveReading(Meter meter, MessageSource messageSource);
}
