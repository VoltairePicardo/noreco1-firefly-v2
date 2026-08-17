package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableSummary;
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
@RequestMapping(value = "/petty-cash-voucher")
public class PettyCashVoucherController {

    @Autowired
    @Qualifier("pettyCashTransServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("pettyCashTransDetailServiceImpl")
    PrintableSummary printableSummary;

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
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PCVoucher.jrxml";

        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value = "/print/{checkedByAcctNo}/{replenishedByAcctNo}")
    public void printToPdf(
            @PathVariable Integer checkedByAcctNo,
            @PathVariable Integer replenishedByAcctNo,
            @RequestParam(value = "batch") Integer batch,
            @RequestParam(value = "from") String from,
            @RequestParam(value = "to") String to,
            @RequestParam(value = "documentStatusId", required = false) Integer documentStatusId,
            @RequestParam(value = "officeId", required = false) Integer officeId,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "token") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        HashMap params = printableSummary.reportParameters(request, checkedByAcctNo, replenishedByAcctNo, from, to, documentStatusId, officeId, batch);
        JRDataSource dataSource = printableSummary.datasource(batch, documentStatusId, officeId);
        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/PCVSummary.jrxml";

        downloadService.download(type, token, response, params, template, dataSource);
    }
}
