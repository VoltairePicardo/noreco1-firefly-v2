package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CNVSDetail;
import com.noreco1.fireflyv2.service.CanvassService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CanvassValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service(value = "cnvsServiceImpl")
public class CanvassServiceImpl implements CanvassService, PrintableVoucher {

    private Canvass model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    CanvassRepo canvassRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    CanvassDetailServiceImpl canvassDetailDto;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    Environment env;

    @Override
    @Transactional(readOnly = true)
    public Canvass findByCode(String code) {
        List<Canvass> canvasses = canvassRepo.findByCode(code);

        if (!Checker.collectionIsEmpty(canvasses)) {
            return canvasses.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        Canvass canvass = (Canvass) v;
        return this.processCreate(canvass, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource) {
        Canvass canvass = (Canvass) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CanvassValidator validator = new CanvassValidator();
        validator.setService(this);
        validator.validate(canvass, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            Canvass existingCanvass;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(canvass.getVoucherDate()));

            Boolean insertMode = canvass.getId() == null || canvass.getId() == 0;
            if (insertMode) { // insert mode

                Employee employee = this.employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                String departmentAbbreviation = employee != null ? employee.getDepartment().getAbbreviation() : "";

                Object latestCanvassCode = canvassRepo.findLatestCanvassCodeByYear(voucherYear, "%-"+departmentAbbreviation+"-%");
                canvass.setCode(generatorFacade.voucherCode("CF-"+departmentAbbreviation, (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), canvass.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                canvass.setDocumentStatus(documentStatus);

                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CANVASS.getId());

                canvass.setTransaction(generatorFacade.transaction());
                canvass.setWorkflow(wf);
                canvass.setCreatedBy(createdBy);
                canvass.setCreatedAt(new Date());
                canvass.setUpdatedAt(new Date());
                existingCanvass = canvass;
            } else {
                existingCanvass = canvassRepo.findById(canvass.getId()).orElse(null);
            }
            // use for document logging
            Map oldMap = this.forLogMapMain(existingCanvass);

            existingCanvass.setVoucherDate(canvass.getVoucherDate());
            existingCanvass.setYear(voucherYear);
            existingCanvass.setSupplier(canvass.getSupplier());
            existingCanvass.setUpdatedAt(new Date());

            this.model = canvassRepo.save(existingCanvass);

            if (this.model != null) {
                // start: update default signatories
                // signatoryFacade.canvass(this.model);
                // end: update default signatories

                if (!insertMode) {
                    canvassDetailRepo.deleteByCanvassId(this.model.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldMap = null;
                }

                ArrayList<CanvassDetailDto> canvassDetails = canvass.getCanvassDetails();
                for(CanvassDetailDto canvassDetailLine: canvassDetails) {

                    // save only with unit price if has supplier
                    // save all if no supplier
                    boolean hasPrice1 = canvassDetailLine.getPriceSupplier1() != null && canvassDetailLine.getPriceSupplier1().compareTo(BigDecimal.ZERO) > 0;
                    boolean hasPrice2 = canvassDetailLine.getPriceSupplier2() != null && canvassDetailLine.getPriceSupplier2().compareTo(BigDecimal.ZERO) > 0;
                    boolean hasPrice3 = canvassDetailLine.getPriceSupplier3() != null && canvassDetailLine.getPriceSupplier3().compareTo(BigDecimal.ZERO) > 0;

                    boolean hasPrice = hasPrice1 || hasPrice2 || hasPrice3;

                    boolean save = canvass.getSuppliers().isEmpty() || hasPrice;
                    if(save) {

                        PurchaseRequestDetail purchaseRequestDetail = new PurchaseRequestDetail();
                        purchaseRequestDetail.setId(canvassDetailLine.getRvDetailId());

                        if(hasPrice) {

                            // save multiple lines, with supplier
                            // first supplier: has supplier and price is set
                            if(canvass.getSuppliers().size() > 0 && canvass.getSuppliers().get(0) != null && hasPrice1) {

                                CanvassDetail canvassDetail1 = new CanvassDetail();
                                canvassDetail1.setCanvass(this.model);
                                canvassDetail1.setPurchaseRequestDetail(purchaseRequestDetail);
                                canvassDetail1.setUnitPrice(canvassDetailLine.getPriceSupplier1());
                                canvassDetail1.setSupplier(canvass.getSuppliers().get(0));

                                canvassDetailRepo.save(canvassDetail1);
                            }
                            // second
                            if(canvass.getSuppliers().size() > 1 && canvass.getSuppliers().get(1) != null && hasPrice2) {

                                CanvassDetail canvassDetail2 = new CanvassDetail();
                                canvassDetail2.setCanvass(this.model);
                                canvassDetail2.setPurchaseRequestDetail(purchaseRequestDetail);
                                canvassDetail2.setUnitPrice(canvassDetailLine.getPriceSupplier2());
                                canvassDetail2.setSupplier(canvass.getSuppliers().get(1));

                                canvassDetailRepo.save(canvassDetail2);
                            }
                            // last
                            if(canvass.getSuppliers().size() > 2 && canvass.getSuppliers().get(2) != null && hasPrice3) {

                                CanvassDetail canvassDetail3 = new CanvassDetail();
                                canvassDetail3.setCanvass(this.model);
                                canvassDetail3.setPurchaseRequestDetail(purchaseRequestDetail);
                                canvassDetail3.setUnitPrice(canvassDetailLine.getPriceSupplier3());
                                canvassDetail3.setSupplier(canvass.getSuppliers().get(2));

                                canvassDetailRepo.save(canvassDetail3);
                            }

                        } else {
                            // creating canvass form only. save 1
                            CanvassDetail canvassDetail = new CanvassDetail();
                            canvassDetail.setCanvass(this.model);
                            canvassDetail.setPurchaseRequestDetail(purchaseRequestDetail);
                            canvassDetail.setUnitPrice(canvassDetailLine.getUnitPrice());

                            canvassDetailRepo.save(canvassDetail);

                        }
                    }
                }
                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Canvass successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public Map quotationDefaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.QUOTATION_SUMMARY);
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            Canvass doc = canvassRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CF);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CanvassListDto> findAll() {
        List<Canvass> vouchers = canvassRepo.findAll();

        List<CanvassListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(Canvass canvass : vouchers) {
                CanvassListDto canvassListDto = new CanvassListDto();
                canvassListDto.setId(canvass.getId());
                canvassListDto.setVoucherDate(canvass.getVoucherDate());
                canvassListDto.setLocalCode(canvass.getCode());

                SlEntity createdBy = slEntityRepo.findById(canvass.getCreatedBy().getAccountNo()).orElse(null);
                canvassListDto.setPreparedBy(createdBy != null ? createdBy.getName() : "");

                returnVouchers.add(canvassListDto);
            }

            return returnVouchers;
        }
        return null;
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

            List<Canvass> docs = canvassRepo.findByDocumentStatusIdAndVoucherDateAndOfficeId(id, fromDate, toDate);
             return this.makeCanvassListMap(docs);
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
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId()
            };

            List<Canvass> docs = canvassRepo.findByVoucherDateBetweenAndPendingAndOfficeId(fromDate, toDate, Arrays.asList(ids));
            return this.makeCanvassListMap(docs);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public CanvassDto findById(Integer id) {
        Canvass canvass =  canvassRepo.findById(id).orElse(null);
        CanvassDto canvassDto = new CanvassDto();

        if (canvass != null) {
            canvassDto.setId(canvass.getId());
            canvassDto.setLocalCode(canvass.getCode());
            canvassDto.setTransId(canvass.getTransaction().getId());

            SlEntity createdBy = slEntityRepo.findById(canvass.getCreatedBy().getAccountNo()).orElse(null);

            canvassDto.setCreatedBy(createdBy);
            canvassDto.setVoucherDate(canvass.getVoucherDate());
            canvassDto.setDocumentStatus(canvass.getDocumentStatus());
            canvassDto.setCreated(canvass.getCreatedAt());
            canvassDto.setLastUpdated(canvass.getUpdatedAt());
            canvassDto.setSupplier(canvass.getSupplier());
            canvassDto.setOffice(canvass.getOffice());

            List<CanvassDetail> detailList = canvassDetailRepo.findByCanvassId(id);
            if(!detailList.isEmpty()) {

                Map ids = new HashMap();
                for (CanvassDetail detail:detailList) {
                    if(detail.getSupplier() != null) {

                        // prevent dupes
                        Object o = ids.get(detail.getSupplier().getId());
                        if(o == null) {

                            Supplier s = new Supplier();
                            s.setId(detail.getSupplier().getId());
                            s.setName(detail.getSupplier().getName());

                            canvassDto.getSuppliers().add(s);
                            ids.put(detail.getSupplier().getId(), detail.getSupplier());
                        }
                    }
                }
            }
        }

        return  canvassDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        Canvass canvass = canvassRepo.findById(id).orElse(null);

        if (canvass != null) {

            Employee employee = employeeRepo.findOneByAccountNumber(canvass.getCreatedBy().getAccountNo());

            params.put("CANVASSED_CODE", canvass.getCode());
            params.put("CANVASSED_BY", employee.getName());
            params.put("CANVASSED_BY_POS", employee.getPosition() != null ? employee.getPosition().getName():"");
            params.put("CANVASSED_BY_SIGN",employee.getSignature() != null ? env.getProperty("path.attachments") + employee.getSignature().getFilename() : "");
            params.put("CREATED_DATE", canvass.getCreatedAt());

            CanvassDetail canvassDetail = this.canvassDetailRepo.findFirstByCanvassId(canvass.getId());

            params.put("REQUEST_CODE", canvassDetail.getPurchaseRequestDetail().getPurchaseRequest().getCode());

        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<CNVSDetail> details = new ArrayList<>();

        Canvass voucher = canvassRepo.findById(id).orElse(null);
        if (voucher != null) {
            List<CanvassDetailDto> cnvsDetailLineDtos = canvassDetailDto.getCanvassDetails(id);

            if (!Checker.collectionIsEmpty(cnvsDetailLineDtos)) {
                for(CanvassDetailDto dto : cnvsDetailLineDtos) {
                    CNVSDetail d = new CNVSDetail();

                    d.setId(cnvsDetailLineDtos.indexOf(dto) + 1);
                    d.setDescription(dto.getItemDescription());
                    d.setUnitCode(dto.getUnitCode());
                    d.setQuantity(dto.getQuantity());
                    d.setUnitPrice(dto.getUnitPrice());

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
        Canvass canvass =  canvassRepo.findById(postData.getDocumentId()).orElse(null);

        if (canvass != null) {
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            canvass.setDocumentStatus(afterActionDocumentStatus);
            canvass.setUpdatedAt(null);
            canvass = canvassRepo.save(canvass);

            if (canvass != null) {
                documentProcessingFacade.processAction(canvass.getTransaction(), actionMap, null, processedBy);

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
        Canvass voucher = canvassRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(Canvass canvass) {
        return documentLoggerFacade.makeLog(canvass);
    }

    private List<Map> makeCanvassListMap(List<Canvass> cs ) {

        List<Map> mapList = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cs)) {
            for(Canvass c:cs) {
                mapList.add(composeCanvassMap(c));
            }
        }
        return mapList;
    }

    private Map composeCanvassMap(Canvass canvass) {
        Map map = new HashMap();

        map.put("id", canvass.getId());
        map.put("localCode", canvass.getCode());
        map.put("voucherDate", canvass.getVoucherDate());
        map.put("preparedBy", canvass.getCreatedBy().getFullName());
        map.put("status", canvass.getDocumentStatus().getStatus());

        return map;
    }

}
