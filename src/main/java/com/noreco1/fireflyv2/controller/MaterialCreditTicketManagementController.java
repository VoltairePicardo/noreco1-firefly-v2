package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MaterialCreditTicketService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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

@Controller
@RequestMapping(value = "/api/inventory/mct-mgt")
public class MaterialCreditTicketManagementController {

    @Autowired
    @Qualifier("materialCreditTicketServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private MaterialCreditTicketService materialCreditTicketService;

    private final String BASE_PATH = "inventory/mct/partials/";
    private final String MAIN = "inventory/mct/main";

    // view providers
    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return MAIN;
    }

    @RequestMapping(value = "/inner-main", method = RequestMethod.GET)
    public String innerMain() {
        return "inventory/mct/main-inner";
    }

    @RequestMapping(value = "/new-mct-page", method = RequestMethod.GET)
    public String newRelease(HttpServletRequest request) {
        return BASE_PATH + "add-edit";
    }

    @RequestMapping(value = "/mct-details-page", method = RequestMethod.GET)
    public String details(HttpServletRequest request) {
        return BASE_PATH + "details";
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {


        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.materialCreditTicketService.getReportMeta());

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/MaterialCreditTicket.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
