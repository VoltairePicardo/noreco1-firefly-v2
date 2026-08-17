package com.noreco1.fireflyv2.service;

import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

public interface Bir1601E extends Printable {

    @Transactional(readOnly = true)
    void fillPdf(Integer year, Integer month, String token, HttpServletResponse response);

    @Transactional(readOnly = true)
    HashMap reportParametersForSchedule(Integer year, Integer month, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForSchedule(Integer year, Integer month);
}
