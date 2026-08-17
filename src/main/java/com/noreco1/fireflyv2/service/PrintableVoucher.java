package com.noreco1.fireflyv2.service;

import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface PrintableVoucher extends Printable {

    @Transactional(readOnly = true)
    public HashMap reportParameters(Integer vid, HttpServletRequest request);

    @Transactional(readOnly = true)
    public JRDataSource datasource(Integer vid);
}
