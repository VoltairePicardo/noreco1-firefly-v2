package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/api/requisition-voucher")
public class PurchaseRequestController {

    @Autowired
    @Qualifier("rvServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value="/export1/{id}")
    public void exportToPdf1(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value="/export2/{id}")
    public void exportToPdf2(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value="/export3/{id}")
    public void exportToPdf3(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
