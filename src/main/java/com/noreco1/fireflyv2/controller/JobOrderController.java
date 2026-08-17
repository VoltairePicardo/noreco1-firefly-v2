package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.JoDetailDto;
import com.noreco1.fireflyv2.controller.response.JoDto;
import com.noreco1.fireflyv2.controller.response.JoListDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.JobOrder;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.JoDetailService;
import com.noreco1.fireflyv2.service.JobOrderService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-order")
public class JobOrderController {

    @Autowired
    @Qualifier("joServiceImpl")
    private JobOrderService jobOrderService;

    @Autowired
    private JoDetailService joDetailService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return jobOrderService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return jobOrderService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return jobOrderService.getDocumentsStatuses();
    }

    /** Returns vendors that have APPROVED Job Orders with remaining acceptance. */
    @GetMapping("/approved-vendors")
    public List<Map> approvedVendors() {
        // statusId param is ignored by impl — always uses APPROVED status internally
        return jobOrderService.findJobOrderSuppliersByStatus(0);
    }

    /** Returns APPROVED Job Orders for a specific supplier (for JOA creation). */
    @GetMapping("/by-supplier/{accountNo}")
    public List<JoListDto> bySupplier(@PathVariable Integer accountNo) {
        return jobOrderService.findBySupplierAccountNo(accountNo);
    }

    /** Returns all line items for a given JO. */
    @GetMapping("/detail/{joId}")
    public List<JoDetailDto> detail(@PathVariable Integer joId) {
        return joDetailService.getJoDetails(joId);
    }

    /** Returns only non-fully-accepted line items (for JOA creation). */
    @GetMapping("/detail-for-joa/{joId}")
    public List<JoDetailDto> detailForJoa(@PathVariable Integer joId) {
        return joDetailService.getJoDetailsForJoa(joId);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody JobOrder jobOrder, HttpServletRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(jobOrder, "jobOrder");
        return jobOrderService.processCreate(jobOrder, bindingResult, messageSource, request);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody JobOrder jobOrder, HttpServletRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(jobOrder, "jobOrder");
        return jobOrderService.processUpdate(jobOrder, bindingResult, messageSource, request, Collections.emptyList());
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return jobOrderService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public JoDto getById(@PathVariable Integer id) {
        return jobOrderService.findById(id);
    }

    @Autowired
    @Qualifier("joServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/JobOrder.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

}
