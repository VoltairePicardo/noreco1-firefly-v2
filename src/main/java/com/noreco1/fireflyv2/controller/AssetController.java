package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.form.AssetVoucherLinkForm;
import com.noreco1.fireflyv2.controller.form.RetireAssetForm;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Asset;
import com.noreco1.fireflyv2.model.AssetDepreciationSchedule;
import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import com.noreco1.fireflyv2.service.AssetService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/cpr")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private MessageSource messageSource;

    // ── List (server-side paginated with filters) ──────────────────────────────

    @GetMapping("/list-paged")
    public Page<Asset> listPaged(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(name = "asset-type", defaultValue = "0") Integer assetTypeId,
            @RequestParam(name = "fully-depreciated", defaultValue = "") String fullyDepreciated,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasType   = assetTypeId != null && assetTypeId > 0;
        boolean hasDepStatus = fullyDepreciated != null && !fullyDepreciated.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        if (hasType && hasDepStatus && hasStatus) {
            boolean fd = "true".equalsIgnoreCase(fullyDepreciated) || "1".equals(fullyDepreciated);
            return assetService.find(q, pageable, assetTypeId, fd, status);
        }
        if (hasType && hasDepStatus) {
            boolean fd = "true".equalsIgnoreCase(fullyDepreciated) || "1".equals(fullyDepreciated);
            return assetService.find(q, pageable, assetTypeId, fd);
        }
        if (hasType && hasStatus) {
            return assetService.find(q, pageable, assetTypeId, status);
        }
        if (hasType) {
            return assetService.find(q, pageable, assetTypeId);
        }
        if (hasDepStatus && hasStatus) {
            boolean fd = "true".equalsIgnoreCase(fullyDepreciated) || "1".equals(fullyDepreciated);
            return assetService.find(q, pageable, fd, status);
        }
        if (hasDepStatus) {
            boolean fd = "true".equalsIgnoreCase(fullyDepreciated) || "1".equals(fullyDepreciated);
            return assetService.find(q, pageable, fd);
        }
        if (hasStatus) {
            return assetService.find(q, pageable, status);
        }
        return assetService.find(q, pageable);
    }

    // ── Single record ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        HashMap data = assetService.findById(id);
        return data != null ? ResponseEntity.ok(data) : ResponseEntity.notFound().build();
    }

    // ── Items ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/items")
    public List<Map> getItems(@PathVariable Integer id) {
        return assetService.getItems(id);
    }

    // ── Account Details ────────────────────────────────────────────────────────

    @GetMapping("/{id}/details")
    public List<Map> getDetails(
            @PathVariable Integer id,
            @RequestParam(name = "voucherTransNo", required = false) Integer voucherTransNo,
            @RequestParam(name = "transType", required = false) Integer transType) {
        return assetService.getDetails(id, voucherTransNo, transType);
    }

    // ── Depreciation Schedule ──────────────────────────────────────────────────

    @GetMapping("/{id}/depreciation-schedule")
    public List<AssetDepreciationSchedule> getDepreciationSchedule(@PathVariable Integer id) {
        return assetService.findAssetDepreciationScheduleByAssetId(id);
    }

    // ── Link Types ─────────────────────────────────────────────────────────────

    @GetMapping("/link-types")
    public List<AssetVoucherLinkType> getLinkTypes() {
        return assetService.getLinkTypes();
    }

    // ── For Maintenance Order ──────────────────────────────────────────────────

    @GetMapping("/for-maintenance-order")
    public Page<Asset> forMaintenanceOrder(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q != null && !q.isBlank()) {
            return assetService.findAllForMaintenanceOrderByQuery(q, pageable);
        }
        return assetService.findAllForMaintenanceOrder(pageable);
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @PostMapping("/create")
    public PostResponse create(@RequestBody Asset asset) {
        BindingResult bindingResult = new BeanPropertyBindingResult(asset, "asset");
        return assetService.processCreate(asset, bindingResult, messageSource);
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @PostMapping("/update")
    public PostResponse update(@RequestBody Asset asset) {
        BindingResult bindingResult = new BeanPropertyBindingResult(asset, "asset");
        return assetService.processUpdate(asset, bindingResult, messageSource);
    }

    // ── Process Workflow ───────────────────────────────────────────────────────

    @PostMapping("/process")
    public PostResponse process(@RequestBody Map<String, Object> payload) {
        // Workflow processing — delegate through the service if available,
        // otherwise return not-implemented response.
        PostResponse response = new PostResponse();
        response.setFailureMessage("Workflow processing not yet implemented for CPR.");
        return response;
    }

    // ── Retire ─────────────────────────────────────────────────────────────────

    @PostMapping("/retire")
    public PostResponse retire(@RequestBody RetireAssetForm form) {
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "retireAssetForm");
        return assetService.retire(form, bindingResult, messageSource);
    }

    // ── Link Asset Voucher ─────────────────────────────────────────────────────

    @PostMapping("/link-asset-voucher")
    public PostResponse linkAssetVoucher(@RequestBody AssetVoucherLinkForm form) {
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "assetVoucherLinkForm");
        return assetService.saveLink(form, bindingResult, messageSource);
    }
}
