package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PayReqDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.PaymentRequest;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PaymentRequestService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-request")
public class PaymentRequestController {

    @Autowired
    @Qualifier("paymentRequestServiceImpl")
    private PaymentRequestService service;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listPending(@PathVariable String from, @PathVariable String to) {
        return service.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return service.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return service.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody PaymentRequest pr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pr, "paymentRequest");
        PostResponse response = service.processCreate(pr, bindingResult, messageSource);
        if (response.isSuccess()) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody PaymentRequest pr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pr, "paymentRequest");
        PostResponse response = service.processUpdate(pr, bindingResult, messageSource);
        if (response.isSuccess()) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return service.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public PayReqDto getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Autowired
    @Qualifier("paymentRequestServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PaymentRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
