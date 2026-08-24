package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.CostEstimateDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.ItemStock;
import com.noreco1.fireflyv2.model.Purpose;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.InventoryCategoryRepo;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.repo.ItemStockRepo;
import com.noreco1.fireflyv2.repo.PurposeRepo;
import com.noreco1.fireflyv2.repo.StockWithdrawalRepo;
import com.noreco1.fireflyv2.service.CostEstimateService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.PurchaseRequestDetailService;
import com.noreco1.fireflyv2.service.StockWithdrawalService;
import com.noreco1.fireflyv2.service.WorkOrderService;
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
@RequestMapping("/api/withdrawal")
public class StockWithdrawalController {

    @Autowired
    @Qualifier("withdrawalServiceImpl")
    private StockWithdrawalService withdrawalService;

    @Autowired
    private StockWithdrawalRepo withdrawalRepo;

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private InventoryCategoryRepo inventoryCategoryRepo;

    @Autowired
    private PurposeRepo purposeRepo;

    @Autowired
    private ItemStockRepo itemStockRepo;

    @Autowired
    private PurchaseRequestDetailService purchaseRequestDetailService;

    @Autowired
    @Qualifier("workOrderServiceImpl")
    private WorkOrderService workOrderService;

    @Autowired
    @Qualifier("costEstimateServiceImpl")
    private CostEstimateService costEstimateService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return withdrawalService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return withdrawalService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return withdrawalService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return withdrawalService.getDocumentsStatuses();
    }

    @GetMapping("/inventory-locations")
    public List<InventoryLocation> inventoryLocations() {
        return inventoryLocationRepo.findAllByOrderByDescriptionAsc();
    }

    @GetMapping("/inventory-categories")
    public List<InventoryCategory> inventoryCategories() {
        return inventoryCategoryRepo.findAll();
    }

    @GetMapping("/purposes")
    public List<Purpose> purposes() {
        return purposeRepo.findAll();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return withdrawalService.defaultSignatories();
    }

    @GetMapping("/item-stocks/{locationId}/{categoryId}")
    public List<ItemStock> itemStocksForWithdrawal(@PathVariable Integer locationId,
                                                    @PathVariable Integer categoryId) {
        return itemStockRepo.findAllByInventoryLocationIdAndItemInventoryCategoryIdAndTotalQuantityGreaterThanOrderByItemCode(
                locationId, categoryId, BigDecimal.ZERO);
    }

    @GetMapping("/rv-details/{rvId}/{locationId}/{categoryId}")
    public List<Map> rvDetailsForWithdrawal(@PathVariable Integer rvId,
                                             @PathVariable Integer locationId,
                                             @PathVariable Integer categoryId) {
        return purchaseRequestDetailService.getRvDetailsForWithdrawal(rvId, locationId, categoryId);
    }

    @GetMapping("/work-order-details/{workOrderId}/{locationId}/{categoryId}")
    public List<Map> workOrderDetailsForWithdrawal(@PathVariable Integer workOrderId,
                                                    @PathVariable Integer locationId,
                                                    @PathVariable Integer categoryId) {
        return workOrderService.getProjectCostEstimateDetail(workOrderId, locationId, categoryId);
    }

    @GetMapping("/cost-estimate-details/{transactionId}")
    public List<CostEstimateDetailDto> costEstimateDetailsForWithdrawal(@PathVariable Integer transactionId) {
        return costEstimateService.findAllDetailByCostEstimateTransId(transactionId);
    }

    @GetMapping("/{id}")
    public StockWithdrawal getById(@PathVariable Integer id) {
        return withdrawalService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            StockWithdrawal sw = new StockWithdrawal();
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                sw.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                sw.setYear(year);
                sw.setType(1);
                Object latestCode = withdrawalRepo.findLatestCodeByYear(year, 1);
                String code = generatorFacade.voucherCodeNoOffice("MRS",
                        latestCode == null ? "" : String.valueOf(latestCode),
                        voucherDate, GlobalConstant.COUNTER_PAD_4);
                sw.setCode(code);
            }
            sw.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
            Date now = new Date();
            sw.setCreatedAt(now);
            sw.setUpdatedAt(now);
            sw.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            sw.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.WITHDRAWAL.getId());
            sw.setWorkflow(wf);
            sw.setTransaction(generatorFacade.transaction());
            StockWithdrawal saved = withdrawalRepo.save(sw);
            response.setSuccessMessage("Stock Withdrawal saved.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to save: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) { response.setFailureMessage("ID is required."); return response; }
        Integer id = ((Number) idObj).intValue();
        StockWithdrawal sw = withdrawalRepo.findById(id).orElse(null);
        if (sw == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                sw.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            sw.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
            sw.setUpdatedAt(new Date());
            StockWithdrawal saved = withdrawalRepo.save(sw);
            response.setSuccessMessage("Stock Withdrawal updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return withdrawalService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("withdrawalServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockWithdrawal.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

}
