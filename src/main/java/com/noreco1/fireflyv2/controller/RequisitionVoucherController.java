package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ModeOfProcurement;
import com.noreco1.fireflyv2.model.PurchaseRequest;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.PurchaseRequestService;
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
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/requisition-voucher")
public class RequisitionVoucherController {

    @Autowired
    @Qualifier("rvServiceImpl")
    private PurchaseRequestService purchaseRequestService;

    @Autowired
    @Qualifier("rvServiceImpl")
    private PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<RvListDto> list() {
        return purchaseRequestService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return purchaseRequestService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return purchaseRequestService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/list/canvass")
    public List<RvListDto> listForCanvass(@RequestParam Integer[] canvassIds) {
        return purchaseRequestService.getRequisitionVoucherForCanvass(canvassIds);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return purchaseRequestService.getDocumentsStatuses();
    }

    @GetMapping("/modes-of-procurement/{rvId}")
    public List<ModeOfProcurement> modesOfProcurement(@PathVariable int rvId) {
        return purchaseRequestService.modesOfProcurement(rvId);
    }

    @PostMapping("/set-mode-of-procurement")
    public PostResponse setModeOfProcurement(@RequestBody SetModeOfProcurementDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "setModeOfProcurementDto");
        return purchaseRequestService.setModeOfProcurement(dto, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return purchaseRequestService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public RvDto getById(@PathVariable Integer id) {
        return purchaseRequestService.findByRvId(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody PurchaseRequest purchaseRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(purchaseRequest, "purchaseRequest");
        PostResponse response = purchaseRequestService.processCreate(purchaseRequest, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            purchaseRequestService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody PurchaseRequest purchaseRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(purchaseRequest, "purchaseRequest");
        PostResponse response = purchaseRequestService.processUpdate(purchaseRequest, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            purchaseRequestService.logNewValue(response.getLogId());
        }
        return response;
    }

    // For PO (Purchase Request)
    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        exportRv(id, type, token, response, request);
    }

    // For IT (Internal Transfer)
    @GetMapping("/export1/{id}")
    public void exportToPdf1(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {
        exportRv(id, type, token, response, request);
    }

    // For REP (Repair/JO)
    @GetMapping("/export2/{id}")
    public void exportToPdf2(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {
        exportRv(id, type, token, response, request);
    }

    // For LAB (Labor)
    @GetMapping("/export3/{id}")
    public void exportToPdf3(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {
        exportRv(id, type, token, response, request);
    }

    private void exportRv(Integer id, String type, String token,
                          HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
