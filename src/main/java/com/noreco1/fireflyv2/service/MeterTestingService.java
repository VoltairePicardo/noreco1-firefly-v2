package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.MeterTestingResultDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MeterTesting;
import com.noreco1.fireflyv2.model.MeterTestingOption;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

public interface MeterTestingService {

    MeterTestingResultDto extractMeterTestingData(MultipartFile excelFile);

    PostResponse create(MeterTesting meterTesting, BindingResult bindingResult, MessageSource messageSource);
    PostResponse createIndividual(MeterTesting meterTesting, BindingResult bindingResult, MessageSource messageSource);

    Page<MeterTesting> findAll(Pageable pageable);
    Page<MeterTesting> find(String testedBy, Pageable pageable);
    MeterTesting findById(Integer id);
    List<MeterTestingOption> findActiveOptions();

    HashMap<String, Object> meterTestingParameters(HttpServletRequest request, Integer id);
    JRDataSource datasourceMeterTesting(Integer id);
}
