package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.ItemStock;
import com.noreco1.fireflyv2.model.StockTransfer;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.repo.ItemStockRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockTransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-transfer")
public class StockTransferController {

    @Autowired
    @Qualifier("stockTransferServiceImpl")
    private StockTransferService stockTransferService;

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private ItemStockRepo itemStockRepo;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return stockTransferService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return stockTransferService.findByDateRangeAll(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return stockTransferService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return stockTransferService.getDocumentsStatuses();
    }

    @GetMapping("/inventory-locations")
    public List<InventoryLocation> inventoryLocations() {
        return inventoryLocationRepo.findAllByOrderByDescriptionAsc();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return stockTransferService.defaultSignatories();
    }

    @GetMapping("/item-stocks/{locationId}")
    public List<ItemStock> itemStocks(@PathVariable Integer locationId) {
        return itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(locationId, BigDecimal.ZERO);
    }

    @GetMapping("/{id}")
    public StockTransfer getById(@PathVariable Integer id) {
        return stockTransferService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody StockTransfer stockTransfer) {
        BindingResult br = new BeanPropertyBindingResult(stockTransfer, "stockTransfer");
        return stockTransferService.processCreate(stockTransfer, br, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody StockTransfer stockTransfer) {
        BindingResult br = new BeanPropertyBindingResult(stockTransfer, "stockTransfer");
        return stockTransferService.processUpdate(stockTransfer, br, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return stockTransferService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("stockTransferServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockTransfer.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
