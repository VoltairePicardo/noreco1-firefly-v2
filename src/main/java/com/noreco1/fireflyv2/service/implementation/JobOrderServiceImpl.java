package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.JODetail;
import com.noreco1.fireflyv2.service.JobOrderService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.JoValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Personal on 6/18/2015.
 */
@Service(value = "joServiceImpl")
public class JobOrderServiceImpl implements JobOrderService, PrintableVoucher {

    private JobOrder model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    JobOrderRepo jobOrderRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    JoDetailRepo joDetailRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    JoDetailServiceImpl joDetailDto;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    DocumentWorkflowLogRepo documentWorkflowLogRepo;

    @Autowired
    DocumentWorkflowActionMapRepo documentWorkflowActionMapRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    JobOrderBudgetDetailRepo jobOrderBudgetDetailRepo;

    @Autowired
    PurchaseRequestDetailRepo purchaseRequestDetailRepo;

    @Autowired
    Environment env;

    @Override
    @Transactional(readOnly = true)
    public JobOrder findOneByCode(String code) {
        return jobOrderRepo.findOneByCode(code);
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        JobOrder jobOrder = (JobOrder) v;
        return this.processCreate(jobOrder, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        JobOrder jobOrder = (JobOrder) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        JoValidator validator = new JoValidator();
        validator.setService(this);
        validator.validate(jobOrder, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            JobOrder existingJo = null;
            Workflow existingWF = new Workflow();
            Workflow wf = new Workflow();

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(jobOrder.getVoucherDate()));

            User budgetCheckedBy = userRepo.findOneByAccountNo(jobOrder.getBudgetCheckedBy().getAccountNo());
            User checkedBy = userRepo.findOneByAccountNo(jobOrder.getCheckedBy().getAccountNo());
            User approvedBy = userRepo.findOneByAccountNo(jobOrder.getApprovingOfficer().getAccountNo());

            Boolean insertMode = jobOrder.getId() == null;
            if (insertMode) { // insert mode
                Object latestCanvassCode = jobOrderRepo.findLatestJoCodeByYear(voucherYear);
                jobOrder.setCode(generatorFacade.voucherCodeNoOffice("JO", (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), jobOrder.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                jobOrder.setDocumentStatus(documentStatus);

                jobOrder.setTransaction(generatorFacade.transaction());
                jobOrder.setWorkflow(wf);
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JO.getId());
                jobOrder.setCreatedBy(createdBy);
                jobOrder.setTerm(0);//Just putting default value for this is not implemented yet..
                existingJo = jobOrder;
            } else {
                existingJo = jobOrderRepo.findById(jobOrder.getId()).orElse(null);
                existingWF = existingJo.getWorkflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JO.getId());
                if(!existingWF.getId().equals(wf.getId())){
                    existingJo.setWorkflow(wf);
                }
            }

            // use for document logging
            Map oldMap = this.forLogMapMain(existingJo);

            existingJo.setVendor(jobOrder.getVendor());
            existingJo.setVoucherDate(jobOrder.getVoucherDate());
            existingJo.setYear(voucherYear);
            existingJo.setBudgetCheckedBy(budgetCheckedBy);
            existingJo.setCheckedBy(checkedBy);
            existingJo.setApprovingOfficer(approvedBy);
            existingJo.setAmount(jobOrder.getAmount());
            existingJo.setDescription(jobOrder.getDescription());
            existingJo.setPaymentTerm(jobOrder.getPaymentTerm());
            existingJo.setPaymentTermInWords(jobOrder.getPaymentTermInWords());
            existingJo.setBudgetLineItemDetail(jobOrder.getBudgetLineItemDetail());
            existingJo.setBudgetLineItemBalancePOJORFP(jobOrder.getBudgetLineItemBalancePOJORFP());
            existingJo.setBudgetLineItemBalanceCV(jobOrder.getBudgetLineItemBalanceCV());

            this.model = jobOrderRepo.save(existingJo);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.jo(jobOrder);
                // end: update default signatories

                if (!insertMode) {
                    joDetailRepo.deleteByJobOrderId(existingJo.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                } else if(!existingWF.getId().equals(wf.getId())){
                    DocumentWorkflowLog docLog = documentWorkflowLogRepo.findOneByTransactionId(this.model.getTransaction().getId());
                    List<DocumentWorkflowActionMap> workflowActionMaps = documentWorkflowActionMapRepo.findDocumentCreatedAndWorkflowId(wf.getId());
                    if (workflowActionMaps != null && workflowActionMaps.size() > 0) {
                        docLog.setDocumentWorkflowActionMap(workflowActionMaps.get(0));
                    }
                    documentWorkflowLogRepo.save(docLog);
                }

                ArrayList<JoDetailDto> poDetails = jobOrder.getJoDetails();
                for(JoDetailDto poDetailLine: poDetails) {

                    JoDetail poDetail = new JoDetail();

                    JobOrder jo1 = new JobOrder();
                    jo1.setId(this.model.getId());
                    poDetail.setJobOrder(jo1);

                    PurchaseRequestDetail purchaseRequestDetail = new PurchaseRequestDetail();
                    purchaseRequestDetail.setId(poDetailLine.getRvDetailId());
                    purchaseRequestDetail.setPoQuantity(poDetailLine.getQuantity());
                    poDetail.setPurchaseRequestDetail(purchaseRequestDetail);

                    poDetail.setQuantity(poDetailLine.getQuantity());
                    poDetail.setUnitPrice(poDetailLine.getUnitPrice());
                    poDetail.setVat(poDetailLine.getVat());
                    poDetail.setDiscount(poDetailLine.getDiscount());
                    poDetail.setAmount(poDetailLine.getItemAmount());

                    JoDetail newJod = joDetailRepo.save(poDetail);
                    if(newJod != null){
                        PurchaseRequestDetailRepo.updatePoQuantityById(purchaseRequestDetail.getId(), purchaseRequestDetail.getPoQuantity());
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Job Order successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            JobOrder doc = jobOrderRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.JO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoListDto> findAll() {

        try {
            List<JobOrder> vouchers = jobOrderRepo.findAll();

            List<JoListDto> returnVouchers = new ArrayList<>();
            if (!Checker.collectionIsEmpty(vouchers)) {
                for(JobOrder jo : vouchers) {
                    JoListDto joListDto = new JoListDto();
                    joListDto.setId(jo.getId());
                    joListDto.setVoucherDate(jo.getVoucherDate());
                    joListDto.setLocalCode(jo.getCode());
                    joListDto.setSupplier(jo.getVendor().getName());
                    joListDto.setAmount(jo.getAmount());
                    joListDto.setStatus(jo.getDocumentStatus().getStatus());

                    SlEntity createdBy = slEntityRepo.findById(jo.getCreatedBy().getAccountNo()).orElse(null);
                    joListDto.setPreparedBy(createdBy.getName());

                    returnVouchers.add(joListDto);
                }

                return returnVouchers;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findJobOrderSuppliersByStatus(Integer statusId) {
        List<Map> mapList = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        List<JobOrder> vouchers = jobOrderRepo.findJobOrderSuppliersForJoa(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
        if (!Checker.collectionIsEmpty(vouchers)) {
            for (JobOrder jo : vouchers) {
                Integer accountNo = jo.getVendor().getAccountNo();
                if (seen.add(accountNo)) {
                    mapList.add(composeEntityMap(jo));
                }
            }
        }
        return mapList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoListDto> findBySupplierAccountNo(Integer accountNo) {
        List<JobOrder> vouchers = jobOrderRepo.findJobOrdersForJoa(accountNo, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<JoListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for (JobOrder jo : vouchers) {
                JoListDto joListDto = new JoListDto();
                joListDto.setId(jo.getId());
                joListDto.setVoucherDate(jo.getVoucherDate());
                joListDto.setLocalCode(jo.getCode());
                joListDto.setVendorAccountNo(jo.getVendor().getAccountNo());
                joListDto.setSupplier(jo.getVendor().getName());
                joListDto.setAmount(jo.getAmount());
                joListDto.setStatus(jo.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(jo.getCreatedBy().getAccountNo()).orElse(null);
                if (createdBy != null) joListDto.setPreparedBy(createdBy.getName());

                returnVouchers.add(joListDto);
            }
            return returnVouchers;
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JoListDto> findForCV() {
        List<JobOrder> vouchers = jobOrderRepo.findJobOrdersForCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<JoListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(JobOrder jo : vouchers) {
                JoListDto joListDto = new JoListDto();
                joListDto.setId(jo.getId());
                joListDto.setVoucherDate(jo.getVoucherDate());
                joListDto.setLocalCode(jo.getCode());
                joListDto.setSupplier(jo.getVendor().getName());
                joListDto.setAmount(jo.getAmount());
                joListDto.setStatus(jo.getDocumentStatus().getStatus());

                SlEntity createdBy = slEntityRepo.findById(jo.getCreatedBy().getAccountNo()).orElse(null);
                joListDto.setPreparedBy(createdBy.getName());

                joListDto.setBudgetLineItemDetail(jo.getBudgetLineItemDetail());

                returnVouchers.add(joListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer id) {

        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            List<JobOrder> docs = jobOrderRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeJOListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map> findByDateRangePending(String from, String to) {
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

            List<JobOrder> docs = jobOrderRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeJOListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Page<JobOrder> findByStatusAndFilter(Integer statusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return jobOrderRepo.findByStatusAndFilter(statusId, "%"+filter+"%", pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JobOrderBudgetDetail> getJobOrderBudgetDetail(Integer joId) {

        List<JobOrderBudgetDetail> jobOrderBudgetDetails = new ArrayList<>();

        try {

            jobOrderBudgetDetails = this.jobOrderBudgetDetailRepo.findAllByJobOrderId(joId);

            for (JobOrderBudgetDetail jobOrderBudgetDetail : jobOrderBudgetDetails){

                jobOrderBudgetDetail.setParent(jobOrderBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getTitle());

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return jobOrderBudgetDetails;

    }

    @Override
    public boolean isDocumentForCashFlowItemAssignment(Integer transactionId) {

        boolean isDocumentForCashFlowItemAssignment = false;

        try {

            JobOrder existingJO = this.jobOrderRepo.findOneByTransactionId(transactionId);

            if (existingJO != null && isCurrentUserAuthorized(existingJO) && isDocumentForBudgetChecking(existingJO)) {
                isDocumentForCashFlowItemAssignment = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isDocumentForCashFlowItemAssignment;

    }

    @Override
    public PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            JobOrder existingJo = this.jobOrderRepo.findOneByTransactionId(dto.getTransId());

            if(existingJo.getBudgetCheckedBy().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                existingJo.setCashFlowItemBalancePOJORFP(dto.getCashFlowItemBalancePOJORFP());
                existingJo.setCashFlowItemBalanceCV(dto.getCashFlowItemBalanceCV());
                existingJo.setCashFlowItemTotal(dto.getCashFlowItemTotal());

                this.model = jobOrderRepo.save(existingJo);

                if (this.model != null) {

                    jobOrderBudgetDetailRepo.deleteByJobOrderId(existingJo.getId());

                    ArrayList<BudgetSubItem> budgetDetails = dto.getBudgetSubItems();
                    for (BudgetSubItem budgetSubItem : budgetDetails){

                        JobOrderBudgetDetail newJobOrderBudgetDetail = new JobOrderBudgetDetail();

                        JobOrder jo = new JobOrder();
                        jo.setId(this.model.getId());
                        newJobOrderBudgetDetail.setJobOrder(jo);

                        newJobOrderBudgetDetail.setBudgetSubItem(budgetSubItem);
                        newJobOrderBudgetDetail.setAmount(budgetSubItem.getAmount());
                        newJobOrderBudgetDetail.setBudgetSubItemAmountBalanceCV(budgetSubItem.getBudgetSubItemAmountBalanceCV());
                        newJobOrderBudgetDetail.setBudgetSubItemAmountBalancePOJO(budgetSubItem.getBudgetSubItemAmountBalancePOJO());

                        jobOrderBudgetDetailRepo.save(newJobOrderBudgetDetail);

                    }

                    response.setSuccessMessage("Entries successfully saved!");
                    response.setSuccess(true);

                }

            } else {

                response.setFailureMessage("Invalid user or update is restricted!!");
                response.setSuccess(false);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public Page<JobOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(Integer statusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return jobOrderRepo.findAllForCreditCardPurchaseRequestByStatusAndFilter(statusId, "%"+filter+"%", pageable);
    }

    private Map composeEntityMap(JobOrder jo) {
        Map map = new HashMap();

        map.put("accountNo", jo.getVendor().getAccountNo());
        map.put("name", jo.getVendor().getName());
        map.put("address", jo.getVendor().getAddress());
        map.put("marker", jo.getVendor().getMarker());

        return map;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public JoDto findById(Integer id) {
        JobOrder jo =  jobOrderRepo.findById(id).orElse(null);
        JoDto joDto = new JoDto();

        if (jo != null) {
            joDto.setId(jo.getId());
            joDto.setLocalCode(jo.getCode());
            joDto.setTransId(jo.getTransaction().getId());
            joDto.setAmount(jo.getAmount());

            SlEntity createdBy = slEntityRepo.findById(jo.getCreatedBy().getAccountNo()).orElse(null);
            SlEntity budgetCheckedBy = slEntityRepo.findById(jo.getBudgetCheckedBy().getAccountNo()).orElse(null);
            SlEntity checkedBy = slEntityRepo.findById(jo.getCheckedBy().getAccountNo()).orElse(null);
            SlEntity approvedBy = slEntityRepo.findById(jo.getApprovingOfficer().getAccountNo()).orElse(null);

            SlEntity notedBy = null;

            if(jo.getNotedBy() != null) {
                notedBy = slEntityRepo.findById(jo.getNotedBy().getAccountNo()).orElse(null);
            } else {
                notedBy = (SlEntity) signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.JO).get("notedBy");
            }

            joDto.setCreatedBy(createdBy);
            joDto.setNotedBy(notedBy);
            joDto.setBudgetCheckedBy(budgetCheckedBy);
            joDto.setCheckedBy(checkedBy);
            joDto.setApprovedBy(approvedBy);
            joDto.setTerm(jo.getTerm());
            joDto.setVendor(jo.getVendor());
            joDto.setVoucherDate(jo.getVoucherDate());
            joDto.setDocumentStatus(jo.getDocumentStatus());
            joDto.setCreated(jo.getCreatedAt());
            joDto.setLastUpdated(jo.getUpdatedAt());
            joDto.setDescription(jo.getDescription());
            joDto.setPaymentTerm(jo.getPaymentTerm());
            joDto.setPaymentTermInWords(jo.getPaymentTermInWords());
            joDto.setBudgetLineItemBalancePOJORFP(jo.getBudgetLineItemBalancePOJORFP());
            joDto.setBudgetLineItemBalanceCV(jo.getBudgetLineItemBalanceCV());
            joDto.setCashFlowItemBalancePOJORFP(jo.getCashFlowItemBalancePOJORFP());
            joDto.setCashFlowItemBalanceCV(jo.getCashFlowItemBalanceCV());
            joDto.setCashFlowItemTotal(jo.getCashFlowItemTotal());

            List<JoDetail> joDetails = joDetailRepo.findByJobOrderId(jo.getId());

            if(!joDetails.isEmpty()){

                for(JoDetail joDetail : joDetails){

                    if(joDetail.getPurchaseRequestDetail() != null){

                        joDto.setPurchaseRequest(joDetail.getPurchaseRequestDetail().getPurchaseRequest());

                    }

                }

            }

        }

        return  joDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        JobOrder jobOrder = jobOrderRepo.findById(id).orElse(null);
        Supplier supplier = supplierRepo.findOneByAccountNumber(jobOrder.getVendor().getAccountNo());

        if (jobOrder != null) {
            params.put("VOUCHER_NO", jobOrder.getCode());
            params.put("V_DATE", jobOrder.getVoucherDate());
            params.put("SUPPLIER", supplier.getName());
            params.put("PURPOSE", jobOrder.getDescription());
            params.put("SUPPLIER_ADDRESS",supplier.getAddress());
            params.put("SUPPLIER_CONTACT", supplier.getPhone());
            params.put("SUPPLIER_TIN", StringFormatter.getValueOrBlank(supplier.getTin()).equals("") ? "": ("TIN # " + supplier.getTin()));
            params.put("AMOUNT", jobOrder.getAmount());

            if(Checker.isValidId(jobOrder.getPaymentTerm())){
                params.put("PAYMENT_TERM", jobOrder.getPaymentTerm() + " DAYS");
            } else {
                params.put("PAYMENT_TERM", jobOrder.getPaymentTermInWords());
            }

            if (jobOrder.getBudgetLineItemDetail() != null) {
                params.put("BUDGET_LINE_ITEM", jobOrder.getBudgetLineItemDetail().getTitle() + " - " + jobOrder.getBudgetLineItemDetail().getCode());
            } else {
                params.put("BUDGET_LINE_ITEM", "");
            }
            params.put("BUDGET_LINE_ITEM_BALANCE", jobOrder.getBudgetLineItemBalancePOJORFP());

            JobOrderBudgetDetail jobOrderBudgetDetail = jobOrderBudgetDetailRepo.findFirstByJobOrderIdOrderByIdAsc(jobOrder.getId());

            if(jobOrderBudgetDetail != null){
                params.put("CASH_FLOW_ITEM", jobOrderBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getTitle());
//                params.put("CASH_FLOW_ITEM_AMOUNT", jobOrder.getCashFlowItemBalancePOJORFP().subtract(jobOrder.getCashFlowItemTotal()));
                params.put("CASH_FLOW_ITEM_AMOUNT", BigDecimal.ZERO);
            }

            List<JoDetail> joDetails = this.joDetailRepo.findByJobOrderId(jobOrder.getId());

            PurchaseRequest purchaseRequest = null;

            if(Checker.collectionIsNotEmpty(joDetails)){
                for (JoDetail joDetail : joDetails){
                    if(purchaseRequest == null) {
                        PurchaseRequestDetail purchaseRequestDetail = this.purchaseRequestDetailRepo.findById(joDetail.getPurchaseRequestDetail().getId()).orElse(null);
                        if(purchaseRequestDetail != null) {
                            purchaseRequest = purchaseRequestDetail.getPurchaseRequest();
                        }
                    }
                }
            }

            if(purchaseRequest != null) {

                Employee requestedBy = employeeRepo.findOneByAccountNumber(purchaseRequest.getCreatedBy().getAccountNo());

                params.put("REQUESTED_BY", requestedBy.getName());
                params.put("REQUESTED_BY_POS", requestedBy.getPosition() != null ? requestedBy.getPosition().getName() : "");

                if (requestedBy.getSignature() != null) {
                    params.put("REQUESTED_BY_SIGN", env.getProperty("path.attachments") + requestedBy.getSignature().getFilename());
                }

            }

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.JO, jobOrder);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<JODetail> details = new ArrayList<>();

        JobOrder voucher = jobOrderRepo.findById(id).orElse(null);
        if (voucher != null) {

            List<JoDetail> works = joDetailRepo.findWorksByJobOrderId(voucher.getId());
            if (!works.isEmpty()) {
                for(JoDetail detail:works) {

                    JODetail d = new JODetail();
                    d.setId(detail.getId());
                    d.setDescription(detail.getPurchaseRequestDetail().getJoDescription());
                    d.setUnitCode(detail.getPurchaseRequestDetail().getUnitMeasure().getCode());
                    d.setQuantity(detail.getQuantity());
                    d.setUnitPrice(detail.getUnitPrice());
                    d.setAmount(detail.getAmount());

                    details.add(d);
                }
            }

            List<JoDetail> materials = joDetailRepo.findMaterialsByJobOrderId(voucher.getId());
            if (!materials.isEmpty()) {

                if (!works.isEmpty()) {

                    JODetail blank1 = new JODetail();
                    blank1.setDescription("");
                    details.add(blank1);

                    JODetail blank2 = new JODetail();
                    blank2.setDescription("Materials:");
                    details.add(blank2);
                }

                for(JoDetail detail:materials) {

                    JODetail d = new JODetail();
                    d.setId(detail.getId());
                    d.setDescription(detail.getPurchaseRequestDetail().getItem().getDescription());
                    d.setUnitCode(detail.getPurchaseRequestDetail().getUnitMeasure().getCode());
                    d.setQuantity(detail.getQuantity());
                    d.setUnitPrice(detail.getUnitPrice());
                    d.setAmount(detail.getAmount());

                    details.add(d);
                }
            }

        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        JobOrder jobOrder =  jobOrderRepo.findById(postData.getDocumentId()).orElse(null);

        if (jobOrder != null) {
            // for logging
            Map oldMap = this.forLogMapMain(jobOrder);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(jobOrder, jobOrder.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            jobOrder.setDocumentStatus(afterActionDocumentStatus);
            jobOrder.setUpdatedAt(null);
            jobOrder = jobOrderRepo.save(jobOrder);

            // for logging
            Map newMap = this.forLogMapMain(jobOrder);
            newMap.put("remarks", postData.getRemarks());

            if (jobOrder != null) {
                documentProcessingFacade.processAction(jobOrder.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(jobOrder.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        List<DocumentStatus> statuses1 = documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.JO.getId());
        List<DocumentStatus> statuses2 = documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.JO_FORBIDDING.getId());

        List<DocumentStatus> statuses = new ArrayList<>();
        if(!statuses1.isEmpty()) {
            statuses.addAll(statuses1);
            statuses = this.getExtraStatuses(statuses, statuses2);
        } else if(!statuses2.isEmpty()) {
            statuses.addAll(statuses2);
            statuses = this.getExtraStatuses(statuses, statuses1);
        }

        return statuses;
    }

    private List<DocumentStatus> getExtraStatuses(List<DocumentStatus> returnStatuses, List<DocumentStatus> newStatuses) {
        if(!newStatuses.isEmpty()) {
            for(DocumentStatus status:newStatuses) {
                boolean inReturnStatuses = false;
                for(DocumentStatus exStatus:returnStatuses) {
                    if(status.getId().equals(exStatus.getId())) {
                        inReturnStatuses = true;
                        break;
                    }
                }

                if(!inReturnStatuses) returnStatuses.add(status);
            }
        }

        return returnStatuses;

    }

    private Map forLogMapMain(JobOrder jo) {
        return  documentLoggerFacade.makeLog(jo);
    }

    private List<Map> makeJOListMap(List<JobOrder> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(JobOrder c:cs) {
                mapList.add(composeJOMap(c));
            }
        }
        return mapList;
    }

    private Map composeJOMap(JobOrder r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("localCode", r.getCode());
        map.put("voucherDate", r.getVoucherDate());
        map.put("supplier", r.getVendor().getName());
        map.put("amount", r.getAmount());
        map.put("preparedBy", r.getCreatedBy().getFullName());
        map.put("status", r.getDocumentStatus().getStatus());

        return map;
    }

    private boolean isCurrentUserAuthorized(JobOrder existingJO) {
        return existingJO.getBudgetCheckedBy().getAccountNo().equals(this.authenticationFacade.getLoggedIn().getAccountNo());
    }

    private boolean isDocumentForBudgetChecking(JobOrder existingJO) {
        return existingJO.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_CHECKING.getId());
    }

}