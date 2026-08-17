package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.DocumentWorkflowActionMapRepo;
import com.noreco1.fireflyv2.repo.ReturnMemorandumReceiptDetailRepo;
import com.noreco1.fireflyv2.repo.ReturnMemorandumReceiptRepo;
import com.noreco1.fireflyv2.repo.StockWithdrawalDetailRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ReturnMemorandumReceiptDto;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDto;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReturnMemorandumReceiptService;
import com.noreco1.fireflyv2.validator.ReturnMemorandumReceiptValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@Service(value = "returnMemorandumReceiptServiceImpl")
public class ReturnMemorandumReceiptServiceImpl implements ReturnMemorandumReceiptService, PrintableVoucher {

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private SignatureFacade signatureFacade;

    @Autowired
    private DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private StockWithdrawalDetailRepo stockWithdrawalDetailRepo;

    @Autowired
    private DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private ReturnMemorandumReceiptRepo returnMemorandumReceiptRepo;

    @Autowired
    private ReturnMemorandumReceiptDetailRepo returnMemorandumReceiptDetailRepo;

    @Override
    public Page<ReturnMemorandumReceipt> findAll(String startDate, String endDate, Pageable pageable) {
        return returnMemorandumReceiptRepo.findAllByDateBetweenOrderByDateAscCodeAsc(DateHelper.strToDate(startDate, "yyyy-MM-dd"), DateHelper.strToDate(endDate, "yyyy-MM-dd"), pageable);
    }

    @Override
    public Page<ReturnMemorandumReceipt> findAllByQuery(String query, String startDate, String endDate, Pageable pageable) {
        return returnMemorandumReceiptRepo.findAllByCodeContainsAndDateBetweenOrderByDateAscCodeAsc(query, DateHelper.strToDate(startDate, "yyyy-MM-dd"), DateHelper.strToDate(endDate, "yyyy-MM-dd"), pageable);
    }

    @Override
    public Page<ReturnMemorandumReceipt> findAllByEmployee(Integer employeeAccountNo, String startDate, String endDate, Pageable pageable) {
        return returnMemorandumReceiptRepo.findAllByMemorandumReceiptEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(employeeAccountNo, DateHelper.strToDate(startDate, "yyyy-MM-dd"), DateHelper.strToDate(endDate, "yyyy-MM-dd"), pageable);
    }

    @Override
    public Page<ReturnMemorandumReceipt> findAllByQueryAndEmployee(String query, Integer employeeAccountNo, String startDate, String endDate, Pageable pageable) {
        return returnMemorandumReceiptRepo.findAllByCodeContainsAndMemorandumReceiptEmployeeAccountNoAndDateBetweenOrderByDateAscCodeAsc(query, employeeAccountNo, DateHelper.strToDate(startDate, "yyyy-MM-dd"), DateHelper.strToDate(endDate, "yyyy-MM-dd"), pageable);
    }

    @Override
    public ReturnMemorandumReceiptDto findById(Integer id) {

        ReturnMemorandumReceiptDto returnMemorandumReceiptDto = new ReturnMemorandumReceiptDto();

        try {

            ReturnMemorandumReceipt returnMemorandumReceipt =  returnMemorandumReceiptRepo.findById(id).orElse(null);

            if(returnMemorandumReceipt != null){

                returnMemorandumReceiptDto.setId(returnMemorandumReceipt.getId());
                returnMemorandumReceiptDto.setCode(returnMemorandumReceipt.getCode());
                returnMemorandumReceiptDto.setDate(returnMemorandumReceipt.getDate());
                returnMemorandumReceiptDto.setEmployee(returnMemorandumReceipt.getMemorandumReceipt().getEmployee());
                returnMemorandumReceiptDto.setOffice(returnMemorandumReceipt.getMemorandumReceipt().getOffice());
                returnMemorandumReceiptDto.setUser(returnMemorandumReceipt.getCreatedBy());
                returnMemorandumReceiptDto.setDocumentStatus(returnMemorandumReceipt.getDocumentStatus());
                returnMemorandumReceiptDto.setWorkflow(returnMemorandumReceipt.getWorkflow());
                returnMemorandumReceiptDto.setTransaction(returnMemorandumReceipt.getTransaction());
                returnMemorandumReceiptDto.setMemorandumReceipt(returnMemorandumReceipt.getMemorandumReceipt());
                returnMemorandumReceiptDto.setRemarks(returnMemorandumReceipt.getRemarks());

                List<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails = returnMemorandumReceiptDetailRepo.findAllByReturnMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(returnMemorandumReceipt.getId());
                returnMemorandumReceiptDto.setReturnMemorandumReceiptDetails(returnMemorandumReceiptDetails);

                StockWithdrawalDto stockWithdrawalDto = new StockWithdrawalDto();
                stockWithdrawalDto.setId(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getId());
                stockWithdrawalDto.setCode(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getCode());
                stockWithdrawalDto.setDescription(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getDescription());
                stockWithdrawalDto.setVoucherDate(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getVoucherDate());
                stockWithdrawalDto.setType(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getType());

                ArrayList<StockWithdrawalDetail> details = stockWithdrawalDetailRepo.findByStockWithdrawalTransactionId(returnMemorandumReceipt.getMemorandumReceipt().getStockWithdrawal().getTransaction().getId());

                stockWithdrawalDto.setDetails(details);

                returnMemorandumReceiptDto.setStockWithdrawal(stockWithdrawalDto);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return returnMemorandumReceiptDto;

    }

    @Override
    public PostResponse create(ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            ReturnMemorandumReceiptValidator validator = new ReturnMemorandumReceiptValidator();
            validator.setService(this);
            validator.validate(returnMemorandumReceipt, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save return memorandum receipt");
            }else {

                ReturnMemorandumReceipt existingReturnMemorandumReceipt = null;

                Integer returnMemorandumReceiptYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(returnMemorandumReceipt.getDate()));

                Boolean insertMode = returnMemorandumReceipt.getId() == null;
                if (insertMode) { // insert mode

                    Object latestCode = returnMemorandumReceiptRepo.findLatestCodeByYear(returnMemorandumReceiptYear);
                    String code = generatorFacade.voucherCodeNoOffice("RMRTE", (latestCode == null ? "" : String.valueOf(latestCode)), returnMemorandumReceipt.getDate(), GlobalConstant.COUNTER_PAD_4);
                    returnMemorandumReceipt.setCode(code);

                    DocumentStatus documentStatus = new DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    returnMemorandumReceipt.setDocumentStatus(documentStatus);

                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RMRTE.getId());
                    returnMemorandumReceipt.setWorkflow(wf);

                    returnMemorandumReceipt.setTransaction(generatorFacade.transaction());
                    returnMemorandumReceipt.setCreatedBy(authenticationFacade.getLoggedIn());

                    existingReturnMemorandumReceipt = returnMemorandumReceipt;

                } else {
                    existingReturnMemorandumReceipt = returnMemorandumReceiptRepo.findById(returnMemorandumReceipt.getId()).orElse(null);
                }

                // use for document logging
                Map oldMap = this.forLogMapMain(existingReturnMemorandumReceipt);

                existingReturnMemorandumReceipt.setMemorandumReceipt(returnMemorandumReceipt.getMemorandumReceipt());
                existingReturnMemorandumReceipt.setReturnMemorandumReceiptDetails(returnMemorandumReceipt.getReturnMemorandumReceiptDetails());
                existingReturnMemorandumReceipt.setRemarks(returnMemorandumReceipt.getRemarks());
                existingReturnMemorandumReceipt.setOffice(returnMemorandumReceipt.getOffice());

                ReturnMemorandumReceipt savedReturnMemorandumReceipt = returnMemorandumReceiptRepo.save(existingReturnMemorandumReceipt);

                if (Checker.isValidId(savedReturnMemorandumReceipt.getId())) {

                    if (!insertMode) {
                        returnMemorandumReceiptDetailRepo.deleteAllByReturnMemorandumReceiptId(existingReturnMemorandumReceipt.getId());
                    }

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(savedReturnMemorandumReceipt.getTransaction(), null, savedReturnMemorandumReceipt.getWorkflow(), authenticationFacade.getLoggedIn());
                        oldMap = null;
                    }

                    for(ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail : savedReturnMemorandumReceipt.getReturnMemorandumReceiptDetails()) {

                        ReturnMemorandumReceiptDetail detail = new ReturnMemorandumReceiptDetail();

                        detail.setReturnMemorandumReceipt(savedReturnMemorandumReceipt);
                        detail.setStockWithdrawalDetail(returnMemorandumReceiptDetail.getStockWithdrawalDetail());
                        detail.setQuantity(returnMemorandumReceiptDetail.getQuantity());
                        detail.setReturnedQuantity(returnMemorandumReceiptDetail.getReturnedQuantity());
                        detail.setUsable(returnMemorandumReceiptDetail.getUsable());

                        returnMemorandumReceiptDetailRepo.save(detail);

                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(savedReturnMemorandumReceipt.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(savedReturnMemorandumReceipt.getId());
                    response.setSuccessMessage("Memorandum receipt returned successfully!");
                    response.setSuccess(true);

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse update(ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult, MessageSource messageSource) {
        return this.create(returnMemorandumReceipt, bindingResult, messageSource);
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            User processedBy = authenticationFacade.getLoggedIn();
            ReturnMemorandumReceipt returnMemorandumReceipt = this.returnMemorandumReceiptRepo.findById(postData.getDocumentId()).orElse(null);

            if (returnMemorandumReceipt != null) {
                // for logging
                Map oldMap = this.forLogMapMain(returnMemorandumReceipt);
                DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
                DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

                // set dynamic property here
                if (actionMap.getPropSignatureType() != null) {
                    try {
                        ClassHelper.setSignatoryValue(returnMemorandumReceipt, returnMemorandumReceipt.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                returnMemorandumReceipt.setDocumentStatus(afterActionDocumentStatus);
                returnMemorandumReceipt.setUpdatedAt(null);
                returnMemorandumReceipt = this.returnMemorandumReceiptRepo.save(returnMemorandumReceipt);

                // for logging
                Map newMap = this.forLogMapMain(returnMemorandumReceipt);
                newMap.put("remarks", postData.getRemarks());

                if (returnMemorandumReceipt != null) {
                    documentProcessingFacade.processAction(returnMemorandumReceipt.getTransaction(), actionMap, null, processedBy);
                    documentLoggerFacade.log(returnMemorandumReceipt.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

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
    public Page<ReturnMemorandumReceipt> findAllForReassignment(String query, Pageable pageable) {
        return returnMemorandumReceiptRepo.findAllForMemorandumReceipt(query, pageable);
    }

    @Override
    public ArrayList<ReturnMemorandumReceiptDetail> findAllByReturnMR(Integer id) {
        return returnMemorandumReceiptDetailRepo.findAllByReturnMemorandumReceiptIdAndUsableTrueOrderByStockWithdrawalDetailItemDescriptionAsc(id);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ReturnMemorandumReceipt returnMemorandumReceipt = returnMemorandumReceiptRepo.findById(vid).orElse(null);

        if (returnMemorandumReceipt != null) {
            params.put("CODE", returnMemorandumReceipt.getCode());
            params.put("DATE", returnMemorandumReceipt.getDate());
            params.put("EMPLOYEE", returnMemorandumReceipt.getMemorandumReceipt().getEmployee() != null ? returnMemorandumReceipt.getMemorandumReceipt().getEmployee().getName() : returnMemorandumReceipt.getOffice().getName());
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.RMRTE, returnMemorandumReceipt);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails = new ArrayList<>();
        ReturnMemorandumReceipt returnMemorandumReceipt = returnMemorandumReceiptRepo.findById(vid).orElse(null);
        if (returnMemorandumReceipt != null) {
            returnMemorandumReceiptDetails = returnMemorandumReceiptDetailRepo.findAllByReturnMemorandumReceiptIdOrderByStockWithdrawalDetailItemDescriptionAsc(returnMemorandumReceipt.getId());
        }
        return new JRBeanCollectionDataSource(returnMemorandumReceiptDetails);
    }

    private Map forLogMapMain(ReturnMemorandumReceipt returnMemorandumReceipt) {
        return  documentLoggerFacade.makeLog(returnMemorandumReceipt);
    }

}
