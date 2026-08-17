package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import com.noreco1.fireflyv2.model.OtherDeposit;
import com.noreco1.fireflyv2.repo.CheckVoucherChequeRepo;
import com.noreco1.fireflyv2.repo.OtherDepositRepo;
import com.noreco1.fireflyv2.repo.ReleasedCheckRepo;
import com.noreco1.fireflyv2.service.BankReconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bank-reconciliation")
public class BankReconciliationController {

    @Autowired
    CheckVoucherChequeRepo checkVoucherChequeRepo;

    @Autowired
    ReleasedCheckRepo releasedCheckRepo;

    @Autowired
    OtherDepositRepo otherDepositRepo;

    @Autowired
    @Qualifier("brServiceImpl")
    BankReconService brService;

    @Autowired
    MessageSource messageSource;

    /**
     * Combined list of Released Checks (RC) and Other Deposits (OD).
     */
    @GetMapping("/list")
    public List<?> list() {
        return brService.findAll();
    }

    /**
     * Released Checks (RC) only — kept for backward compatibility.
     */
    @GetMapping("/rc")
    public List<Map<String, Object>> listReleasedChecks() {
        List<Object[]> rows = checkVoucherChequeRepo.findByReleasedWithCheckVoucherAndAmount(true);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",            row[0]);
            map.put("checkNumber",   row[1]);
            map.put("amount",        row[2]);
            map.put("code",          row[3]);
            map.put("voucherDate",   row[4]);
            map.put("particulars",   row[5]);
            map.put("accountTitle",  row[6]);
            map.put("cleared",       row[7]);
            map.put("transactionId", row[8]);
            map.put("type",          "RC");
            result.add(map);
        }
        return result;
    }

    /**
     * Toggle cleared / not-cleared for a released check (RC type).
     */
    @PostMapping("/process-rc")
    public PostResponse processRc(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            Integer id      = (Integer) payload.get("id");
            Boolean cleared = (Boolean) payload.get("cleared");
            CheckVoucherCheque cvc = checkVoucherChequeRepo.findById(id).orElse(null);
            if (cvc == null) {
                response.setSuccess(false);
                response.setFailureMessage("Check not found.");
                return response;
            }
            cvc.setCleared(cleared);
            checkVoucherChequeRepo.save(cvc);
            response.setSuccess(true);
            response.setModelId(id);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setFailureMessage("Failed to update status: " + e.getMessage());
        }
        return response;
    }

    // ─── Other Deposit (OD) ─────────────────────────────────────────────────

    /**
     * Get a single Other Deposit by ID.
     */
    @GetMapping("/od/{id}")
    public Object getOdById(@PathVariable Integer id) {
        return brService.findByOdId(id);
    }

    /**
     * Create a new Other Deposit.
     */
    @PostMapping("/od/create")
    public PostResponse createOd(@RequestBody OtherDeposit od) {
        BindingResult bindingResult = new BeanPropertyBindingResult(od, "otherDeposit");
        return brService.processCreate(od, bindingResult, messageSource);
    }

    /**
     * Update an existing Other Deposit.
     */
    @PostMapping("/od/update")
    public PostResponse updateOd(@RequestBody OtherDeposit od) {
        BindingResult bindingResult = new BeanPropertyBindingResult(od, "otherDeposit");
        return brService.processUpdate(od, bindingResult, messageSource);
    }

    /**
     * Toggle cleared / not-cleared for an Other Deposit (OD type).
     */
    @PostMapping("/process-od")
    public PostResponse processOd(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            Integer id      = (Integer) payload.get("id");
            Boolean cleared = (Boolean) payload.get("cleared");
            OtherDeposit od = otherDepositRepo.findById(id).orElse(null);
            if (od == null) {
                response.setSuccess(false);
                response.setFailureMessage("Other Deposit not found.");
                return response;
            }
            od.setCleared(cleared);
            otherDepositRepo.save(od);
            response.setSuccess(true);
            response.setModelId(id);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setFailureMessage("Failed to update status: " + e.getMessage());
        }
        return response;
    }
}
