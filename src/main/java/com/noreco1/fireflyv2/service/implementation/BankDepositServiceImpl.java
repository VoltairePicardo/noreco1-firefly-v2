package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.BankDepositService;
import com.noreco1.fireflyv2.service.PrintableVoucher;

import com.noreco1.fireflyv2.validator.BankDepositValidator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BankDepositServiceImpl implements BankDepositService, PrintableVoucher {

    private BankDeposit model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    BankDepositRepo bankDepositRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    private TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    private TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    private SettingFacade settingFacade;

    @Autowired
    private BankAccountRepo bankAccountRepo;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Override
    @Transactional
    public PostResponse processUpdate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(bankDeposit, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BankDepositValidator validator = new BankDepositValidator();
        validator.validate(bankDeposit, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            BankDeposit existingBankDeposit = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(new Date()));

            Boolean insertMode = bankDeposit.getId() == null;
            if (insertMode) { // insert mode
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                String offAcro = employee.getOffice().getAcronym();
                Object latestBankDepositCode = bankDepositRepo.findLatestBankDepositCodeByYear(voucherYear, "%-"+offAcro+"-%");
                bankDeposit.setCode(generatorFacade.voucherCode("BD-"+offAcro, (latestBankDepositCode == null ? "" : String.valueOf(latestBankDepositCode)), new Date()));

                bankDeposit.setCreatedBy(createdBy);
                bankDeposit.setTransaction(generatorFacade.transaction());
                bankDeposit.setCreatedAt(new Date());

                existingBankDeposit = bankDeposit;

            } else {
                existingBankDeposit = bankDepositRepo.findById(bankDeposit.getId()).orElse(null);
            }

            // editable fields
            existingBankDeposit.setUpdatedAt(new Date());
            existingBankDeposit.setDepositDate(bankDeposit.getDepositDate());
            existingBankDeposit.setReferenceNumber(bankDeposit.getReferenceNumber());
            existingBankDeposit.setPostingDate(bankDeposit.getPostingDate());
            existingBankDeposit.setCollectionDate(bankDeposit.getCollectionDate());
            existingBankDeposit.setBankAccount(bankDeposit.getBankAccount());
            existingBankDeposit.setCashAmount(bankDeposit.getCashAmount());
            existingBankDeposit.setCheckAmount(bankDeposit.getCheckAmount());
            existingBankDeposit.setFromUpload(true);

            this.model = bankDepositRepo.save(existingBankDeposit);

            if (this.model != null) {
                this.saveToTemporaryBatch(this.model);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Bank deposit successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public PostResponse processUpdate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        PostResponse response = this.processUpdate(bankDeposit, bindingResult, messageSource);

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
    public PostResponse processCreate(BankDeposit bankDeposit, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = this.processCreate(bankDeposit, bindingResult, messageSource);

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
    @Transactional(readOnly = true)
    public List<BankDepositListDto> findAll() {
        List<BankDeposit> vouchers = bankDepositRepo.findAll();

        List<BankDepositListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(BankDeposit deposit : vouchers) {
                BankDepositListDto bankDepositListDto = new BankDepositListDto();
                bankDepositListDto.setId(deposit.getId());
                bankDepositListDto.setTransId(deposit.getTransaction().getId());
                bankDepositListDto.setLocalCode(deposit.getCode());
                bankDepositListDto.setDate(deposit.getCreatedAt());
                bankDepositListDto.setDepositDate(deposit.getDepositDate());
                bankDepositListDto.setReferenceNumber(deposit.getReferenceNumber());
                bankDepositListDto.setPostingDate(deposit.getPostingDate());
                bankDepositListDto.setCollectionDate(deposit.getCollectionDate());
                bankDepositListDto.setBankAccount(deposit.getBankAccount());
                bankDepositListDto.setCashAmount(deposit.getCashAmount());
                bankDepositListDto.setCheckAmount(deposit.getCheckAmount());

                returnVouchers.add(bankDepositListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BankDepositDto findById(Integer id) {

        BankDeposit bankDeposit =  bankDepositRepo.findById(id).orElse(null);
        BankDepositDto bankDepositDto = new BankDepositDto();

        if (bankDeposit != null) {
            bankDepositDto.setId(bankDeposit.getId());
            bankDepositDto.setReferenceNumber(bankDeposit.getReferenceNumber());
            bankDepositDto.setLocalCode(bankDeposit.getCode());
            bankDepositDto.setTransId(bankDeposit.getTransaction().getId());
            bankDepositDto.setCreated(bankDeposit.getCreatedAt());
            bankDepositDto.setLastUpdated(bankDeposit.getUpdatedAt());
            bankDepositDto.setPreparedBy(bankDeposit.getCreatedBy());

            bankDepositDto.setDepositDate(bankDeposit.getDepositDate());
            bankDepositDto.setReferenceNumber(bankDeposit.getReferenceNumber());
            bankDepositDto.setPostingDate(bankDeposit.getPostingDate());
            bankDepositDto.setCollectionDate(bankDeposit.getCollectionDate());
            bankDepositDto.setBankAccount(bankDeposit.getBankAccount());
            bankDepositDto.setCashAmount(bankDeposit.getCashAmount());
            bankDepositDto.setCheckAmount(bankDeposit.getCheckAmount());

        }

        return  bankDepositDto;
    }

    @Override
    public PostResponse uploadDeposits(UploadBankDepositDto uploadBankDepositDto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        int successCount = 0;

        try {

            User createdBy = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
            String offAcro = employee.getOffice().getAcronym();
            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(new Date()));

            for (String bankDepositLine : uploadBankDepositDto.getBankDepositLines()) {
                BankDeposit bankDeposit = new BankDeposit();

                String[] separated = bankDepositLine.split("\\|");

                String strDate = separated[4];
                String strAmount = separated[2];

                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(strDate);

                Object latestBankDepositCode = bankDepositRepo.findLatestBankDepositCodeByYear(voucherYear, "%-"+offAcro+"-%");
                bankDeposit.setCode(generatorFacade.voucherCode("BD-"+offAcro, (latestBankDepositCode == null ? "" : String.valueOf(latestBankDepositCode)), new Date()));
                bankDeposit.setCreatedBy(createdBy);
                bankDeposit.setTransaction(generatorFacade.transaction());

                bankDeposit.setAccountNumber(separated[0]);
                bankDeposit.setDepositDate(date);
                bankDeposit.setReferenceNumber(separated[1]);
                bankDeposit.setCashAmount(new BigDecimal(strAmount));
                bankDeposit.setBankAccount(uploadBankDepositDto.getBankAccount());

                List<BankDeposit> existingBankDeposit = bankDepositRepo.findAllByReferenceNumberAndBankAccountIdAndDepositDateAndCashAmount(bankDeposit.getReferenceNumber(), bankDeposit.getBankAccount().getId(), bankDeposit.getDepositDate(), bankDeposit.getCashAmount());
                if(existingBankDeposit.isEmpty()){
                    bankDepositRepo.save(bankDeposit);
                }
                successCount++;
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    if(successCount > 0){
        response.setModelId(0);
        response.setSuccessMessage(successCount + " Bank deposit successfully uploaded!");
        response.setSuccess(true);
    }

        return response;
    }

    private void saveToTemporaryBatch(BankDeposit bankDeposit) {

        Integer transactionId = bankDeposit.getTransaction().getId();

        // Clean up any existing TemporaryBatch
        TemporaryBatch existing = temporaryBatchRepo.findFirstByTransactionId(transactionId);
        if (existing != null && Checker.isValidId(existing.getId())) {
            temporaryGeneralLedgerRepo.deleteAllByTemporaryBatchId(existing.getId());
            temporaryBatchRepo.deleteById(existing.getId());
        }

        // Create and save new TemporaryBatch
        TemporaryBatch temporaryBatch = new TemporaryBatch();
        temporaryBatch.setDate(DateHelper.getServerDate());
        temporaryBatch.setTransaction(bankDeposit.getTransaction());

        DocumentType docType = new DocumentType();
        docType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.BAD.getId());
        temporaryBatch.setDocumentType(docType);

        temporaryBatch.setVoucherCreated(Boolean.FALSE);
        temporaryBatch.setRemarks("Bank deposit for " + DateHelper.dateToLongDate(bankDeposit.getDepositDate()));

        TemporaryBatch savedBatch = temporaryBatchRepo.save(temporaryBatch);

        if (Checker.isValidId(savedBatch.getId())) {
            saveTemporaryGeneralLedgerEntries(savedBatch, bankDeposit);
        }

    }

    private void saveTemporaryGeneralLedgerEntries(TemporaryBatch batch, BankDeposit deposit) {
        BigDecimal totalAmount = deposit.getCashAmount().add(deposit.getCheckAmount());

        // Save Debit entry
        saveTemporaryGeneralLedger(batch, deposit.getBankAccount().getId(), totalAmount, true);

        // Save Credit entry
        saveTemporaryGeneralLedger(batch, 0, totalAmount, false);
    }

    private void saveTemporaryGeneralLedger(TemporaryBatch batch, Integer bankAccountId, BigDecimal amount, boolean isDebit) {

        TemporaryGeneralLedger entry = new TemporaryGeneralLedger();

        entry.setTransaction(batch.getTransaction());
        entry.setTemporaryBatch(batch);

        if (isDebit) {

            entry.setDebit(amount);
            entry.setCredit(BigDecimal.ZERO);

            BankAccount bankAccount = this.bankAccountRepo.findById(bankAccountId).orElse(null);
            entry.setAccount(bankAccount.getAccount());

        } else {

            entry.setDebit(BigDecimal.ZERO);
            entry.setCredit(amount);

            Map<String, Object> stringObjectMap = this.settingFacade.getByCode("CASH_ON_HAND_ACCOUNT");
            if (stringObjectMap != null) {
                Integer cashOnHandId = (Integer) stringObjectMap.get("id");
                entry.setAccount(Account.builder().id(cashOnHandId).build());
            }

        }

        temporaryGeneralLedgerRepo.save(entry);

    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        BankDeposit bankDeposit = bankDepositRepo.findById(vid).orElse(null);
        if (bankDeposit != null) {
            params.put("TRANS_ID", bankDeposit.getTransaction().getId());
            params.put("VOUCHER_NO", bankDeposit.getCode());
            params.put("V_DATE", bankDeposit.getDepositDate());
            params.put("EXPLANATION", "");

            User preparedBy = bankDeposit.getCreatedBy();
            if (preparedBy != null) {
                Employee emp = employeeRepo.findOneByAccountNumber(preparedBy.getAccountNo());
                String name = emp != null ? emp.getName() : preparedBy.getUsername();
                String position = emp != null && emp.getPosition() != null ? emp.getPosition().getName() : "";
                params.put("PREPARAR", name);
                params.put("PREPARAR_POS", position);
            } else {
                params.put("PREPARAR", "");
                params.put("PREPARAR_POS", "");
            }
            params.put("CHECKER", "");
            params.put("CHECKER_POS", "");
            params.put("APPROVAR", "");
            params.put("APPROVAR_POS", "");
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        BankDeposit bankDeposit = bankDepositRepo.findById(vid).orElse(null);
        if (bankDeposit != null) {
            return new JRBeanCollectionDataSource(
                ledgerDtoers.getVoucherLedgerLines(bankDeposit.getTransaction().getId())
            );
        }
        return new JRBeanCollectionDataSource(Collections.emptyList());
    }

}
