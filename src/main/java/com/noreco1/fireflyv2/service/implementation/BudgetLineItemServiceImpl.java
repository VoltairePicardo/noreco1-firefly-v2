package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ClassHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;
import com.noreco1.fireflyv2.service.BudgetLineItemService;
import com.noreco1.fireflyv2.validator.BudgetLineItemValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BudgetLineItemServiceImpl implements BudgetLineItemService {

    private BudgetLineItem model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private BudgetLineItemRepo budgetLineItemRepo;

    @Autowired
    private BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    private FileFacade fileFacade;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private SignatoryFacade signatoryFacade;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private DocumentLogRepo documentLogRepo;

    @Autowired
    private DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private BudgetSubItemRepo budgetSubItemRepo;

    @Autowired
    private DocumentDtoer documentDtoer;

    @Autowired
    private SettingFacade settingFacade;

    @Override
    public Page<BudgetLineItem> findAll(Pageable pageable) {

        try {

            Employee employee = this.employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

            if (employee != null){
                return this.budgetLineItemRepo.findAllByDivisionIdOrderByYearAsc(employee.getDivision().getId(), pageable);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Page<BudgetLineItem> findAllByYear(Integer year, Pageable pageable) {
        return this.budgetLineItemRepo.findAllByYear(year, authenticationFacade.getLoggedIn().getId(), pageable);
    }

    @Override
    public Page<BudgetLineItem> findAllByYearAndDivision(Integer year, Integer division, Pageable pageable) {
        return this.budgetLineItemRepo.findAllByYearAndDivisionId(year, division, authenticationFacade.getLoggedIn().getId(), pageable);
    }

    @Override
    public BudgetLineItem findById(Integer id) {

        BudgetLineItem budgetLineItem = new BudgetLineItem();

        try {

            budgetLineItem = this.budgetLineItemRepo.findById(id).orElse(null);

            if (Checker.isValidId(budgetLineItem.getId())){

                List<BudgetLineItemDetail> budgetLineItemDetails = this.budgetLineItemDetailRepo.findAllByBudgetLineItemId(budgetLineItem.getId());

                if (Checker.collectionIsNotEmpty(budgetLineItemDetails)){
                    budgetLineItem.setBudgetLineItemDetails(budgetLineItemDetails);
                }

                if(budgetLineItem.getVerifiedBy() != null){
                    if(authenticationFacade.getLoggedIn().getAccountNo().equals(budgetLineItem.getVerifiedBy().getAccountNo()) &&
                            budgetLineItem.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_VERIFICATION.getId())){
                        budgetLineItem.setIsForAddingAdditionalDetails(Boolean.TRUE);
                    }
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return budgetLineItem;

    }

    @Override
    public PostResponse delete(Integer id) {

        PostResponse response = new PostResponse();

        try {

            BudgetLineItem foundBudgetLineItem = this.findById(id);

            if(foundBudgetLineItem != null) {
                this.budgetLineItemRepo.delete(foundBudgetLineItem);
                this.budgetLineItemDetailRepo.deleteInBatch(foundBudgetLineItem.getBudgetLineItemDetails());
                response.setSuccessMessage("Budget line item has been deleted.");
            } else {
                response.setFailureMessage("Budget line item is not available.");
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse additionalDetail(BudgetLineItem budgetLineItem, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            List<BudgetLineItemDetail> budgetLineItemDetails = budgetLineItem.getBudgetLineItemDetails();

            for(BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails){

                BudgetLineItemDetail detail = this.budgetLineItemDetailRepo.findById(budgetLineItemDetail.getId()).orElse(null);

                if (Checker.isValidId(detail.getId())){

                    detail.setCode(budgetLineItemDetail.getCode());
                    detail.setFundingSource(budgetLineItemDetail.getFundingSource());
                    detail.setCashflowItem(budgetLineItemDetail.getCashflowItem());
                    detail.setGeneralClassification(budgetLineItemDetail.getGeneralClassification());
                    detail.setRemarks(budgetLineItemDetail.getRemarks());

                    this.budgetLineItemDetailRepo.save(detail);

                }

            }

            response.setSuccessMessage("Added details successfully saved!");
            response.setSuccess(true);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public List<BudgetLineItemDetail> getBudgetLineItemDetailForRV(Boolean isFromPR) {

        List<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

        try{

            if(isFromPR){
                budgetLineItemDetails = this.budgetLineItemDetailRepo.findAllByBudgetLineItemDocumentStatusIdOrderByCodeAsc(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            } else {
                Employee employee = this.employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
                budgetLineItemDetails = this.budgetLineItemDetailRepo.findAllByBudgetLineItemDocumentStatusIdAndBudgetLineItemDivisionIdOrderByCodeAsc(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), employee.getDivision().getId());
            }

            //check if budget line item detail has sub items return true else false
            if (Checker.collectionIsNotEmpty(budgetLineItemDetails)) {
                for (BudgetLineItemDetail lineItemDetail : budgetLineItemDetails) {
                    lineItemDetail.setHasSubItems(Checker.collectionIsNotEmpty(budgetSubItemRepo.findAllByBudgetLineItemDetailId(lineItemDetail.getId())));
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return budgetLineItemDetails;

    }

    @Override
    public BigDecimal getBudgetLineItemDetailQuantityBalance(Integer budgetLineItemDetailId) {

        BigDecimal balance = BigDecimal.ZERO;

        try {

            BigDecimal getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailQuantityBalance(com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), budgetLineItemDetailId);

            if (getBalance.compareTo(BigDecimal.ZERO) != -1){
                balance = getBalance;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return balance;

    }

    @Override
    public BigDecimal getBudgetLineItemDetailAmountBalance(Integer budgetLineItemDetailId) {
        BigDecimal balance = BigDecimal.ZERO;

        try {

            BigDecimal getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalance(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), budgetLineItemDetailId);

            if (getBalance.compareTo(BigDecimal.ZERO) != -1){
                balance = getBalance;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return balance;
    }

    @Override
    public BigDecimal getBudgetLineItemDetailAmountBalanceByType(Integer budgetLineItemDetailId, String type) {
        BigDecimal balance = BigDecimal.ZERO;

        try {

            BigDecimal getBalance = BigDecimal.ZERO;

            if(Objects.equals(type, DocumentType.CV.getCode())){
                getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalanceCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetLineItemDetailId);
            } else if(Objects.equals(type, DocumentType.CA.getCode())){
                getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalanceCA(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetLineItemDetailId);
            } else if(Objects.equals(type, DocumentType.PETTY_CASH_LIQUIDATION.getCode())){
                getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalancePCL(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetLineItemDetailId);
            } else if(Objects.equals(type, DocumentType.PCV.getCode())){
                getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalancePCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetLineItemDetailId);
            } else {
                getBalance = this.budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalancePOJO(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), budgetLineItemDetailId);
            }

            if (getBalance.compareTo(BigDecimal.ZERO) != -1){
                balance = getBalance;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return balance;
    }

    @Override
    public List<BudgetLineItem> findAllByYearAndDivisionParams(Integer year, Integer department, Integer division, Integer status) {

        List<BudgetLineItem> budgetLineItems;

        boolean isGeneralManager = this.isCurrentUserGeneralManager();
        boolean isFsdManager = this.isCurrentUserFsdManager();
        boolean hasValidStatus = Checker.isValidId(status);

        if(isGeneralManager){
            budgetLineItems = this.budgetLineItemRepo.findAllForGeneralManager(year, department, division, hasValidStatus ? status : null);
        } else if (isFsdManager){
            budgetLineItems = this.budgetLineItemRepo.findAllForFsdManager(year, department, division, hasValidStatus ? status : null);
        } else {
            Integer userId = this.authenticationFacade.getLoggedIn().getId();
            budgetLineItems = this.budgetLineItemRepo.findAllBySelectedYearAndSelectedDivisionId(year, department, division, hasValidStatus ? status : null, userId);
        }

        for (BudgetLineItem lineItem : budgetLineItems){
            List<BudgetLineItemDetail> details = this.budgetLineItemDetailRepo.findAllByBudgetLineItemId(lineItem.getId());
            if(Checker.collectionIsNotEmpty(details)){
                lineItem.setBudgetLineItemDetails(details);
            }
            lineItem.setIsForApproval(isGeneralManager && lineItem.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId()));
            lineItem.setCode(com.noreco1.fireflyv2.model.enums.DocumentType.BUDGET_LINE_ITEM.getCode());
        }

        return budgetLineItems;

    }

    @Override
    public Boolean isCurrentUserFsdManager() {
        try {
            User loggedInUser = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());

            if (employee == null || employee.getPosition() == null) {
                return Boolean.FALSE;
            }

            Map<String, Object> fsdManager = settingFacade.getByCode("BUDGET_LINE_ITEM_FSD_MANAGER");
            Integer positionId = (Integer) fsdManager.get("id");

            return positionId != null && positionId.equals(employee.getPosition().getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean isCurrentUserGeneralManager() {
        try {
            User loggedInUser = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());

            if (employee == null || employee.getPosition() == null) {
                return Boolean.FALSE;
            }

            Map<String, Object> fsdManager = settingFacade.getByCode("BUDGET_LINE_ITEM_GENERAL_MANAGER");
            Integer positionId = (Integer) fsdManager.get("id");

            return positionId != null && positionId.equals(employee.getPosition().getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return Boolean.FALSE;
        }
    }

    @Override
    public BudgetLineItemDetail findBudgetLineItemDetailById(Integer id) {
        return this.budgetLineItemDetailRepo.findById(id).orElse(null);
    }

    @Override
    public PostResponse updateNeaApprovedAmount(BudgetLineItemDetail form, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            BudgetLineItemDetail detail = this.budgetLineItemDetailRepo.findById(form.getId()).orElse(null);

            if (detail != null) {

                detail.setNEAApprovedAmount( form.getNEAApprovedAmount());
                detail.setFinalAmount(form.getNEAApprovedAmount());

                budgetLineItemDetailRepo.save(detail);

                documentLoggerFacade.budgetLineItemDetailLog(detail, authenticationFacade.getLoggedIn());

                response.setSuccess(true);
                response.setSuccessMessage("Document successfully processed");

            } else {
                response.setSuccess(false);
                response.setFailureMessage("Budget Line Item detail not found");
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse updateSupplementalAmount(BudgetLineItemDetail form,
                                                 BindingResult bindingResult,
                                                 MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {
            BudgetLineItemDetail detail = budgetLineItemDetailRepo.findById(form.getId()).orElse(null);

            if (detail == null) {
                response.setSuccess(false);
                response.setFailureMessage("Budget Line Item detail not found");
                return response;
            }

            // Resolve amounts safely
            BigDecimal supplementalAmount = form.getSupplementalAmount() != null
                    ? form.getSupplementalAmount()
                    : BigDecimal.ZERO;

            BigDecimal neaApprovedAmount = detail.getNEAApprovedAmount() != null
                    ? detail.getNEAApprovedAmount()
                    : BigDecimal.ZERO;

            BigDecimal quantity = detail.getQuantity() != null
                    ? detail.getQuantity()
                    : BigDecimal.ONE;

            // Update detail
            detail.setSupplementalAmount(supplementalAmount);

            BigDecimal totalAmount = neaApprovedAmount.add(supplementalAmount);
            detail.setTotalPrice(quantity.multiply(totalAmount));
            detail.setFinalAmount(totalAmount);

            budgetLineItemDetailRepo.save(detail);

            // Log update
            documentLoggerFacade.budgetLineItemDetailLog(detail, authenticationFacade.getLoggedIn());

            // Workflow processing
            ProcessDocumentDto documentDto = new ProcessDocumentDto();
            documentDto.setDocumentId(detail.getBudgetLineItem().getId());
            documentDto.setTransId(detail.getBudgetLineItem().getTransaction().getId());

            WorkflowActionsDto workflowActionsDto = new WorkflowActionsDto();
            workflowActionsDto.setActionMapId(1319);
            documentDto.setWorkflowActionsDto(workflowActionsDto);

            documentDto.setRemarks("Supplemental Budget added.");

            this.process(documentDto, bindingResult, messageSource);

            response.setSuccess(true);
            response.setSuccessMessage("Document successfully processed");

        } catch (Exception ex) {
            response.setSuccess(false);
            response.setFailureMessage("Error processing document");
        }

        return response;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            User processedBy = authenticationFacade.getLoggedIn();
            BudgetLineItem budgetLineItem = this.budgetLineItemRepo.findById(postData.getDocumentId()).orElse(null);

            if (budgetLineItem != null) {
                // for logging
                Map oldMap = this.forLogMapMain(budgetLineItem);
                DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
                DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

                // set dynamic property here
                if (actionMap.getPropSignatureType() != null) {
                    try {
                        ClassHelper.setSignatoryValue(budgetLineItem, budgetLineItem.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                budgetLineItem.setDocumentStatus(afterActionDocumentStatus);
                budgetLineItem.setUpdatedAt(null);
                budgetLineItem = this.budgetLineItemRepo.save(budgetLineItem);

                // for logging
                Map newMap = this.forLogMapMain(budgetLineItem);
                newMap.put("remarks", postData.getRemarks());

                if (budgetLineItem != null) {
                    documentProcessingFacade.processAction(budgetLineItem.getTransaction(), actionMap, null, processedBy);
                    documentLoggerFacade.log(budgetLineItem.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                    response.setSuccessMessage("Document successfully processed");
                    response.setSuccess(true);
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        BudgetLineItem budgetLineItem = (BudgetLineItem) v;
        return this.processCreate(budgetLineItem, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        BudgetLineItem budgetLineItem = (BudgetLineItem) v;
        PostResponse response = new PostResponse();

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BudgetLineItemValidator validator = new BudgetLineItemValidator();
        validator.setService(this);
        validator.setAuthenticationFacade(this.authenticationFacade);
        validator.setEmployeeRepo(this.employeeRepo);
        validator.validate(budgetLineItem, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            User checkedBy = userRepo.findOneByAccountNo(budgetLineItem.getCheckedBy().getAccountNo());
            User verifiedBy = userRepo.findOneByAccountNo(budgetLineItem.getVerifiedBy().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(budgetLineItem.getApprovingOfficer().getAccountNo());

            Employee employee = this.employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
            String employeeDepartment = employee.getDepartment().getAbbreviation();
            String employeeDivision = (employee.getDivision() == null || Checker.isStringNullOrEmpty(employee.getDivision().getAbbreviation())) ? null : employee.getDivision().getAbbreviation();

            BudgetLineItem existingBudgetLineItem = null;

            Boolean insertMode = budgetLineItem.getId() == null || budgetLineItem.getId() == 0;
            if (insertMode) { // insert mode

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                budgetLineItem.setDocumentStatus(documentStatus);

                budgetLineItem.setTransaction(generatorFacade.transaction());
                budgetLineItem.setCreatedBy(createdBy);
                existingBudgetLineItem = budgetLineItem;

            } else {
                existingBudgetLineItem = this.budgetLineItemRepo.findById(budgetLineItem.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = documentLoggerFacade.makeLog(existingBudgetLineItem);

            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.BUDGET_LINE_ITEM.getId());

            existingBudgetLineItem.setWorkflow(wf);
            existingBudgetLineItem.setCreatedBy(authenticationFacade.getLoggedIn());
            existingBudgetLineItem.setCheckedBy(checkedBy);
            existingBudgetLineItem.setVerifiedBy(verifiedBy);
            existingBudgetLineItem.setApprovingOfficer(approvedBy);
            existingBudgetLineItem.setYear(budgetLineItem.getYear());
            existingBudgetLineItem.setDivision(budgetLineItem.getDivision());
            existingBudgetLineItem.setDepartment(budgetLineItem.getDepartment());
            existingBudgetLineItem.setForSupplementalBudget(budgetLineItem.getForSupplementalBudget());

            this.model = this.budgetLineItemRepo.save(existingBudgetLineItem);

            if (this.model != null) {

                // start: update default signatories
                // default signatories was set to settings table where code = BUDGET_LINE_ITEM_SIGNATORIES
                // signatoryFacade.budgetLineItem(this.model);
                // end: update default signatories

                if (!insertMode) {
                    budgetLineItemDetailRepo.deleteByBudgetLineItemId(existingBudgetLineItem.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                if (Checker.collectionIsNotEmpty(budgetLineItem.getBudgetLineItemDetails())){

                    List<BudgetLineItemDetail> budgetLineItemDetails = budgetLineItem.getBudgetLineItemDetails();

                    for(BudgetLineItemDetail budgetLineItemDetail : budgetLineItemDetails){

                        String latestCode = budgetLineItemDetailRepo.findLatestBudgetLineItemCodeDynamic(budgetLineItemDetail.getYear(), employeeDepartment, employeeDivision);

                        BudgetLineItemDetail detail = new BudgetLineItemDetail();

                        detail.setBudgetLineItem(this.model);
                        detail.setTitle(budgetLineItemDetail.getTitle());
                        detail.setLength(budgetLineItemDetail.getLength());
                        detail.setLocation(budgetLineItemDetail.getLocation());
                        detail.setQuantity(budgetLineItemDetail.getQuantity());
                        detail.setTotalPrice(budgetLineItemDetail.getTotalPrice());

                        if(this.model.getForSupplementalBudget()){
                            detail.setApplicationAmount(BigDecimal.ZERO);
                            detail.setNEAApprovedAmount(BigDecimal.ZERO);
                            detail.setSupplementalAmount(budgetLineItemDetail.getApplicationAmount());
                            detail.setFinalAmount(budgetLineItemDetail.getApplicationAmount());
                        } else {
                            detail.setApplicationAmount(budgetLineItemDetail.getApplicationAmount());
                            detail.setNEAApprovedAmount(budgetLineItemDetail.getApplicationAmount());
                            detail.setSupplementalAmount(BigDecimal.ZERO);
                            detail.setFinalAmount(budgetLineItemDetail.getApplicationAmount());
                        }

                        String code;

                        if(employee.getDepartment() != null && employee.getDivision() != null){
                            code = this.generatorFacade.budgetLineItemCodeForDepartmentAndDivisionOnly(latestCode, employeeDepartment, employeeDivision, budgetLineItemDetail.getYear()).toString();
                        } else {
                            code = this.generatorFacade.budgetLineItemCodeForDepartmentOnly(latestCode, employeeDepartment, budgetLineItemDetail.getYear()).toString();
                        }

                        detail.setCode(code);
                        detail.setFundingSource(budgetLineItemDetail.getFundingSource());
                        detail.setBudgetItemClassification(budgetLineItemDetail.getBudgetItemClassification());
                        detail.setRemarks(budgetLineItemDetail.getRemarks());
                        detail.setSpecification(budgetLineItemDetail.getSpecification());
                        detail.setProjectType(budgetLineItemDetail.getProjectType());
                        detail.setItem(budgetLineItemDetail.getItem());
                        detail.setUnit(budgetLineItemDetail.getUnit());
                        detail.setExpectedDeliveryDate(budgetLineItemDetail.getExpectedDeliveryDate());
                        detail.setGeneralClassification(budgetLineItemDetail.getGeneralClassification());
                        detail.setCashflowItem(budgetLineItemDetail.getCashflowItem());
                        detail.setYear(budgetLineItemDetail.getYear());
                        detail.setStartMonth(budgetLineItemDetail.getStartMonth());
                        detail.setEndMonth(budgetLineItemDetail.getEndMonth());
                        detail.setStrategicInitiative(budgetLineItemDetail.getStrategicInitiative());

                        this.budgetLineItemDetailRepo.save(detail);

                    }

                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Budget line item successfully saved!");

            }
        }

        return response;

    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            BudgetLineItem doc = this.budgetLineItemRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    private Map forLogMapMain(BudgetLineItem budgetLineItem) {
        return documentLoggerFacade.makeLog(budgetLineItem);
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.BUDGET_LINE_ITEM);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        PostResponse response = this.processCreate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null && mRequest.getFileMap() != null) {
                fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
            }
        }

        return response;
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = this.processCreate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        BudgetLineItem budgetLineItem = budgetLineItemRepo.findFirstByOrderByIdAsc();
        if (budgetLineItem != null) {
            return documentDtoer.getDocumentStatuses(budgetLineItem.getWorkflow().getId());
        }

        return null;
    }
}
