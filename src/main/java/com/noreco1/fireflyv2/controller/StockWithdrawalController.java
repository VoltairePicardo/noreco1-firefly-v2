package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/withdrawal")
public class StockWithdrawalController {

    private final StockWithdrawalService withdrawalService;
    private final MessageSource messageSource;
    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;

    public StockWithdrawalController(@Qualifier("withdrawalServiceImpl") StockWithdrawalService withdrawalService, MessageSource messageSource, @Qualifier("withdrawalServiceImpl") PrintableVoucher printableVoucher, DownloadService downloadService) {
        this.withdrawalService = withdrawalService;
        this.messageSource = messageSource;
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
    }

    @GetMapping("/list")
    public List<Map> list() {
        return withdrawalService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return withdrawalService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return withdrawalService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return withdrawalService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public StockWithdrawal getById(@PathVariable Integer id) {
        return withdrawalService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        return withdrawalService.create(payload);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        return withdrawalService.update(payload);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return withdrawalService.process(dto, br, messageSource);
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockWithdrawal.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

}
