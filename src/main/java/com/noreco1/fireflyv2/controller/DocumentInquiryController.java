package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.StockReleaseDetailDto;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
import com.noreco1.fireflyv2.model.ItemTransactionDetail;
import com.noreco1.fireflyv2.model.StockReleaseDetail;
import com.noreco1.fireflyv2.model.StockWithdrawalDetail;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.CostEstimateDetailRepo;
import com.noreco1.fireflyv2.repo.ItemTransactionDetailRepo;
import com.noreco1.fireflyv2.repo.ReceivingReportDetailRepo;
import com.noreco1.fireflyv2.repo.SiteInspectionReportDescriptionRepo;
import com.noreco1.fireflyv2.repo.StockReleaseDetailRepo;
import com.noreco1.fireflyv2.repo.StockWithdrawalDetailRepo;
import com.noreco1.fireflyv2.service.DocumentInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/document-inquiry")
@RequiredArgsConstructor
public class DocumentInquiryController {

    private final DocumentInquiryService docInqService;
    private final ReceivingReportDetailRepo rrDetailRepo;
    private final StockWithdrawalDetailRepo swDetailRepo;
    private final StockReleaseDetailRepo srlDetailRepo;
    private final ItemTransactionDetailRepo itemTransactionDetailRepo;
    private final CostEstimateDetailRepo ceDetailRepo;
    private final SiteInspectionReportDescriptionRepo sirDescRepo;

    // ── Core list endpoints ──────────────────────────────────────────────────

    @GetMapping("/document-types")
    public List<Map<String, Object>> documentTypes() {
        return buildDocumentTypes();
    }

    @GetMapping("/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}")
    public List<DocInqListDto> docInqList(
            @PathVariable Integer docTypeId,
            @PathVariable String tableName,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable String particulars,
            @RequestParam(required = false, defaultValue = "0") Integer suppId,
            @RequestParam(required = false, defaultValue = "") String dDate,
            @RequestParam(required = false, defaultValue = "") String c,
            @RequestParam(required = false, defaultValue = "") String eAmt,
            @RequestParam(required = false, defaultValue = "") String tAmt,
            @RequestParam(required = false, defaultValue = "0") Integer d) {
        return docInqService.findDocumentsByTypeIdStartDateEndDate(
                docTypeId, tableName, startDate, endDate, particulars,
                suppId, dDate, c, eAmt, tAmt, d);
    }

    @GetMapping("/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}/userId/{userId}")
    public List<DocInqListDto> docInqListByUser(
            @PathVariable Integer docTypeId,
            @PathVariable String tableName,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable String particulars,
            @PathVariable Integer userId) {
        return docInqService.findDocumentsByUserId(userId, docTypeId, tableName, startDate, endDate, particulars);
    }

    @GetMapping("/search/{query}")
    public List<DocInqListDto> search(@PathVariable String query) {
        return docInqService.findDocumentsByQuery(query);
    }

    @GetMapping("/purchase-cycle/{rvdId}")
    public List<DocInqListDto> purchaseCycle(@PathVariable Integer rvdId) {
        return docInqService.findDocumentsByRvdId(rvdId);
    }

    @GetMapping("/accounting-cycle/{transId}")
    public List<DocInqListDto> accountingCycle(@PathVariable Integer transId) {
        return docInqService.findDocumentsByTransId(transId);
    }

    @GetMapping("/inventory-cycle/{transId}")
    public List<DocInqListDto> inventoryCycle(@PathVariable Integer transId) {
        return docInqService.findInventoryDocumentsByTransId(transId);
    }

    // ── Document detail endpoints ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    @GetMapping("/rr-detail/{rrId}")
    public List<Map<String, Object>> rrDetail(@PathVariable Integer rrId) {
        return rrDetailRepo.findByReceivingReportId(rrId).stream()
            .map(d -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",          d.getId());
                m.put("description", d.getItem() != null ? d.getItem().getDescription() : null);
                m.put("unitCode",    null);
                m.put("quantity",    d.getQuantityReceived());
                m.put("netAmount",   d.getNetAmount());
                return m;
            }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping("/sw-items/{swId}")
    public List<StockWithdrawalDetailDto> swItems(@PathVariable Integer swId) {
        return swDetailRepo.findByStockWithdrawalId(swId)
            .stream().map(StockWithdrawalDetail::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping("/srl-items/{srlId}")
    public List<StockReleaseDetailDto> srlItems(@PathVariable Integer srlId) {
        return srlDetailRepo.findByStockReleaseId(srlId)
            .stream().map(StockReleaseDetail::toDto).collect(Collectors.toList());
    }

    // Used for ST, SA, MCT, MST, SRC — all share ItemTransactionDetail keyed by transId
    @Transactional(readOnly = true)
    @GetMapping("/inv-items/{transId}")
    public List<ItemTransactionDetailDto> invItems(@PathVariable Integer transId) {
        return itemTransactionDetailRepo.findByTransactionId(transId)
            .stream().map(ItemTransactionDetail::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping("/ce-details/{ceId}")
    public List<Map<String, Object>> ceDetails(@PathVariable Integer ceId) {
        return ceDetailRepo.findByCostEstimateId(ceId).stream()
            .map(d -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",            d.getId());
                m.put("itemDescription", d.getItem() != null ? d.getItem().getDescription() : null);
                m.put("quantity",      d.getQuantity());
                m.put("unitCost",      d.getUnitCost());
                m.put("totalCost",     d.getTotalCost());
                m.put("inventoryCost", d.getInventoryCost());
                m.put("markUp",        d.getMarkUp());
                return m;
            }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping("/sir-details/{sirId}")
    public List<?> sirDetails(@PathVariable Integer sirId) {
        return sirDescRepo.findBySiteInspectionReportId(sirId);
    }

    // ── Document-types config ────────────────────────────────────────────────

    private List<Map<String, Object>> buildDocumentTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        // type: 1=Purchasing, 2=Accounting, 3=Inventory, 4=WorkOrder
        addType(types, DocumentType.RV,                     "PurchaseRequest",        "purpose",            1);
        addType(types, DocumentType.CF,                     "Canvass",                "FK_vendorAccountNo", 1);
        addType(types, DocumentType.QUOTATION_SUMMARY,      "Quotation",              "particulars",        1);
        addType(types, DocumentType.PO,                     "PurchaseOrder",          "FK_vendorAccountNo", 1);
        addType(types, DocumentType.JO,                     "JobOrder",               "FK_vendorAccountNo", 1);
        addType(types, DocumentType.JOA,                    "JoAcceptance",           "FK_vendorAccountNo", 1);
        addType(types, DocumentType.PR,                     "PaymentRequest",         "FK_vendorAccountNo", 1);
        addType(types, DocumentType.APV,                    "AccountsPayableVoucher", "particulars",        2);
        addType(types, DocumentType.CV,                     "CheckVoucher",           "particulars",        2);
        addType(types, DocumentType.JV,                     "JournalVoucher",         "explanation",        2);
        addType(types, DocumentType.CRV,                    "CashReceipts",           "particulars",        2);
        addType(types, DocumentType.SV,                     "SalesVoucher",           "particulars",        2);
        addType(types, DocumentType.RR,                     "ReceivingReport",        "purpose",            3);
        addType(types, DocumentType.SW,                     "StockWithdrawal",        "description",        3);
        addType(types, DocumentType.SRL,                    "StockRelease",           "description",        3);
        addType(types, DocumentType.ST,                     "StockTransfer",          "description",        3);
        addType(types, DocumentType.SRC,                    "StockReceive",           "description",        3);
        addType(types, DocumentType.SA,                     "StockAdjustment",        "description",        3);
        addType(types, DocumentType.MCT,                    "MaterialCreditTicket",   "description",        3);
        addType(types, DocumentType.MST,                    "MaterialSalvageTicket",  "description",        3);
        addType(types, DocumentType.CE,                     "CostEstimate",           "description",        4);
        addType(types, DocumentType.SITE_INSPECTION_REPORT, "SiteInspectionReport",   "description",        4);
        return types;
    }

    private void addType(List<Map<String, Object>> list, DocumentType dt,
                         String tableName, String pt, int type) {
        Map<String, Object> m = new LinkedHashMap<>();
        // v2 enum: getDescription()=short code e.g. "RV"; getCode()=full name e.g. "Purchase or Work Request"
        m.put("id",        dt.getId());
        m.put("code",      dt.getDescription());   // short code e.g. "RV"
        m.put("desc",      dt.getCode());           // full display name e.g. "Purchase or Work Request"
        m.put("tableName", tableName);
        m.put("pt",        pt);
        m.put("type",      type);
        m.put("module",    dt.getModule());
        m.put("order",     moduleOrder(dt.getModule()));
        list.add(m);
    }

    private int moduleOrder(String module) {
        return switch (module) {
            case "PURCHASING"      -> 1;
            case "ACCOUNTING CORE" -> 2;
            case "INVENTORY"       -> 3;
            case "WORK ORDER"      -> 4;
            default                -> 5;
        };
    }
}
