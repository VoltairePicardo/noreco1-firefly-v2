package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableBudget;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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
@RequestMapping(value = "/budget-mgt")
public class BudgetController {

    @Autowired
    @Qualifier("budgetServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("budgetServiceImpl")
    PrintableBudget printableBudget;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value = "/export/{id}")
    public void exportToPdf(
            @PathVariable Integer id,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "token") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/BudgetVoucher.jrxml";

        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value = "/print/{id}")
    public void printToPdf(
            @PathVariable Integer id,
            @RequestParam(value = "year") Integer year,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "token") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableBudget.datasource(id, year);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/BudgetByYear.jrxml";

        downloadService.download(type, token, response, params, template, dataSource);
    }
}
