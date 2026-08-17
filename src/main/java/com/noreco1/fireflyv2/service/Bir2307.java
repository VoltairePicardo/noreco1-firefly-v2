package com.noreco1.fireflyv2.service;

import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;

public interface Bir2307 extends Printable {

    @Transactional(readOnly = true)
    void fillPdf(Integer transId, Integer payeeAccountNo, String token, HttpServletResponse response);

    @Transactional(readOnly = true)
    void fillPdfMultiple(Integer transId, String token, HttpServletResponse response);

}
