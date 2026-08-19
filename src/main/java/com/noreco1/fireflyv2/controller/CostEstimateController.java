package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.CostEstimateAssemblyUnitRepo;
import com.noreco1.fireflyv2.repo.CostEstimateDetailRepo;
import com.noreco1.fireflyv2.repo.CostEstimateMiscellaneousChargeRepo;
import com.noreco1.fireflyv2.repo.CostEstimateRepo;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
import com.noreco1.fireflyv2.common.facade.DocumentProcessingFacade;
import com.noreco1.fireflyv2.service.CostEstimateService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cost-estimate")
public class CostEstimateController {

    @Autowired
    @Qualifier("costEstimateServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("costEstimateServiceImpl")
    private CostEstimateService costEstimateService;

    @Autowired
    private CostEstimateRepo costEstimateRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private CostEstimateDetailRepo costEstimateDetailRepo;

    @Autowired
    private CostEstimateMiscellaneousChargeRepo costEstimateMiscellaneousChargeRepo;

    @Autowired
    private CostEstimateAssemblyUnitRepo costEstimateAssemblyUnitRepo;

    @GetMapping("/list")
    public Page<CostEstimate> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (q != null && !q.isEmpty()) {
            return costEstimateService.findByQuery(q, pageable);
        }
        return costEstimateService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public CostEstimate getById(@PathVariable Integer id) {
        return costEstimateService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<com.noreco1.fireflyv2.model.DocumentStatus> documentStatuses() {
        return costEstimateService.getDocumentsStatuses();
    }

    @Transactional
    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            CostEstimate ce = buildCostEstimate(payload, null);
            CostEstimate saved = costEstimateRepo.save(ce);
            if (saved != null) {
                documentProcessingFacade.processAction(saved.getTransaction(), null, saved.getWorkflow(), authenticationFacade.getLoggedIn());
                saveLineItems(saved, payload);
                response.setSuccessMessage("Cost Estimate successfully saved.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to save Cost Estimate.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setFailureMessage("Failed to save Cost Estimate: " + e.getMessage());
        }
        return response;
    }

    @Transactional
    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) {
            response.setFailureMessage("ID is required for update.");
            return response;
        }
        Integer id = ((Number) idObj).intValue();
        CostEstimate existing = costEstimateRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Cost Estimate not found.");
            return response;
        }

        if (payload.get("date") instanceof String dateStr && !dateStr.isEmpty()) {
            existing.setVoucherDate(java.sql.Date.valueOf(dateStr));
        }

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            existing.setProject(p);
        }

        Object invLocObj = payload.get("inventoryLocation");
        if (invLocObj instanceof Map<?, ?> ilm && ilm.get("id") != null) {
            InventoryLocation il = new InventoryLocation();
            il.setId(((Number) ilm.get("id")).intValue());
            existing.setInventoryLocation(il);
        }

        Object typeObj = payload.get("type");
        if (typeObj instanceof Map<?, ?> tm && tm.get("id") != null) {
            existing.setType(((Number) tm.get("id")).intValue());
        }

        if (payload.get("notes") instanceof String n) existing.setNotes(n);

        existing.setLaborCostPercentage(toBigDecimal(payload.get("laborCostPercentage")));
        existing.setLaborCost(toBigDecimal(payload.get("laborCost")));
        existing.setFreightHandlingPercentage(toBigDecimal(payload.get("freightHandlingPercentage")));
        existing.setFreightHandling(toBigDecimal(payload.get("freightHandling")));
        existing.setContingencyPercentage(toBigDecimal(payload.get("contingencyPercentage")));
        existing.setContingency(toBigDecimal(payload.get("contingency")));
        existing.setTotalMaterialCost(toBigDecimal(payload.get("totalMaterialCost")));
        existing.setTotalMeteringCost(toBigDecimal(payload.get("totalMeteringCost")));
        existing.setTotalMiscellaneousCharge(toBigDecimal(payload.get("totalMiscellaneousCharge")));
        existing.setTotalAssemblyLaborCost(toBigDecimal(payload.get("totalAssemblyLaborCost")));

        existing.setApprovingOfficer(resolveUser(payload.get("approvedBy")));
        existing.setRecommendedBy(resolveUser(payload.get("recommendedBy")));
        existing.setChecker(resolveUser(payload.get("concurredBy")));

        existing.setUpdatedAt(new Date());
        CostEstimate saved = costEstimateRepo.save(existing);
        if (saved != null) {
            // Delete existing line items and replace with updated ones
            costEstimateDetailRepo.deleteByCostEstimateId(saved.getId());
            costEstimateMiscellaneousChargeRepo.deleteByCostEstimateId(saved.getId());
            costEstimateAssemblyUnitRepo.deleteByCostEstimateId(saved.getId());
            saveLineItems(saved, payload);
            response.setSuccessMessage("Cost Estimate successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Cost Estimate.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return costEstimateService.process(dto, bindingResult, messageSource);
    }

    @SuppressWarnings("unchecked")
    private void saveLineItems(CostEstimate saved, Map<String, Object> payload) {
        // Accessories (category=1)
        Object detailsAccessObj = payload.get("detailsAccess");
        if (detailsAccessObj instanceof List<?> detailsAccessList) {
            for (Object o : detailsAccessList) {
                if (o instanceof Map<?, ?> d && d.get("id") instanceof Number itemId && itemId.intValue() > 0) {
                    CostEstimateDetail detail = new CostEstimateDetail();
                    detail.setCostEstimate(saved);
                    Item item = new Item();
                    item.setId(itemId.intValue());
                    detail.setItem(item);
                    detail.setCategory(1);
                    java.math.BigDecimal qty = toBigDecimal(d.get("quantity"));
                    java.math.BigDecimal unitCost = toBigDecimal(d.get("unitCost"));
                    detail.setQuantity(qty);
                    detail.setUnitCost(unitCost);
                    detail.setTotalCost(unitCost.multiply(qty));
                    detail.setInventoryCost(java.math.BigDecimal.ZERO);
                    detail.setMarkUp(java.math.BigDecimal.ZERO);
                    costEstimateDetailRepo.save(detail);
                }
            }
        }

        // Metering (category=2)
        Object detailsMeterObj = payload.get("detailsMeter");
        if (detailsMeterObj instanceof List<?> detailsMeterList) {
            for (Object o : detailsMeterList) {
                if (o instanceof Map<?, ?> d && d.get("id") instanceof Number itemId && itemId.intValue() > 0) {
                    CostEstimateDetail detail = new CostEstimateDetail();
                    detail.setCostEstimate(saved);
                    Item item = new Item();
                    item.setId(itemId.intValue());
                    detail.setItem(item);
                    detail.setCategory(2);
                    java.math.BigDecimal qty = toBigDecimal(d.get("quantity"));
                    java.math.BigDecimal unitCost = toBigDecimal(d.get("unitCost"));
                    detail.setQuantity(qty);
                    detail.setUnitCost(unitCost);
                    detail.setTotalCost(unitCost.multiply(qty));
                    detail.setInventoryCost(java.math.BigDecimal.ZERO);
                    detail.setMarkUp(java.math.BigDecimal.ZERO);
                    costEstimateDetailRepo.save(detail);
                }
            }
        }

        // Assemblies
        Object assembliesObj = payload.get("assemblies");
        if (assembliesObj instanceof List<?> assembliesList) {
            for (Object o : assembliesList) {
                if (o instanceof Map<?, ?> a && a.get("id") instanceof Number auId && auId.intValue() > 0) {
                    CostEstimateAssemblyUnit au = new CostEstimateAssemblyUnit();
                    au.setCostEstimate(saved);
                    AssemblyUnit assemblyUnit = new AssemblyUnit();
                    assemblyUnit.setId(auId.intValue());
                    au.setAssemblyUnit(assemblyUnit);
                    java.math.BigDecimal qty = toBigDecimal(a.get("quantity"));
                    java.math.BigDecimal laborCost = toBigDecimal(a.get("laborCost"));
                    au.setQuantity(qty);
                    au.setUnitCost(laborCost);
                    au.setTotalCost(laborCost.multiply(qty));
                    costEstimateAssemblyUnitRepo.save(au);
                }
            }
        }

        // Misc Charges
        Object miscChargesObj = payload.get("miscCharges");
        if (miscChargesObj instanceof List<?> miscChargesList) {
            for (Object o : miscChargesList) {
                if (o instanceof Map<?, ?> m && m.get("id") instanceof Number mcId && mcId.intValue() > 0) {
                    CostEstimateMiscellaneousCharge mc = new CostEstimateMiscellaneousCharge();
                    mc.setCostEstimate(saved);
                    MiscellaneousCharge miscCharge = new MiscellaneousCharge();
                    miscCharge.setId(mcId.intValue());
                    mc.setMiscellaneousCharge(miscCharge);
                    java.math.BigDecimal qty = toBigDecimal(m.get("quantity"));
                    java.math.BigDecimal unitCost = toBigDecimal(m.get("unitCost"));
                    mc.setQuantity(qty);
                    mc.setUnitCost(unitCost);
                    mc.setTotalCost(unitCost.multiply(qty));
                    mc.setRemarks(m.get("remarks") instanceof String r ? r : "");
                    costEstimateMiscellaneousChargeRepo.save(mc);
                }
            }
        }
    }

    private CostEstimate buildCostEstimate(Map<String, Object> payload, Integer id) {
        CostEstimate ce = new CostEstimate();
        if (id != null) ce.setId(id);

        // Date, year, code
        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            Date voucherDate = java.sql.Date.valueOf(dateStr);
            ce.setVoucherDate(voucherDate);
            int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
            ce.setYear(year);
            if (id == null) {
                Object latestCode = costEstimateRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeNoOffice(
                        "CE", latestCode == null ? "" : String.valueOf(latestCode),
                        voucherDate, GlobalConstant.COUNTER_PAD_4);
                ce.setCode(code);
            }
        }

        // Project
        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            ce.setProject(p);
        }

        // Inventory location
        Object invLocObj = payload.get("inventoryLocation");
        if (invLocObj instanceof Map<?, ?> ilm && ilm.get("id") != null) {
            InventoryLocation il = new InventoryLocation();
            il.setId(((Number) ilm.get("id")).intValue());
            ce.setInventoryLocation(il);
        }

        // Type (int field)
        Object typeObj = payload.get("type");
        if (typeObj instanceof Map<?, ?> tm && tm.get("id") != null) {
            ce.setType(((Number) tm.get("id")).intValue());
        }

        // Notes
        if (payload.get("notes") instanceof String n) ce.setNotes(n);

        // BigDecimal fields
        ce.setLaborCostPercentage(toBigDecimal(payload.get("laborCostPercentage")));
        ce.setLaborCost(toBigDecimal(payload.get("laborCost")));
        ce.setFreightHandlingPercentage(toBigDecimal(payload.get("freightHandlingPercentage")));
        ce.setFreightHandling(toBigDecimal(payload.get("freightHandling")));
        ce.setContingencyPercentage(toBigDecimal(payload.get("contingencyPercentage")));
        ce.setContingency(toBigDecimal(payload.get("contingency")));
        ce.setTotalMaterialCost(toBigDecimal(payload.get("totalMaterialCost")));
        ce.setTotalMeteringCost(toBigDecimal(payload.get("totalMeteringCost")));
        ce.setTotalMiscellaneousCharge(toBigDecimal(payload.get("totalMiscellaneousCharge")));
        ce.setTotalAssemblyLaborCost(toBigDecimal(payload.get("totalAssemblyLaborCost")));

        // Signatories
        ce.setApprovingOfficer(resolveUser(payload.get("approvedBy")));
        ce.setRecommendedBy(resolveUser(payload.get("recommendedBy")));
        ce.setChecker(resolveUser(payload.get("concurredBy")));

        // Audit / document fields (insert only)
        if (id == null) {
            ce.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            ce.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CE.getId());
            ce.setWorkflow(wf);
            ce.setTransaction(generatorFacade.transaction());
        }

        return ce;
    }

    private User resolveUser(Object obj) {
        if (obj instanceof Map<?, ?> m && m.get("accountNo") != null) {
            try {
                Integer accountNo = ((Number) m.get("accountNo")).intValue();
                return userRepo.findOneByAccountNo(accountNo);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private java.math.BigDecimal toBigDecimal(Object val) {
        if (val == null) return java.math.BigDecimal.ZERO;
        try { return new java.math.BigDecimal(val.toString()); } catch (Exception e) { return java.math.BigDecimal.ZERO; }
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CostEstimate.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
