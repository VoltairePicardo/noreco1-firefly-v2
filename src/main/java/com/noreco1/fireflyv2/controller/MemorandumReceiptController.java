package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.MemorandumReceiptDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MemorandumReceiptService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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

import java.util.*;

@RestController
@RequestMapping("/api/memorandum-receipt")
public class MemorandumReceiptController {

    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;
    private final MemorandumReceiptService memorandumReceiptService;
    private final MessageSource messageSource;

    public MemorandumReceiptController(@Qualifier("memorandumReceiptServiceImpl") PrintableVoucher printableVoucher, DownloadService downloadService, MemorandumReceiptService memorandumReceiptService, MessageSource messageSource) {
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
        this.memorandumReceiptService = memorandumReceiptService;
        this.messageSource = messageSource;
    }

    @GetMapping(value = "/list-paged")
    @ResponseBody
    public Page<Map<String, Object>> listPaged(@RequestParam String from, @RequestParam String to,
                                               @RequestParam(value = "em", required = false) Integer employeeAccountNo,
                                               @RequestParam(value = "q", required = false) String query,
                                               Pageable pageable) {
        return memorandumReceiptService.getMemorandumReceiptPaged(from, to, employeeAccountNo, query, pageable);
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public MemorandumReceiptDto get(@PathVariable Integer id) {
        return memorandumReceiptService.findById(id);
    }


    @PostMapping(value = "/create")
    @ResponseBody
    public PostResponse create(@Valid @RequestBody MemorandumReceipt memorandumReceipt, BindingResult bindingResult) {
        return memorandumReceiptService.create(memorandumReceipt, bindingResult, messageSource);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public PostResponse update(@Valid @RequestBody MemorandumReceipt memorandumReceipt, BindingResult bindingResult, HttpServletRequest request) {
        return memorandumReceiptService.update(memorandumReceipt, bindingResult, messageSource);
    }

    @GetMapping(value = "/stock-withdrawal-balance/{id}")
    @ResponseBody
    public List<Map> getAvailableItemStock(@PathVariable Integer id) {
        return memorandumReceiptService.getStockWithdrawalBalance(id);
    }


    @GetMapping(value = "/stock-withdrawal-employees/{id}")
    @ResponseBody
    public ArrayList<SlEntity> getStockWithdrawalEmployees(@PathVariable Integer id) {
        return memorandumReceiptService.getStockWithdrawalEmployees(id);
    }

    @PostMapping(value = "/create-multiple")
    @ResponseBody
    public PostResponse createMultiple(@Valid @RequestBody List<MemorandumReceipt> memorandumReceipts, BindingResult bindingResult) {
        return memorandumReceiptService.createMultiple(memorandumReceipts, bindingResult, messageSource);
    }

    @PostMapping(value = "/create-returned-mr")
    @ResponseBody
    public PostResponse createMultiple(@Valid @RequestBody MemorandumReceipt memorandumReceipt, BindingResult bindingResult) {
        return memorandumReceiptService.createReturnedMR(memorandumReceipt, bindingResult, messageSource);
    }

    @PostMapping(value = "/process")
    @ResponseBody
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return this.memorandumReceiptService.process(postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/default-signatories")
    @ResponseBody
    public Map defaultSignatories() {
        return this.memorandumReceiptService.defaultSignatories();
    }

    @GetMapping(value = "/employee/{accountNo}/{forEditing}")
    @ResponseBody
    public ArrayList<MemorandumReceipt> getAllEmployeesMemorandumReceipt(@PathVariable Integer accountNo, @PathVariable Boolean forEditing) {
        return memorandumReceiptService.getAllEmployeesMemorandumReceipt(accountNo, forEditing);
    }

    @GetMapping(value = "/office/{officeId}/{forEditing}")
    @ResponseBody
    public ArrayList<MemorandumReceipt> getAllOfficesMemorandumReceipt(@PathVariable Integer officeId, @PathVariable Boolean forEditing) {
        return memorandumReceiptService.getAllOfficesMemorandumReceipt(officeId, forEditing);
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/MemorandumReceipt.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
