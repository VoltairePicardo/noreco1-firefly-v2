package com.noreco1.fireflyv2.service;

import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.transaction.annotation.Transactional;

public interface PrintableBudget extends PrintableVoucher {
    @Transactional(readOnly = true)
    public JRDataSource datasource(Integer vid, Integer year);
}
