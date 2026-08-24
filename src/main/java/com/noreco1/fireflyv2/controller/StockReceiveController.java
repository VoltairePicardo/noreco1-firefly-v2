package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.repo.ItemTransactionDetailRepo;
import com.noreco1.fireflyv2.repo.StockTransferRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReceiveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-receive")
public class StockReceiveController {

    @Autowired
    @Qualifier("stockReceiveServiceImpl")
    private StockReceiveService stockReceiveService;

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private StockTransferRepo stockTransferRepo;

    @Autowired
    private ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return stockReceiveService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return stockReceiveService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return stockReceiveService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return stockReceiveService.getDocumentsStatuses();
    }

    @GetMapping("/inventory-locations")
    public List<InventoryLocation> inventoryLocations() {
        return inventoryLocationRepo.findAllByOrderByDescriptionAsc();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return stockReceiveService.defaultSignatories();
    }

    @GetMapping("/receiving-documents/{locationId}")
    public Page<StockTransfer> receivingDocuments(
            @PathVariable Integer locationId,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Integer approved = com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId();
        Page<StockTransfer> result = q.isBlank()
            ? stockTransferRepo.findForReceiving(approved, locationId, pageable)
            : stockTransferRepo.findForReceivingByQuery("%" + q + "%", locationId, approved, pageable);
        for (StockTransfer st : result.getContent()) {
            if (st.getTransaction() != null) {
                ArrayList<ItemTransactionDetail> items =
                    itemTransactionDetailRepo.findByTransactionId(st.getTransaction().getId());
                ArrayList<ItemTransactionDetailDto> dtos = new ArrayList<>();
                for (ItemTransactionDetail d : items) {
                    ItemTransactionDetailDto dto = d.toReceiveDto();
                    dto.setQuantityOrdered(d.getQuantity());
                    dtos.add(dto);
                }
                st.setDetails(dtos);
            }
        }
        return result;
    }

    @GetMapping("/{id}")
    public StockReceive getById(@PathVariable Integer id) {
        return stockReceiveService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody StockReceive stockReceive) {
        BindingResult br = new BeanPropertyBindingResult(stockReceive, "stockReceive");
        return stockReceiveService.processCreate(stockReceive, br, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody StockReceive stockReceive) {
        BindingResult br = new BeanPropertyBindingResult(stockReceive, "stockReceive");
        return stockReceiveService.processUpdate(stockReceive, br, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return stockReceiveService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("stockReceiveServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockReceive.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
