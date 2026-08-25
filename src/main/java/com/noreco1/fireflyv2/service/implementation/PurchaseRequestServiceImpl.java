package com.noreco1.fireflyv2.service.implementation;

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
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.RVDetail;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.PurchaseRequestService;
import com.noreco1.fireflyv2.validator.RvValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
 * Created by Personal on 3/20/2015.
 */
@Service(value = "rvServiceImpl")
public class PurchaseRequestServiceImpl implements PurchaseRequestService, PrintableVoucher {

    private PurchaseRequest model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    PurchaseRequestRepo purchaseRequestRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    PurchaseRequestDetailServiceImpl rvDetailDto;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Autowired
    CanvassRepo canvassRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    ModeOfProcurementRepo modeOfProcurementRepo;

    @Autowired
    SettingFacade settingFacade;
    @Autowired
    RoleRepo roleRepo;

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequest findByCode(String code) {
        List<PurchaseRequest> purchaseRequests = purchaseRequestRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(purchaseRequests)) {
            return purchaseRequests.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PurchaseRequest rv = (PurchaseRequest) v;
        return this.processCreate(rv, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PurchaseRequest rv = (PurchaseRequest) v;
        PostResponse response = new PostResponse();

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        RvValidator validator = new RvValidator();
        validator.setService(this);
        validator.validate(rv, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            PurchaseRequest existingRv = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(rv.getVoucherDate()));

            if (rv.getApprovingOfficer() == null || rv.getApprovingOfficer().getAccountNo() == null) {
                bindingResult.rejectValue("approvingOfficer", "error.approvingOfficer", "Please select approving officer.");
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            User approvedBy = userRepo.findOneByAccountNo(rv.getApprovingOfficer().getAccountNo());
            if (approvedBy == null) {
                bindingResult.rejectValue("approvingOfficer", "error.approvingOfficer", "Selected approving officer does not have a system account. Please select a valid user.");
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            User inventoryCheckedBy = rv.getInventoryCheckedBy() != null ? userRepo.findOneByAccountNo(rv.getInventoryCheckedBy().getAccountNo()):null;
            User reviewedAcceptedBy = rv.getReviewedAcceptedBy() != null ? userRepo.findOneByAccountNo(rv.getReviewedAcceptedBy().getAccountNo()):null;
            Boolean insertMode = rv.getId() == null;
            if (insertMode) { // insert mode

                Employee employee = this.employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                String departmentAbbreviation = employee != null ? employee.getDepartment().getAbbreviation() : "";

                String prefix;

                if(rv.getRvType() == RvType.FOR_PO.getId()){
                    prefix = "PR-" + departmentAbbreviation;
                } else {
                    prefix = "WR-" + departmentAbbreviation;
                }

                Object latestApvCode = purchaseRequestRepo.findLatestRvCodeByYear(voucherYear, "%"+prefix+"%");

                rv.setCode(generatorFacade.voucherCode(prefix, (latestApvCode == null ? "" : String.valueOf(latestApvCode)), rv.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                rv.setDocumentStatus(documentStatus);

                rv.setTransaction(generatorFacade.transaction());
                rv.setCreatedBy(createdBy);
                rv.setDepartment(employee != null ? employee.getDepartment() : null);
                rv.setCreatedAt(new java.util.Date());
                rv.setUpdatedAt(new java.util.Date());
                existingRv = rv;

            } else {

                existingRv = purchaseRequestRepo.findById(rv.getId()).orElse(null);

                if (existingRv == null) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("Request is not available");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }

                // check application status, edit is allowed only to 'Document Create' and 'Returned to Creator'
                if (!( existingRv.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId()) ||
                        existingRv.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId())) ) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Request is not available for editing");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);

                    return response;

                }

                // employee created can only edit its Document
                Integer accountNumber = existingRv.getCreatedBy().getAccountNo();

                if (!accountNumber.equals(createdBy.getAccountNo())) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("You are not authorized to update Request");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);

                    return response;
                }

            }

            if(rv.getRvType() == RvType.FOR_LAB.getId()){
                existingRv.setEmployee(rv.getEmployee());
                existingRv.setDurationStart(rv.getDurationStart());
                existingRv.setDurationEnd(rv.getDurationEnd());
            }

            Map oldMap = this.forLogMapMain(existingRv);

            Workflow wf = new Workflow();

            if(inventoryCheckedBy != null){
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RV_WAREHOUSE.getId());
            } else {
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RV.getId());
            }

            existingRv.setWorkflow(wf);

            existingRv.setVoucherDate(rv.getVoucherDate());
            existingRv.setDeliveryDate(rv.getDeliveryDate());
            existingRv.setPurpose(rv.getPurpose());
            existingRv.setApprovingOfficer(approvedBy);
            existingRv.setYear(voucherYear);
            existingRv.setRvItType(rv.getRvItType());
            existingRv.setEstimatedAmount(rv.getEstimatedAmount());
            existingRv.setBudgetLineItemDetail(rv.getBudgetLineItemDetail());
            existingRv.setBudgetSubItem(rv.getBudgetSubItem());
            existingRv.setInventoryCheckedBy(inventoryCheckedBy);
            existingRv.setReviewedAcceptedBy(reviewedAcceptedBy);
            existingRv.setEmergencyPurchase(rv.getEmergencyPurchase());
            existingRv.setWorkOrder(rv.getWorkOrder());
            existingRv.setVehicle(rv.getVehicle());
            existingRv.setCostEstimate(rv.getCostEstimate());
            existingRv.setOffice(rv.getOffice());
            existingRv.setUpdatedAt(new java.util.Date());

            this.model = purchaseRequestRepo.save(existingRv);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.rv(this.model);
                // end: update default signatories

                if (!insertMode) {
                    PurchaseRequestDetailRepo.deleteByPurchaseRequestId(this.model.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<RvDetailDto> rvDetails = rv.getRvDetails();
                for(RvDetailDto rvDetailLine: rvDetails) {

                    PurchaseRequestDetail purchaseRequestDetail = new PurchaseRequestDetail();
                    purchaseRequestDetail.setQuantity(rvDetailLine.getQuantity());
                    purchaseRequestDetail.setPoQuantity(BigDecimal.ZERO);

                    PurchaseRequest purchaseRequest = new PurchaseRequest();
                    purchaseRequest.setId(this.model.getId());
                    purchaseRequestDetail.setPurchaseRequest(purchaseRequest);

                    if (rvDetailLine.getItemId() != 0) {
                        Item item = new Item();
                        item.setId(rvDetailLine.getItemId());
                        purchaseRequestDetail.setItem(item);
                        purchaseRequestDetail.setJoDescription(rvDetailLine.getJoDescription());
                    } else {
                        // No item selected — store itemDescription in joDescription so it survives round-trips
                        String desc = rvDetailLine.getItemDescription();
                        String jo   = rvDetailLine.getJoDescription();
                        purchaseRequestDetail.setJoDescription(
                            (desc != null && !desc.isEmpty()) ? desc : jo
                        );
                    }

                    UnitMeasure unit = new UnitMeasure();
                    unit.setId(rvDetailLine.getUnitId());
                    purchaseRequestDetail.setUnitMeasure(unit);

                    PurchaseRequestDetailRepo.save(purchaseRequestDetail);
                }
                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("PR successfully saved!");
                response.setSuccess(true);
            }
        }

        if (Checker.documentSaved(response)) {
            this.logNewValue(response.getLogId());
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            PurchaseRequest doc = purchaseRequestRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.RV);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RvListDto> findAll() {

        List<PurchaseRequest> vouchers = purchaseRequestRepo.findByOrderByIdDesc();

        List<RvListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseRequest rv : vouchers) {
                RvListDto rvListDto = new RvListDto();
                rvListDto.setId(rv.getId());
                rvListDto.setVoucherDate(rv.getVoucherDate());
                rvListDto.setDeliveryDate(rv.getDeliveryDate());
                rvListDto.setLocalCode(rv.getCode());
                rvListDto.setPurpose(rv.getPurpose());
                rvListDto.setStatus(rv.getDocumentStatus().getStatus());
                if (rv.getRvType() == RvType.FOR_PO.getId()) {
                    rvListDto.setRvType(RvType.FOR_PO.getDescription());
                } else if (rv.getRvType() == RvType.FOR_IT.getId()){
                    rvListDto.setRvType(RvType.FOR_IT.getDescription());
                } else if (rv.getRvType() == RvType.FOR_REP.getId()){
                    rvListDto.setRvType(RvType.FOR_REP.getDescription());
                } else if (rv.getRvType() == RvType.FOR_LAB.getId()){
                    rvListDto.setRvType(RvType.FOR_LAB.getDescription());
                }
                rvListDto.setRvTypeId(rv.getRvType());

                if (rv.getCreatedBy() != null) {
                    SlEntity createdBy = slEntityRepo.findById(rv.getCreatedBy().getAccountNo()).orElse(null);
                    rvListDto.setPreparedBy(createdBy == null ? "" : createdBy.getName());
                }

                returnVouchers.add(rvListDto);
            }
        }
        return returnVouchers;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvListDto> findAllWithQuotations() {
        List<PurchaseRequest> vouchers = purchaseRequestRepo.findPurchaseRequestsWithQuotations();

        List<RvListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseRequest rv : vouchers) {
                RvListDto rvListDto = new RvListDto();
                rvListDto.setId(rv.getId());
                rvListDto.setVoucherDate(rv.getVoucherDate());
                rvListDto.setDeliveryDate(rv.getDeliveryDate());
                rvListDto.setLocalCode(rv.getCode());
                rvListDto.setPurpose(rv.getPurpose());
                rvListDto.setStatus(rv.getDocumentStatus().getStatus());
                if (rv.getRvType() == RvType.FOR_PO.getId()) {
                    rvListDto.setRvType(RvType.FOR_PO.getDescription());
                } else if (rv.getRvType() == RvType.FOR_IT.getId()){
                    rvListDto.setRvType(RvType.FOR_IT.getDescription());
                } else if (rv.getRvType() == RvType.FOR_REP.getId()){
                    rvListDto.setRvType(RvType.FOR_REP.getDescription());
                } else if (rv.getRvType() == RvType.FOR_LAB.getId()){
                    rvListDto.setRvType(RvType.FOR_LAB.getDescription());
                }
                rvListDto.setRvTypeId(rv.getRvType());

                SlEntity createdBy = slEntityRepo.findById(rv.getCreatedBy().getAccountNo()).orElse(null);
                rvListDto.setPreparedBy(createdBy == null ? "":createdBy.getName());
                rvListDto.setBudgetLineItemDetail(rv.getBudgetLineItemDetail());

                returnVouchers.add(rvListDto);
            }
        }
        return returnVouchers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RvListDto> findAllRestricted() {

        User loggedIn = authenticationFacade.getLoggedIn();
        List<PurchaseRequest> vouchers = purchaseRequestRepo.findAllByAllowedUsers(loggedIn.getId());

        List<RvListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseRequest rv : vouchers) {
                RvListDto rvListDto = new RvListDto();
                rvListDto.setId(rv.getId());
                rvListDto.setVoucherDate(rv.getVoucherDate());
                rvListDto.setDeliveryDate(rv.getDeliveryDate());
                rvListDto.setLocalCode(rv.getCode());
                rvListDto.setPurpose(rv.getPurpose());
                rvListDto.setStatus(rv.getDocumentStatus().getStatus());
                if (rv.getRvType() == RvType.FOR_PO.getId()) {
                    rvListDto.setRvType(RvType.FOR_PO.getDescription());
                } else if (rv.getRvType() == RvType.FOR_IT.getId()){
                    rvListDto.setRvType(RvType.FOR_IT.getDescription());
                } else if (rv.getRvType() == RvType.FOR_REP.getId()){
                    rvListDto.setRvType(RvType.FOR_REP.getDescription());
                } else if (rv.getRvType() == RvType.FOR_LAB.getId()){
                    rvListDto.setRvType(RvType.FOR_LAB.getDescription());
                }
                rvListDto.setRvTypeId(rv.getRvType());

                SlEntity createdBy = slEntityRepo.findById(rv.getCreatedBy().getAccountNo()).orElse(null);
                rvListDto.setPreparedBy(createdBy == null ? "":createdBy.getName());

                returnVouchers.add(rvListDto);
            }
        }
        return returnVouchers;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvListDto> getRequisitionVoucherForCanvass(Integer[] canvassIds) {
        List<Object[]> vouchers = purchaseRequestRepo.findPurchaseRequestsForCanvass(Arrays.asList(canvassIds));

        List<RvListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(Object[] rv : vouchers) {
                RvListDto rvListDto = new RvListDto();
                rvListDto.setId((Integer)rv[0]);
                rvListDto.setVoucherDate((Date) rv[1]);
                rvListDto.setDeliveryDate((Date) rv[2]);
                rvListDto.setLocalCode((String)rv[3]);
                rvListDto.setPurpose((String)rv[4]);
                rvListDto.setStatus((String)rv[5]);
                Integer rvTypeId = (Integer)rv[6];
                if (rvTypeId == RvType.FOR_PO.getId()) {
                    rvListDto.setRvType(RvType.FOR_PO.getDescription());
                } else if (rvTypeId == RvType.FOR_IT.getId()){
                    rvListDto.setRvType(RvType.FOR_IT.getDescription());
                } else if (rvTypeId == RvType.FOR_REP.getId()){
                    rvListDto.setRvType(RvType.FOR_REP.getDescription());
                } else if (rvTypeId == RvType.FOR_LAB.getId()){
                    rvListDto.setRvType(RvType.FOR_LAB.getDescription());
                }
                rvListDto.setTransId((Integer)rv[7]);
                rvListDto.setRvTypeId(rvTypeId);

                rvListDto.setPreparedBy((String)rv[8] == null ? "":(String)rv[8]);
                rvListDto.setCanvassNo((String)rv[9]);

                returnVouchers.add(rvListDto);
            }
        }
        return returnVouchers;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvListDto> getRequisitionVoucherForPO() {
        List<PurchaseRequest> vouchers = purchaseRequestRepo.findPurchaseRequestsForPO(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<RvListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(PurchaseRequest rv : vouchers) {
                RvListDto rvListDto = new RvListDto();
                rvListDto.setId(rv.getId());
                rvListDto.setVoucherDate(rv.getVoucherDate());
                rvListDto.setDeliveryDate(rv.getDeliveryDate());
                rvListDto.setLocalCode(rv.getCode());
                rvListDto.setPurpose(rv.getPurpose());
                rvListDto.setStatus(rv.getDocumentStatus().getStatus());
                if (rv.getRvType() == RvType.FOR_PO.getId()) {
                    rvListDto.setRvType(RvType.FOR_PO.getDescription());
                } else if (rv.getRvType() == RvType.FOR_IT.getId()){
                    rvListDto.setRvType(RvType.FOR_IT.getDescription());
                } else if (rv.getRvType() == RvType.FOR_REP.getId()){
                    rvListDto.setRvType(RvType.FOR_REP.getDescription());
                } else if (rv.getRvType() == RvType.FOR_LAB.getId()){
                    rvListDto.setRvType(RvType.FOR_LAB.getDescription());
                }
                rvListDto.setTransId(rv.getTransaction().getId());
                rvListDto.setRvTypeId(rv.getRvType());

                SlEntity createdBy = slEntityRepo.findById(rv.getCreatedBy().getAccountNo()).orElse(null);
                rvListDto.setPreparedBy(createdBy == null ? "":createdBy.getName());

                returnVouchers.add(rvListDto);
            }
        }
        return returnVouchers;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Map<String, Object>> getRequisitionVoucherForStockWithdrawal(String query, Integer invLocId, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return purchaseRequestRepo.findPurchaseRequestsForStockWithdrawal(invLocId, authenticationFacade.getLoggedIn().getId(), pageable);
        } else {
            return purchaseRequestRepo.findPurchaseRequestsForStockWithdrawal("%"+query+"%", invLocId, authenticationFacade.getLoggedIn().getId(), pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Map<String, Object>> getRequisitionVoucherForRR(String query, Pageable pageable) {
//        if (Checker.isStringNullAndEmpty(query)) {
//            return purchaseRequestRepo.findPurchaseRequestsForRR(pageable);
//        } else {
//            return purchaseRequestRepo.findPurchaseRequestsForRR("%"+query+"%", pageable);
//        }

        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PurchaseRequest> getPurchaseRequestForPOBudgetAmountBalance() {

        List<PurchaseRequest> purchaseRequests = new ArrayList<>();

        try {

            purchaseRequests = purchaseRequestRepo.findPurchaseRequestsForPoAmountBudgetBalance();

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return purchaseRequests;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvListDto> getPurchaseRequestForCanvass() {

        List<RvListDto> rvListDtoArrayList = new ArrayList<>();

        try {

            List types = new ArrayList();
            types.add(RvType.FOR_PO.getId());
            types.add(RvType.FOR_IT.getId());
            types.add(RvType.FOR_LAB.getId());
            types.add(RvType.FOR_REP.getId());

            List<PurchaseRequest> purchaseRequests = purchaseRequestRepo.findAllPurchaseRequestForCanvass(types, com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId());

            for(PurchaseRequest purchaseRequest : purchaseRequests){

                RvListDto rvListDto = new RvListDto();

                rvListDto.setId(purchaseRequest.getId());
                rvListDto.setLocalCode(purchaseRequest.getCode());
                rvListDto.setVoucherDate(purchaseRequest.getVoucherDate());
                rvListDto.setPurpose(purchaseRequest.getPurpose());
                rvListDto.setPreparedBy(purchaseRequest.getCreatedBy().getFullName());
                rvListDto.setTransId(purchaseRequest.getTransaction().getId());

                rvListDtoArrayList.add(rvListDto);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return rvListDtoArrayList;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            // Extend toDate to end of day so records created later in the day are included
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            toDate = cal.getTime();

            User loggedIn = authenticationFacade.getLoggedIn();

            List<PurchaseRequest> docs;

            if(isPurchasingOfficer(loggedIn.getId())){

                if(Checker.isValidId(docStatusId)){
                    docs = purchaseRequestRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusId(loggedIn.getId(), fromDate, toDate, docStatusId);
                } else {
                    docs = purchaseRequestRepo.findByDocumentStatusIdAndVoucherDateBetween(com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), fromDate, toDate);
                }

            } else {
                docs = purchaseRequestRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusId(loggedIn.getId(), fromDate, toDate, docStatusId);
            }

            return this.makeRIVListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
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

            // Extend toDate to end of day so records created later in the day are included
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            toDate = cal.getTime();

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANVASSED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            User loggedIn = authenticationFacade.getLoggedIn();

            List<PurchaseRequest> docs;

            if(isPurchasingOfficer(loggedIn.getId())){
                docs = purchaseRequestRepo.findByDocumentStatusIdAndVoucherDateBetween(com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), fromDate, toDate);
            } else {
                docs = purchaseRequestRepo.findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotIn(loggedIn.getId(), fromDate, toDate, Arrays.asList(ids));
            }

            return this.makeRIVListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<com.noreco1.fireflyv2.model.ModeOfProcurement> modesOfProcurement(int rvId) {

        try {

            PurchaseRequest voucher = purchaseRequestRepo.findById(rvId).orElse(null);

            if(voucher != null) {

                Integer accountNo = voucher.getReviewedAcceptedBy().getAccountNo();

                if(accountNo.equals(authenticationFacade.getLoggedIn().getAccountNo()) || isPurchasingOfficer(authenticationFacade.getLoggedIn().getId())) {

                    if(voucher.getRvType().equals(RvType.FOR_PO.getId())){
                        return modeOfProcurementRepo.findAllByIsForPOTrue();
                    } else {
                        return modeOfProcurementRepo.findAll();
                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return new ArrayList<>();

    }

    @Override
    public PostResponse setModeOfProcurement(SetModeOfProcurementDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        PurchaseRequest purchaseRequest = purchaseRequestRepo.findById(postData.getDocumentId()).orElse(null);

        if (purchaseRequest != null && purchaseRequest.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId())) {
            // for logging
            Map oldMap = this.forLogMapMain(purchaseRequest);

            purchaseRequest.setUpdatedAt(null);
            purchaseRequest.setBacDate(new Date());
            purchaseRequest.setModeOfProcurement(postData.getMode());

            purchaseRequest = purchaseRequestRepo.save(purchaseRequest);

            // for logging
            Map newMap = this.forLogMapMain(purchaseRequest);
            newMap.put("remarks", postData.getRemarks());

            if (purchaseRequest != null) {
                documentLoggerFacade.log(purchaseRequest.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Mode of procurement has been set.");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public RvDto findByRvId(Integer id) {
        User loggedIn = authenticationFacade.getLoggedIn();

        PurchaseRequest purchaseRequest = purchaseRequestRepo.findById(id).orElse(null);
        RvDto rvDto = new RvDto();

        if (purchaseRequest != null) {
            if (!(purchaseRequest.getApprovingOfficer().getId().equals(loggedIn.getId()) ||
                    purchaseRequest.getCreatedBy().getId().equals(loggedIn.getId()) ||
                    (purchaseRequest.getReviewedAcceptedBy() != null && purchaseRequest.getReviewedAcceptedBy().getId().equals(loggedIn.getId())))
                    ) {

                // check if in APPROVED_VOUCHERS_USER_ROLES
                List<Integer> roleIds = settingFacade.approvedVouchersPurchasingRoles();
                List<Role> roles = roleRepo.findByUserIdAndRoles(loggedIn.getId(), roleIds);

                if(roles.isEmpty()) {
                    return null;
                }
            }

            if(purchaseRequest.getBudgetSubItem() != null){
                purchaseRequest.getBudgetLineItemDetail().setHasSubItems(Boolean.TRUE);
            }

            rvDto.setId(purchaseRequest.getId());
            rvDto.setPurpose(purchaseRequest.getPurpose());
            rvDto.setLocalCode(purchaseRequest.getCode());
            rvDto.setTransId(purchaseRequest.getTransaction().getId());

            SlEntity createdBy = slEntityRepo.findById(purchaseRequest.getCreatedBy().getAccountNo()).orElse(null);
            SlEntity approvedBy = slEntityRepo.findOneByAccountNo(purchaseRequest.getApprovingOfficer().getAccountNo());

            SlEntity inventoryCheckedBy = null;

            if(purchaseRequest.getInventoryCheckedBy() != null){
                inventoryCheckedBy = slEntityRepo.findById(purchaseRequest.getInventoryCheckedBy().getAccountNo()).orElse(null);
            }

            SlEntity reviewedAcceptedBy = purchaseRequest.getReviewedAcceptedBy() != null
                    ? slEntityRepo.findById(purchaseRequest.getReviewedAcceptedBy().getAccountNo()).orElse(null) : null;

            if(purchaseRequest.getRvType() == RvType.FOR_IT.getId()) {
                rvDto.setRvItType(purchaseRequest.getRvItType());
            }
            if (purchaseRequest.getRvType() == RvType.FOR_PO.getId()) {
                rvDto.setRvType(RvType.FOR_PO.getDescription());
            } else if (purchaseRequest.getRvType() == RvType.FOR_IT.getId()){
                rvDto.setRvType(RvType.FOR_IT.getDescription());
            } else if (purchaseRequest.getRvType() == RvType.FOR_REP.getId()){
                rvDto.setRvType(RvType.FOR_REP.getDescription());
            } else if (purchaseRequest.getRvType() == RvType.FOR_LAB.getId()){
                rvDto.setRvType(RvType.FOR_LAB.getDescription());
                if(purchaseRequest.getEmployee() != null) {
                    SlEntity employee = slEntityRepo.findById(purchaseRequest.getEmployee().getAccountNo()).orElse(null);
                    rvDto.setEmployee(employee);
                }
                rvDto.setDurationStart(purchaseRequest.getDurationStart());
                rvDto.setDurationEnd(purchaseRequest.getDurationEnd());
            }

            rvDto.setCreatedBy(createdBy);
            rvDto.setInventoryCheckedBy(inventoryCheckedBy);
            rvDto.setReviewedAcceptedBy(reviewedAcceptedBy);
            rvDto.setApprovedBy(approvedBy);
            rvDto.setDeliveryDate(purchaseRequest.getDeliveryDate());
            rvDto.setVoucherDate(purchaseRequest.getVoucherDate());
            rvDto.setDocumentStatus(purchaseRequest.getDocumentStatus());
            rvDto.setCreated(purchaseRequest.getCreatedAt());
            rvDto.setLastUpdated(purchaseRequest.getUpdatedAt());
            rvDto.setModeOfProcurement(purchaseRequest.getModeOfProcurement());
            rvDto.setEstimatedAmount(purchaseRequest.getEstimatedAmount());
            rvDto.setOffice(purchaseRequest.getOffice());
            Employee deptEmployee = employeeRepo.findOneByAccountNumber(purchaseRequest.getCreatedBy().getAccountNo());
            if (deptEmployee != null && deptEmployee.getPosition() != null) {
                rvDto.setDepartment(deptEmployee.getPosition().getDepartment());
            }
            rvDto.setBudgetLineItemDetail(purchaseRequest.getBudgetLineItemDetail());
            rvDto.setBudgetSubItem(purchaseRequest.getBudgetSubItem());
            rvDto.setEmergencyPurchase(purchaseRequest.getEmergencyPurchase());
            rvDto.setWorkOrder(purchaseRequest.getWorkOrder());
            rvDto.setVehicle(purchaseRequest.getVehicle());
            rvDto.setCostEstimate(purchaseRequest.getCostEstimate());
            rvDto.setRvTypeId(purchaseRequest.getRvType());
        }

        return  rvDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        PurchaseRequest purchaseRequest = purchaseRequestRepo.findById(id).orElse(null);

        if (purchaseRequest != null) {

            String reportTile;

            if(purchaseRequest.getRvType() == RvType.FOR_PO.getId()){
                reportTile = "PURCHASE REQUEST";
            } else {
                reportTile = "WORK REQUEST";
            }

            Employee requestedBy = employeeRepo.findOneByAccountNumber(purchaseRequest.getCreatedBy().getAccountNo());

            params.put("REPORT_TITLE", reportTile);
            params.put("VOUCHER_NO", purchaseRequest.getCode());
            params.put("V_DATE", purchaseRequest.getVoucherDate());
            params.put("ESTIMATED_AMOUNT", purchaseRequest.getEstimatedAmount());

            params.put("NAME", requestedBy != null ? requestedBy.getName() : "");
            params.put("POSITION", requestedBy != null && requestedBy.getPosition() != null ? requestedBy.getPosition().getName() : "");
            params.put("DEPARTMENT", requestedBy != null && requestedBy.getPosition() != null && requestedBy.getPosition().getDepartment() != null ? requestedBy.getPosition().getDepartment().getName() : "");
            params.put("PURPOSE", purchaseRequest.getPurpose());
            params.put("REMARKS", "");
            params.put("EMPLOYEE", purchaseRequest.getEmployee() == null ? "" : purchaseRequest.getEmployee().getName());
            params.put("DURATION_S", purchaseRequest.getDurationStart() == null ? "" : purchaseRequest.getDurationStart());
            params.put("DURATION_E", purchaseRequest.getDurationEnd() == null ? "" : purchaseRequest.getDurationEnd());
            params.put("IT_TYPE", purchaseRequest.getRvItType() == null ? "" : purchaseRequest.getRvItType());
            params.put("BUDGET_SOURCE", purchaseRequest.getBudgetLineItemDetail() != null ? purchaseRequest.getBudgetLineItemDetail().getTitle() +" - "+ purchaseRequest.getBudgetLineItemDetail().getCode():"");

            params.put("WORKFLOW", purchaseRequest.getWorkflow().getId());
            params.put("WORK_ORDER", purchaseRequest.getWorkOrder() != null ? purchaseRequest.getWorkOrder().getCode() : "");
            params.put("IS_EMERGENCY_PURCHASE", purchaseRequest.getEmergencyPurchase() ? "Yes" : "No");
            java.net.URL subreportUrl = getClass().getResource("/jasper/vouchers/sub_reports/");
            params.put("SUBREPORT_DIR", subreportUrl != null ? subreportUrl.toString() + "/" : "jasper/vouchers/sub_reports/");

            params = signatureFacade.getDocumentSignature(params, DocumentType.RV, purchaseRequest);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<RVDetail> details = new ArrayList<>();

        PurchaseRequest voucher = purchaseRequestRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<RvDetailDto> rvDetailLineDtos = rvDetailDto.getRvDetailsWithItemGroup(id);

            if (!Checker.collectionIsEmpty(rvDetailLineDtos)) {
                for(RvDetailDto dto : rvDetailLineDtos) {
                    RVDetail d = new RVDetail();

                    d.setId(rvDetailLineDtos.indexOf(dto) + 1);
                    d.setDescription(dto.getJoDescription() == null ? dto.getItemDescription() : dto.getJoDescription());
                    d.setUnitCode(dto.getUnitCode());
                    d.setQuantity(dto.getQuantity());
                    d.setItemGroup(dto.getItemGroup());
                    d.setItemGroupName(dto.getItemGroupName());

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
        PurchaseRequest purchaseRequest = purchaseRequestRepo.findById(postData.getDocumentId()).orElse(null);

        if (purchaseRequest != null) {
            // for logging
            Map oldMap = this.forLogMapMain(purchaseRequest);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(purchaseRequest, purchaseRequest.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            purchaseRequest.setDocumentStatus(afterActionDocumentStatus);
            purchaseRequest = purchaseRequestRepo.save(purchaseRequest);

            // update canvass too
            if (afterActionDocumentStatus.getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANVASSED.getId())) {
                // RIV status will be "Canvassed" while Canvass will be "RIV For PO/JO"
                DocumentStatus rivForPO = new DocumentStatus();
                rivForPO.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.RIV_FOR_PO_JO.getId());

                List<CanvassDetail> canvassDetails = canvassDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(purchaseRequest.getId());
                if (!canvassDetails.isEmpty()) {
                    for (CanvassDetail canvassDetail : canvassDetails) {
                        Canvass canvass = canvassDetail.getCanvass();
                        if (canvass != null) {
                            canvass.setDocumentStatus(rivForPO);
                            canvassRepo.save(canvass);
                        }
                    }
                }
            }

            // for logging
            Map newMap = this.forLogMapMain(purchaseRequest);
            newMap.put("remarks", postData.getRemarks());

            if (purchaseRequest != null) {
                documentProcessingFacade.processAction(purchaseRequest.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(purchaseRequest.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

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
        PurchaseRequest voucher = purchaseRequestRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(PurchaseRequest rv) {
        return documentLoggerFacade.makeLog(rv);
    }

    private List<Map> makeRIVListMap(List<PurchaseRequest> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(PurchaseRequest c:cs) {
                mapList.add(composeRivMap(c));
            }
        }
        return mapList;
    }

    private Map composeRivMap(PurchaseRequest r) {
        Map map = new HashMap();

        map.put("id", r.getId());
        map.put("localCode", r.getCode());
        map.put("deliveryDate", r.getDeliveryDate());
        map.put("purpose", r.getPurpose());

        if (r.getRvType() == RvType.FOR_PO.getId()) {
            map.put("rvType", RvType.FOR_PO.getDescription());
        } else if (r.getRvType() == RvType.FOR_IT.getId()){
            map.put("rvType", RvType.FOR_IT.getDescription());
        } else if (r.getRvType() == RvType.FOR_REP.getId()){
            map.put("rvType", RvType.FOR_REP.getDescription());
        } else if (r.getRvType() == RvType.FOR_REP.getId()) {
            map.put("rvType", RvType.FOR_REP.getDescription());
        } else if (r.getRvType() == RvType.FOR_LAB.getId()) {
            map.put("rvType", RvType.FOR_LAB.getDescription());
        }

        map.put("rvTypeId", r.getRvType());
        map.put("voucherDate", r.getVoucherDate());
        map.put("preparedBy", r.getCreatedBy().getFullName());
        map.put("status", r.getDocumentStatus().getStatus());

        return map;
    }

    private boolean isRankAndFile(Integer workflowId) {
        return workflowId.equals(com.noreco1.fireflyv2.model.enums.Workflow.RV.getId());
    }

    private boolean isManagerial(Integer workflowId) {
        return workflowId.equals(com.noreco1.fireflyv2.model.enums.Workflow.RV_MANAGERIAL.getId());
    }

    private boolean isSupervisory(Integer workflowId) {
        return workflowId.equals(com.noreco1.fireflyv2.model.enums.Workflow.RV_SUPERVISORY.getId());
    }

    private boolean isPurchasingOfficer(Integer userId){

        Boolean isPurchasingOfficer = Boolean.FALSE;

        List<Object[]> userRoles = userRepo.findRolesByUserId(userId);

        if(Checker.collectionIsNotEmpty(userRoles)){

            for(Object[] o : userRoles){

                Integer id = (Integer) o[0];

                if(id.equals(UserRoleEnum.PURCHASING_OFFICER.getId())){
                    isPurchasingOfficer = Boolean.TRUE;
                    break;
                }

            }
        }

        return isPurchasingOfficer;

    }


    @Override
    public Page<Map<String, Object>> purchaseRequestListForStockWithdrawal(Integer locationId, String query, Pageable pageable) {
        User user = authenticationFacade.getLoggedIn();

        if (Checker.isStringNullAndEmpty(query)) {
            return purchaseRequestRepo.findPurchaseRequestsForStockWithdrawal(locationId, user.getId(), pageable);
        } else {
            return purchaseRequestRepo.findPurchaseRequestsForStockWithdrawal("%"+query+"%", locationId, user.getId(), pageable);
        }
    }
}
