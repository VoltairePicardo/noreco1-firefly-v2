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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list-paged")
    public Page<Map<String, Object>> listPaged(@RequestParam String from, @RequestParam String to,
                                                @RequestParam(required = false) Integer statusId,
                                                @RequestParam(required = false) String query,
                                                Pageable pageable) {
        return withdrawalService.getStockWithdrawalPaged(from, to, statusId, query, pageable);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return withdrawalService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return withdrawalService.defaultSignatories();
    }

    @GetMapping("/{id}")
    public StockWithdrawal getById(@PathVariable Integer id) {
        return withdrawalService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody StockWithdrawal withdrawal) {
        BindingResult bindingResult = new BeanPropertyBindingResult(withdrawal, "withdrawal");
        return withdrawalService.processCreate(withdrawal, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody StockWithdrawal withdrawal) {
        BindingResult bindingResult = new BeanPropertyBindingResult(withdrawal, "withdrawal");
        return withdrawalService.processUpdate(withdrawal, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return withdrawalService.process(dto, br, messageSource);
    }


    @GetMapping("/memorandum-receipt-vouchers")
    public Page<StockWithdrawal> getMemorandumReceiptVouchers(@RequestParam(value = "q", required = false) String query,
                                                              @RequestParam(value = "f", required = false) Boolean multipleEmployee,
                                                              Pageable pageable) {

        return withdrawalService.getMemorandumReceiptVouchers(query, multipleEmployee, pageable);
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
