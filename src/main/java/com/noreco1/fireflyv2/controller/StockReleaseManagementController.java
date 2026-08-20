package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReleaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * Created by lenovo on 5/4/2017.
 */

@Controller
@RequestMapping(value = "/inventory/releasing")
public class StockReleaseManagementController {

    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;
    private final StockReleaseService stockReleaseService;

    private final String BASE_PATH = "inventory/releasing/partials/";
    private final String MAIN = "inventory/releasing/main";

    public StockReleaseManagementController(@Qualifier("stockReleaseServiceImpl") PrintableVoucher printableVoucher, DownloadService downloadService, StockReleaseService stockReleaseService) {
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
        this.stockReleaseService = stockReleaseService;
    }


    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return MAIN;
    }

    @RequestMapping(value = "/inner-main", method = RequestMethod.GET)
    public String innerMain() {
        return "inventory/releasing/main-inner";
    }

    @RequestMapping(value = "/new-releasing-page", method = RequestMethod.GET)
    public String newRelease(HttpServletRequest request) {
        return BASE_PATH + "add-edit";
    }

    @RequestMapping(value = "/releasing-details-page", method = RequestMethod.GET)
    public String details(HttpServletRequest request) {
        return BASE_PATH + "details";
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        String srType = request.getParameter("of");

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.stockReleaseService.getReportMeta());   // items with serial no


        String jrxml = (srType == null) ? "StockReleaseMCT" : srType.equals("1") ? "StockRelease" : "StockReleaseTransfer";

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/"+jrxml+".jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
