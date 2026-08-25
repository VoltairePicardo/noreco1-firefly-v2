package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.reports.BDetail;
import com.noreco1.fireflyv2.service.BudgetService;
import com.noreco1.fireflyv2.service.PrintableBudget;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.BudgetValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service(value = "budgetServiceImpl")
public class BudgetServiceImpl implements BudgetService, PrintableVoucher, PrintableBudget {

    private Budget model;

    @Autowired
    BudgetRepo budgetRepo;

    @Autowired
    BudgetDetailRepo budgetDetailRepo;

    @Autowired
    BudgetReportRepo budgetReportRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    SignatureFacade signatureFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Budget findById(Integer id) {
        return budgetRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Budget> findAll() {
        List<Budget> budgets = budgetRepo.findAll();
        for (Budget b : budgets) {
            BigDecimal total = budgetDetailRepo.sumAmountByBudgetId(b.getId());
            b.setAmount(total != null ? total : BigDecimal.ZERO);
        }
        return budgets;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Budget> getBudgetYears() {
        return budgetRepo.getBudgetYears();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BudgetDetail> findBudgetDetailByBudgetId(Integer budgetId) {
        return budgetDetailRepo.findByBudgetId(budgetId);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        Budget budget = budgetRepo.findById(vid).orElse(null);

        if (budget != null) {
//            params.put("VOUCHER_NO", budget.getCode());
//            params.put("V_DATE", budget.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(budget.getAmount()));
            params.put("TOTAL", budget.getAmount());
//            params.put("APPROVAR", budget.getApprovingOfficer().getFullName());
//            params.put("CHECKER", budget.getChecker().getFullName());
//            params.put("PREPARAR", budget.getCreatedBy().getFullName());
//            params.put("YEAR", budget.getYear());
//            params.put("DEPARTMENT_ID", budget.getDepartment().getId());
//            params.put("DEPARTMENT_NAME", budget.getDepartment().getName());

//            params = signatureFacade.getDocumentSignature(params, DocumentType.BUDG, budget);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<BDetail> details = new ArrayList<>();
        List<BudgetDetail> budgetDetails = budgetDetailRepo.findByBudgetId(vid);

        if (!Checker.collectionIsEmpty(budgetDetails)) {
            for (BudgetDetail dto : budgetDetails) {
                BDetail d = new BDetail();

//                d.setCode(dto.getBudget().getCode());
                d.setCashflowItemName(dto.getCashflowItem().getName());
                d.setAmount(dto.getAmount());

                details.add(d);
            }
        }

        return new JRBeanCollectionDataSource(details);
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Budget v, BindingResult bindingResult, MessageSource messageSource) {

        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Budget v, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        BudgetValidator validator = new BudgetValidator();

        validator.validate(v, bindingResult);

        try {

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();

                response = messageFormatter.getResponse();
            } else {
                Budget existingBudget;
                User createdBy = authenticationFacade.getLoggedIn();
//                User approvedBy = userRepo.findOneByAccountNo(budg.getApprovingOfficer().getAccountNo());
//                User checkedBy = approvedBy;
                Boolean insertMode = (v.getId() == null || v.getId() < 1);
//                DocumentStatus ds = new DocumentStatus();
//                Workflow wf = new Workflow();

//                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
//                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
//                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.BUDGET.getId());

                // Insert mode.
                if (insertMode) {
//                    v.setCode("BUDG");
//                    v.setDocumentStatus(ds);
//                    v.setTransaction(generatorFacade.transaction());
//                    v.setWorkflow(wf);

                    existingBudget = v;
                    existingBudget.setCreatedAt(new Date());
                } else {
                    List<Integer> statusAllowed = new ArrayList<>();
                    existingBudget = budgetRepo.findById(v.getId()).orElse(null);

                    // Check if data still exists in database when updating.
                    if (existingBudget.getId() != null) {
                        Budget data = existingBudget;

                        if (data == null) {
                            ArrayList<String> messages = new ArrayList();
                            messages.add("Action is not allowed");

                            response.setNotAuthorized(true);
                            response.setMessages(messages);
                            response.setSuccess(false);

                            return response;
                        }
                    }

                    /*statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    if (statusAllowed.indexOf(existingBudget.getDocumentStatus().getId()) < 0) {
                        ArrayList<String> messages = new ArrayList<>();

                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }*/
                }

                // Compute total from submitted details
                ArrayList<BudgetDetail> budgetDetailDtos = v.getBudgetDetails();
                BigDecimal total = BigDecimal.ZERO;
                if (budgetDetailDtos != null) {
                    for (BudgetDetail d : budgetDetailDtos) {
                        total = total.add(d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO);
                    }
                }

                // Editable fields.
                existingBudget.setAmount(total);
//                existingBudget.setWorkflow(wf);
//                existingBudget.setVoucherDate(v.getVoucherDate());
//                existingBudget.setChecker(checkedBy);
                existingBudget.setCreatedBy(createdBy);
//                existingBudget.setApprovingOfficer(approvedBy);
                existingBudget.setYear(v.getYear());
                existingBudget.setUpdatedAt(new Date());

                Budget newBudg = budgetRepo.save(existingBudget);

                if (newBudg != null) {
                    // start: update default signatories
//                    signatoryFacade.budget(newBudg);
                    // end: update default signatories

                    if (!insertMode) {
                        budgetDetailRepo.deleteByBudgetId(newBudg.getId());
                    }

                    // Log action only when adding document.
//                    if (insertMode) {
//                        documentProcessingFacade.processAction(newBudg.getTransaction(), null, newBudg.getWorkflow(), createdBy);
//                    }

                    for (BudgetDetail budgetDetail : budgetDetailDtos) {
                        budgetDetail.setBudget(existingBudget);

                        budgetDetailRepo.save(budgetDetail);
                    }

                    response.setModelId(newBudg.getId());
                    response.setSuccessMessage("Budget successfully saved!");
                    response.setSuccess(true);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BudgetDetail> findAllBudgetDetailsByYear(Integer year) {
        return this.budgetDetailRepo.findAllByBudgetYear(year);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getBudgetDetailBalance(Integer budgetDetailId) {
        return this.budgetDetailRepo.getBudgetDetailBalance(budgetDetailId);
    }

//    @Override
//    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
//        PostResponse response = new PostResponse();
//        User processedBy = authenticationFacade.getLoggedIn();
//        Budget budget = budgetRepo.findById(postData.getDocumentId()).orElse(null);
//
//        if (budget != null && budget.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
//            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
//            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();
//
//            budget.setDocumentStatus(afterActionDocumentStatus);
//            budget.setUpdatedAt(null);
//            budget = budgetRepo.save(budget);
//
//            if (budget != null) {
//                documentProcessingFacade.processAction(budget.getTransaction(), actionMap, null, processedBy);
//
//                response.setSuccessMessage("Document successfully processed");
//                response.setSuccess(true);
//            }
//        }
//
//        return response;
//    }

//    @Override
//    public Map defaultSignatories() {
//        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.BUDG);
//    }

    @Override
    public JRDataSource datasource(Integer vid, Integer year) {
        List<BDetail> details = new ArrayList<>();
        List<BudgetDetail> budgetDetails = budgetReportRepo.findByBudgetYear(year);

        if (!Checker.collectionIsEmpty(budgetDetails)) {
            for (BudgetDetail dto : budgetDetails) {
                BDetail d = new BDetail();

//                d.setCode(dto.getBudget().getCode());
                d.setCashflowItemName(dto.getCashflowItem().getName());
                d.setAmount(dto.getAmount());
//                d.setYear(dto.getBudget().getYear());
//                d.setDepartmentName(dto.getBudget().getDepartment().getName());
//                d.setVoucherDate(dto.getBudget().getVoucherDate());
                d.setCashflowChildName(dto.getCashflowItem().getName());
                d.setCashflowParentName(dto.getCashflowItem().getParentCashflowItem().getName());
                d.setAmtOgm(dto.getAmtOgm());
                d.setAmtAod(dto.getAmtAod());
                d.setAmtIsd(dto.getAmtIsd());
                d.setAmtFsd(dto.getAmtFsd());
                d.setAmtTsd(dto.getAmtTsd());
                d.setAmtBod(dto.getAmtBod());
                d.setTotalYearBudget(dto.getTotalYearBudget());

                details.add(d);
            }
        }

        return new JRBeanCollectionDataSource(details, false);
    }
}
