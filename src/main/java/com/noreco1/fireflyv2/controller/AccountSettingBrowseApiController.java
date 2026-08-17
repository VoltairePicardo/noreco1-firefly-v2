package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AccountSettingBrowseApiController {

    // ── Existing services (inventory / RR browse) ──────────────────────
    @Autowired private ReceivingReportService receivingReportService;
    @Autowired private StockReceiveService stockReceiveService;
    @Autowired private MaterialCreditTicketService materialCreditTicketService;
    @Autowired private StockReleaseService stockReleaseService;
    @Autowired private MaterialSalvageTicketService materialSalvageTicketService;
    @Autowired private StockAdjustmentService stockAdjustmentService;

    // ── Services with built-in paged browse ───────────────────────────
    @Autowired private PaymentRequestService paymentRequestService;
    @Autowired private JoAcceptanceService joAcceptanceService;
    @Autowired private ApvService apvService;

    // ── Repos for direct paged browse ─────────────────────────────────
    @Autowired private CanvassRepo canvassRepo;
    @Autowired private QuotationRepo quotationRepo;
    @Autowired private PurchaseOrderRepo purchaseOrderRepo;
    @Autowired private JobOrderRepo jobOrderRepo;
    @Autowired private CreditCardPurchaseRequestRepo creditCardPurchaseRequestRepo;
    @Autowired private CheckVoucherRepo checkVoucherRepo;
    @Autowired private JournalVoucherRepo journalVoucherRepo;
    @Autowired private PurchaseRequestRepo purchaseRequestRepo;

    // ── Helper ────────────────────────────────────────────────────────

    private PageRequest pageOf(int page, int size) {
        return PageRequest.of(page, size);
    }

    // ── Receiving Reports ─────────────────────────────────────────────

    @GetMapping("/api/receiving-report/apv-approved-paged")
    public Page<?> rrApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return receivingReportService.findAllApprovedForApvPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Stock Receive ──────────────────────────────────────────────────

    @GetMapping("/api/inventory/receiving/approved-paged")
    public Page<?> stockReceiveApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return stockReceiveService.findAllApprovedForAccountSettingPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Material Credit Ticket ─────────────────────────────────────────

    @GetMapping("/api/inventory/mct/approved-paged")
    public Page<?> mctApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return materialCreditTicketService.findAllApprovedForAccountSettingPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Stock Release (MIV) ────────────────────────────────────────────

    @GetMapping("/api/inventory/releasing/approved-paged")
    public Page<?> stockReleaseApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return stockReleaseService.findAllApprovedForAccountSettingPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Material Salvage Ticket ────────────────────────────────────────

    @GetMapping("/api/inventory/mst/approved-paged")
    public Page<?> mstApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return materialSalvageTicketService.findAllApprovedForAccountSettingPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Stock Adjustment ───────────────────────────────────────────────

    @GetMapping("/api/inventory/stock-adjustment/approved-paged")
    public Page<?> stockAdjustmentApprovedPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return stockAdjustmentService.findAllApprovedForAccountSettingPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Requisition Voucher ────────────────────────────────────────────

    @GetMapping("/api/requisition-voucher/browse-paged")
    public Page<Map<String, Object>> rvBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PurchaseRequest> data = q.isEmpty()
                ? purchaseRequestRepo.findAll(pageOf(page, size))
                : purchaseRequestRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", e.getEstimatedAmount());
            m.put("particulars", e.getPurpose());
            return m;
        });
    }

    // ── Canvass ────────────────────────────────────────────────────────

    @GetMapping("/api/canvass/browse-paged")
    public Page<Map<String, Object>> canvassBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Canvass> data = q.isEmpty()
                ? canvassRepo.findAll(pageOf(page, size))
                : canvassRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", null);
            m.put("particulars", null);
            return m;
        });
    }

    // ── Quotation ──────────────────────────────────────────────────────

    @GetMapping("/api/quotation/browse-paged")
    public Page<Map<String, Object>> quotationBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Quotation> data = q.isEmpty()
                ? quotationRepo.findAll(pageOf(page, size))
                : quotationRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getDate());
            m.put("netAmount", null);
            m.put("particulars", e.getParticular());
            return m;
        });
    }

    // ── Purchase Order ─────────────────────────────────────────────────

    @GetMapping("/api/purchase-order/browse-paged")
    public Page<Map<String, Object>> poBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PurchaseOrder> data = q.isEmpty()
                ? purchaseOrderRepo.findAll(pageOf(page, size))
                : purchaseOrderRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", e.getAmount());
            m.put("particulars", null);
            return m;
        });
    }

    // ── Job Order ──────────────────────────────────────────────────────

    @GetMapping("/api/job-order/browse-paged")
    public Page<Map<String, Object>> joBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<JobOrder> data = q.isEmpty()
                ? jobOrderRepo.findAll(pageOf(page, size))
                : jobOrderRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", e.getAmount());
            m.put("particulars", e.getDescription());
            return m;
        });
    }

    // ── JO Acceptance ──────────────────────────────────────────────────

    @GetMapping("/api/jo-acceptance/browse-paged")
    public Page<?> joaBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return joAcceptanceService.findAllApprovedForApvPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Payment Request ────────────────────────────────────────────────

    @GetMapping("/api/payment-request/browse-paged")
    public Page<?> paymentRequestBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentRequestService.findAllApprovedForApvPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Credit Card Purchase Request ───────────────────────────────────

    @GetMapping("/api/credit-card-purchase-request/browse-paged")
    public Page<Map<String, Object>> ccprBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CreditCardPurchaseRequest> data = q.isEmpty()
                ? creditCardPurchaseRequestRepo.findAll(pageOf(page, size))
                : creditCardPurchaseRequestRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", null);
            m.put("particulars", e.getPurpose());
            return m;
        });
    }

    // ── Accounts Payable Voucher ───────────────────────────────────────

    @GetMapping("/api/apv/browse-paged")
    public Page<?> apvBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return apvService.findAllApprovedForCvPaged(q.isEmpty() ? null : q, pageOf(page, size));
    }

    // ── Disbursement (Check Voucher) ───────────────────────────────────

    @GetMapping("/api/disbursement/browse-paged")
    public Page<Map<String, Object>> disbursementBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CheckVoucher> data = q.isEmpty()
                ? checkVoucherRepo.findAll(pageOf(page, size))
                : checkVoucherRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", e.getCheckAmount());
            m.put("particulars", e.getParticulars() != null ? e.getParticulars() : e.getRemarks());
            return m;
        });
    }

    // ── General Journal ────────────────────────────────────────────────

    @GetMapping("/api/general-journal/browse-paged")
    public Page<Map<String, Object>> generalJournalBrowsePaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<JournalVoucher> data = q.isEmpty()
                ? journalVoucherRepo.findAll(pageOf(page, size))
                : journalVoucherRepo.findByCodeContainingIgnoreCase(q, pageOf(page, size));
        return data.map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("localCode", e.getCode());
            m.put("voucherDate", e.getVoucherDate());
            m.put("netAmount", null);
            m.put("particulars", e.getExplanation() != null ? e.getExplanation() : e.getRemarks());
            return m;
        });
    }

}
