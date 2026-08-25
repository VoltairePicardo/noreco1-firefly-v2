package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.CashAdvanceParticular;
import com.noreco1.fireflyv2.repo.CashAdvanceParticularRepo;
import com.noreco1.fireflyv2.service.CashAdvanceParticularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CashAdvanceParticularServiceImpl implements CashAdvanceParticularService {

    @Autowired
    CashAdvanceParticularRepo cashAdvanceParticularRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        CashAdvanceParticular cashAdvanceParticular = cashAdvanceParticularRepo.findById(id).orElse(null);

        if (cashAdvanceParticular != null) {
            map = composeHashMap(cashAdvanceParticular);
        }

        return map;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();
        List<CashAdvanceParticular> cashAdvanceParticulars = cashAdvanceParticularRepo.findAll();

        if (!Checker.collectionIsEmpty(cashAdvanceParticulars)) {
            for (CashAdvanceParticular cashAdvanceParticular : cashAdvanceParticulars) {
                mapList.add(composeHashMap(cashAdvanceParticular));
            }
        }

        return mapList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findByCAId(Integer caId) {
        List<HashMap> mapList = new ArrayList<>();
        List<CashAdvanceParticular> cashAdvanceParticulars = cashAdvanceParticularRepo.findByCashAdvanceId(caId);

        if (!Checker.collectionIsEmpty(cashAdvanceParticulars)) {
            for (CashAdvanceParticular cashAdvanceParticular : cashAdvanceParticulars) {
                mapList.add(composeHashMap(cashAdvanceParticular));
            }
        }

        return mapList;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findByCAIdForLiquidation(Integer id, Integer calId) {
        List<HashMap> mapList = new ArrayList<>();
        List<CashAdvanceParticular> cashAdvanceParticulars = cashAdvanceParticularRepo.findByCAIdForLiquidation(id, calId);

        if (!Checker.collectionIsEmpty(cashAdvanceParticulars)) {
            for (CashAdvanceParticular cashAdvanceParticular : cashAdvanceParticulars) {
                mapList.add(composeHashMap(cashAdvanceParticular));
            }
        }

        return mapList;
    }

    private HashMap composeHashMap(CashAdvanceParticular cashAdvanceParticular) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> cashAdvance;
        HashMap<String, Object> documentStatus;
        HashMap<String, Object> transaction;
        HashMap<String, Object> employee;
        HashMap<String, Object> createdBy;
        HashMap<String, Object> approvingOfficer;
        HashMap<String, Object> recommendedBy;

        try {
            // Set document status object.
            documentStatus = new HashMap<>();

            documentStatus.put("id", cashAdvanceParticular.getCashAdvance().getDocumentStatus().getId());
            documentStatus.put("status", cashAdvanceParticular.getCashAdvance().getDocumentStatus().getStatus());

            // Set transaction object.
            transaction = new HashMap<>();

            transaction.put("id", cashAdvanceParticular.getCashAdvance().getTransaction().getId());
            transaction.put("createdAt", cashAdvanceParticular.getCashAdvance().getTransaction().getCreatedAt());
            transaction.put("createdBy", cashAdvanceParticular.getCashAdvance().getTransaction().getCreatedBy());

            // Set employee object.
            employee = new HashMap<>();

            employee.put("id", cashAdvanceParticular.getCashAdvance().getEmployee().getId());
            employee.put("accountNo", cashAdvanceParticular.getCashAdvance().getEmployee().getAccountNumber());
            employee.put("fullName", cashAdvanceParticular.getCashAdvance().getEmployee().getName());

            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", cashAdvanceParticular.getCashAdvance().getCreatedBy().getId());
            createdBy.put("accountNo", cashAdvanceParticular.getCashAdvance().getCreatedBy().getAccountNo());
            createdBy.put("fullName", cashAdvanceParticular.getCashAdvance().getCreatedBy().getFullName());

            // Set approved by user object.
            approvingOfficer = new HashMap<>();

            approvingOfficer.put("id", cashAdvanceParticular.getCashAdvance().getApprovingOfficer().getId());
            approvingOfficer.put("accountNo", cashAdvanceParticular.getCashAdvance().getApprovingOfficer().getAccountNo());
            approvingOfficer.put("fullName", cashAdvanceParticular.getCashAdvance().getApprovingOfficer().getFullName());

            // Set audited by user object.
            recommendedBy = new HashMap<>();

            recommendedBy.put("id", cashAdvanceParticular.getCashAdvance().getRecommendedBy().getId());
            recommendedBy.put("accountNo", cashAdvanceParticular.getCashAdvance().getRecommendedBy().getAccountNo());
            recommendedBy.put("fullName", cashAdvanceParticular.getCashAdvance().getRecommendedBy().getFullName());

            // Set cash advance object.
            cashAdvance = new HashMap<>();

            cashAdvance.put("id", cashAdvanceParticular.getCashAdvance().getId());
            cashAdvance.put("code", cashAdvanceParticular.getCashAdvance().getCode());
            cashAdvance.put("accountNo", cashAdvanceParticular.getCashAdvance().getAccountNo());
            cashAdvance.put("employee", employee);
            cashAdvance.put("cashAdvanceDate", cashAdvanceParticular.getCashAdvance().getCashAdvanceDate());
            cashAdvance.put("voucherDate", cashAdvanceParticular.getCashAdvance().getVoucherDate());
            cashAdvance.put("amount", cashAdvanceParticular.getCashAdvance().getAmount());
            cashAdvance.put("purpose", cashAdvanceParticular.getCashAdvance().getPurpose());
            cashAdvance.put("remarks", cashAdvanceParticular.getCashAdvance().getRemarks());
            cashAdvance.put("location", cashAdvanceParticular.getCashAdvance().getLocation());
            cashAdvance.put("budgetLineItemDetail", cashAdvanceParticular.getCashAdvance().getBudgetLineItemDetail());
            cashAdvance.put("createdBy", createdBy);
            cashAdvance.put("approvingOfficer", approvingOfficer);
            cashAdvance.put("recommendedBy", recommendedBy);
            cashAdvance.put("documentStatus", documentStatus);
            cashAdvance.put("createdAt", cashAdvanceParticular.getCashAdvance().getCreatedAt());
            cashAdvance.put("updatedAt", cashAdvanceParticular.getCashAdvance().getUpdatedAt());
            cashAdvance.put("transaction", transaction);

            hm.put("id", cashAdvanceParticular.getId());
            hm.put("cashAdvance", cashAdvance);
            hm.put("particular", cashAdvanceParticular.getParticular());
            hm.put("amount", cashAdvanceParticular.getAmount());
            hm.put("date", cashAdvanceParticular.getDate());
            hm.put("quantity", cashAdvanceParticular.getQuantity());
            hm.put("unit", cashAdvanceParticular.getUnit());
            hm.put("total", cashAdvanceParticular.getTotal());
            hm.put("amountStr",  new DecimalFormat("#,##0.00").format(cashAdvanceParticular.getAmount()));
        } catch (Exception ex) {
            Logger.getLogger(CashAdvanceParticularServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }
}
