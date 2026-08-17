package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.repo.CheckConfigRepo;
import com.noreco1.fireflyv2.controller.response.reports.CheckDto;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.*;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/check-voucher")
public class CheckVoucherController {

    @Autowired
    @Qualifier("cvServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("cvServiceImpl")
    PrintableCheque printableCheque;

    @Autowired
    @Qualifier("cvServiceImpl")
    Bir2307 bir2307;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    CheckConfigRepo checkConfigRepo;

    @Autowired
    CvService cvService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CheckVoucher.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value="/print-check-jas/{transId}/{bankAccountId}")
    public void printCheckJasper(@PathVariable Integer transId, @PathVariable Integer bankAccountId,
                           @RequestParam(value = "type") String type,
                           @RequestParam(value = "token") String token,
                           HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableCheque.reportParameters(transId, bankAccountId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CheckForPrint.jrxml";
        downloadService.download(type, token, response, params, template, null);
    }

    @RequestMapping(value="/print-2307/{accountNo}/{transId}")
    public void print2307(@PathVariable Integer transId, @PathVariable Integer accountNo,
                          @RequestParam(value = "token") String token,
                          HttpServletResponse response, HttpServletRequest request) {

        bir2307.fillPdf(transId, accountNo, token, response);
    }

    @RequestMapping(value="/print-2307-sub-suppliers/{transId}")
    public void print2307(@PathVariable Integer transId,
                          @RequestParam(value = "token") String token,
                          HttpServletResponse response, HttpServletRequest request) {
        bir2307.fillPdfMultiple(transId, token, response);
    }
}
