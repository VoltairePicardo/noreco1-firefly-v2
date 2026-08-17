package com.noreco1.fireflyv2.service;

import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface PrintableCheque extends PrintableVoucher {

    @Transactional(readOnly = true)
    public HashMap reportParameters(Integer transId, Integer bankAccountId);

}
