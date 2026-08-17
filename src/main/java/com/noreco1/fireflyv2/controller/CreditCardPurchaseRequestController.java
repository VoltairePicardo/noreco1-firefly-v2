package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.CreditCardPurchaseRequest;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestBatchDto;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.CreditCardPurchaseRequestService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/credit-card-purchase-request")
public class CreditCardPurchaseRequestController {

    @Autowired
    @Qualifier("creditCardPurchaseRequestServiceImpl")
    private CreditCardPurchaseRequestService service;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{from}/{to}")
    public List<CreditCardPurchaseRequest> list(@PathVariable String from, @PathVariable String to) {
        return service.findByDateRangeAndStatusId(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<CreditCardPurchaseRequest> listByStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return service.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> getDocumentStatuses() {
        return service.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody CreditCardPurchaseRequest ccpr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(ccpr, "creditCardPurchaseRequest");
        PostResponse response = service.processCreate(ccpr, bindingResult, messageSource);
        if (response.isSuccess()) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody CreditCardPurchaseRequest ccpr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(ccpr, "creditCardPurchaseRequest");
        PostResponse response = service.processUpdate(ccpr, bindingResult, messageSource);
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

    @PostMapping("/create-batch")
    public PostResponse createBatch(@RequestBody CreditCardPurchaseRequestBatchDto dto) {
        return service.createBatch(dto);
    }

    @GetMapping("/{id}")
    public CreditCardPurchaseRequestDto getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Autowired
    @Qualifier("creditCardPurchaseRequestServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CreditCardPurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

}
