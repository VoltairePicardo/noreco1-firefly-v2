package com.noreco1.fireflyv2.service;

import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface PrintableSummary extends Printable {
    HashMap reportParameters(HttpServletRequest request, Integer checkedByAcctNo, Integer replenishedByAcctNo,String from, String to, Integer documentStatusId, Integer officeId, Integer batchId);

    @Transactional(readOnly = true)
    JRDataSource datasource(Short year);

    @Transactional(readOnly = true)
    JRDataSource datasource(Integer batch, Integer documentStatusId, Integer officeId);
}
