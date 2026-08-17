package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReceivingReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.noreco1.fireflyv2.controller.response.ReceivingReportDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/rr")
public class ReceivingReportController {

    @Autowired
    @Qualifier("receivingReportServiceImpl")
    private ReceivingReportService rrService;

    @Autowired
    private ReceivingReportRepo rrRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SupplierRepo supplierRepo;

    @Autowired
    private PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    private JobOrderRepo jobOrderRepo;

    @Autowired
    private ItemsForRepairRepo itemsForRepairRepo;

    @GetMapping("/list")
    public List<Map> list() {
        return rrService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31");
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return rrService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return rrService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return rrService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public Map getById(@PathVariable Integer id) {
        return rrService.findById(id);
    }

    @GetMapping("/for-jv")
    public Page<ReceivingReportDocumentDto> forJv(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return rrService.findAllForJv(query, PageRequest.of(page, size));
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return rrService.defaultSignatories();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        ReceivingReport rr = buildReceivingReport(payload);
        if (rr.getSupplier() == null) {
            PostResponse r = new PostResponse();
            r.setFailureMessage("Could not resolve supplier. Please check document references.");
            return r;
        }
        BindingResult br = new BeanPropertyBindingResult(rr, "receivingReport");
        PostResponse response = rrService.processCreate(rr, br, messageSource);
        if (Checker.documentSaved(response)) {
            rrService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        Object idObj = payload.get("id");
        if (idObj == null) {
            PostResponse r = new PostResponse();
            r.setFailureMessage("ID is required.");
            return r;
        }
        ReceivingReport rr = buildReceivingReport(payload);
        rr.setId(((Number) idObj).intValue());
        if (rr.getSupplier() == null) {
            PostResponse r = new PostResponse();
            r.setFailureMessage("Could not resolve supplier. Please check document references.");
            return r;
        }
        BindingResult br = new BeanPropertyBindingResult(rr, "receivingReport");
        PostResponse response = rrService.processUpdate(rr, br, messageSource);
        if (Checker.documentSaved(response)) {
            rrService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return rrService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("receivingReportServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/ReceivingReport.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ReceivingReport buildReceivingReport(Map<String, Object> payload) {
        ReceivingReport rr = new ReceivingReport();

        // Dates
        Object deliveryDateObj = payload.get("deliveryDate");
        if (deliveryDateObj instanceof String s && !s.isEmpty()) {
            rr.setDeliveryDate(java.sql.Date.valueOf(s));
        }
        Object invoiceDateObj = payload.get("invoiceDate");
        if (invoiceDateObj instanceof String s && !s.isEmpty()) {
            rr.setInvoiceDate(java.sql.Date.valueOf(s));
        }

        // Scalar fields
        rr.setDeliveryNumber(payload.get("deliveryNumber") instanceof String s ? s : null);
        rr.setInvoiceNumber(payload.get("invoiceNumber") instanceof String s ? s : null);
        rr.setRemarks(payload.get("remarks") instanceof String s ? s : null);
        rr.setTotalAmount(payload.get("totalAmount") instanceof Number n
                ? new BigDecimal(n.toString()) : BigDecimal.ZERO);

        // Boolean flags
        rr.setIsJO(Boolean.TRUE.equals(payload.get("isJO")));
        rr.setIsRV(Boolean.TRUE.equals(payload.get("isRV")));
        rr.setIsRepairedItems(Boolean.TRUE.equals(payload.get("isRepairedItems")));

        // Inventory location (stub — service does not access its fields, only stores FK)
        if (payload.get("inventoryLocation") instanceof Map<?, ?> locMap && locMap.get("id") instanceof Number n) {
            InventoryLocation loc = new InventoryLocation();
            loc.setId(n.intValue());
            rr.setInventoryLocation(loc);
        }

        // Supplier (resolved from payload or reference document)
        rr.setSupplier(resolveSupplier(payload));

        // Checker (User stub — service reads accountNo then loads full User from DB)
        if (payload.get("checker") instanceof Map<?, ?> m && m.get("accountNo") instanceof Number n) {
            User checker = new User();
            checker.setAccountNo(n.intValue());
            rr.setChecker(checker);
        }

        // Approving Officer (same stub approach)
        if (payload.get("approvingOfficer") instanceof Map<?, ?> m && m.get("accountNo") instanceof Number n) {
            User approver = new User();
            approver.setAccountNo(n.intValue());
            rr.setApprovingOfficer(approver);
        }

        // Details
        boolean isJO  = Boolean.TRUE.equals(rr.getIsJO());
        boolean isRV  = Boolean.TRUE.equals(rr.getIsRV());
        boolean isIFR = Boolean.TRUE.equals(rr.getIsRepairedItems());
        String deliveryNumber = rr.getDeliveryNumber();

        List<Map> detailPayloads = payload.get("rrDetails") instanceof List<?> list
                ? (List<Map>) list : Collections.emptyList();
        List<ReceivingReportDetail> builtDetails = buildRrDetails(detailPayloads, isJO, isRV, isIFR, deliveryNumber);
        rr.setRrDetails(builtDetails);

        // totalQuantity = sum of quantityReceived across all detail rows (DB is NOT NULL)
        BigDecimal totalQty = builtDetails.stream()
                .map(d -> d.getQuantityReceived() != null ? d.getQuantityReceived() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        rr.setTotalQuantity(totalQty);

        return rr;
    }

    private Supplier resolveSupplier(Map<String, Object> payload) {
        // Direct supplier from payload (RV type sends accountNo)
        if (payload.get("supplier") instanceof Map<?, ?> m && m.get("accountNo") instanceof Number n) {
            return supplierRepo.findOneByAccountNumber(n.intValue());
        }

        // From Purchase Order vendor
        if (payload.get("purchaseOrder") instanceof Map<?, ?> m && m.get("id") instanceof Number n) {
            PurchaseOrder po = purchaseOrderRepo.findById(n.intValue()).orElse(null);
            if (po != null && po.getVendor() != null) {
                return supplierRepo.findOneByAccountNumber(po.getVendor().getAccountNo());
            }
        }

        // From Job Order vendor
        if (payload.get("jobOrder") instanceof Map<?, ?> m && m.get("id") instanceof Number n) {
            JobOrder jo = jobOrderRepo.findById(n.intValue()).orElse(null);
            if (jo != null && jo.getVendor() != null) {
                return supplierRepo.findOneByAccountNumber(jo.getVendor().getAccountNo());
            }
        }

        // From Items For Repair supplier
        if (payload.get("itemsForRepair") instanceof Map<?, ?> m && m.get("id") instanceof Number n) {
            ItemsForRepair ifr = itemsForRepairRepo.findById(n.intValue()).orElse(null);
            if (ifr != null) {
                return ifr.getSupplier();
            }
        }

        return null;
    }

    private List<ReceivingReportDetail> buildRrDetails(List<Map> detailPayloads,
                                                        boolean isJO, boolean isRV, boolean isIFR,
                                                        String deliveryNumber) {
        List<ReceivingReportDetail> details = new ArrayList<>();
        if (detailPayloads == null) return details;

        for (Map<?, ?> row : detailPayloads) {
            ReceivingReportDetail detail = new ReceivingReportDetail();

            // Item stub (service uses item.id indirectly during approve, not during create)
            if (row.get("itemId") instanceof Number n) {
                Item item = new Item();
                item.setId(n.intValue());
                detail.setItem(item);
            }

            // For PO/JO, the row.id = PoDetail.id or JoDetail.id (see PoDetailDto and JoDetailDto)
            Integer detailRefId = row.get("id") instanceof Number n ? n.intValue() : null;
            if (detailRefId != null && detailRefId > 0) {
                if (isJO) {
                    JoDetail jd = new JoDetail();
                    jd.setId(detailRefId);
                    detail.setJoDetail(jd);
                } else if (!isRV && !isIFR) {
                    PoDetail pd = new PoDetail();
                    pd.setId(detailRefId);
                    detail.setPoDetail(pd);
                }
            }

            // FK_purchaseRequestDetailId is NOT NULL in DB — set from rvDetailId (PO/JO details link to an RV)
            Integer rvDetailId = row.get("rvDetailId") instanceof Number n ? n.intValue() : null;
            if (rvDetailId != null && rvDetailId > 0) {
                PurchaseRequestDetail prd = new PurchaseRequestDetail();
                prd.setId(rvDetailId);
                detail.setPurchaseRequestDetail(prd);
            }

            BigDecimal qtyOrdered  = toBigDecimal(row.get("quantity"));
            BigDecimal qtyReceived = toBigDecimal(row.get("quantityReceived"));
            BigDecimal unitPrice   = toBigDecimal(row.get("unitPrice"));
            BigDecimal adjustment  = toBigDecimal(row.get("adjustment"));
            BigDecimal netAmount   = toBigDecimal(row.get("netAmount"));
            BigDecimal amount      = unitPrice.multiply(qtyReceived);   // raw: price × qty

            detail.setQuantityOrdered(qtyOrdered);
            detail.setQuantityReceived(qtyReceived);
            detail.setUnitPrice(unitPrice);
            detail.setAmount(amount);
            detail.setAdjustment(adjustment);
            detail.setNetAmount(netAmount);
            detail.setDiscount(BigDecimal.ZERO);
            detail.setVat(BigDecimal.ZERO);
            detail.setDeliveredQuantity(qtyReceived);
            detail.setDeliveryNumber(deliveryNumber);

            details.add(detail);
        }
        return details;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
