package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockAdjustment;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockAdjustmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-adjustment")
public class StockAdjustmentController {

    private final MessageSource messageSource;
    private final StockAdjustmentService stockAdjustmentService;
    private final DownloadService downloadService;
    private final PrintableVoucher printableVoucher;

    public StockAdjustmentController(MessageSource messageSource, StockAdjustmentService stockAdjustmentService, DownloadService downloadService, @Qualifier("stockAdjustmentServiceImpl") PrintableVoucher printableVoucher) {
        this.messageSource = messageSource;
        this.stockAdjustmentService = stockAdjustmentService;
        this.downloadService = downloadService;
        this.printableVoucher = printableVoucher;
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public List<StockAdjustment> list() {
        return stockAdjustmentService.findAll();
    }

    @GetMapping(value = "/list-paged")
    @ResponseBody
    public Page<Map<String, Object>> listPaged(@RequestParam String from, @RequestParam String to,
                                                @RequestParam(required = false) Integer statusId,
                                                @RequestParam(required = false) String query,
                                                Pageable pageable) {
        return stockAdjustmentService.getStockAdjustmentPaged(from, to, statusId, query, pageable);
    }

    @GetMapping(value = "/list/{from}/{to}/{officeId}")
    @ResponseBody
    public List<Map> listByDateAndStatusPending(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId) {
        return stockAdjustmentService.findByDateRangePending(from, to, officeId);
    }

    @GetMapping(value = "/list/{from}/{to}/{status}/{officeId}")
    @ResponseBody
    public List<Map> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status, @PathVariable Integer officeId) {
        return stockAdjustmentService.findByDateRangeAndStatusId(from, to, status, officeId);
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public PostResponse create(@Valid @RequestBody StockAdjustment stockAdjustment, BindingResult bindingResult) {
        PostResponse response = stockAdjustmentService.processCreate(stockAdjustment, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            stockAdjustmentService.logNewValue(response.getLogId());
        }
        return response;
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public StockAdjustment get(@PathVariable Integer id, HttpServletRequest request) {
        return stockAdjustmentService.findById(id);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public PostResponse update(@Valid @RequestBody StockAdjustment stockAdjustment, BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = stockAdjustmentService.processUpdate(stockAdjustment, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            stockAdjustmentService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/process")
    @ResponseBody
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return stockAdjustmentService.process(postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/default-signatories")
    @ResponseBody
    public Map defaultSignatories() {
        return stockAdjustmentService.defaultSignatories();
    }

    @GetMapping(value = "/document-statuses")
    @ResponseBody
    public List<DocumentStatus> getWorkflowActions() {
        return stockAdjustmentService.getDocumentsStatuses();
    }

    @GetMapping(value = "/summary/{from}/{to}")
    @ResponseBody
    public List<StockAdjustment> listForSummaryReport(@PathVariable String from, @PathVariable String to, HttpServletRequest request) {
        return stockAdjustmentService.getListForSummaryReport(from, to, request);
    }

    @GetMapping(value = "/items/{transId}")
    @ResponseBody
    public List<ItemTransactionDetailDto> itemsPerTransactionId(@PathVariable Integer transId) {
        return stockAdjustmentService.getItems(transId);
    }


    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {


        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockAdjustment.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

//    @RequestMapping(value = "/approved-paged", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<StockAdjustmentDocumentResource>> approvedListForStockAdjustmentPaged(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                                    @RequestParam(value="q", required = false ) String query) {
//
//        Page<StockAdjustmentDocumentDto> documents = stockAdjustmentService.findAllApprovedForAccountSettingPaged(query, pageable);
//        return new ResponseEntity<PagedResources<StockAdjustmentDocumentResource>>(assembler.toResource(documents), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/for-jv-approved-paged", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<StockAdjustmentDocumentResource>> approvedListForJVPaged(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                       @RequestParam(value="q", required = false ) String query) {
//
//        Page<StockAdjustmentDocumentDto> documents = stockAdjustmentService.findAllApprovedForJVPaged(query, pageable);
//        return new ResponseEntity<PagedResources<StockAdjustmentDocumentResource>>(assembler.toResource(documents), HttpStatus.OK);
//    }
}
