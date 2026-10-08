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
import org.springframework.transaction.annotation.Isolation;
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

    private JobOrderAcceptance model;

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
    public JobOrderAcceptance findByCode(String code) {
        List<JobOrderAcceptance> pos = joAcceptanceRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(pos)) {
            return pos.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        JobOrderAcceptance jobOrderAcceptance = (JobOrderAcceptance) v;
        return this.processCreate(jobOrderAcceptance, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        JobOrderAcceptance jobOrderAcceptance = (JobOrderAcceptance) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        JoAcceptanceValidator validator = new JoAcceptanceValidator();
        validator.setService(this);
        validator.validate(jobOrderAcceptance, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            JobOrderAcceptance existingJoa = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(jobOrderAcceptance.getVoucherDate()));

            User inspectedBy = jobOrderAcceptance.getInspectedBy() != null ? userRepo.findOneByAccountNo(jobOrderAcceptance.getInspectedBy().getAccountNo()):null;

            Boolean insertMode = jobOrderAcceptance.getId() == null;
            if (insertMode) { // insert mode

                Object latestJoaCode = joAcceptanceRepo.findLatestJoAcceptanceCodeByYear(voucherYear);
                jobOrderAcceptance.setCode(generatorFacade.voucherCodeNoOffice("JOA", (latestJoaCode == null ? "" : String.valueOf(latestJoaCode)), jobOrderAcceptance.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                jobOrderAcceptance.setDocumentStatus(documentStatus);

                jobOrderAcceptance.setTransaction(generatorFacade.transaction());
                jobOrderAcceptance.setCreatedBy(createdBy);
                jobOrderAcceptance.setHasPayReq(false);
                existingJoa = jobOrderAcceptance;
            } else {
                existingJoa = joAcceptanceRepo.findById(jobOrderAcceptance.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingJoa);

            Workflow wf = new Workflow();

            if(inspectedBy != null){
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JOA_INSPECTION.getId());
            } else {
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.JOA.getId());
            }

            jobOrderAcceptance.setWorkflow(wf);
            existingJoa.setVendor(jobOrderAcceptance.getVendor());
            existingJoa.setJobOrder(jobOrderAcceptance.getJobOrder());
            existingJoa.setVoucherDate(jobOrderAcceptance.getVoucherDate());
            existingJoa.setYear(voucherYear);
            existingJoa.setInspectedBy(inspectedBy);
            existingJoa.setAmount(jobOrderAcceptance.getAmount());
            existingJoa.setType(jobOrderAcceptance.getType());
            existingJoa.setNetAmount(jobOrderAcceptance.getNetAmount());
            existingJoa.setAdjustment(jobOrderAcceptance.getAdjustment());
            existingJoa.setInvoiceNumber(jobOrderAcceptance.getInvoiceNumber());
            existingJoa.setInvoiceDate(jobOrderAcceptance.getInvoiceDate());

            boolean isFullPayment = false;

            ArrayList<JoAcceptanceDetailDto> joaDetails = jobOrderAcceptance.getJoAcceptanceDetails();
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
                    joAcceptanceDetailRepo.deleteByJobOrderAcceptanceId(existingJoa.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<JoAcceptanceDetailDto> joAcceptanceDetails = jobOrderAcceptance.getJoAcceptanceDetails();
                for(JoAcceptanceDetailDto joAcceptanceDetailLine: joAcceptanceDetails) {

                    JobOrderAcceptanceDetail jobOrderAcceptanceDetail = new JobOrderAcceptanceDetail();

                    JobOrderAcceptance joa1 = new JobOrderAcceptance();
                    joa1.setId(this.model.getId());
                    jobOrderAcceptanceDetail.setJobOrderAcceptance(joa1);

                    JobOrderDetail jobOrderDetail = new JobOrderDetail();
                    jobOrderDetail.setId(joAcceptanceDetailLine.getJoDetailId());
                    jobOrderDetail.setAcceptedAmount(joAcceptanceDetailLine.getAcceptedAmount());
                    jobOrderAcceptanceDetail.setJobOrderDetail(jobOrderDetail);

                    jobOrderAcceptanceDetail.setQuantity(joAcceptanceDetailLine.getQuantity());
                    jobOrderAcceptanceDetail.setUnitPrice(joAcceptanceDetailLine.getUnitPrice());
                    jobOrderAcceptanceDetail.setVat(joAcceptanceDetailLine.getVat());
                    jobOrderAcceptanceDetail.setDiscount(joAcceptanceDetailLine.getDiscount());
                    jobOrderAcceptanceDetail.setAmount(joAcceptanceDetailLine.getItemAmount());
                    jobOrderAcceptanceDetail.setAdjustment(joAcceptanceDetailLine.getAdjustment());
                    jobOrderAcceptanceDetail.setNetAmount(joAcceptanceDetailLine.getNetAmount());

                    JobOrderAcceptanceDetail newJoad = joAcceptanceDetailRepo.save(jobOrderAcceptanceDetail);
                    if(newJoad != null){
                        joDetailRepo.updateAcceptedAmountById(jobOrderDetail.getId(), jobOrderDetail.getAcceptedAmount());
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
        org.springframework.data.domain.Page<JobOrderAcceptance> joAcceptances;
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<CvVoucherDto> findAllApprovedForCvPaged(String query, Pageable pageable) {

        org.springframework.data.domain.Page<JobOrderAcceptance> joAcceptances;

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

                List<JobOrderDetail> details = joDetailRepo.findByJobOrderId(entity.getJobOrder().getId());
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
            JobOrderAcceptance doc = joAcceptanceRepo.findOneByTransactionId(documentLog.getTransaction().getId());

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

            List<JobOrderAcceptance> vouchers = joAcceptanceRepo.findAll();

            List<JoAcceptanceListDto> returnVouchers = new ArrayList<>();
            if (!Checker.collectionIsEmpty(vouchers)) {
                for(JobOrderAcceptance joa : vouchers) {
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JoAcceptanceListDto> findByPayReq() {

        List<JobOrderAcceptance> vouchers = joAcceptanceRepo.findByPayReqAndStatusId(false, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<JoAcceptanceListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(JobOrderAcceptance joa : vouchers) {
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

            List<JobOrderAcceptance> docs = joAcceptanceRepo.findByDocumentStatusIdAndVoucherDateBetweenAndCreatedById(id, fromDate, toDate, authenticationFacade.getLoggedIn().getId());
            return this.makeJOAListMap(docs);
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

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<JobOrderAcceptance> docs = joAcceptanceRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInAndCreatedById(fromDate, toDate, Arrays.asList(ids), authenticationFacade.getLoggedIn().getId());
            return this.makeJOAListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public JoAcceptanceDto findById(Integer id) {
        JobOrderAcceptance joa =  joAcceptanceRepo.findById(id).orElse(null);
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
                joDto.setVoucherDate(v.getVoucherDate());
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

        JobOrderAcceptance jobOrderAcceptance = joAcceptanceRepo.findById(id).orElse(null);

        if (jobOrderAcceptance != null) {

            Employee preparedBy = employeeRepo.findOneByAccountNumber(jobOrderAcceptance.getCreatedBy().getAccountNo());

            if(jobOrderAcceptance.getInspectedBy() != null){
                Employee inspectedBy = employeeRepo.findOneByAccountNumber(jobOrderAcceptance.getInspectedBy().getAccountNo());

                params.put("INSPECTEDBY", inspectedBy.getName());
                params.put("INSPECTEDBY_POS", inspectedBy.getPosition() == null ? "":inspectedBy.getPosition().getName());
                params.put("INSPECTEDBY_HEADER", "Inspected By: ");

            }
            params.put("VOUCHER_NO", jobOrderAcceptance.getCode());
            params.put("V_DATE", jobOrderAcceptance.getVoucherDate());
            params.put("JO_DESCRIPTION", jobOrderAcceptance.getJobOrder().getDescription());
            params.put("INVOICE_DATE", jobOrderAcceptance.getInvoiceDate());
            params.put("INVOICE_NUMBER", jobOrderAcceptance.getInvoiceNumber());
            params.put("PREPAREDBY", preparedBy.getName());
            params.put("PREPAREDBY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());
            params.put("SUPPLIER", jobOrderAcceptance.getVendor().getName());
            params.put("SUPPLIER_ADDRESS", jobOrderAcceptance.getVendor().getAddress());
            params.put("AMOUNT", jobOrderAcceptance.getNetAmount());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(jobOrderAcceptance.getNetAmount()) + "  (Php. " + new DecimalFormat("#,##0.00").format(jobOrderAcceptance.getNetAmount()) + ")");
            params.put("PAYMENT_TYPE", jobOrderAcceptance.getIsFullPayment() ? "full" : "partial");
            params.put("JO_NO", jobOrderAcceptance.getJobOrder().getCode());
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.JOA, jobOrderAcceptance);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<JOADetail> details = new ArrayList<>();

        JobOrderAcceptance voucher = joAcceptanceRepo.findById(id).orElse(null);
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
        JobOrderAcceptance jobOrderAcceptance =  joAcceptanceRepo.findById(postData.getDocumentId()).orElse(null);

        if (jobOrderAcceptance != null) {
            // for logging
            Map oldMap = this.forLogMapMain(jobOrderAcceptance);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(jobOrderAcceptance, jobOrderAcceptance.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            jobOrderAcceptance.setDocumentStatus(afterActionDocumentStatus);
            jobOrderAcceptance.setUpdatedAt(null);
            jobOrderAcceptance = joAcceptanceRepo.save(jobOrderAcceptance);

            // for logging
            Map newMap = this.forLogMapMain(jobOrderAcceptance);
            newMap.put("remarks", postData.getRemarks());

            if (jobOrderAcceptance != null) {
                documentProcessingFacade.processAction(jobOrderAcceptance.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(jobOrderAcceptance.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

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
        JobOrderAcceptance jo = joAcceptanceRepo.findFirstByOrderByIdAsc();
        if (jo != null) {
            return documentDtoer.getDocumentStatuses(jo.getWorkflow().getId());
        }
        return null;
    }

    private Map forLogMapMain(JobOrderAcceptance joa) {
        return documentLoggerFacade.makeLog(joa);
    }

    private List<Map> makeJOAListMap(List<JobOrderAcceptance> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(JobOrderAcceptance c:cs) {
                mapList.add(composeJOAMap(c));
            }
        }
        return mapList;
    }

    private Map composeJOAMap(JobOrderAcceptance r) {
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
