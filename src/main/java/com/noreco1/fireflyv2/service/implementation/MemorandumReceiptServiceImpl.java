package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.MemorandumReceiptService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.MemorandumReceiptValidator;
import com.noreco1.fireflyv2.validator.MultipleMemorandumReceiptValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Tri-Nvent on 3/27/2020.
 */
@Service(value = "memorandumReceiptServiceImpl")
public class MemorandumReceiptServiceImpl implements MemorandumReceiptService, PrintableVoucher {

    @Autowired
    MemorandumReceiptRepo memorandumReceiptRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    MemorandumReceiptDetailRepo memorandumReceiptDetailRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private StockWithdrawalEmployeeRepo stockWithdrawalEmployeeRepo;

    @Autowired
    private SlEntityRepo slEntityRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SignatoryFacade signatoryFacade;

    @Autowired
    private DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private ReturnMemorandumReceiptDetailRepo returnMemorandumReceiptDetailRepo;

    @Autowired
    private SignatureFacade signatureFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MemorandumReceipt> findAll(String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return memorandumReceiptRepo.findAllByDateBetweenOrderByDateAscCodeAsc(fromDate, toDate, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MemorandumReceipt> findAllByQuery(String query, String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return memorandumReceiptRepo.findAllByCodeContainsAndDateBetweenOrderByDateAscCodeAsc(query, fromDate, toDate, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MemorandumReceipt> findAllByEmployee(Integer employeeAccountNo, String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return memorandumReceiptRepo.findAllByEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(employeeAccountNo, fromDate, toDate, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MemorandumReceipt> findAllByQueryAndEmployee(String query, Integer employeeAccountNo, String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return memorandumReceiptRepo.findAllByCodeContainsAndEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(query, employeeAccountNo, fromDate, toDate, pageable);
    }

    @Override
    @Transactional
    public PostResponse update(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource) {
        return this.create(memorandumReceipt, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse create(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            MemorandumReceiptValidator validator = new MemorandumReceiptValidator();
            validator.setService(this);
            validator.validate(memorandumReceipt, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save Memorandum Receipt");
            }else {
                User createdBy = authenticationFacade.getLoggedIn();
                Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                MemorandumReceipt existingMemorandumReceipt = null;

                Boolean insertMode = memorandumReceipt.getId() == null;

                if (insertMode) { // insert mode

                    Integer memorandumReceiptYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(memorandumReceipt.getDate()));
                    Object latestCode = memorandumReceiptRepo.findLatestCodeByYear(memorandumReceiptYear);
                    String code = generatorFacade.voucherCodeNoOffice("MRTE", (latestCode == null ? "" : String.valueOf(latestCode)), memorandumReceipt.getDate(), GlobalConstant.COUNTER_PAD_4);

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    memorandumReceipt.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MRTE.getId());
                    memorandumReceipt.setWorkflow(wf);

                    User approvedBy = userRepo.findOneByAccountNo(memorandumReceipt.getApprovingOfficer().getAccountNo());

                    memorandumReceipt.setCode(code);
                    memorandumReceipt.setCreatedBy(authenticationFacade.getLoggedIn());
                    memorandumReceipt.setTransaction(generatorFacade.transaction());
                    memorandumReceipt.setApprovingOfficer(approvedBy);

                    existingMemorandumReceipt = memorandumReceipt;

                } else {
                    existingMemorandumReceipt = memorandumReceiptRepo.findById(memorandumReceipt.getId()).orElse(null);
                }
                // use for document logging
                Map oldMap = this.forLogMapMain(existingMemorandumReceipt);

                existingMemorandumReceipt.setOffice(loggedInEmployee.getOffice());
                existingMemorandumReceipt.setReturnMemorandumReceipt(memorandumReceipt.getReturnMemorandumReceipt());
                existingMemorandumReceipt.setMemorandumReceiptDetails(memorandumReceipt.getMemorandumReceiptDetails());

                MemorandumReceipt savedMemorandumReceipt = memorandumReceiptRepo.save(existingMemorandumReceipt);

                if (savedMemorandumReceipt != null) {

                    if (!insertMode) {
                        memorandumReceiptDetailRepo.deleteAllByMemorandumReceiptId(savedMemorandumReceipt.getId());
                    }

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(savedMemorandumReceipt.getTransaction(), null, savedMemorandumReceipt.getWorkflow(), authenticationFacade.getLoggedIn());
                        oldMap = null;
                    }

                    this.saveMemorandumReceiptDetail(savedMemorandumReceipt, memorandumReceipt.getMemorandumReceiptDetails());

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(savedMemorandumReceipt.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(savedMemorandumReceipt.getId());
                    response.setSuccessMessage("MR successfully saved!");
                    response.setSuccess(true);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public PostResponse createMultiple(List<MemorandumReceipt> memorandumReceipts, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            MultipleMemorandumReceiptValidator validator = new MultipleMemorandumReceiptValidator();
            validator.setService(this);
            validator.validate(memorandumReceipts, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save Memorandum Receipt");
            }else {

                User createdBy = authenticationFacade.getLoggedIn();
                Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                List<MemorandumReceipt> savedMemorandumReceipts = new ArrayList<>();

                for (MemorandumReceipt memorandumReceipt: memorandumReceipts){

                    Integer memorandumReceiptYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(memorandumReceipt.getDate()));
                    Object latestCode = memorandumReceiptRepo.findLatestCodeByYear(memorandumReceiptYear);
                    String code = generatorFacade.voucherCodeNoOffice("MRTE", (latestCode == null ? "" : String.valueOf(latestCode)), memorandumReceipt.getDate(), GlobalConstant.COUNTER_PAD_4);

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    memorandumReceipt.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MRTE.getId());
                    memorandumReceipt.setWorkflow(wf);

                    User approvedBy = userRepo.findOneByAccountNo(memorandumReceipt.getApprovingOfficer().getAccountNo());

                    memorandumReceipt.setCode(code);
                    memorandumReceipt.setCreatedBy(authenticationFacade.getLoggedIn());
                    memorandumReceipt.setTransaction(generatorFacade.transaction());
                    memorandumReceipt.setApprovingOfficer(approvedBy);
                    memorandumReceipt.setOffice(loggedInEmployee.getOffice());

                    MemorandumReceipt savedMemo = memorandumReceiptRepo.save(memorandumReceipt);

                    if(Checker.isValidId(savedMemo.getId())){

                        // start: update default signatories
                        signatoryFacade.memorandumReceipt(savedMemo);
                        // end: update default signatories

                        this.saveMemorandumReceiptDetail(savedMemo, memorandumReceipt.getMemorandumReceiptDetails());

                        documentProcessingFacade.processAction(savedMemo.getTransaction(), null, savedMemo.getWorkflow(), savedMemo.getCreatedBy());
                        documentLoggerFacade.log(savedMemo.getTransaction(), authenticationFacade.getLoggedIn(), null, documentLoggerFacade.makeLog(savedMemo));

                        savedMemorandumReceipts.add(savedMemo);

                    }

                }

                if(Checker.collectionIsNotEmpty(savedMemorandumReceipts)){
                    response.setSuccess(true);
                    response.setSuccessMessage(savedMemorandumReceipts.size() + " memorandum receipt has been successfully created");
                } else {
                    response.setSuccess(false);
                    response.setFailureMessage("Failed to create multiple memorandum receipt");
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse createReturnedMR(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            MemorandumReceiptValidator validator = new MemorandumReceiptValidator();
            validator.setService(this);
            validator.validate(memorandumReceipt, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save Memorandum Receipt");
            }else {

                User createdBy = authenticationFacade.getLoggedIn();
                Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

                MemorandumReceipt existingMemorandumReceipt = null;

                Boolean insertMode = memorandumReceipt.getId() == null;

                if (insertMode) { // insert mode

                    Integer memorandumReceiptYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(memorandumReceipt.getDate()));
                    Object latestCode = memorandumReceiptRepo.findLatestCodeByYear(memorandumReceiptYear);
                    String code = generatorFacade.voucherCodeNoOffice("MRTE", (latestCode == null ? "" : String.valueOf(latestCode)), memorandumReceipt.getDate(), GlobalConstant.COUNTER_PAD_4);

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    memorandumReceipt.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MRTE.getId());
                    memorandumReceipt.setWorkflow(wf);

                    User approvedBy = userRepo.findOneByAccountNo(memorandumReceipt.getApprovingOfficer().getAccountNo());

                    memorandumReceipt.setCode(code);
                    memorandumReceipt.setCreatedBy(authenticationFacade.getLoggedIn());
                    memorandumReceipt.setTransaction(generatorFacade.transaction());
                    memorandumReceipt.setApprovingOfficer(approvedBy);

                    existingMemorandumReceipt = memorandumReceipt;

                } else {
                    existingMemorandumReceipt = memorandumReceiptRepo.findById(memorandumReceipt.getId()).orElse(null);
                }
                // use for document logging
                Map oldMap = this.forLogMapMain(existingMemorandumReceipt);

                existingMemorandumReceipt.setReturnMemorandumReceipt(memorandumReceipt.getReturnMemorandumReceipt());
                existingMemorandumReceipt.setMemorandumReceiptDetails(memorandumReceipt.getMemorandumReceiptDetails());
                existingMemorandumReceipt.setOffice(loggedInEmployee.getOffice());

                MemorandumReceipt savedMemorandumReceipt = memorandumReceiptRepo.save(existingMemorandumReceipt);

                if (savedMemorandumReceipt != null) {

                    if (!insertMode) {
                        memorandumReceiptDetailRepo.deleteAllByMemorandumReceiptId(savedMemorandumReceipt.getId());
                    }

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(savedMemorandumReceipt.getTransaction(), null, savedMemorandumReceipt.getWorkflow(), authenticationFacade.getLoggedIn());
                        oldMap = null;
                    }

                    this.saveMemorandumReceiptDetail(savedMemorandumReceipt, memorandumReceipt.getMemorandumReceiptDetails());

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(savedMemorandumReceipt.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(savedMemorandumReceipt.getId());
                    response.setSuccessMessage("Returned MR successfully saved!");
                    response.setSuccess(true);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            User processedBy = authenticationFacade.getLoggedIn();
            MemorandumReceipt memorandumReceipt = this.memorandumReceiptRepo.findById(postData.getDocumentId()).orElse(null);

            if (memorandumReceipt != null) {
                // for logging
                Map oldMap = this.forLogMapMain(memorandumReceipt);
                DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
                DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

                // set dynamic property here
                if (actionMap.getPropSignatureType() != null) {
                    try {
                        ClassHelper.setSignatoryValue(memorandumReceipt, memorandumReceipt.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                memorandumReceipt.setDocumentStatus(afterActionDocumentStatus);
                memorandumReceipt.setUpdatedAt(null);
                memorandumReceipt = this.memorandumReceiptRepo.save(memorandumReceipt);

                // for logging
                Map newMap = this.forLogMapMain(memorandumReceipt);
                newMap.put("remarks", postData.getRemarks());

                if (memorandumReceipt != null) {
                    documentProcessingFacade.processAction(memorandumReceipt.getTransaction(), actionMap, null, processedBy);
                    documentLoggerFacade.log(memorandumReceipt.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

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
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.MRTE);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public MemorandumReceiptDto findById(Integer id) {
        MemorandumReceipt memorandumReceipt =  memorandumReceiptRepo.findById(id).orElse(null);
        MemorandumReceiptDto memorandumReceiptDto = new MemorandumReceiptDto();
        if(memorandumReceipt != null){

            memorandumReceiptDto.setId(memorandumReceipt.getId());
            memorandumReceiptDto.setCode(memorandumReceipt.getCode());
            memorandumReceiptDto.setDate(memorandumReceipt.getDate());
            memorandumReceiptDto.setEmployee(memorandumReceipt.getEmployee());
            memorandumReceiptDto.setOffice(memorandumReceipt.getOffice());
            memorandumReceiptDto.setUser(memorandumReceipt.getCreatedBy());
            memorandumReceiptDto.setApprovingOfficer(memorandumReceipt.getApprovingOfficer());
            memorandumReceiptDto.setDocumentStatus(memorandumReceipt.getDocumentStatus());
            memorandumReceiptDto.setWorkflow(memorandumReceipt.getWorkflow());
            memorandumReceiptDto.setTransaction(memorandumReceipt.getTransaction());
            List<MemorandumReceiptDetail> memorandumReceiptDetails = memorandumReceiptDetailRepo.findAllByMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(memorandumReceipt.getId());
            memorandumReceiptDto.setMemorandumReceiptDetails(memorandumReceiptDetails);

            if(memorandumReceipt.getStockWithdrawal() != null){
                StockWithdrawalDto stockWithdrawalDto = new StockWithdrawalDto();
                stockWithdrawalDto.setId(memorandumReceipt.getStockWithdrawal().getId());
                stockWithdrawalDto.setCode(memorandumReceipt.getStockWithdrawal().getCode());
                stockWithdrawalDto.setDescription(memorandumReceipt.getStockWithdrawal().getDescription());
                stockWithdrawalDto.setVoucherDate(memorandumReceipt.getStockWithdrawal().getVoucherDate());
                stockWithdrawalDto.setType(memorandumReceipt.getStockWithdrawal().getType());

                ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalTransactionId(memorandumReceipt.getStockWithdrawal().getTransaction().getId());

                stockWithdrawalDto.setDetails(details);

                memorandumReceiptDto.setStockWithdrawal(stockWithdrawalDto);
            }

            if(memorandumReceipt.getReturnMemorandumReceipt() != null){

                ReturnMemorandumReceiptDto returnMemorandumReceiptDto = new ReturnMemorandumReceiptDto();

                returnMemorandumReceiptDto.setId(memorandumReceipt.getReturnMemorandumReceipt().getId());
                returnMemorandumReceiptDto.setCode(memorandumReceipt.getReturnMemorandumReceipt().getCode());
                returnMemorandumReceiptDto.setDate(memorandumReceipt.getReturnMemorandumReceipt().getDate());
                returnMemorandumReceiptDto.setEmployee(memorandumReceipt.getReturnMemorandumReceipt().getMemorandumReceipt().getEmployee());

                ArrayList<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails = returnMemorandumReceiptDetailRepo.findAllByReturnMemorandumReceiptIdAndUsableTrueOrderByStockWithdrawalDetailItemDescriptionAsc(memorandumReceipt.getReturnMemorandumReceipt().getId());
                returnMemorandumReceiptDto.setReturnMemorandumReceiptDetails(returnMemorandumReceiptDetails);

                memorandumReceiptDto.setReturnMemorandumReceipt(returnMemorandumReceiptDto);

            }

            List<ItemTransactionDetail> itemTransactionDetails = itemTransactionDetailRepo.findAllByMemorandumReceiptDetailMemorandumReceiptId(memorandumReceipt.getId());
            memorandumReceiptDto.setHasMst(Checker.collectionIsNotEmpty(itemTransactionDetails));

        }

        return memorandumReceiptDto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getStockWithdrawalBalance(Integer stockWithdrawalDetailId) {
        List<Map> ret = new ArrayList<>();

        List<Object[]> stockWithdrawalBalances = memorandumReceiptDetailRepo.getStockWithdrawalBalance(stockWithdrawalDetailId);
        for (Object[] stockWithdrawalBalance : stockWithdrawalBalances) {
            Map data = new HashMap();
            data.put("assigned", stockWithdrawalBalance != null ? stockWithdrawalBalance[1] : 0);
            ret.add(data);
        }

        return ret;
    }

    private void saveMemorandumReceiptDetail(MemorandumReceipt savedMemorandumReceipt, List<MemorandumReceiptDetail> memorandumReceiptDetails) {
        if (!Checker.collectionIsEmpty(memorandumReceiptDetails)) {
            for (MemorandumReceiptDetail memorandumReceiptDetail : memorandumReceiptDetails) {
                memorandumReceiptDetail.setMemorandumReceipt(savedMemorandumReceipt);
                memorandumReceiptDetailRepo.save(memorandumReceiptDetail);
            }
        }
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MemorandumReceipt memorandumReceipt = memorandumReceiptRepo.findById(id).orElse(null);

        if (memorandumReceipt != null) {

            String issuedTo = "";

            if(memorandumReceipt.getEmployee() != null){
                issuedTo = memorandumReceipt.getEmployee().getName();
            } else {
                issuedTo = memorandumReceipt.getOffice().getName();
            }

            params.put("CODE", memorandumReceipt.getCode());
            params.put("STOCK_WITHDRAWAL_CODE", memorandumReceipt.getStockWithdrawal() != null ? memorandumReceipt.getStockWithdrawal().getCode() : "");
            params.put("RETURNED_MR_CODE", memorandumReceipt.getReturnMemorandumReceipt() != null ? memorandumReceipt.getReturnMemorandumReceipt().getCode() : "");
            params.put("DATE", memorandumReceipt.getDate());
            params.put("ISSUED_TO", issuedTo);
            params = signatureFacade.getDocumentSignature(params, DocumentType.MRTE, memorandumReceipt);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<MemorandumReceiptDetail> memorandumReceiptDetails = new ArrayList<>();
        MemorandumReceipt memorandumReceipt = memorandumReceiptRepo.findById(id).orElse(null);
        if (memorandumReceipt != null) {
            memorandumReceiptDetails = memorandumReceiptDetailRepo.findAllByMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(memorandumReceipt.getId());

            for (MemorandumReceiptDetail memorandumReceiptDetail : memorandumReceiptDetails){
                if(memorandumReceiptDetail.getReassignedQuantity().compareTo(BigDecimal.ZERO) == 1){
                    memorandumReceiptDetail.setQuantity(memorandumReceiptDetail.getReassignedQuantity().intValue());
                }
            }

        }
        return new JRBeanCollectionDataSource(memorandumReceiptDetails);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable) {
        Page<MemorandumReceipt> memorandumReceipts;

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        Integer invLocId = employee.getOffice().getInventoryLocation().getId();

        if(query != null){
            memorandumReceipts = memorandumReceiptRepo.findAllForStockRelease("%"+query.toUpperCase()+"%", invLocId, pageable);
        } else {
            memorandumReceipts = memorandumReceiptRepo.findAllForStockRelease(invLocId, pageable);
        }

        return memorandumReceipts.map(entity -> {
                InventoryDocumentDto dto = new InventoryDocumentDto();
                List<MemorandumReceiptDetail> details = memorandumReceiptDetailRepo.findAllByMemorandumReceiptId(entity.getId());

                ArrayList<StockWithdrawalDetailDto> detailsDto = new ArrayList<>();
                for (MemorandumReceiptDetail d : details) {
                    detailsDto.add(d.toDto());
                }

                dto.setDate(entity.getDate());
                dto.setCode(entity.getCode());
                dto.setPurpose(entity.getStockWithdrawal().getDescription());
                dto.setWithdrawalDetails(detailsDto);
                dto.setTransId(entity.getTransaction().getId());
                dto.setCreatedBy(entity.getEmployee().getName());

                User user = new User();
                user.setId(entity.getStockWithdrawal().getCreatedBy().getId());
                user.setFullName(entity.getStockWithdrawal().getCreatedBy().getFullName());
                user.setAccountNo(entity.getStockWithdrawal().getCreatedBy().getAccountNo());

                dto.setCreatedByUser(user);

                dto.setDepartmentName(entity.getStockWithdrawal().getDepartment().getName());
                dto.setInventoryCategoryTypeId(entity.getStockWithdrawal().getInventoryCategory().getType());

                return dto;
        });
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public ArrayList<SlEntity> getStockWithdrawalEmployees(Integer stockWithdrawalId) {

        ArrayList<SlEntity> employees = new ArrayList<>();

        try {

            ArrayList<StockWithdrawalEmployee> stockWithdrawalEmployees = this.stockWithdrawalEmployeeRepo.findAllByStockWithdrawalId(stockWithdrawalId);

            if(Checker.collectionIsNotEmpty(stockWithdrawalEmployees)){

                for (StockWithdrawalEmployee stockWithdrawalEmployee : stockWithdrawalEmployees){

                    employees.add(slEntityRepo.findOneByAccountNo(stockWithdrawalEmployee.getEmployee().getAccountNumber()));

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return employees;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public ArrayList<MemorandumReceipt> getAllEmployeesMemorandumReceipt(Integer accountNo, Boolean forEditing) {

        ArrayList<MemorandumReceipt> memorandumReceipts = new ArrayList<>();

        try {

            if(forEditing){
                memorandumReceipts = memorandumReceiptRepo.findAllByEmployeeAccountNoAndDocumentStatusId(accountNo, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            } else {
                memorandumReceipts = memorandumReceiptRepo.findAllByEmployeeAccountNoNotInReturnMemorandumReceipt(accountNo, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            }

            if(Checker.collectionIsNotEmpty(memorandumReceipts)){

                for (MemorandumReceipt memorandumReceipt : memorandumReceipts){

                    List<MemorandumReceiptDetail> memorandumReceiptDetails = memorandumReceiptDetailRepo.findAllByMemorandumReceiptDetailNotInReturnDetailOrderByItemDescription(memorandumReceipt.getId());

                    if(Checker.collectionIsNotEmpty(memorandumReceiptDetails)){

                        for(MemorandumReceiptDetail memorandumReceiptDetail : memorandumReceiptDetails){

                            memorandumReceiptDetail.setReturned(returnMemorandumReceiptDetailRepo.getReturnedQuantity(memorandumReceiptDetail.getMemorandumReceipt().getId() , memorandumReceiptDetail.getStockWithdrawalDetail().getId()));

                        }

                    }

                    memorandumReceipt.setMemorandumReceiptDetails(memorandumReceiptDetails);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return memorandumReceipts;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public ArrayList<MemorandumReceipt> getAllOfficesMemorandumReceipt(Integer officeId, Boolean forEditing) {

        ArrayList<MemorandumReceipt> memorandumReceipts = new ArrayList<>();

        try {

            if(forEditing){
                memorandumReceipts = memorandumReceiptRepo.findAllByDocumentStatusIdAndEmployeeAccountNoIsNull(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            } else {
                memorandumReceipts = memorandumReceiptRepo.findAllByOfficeNotInReturnMemorandumReceipt(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            }

            if(Checker.collectionIsNotEmpty(memorandumReceipts)){

                for (MemorandumReceipt memorandumReceipt : memorandumReceipts){

                    List<MemorandumReceiptDetail> memorandumReceiptDetails = memorandumReceiptDetailRepo.findAllByMemorandumReceiptDetailNotInReturnDetailOrderByItemDescription(memorandumReceipt.getId());

                    if(Checker.collectionIsNotEmpty(memorandumReceiptDetails)){

                        for(MemorandumReceiptDetail memorandumReceiptDetail : memorandumReceiptDetails){

                            memorandumReceiptDetail.setReturned(returnMemorandumReceiptDetailRepo.getReturnedQuantity(memorandumReceiptDetail.getMemorandumReceipt().getId() , memorandumReceiptDetail.getStockWithdrawalDetail().getId()));

                        }

                    }

                    memorandumReceipt.setMemorandumReceiptDetails(memorandumReceiptDetails);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return memorandumReceipts;

    }

    private Map forLogMapMain(MemorandumReceipt memorandumReceipt) {
        return documentLoggerFacade.makeLog(memorandumReceipt);
    }

}
