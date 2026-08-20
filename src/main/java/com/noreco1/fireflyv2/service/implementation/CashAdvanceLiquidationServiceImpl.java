package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ClassHelper;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.service.CashAdvanceLiquidationService;
import com.noreco1.fireflyv2.validator.CashAdvanceLiquidationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CashAdvanceLiquidationServiceImpl implements CashAdvanceLiquidationService {

    private CashAdvanceLiquidation model;

    @Autowired
    CashAdvanceLiquidationRepo cashAdvanceLiquidationRepo;

    @Autowired
    CashAdvanceLiquidationItemRepo cashAdvanceLiquidationItemRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    UserRepo userRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CashAdvanceLiquidation findById(Integer id) {
        CashAdvanceLiquidation cashAdvanceLiquidation = cashAdvanceLiquidationRepo.findById(id).orElse(null);

        if(cashAdvanceLiquidation != null){

            List<CashAdvanceLiquidationItem> items = cashAdvanceLiquidationItemRepo.findByCashAdvanceLiquidationId(cashAdvanceLiquidation.getId());
            if(!items.isEmpty()){
                cashAdvanceLiquidation.setCashAdvanceLiquidationItems(items);
            }

        }

        return cashAdvanceLiquidation;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findAll() {

        User loggedIn = authenticationFacade.getLoggedIn();
        List<CashAdvanceLiquidation> cashAdvanceLiquidations = cashAdvanceLiquidationRepo.findAllByAllowedUsers(loggedIn.getId());
        return  this.makeCashAdvanceLiquidationList(cashAdvanceLiquidations);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findByStatusId(Integer id) {
        try {
            List<CashAdvanceLiquidation> list = cashAdvanceLiquidationRepo.findByDocumentStatusId(id);

            return this.makeCashAdvanceLiquidationList(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findByDateRangeAndStatusId(String from, String to, Integer id, Integer officeId) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<CashAdvanceLiquidation> list = cashAdvanceLiquidationRepo.findByDateRangeAndStatusIdAndOfficeId(
                    authenticationFacade.getLoggedIn().getId(),
                    id,
                    fromDate,
                    toDate, officeId);

            return this.makeCashAdvanceLiquidationList(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<HashMap> findByDateRange(String from, String to, Integer officeId) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<CashAdvanceLiquidation> vouchers = cashAdvanceLiquidationRepo.findByDateRangeAndNotApprovedOrDeniedAndOfficeId(
                    authenticationFacade.getLoggedIn().getId(),
                    Arrays.asList(ids),
                    fromDate,
                    toDate, officeId);

            return this.makeCashAdvanceLiquidationList(vouchers);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CashAdvanceLiquidation> findAllForJv(String query, Pageable pageable) {
        if (Checker.isStringNullAndEmpty(query)) {
            return cashAdvanceLiquidationRepo.findAllForJV(pageable);
        } else {
            return cashAdvanceLiquidationRepo.findAllForJVByQuery("%"+query+"%", pageable);
        }
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        User processedBy = authenticationFacade.getLoggedIn();
        CashAdvanceLiquidation cashAdvanceLiquidation = cashAdvanceLiquidationRepo.findById(postData.getDocumentId()).orElse(null);

        if (cashAdvanceLiquidation != null && cashAdvanceLiquidation.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldCAMap = this.forLogMapMain(cashAdvanceLiquidation);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(cashAdvanceLiquidation, cashAdvanceLiquidation.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cashAdvanceLiquidation.setDocumentStatus(afterActionDocumentStatus);
            cashAdvanceLiquidation.setUpdatedAt(null);

            cashAdvanceLiquidation = cashAdvanceLiquidationRepo.save(cashAdvanceLiquidation);

            // for logging
            Map newCAMap = this.forLogMapMain(cashAdvanceLiquidation);
            newCAMap.put("remarks", postData.getRemarks());

            if (cashAdvanceLiquidation != null) {

                documentProcessingFacade.processAction(cashAdvanceLiquidation.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(cashAdvanceLiquidation.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, newCAMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashAdvanceLiquidation ca = (CashAdvanceLiquidation) v;

        return this.processCreate(ca, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CashAdvanceLiquidation cal = (CashAdvanceLiquidation) v;
        PostResponse response = new PostResponse();
        try {

            MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
            CashAdvanceLiquidationValidator validator = new CashAdvanceLiquidationValidator();
            validator.validate(cal, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();

                response = messageFormatter.getResponse();
            } else {
                CashAdvanceLiquidation existingCal = null;
                User createdBy = authenticationFacade.getLoggedIn();
                User approvingOfficer = userRepo.findOneByAccountNo(cal.getApprovingOfficer().getAccountNo());
                User recommendedBy = userRepo.findOneByAccountNo(cal.getRecommendedBy().getAccountNo());
                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(cal.getVoucherDate()));
                boolean insertMode = !Checker.isValidId(cal.getId());
                DocumentStatus ds = new DocumentStatus();
                Workflow wf = new Workflow();

                int existingId = 0;

                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CAL.getId());

                // Insert mode.
                if (insertMode) {
                    String offAcro = cal.getOffice().getAcronym();
                    Object latestPcvCode = cashAdvanceLiquidationRepo.findLatestCaCodeByYear(voucherYear, "%-"+offAcro+"-%");
                    cal.setCode(generatorFacade.voucherCode("CAL-"+offAcro, (latestPcvCode == null ? "" : String.valueOf(latestPcvCode)), cal.getVoucherDate()));
                    cal.setDocumentStatus(ds);
                    cal.setTransaction(generatorFacade.transaction());
                    cal.setCreatedBy(createdBy);
                    cal.setWorkflow(wf);

                    existingCal = cal;
                } else {
                    existingId = cal.getId();
                    List<Integer> statusAllowed = new ArrayList<>();
                    existingCal = cashAdvanceLiquidationRepo.findById(cal.getId()).orElse(null);

                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    if (statusAllowed.indexOf(existingCal.getDocumentStatus().getId()) < 0) {
                        ArrayList<String> messages = new ArrayList<>();

                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }
                }

                // use for document logging
                Map oldCAMap = this.forLogMapMain(existingCal);

                // Editable fields.
                existingCal.setVoucherDate(cal.getVoucherDate());
                existingCal.setCashAdvance(cal.getCashAdvance());
                existingCal.setAmount(cal.getAmount());
                existingCal.setCreatedBy(createdBy);
                existingCal.setApprovingOfficer(approvingOfficer);
                existingCal.setRecommendedBy(recommendedBy);
                existingCal.setYear(voucherYear);
                existingCal.setOffice(cal.getOffice());
                existingCal.setYear(voucherYear);
                existingCal.setWorkflow(wf);
                existingCal.setRemarks(cal.getRemarks());
                existingCal.setForClearing(cal.isForClearing());

                BigDecimal totalExistingReturnedAmount = BigDecimal.ZERO;

                if(cal.isForClearing()){

                    List<CashAdvanceLiquidationItem> existingItems = cashAdvanceLiquidationItemRepo.findAllByCaId(cal.getCashAdvance().getId(), existingId);
                    if(!existingItems.isEmpty()){

                        for(CashAdvanceLiquidationItem item : existingItems){
                            totalExistingReturnedAmount = totalExistingReturnedAmount.add(item.getAmount());
                        }

                    }

                    existingCal.setTotalReturnedAmount(totalExistingReturnedAmount.add(cal.getAmount()));

                }

                this.model = cashAdvanceLiquidationRepo.save(existingCal);

                if (this.model != null) {

                    // start: update default signatories
                    signatoryFacade.cal(this.model);
                    // end: update default signatories

                    List<CashAdvanceLiquidationItem> cashAdvanceLiquidationItems = cal.getCashAdvanceLiquidationItems();

                    // Log action only when adding document.
                    if (insertMode) {
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldCAMap = null;
                    } else {
                        cashAdvanceLiquidationItemRepo.deleteByCashAdvanceLiquidationId(this.model.getId());
                    }

                    for (CashAdvanceLiquidationItem cashAdvanceLiquidationItem : cashAdvanceLiquidationItems) {
                        cashAdvanceLiquidationItem.setCashAdvanceLiquidation(this.model);
                        cashAdvanceLiquidationItemRepo.save(cashAdvanceLiquidationItem);
                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldCAMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());

                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("CAL successfully saved!");
                    response.setSuccess(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            CashAdvanceLiquidation cal = cashAdvanceLiquidationRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (cal != null) {
                Map map = forLogMapMain(cal);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.CAL);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(filesToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        CashAdvanceLiquidation cal = cashAdvanceLiquidationRepo.findFirstByOrderByIdAsc();

        if (cal != null) {
            return documentDtoer.getDocumentStatuses(cal.getWorkflow().getId());
        }

        return null;
    }

    private List<HashMap> makeCashAdvanceLiquidationList(List<CashAdvanceLiquidation> cashAdvanceLiquidations) {
        List<HashMap> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cashAdvanceLiquidations)) {
            for (CashAdvanceLiquidation cashAdvanceLiquidation : cashAdvanceLiquidations) {

                HashMap map = new HashMap();

                map.put("id", cashAdvanceLiquidation.getId());
                map.put("code", cashAdvanceLiquidation.getCode());
                map.put("date", cashAdvanceLiquidation.getVoucherDate());
                map.put("documentStatus", cashAdvanceLiquidation.getDocumentStatus());
                map.put("preparedBy", cashAdvanceLiquidation.getCreatedBy());
                map.put("amount", cashAdvanceLiquidation.getAmount());
                map.put("employee", cashAdvanceLiquidation.getCashAdvance().getEmployee().getName());
                mapList.add(map);
            }
        }

        return mapList;
    }

    private Map forLogMapMain(CashAdvanceLiquidation cal) {
        return documentLoggerFacade.makeLog(cal);
    }
}
