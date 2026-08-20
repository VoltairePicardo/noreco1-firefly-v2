package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockRelease;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.resource.InventoryDocumentResource;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReleaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-release")
public class StockReleaseController {

    private final StockReleaseService stockReleaseService;
    private final MessageSource messageSource;
    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;

    public StockReleaseController(@Qualifier("stockReleaseServiceImpl") StockReleaseService stockReleaseService,
                                   MessageSource messageSource,
                                   @Qualifier("stockReleaseServiceImpl") PrintableVoucher printableVoucher,
                                   DownloadService downloadService) {
        this.stockReleaseService = stockReleaseService;
        this.messageSource = messageSource;
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
    }

    @GetMapping("/list-paged")
    public Page<Map<String, Object>> listPaged(@RequestParam String from, @RequestParam String to,
                                                @RequestParam(required = false) Integer statusId,
                                                @RequestParam(required = false) String query,
                                                Pageable pageable) {
        return stockReleaseService.getStockReleasePaged(from, to, statusId, query, pageable);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return stockReleaseService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return stockReleaseService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return stockReleaseService.defaultSignatories();
    }

    @GetMapping("/withdrawal-documents")
    public List<StockWithdrawal> withdrawalDocuments() {
        return stockReleaseService.findStockWithdrawalByDocumentStatusId(
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
    }

    @GetMapping("/{id}")
    public StockRelease getById(@PathVariable Integer id) {
        return stockReleaseService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@Valid @RequestBody StockRelease release, BindingResult bindingResult) {
        return stockReleaseService.processCreate(release, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@Valid @RequestBody StockRelease release, BindingResult bindingResult) {
        return stockReleaseService.processUpdate(release, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return stockReleaseService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                             @RequestParam(value = "type") String type,
                             @RequestParam(value = "token") String token,
                             HttpServletResponse response, HttpServletRequest request) {

        String srType = request.getParameter("of");

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(stockReleaseService.getReportMeta());   // items with serial no

        String jrxml = (srType == null) ? "StockReleaseMCT" : srType.equals("1") ? "StockRelease" : "StockReleaseTransfer";

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/" + jrxml + ".jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value = "/documents")
    public ResponseEntity<PagedModel<InventoryDocumentResource>> getInventoryDocuments(Pageable pageable, PagedResourcesAssembler<InventoryDocumentDto> assembler,
                                                                                       @RequestParam(value = "q", required = false) String query,
                                                                                       @RequestParam(value = "t", required = false) String type) {

        return stockReleaseService.findInventoryDocumentsForReleasing(type, query, pageable)
                .map(documents -> ResponseEntity.ok(assembler.toModel(documents, InventoryDocumentResource::new)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
