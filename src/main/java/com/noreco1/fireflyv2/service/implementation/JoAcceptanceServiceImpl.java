package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.JOADetail;
import com.noreco1.fireflyv2.service.JoAcceptanceService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.JoAcceptanceValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Personal on 7/7/2015.
 */
@Service(value = "joaServiceImpl")
public class JoAcceptanceServiceImpl implements JoAcceptanceService, PrintableVoucher {

    private JoAcceptance model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    JoAcceptanceDetailRepo joAcceptanceDetailRepo;

    @Autowired
    JoDetailRepo joDetailRepo;

    @Autowired
    JobOrderRepo joRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    JoAcceptanceDetailServiceImpl joAcceptanceDetailDto;

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
    EmployeeRepo employeeRepo;

    @Autowired
    FileFacade fileFacade;

    @Override
    @Transactional(readOnly = true)
    public JoAcceptance findByCode(String code) {
        List<JoAcceptance> pos = joAcceptanceRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(pos)) {
            return pos.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        JoAcceptance joAcceptance = (JoAcceptance) v;
        return this.processCreate(joAcceptance, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        JoAcceptance joAcceptance = (JoAcceptance) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        JoAcceptanceValidator validator = new JoAcceptanceValidator();
        validator.setService(this);
        validator.validate(joAcceptance, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            JoAcceptance existingJoa = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(joAcceptance.getVoucherDate()));

            User inspectedBy = joAcceptance.getInspectedBy() != null ? userRepo.findOneByAccountNo(joAcceptance.getInspectedBy().getAccountNo()):null;

            Boolean insertMode = joAcceptance.getId() == null;
            if (insertMode) { // insert mode

                Object latestJoaCode = joAcceptanceRepo.findLatestJoAcceptanceCodeByYear(voucherYear);
                joAcceptance.setCode(generatorFacade.voucherCodeNoOffice("JOA", (latestJoaCode == null ? "" : String.valueOf(latestJoaCode)), joAcceptance.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                joAcceptance.setDocumentStatus(documentStatus);

                joAcceptance.setTransaction(generatorFacade.transaction());
                joAcceptance.setCreatedBy(createdBy);
                joAcceptance.setHasPayReq(false);
                existingJoa = joAcceptance;
            } else {
                existingJoa = joAcceptanceRepo.findById(joAcceptance.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingJoa);

            Workflow wf = new Workflow();

            if(inspectedBy != null){
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JOA_INSPECTION.getId());
            } else {
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JOA.getId());
            }

            joAcceptance.setWorkflow(wf);
            existingJoa.setVendor(joAcceptance.getVendor());
            existingJoa.setJobOrder(joAcceptance.getJobOrder());
            existingJoa.setVoucherDate(joAcceptance.getVoucherDate());
            existingJoa.setYear(voucherYear);
            existingJoa.setInspectedBy(inspectedBy);
            existingJoa.setAmount(joAcceptance.getAmount());
            existingJoa.setType(joAcceptance.getType());
            existingJoa.setNetAmount(joAcceptance.getNetAmount());
            existingJoa.setAdjustment(joAcceptance.getAdjustment());
            existingJoa.setInvoiceNumber(joAcceptance.getInvoiceNumber());
            existingJoa.setInvoiceDate(joAcceptance.getInvoiceDate());

            boolean isFullPayment = false;

            ArrayList<JoAcceptanceDetailDto> joaDetails = joAcceptance.getJoAcceptanceDetails();
            for(JoAcceptanceDetailDto joAcceptanceDetailLine: joaDetails) {
                boolean itemFullyAccepted = joAcceptanceDetailLine.getRemainingAmount().equals(joAcceptanceDetailLine.getItemAmount());

                isFullPayment = itemFullyAccepted;
                if(!itemFullyAccepted) break;

            }

            existingJoa.setIsFullPayment(isFullPayment);

            this.model = joAcceptanceRepo.save(existingJoa);

            if (this.model != null) {

                // start: update default signatories
//                signatoryFacade.joAcceptance(this.model);
                // end: update default signatories

                if (!insertMode) {
                    joAcceptanceDetailRepo.deleteByJoAcceptanceId(existingJoa.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<JoAcceptanceDetailDto> joAcceptanceDetails = joAcceptance.getJoAcceptanceDetails();
                for(JoAcceptanceDetailDto joAcceptanceDetailLine: joAcceptanceDetails) {

                    JoAcceptanceDetail joAcceptanceDetail = new JoAcceptanceDetail();

                    JoAcceptance joa1 = new JoAcceptance();
                    joa1.setId(this.model.getId());
                    joAcceptanceDetail.setJoAcceptance(joa1);

                    JoDetail joDetail = new JoDetail();
                    joDetail.setId(joAcceptanceDetailLine.getJoDetailId());
                    joDetail.setAcceptedAmount(joAcceptanceDetailLine.getAcceptedAmount());
                    joAcceptanceDetail.setJoDetail(joDetail);

                    joAcceptanceDetail.setQuantity(joAcceptanceDetailLine.getQuantity());
                    joAcceptanceDetail.setUnitPrice(joAcceptanceDetailLine.getUnitPrice());
                    joAcceptanceDetail.setVat(joAcceptanceDetailLine.getVat());
                    joAcceptanceDetail.setDiscount(joAcceptanceDetailLine.getDiscount());
                    joAcceptanceDetail.setAmount(joAcceptanceDetailLine.getItemAmount());
                    joAcceptanceDetail.setAdjustment(joAcceptanceDetailLine.getAdjustment());
                    joAcceptanceDetail.setNetAmount(joAcceptanceDetailLine.getNetAmount());

                    JoAcceptanceDetail newJoad = joAcceptanceDetailRepo.save(joAcceptanceDetail);
                    if(newJoad != null){
                        joDetailRepo.updateAcceptedAmountById(joDetail.getId(), joDetail.getAcceptedAmount());
                    }
                }
                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Job Order Certification successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
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
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
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
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ApvPurchasingDocumentDto> findAllApprovedForApvPaged(String query, Pageable pageable) {
        org.springframework.data.domain.Page<JoAcceptance> joAcceptances;
        if(query != null){
            joAcceptances = joAcceptanceRepo.findAllByQueryForApv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), DocumentType.JOA.getId(), pageable);
        } else {
            joAcceptances = joAcceptanceRepo.findAllForApv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), DocumentType.JOA.getId(), pageable);
        }

        return joAcceptances.map(entity -> {
                ApvPurchasingDocumentDto dto = new ApvPurchasingDocumentDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setNetAmount(entity.getAmount());
                dto.setParticulars(entity.getCode() + " - " + entity.getVendor().getName());
                dto.setSlentityAccountNo(entity.getVendor().getAccountNo());
                dto.setSlentityName(entity.getVendor().getName());
                dto.setInvoiceDate(entity.getInvoiceDate());
                dto.setPaymentTerm(entity.getJobOrder().getPaymentTerm());

                return dto;
        });
    }

    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<JoAcceptance> joAcceptances;

        if(query != null){
            joAcceptances = joAcceptanceRepo.findAllByQueryAndDocumentStatusForCv("%"+query+"%", com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        } else {
            joAcceptances = joAcceptanceRepo.findAllByDocumentStatusForCv(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(), pageable);
        }

        return joAcceptances.map(entity -> {
                CvVoucherDto dto = new CvVoucherDto();

                dto.setVoucherDate(entity.getVoucherDate());
                dto.setLocalCode(entity.getCode());
                dto.setId(entity.getId());
                dto.setPreparedBy(entity.getCreatedBy().getFullName());
                dto.setAmount(entity.getAmount());
                dto.setParticulars(entity.getCode() + " - " + entity.getInvoiceNumber());
                dto.setTransId(entity.getTransaction().getId());
                dto.setSlentityAccountNo(entity.getVendor().getAccountNo());
                dto.setSlentityName(entity.getVendor().getName());
                dto.setExtensionUrl("jo-acceptance");
                dto.setBudgetLineItemDetail(null);

                List<JoDetail> details = joDetailRepo.findByJobOrderId(entity.getJobOrder().getId());
                if(!details.isEmpty()){

                    if(details.get(0).getPurchaseRequestDetail() != null){

                        if(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail() != null){

                            dto.setBudgetLineItemDetail(details.get(0).getPurchaseRequestDetail().getPurchaseRequest().getBudgetLineItemDetail());

                        }

                    }

                }

                return dto;
        });

    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            JoAcceptance doc = joAcceptanceRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.JOA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoAcceptanceListDto> findAll() {
        try {

            List<JoAcceptance> vouchers = joAcceptanceRepo.findAll();

            List<JoAcceptanceListDto> returnVouchers = new ArrayList<>();
            if (!Checker.collectionIsEmpty(vouchers)) {
                for(JoAcceptance joa : vouchers) {
                    JoAcceptanceListDto joAcceptanceListDto = new JoAcceptanceListDto();
                    joAcceptanceListDto.setId(joa.getId());
                    joAcceptanceListDto.setVoucherDate(joa.getVoucherDate());
                    joAcceptanceListDto.setLocalCode(joa.getCode());
                    joAcceptanceListDto.setSupplier(joa.getVendor().getName());
                    joAcceptanceListDto.setAmount(joa.getNetAmount());
                    joAcceptanceListDto.setStatus(joa.getDocumentStatus().getStatus());

                    SlEntity createdBy = slEntityRepo.findById(joa.getCreatedBy().getAccountNo()).orElse(null);
                    joAcceptanceListDto.setPreparedBy(createdBy.getName());

                    returnVouchers.add(joAcceptanceListDto);
                }

                return returnVouchers;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<JoAcceptanceListDto> findByPayReq() {

        List<JoAcceptance> vouchers = joAcceptanceRepo.findByPayReqAndStatusId(false, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<JoAcceptanceListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(JoAcceptance joa : vouchers) {
                JoAcceptanceListDto joAcceptanceListDto = new JoAcceptanceListDto();
                joAcceptanceListDto.setId(joa.getId());
                joAcceptanceListDto.setVoucherDate(joa.getVoucherDate());
                joAcceptanceListDto.setLocalCode(joa.getCode());
                joAcceptanceListDto.setSupplier(joa.getVendor().getName());
                joAcceptanceListDto.setAmount(joa.getNetAmount());
                joAcceptanceListDto.setStatus(joa.getDocumentStatus().getStatus());
                joAcceptanceListDto.setVendor(joa.getVendor());

                SlEntity createdBy = slEntityRepo.findById(joa.getCreatedBy().getAccountNo()).orElse(null);
                joAcceptanceListDto.setPreparedBy(createdBy.getName());

                returnVouchers.add(joAcceptanceListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public Map findByApvId(Integer apvId) {
        Map m = null;

        List<Object[]> rows = joAcceptanceRepo.findByApvId(apvId, DocumentType.JOA.getId());

        if (!Checker.collectionIsEmpty(rows)) {
            Object[] row = rows.get(0);

            m = new HashMap();
            m.put("id", row[0]);
            m.put("localCode", row[1]);
            m.put("netAmount", row[2]);
            m.put("particulars", row[3]);
            m.put("voucherDate", row[4]);
            m.put("preparedBy", row[5]);
        }

        return m;
    }

    @Override
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

            List<JoAcceptance> docs = joAcceptanceRepo.findByDocumentStatusIdAndVoucherDateBetweenAndCreatedById(id, fromDate, toDate, authenticationFacade.getLoggedIn().getId());
            return this.makeJOAListMap(docs);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

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

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<JoAcceptance> docs = joAcceptanceRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInAndCreatedById(fromDate, toDate, Arrays.asList(ids), authenticationFacade.getLoggedIn().getId());
            return this.makeJOAListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public JoAcceptanceDto findById(Integer id) {
        JoAcceptance joa =  joAcceptanceRepo.findById(id).orElse(null);
        JoAcceptanceDto joaDto = new JoAcceptanceDto();

        List<Object[]> list = joAcceptanceRepo.findJobOrderById(id);
        JoDto joDto = null;
        if (!Checker.collectionIsEmpty(list)) {
            Integer joId = (Integer)(list.get(0)[0]); // take one only for now
            JobOrder v = joRepo.findById(joId).orElse(null);
            if (v != null) {
                joDto = new JoDto();
                joDto.setId(v.getId());
                joDto.setAmount(v.getAmount());
                joDto.setVendor(v.getVendor());
                joDto.setLocalCode(v.getCode());
                joDto.setTransId(v.getTransaction().getId());
            }
        }

        if (joa != null) {
            joaDto.setId(joa.getId());
            joaDto.setLocalCode(joa.getCode());
            joaDto.setTransId(joa.getTransaction().getId());
            joaDto.setAmount(joa.getAmount());

            SlEntity createdBy = slEntityRepo.findById(joa.getCreatedBy().getAccountNo()).orElse(null);

            if(joa.getInspectedBy() != null){
                SlEntity inspectedBy = slEntityRepo.findById(joa.getInspectedBy().getAccountNo()).orElse(null);
                joaDto.setInspectedBy(inspectedBy);
            }

            if (joDto != null) {
                joaDto.setJobOrder(joDto);
            }

            joaDto.setCreatedBy(createdBy);
            joaDto.setVendor(joa.getVendor());
            joaDto.setVoucherDate(joa.getVoucherDate());
            joaDto.setDocumentStatus(joa.getDocumentStatus());
            joaDto.setCreated(joa.getCreatedAt());
            joaDto.setLastUpdated(joa.getUpdatedAt());
            joaDto.setType(joa.getType());
            joaDto.setInvoiceDate(joa.getInvoiceDate());
            joaDto.setInvoiceNumber(joa.getInvoiceNumber());
        }

        return  joaDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        JoAcceptance joAcceptance = joAcceptanceRepo.findById(id).orElse(null);

        if (joAcceptance != null) {

            Employee preparedBy = employeeRepo.findOneByAccountNumber(joAcceptance.getCreatedBy().getAccountNo());

            if(joAcceptance.getInspectedBy() != null){
                Employee inspectedBy = employeeRepo.findOneByAccountNumber(joAcceptance.getInspectedBy().getAccountNo());

                params.put("INSPECTEDBY", inspectedBy.getName());
                params.put("INSPECTEDBY_POS", inspectedBy.getPosition() == null ? "":inspectedBy.getPosition().getName());
                params.put("INSPECTEDBY_HEADER", "Inspected By: ");

            }
            params.put("VOUCHER_NO", joAcceptance.getCode());
            params.put("V_DATE", joAcceptance.getVoucherDate());
            params.put("JO_DESCRIPTION", joAcceptance.getJobOrder().getDescription());
            params.put("INVOICE_DATE", joAcceptance.getInvoiceDate());
            params.put("INVOICE_NUMBER", joAcceptance.getInvoiceNumber());
            params.put("PREPAREDBY", preparedBy.getName());
            params.put("PREPAREDBY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());
            params.put("SUPPLIER", joAcceptance.getVendor().getName());
            params.put("SUPPLIER_ADDRESS", joAcceptance.getVendor().getAddress());
            params.put("AMOUNT", joAcceptance.getNetAmount());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(joAcceptance.getNetAmount()) + "  (Php. " + new DecimalFormat("#,##0.00").format(joAcceptance.getNetAmount()) + ")");
            params.put("PAYMENT_TYPE", joAcceptance.getIsFullPayment() ? "full" : "partial");
            params.put("JO_NO", joAcceptance.getJobOrder().getCode());
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.JOA, joAcceptance);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<JOADetail> details = new ArrayList<>();

        JoAcceptance voucher = joAcceptanceRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<JoAcceptanceDetailDto> joAcceptanceDetailDtos = joAcceptanceDetailDto.getJoaDetails(id);

            if (!Checker.collectionIsEmpty(joAcceptanceDetailDtos)) {
                for(JoAcceptanceDetailDto dto : joAcceptanceDetailDtos) {
                    JOADetail d = new JOADetail();

                    d.setRank(joAcceptanceDetailDtos.indexOf(dto) + 1);
                    d.setDescription(dto.getItemDescription() == null ? dto.getJoDescription() : dto.getItemDescription());
                    d.setAmount(dto.getItemAmount());
                    d.setAdjustment(dto.getAdjustment());
                    d.setNetAmount(dto.getNetAmount());
                    d.setQuantity(dto.getQuantity());
                    d.setUnitPrice(dto.getUnitPrice());
                    d.setUnitCode(dto.getUnitCode());

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
        JoAcceptance joAcceptance =  joAcceptanceRepo.findById(postData.getDocumentId()).orElse(null);

        if (joAcceptance != null) {
            // for logging
            Map oldMap = this.forLogMapMain(joAcceptance);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(joAcceptance, joAcceptance.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            joAcceptance.setDocumentStatus(afterActionDocumentStatus);
            joAcceptance.setUpdatedAt(null);
            joAcceptance = joAcceptanceRepo.save(joAcceptance);

            // for logging
            Map newMap = this.forLogMapMain(joAcceptance);
            newMap.put("remarks", postData.getRemarks());

            if (joAcceptance != null) {
                documentProcessingFacade.processAction(joAcceptance.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(joAcceptance.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        return null;
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
        JoAcceptance jo = joAcceptanceRepo.findFirstByOrderByIdAsc();
        if (jo != null) {
            return documentDtoer.getDocumentStatuses(jo.getWorkflow().getId());
        }
        return null;
    }

    private Map forLogMapMain(JoAcceptance joa) {
        return documentLoggerFacade.makeLog(joa);
    }

    private List<Map> makeJOAListMap(List<JoAcceptance> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(JoAcceptance c:cs) {
                mapList.add(composeJOAMap(c));
            }
        }
        return mapList;
    }

    private Map composeJOAMap(JoAcceptance r) {
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
}
