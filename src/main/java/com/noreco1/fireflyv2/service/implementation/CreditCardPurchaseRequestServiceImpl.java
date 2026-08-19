package com.noreco1.fireflyv2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestBatchDto;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.CreditCardPurchaseRequestService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CreditCardPurchaseRequestValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value = "creditCardPurchaseRequestServiceImpl")
public class CreditCardPurchaseRequestServiceImpl implements CreditCardPurchaseRequestService, PrintableVoucher {

    private CreditCardPurchaseRequest model;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private DocumentDtoer documentDtoer;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private SlEntityRepo slEntityRepo;

    @Autowired
    private DocumentLogRepo documentLogRepo;

    @Autowired
    private DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private SignatoryFacade signatoryFacade;

    @Autowired
    private SignatureFacade signatureFacade;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private PoDetailRepo poDetailRepo;

    @Autowired
    private JoDetailRepo joDetailRepo;

    @Autowired
    private CreditCardPurchaseRequestRepo creditCardPurchaseRequestRepo;

    @Autowired
    private CreditCardPurchaseRequestBatchRepo creditCardPurchaseRequestBatchRepo;

    @Autowired
    private SettingFacade settingFacade;

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        CreditCardPurchaseRequest creditCardPurchaseRequest =  creditCardPurchaseRequestRepo.findById(postData.getDocumentId()).orElse(null);

        if (creditCardPurchaseRequest != null) {
            // for logging
            Map oldMap = this.forLogMapMain(creditCardPurchaseRequest);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(creditCardPurchaseRequest, creditCardPurchaseRequest.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            creditCardPurchaseRequest.setDocumentStatus(afterActionDocumentStatus);
            creditCardPurchaseRequest.setUpdatedAt(null);
            creditCardPurchaseRequest = creditCardPurchaseRequestRepo.save(creditCardPurchaseRequest);

            // for logging
            Map newMap = this.forLogMapMain(creditCardPurchaseRequest);
            newMap.put("remarks", postData.getRemarks());

            if (creditCardPurchaseRequest != null) {
                documentProcessingFacade.processAction(creditCardPurchaseRequest.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(creditCardPurchaseRequest.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;

    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        CreditCardPurchaseRequest creditCardPurchaseRequest = (CreditCardPurchaseRequest) v;

        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CreditCardPurchaseRequestValidator validator = new CreditCardPurchaseRequestValidator();
        validator.setService(this);
        validator.validate(creditCardPurchaseRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {

            User createdBy = authenticationFacade.getLoggedIn();
            CreditCardPurchaseRequest existingCCPR = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(creditCardPurchaseRequest.getVoucherDate()));

            User recommendedBy = userRepo.findOneByAccountNo(creditCardPurchaseRequest.getRecommendingOfficer().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(creditCardPurchaseRequest.getApprovingOfficer().getAccountNo());

            Boolean insertMode = creditCardPurchaseRequest.getId() == null;
            if (insertMode) { // insert mode
                Object latestCode = creditCardPurchaseRequestRepo.findLatestCodeByYear(voucherYear);
                creditCardPurchaseRequest.setCode(generatorFacade.voucherCodeNoOffice("CCPR", (latestCode == null ? "" : String.valueOf(latestCode)), creditCardPurchaseRequest.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                creditCardPurchaseRequest.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CCPR.getId());
                creditCardPurchaseRequest.setWorkflow(wf);

                creditCardPurchaseRequest.setYear(voucherYear);
                creditCardPurchaseRequest.setTransaction(generatorFacade.transaction());
                creditCardPurchaseRequest.setCreatedBy(createdBy);
                creditCardPurchaseRequest.setCreatedAt(new Date());

                existingCCPR = creditCardPurchaseRequest;

            } else {
                existingCCPR = creditCardPurchaseRequestRepo.findById(creditCardPurchaseRequest.getId()).orElse(null);
                existingCCPR.setUpdatedAt(new Date());
            }

            // use for document logging
            Map oldMap = this.forLogMapMain(existingCCPR);

            existingCCPR.setVoucherDate(creditCardPurchaseRequest.getVoucherDate());
            existingCCPR.setYear(voucherYear);
            existingCCPR.setMode(creditCardPurchaseRequest.getMode());
            existingCCPR.setFundingSource(creditCardPurchaseRequest.getFundingSource());
            existingCCPR.setPurpose(creditCardPurchaseRequest.getPurpose());
            existingCCPR.setRecommendingOfficer(recommendedBy);
            existingCCPR.setApprovingOfficer(approvedBy);
            existingCCPR.setPurchaseOrder(creditCardPurchaseRequest.getPurchaseOrder());
            existingCCPR.setJobOrder(creditCardPurchaseRequest.getJobOrder());

            this.model = creditCardPurchaseRequestRepo.save(existingCCPR);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.creditCardPurchaseRequest(this.model);
                // end: update default signatories

//                if (!insertMode) {
//                    costEstimateDetailRepo.deleteByCostEstimateId(model.getId());
//                    costEstimateMiscellaneousChargeRepo.deleteByCostEstimateId(model.getId());
//                    costEstimateAssemblyUnitItemRepo.deleteByCostEstimateAssemblyUnitCostEstimateId(model.getId());
//                    costEstimateAssemblyUnitRepo.deleteByCostEstimateId(model.getId());
//                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Credit card purchase request successfully saved!");
                response.setSuccess(true);

            }

        }

        return response;

    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            CreditCardPurchaseRequest doc = creditCardPurchaseRequestRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CCPR);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        CreditCardPurchaseRequest voucher = creditCardPurchaseRequestRepo.findFirstByOrderByIdAsc();
        if (voucher != null && voucher.getWorkflow() != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    @Override
    public List<CreditCardPurchaseRequest> findByDateRangeAndStatusId(String from, String to, Integer docStatusId) {

        List<CreditCardPurchaseRequest> creditCardPurchaseRequests = new ArrayList<>();

        try {

            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            if(Checker.isValidId(docStatusId)){
                creditCardPurchaseRequests = this.creditCardPurchaseRequestRepo.findAllByVoucherDateBetweenAndDocumentStatusId(fromDate, toDate, docStatusId);
            } else {
                creditCardPurchaseRequests = this.creditCardPurchaseRequestRepo.findAllByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return creditCardPurchaseRequests;

    }

    @Override
    public CreditCardPurchaseRequestDto findById(Integer id) {

        CreditCardPurchaseRequestDto dto = new CreditCardPurchaseRequestDto();

        try {

            CreditCardPurchaseRequest creditCardPurchaseRequest = this.creditCardPurchaseRequestRepo.findById(id).orElse(null);

            dto.setId(creditCardPurchaseRequest.getId());
            dto.setCode(creditCardPurchaseRequest.getCode());
            dto.setVoucherDate(creditCardPurchaseRequest.getVoucherDate());
            dto.setDocumentStatus(creditCardPurchaseRequest.getDocumentStatus());
            dto.setWorkflow(creditCardPurchaseRequest.getWorkflow());
            dto.setTransaction(creditCardPurchaseRequest.getTransaction());

            if(creditCardPurchaseRequest.getCreatedBy() != null){
                SlEntity createdBy = this.slEntityRepo.findOneByAccountNo(creditCardPurchaseRequest.getCreatedBy().getAccountNo());
                dto.setCreatedBy(createdBy);
            }

            if(creditCardPurchaseRequest.getRequestedBy() != null){
                SlEntity requestedBy = this.slEntityRepo.findOneByAccountNo(creditCardPurchaseRequest.getRequestedBy().getAccountNo());
                dto.setRequestedBy(requestedBy);
            }

            if(creditCardPurchaseRequest.getSupplier() != null){
                SlEntity supplier = this.slEntityRepo.findOneByAccountNo(creditCardPurchaseRequest.getSupplier().getAccountNo());
                dto.setSupplier(supplier);
            }

            dto.setPurpose(creditCardPurchaseRequest.getPurpose());
            dto.setMode(creditCardPurchaseRequest.getMode());
            dto.setFundingSource(creditCardPurchaseRequest.getFundingSource());

            SlEntity recommendedBy = this.slEntityRepo.findOneByAccountNo(creditCardPurchaseRequest.getRecommendingOfficer().getAccountNo());
            dto.setRecommendingOfficer(recommendedBy);

            SlEntity approvedBy = this.slEntityRepo.findOneByAccountNo(creditCardPurchaseRequest.getApprovingOfficer().getAccountNo());
            dto.setApprovingOfficer(approvedBy);

            dto.setLastUpdated(creditCardPurchaseRequest.getUpdatedAt());

            dto.setPurchaseOrder(creditCardPurchaseRequest.getPurchaseOrder());
            dto.setJobOrder(creditCardPurchaseRequest.getJobOrder());
            dto.setBatch(creditCardPurchaseRequest.getBatch());
            dto.setExpenseAccount(creditCardPurchaseRequest.getExpenseAccount());

            dto.setForAccountingAdditionalDetails(this.isLoggedInUserFromAccounting());

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return dto;

    }

    @Override
    public PostResponse createBatch(CreditCardPurchaseRequestBatchDto dto) {

        PostResponse response = new PostResponse();

        User loggedIn = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(loggedIn.getAccountNo());

        if (employee == null) {
            response.setFailureMessage("Employee not found");
            response.setSuccess(false);
            return response;
        }

        if (employee.getOffice() == null) {
            response.setFailureMessage("User Area Office not found");
            response.setSuccess(false);
            return response;
        }

        CreditCardPurchaseRequestBatch batch = new CreditCardPurchaseRequestBatch();
        batch.setCreatedBy(loggedIn);
        batch.setStatus(dto.getStatus() != 0);
        batch.setOffice(employee.getOffice());
        batch.setCreatedAt(new java.util.Date());

        batch = creditCardPurchaseRequestBatchRepo.save(batch);

        if (batch.getId() != null) {
            response.setSuccess(true);
            response.setSuccessMessage("Batch successfully created");
        } else {
            response.setSuccess(false);
            response.setFailureMessage("Failed to create Batch");
        }

        return response;
    }

    @Override
    public List<Map<String, Object>> findAllBatchesByAreaOffice() {

        List<Map<String, Object>> mapList = new ArrayList<>();

        Employee loggedInEmployee = employeeRepo
                .findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        // Check if employee or office is null
        if (loggedInEmployee == null || loggedInEmployee.getOffice() == null) {
            return mapList; // return empty list safely
        }

        List<CreditCardPurchaseRequestBatch> batches =
                creditCardPurchaseRequestBatchRepo.findAllByOfficeId(
                        loggedInEmployee.getOffice().getId()
                );

        if (!Checker.collectionIsEmpty(batches)) {
            for (CreditCardPurchaseRequestBatch batch : batches) {
                mapList.add(composeBatchMap(batch));
            }
        }

        return mapList;
    }

    @Override
    public PostResponse additionalDetail(CreditCardPurchaseRequestDto creditCardPurchaseRequest, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        if (bindingResult.hasErrors()) {
            response.setSuccess(false);
            response.setFailureMessage("Invalid request data.");
            return response;
        }

        try {

            CreditCardPurchaseRequest existingRequest = creditCardPurchaseRequestRepo.findById(creditCardPurchaseRequest.getId()).orElse(null);

            if (existingRequest == null) {
                response.setSuccess(false);
                response.setFailureMessage("Credit card purchase request not found.");
                return response;
            }

            existingRequest.setBatch(creditCardPurchaseRequest.getBatch());
            existingRequest.setExpenseAccount(creditCardPurchaseRequest.getExpenseAccount());
            existingRequest.setUpdatedAt(DateHelper.getServerDate());

            creditCardPurchaseRequestRepo.save(existingRequest);

            response.setSuccess(true);
            response.setSuccessMessage("Additional details saved successfully.");

        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);

            response.setSuccess(false);
            response.setFailureMessage("An error occurred while saving additional details.");
        }

        return response;

    }

    @Override
    public List<Map<String, Object>> findAllBatchesForJv() {

        List<Map<String, Object>> mapList = new ArrayList<>();

        List<CreditCardPurchaseRequestBatch> batches = creditCardPurchaseRequestBatchRepo.findAllBatchesForJv();

        if (!Checker.collectionIsEmpty(batches)) {
            for (CreditCardPurchaseRequestBatch batch : batches) {
                mapList.add(composeBatchMap(batch));
            }
        }

        return mapList;

    }

    @Override
    public List<CreditCardPurchaseRequestDto> getCreditCardPurchaseRequestByBatch(Integer id) {

        List<CreditCardPurchaseRequestDto> dtoList = new ArrayList<>();

        try {

            List<CreditCardPurchaseRequest> requests = creditCardPurchaseRequestRepo.findAllByBatchId(id);

            for (CreditCardPurchaseRequest request : requests){

                CreditCardPurchaseRequestDto dto = new CreditCardPurchaseRequestDto();

                dto.setPurchaseOrder(request.getPurchaseOrder());
                dto.setJobOrder(request.getJobOrder());
                dto.setExpenseAccount(request.getExpenseAccount());

                dtoList.add(dto);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return dtoList;

    }

    private Map forLogMapMain(CreditCardPurchaseRequest creditCardPurchaseRequest) {
        return documentLoggerFacade.makeLog(creditCardPurchaseRequest);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        CreditCardPurchaseRequest creditCardPurchaseRequest = creditCardPurchaseRequestRepo.findById(vid).orElse(null);

        if (creditCardPurchaseRequest != null) {

            params.put("VOUCHER_NO", creditCardPurchaseRequest.getCode());
            params.put("VOUCHER_DATE", creditCardPurchaseRequest.getVoucherDate());

            String requestedBy = "";
            String requestedByDepartment = "";
            String supplierName = "";
            String supplierAddress = "";

            if(creditCardPurchaseRequest.getPurchaseOrder() != null){
                PurchaseOrder linkPO = creditCardPurchaseRequest.getPurchaseOrder();
                List<PoDetail> poDetails = this.poDetailRepo.findByPurchaseOrderId(linkPO.getId());
                if(Checker.collectionIsNotEmpty(poDetails)){
                    for (PoDetail poDetail : poDetails){
                        Integer accountNo = poDetail.getPurchaseRequestDetail().getPurchaseRequest().getCreatedBy().getAccountNo();
                        Employee employee = this.employeeRepo.findOneByAccountNumber(accountNo);
                        if(employee != null && Checker.isValidId(employee.getId())){
                            requestedBy = employee.getName();
                            requestedByDepartment = employee.getDepartment().getAbbreviation();
                        }
                    }
                }
                SlEntity slEntity = this.slEntityRepo.findOneByAccountNo(linkPO.getVendor().getAccountNo());
                supplierName = slEntity.getName();
                supplierAddress = slEntity.getAddress();
            }

            if(creditCardPurchaseRequest.getJobOrder() != null){
                JobOrder linkJO = creditCardPurchaseRequest.getJobOrder();
                List<JoDetail> joDetails = this.joDetailRepo.findByJobOrderId(linkJO.getId());
                if(Checker.collectionIsNotEmpty(joDetails)){
                    for (JoDetail joDetail : joDetails){
                        Integer accountNo = joDetail.getPurchaseRequestDetail().getPurchaseRequest().getCreatedBy().getAccountNo();
                        Employee employee = this.employeeRepo.findOneByAccountNumber(accountNo);
                        if(employee != null && Checker.isValidId(employee.getId())){
                            requestedBy = employee.getName();
                            requestedByDepartment = employee.getDepartment().getAbbreviation();
                        }
                    }
                }
                SlEntity slEntity = this.slEntityRepo.findOneByAccountNo(linkJO.getVendor().getAccountNo());
                supplierName = slEntity.getName();
                supplierAddress = slEntity.getAddress();
            }

            params.put("REQUESTED_BY", requestedBy);
            params.put("REQUESTED_BY_DEPARTMENT", requestedByDepartment);
            params.put("PURPOSE", creditCardPurchaseRequest.getPurpose());
            params.put("MODE", creditCardPurchaseRequest.getMode().getDescription());
            params.put("FUNDING_SOURCE", creditCardPurchaseRequest.getFundingSource().getDescription());

            params.put("SUPPLIER_NAME", supplierName);
            params.put("SUPPLIER_ADDRESS", supplierAddress);

            params = signatureFacade.getDocumentSignature(params, DocumentType.CCPR, creditCardPurchaseRequest);

        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {

        List<Map> details = new ArrayList<>();

        CreditCardPurchaseRequest creditCardPurchaseRequest = this.creditCardPurchaseRequestRepo.findById(vid).orElse(null);

        if(creditCardPurchaseRequest.getPurchaseOrder() != null){
            PurchaseOrder linkPO = creditCardPurchaseRequest.getPurchaseOrder();
            List<PoDetail> poDetails = this.poDetailRepo.findByPurchaseOrderId(linkPO.getId());
            if(Checker.collectionIsNotEmpty(poDetails)){
                for (PoDetail poDetail : poDetails){
                    Map map = new HashMap();

                    map.put("quantity", poDetail.getQuantity());
                    map.put("itemCode", poDetail.getPurchaseRequestDetail().getItem().getCode());
                    map.put("itemDescription", poDetail.getPurchaseRequestDetail().getItem().getDescription());
                    map.put("unitPrice", poDetail.getUnitPrice());
                    map.put("total", poDetail.getAmount());

                    details.add(map);
                }
            }
        }

        if(creditCardPurchaseRequest.getJobOrder() != null){
            JobOrder linkJO = creditCardPurchaseRequest.getJobOrder();
            List<JoDetail> joDetails = this.joDetailRepo.findByJobOrderId(linkJO.getId());
            if(Checker.collectionIsNotEmpty(joDetails)){
                for (JoDetail joDetail : joDetails){
                    Map map = new HashMap();

                    map.put("quantity", joDetail.getQuantity());
                    map.put("itemCode", joDetail.getPurchaseRequestDetail().getItem() != null ? joDetail.getPurchaseRequestDetail().getItem().getCode() : "");
                    map.put("itemDescription", joDetail.getPurchaseRequestDetail().getItem() != null ? joDetail.getPurchaseRequestDetail().getItem().getDescription() : joDetail.getPurchaseRequestDetail().getJoDescription());
                    map.put("unitPrice", joDetail.getUnitPrice());
                    map.put("total", joDetail.getAmount());

                    details.add(map);
                }
            }
        }

        return new JRBeanCollectionDataSource(details);

    }

    private Map<String, Object> composeBatchMap(CreditCardPurchaseRequestBatch batch) {

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> createdBy = new HashMap<>();
        createdBy.put("id", batch.getCreatedBy().getId());
        createdBy.put("accountNo", batch.getCreatedBy().getAccountNo());
        createdBy.put("fullName", batch.getCreatedBy().getFullName());

        result.put("id", batch.getId());
        result.put("createdBy", createdBy);
        result.put("status", batch.getStatus() ? "Closed" : "Active");
        result.put("createdAt", batch.getCreatedAt());

        return result;
    }

    private Boolean isLoggedInUserFromAccounting() {

        try {
            // Fetch the setting map
            Map setting = this.settingFacade
                    .getByCode("CREDIT_CARD_PURCHASE_REQUEST_ACCOUNTING_ADDITIONAL_DETAILS_POSITIONS");

            if (setting == null || setting.get("positionIds") == null) {
                return false;
            }

            // Extract the positionIds directly
            @SuppressWarnings("unchecked")
            List<Integer> positionIds = (List<Integer>) setting.get("positionIds");

            if (positionIds.isEmpty()) {
                return false;
            }

            // Get logged-in employee's position
            Integer loggedInAccountNumber = authenticationFacade.getLoggedIn().getAccountNo();
            Employee employee = employeeRepo.findOneByAccountNumber(loggedInAccountNumber);
            Integer employeePositionId = employee.getPosition().getId();

            // Check if the employee's position is in the allowed list
            return positionIds.contains(employeePositionId);

        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
