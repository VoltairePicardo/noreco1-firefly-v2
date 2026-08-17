package com.noreco1.fireflyv2.service.implementation;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.repo.CheckVoucherIEMOPBillingRepo;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.controller.response.reports.CheckDto;
import com.noreco1.fireflyv2.service.Bir2307;
import com.noreco1.fireflyv2.service.CvService;
import com.noreco1.fireflyv2.service.PrintableCheque;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.CvValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service(value = "cvServiceImpl")
public class CvServiceImpl implements CvService, PrintableVoucher, PrintableCheque, Bir2307 {

    private CheckVoucher model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    CheckVoucherRepo cvRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    CheckVoucherChequeRepo chequeRepo;

    @Autowired
    CheckConfigRepo checkConfigRepo;

    @Autowired
    CheckVoucherApvRepo checkVoucherApvRepo;

    @Autowired
    CheckVoucherApvPaidInstallmentRepo checkVoucherApvPaidInstallmentRepo;

    @Autowired
    AccountsPayableVoucherRepo apvRepo;

    @Autowired
    ReleasedCheckRepo releasedCheckRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    TokenService tokenService;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    CheckVoucherIncomePaymentRepo incomePaymentRepo;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    LedgerDtoer ledgerDtoer;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    OrganizationRepo organizationRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    CheckVoucherCashAdvanceRepo checkVoucherCashAdvanceRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    CheckVoucherJvRepo checkVoucherJvRepo;

    @Autowired
    CheckVoucherRrRepo checkVoucherRrRepo;

    @Autowired
    CheckVoucherJoAcceptanceRepo checkVoucherJoAcceptanceRepo;

    @Autowired
    BankAccountRepo bankAccountRepo;

    @Autowired
    CheckVoucherChequeRepo checkVoucherChequeRepo;

    @Autowired
    CheckVoucherIEMOPBillingRepo checkVoucherIEMOPBillingRepo;

    @Autowired
    AccountsPayableVoucherIEMOPBillingRepo accountsPayableVoucherIEMOPBillingRepo;

    @Autowired
    CheckVoucherBudgetDetailRepo checkVoucherBudgetDetailRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    TaxCodeRepo taxCodeRepo;

    @Autowired
    BudgetDetailRepo budgetDetailRepo;

    @Autowired
    BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CheckVoucher cv = (CheckVoucher) v;
        return this.processCreate(cv, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        CheckVoucher cv = (CheckVoucher) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        CvValidator validator = new CvValidator();
        validator.setLedgerDtoer(this.ledgerDtoers); // this is f*cking workaround
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.setLegderFacade(this.ledgerFacade);
        validator.validate(cv, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            CheckVoucher existingCv = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(cv.getVoucherDate()));

            User checker = userRepo.findOneByAccountNo(cv.getChecker().getAccountNo());
            User budgetOfficer = userRepo.findOneByAccountNo(cv.getBudgetOfficer().getAccountNo());
            User recApp = userRepo.findOneByAccountNo(cv.getRecommendingOfficer().getAccountNo());
            User auditingOfficer = userRepo.findOneByAccountNo(cv.getAuditingOfficer().getAccountNo());
            User checkPrinter = userRepo.findOneByAccountNo(cv.getCheckPrinter().getAccountNo());
            User approvingOfficer = userRepo.findOneByAccountNo(cv.getApprovingOfficer().getAccountNo());

            User secondCheckSign = null;
            if (cv.getSecondCheckSign() != null) {
                secondCheckSign = userRepo.findOneByAccountNo(cv.getSecondCheckSign().getAccountNo());
            }

            Boolean insertMode = cv.getId() == null;
            if (insertMode) { // insert mode
                Object latestApvCode = cvRepo.findLatestCvCodeByYear(voucherYear);
                cv.setCode(generatorFacade.voucherCodeNoOffice("CV", (latestApvCode == null ? "" : String.valueOf(latestApvCode)), cv.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CV.getId());

                cv.setDocumentStatus(documentStatus);
                cv.setCreatedBy(createdBy);
                cv.setTransaction(generatorFacade.transaction());
                cv.setWorkflow(wf);

                existingCv = cv;

            } else {
                existingCv = cvRepo.findById(cv.getId()).orElse(null);

                List<Integer> statusAllowed = new ArrayList();
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                if (statusAllowed.indexOf(existingCv.getDocumentStatus().getId()) < 0) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }
            // use for document logging
            Map oldJvMap = this.forLogMapMain(existingCv);

            // editable fields
            existingCv.setPayee(cv.getPayee());
            existingCv.setParticulars(cv.getParticulars());
            existingCv.setVoucherDate(cv.getVoucherDate());
            existingCv.setChecker(checker);
            existingCv.setBudgetOfficer(budgetOfficer);
            existingCv.setRecommendingOfficer(recApp);
            existingCv.setAuditingOfficer(auditingOfficer);
            existingCv.setCheckPrinter(checkPrinter);
            existingCv.setApprovingOfficer(approvingOfficer);
            existingCv.setSecondCheckSign(secondCheckSign);
            existingCv.setYear(voucherYear);
            existingCv.setAmount(cv.getAmount());
            existingCv.setCheckAmount(cv.getCheckAmount());
            existingCv.setRrNumber(cv.getRrNumber());
            existingCv.setAdditionalPayeeInfo(cv.getAdditionalPayeeInfo());
            existingCv.setBank(cv.getBank());
//            existingCv.setBudgetLineItemDetail(cv.getBudgetLineItemDetail());
            existingCv.setPurchaseOrder(cv.getPurchaseOrder());
            existingCv.setJobOrder(cv.getJobOrder());

            this.model = cvRepo.save(existingCv);

            if (this.model != null) {
                // start: update default signatories
                signatoryFacade.cv(this.model);
                // end: update default signatories

                // save apv
                checkVoucherApvRepo.deleteByCheckVoucherId(this.model.getId()); // with or without
                if (cv.getAccountsPayableVoucher() != null && cv.getAccountsPayableVoucher().getId() != null) {
                    CheckVoucherApv checkVoucherApv = new CheckVoucherApv();
                    checkVoucherApv.setCheckVoucher(this.model);
                    checkVoucherApv.setAccountsPayableVoucher(cv.getAccountsPayableVoucher());
                    CheckVoucherApv savedCheckVoucherApv = checkVoucherApvRepo.save(checkVoucherApv);

                    if (Checker.isValidId(savedCheckVoucherApv.getId()) && Checker.collectionIsNotEmpty(cv.getSelectedInstallmentDetails())) {
                        for (AccountsPayableVoucherInstallmentDetail detail : cv.getSelectedInstallmentDetails()) {
                            CheckVoucherApvPaidInstallment paidInstallment = new CheckVoucherApvPaidInstallment();
                            paidInstallment.setCheckVoucherApv(savedCheckVoucherApv);
                            paidInstallment.setAccountsPayableVoucherInstallmentDetail(detail);
                            checkVoucherApvPaidInstallmentRepo.save(paidInstallment);
                        }
                    }
                }

                // save iemop billing
                checkVoucherIEMOPBillingRepo.deleteByCheckVoucherId(this.model.getId()); // with or without

                if (!Checker.collectionIsEmpty(cv.getIemopBillings())) {
                    for(IEMOPBilling iemopBilling : cv.getIemopBillings()) {

                        CheckVoucherIEMOPBilling checkVoucherIEMOPBilling = new CheckVoucherIEMOPBilling();
                        checkVoucherIEMOPBilling.setCheckVoucher(this.model);
                        checkVoucherIEMOPBilling.setIemopBilling(iemopBilling);

                        checkVoucherIEMOPBillingRepo.save(checkVoucherIEMOPBilling);
                    }
                }

                // save cash advance
                checkVoucherCashAdvanceRepo.deleteByCheckVoucherId(this.model.getId());
               /* if (cv.getCashAdvance() != null && cv.getCashAdvance().getId() != null) {
                    CheckVoucherCashAdvance checkVoucherCashAdvance = new CheckVoucherCashAdvance();
                    checkVoucherCashAdvance.setCheckVoucher(this.model);
                    checkVoucherCashAdvance.setCashAdvance(cv.getCashAdvance());
                    checkVoucherCashAdvanceRepo.save(checkVoucherCashAdvance);
                }*/
                if (!cv.getCashAdvances().isEmpty()) {

                    for(CashAdvance cashAdvance : cv.getCashAdvances()){
                        CheckVoucherCashAdvance checkVoucherCashAdvance = new CheckVoucherCashAdvance();
                        checkVoucherCashAdvance.setCheckVoucher(this.model);
                        checkVoucherCashAdvance.setCashAdvance(cashAdvance);
                        checkVoucherCashAdvanceRepo.save(checkVoucherCashAdvance);
                    }

                }

                // save journal voucher
                checkVoucherJvRepo.deleteByCheckVoucherId(this.model.getId());
                if (cv.getJournalVoucher() != null && cv.getJournalVoucher().getId() != null) {
                    CheckVoucherJv checkVoucherJv = new CheckVoucherJv();
                    checkVoucherJv.setCheckVoucher(this.model);
                    checkVoucherJv.setJournalVoucher(cv.getJournalVoucher());
                    checkVoucherJvRepo.save(checkVoucherJv);
                }

                // save receiving report
                checkVoucherRrRepo.deleteByCheckVoucherId(this.model.getId());
                if (cv.getReceivingReport() != null && cv.getReceivingReport().getId() != null) {
                    CheckVoucherRr checkVoucherRr = new CheckVoucherRr();
                    checkVoucherRr.setCheckVoucher(this.model);
                    checkVoucherRr.setReceivingReport(cv.getReceivingReport());
                    checkVoucherRrRepo.save(checkVoucherRr);
                }

                // save jo acceptance
                checkVoucherJoAcceptanceRepo.deleteByCheckVoucherId(this.model.getId());
                if (cv.getJoAcceptance() != null && cv.getJoAcceptance().getId() != null) {
                    CheckVoucherJoAcceptance checkVoucherJoAcceptance = new CheckVoucherJoAcceptance();
                    checkVoucherJoAcceptance.setCheckVoucher(this.model);
                    checkVoucherJoAcceptance.setJoAcceptance(cv.getJoAcceptance());
                    checkVoucherJoAcceptanceRepo.save(checkVoucherJoAcceptance);
                }

                List<GeneralLedgerLineDto2> cvGeneralLedgerLines = cv.getGeneralLedgerLines();

                // save check numbers
                chequeRepo.deleteByTransactionId(this.model.getTransaction().getId());
                List<CheckVoucherCheque> checkNumbers = cv.getCheckNumbers();
                if (!Checker.collectionIsEmpty(checkNumbers)) {
                    for(CheckVoucherCheque ch:checkNumbers) {
                        ch.setTransaction(this.model.getTransaction());
                        ch.setCleared(false);
                        ch.setPrinted(false);
                        ch.setReleased(false);
                        chequeRepo.save(ch);
                    }
                }
                ledgerFacade.postGeneralLedger(this.model.getTransaction(), cvGeneralLedgerLines, cv.getSubLedgerLines(), this.model.getVoucherDate());

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldJvMap = null; // new document has no old value
                }

//                checkVoucherBudgetDetailRepo.deleteByCheckVoucherId(this.model.getId());
//                ArrayList<BudgetSubItem> budgetDetails = cv.getBudgetDetails();
//                for (BudgetSubItem budgetSubItem : budgetDetails){
//
//                    CheckVoucherBudgetDetail newCheckVoucherBudgetDetail = new CheckVoucherBudgetDetail();
//
//                    CheckVoucher voucher = new CheckVoucher();
//                    voucher.setId(this.model.getId());
//                    newCheckVoucherBudgetDetail.setCheckVoucher(voucher);
//
//                    newCheckVoucherBudgetDetail.setBudgetSubItem(budgetSubItem);
//                    newCheckVoucherBudgetDetail.setBudgetSubItemAmountBalanceCV(budgetSubItem.getBudgetSubItemAmountBalanceCV());
//                    newCheckVoucherBudgetDetail.setBudgetSubItemAmountBalancePOJO(budgetSubItem.getBudgetSubItemAmountBalancePOJO());
//                    newCheckVoucherBudgetDetail.setAmount(budgetSubItem.getAmount());
//
//                    checkVoucherBudgetDetailRepo.save(newCheckVoucherBudgetDetail);
//
//                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("CV successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            CheckVoucher jv = cvRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (jv != null) {
                Map map = forLogMapMain(jv);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CvListDto> findAll() {
        List<CheckVoucher> vouchers = cvRepo.findAll();
        return this.makeCvListDto(vouchers);
    }

    @Override
    public CvDto findById(Integer id) {

        CvDto cvDto = new CvDto();

        try {

            CheckVoucher checkVoucher =  cvRepo.findById(id).orElse(null);

            CheckVoucherApv checkVoucherApv = checkVoucherApvRepo.findByCheckVoucherId(id);
            CvVoucherDto apvDto = null;

            if(checkVoucherApv != null){
                apvDto = new CvVoucherDto();
                apvDto.setVoucherDate(checkVoucherApv.getAccountsPayableVoucher().getVoucherDate());
                apvDto.setLocalCode(checkVoucherApv.getAccountsPayableVoucher().getCode());
                apvDto.setId(checkVoucherApv.getAccountsPayableVoucher().getId());
                apvDto.setPreparedBy(checkVoucherApv.getAccountsPayableVoucher().getCreatedBy().getFullName());
                apvDto.setAmount(checkVoucherApv.getAccountsPayableVoucher().getAmount());
                apvDto.setParticulars(checkVoucherApv.getAccountsPayableVoucher().getCode() + " - " + checkVoucherApv.getAccountsPayableVoucher().getVendor().getName());
                apvDto.setSlentityAccountNo(checkVoucherApv.getAccountsPayableVoucher().getVendor().getAccountNo());
                apvDto.setSlentityName(checkVoucherApv.getAccountsPayableVoucher().getVendor().getName());
                apvDto.setInvoiceDate(checkVoucherApv.getAccountsPayableVoucher().getInvoiceDate());
                apvDto.setDueDate(checkVoucherApv.getAccountsPayableVoucher().getDueDate());
                apvDto.setTransId(checkVoucherApv.getAccountsPayableVoucher().getTransaction().getId());

            }

            /*CheckVoucherCashAdvance checkVoucherCashAdvance = checkVoucherCashAdvanceRepo.findByCheckVoucherId(id);
            CvVoucherDto cashAdvanceDto = null;
            if (checkVoucherCashAdvance != null) {
                cashAdvanceDto = new CvVoucherDto();
                cashAdvanceDto.setVoucherDate(checkVoucherCashAdvance.getCashAdvance().getVoucherDate());
                cashAdvanceDto.setLocalCode(checkVoucherCashAdvance.getCashAdvance().getCode());
                cashAdvanceDto.setId(checkVoucherCashAdvance.getCashAdvance().getId());
                cashAdvanceDto.setPreparedBy(checkVoucherCashAdvance.getCashAdvance().getCreatedBy().getFullName());
                cashAdvanceDto.setAmount(checkVoucherCashAdvance.getCashAdvance().getAmount());
                cashAdvanceDto.setParticulars(checkVoucherCashAdvance.getCashAdvance().getCode());
                cashAdvanceDto.setSlentityAccountNo(checkVoucherCashAdvance.getCashAdvance().getEmployee().getAccountNumber());
                cashAdvanceDto.setSlentityName(checkVoucherCashAdvance.getCashAdvance().getEmployee().getName());
                cashAdvanceDto.setTransId(checkVoucherCashAdvance.getCashAdvance().getTransaction().getId());
                cashAdvanceDto.setEmployee(checkVoucherCashAdvance.getCashAdvance().getEmployee());
            }*/

            List<CheckVoucherCashAdvance> checkVoucherCashAdvances = checkVoucherCashAdvanceRepo.findAllByCheckVoucherId(id);
            List<Map> cashAdvances = new ArrayList<>();
            if (!checkVoucherCashAdvances.isEmpty()) {

                for(CheckVoucherCashAdvance checkVoucherCashAdvance : checkVoucherCashAdvances){
                    Map caMap = new HashMap();

                    caMap.put("id", checkVoucherCashAdvance.getCashAdvance().getId());
                    caMap.put("voucherDate", checkVoucherCashAdvance.getCashAdvance().getVoucherDate());
                    caMap.put("localCode", checkVoucherCashAdvance.getCashAdvance().getCode());
                    caMap.put("particulars",  checkVoucherCashAdvance.getCashAdvance().getCode());
                    caMap.put("amount", checkVoucherCashAdvance.getCashAdvance().getAmount());
                    caMap.put("preparedBy", checkVoucherCashAdvance.getCashAdvance().getCreatedBy().getFullName());
                    caMap.put("slEntityAccountNo", checkVoucherCashAdvance.getCashAdvance().getEmployee().getAccountNumber());
                    caMap.put("slEntityName", checkVoucherCashAdvance.getCashAdvance().getEmployee().getName());
                    cashAdvances.add(caMap);
                }
            }

            CheckVoucherJv checkVoucherJv = checkVoucherJvRepo.findByCheckVoucherId(id);
            CvVoucherDto jvDto = null;
            if(checkVoucherJv != null){
                jvDto = new CvVoucherDto();
                jvDto.setVoucherDate(checkVoucherJv.getJournalVoucher().getVoucherDate());
                jvDto.setLocalCode(checkVoucherJv.getJournalVoucher().getCode());
                jvDto.setId(checkVoucherJv.getJournalVoucher().getId());
                jvDto.setPreparedBy(checkVoucherJv.getJournalVoucher().getCreatedBy().getFullName());
                jvDto.setAmount(checkVoucherJv.getJournalVoucher().getAmount());
                jvDto.setParticulars(checkVoucherJv.getJournalVoucher().getCode());
                jvDto.setTransId(checkVoucherJv.getJournalVoucher().getTransaction().getId());
            }

            CheckVoucherRr checkVoucherRr = checkVoucherRrRepo.findByCheckVoucherId(id);
            CvVoucherDto rrDto = null;
            if(checkVoucherRr != null){
                rrDto = new CvVoucherDto();
                rrDto.setVoucherDate(checkVoucherRr.getReceivingReport().getDeliveryDate());
                rrDto.setLocalCode(checkVoucherRr.getReceivingReport().getCode());
                rrDto.setId(checkVoucherRr.getReceivingReport().getId());
                rrDto.setPreparedBy(checkVoucherRr.getReceivingReport().getCreatedBy().getFullName());
                rrDto.setAmount(checkVoucherRr.getReceivingReport().getTotalAmount());
                rrDto.setParticulars(checkVoucherRr.getReceivingReport().getCode());
                rrDto.setTransId(checkVoucherRr.getReceivingReport().getTransaction().getId());
                rrDto.setSlentityAccountNo(checkVoucherRr.getReceivingReport().getSupplier().getAccountNumber());
                rrDto.setSlentityName(checkVoucherRr.getReceivingReport().getSupplier().getName());
            }

            CheckVoucherJoAcceptance checkVoucherJoAcceptance = checkVoucherJoAcceptanceRepo.findByCheckVoucherId(id);
            CvVoucherDto joaDto = null;
            if(checkVoucherJoAcceptance != null){
                joaDto = new CvVoucherDto();
                joaDto.setVoucherDate(checkVoucherJoAcceptance.getJoAcceptance().getVoucherDate());
                joaDto.setLocalCode(checkVoucherJoAcceptance.getJoAcceptance().getCode());
                joaDto.setId(checkVoucherJoAcceptance.getJoAcceptance().getId());
                joaDto.setPreparedBy(checkVoucherJoAcceptance.getJoAcceptance().getCreatedBy().getFullName());
                joaDto.setAmount(checkVoucherJoAcceptance.getJoAcceptance().getAmount());
                joaDto.setParticulars(checkVoucherJoAcceptance.getJoAcceptance().getCode());
                joaDto.setTransId(checkVoucherJoAcceptance.getJoAcceptance().getTransaction().getId());
                joaDto.setSlentityAccountNo(checkVoucherJoAcceptance.getJoAcceptance().getVendor().getAccountNo());
                joaDto.setSlentityName(checkVoucherJoAcceptance.getJoAcceptance().getVendor().getName());
            }

            List<CheckVoucherCheque> checkVoucherCheques = chequeRepo.findByTransactionId(checkVoucher.getTransaction().getId());
            List<Map> bankAccountDetailsMap = new ArrayList<>();
            if(Checker.collectionIsNotEmpty(checkVoucherCheques)){

                for(CheckVoucherCheque checkVoucherCheque : checkVoucherCheques){

                    Map map = new HashMap();

                    map.put("bankAccount", checkVoucherCheque.getBankAccount());
                    map.put("checkNumber", checkVoucherCheque.getCheckNumber());
                    map.put("released", checkVoucherCheque.getReleased());
                    map.put("cleared", checkVoucherCheque.getCleared());
                    map.put("printed", checkVoucherCheque.getPrinted());
                    map.put("transId", checkVoucherCheque.getTransaction().getId());
                    map.put("forCheckWriting", authenticationFacade.getLoggedIn().getAccountNo().equals(checkVoucher.getCheckPrinter().getAccountNo()) && checkVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CHECK_WRITING.getId()));

                    bankAccountDetailsMap.add(map);

                }

            }

            if (checkVoucher != null) {
                cvDto.setId(checkVoucher.getId());
                cvDto.setParticulars(checkVoucher.getParticulars());
                cvDto.setLocalCode(checkVoucher.getCode());
                cvDto.setTransId(checkVoucher.getTransaction().getId());

                SlEntity preparedBy = slEntityRepo.findById(checkVoucher.getCreatedBy().getAccountNo()).orElse(null);
                SlEntity checkPrinter = slEntityRepo.findById(checkVoucher.getCheckPrinter().getAccountNo()).orElse(null);
                SlEntity approvingOfficer = slEntityRepo.findById(checkVoucher.getApprovingOfficer().getAccountNo()).orElse(null);
                SlEntity budgetOfficer = slEntityRepo.findById(checkVoucher.getBudgetOfficer().getAccountNo()).orElse(null);
                SlEntity checker = slEntityRepo.findById(checkVoucher.getChecker().getAccountNo()).orElse(null);
                SlEntity rao = slEntityRepo.findById(checkVoucher.getRecommendingOfficer().getAccountNo()).orElse(null);
                SlEntity auditor = slEntityRepo.findById(checkVoucher.getAuditingOfficer().getAccountNo()).orElse(null);
                if (checkVoucher.getSecondCheckSign() != null) {
                    SlEntity secondCheckSign = slEntityRepo.findById(checkVoucher.getSecondCheckSign().getAccountNo()).orElse(null);
                    cvDto.setSecondCheckSign(secondCheckSign);
                }

                if (apvDto != null) {
                    cvDto.setApvDto(apvDto);
                }

                /*if (cashAdvanceDto != null) {
                    cvDto.setCashAdvanceDto(cashAdvanceDto);
                }
*/

                if (!cashAdvances.isEmpty()) {
                    cvDto.setCashAdvances(cashAdvances);
                }

                if (jvDto != null) {
                    cvDto.setJvDto(jvDto);
                }

                if (rrDto != null) {
                    cvDto.setRrDto(rrDto);
                }

                if (joaDto != null) {
                    cvDto.setJoaDto(joaDto);
                }

                if (Checker.collectionIsNotEmpty(checkVoucherCheques)) {
                    cvDto.setBankAccountDetailsMap(bankAccountDetailsMap);
                }

                if(checkVoucher.getPostedBy() != null) {
                    cvDto.setPostedBy(slEntityRepo.findById(checkVoucher.getPostedBy().getAccountNo()).orElse(null));
                }

                cvDto.setPayee(checkVoucher.getPayee());
                cvDto.setPreparedBy(preparedBy);
                cvDto.setApprovingOfficer(approvingOfficer);
                cvDto.setRecommendingOfficer(rao);
                cvDto.setAuditor(auditor);
                cvDto.setChecker(checker);
                cvDto.setBudgetOfficer(budgetOfficer);
                cvDto.setCheckPrinter(checkPrinter);
                cvDto.setVoucherDate(checkVoucher.getVoucherDate());
                cvDto.setAmount(checkVoucher.getAmount());
                cvDto.setCheckAmount(checkVoucher.getCheckAmount());
                cvDto.setDocumentStatus(checkVoucher.getDocumentStatus());
                cvDto.setCreated(checkVoucher.getCreatedAt());
                cvDto.setLastUpdated(checkVoucher.getUpdatedAt());
                cvDto.setRrNumber(checkVoucher.getRrNumber());
                cvDto.setOffice(checkVoucher.getOffice());
                cvDto.setAdditionalPayeeInfo(checkVoucher.getAdditionalPayeeInfo());
                cvDto.setAdditionalPayeeInfo(checkVoucher.getAdditionalPayeeInfo());
                cvDto.setBank(checkVoucher.getBank());
                cvDto.setBudgetLineItemDetail(checkVoucher.getBudgetLineItemDetail());
                cvDto.setBudgetDetail(checkVoucher.getBudgetDetail());
                cvDto.setPurchaseOrder(checkVoucher.getPurchaseOrder());
                cvDto.setJobOrder(checkVoucher.getJobOrder());

                List<CheckVoucherIEMOPBilling> cvIemopBillings = checkVoucherIEMOPBillingRepo.findAllByCheckVoucherId(checkVoucher.getId());
                List<IEMOPBilling> iemopBillings = new ArrayList<>();
                if(!cvIemopBillings.isEmpty()){
                    for(CheckVoucherIEMOPBilling cvIemopBilling : cvIemopBillings){

                        iemopBillings.add(cvIemopBilling.getIemopBilling());

                    }
                }

                cvDto.setIemopBillings(iemopBillings);

                List<CheckVoucherBudgetDetail> cvBudgetDetail = checkVoucherBudgetDetailRepo.findAllByCheckVoucherId(checkVoucher.getId());
                if(!cvBudgetDetail.isEmpty()){
                    cvDto.setBudgetDetails(cvBudgetDetail);
                }

                cvDto.setForAddingBudgetDetail(authenticationFacade.getLoggedIn().getAccountNo().equals(checkVoucher.getBudgetOfficer().getAccountNo()) && checkVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_BUDGET_OFFICER.getId()));

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return  cvDto;

    }

    @Override
    public ApvDto checkPrintingParams(Integer transId, Integer backAccountId) {
        return null;
    }

    @Override
    public PostResponse updateCheckNumber(CheckVoucherCheque cheque, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        chequeRepo.deleteByBankAccountIdAndTransactionId(cheque.getBankAccount().getId(), cheque.getTransaction().getId());

        if (cheque.getCheckNumber() != null && cheque.getCheckNumber().trim().length() > 0) {
            cheque.setReleased(false);
            cheque.setCleared(false);
            cheque.setPrinted(true);
            CheckVoucherCheque newCh = chequeRepo.save(cheque);
            if (newCh != null) {
                response.setSuccess(true);
            }
        } else { // when removing
            response.setSuccess(true);
        }

        return response;
    }

    @Override
    public CheckDto findForPrintCheckDetails(Integer transId, Integer bankAccountId) {
        CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(transId);

        if (checkVoucher != null && checkVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId())) {

            Map oldJvMap = this.forLogMapMain(checkVoucher);    // for logging

            CheckVoucherCheque c = chequeRepo.findOneByBankAccountIdAndTransactionId(bankAccountId, transId);
            Employee recommendingApprovedBy = employeeRepo.findOneByAccountNumber(checkVoucher.getRecommendingOfficer().getAccountNo());
            Employee approvedBy = employeeRepo.findOneByAccountNumber(checkVoucher.getApprovingOfficer().getAccountNo());
            CheckConfig config = checkConfigRepo.findOneByBankAccountId(bankAccountId);
            String dateFormat = "MM DD YYYY";

            if(config != null){
                if(config.getDateFormat() != null){
                    dateFormat = config.getDateFormat();
                }
            }

            CheckDto check = new CheckDto();
            check.setCheckNumber(c.getCheckNumber());
            check.setAlphaAmount(CurrencyIntoWords.convert(checkVoucher.getCheckAmount()).toUpperCase().replace("CENTS ", ""));
            check.setDate(new SimpleDateFormat(dateFormat).format(checkVoucher.getVoucherDate()).replace(" ", "&nbsp;"));
            check.setNumericAmount(new DecimalFormat("#,##0.00").format(checkVoucher.getCheckAmount()));
            check.setPayee(checkVoucher.getPayee().getName().toUpperCase());
            check.setSig1(approvedBy.getName().toUpperCase());
            if (checkVoucher.getRecommendingOfficer() != null) {
                check.setSig2(recommendingApprovedBy.getName().toUpperCase());
            }

            // logging
            Map newJvMap = this.forLogMapMain(checkVoucher);
            newJvMap.put("remarks", "Print: " + c.getCheckNumber() + " Amount: " + new DecimalFormat("#,##0.00").format(checkVoucher.getCheckAmount()));

            documentLoggerFacade.log(checkVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, newJvMap);

            return check;
        }
        return null;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            CheckVoucher checkVoucher =  cvRepo.findById(postData.getDocumentId()).orElse(null);

            if (checkVoucher != null && checkVoucher.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

                Boolean continueProcessing = Boolean.TRUE;

                if(checkVoucher.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CHECK_WRITING.getId()){

                    List<CheckVoucherCheque> checkVoucherCheques = this.chequeRepo.findByTransactionId(checkVoucher.getTransaction().getId());

                    if(Checker.collectionIsNotEmpty(checkVoucherCheques)){

                        for (CheckVoucherCheque checkVoucherCheque : checkVoucherCheques){

                            if(checkVoucherCheque.getPrinted()){
                                continueProcessing = Boolean.TRUE;
                            } else {
                                continueProcessing = Boolean.FALSE;
                                break;
                            }

                        }

                    }

                }

                if(continueProcessing){

                    User processedBy = authenticationFacade.getLoggedIn();

                    // for logging
                    Map oldJvMap = this.forLogMapMain(checkVoucher);

                    DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
                    DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

                    // set dynamic property here
                    if (actionMap.getPropSignatureType() != null) {
                        try {
                            ClassHelper.setSignatoryValue(checkVoucher, checkVoucher.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    checkVoucher.setDocumentStatus(afterActionDocumentStatus);
                    checkVoucher.setUpdatedAt(null);
                    checkVoucher = cvRepo.save(checkVoucher);

                    // for logging
                    Map newJvMap = this.forLogMapMain(checkVoucher);
                    newJvMap.put("remarks", postData.getRemarks());

                    if (checkVoucher != null) {
                        documentProcessingFacade.processAction(checkVoucher.getTransaction(), actionMap, null, processedBy);
                        documentLoggerFacade.log(checkVoucher.getTransaction(), authenticationFacade.getLoggedIn(), oldJvMap, newJvMap);

                        response.setSuccessMessage("Document successfully processed");
                        response.setSuccess(true);
                    }

                } else {

                    response.setFailureMessage("Check(s) not yet printed");
                    response.setSuccess(false);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        CheckVoucher checkVoucher = cvRepo.findById(vid).orElse(null);

        if (checkVoucher != null) {
            List<CheckVoucherCheque> cvcs = chequeRepo.findByTransactionId(checkVoucher.getTransaction().getId());
            String checkNos = "";
            if(!Checker.collectionIsEmpty(cvcs)){
                Integer i = 0;
                for(CheckVoucherCheque check : cvcs) {
                    checkNos = checkNos + (i == 0 ? "" : "/") + check.getCheckNumber();
                    i++;
                }
            }

            List<Map> cashflowAccounts = new ArrayList<>();
            List<Object[]> cashFlowDetails = voucherCashflowDetailRepo.findByTransactionGroupByCashFlowItem(checkVoucher.getTransaction().getId());

            if(Checker.collectionIsNotEmpty(cashFlowDetails)) {
                for(Object[] cDetail: cashFlowDetails) {

                    Map accountMap = new HashMap();
                    accountMap.put("account", cDetail[1].toString());
                    accountMap.put("amount", cDetail[2]);

                    cashflowAccounts.add(accountMap);
                }
            }

            DecimalFormat df = new DecimalFormat("#,##0.00");

            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/vouchers/sub_reports/");
            params.put("CASHFLOW_ACCOUNTS", new JRBeanCollectionDataSource(cashflowAccounts));
            params.put("TRANS_ID", checkVoucher.getTransaction().getId());
            params.put("VOUCHER_NO", checkVoucher.getCode());
            params.put("V_DATE", checkVoucher.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(checkVoucher.getCheckAmount()) + "***" + "  (P " + df.format(checkVoucher.getCheckAmount()) + ")***");
            params.put("TOTAL", "P " + df.format(checkVoucher.getCheckAmount()));

            String payee = checkVoucher.getPayee().getName() + " " + checkVoucher.getAdditionalPayeeInfo();
            params.put("SUPPLIER_NAME", payee.trim());
            params.put("SUPPLIER_ADDR", checkVoucher.getPayee().getAddress());
            params.put("REMARKS", checkVoucher.getParticulars().trim());
            params.put("NOTES", checkVoucher.getRemarks());
            params.put("CHECK_NOS", checkNos);
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.CV, checkVoucher);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        CheckVoucher voucher = cvRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public HashMap reportParameters(Integer transId, Integer bankAccountId) {
        HashMap<String, Object> par = new HashMap<String, Object>();
        CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(transId);
        CheckVoucherCheque ch = chequeRepo.findOneByBankAccountIdAndTransactionId(bankAccountId, transId);
        CheckConfig config = checkConfigRepo.findOneByBankAccountId(bankAccountId);

        par.put("DATE", new SimpleDateFormat("MMMM dd, yyyy").format(checkVoucher.getVoucherDate()));
        par.put("PAYEE", checkVoucher.getPayee().getName() + "***");
        par.put("AMOUNT", new DecimalFormat("#,##0.00").format(checkVoucher.getCheckAmount()) + "***");
        par.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(checkVoucher.getCheckAmount()).toUpperCase() + "***");
        par.put("CHECK_NO", ch.getCheckNumber());

        if (config.getWithSigner().equals(1)) {
            String s1 = "<b>" + checkVoucher.getApprovingOfficer().getFullName().toUpperCase() + "</b>";
            par.put("SIGN1", s1);
        }
        return par;
    }

    @Override
    public List<Map> findCvChecks(Integer transId) {
        List<CheckVoucherCheque> cheques = chequeRepo.findByTransactionId(transId);

        List<Map> map = new ArrayList<>();

        if (!Checker.collectionIsEmpty(cheques)) {
            for(CheckVoucherCheque ch:cheques) {
                Map m = new HashMap();

                Map account = new HashMap();
                account.put("id", ch.getAccount().getId());

                m.put("transId", ch.getTransaction().getId());
                m.put("checkNumber", ch.getCheckNumber());
                m.put("account", account);
                m.put("bankAccount", ch.getBankAccount());
                m.put("released", ch.getReleased());
                m.put("cleared", ch.getCleared());
                m.put("printed", ch.getPrinted());

                CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(ch.getTransaction().getId());
                m.put("approved", checkVoucher != null && checkVoucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CHECK_WRITING.getId()));

                map.add(m);
            }
        }

        return map;
    }

    @Override
    public List<Map> findChequeNumbersForCheckReleasing(Integer transId) {
        List<Map> map = new ArrayList<>();
        List<CheckVoucherCheque> cheques = chequeRepo.findByTransactionIdAndReleased(transId, false);

        if (!Checker.collectionIsEmpty(cheques)) {
            for(CheckVoucherCheque cvc: cheques) {

                Map c = new HashMap();
                c.put("code", cvc.getAccount().getCode());
                c.put("title", cvc.getAccount().getTitle());
                c.put("checkNumber", cvc.getCheckNumber());
                c.put("id", cvc.getId());

                ReleasedCheque releasedCheque = releasedCheckRepo.findByCheckVoucherChequeId(cvc.getId());

                if(releasedCheque != null){
                    c.put("idNumber", releasedCheque.getIdNumber());
                    c.put("orNumber", releasedCheque.getOrNumber());
                    c.put("depositSlip", releasedCheque.getDepositSlip());
                    c.put("dateReleased", releasedCheque.getDateReleased());
                    c.put("remarks", releasedCheque.getRemarks());
                    c.put("transId", releasedCheque.getTransaction().getId());
                    c.put("personImage", releasedCheque.getPersonImage());
                }

                map.add(c);
            }
        }

        return map;
    }

    @Override
    public List<CvListDto> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<CheckVoucher> vouchers = cvRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeCvListDto(vouchers);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CvListDto> findByDateRange(String from, String to) {
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

            List<CheckVoucher> vouchers = cvRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(fromDate, toDate, Arrays.asList(ids));
            return this.makeCvListDto(vouchers);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map> findCvForReplenish(String from, String to) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }
            List<Map> map = new ArrayList<>();
            List<Object[]> vouchers = cvRepo.findByDocumentStatusIdAndTransactionIdInAndVoucherDateBetween(fromDate, toDate, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
            if (!Checker.collectionIsEmpty(vouchers)) {
                for(Object[] row: vouchers) {

                    HashMap<String, Object> transaction = new HashMap<>();
                    transaction.put("id", row[1]);

                    Map cv = new HashMap();
                    cv.put("id", row[0]);
                    cv.put("transaction", transaction);
                    cv.put("checkAmount", row[2]);
                    cv.put("particulars", row[3]);
                    cv.put("code", row[4]);
                    cv.put("voucherDate", row[5]);
                    cv.put("payee", row[6]);
                    cv.put("accountNo", row[7]);

                    map.add(cv);
                }
            }
            return map;
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public PostResponse updateEntries(CheckVoucher cv, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(cv.getId());

            if(insertMode){

                CheckVoucher existingCv = this.cvRepo.findById(cv.getId()).orElse(null);

                if(existingCv.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingCv.setAmount(cv.getAmount());

                    this.model = cvRepo.save(existingCv);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), cv.getGeneralLedgerLines(), cv.getSubLedgerLines(), this.model.getVoucherDate());

                        response.setSuccessMessage("Entries successfully saved!");
                        response.setSuccess(true);

                    }

                } else {

                    response.setFailureMessage("Invalid user or update is restricted!!");
                    response.setSuccess(false);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public List<Map> findReleasedChecks(String from, String to, String searchText) {

        List<Map> map = new ArrayList<>();

        try {

            String search = (searchText.equalsIgnoreCase("null")) ? null : searchText;

            List<Object[]> releasedChecks;

            if(Checker.isStringNullOrEmpty(search)){
                releasedChecks = cvRepo.findAllForReleasedCheck();
            } else {
                releasedChecks = cvRepo.findAllForReleasedCheckBySupplier("%" + search + "%");
            }

            if (!Checker.collectionIsEmpty(releasedChecks)) {
                for(Object[] row: releasedChecks) {

                    Map releasedCheck = new HashMap();

                    releasedCheck.put("checkVoucherId", row[0]);
                    releasedCheck.put("checkVoucherChequeId", row[1]);
                    releasedCheck.put("code", row[2]);
                    releasedCheck.put("voucherDate", row[3]);
                    releasedCheck.put("checkAmount", row[4]);
                    releasedCheck.put("checkNumber", row[5]);
                    releasedCheck.put("dateReleased", row[6]);
                    releasedCheck.put("releasedBy", row[7]);
                    releasedCheck.put("releasedTo", row[8]);
                    releasedCheck.put("status", row[9]);

                    map.add(releasedCheck);

                }
            }

            return map;

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return map;

    }

    @Override
    public Map findCheckById(Integer id) {

        Map checkMap = new HashMap();

        try {

            CheckVoucherCheque checkVoucherCheque = checkVoucherChequeRepo.findById(id).orElse(null);

            if(Checker.isValidId(checkVoucherCheque.getId())){

                checkMap.put("id", checkVoucherCheque.getId());
                checkMap.put("transaction", checkVoucherCheque.getTransaction());
                checkMap.put("account", checkVoucherCheque.getAccount());
                checkMap.put("checkNumber", checkVoucherCheque.getCheckNumber());
                checkMap.put("released", checkVoucherCheque.getReleased());
                checkMap.put("cleared", checkVoucherCheque.getCleared());
                checkMap.put("printed", checkVoucherCheque.getPrinted());

                CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(checkVoucherCheque.getTransaction().getId());

                checkMap.put("code", checkVoucher.getCode());
                checkMap.put("payee", checkVoucher.getPayee().getName());
                checkMap.put("checkAmount", checkVoucher.getCheckAmount());
                checkMap.put("voucherDate", checkVoucher.getVoucherDate());

                ReleasedCheque releasedCheque = releasedCheckRepo.findByCheckVoucherChequeId(checkVoucherCheque.getId());

                if(releasedCheque != null){
                    checkMap.put("transId", releasedCheque.getTransaction().getId());
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return checkMap;

    }

    @Override
    public Map getNextCheckNumber(Integer bankAccountId) {

        Map map = new HashMap();

        try {

            CheckVoucherCheque checkVoucherCheque = checkVoucherChequeRepo.findFirstByBankAccountIdOrderByIdDesc(bankAccountId);

            if(checkVoucherCheque != null){

                if(StringFormatter.numbersOnly(checkVoucherCheque.getCheckNumber())){

                    String checkNumber = "";

                    checkNumber = String.format("%010d", Integer.valueOf(checkVoucherCheque.getCheckNumber()) + 1);

                    map.put("checkNumber", checkNumber);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return map;

    }

    @Override
    public List<CheckVoucherBudgetDetail> findAllByCheckVoucherId(Integer cvId) {
        return checkVoucherBudgetDetailRepo.findAllByCheckVoucherId(cvId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostResponse additionalBudgetLineItemDetail(
            CheckVoucherAdditionalBudgetLineItemDetailDto detailDto,
            BindingResult bindingResult,
            MessageSource messageSource) {

        PostResponse response = new PostResponse();

        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.add(messageSource.getMessage(error, LocaleContextHolder.getLocale()));
            }
            response.setFailureMessage(String.join(", ", errors));
            response.setSuccess(false);
            return response;
        }

        // Require at least one selection; both being absent is not a valid save
        if (detailDto.getBudgetDetailId() == null && detailDto.getBudgetLineItemDetailId() == null) {
            response.setSuccess(false);
            response.setFailureMessage("Please select either a Cash Flow Item or a Budget Line Item.");
            return response;
        }

        try {
            CheckVoucher checkVoucher = this.cvRepo.findOneByTransactionId(detailDto.getTransactionId());

            if (checkVoucher == null) {
                response.setSuccess(false);
                response.setFailureMessage("Check voucher not found.");
                return response;
            }

            BudgetDetail budgetDetail = null;
            if (detailDto.getBudgetDetailId() != null) {
                budgetDetail = this.budgetDetailRepo.findById(detailDto.getBudgetDetailId()).orElse(null);
                if (budgetDetail == null) {
                    response.setSuccess(false);
                    response.setFailureMessage("Budget detail not found.");
                    return response;
                }
            }

            BudgetLineItemDetail budgetLineItemDetail = null;
            if (detailDto.getBudgetLineItemDetailId() != null) {
                budgetLineItemDetail = this.budgetLineItemDetailRepo.findById(detailDto.getBudgetLineItemDetailId()).orElse(null);
                if (budgetLineItemDetail == null) {
                    response.setSuccess(false);
                    response.setFailureMessage("Budget line item detail not found.");
                    return response;
                }
            }

            checkVoucher.setBudgetLineItemDetail(budgetLineItemDetail);
            checkVoucher.setBudgetDetail(budgetDetail);
            this.cvRepo.save(checkVoucher);

            response.setSuccessMessage("Additional budget line item detail successfully saved!");
            response.setSuccess(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.CV);
    }

    @Override
    public List<Map> findCheckVouchersForReleasing() {
        List<Map> map = new ArrayList<>();
        List<Object[]> checkVouchers = cvRepo.findAllForCheckReleasing();
        if (!Checker.collectionIsEmpty(checkVouchers)) {
            for(Object[] row: checkVouchers) {

                Map cv = new HashMap();
                cv.put("id", row[0]);
                cv.put("transId", row[1]);
                cv.put("checkAmount", row[2]);
                cv.put("particulars", row[3]);
                cv.put("code", row[4]);
                cv.put("voucherDate", row[5]);
                cv.put("payee", row[6]);
                cv.put("remarks", row[7]);
                cv.put("CheckVoucherChequeId", row[8]);

                map.add(cv);
            }
        }

        return map;
    }

    @Override
    public void fillPdf(Integer transId, Integer payeeAccountNo, String token, HttpServletResponse response) {
        HashMap<String, Object> par = new HashMap<String, Object>();
        try {
            Organization organization = organizationRepo.findFirstBy();

            CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(transId);

            CheckVoucherApv apv = checkVoucherApvRepo.findByCheckVoucherId(checkVoucher.getId());
            boolean hasApvTax = false;
            if(apv != null){
                List<CheckVoucherIncomePayment> incomePayments = incomePaymentRepo.findByTransactionId(apv.getAccountsPayableVoucher().getTransaction().getId());
                hasApvTax = !incomePayments.isEmpty();

            }

            String payorSignatory = "";
            String payorSignatoryPos = "";
            String defaultATCCode = "";
            String defaultATCDescription = "";

            try {
                Map defaultAtcMap = settingFacade.getByCode(SettingCode.IEMOP_BILLING_ATC.name());
                if (defaultAtcMap != null) {

                    TaxCode taxCode = taxCodeRepo.findById((Integer) defaultAtcMap.get("id")).orElse(null);

                    if(taxCode != null){
                        defaultATCCode = taxCode.getCode();
                        defaultATCDescription = taxCode.getDescription();
                    }

                }

                Employee approvedBy = this.employeeRepo.findOneByAccountNumber(hasApvTax ? apv.getAccountsPayableVoucher().getApprovingOfficer().getAccountNo() : checkVoucher.getApprovingOfficer().getAccountNo());

                payorSignatory = approvedBy.getName().toUpperCase();
                payorSignatoryPos = approvedBy.getPosition().getName() +" (TIN:"+approvedBy.getTin()+")";

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (checkVoucher == null) return;

            Calendar myCal = new GregorianCalendar();
            myCal.setTime(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate(): checkVoucher.getVoucherDate());

            int firstDate = myCal.getActualMinimum(Calendar.DATE);
            myCal.set(Calendar.DATE, firstDate);
            par.put("DATE_FROM_DD", new SimpleDateFormat("dd").format(myCal.getTime()));
            par.put("DATE_FROM_MM", new SimpleDateFormat("MM").format(myCal.getTime()));
            par.put("DATE_FROM_YYYY", new SimpleDateFormat("yyyy").format(myCal.getTime()));

            int lastDate =  myCal.getActualMaximum(Calendar.DATE);
            myCal.set(Calendar.DATE, lastDate);
            par.put("DATE_TO_DD",  new SimpleDateFormat("dd").format(myCal.getTime()));
            par.put("DATE_TO_MM",  new SimpleDateFormat("MM").format(myCal.getTime()));
            par.put("DATE_TO_YYYY",  new SimpleDateFormat("yyyy").format(myCal.getTime()));

            par.put("PAYOR_NAME", organization != null ? organization.getName():"NEGROS ORIENTAL II ELECTRIC COOPERATIVE, INC");
            par.put("PAYOR_REG_ADDRESS", organization != null ? organization.getAddress():"Real St, Dumaguete City, Negros Oriental, Philippines");
            par.put("PAYOR_REG_ZIP", organization != null ? organization.getZipCode():"6200");

            if(organization.getTin() != null) {

                String[] orgTinArr = StringFormatter.breakTIN(organization.getTin());

                if (orgTinArr.length == 4){
                    par.put("PAYOR_TIN1", Objects.equals(orgTinArr[0], "") ? "" : " " + orgTinArr[0] + "           ");
                    par.put("PAYOR_TIN2", Objects.equals(orgTinArr[1], "") ? "" : " " + orgTinArr[1] + "           ");
                    par.put("PAYOR_TIN3", Objects.equals(orgTinArr[2], "") ? "" : " " + orgTinArr[2] + "           ");
                    par.put("PAYOR_TIN4", Objects.equals(orgTinArr[3], "") ? "" : " " + orgTinArr[3]);
                } else {
                    par.put("PAYOR_TIN1", "");
                    par.put("PAYOR_TIN2", "");
                    par.put("PAYOR_TIN3", "");
                    par.put("PAYOR_TIN4", "");
                }

            }

            Supplier supplier = supplierRepo.findOneByAccountNumber(payeeAccountNo);
            if (supplier == null) {
                Employee employee = employeeRepo.findOneByAccountNumber(payeeAccountNo);
                if (employee != null) {
                    String[] tinArr = StringFormatter.breakTIN(employee.getTin());
                    if (tinArr.length == 4) {
                        par.put("PAYEE_TIN1", Objects.equals(tinArr[0], "") ? "" : tinArr[0] + "            ");
                        par.put("PAYEE_TIN2", Objects.equals(tinArr[1], "") ? "" : tinArr[1] + "            ");
                        par.put("PAYEE_TIN3", Objects.equals(tinArr[2], "") ? "" : tinArr[2] + "            ");
                        par.put("PAYEE_TIN4", Objects.equals(tinArr[3], "") ? "" : tinArr[3]);
                    } else {
                        par.put("PAYEE_TIN1", "");
                        par.put("PAYEE_TIN2", "");
                        par.put("PAYEE_TIN3", "");
                        par.put("PAYEE_TIN4", "");
                    }

                    par.put("PAYEE_NAME", employee.getName());

                    par.put("PAYEE_REG_ADDRESS", employee.getAddress("R"));
                    par.put("PAYEE_REG_ZIP", employee.getZipCode());
                    par.put("PAYEE_FOR_ADDRESS", employee.getAddress("F"));
                    par.put("PAYEE_FOR_ZIP", "");

                }
            } else {

                String[] tinArr = StringFormatter.breakTIN(supplier.getTin());

                if (tinArr.length == 4){
                    par.put("PAYEE_TIN1", Objects.equals(tinArr[0], "") ? "" : tinArr[0] + "            ");
                    par.put("PAYEE_TIN2", Objects.equals(tinArr[1], "") ? "" : tinArr[1] + "            ");
                    par.put("PAYEE_TIN3", Objects.equals(tinArr[2], "") ? "" : tinArr[2] + "            ");
                    par.put("PAYEE_TIN4", Objects.equals(tinArr[3], "") ? "" : tinArr[3]);
                } else {
                    par.put("PAYEE_TIN1", "");
                    par.put("PAYEE_TIN2", "");
                    par.put("PAYEE_TIN3", "");
                    par.put("PAYEE_TIN4", "");
                }

                par.put("PAYEE_NAME", supplier.getName());
                par.put("PAYEE_REG_ADDRESS", supplier.getAddress());
                par.put("PAYEE_REG_ZIP", supplier.getZip());
                par.put("PAYEE_FOR_ADDRESS", "");
                par.put("PAYEE_FOR_ZIP", "");
            }
            // Create an output byte stream where data will be written
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String template = GlobalConstant.JASPER_BASE_PATH + "/templates/BIRForm2307.pdf";
            String newPdfFilename = "/bir2307-" + (new SimpleDateFormat("MMMddyyyy").format(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate(): checkVoucher.getVoucherDate())) + ".pdf";

            PdfReader.unethicalreading = true;
            PdfReader pdfReader = new PdfReader(template);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

            // write contents here:
            int why = 821;

            BaseFont font = BaseFont.createFont();
            PdfContentByte overContent = pdfStamper.getOverContent(1);
            overContent.saveState();
            overContent.setFontAndSize(font, 10.0f);

            overContent.beginText();
            overContent.moveText(157, why);
            overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_MM").toString(), 1) + "    " + StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_DD").toString(), 1) + "     " + StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_YYYY").toString(), 2));
            overContent.endText();

            overContent.beginText();
            overContent.moveText(405, why);
            overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_MM").toString(), 1) + "    " + StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_DD").toString(), 1) + "     " + StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_YYYY").toString(), 2));
            overContent.endText();

            why -= 31;

            overContent.beginText();
            overContent.moveText(213, why);
            overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN1").toString(), 2) + "        " +
                    StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN2").toString(), 2) + "         " +
                    StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN3").toString(), 2) + "         " +
                            StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN4").toString(), 2));
            overContent.endText();

            why -= 30;

            overContent.beginText();
            overContent.moveText(37, why);
            overContent.showText(par.get("PAYEE_NAME").toString());
            overContent.endText();

            why -= 29;

            overContent.beginText();
            overContent.moveText(37, why);
            overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_REG_ADDRESS")));
            overContent.endText();

            why += 3;

            overContent.beginText();
            overContent.moveText(547, why);
            overContent.showText(StringFormatter.addSpacingBetweenChars(StringFormatter.getValueOrBlank(par.get("PAYEE_REG_ZIP")), 2));
            overContent.endText();

            why -= 32;

            // foreign address
            overContent.beginText();
            overContent.moveText(37, why);
            overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_FOR_ADDRESS")));
            overContent.endText();

            /*overContent.beginText();
            overContent.moveText(541, 802);
            overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_FOR_ZIP")));
            overContent.endText();*/

            why -= 28;
            if(organization.getTin() != null) {
                overContent.beginText();
                overContent.moveText(213, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN1").toString(), 2) + "        " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN2").toString(), 2) + "         " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN3").toString(), 2) + "         " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN4").toString(), 2));
                overContent.endText();
            }

            why -= 29;

            overContent.beginText();
            overContent.moveText(37, why);
            overContent.showText(par.get("PAYOR_NAME").toString());
            overContent.endText();

            why -= 29;

            overContent.beginText();
            overContent.moveText(37, why);
            overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYOR_REG_ADDRESS")));
            overContent.endText();

            why += 3;

            overContent.beginText();
            overContent.moveText(547, why);
            overContent.showText(StringFormatter.addSpacingBetweenChars(StringFormatter.getValueOrBlank(par.get("PAYOR_REG_ZIP")), 2));
            overContent.endText();

            // income payments
            List<CheckVoucherIncomePayment> incomePayments = incomePaymentRepo.findByTransactionId(hasApvTax ? apv.getAccountsPayableVoucher().getTransaction().getId() : transId);
            if(Checker.collectionIsNotEmpty(incomePayments)) {

                why -= 45;

                Font f = new Font(font);
                f.setSize(8.0f);

                BigDecimal grandTotalOverall = BigDecimal.ZERO;
                BigDecimal grandTotalQuarter = BigDecimal.ZERO;

                PdfPTable table = new PdfPTable(7);
                table.setTotalWidth(new float[]{ 250.0f, 60.0f, 110.0f, 115.0f, 115.0f, 113.0f, 135.0f });
                table.setWidthPercentage(58.8f);

                for (CheckVoucherIncomePayment incomePayment: incomePayments) {

                    PdfPCell[] cells = new PdfPCell[7];

                    for(int x = 0; x<7; x++) {
                        PdfPCell cell = new PdfPCell(new Phrase("", f));
                        cell.setLeading(1.0f, 1.4f);
                        cell.setNoWrap(false);
                        cell.setBorder(Rectangle.NO_BORDER);

                        cells[x] = cell;
                    }

                    // total
//                    BigDecimal vatablePurchase = iemopBilling.getVatablePurchases() != null ? iemopBilling.getVatablePurchases() : BigDecimal.ZERO;
//                    BigDecimal zeroRatedEchoZone = iemopBilling.getZeroRatedEcoPurchases() != null ? iemopBilling.getZeroRatedEcoPurchases() : BigDecimal.ZERO;
//                    BigDecimal baseAmount = vatablePurchase.add(zeroRatedEchoZone);
//                    String baseAmountStr = new DecimalFormat("#,##0.00").format(baseAmount);
//
//                    BigDecimal wTaxAmount = iemopBilling.getEwtPurchases() != null ? iemopBilling.getEwtPurchases() : BigDecimal.ZERO;
//                    String wTaxAmountStr = new DecimalFormat("#,##0.00").format(wTaxAmount);
//
//                    String description = defaultATCDescription;
//                    String taxCode = defaultATCCode;

                    // Income Payments Subject to Expanded Withholding Tax
                    String incomePaymentDesc = incomePayment != null ? incomePayment.getTaxCode().getDescription():"";
                    cells[0].setPhrase(new Phrase(incomePaymentDesc, f));

                    // ATC
                    cells[1].setPhrase(new Phrase(incomePayment != null ? incomePayment.getTaxCode().getCode() : "", f));

                    // Total
                    BigDecimal totalAmount = incomePayment != null ? incomePayment.getAmount() : BigDecimal.ZERO;
                    grandTotalOverall = grandTotalOverall.add(totalAmount);
                    String total = new DecimalFormat("#,##0.00").format(totalAmount);

                    //QUARTER
                    String month = new SimpleDateFormat("M").format(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate(): checkVoucher.getVoucherDate());

                    //14710
                    //25811
                    //36912
                    switch (month){
                        case "1":
                        case "4":
                        case "7":
                        case "10":
                            cells[2].setPhrase(new Phrase(total, f));
                            cells[2].setHorizontalAlignment(Element.ALIGN_RIGHT);
                            break;

                        case "2":
                        case "5":
                        case "8":
                        case "11":
                            cells[3].setPhrase(new Phrase(total, f));
                            cells[3].setHorizontalAlignment(Element.ALIGN_RIGHT);
                            break;

                        case "3":
                        case "6":
                        case "9":
                        case "12":
                            cells[4].setPhrase(new Phrase(total, f));
                            cells[4].setHorizontalAlignment(Element.ALIGN_RIGHT);
                            break;
                    }

                    cells[5].setPhrase(new Phrase(total, f));
                    cells[5].setHorizontalAlignment(Element.ALIGN_RIGHT);

                    // Tax Withheld for the Quarter
                    BigDecimal quarterAmount = incomePayment != null ? incomePayment.getAmount() : BigDecimal.ZERO;
                    grandTotalQuarter = grandTotalQuarter.add(quarterAmount);

                    String tax = new DecimalFormat("#,##0.00 ").format(quarterAmount);
                    cells[6].setPhrase(new Phrase(tax, f));
                    cells[6].setHorizontalAlignment(Element.ALIGN_RIGHT);

                    PdfPRow row = new PdfPRow(cells);
                    table.getRows().add(row);

                    table.completeRow();
                }

                ColumnText ct = new ColumnText(overContent);
                ct.setSimpleColumn(800, 300, -184, why, 0, PdfContentByte.ALIGN_LEFT);
                ct.addElement(table);
                ct.go();

                why = 424;

                // grand totals
                overContent.beginText();
                overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, new DecimalFormat("#,##0.00").format(grandTotalOverall), 515, why, 0);
                overContent.endText();

                overContent.beginText();
                overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, new DecimalFormat("#,##0.00").format(grandTotalQuarter), 600, why, 0);
                overContent.endText();
            }

            overContent.beginText();
            overContent.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED), 10.0f);
            overContent.moveText(190, 196);
            overContent.showText(payorSignatory);
            overContent.endText();

            overContent.beginText();
            overContent.setFontAndSize(font, 10.0f);
            overContent.moveText(198, 187);
            overContent.showText(payorSignatoryPos);
            overContent.endText();

            overContent.restoreState();

            pdfStamper.close();
            pdfReader.close();

            // Set our response properties
            response.setHeader("Content-Disposition", "inline; filename="+ newPdfFilename);

            // Set content type
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());

            // Write to response stream
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();

            tokenService.remove(token);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fillPdfMultiple(Integer transId, String token, HttpServletResponse response) {
        HashMap<String, Object> par = new HashMap<String, Object>();
        try {
            Organization organization = organizationRepo.findFirstBy();
            CheckVoucher checkVoucher = cvRepo.findOneByTransactionId(transId);

            if (checkVoucher == null) return;

            CheckVoucherApv apv = checkVoucherApvRepo.findByCheckVoucherId(checkVoucher.getId());
            boolean hasApvTax = false;
            boolean hasApv= false;
            boolean hasApvIemopBillings= false;
            if(apv != null){
                List<CheckVoucherIncomePayment> incomePayments = incomePaymentRepo.findByTransactionId(apv.getAccountsPayableVoucher().getTransaction().getId());
                hasApvTax = !incomePayments.isEmpty();
                hasApv =  true;
                hasApvIemopBillings =  accountsPayableVoucherIEMOPBillingRepo.findAllByAccountsPayableVoucherId(apv.getAccountsPayableVoucher().getId()).size() > 0;

            }

            String payorSignatory = "";
            String payorSignatoryPos = "";
            String defaultATCCode = "";
            String defaultATCDescription = "";

            try {

                Map defaultAtcMap = settingFacade.getByCode(SettingCode.IEMOP_BILLING_ATC.name());
                if (defaultAtcMap != null) {

                    TaxCode taxCode = taxCodeRepo.findById((Integer) defaultAtcMap.get("id")).orElse(null);

                    if(taxCode != null){
                        defaultATCCode = taxCode.getCode();
                        defaultATCDescription = taxCode.getDescription();
                    }

                }

                Employee approvedBy = this.employeeRepo.findOneByAccountNumber(hasApvTax ? apv.getAccountsPayableVoucher().getApprovingOfficer().getAccountNo() : checkVoucher.getApprovingOfficer().getAccountNo());

                payorSignatory = approvedBy.getName() + " / " + approvedBy.getPosition().getName() + " / " + " (TIN:"+ (Checker.isStringNullAndEmpty(approvedBy.getTin()) ? "" : approvedBy.getTin()) +")";
//                payorSignatoryPos = approvedBy.getPosition().getName() +" (TIN:"+approvedBy.getTin()+")";

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (checkVoucher == null) return;

//            Calendar myCal = new GregorianCalendar();
//            myCal.setTime(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate(): checkVoucher.getVoucherDate());
//
//            int firstDate = myCal.getActualMinimum(Calendar.DATE);
//            myCal.set(Calendar.DATE, firstDate);
//            par.put("DATE_FROM_DD", new SimpleDateFormat("dd").format(myCal.getTime()));
//            par.put("DATE_FROM_MM", new SimpleDateFormat("MM").format(myCal.getTime()));
//            par.put("DATE_FROM_YYYY", new SimpleDateFormat("yyyy").format(myCal.getTime()));
//
//            int lastDate =  myCal.getActualMaximum(Calendar.DATE);
//            myCal.set(Calendar.DATE, lastDate);
//            par.put("DATE_TO_DD",  new SimpleDateFormat("dd").format(myCal.getTime()));
//            par.put("DATE_TO_MM",  new SimpleDateFormat("MM").format(myCal.getTime()));
//            par.put("DATE_TO_YYYY",  new SimpleDateFormat("yyyy").format(myCal.getTime()));

            Calendar myCal = new GregorianCalendar();
            myCal.setTime(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate() : checkVoucher.getVoucherDate());

            int year = myCal.get(Calendar.YEAR);
            int months = myCal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

            // Determine quarter start and end months
            int startMonth, endMonth;
            if (months >= 1 && months <= 3) {        // Q1
                startMonth = Calendar.JANUARY;
                endMonth = Calendar.MARCH;
            } else if (months >= 4 && months <= 6) { // Q2
                startMonth = Calendar.APRIL;
                endMonth = Calendar.JUNE;
            } else if (months >= 7 && months <= 9) { // Q3
                startMonth = Calendar.JULY;
                endMonth = Calendar.SEPTEMBER;
            } else {                               // Q4
                startMonth = Calendar.OCTOBER;
                endMonth = Calendar.DECEMBER;
            }

            // DATE_FROM = first day of quarter
            Calendar fromCal = new GregorianCalendar(year, startMonth, 1);
            par.put("DATE_FROM_DD", new SimpleDateFormat("dd").format(fromCal.getTime()));
            par.put("DATE_FROM_MM", new SimpleDateFormat("MM").format(fromCal.getTime()));
            par.put("DATE_FROM_YYYY", new SimpleDateFormat("yyyy").format(fromCal.getTime()));

            // DATE_TO = last day of quarter
            Calendar toCal = new GregorianCalendar(year, endMonth, 1);
            int lastDate = toCal.getActualMaximum(Calendar.DATE);
            toCal.set(Calendar.DATE, lastDate);
            par.put("DATE_TO_DD", new SimpleDateFormat("dd").format(toCal.getTime()));
            par.put("DATE_TO_MM", new SimpleDateFormat("MM").format(toCal.getTime()));
            par.put("DATE_TO_YYYY", new SimpleDateFormat("yyyy").format(toCal.getTime()));

            par.put("PAYOR_NAME", organization != null ? organization.getName() : "ILOILO I ELECTRIC COOPERATIVE, INC");
            par.put("PAYOR_REG_ADDRESS", organization != null ? organization.getAddress() : "PBrgy. Namocon, Tigbauan Iloilo, Philippines");
            par.put("PAYOR_REG_ZIP", organization != null ? organization.getZipCode() : "5021");

            if(organization.getTin() != null) {

                String[] orgTinArr = StringFormatter.breakTIN(organization.getTin());

                if (orgTinArr.length == 4){
                    par.put("PAYOR_TIN1", Objects.equals(orgTinArr[0], "") ? "" : " " + orgTinArr[0] + "           ");
                    par.put("PAYOR_TIN2", Objects.equals(orgTinArr[1], "") ? "" : " " + orgTinArr[1] + "           ");
                    par.put("PAYOR_TIN3", Objects.equals(orgTinArr[2], "") ? "" : " " + orgTinArr[2] + "           ");
                    par.put("PAYOR_TIN4", Objects.equals(orgTinArr[3], "") ? "" : " " + orgTinArr[3]);
                } else {
                    par.put("PAYOR_TIN1", "");
                    par.put("PAYOR_TIN2", "");
                    par.put("PAYOR_TIN3", "");
                    par.put("PAYOR_TIN4", "");
                }

            } else {
                par.put("PAYOR_TIN1", "");
                par.put("PAYOR_TIN2", "");
                par.put("PAYOR_TIN3", "");
                par.put("PAYOR_TIN4", "");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String template = GlobalConstant.JASPER_BASE_PATH + "/templates/BIRForm2307.pdf";
            String newPdfFilename = "/bir2307-test.pdf";

            PdfReader.unethicalreading = true;
            PdfReader pdfReader = new PdfReader(template);
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfCopy pdfCopy = new PdfCopy(document, baos);

            document.open();

            List<IEMOPBilling> iemopBillings = new ArrayList<>();

            if(hasApv & hasApvIemopBillings){
                List<AccountsPayableVoucherIEMOPBilling> apvIemopBillings = accountsPayableVoucherIEMOPBillingRepo.findAllByAccountsPayableVoucherId(apv.getAccountsPayableVoucher().getId());
                for(AccountsPayableVoucherIEMOPBilling apvIb : apvIemopBillings){
                    iemopBillings.add(apvIb.getIemopBilling());
                }

            } else {
                List<CheckVoucherIEMOPBilling> cvIemopBillings = checkVoucherIEMOPBillingRepo.findAllByCheckVoucherId(checkVoucher.getId());
                for(CheckVoucherIEMOPBilling cvIb : cvIemopBillings){
                    iemopBillings.add(cvIb.getIemopBilling());
                }

            }

            int numberOfCopies = iemopBillings.size(); // Set the number of times to repeat the template
            for (int i = 0; i < numberOfCopies; i++) {
//                for (int page = 1; page <= pdfReader.getNumberOfPages(); page++) {
//                    PdfImportedPage importedPage = pdfCopy.getImportedPage(pdfReader, page);
//                    pdfCopy.addPage(importedPage);
//
//                }
                PdfImportedPage importedPage = pdfCopy.getImportedPage(pdfReader, 1);
                pdfCopy.addPage(importedPage);
            }

            document.close();
            pdfReader.close();
            pdfCopy.close();

            // Set our response properties
//            response.setHeader("Content-Disposition", "inline; filename=" + newPdfFilename);
//
//            // Set content type
//            response.setContentType("application/pdf");
//            response.setContentLength(baos.size());

            // Write to response stream
//            OutputStream os = response.getOutputStream();
//            baos.writeTo(os);
//            os.flush();

            InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

            // Create a PdfReader using the InputStream

            PdfReader.unethicalreading = true;
            PdfReader newPDFReader = new PdfReader(inputStream);
            PdfStamper pdfStamper = new PdfStamper(newPDFReader, baos);

            int pageNumber = 1;
            for (IEMOPBilling iemopBilling : iemopBillings) {

                SubSupplier subSupplier = iemopBilling.getSubSupplier();

                if(subSupplier != null){

                    String[] tinArr = StringFormatter.breakTIN(subSupplier.getTin());

                    if (tinArr.length == 4){
                        par.put("PAYEE_TIN1", Objects.equals(tinArr[0], "") ? "" : tinArr[0] + "            ");
                        par.put("PAYEE_TIN2", Objects.equals(tinArr[1], "") ? "" : tinArr[1] + "            ");
                        par.put("PAYEE_TIN3", Objects.equals(tinArr[2], "") ? "" : tinArr[2] + "            ");
                        par.put("PAYEE_TIN4", Objects.equals(tinArr[3], "") ? "" : tinArr[3]);
                    } else if (tinArr.length == 3) {
                        par.put("PAYEE_TIN1", Objects.equals(tinArr[0], "") ? "" : tinArr[0] + "            ");
                        par.put("PAYEE_TIN2", Objects.equals(tinArr[1], "") ? "" : tinArr[1] + "            ");
                        par.put("PAYEE_TIN3", Objects.equals(tinArr[2], "") ? "" : tinArr[2] + "            ");
                        par.put("PAYEE_TIN4", "000");
                    } else {
                        par.put("PAYEE_TIN1", "");
                        par.put("PAYEE_TIN2", "");
                        par.put("PAYEE_TIN3", "");
                        par.put("PAYEE_TIN4", "");
                    }

                    par.put("PAYEE_NAME", subSupplier.getTradeName());
                    par.put("PAYEE_REG_ADDRESS", subSupplier.getAddress());
                    par.put("PAYEE_REG_ZIP", "");
                    par.put("PAYEE_FOR_ADDRESS", "");
                    par.put("PAYEE_FOR_ZIP", "");

                } else {
                    par.put("PAYEE_TIN1", "");
                    par.put("PAYEE_TIN2", "");
                    par.put("PAYEE_TIN3", "");
                    par.put("PAYEE_TIN4", "");

                par.put("PAYEE_NAME","");
                par.put("PAYEE_REG_ADDRESS", "");
                par.put("PAYEE_REG_ZIP", "");
                par.put("PAYEE_FOR_ADDRESS", "");
                par.put("PAYEE_FOR_ZIP", "");
                }

                // Create an output byte stream where data will be written
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                PdfReader.unethicalreading = true;

                // write contents here:
                int why = 821;

                BaseFont font = BaseFont.createFont();
                PdfContentByte overContent = pdfStamper.getOverContent(pageNumber);
                overContent.saveState();
                overContent.setFontAndSize(font, 10.0f);

                overContent.beginText();
                overContent.moveText(157, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_MM").toString(), 1) + "    " + StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_DD").toString(), 1) + "     " + StringFormatter.addSpacingBetweenChars(par.get("DATE_FROM_YYYY").toString(), 2));
                overContent.endText();

                overContent.beginText();
                overContent.moveText(405, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_MM").toString(), 1) + "    " + StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_DD").toString(), 1) + "     " + StringFormatter.addSpacingBetweenChars(par.get("DATE_TO_YYYY").toString(), 2));
                overContent.endText();

                why -= 31;

                overContent.beginText();
                overContent.moveText(213, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN1").toString(), 2) + "        " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN2").toString(), 2) + "         " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN3").toString(), 2) + "         " +
                        StringFormatter.addSpacingBetweenChars(par.get("PAYEE_TIN4").toString(), 2));
                overContent.endText();

                why -= 30;

                overContent.beginText();
                overContent.moveText(37, why);
                overContent.showText(par.get("PAYEE_NAME").toString());
                overContent.endText();

                why -= 29;

                overContent.beginText();
                overContent.moveText(37, why);
                overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_REG_ADDRESS")));
                overContent.endText();

                why += 3;

                overContent.beginText();
                overContent.moveText(547, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(StringFormatter.getValueOrBlank(par.get("PAYEE_REG_ZIP")), 2));
                overContent.endText();

                why -= 32;

                // foreign address
                overContent.beginText();
                overContent.moveText(37, why);
                overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_FOR_ADDRESS")));
                overContent.endText();

            /*overContent.beginText();
            overContent.moveText(541, 802);
            overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYEE_FOR_ZIP")));
            overContent.endText();*/

                why -= 28;
                if(organization.getTin() != null) {
                    overContent.beginText();
                    overContent.moveText(213, why);
                    overContent.showText(StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN1").toString(), 2) + "        " +
                            StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN2").toString(), 2) + "         " +
                            StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN3").toString(), 2) + "         " +
                            StringFormatter.addSpacingBetweenChars(par.get("PAYOR_TIN4").toString(), 2));
                    overContent.endText();
                }

                why -= 29;

                overContent.beginText();
                overContent.moveText(37, why);
                overContent.showText(par.get("PAYOR_NAME").toString());
                overContent.endText();

                why -= 29;

                overContent.beginText();
                overContent.moveText(37, why);
                overContent.showText(StringFormatter.getValueOrBlank(par.get("PAYOR_REG_ADDRESS")));
                overContent.endText();

                why += 3;

                overContent.beginText();
                overContent.moveText(547, why);
                overContent.showText(StringFormatter.addSpacingBetweenChars(StringFormatter.getValueOrBlank(par.get("PAYOR_REG_ZIP")), 2));
                overContent.endText();

                // income payments
                    why -= 45;

                    Font f = new Font(font);
                    f.setSize(8.0f);

                    BigDecimal grandTotalOverall = BigDecimal.ZERO;
                    BigDecimal grandTotalQuarter = BigDecimal.ZERO;

                    PdfPTable table = new PdfPTable(7);
                    table.setTotalWidth(new float[]{ 250.0f, 60.0f, 110.0f, 115.0f, 115.0f, 113.0f, 135.0f });
                    table.setWidthPercentage(58.8f);

                        PdfPCell[] cells = new PdfPCell[7];

                        for(int x = 0; x<7; x++) {
                            PdfPCell cell = new PdfPCell(new Phrase("", f));
                            cell.setLeading(1.0f, 1.4f);
                            cell.setNoWrap(false);
                            cell.setBorder(Rectangle.NO_BORDER);

                            cells[x] = cell;
                        }

                // total
                BigDecimal vatablePurchase = iemopBilling.getVatablePurchases() != null ? iemopBilling.getVatablePurchases() : BigDecimal.ZERO;
                BigDecimal zeroRatedEchoZone = iemopBilling.getZeroRatedEcoPurchases() != null ? iemopBilling.getZeroRatedEcoPurchases() : BigDecimal.ZERO;
                BigDecimal baseAmount = vatablePurchase.add(zeroRatedEchoZone);
                String baseAmountStr = new DecimalFormat("#,##0.00").format(baseAmount);

                BigDecimal wTaxAmount = iemopBilling.getEwtPurchases() != null ? iemopBilling.getEwtPurchases() : BigDecimal.ZERO;
                String wTaxAmountStr = new DecimalFormat("#,##0.00").format(wTaxAmount);

                String description = defaultATCDescription;
                String taxCode = defaultATCCode;

                // Income Payments Subject to Expanded Withholding Tax
                        String incomePaymentDesc = description != null ? description:"";
                        cells[0].setPhrase(new Phrase(incomePaymentDesc, f));

                        // ATC
                        cells[1].setPhrase(new Phrase(taxCode != null ? taxCode : "", f));

                        // Total
                        BigDecimal totalAmount = baseAmount != null ? baseAmount : BigDecimal.ZERO;
                        grandTotalOverall = grandTotalOverall.add(totalAmount);
                        String total = new DecimalFormat("#,##0.00").format(totalAmount);

                        //QUARTER
                        String month = new SimpleDateFormat("M").format(hasApvTax ? apv.getAccountsPayableVoucher().getVoucherDate(): checkVoucher.getVoucherDate());

                        //14710
                        //25811
                        //36912
                        switch (month){
                            case "1":
                            case "4":
                            case "7":
                            case "10":
                                cells[2].setPhrase(new Phrase(total, f));
                                cells[2].setHorizontalAlignment(Element.ALIGN_RIGHT);
                                break;

                            case "2":
                            case "5":
                            case "8":
                            case "11":
                                cells[3].setPhrase(new Phrase(total, f));
                                cells[3].setHorizontalAlignment(Element.ALIGN_RIGHT);
                                break;

                            case "3":
                            case "6":
                            case "9":
                            case "12":
                                cells[4].setPhrase(new Phrase(total, f));
                                cells[4].setHorizontalAlignment(Element.ALIGN_RIGHT);
                                break;

                        }

                        cells[5].setPhrase(new Phrase(total, f));
                        cells[5].setHorizontalAlignment(Element.ALIGN_RIGHT);

                        // Tax Withheld for the Quarter
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        BigDecimal quarterAmount = wTaxAmount != null ? wTaxAmount: BigDecimal.ZERO;
                        String displayQuarterAmount = (quarterAmount.compareTo(BigDecimal.ZERO) == 0) ? "" : df.format(quarterAmount);
                        grandTotalQuarter = grandTotalQuarter.add(quarterAmount);
                        String displayQuarterTotalAmount = (grandTotalQuarter.compareTo(BigDecimal.ZERO) == 0) ? "" : df.format(grandTotalQuarter);

                        cells[6].setPhrase(new Phrase(displayQuarterAmount, f));
                        cells[6].setHorizontalAlignment(Element.ALIGN_RIGHT);

                        PdfPRow row = new PdfPRow(cells);
                        table.getRows().add(row);

                        table.completeRow();

                    ColumnText ct = new ColumnText(overContent);
                    ct.setSimpleColumn(800, 300, -184, why, 0, PdfContentByte.ALIGN_LEFT);
                    ct.addElement(table);
                    ct.go();

                    why = 424;

                    // grand totals
                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, new DecimalFormat("#,##0.00").format(grandTotalOverall), 515, why, 0);
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, displayQuarterTotalAmount, 600, why, 0);
                    overContent.endText();

                overContent.beginText();
                overContent.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED), 10.0f);
                overContent.moveText(120, 196);
                overContent.showText(payorSignatory);
                overContent.endText();

                overContent.beginText();
                overContent.setFontAndSize(font, 10.0f);
                overContent.moveText(198, 187);
                overContent.showText(payorSignatoryPos);
                overContent.endText();

                overContent.restoreState();

                pageNumber++;

            }

            pdfStamper.close();
            newPDFReader.close();

            // Set our response properties
            response.setHeader("Content-Disposition", "inline; filename=" + newPdfFilename);

            // Set content type
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());

            // Write to response stream
            OutputStream newOs = response.getOutputStream();
            baos.writeTo(newOs);
            newOs.flush();

            tokenService.remove(token);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {

        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(fileToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    @Transactional
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
        CheckVoucher voucher = cvRepo.findFirstByOrderByIdAsc();
        if (voucher != null) {
            return documentDtoer.getDocumentStatuses(voucher.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(CheckVoucher cv) {
        return documentLoggerFacade.makeLog(cv);
    }

    private List<CvListDto> makeCvListDto(List<CheckVoucher> vouchers ) {

        List<CvListDto> returnVouchers = new ArrayList<>();

        if (!Checker.collectionIsEmpty(vouchers)) {
            for(CheckVoucher cv : vouchers) {
                CvListDto cvListDto = new CvListDto();
                cvListDto.setPayee(cv.getPayee().getName());
                cvListDto.setId(cv.getId());
                cvListDto.setCheckAmount(cv.getCheckAmount());
                cvListDto.setCode(cv.getCode());
                cvListDto.setParticulars(cv.getParticulars());
                cvListDto.setStatus(cv.getDocumentStatus().getStatus());
                cvListDto.setVoucherDate(cv.getVoucherDate());

                if(cv.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                        && cv.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
                    cvListDto.setEnableCheckBox(Boolean.TRUE);
                    cvListDto.setSelected(Boolean.TRUE);
                } else {
                    cvListDto.setEnableCheckBox(Boolean.FALSE);
                    cvListDto.setSelected(Boolean.FALSE);
                }

                cvListDto.setDocumentCode(com.noreco1.fireflyv2.model.enums.DocumentType.CV.getCode());

                returnVouchers.add(cvListDto);
            }
        }

        return returnVouchers;
    }
}
