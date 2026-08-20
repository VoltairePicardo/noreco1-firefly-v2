package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.BankReconListDto;
import com.noreco1.fireflyv2.controller.response.OtherDepositDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.BankReconService;
import com.noreco1.fireflyv2.validator.BankReconValidator;
import com.noreco1.fireflyv2.validator.OdValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 6/30/2015.
 */
@Service(value = "brServiceImpl")
public class BankReconServiceImpl implements BankReconService {

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    BankReconciliationRepo bankReconciliationRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    CheckVoucherChequeRepo chequeRepo;

    @Autowired
    OtherDepositRepo otherDepositRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    AccountRepo accountRepo;

    @Override
    @Transactional
    public PostResponse processUpdate(Object v, BindingResult bindingResult, MessageSource messageSource) {
        BankReconciliation br = (BankReconciliation)v;
        return this.processCreate(br, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Object v, BindingResult bindingResult, MessageSource messageSource) {
        OtherDeposit od = (OtherDeposit) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        OdValidator validator = new OdValidator();
        validator.setService(this);
        validator.validate(od, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            OtherDeposit existingOd = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(od.getVoucherDate()));

            Boolean insertMode = od.getId() == null;
            if (insertMode) { // insert mode
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                od.setOffice(employee.getOffice());
                String offAcro = employee.getOffice().getAcronym();
                Object latestOdCode = otherDepositRepo.findLatestOdCodeByYear(voucherYear, "%-"+offAcro+"-%");
                od.setCode(generatorFacade.voucherCode("OD-"+offAcro, (latestOdCode == null ? "" : String.valueOf(latestOdCode)), od.getVoucherDate()));
                od.setTransaction(generatorFacade.transaction());
                od.setCreatedBy(createdBy);
                od.setCleared(false);
                od.setCreatedAt(new Date());
                existingOd = od;
            } else {
                existingOd = otherDepositRepo.findById(od.getId()).orElse(null);
            }
            existingOd.setUpdatedAt(new Date());
            existingOd.setVoucherDate(od.getVoucherDate());
            existingOd.setCheckNumber(od.getCheckNumber());
            existingOd.setAmount(od.getAmount());
            existingOd.setYear(voucherYear);
            existingOd.setParticulars(od.getParticulars());
            existingOd.setAccount(od.getAccount());

            OtherDeposit newOd = otherDepositRepo.save(existingOd);

            if (newOd != null) {
                response.setModelId(newOd.getId());
                response.setSuccessMessage("Other Deposit successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse process(Object v, BindingResult bindingResult, MessageSource messageSource) {
        BankReconciliation br = (BankReconciliation)v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        BankReconValidator validator = new BankReconValidator();
        validator.setService(this);
        validator.validate(br, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            BankReconciliation existingBr = null;
            CheckVoucherCheque cheque = null;
            OtherDeposit od = null;
//            BankDepositDetail bdd = null;
            if(br.getType() == BankReconType.RC.getId()) {
                cheque = chequeRepo.findById(br.getDocumentId()).orElse(null);
                br.setTransaction(cheque.getTransaction());
            } else if(br.getType() == BankReconType.OD.getId()){
                od = otherDepositRepo.findById(br.getDocumentId()).orElse(null);
                br.setTransaction(od.getTransaction());
            }

            /*else if(br.getType() == BankReconType.DT.getId()){
                bdd = bankDepositDetailRepo.findById(br.getDocumentId()).orElse(null);
                br.setTransaction(bdd.getBankDepositId().getTransaction());
            }*/

            Boolean insertMode = br.getId() == null;
            if (insertMode) { // insert mode
                br.setCreatedBy(createdBy);
                existingBr = br;
            } else {
                existingBr = bankReconciliationRepo.findById(br.getId()).orElse(null);
            }
            existingBr.setType(br.getType());
            existingBr.setCleared(br.getCleared());

            BankReconciliation newBr = bankReconciliationRepo.save(existingBr);

            if (newBr != null) {
                if(br.getType() == BankReconType.RC.getId()) {
                    cheque.setCleared(newBr.getCleared());
                    chequeRepo.save(cheque);
                } else if(br.getType() == BankReconType.OD.getId()){
                    od.setCleared(newBr.getCleared());
                    otherDepositRepo.save(od);
                }
                /*else if(br.getType() == BankReconType.DT.getId()){
                    bdd.setCleared(newBr.getCleared());
                    bankDepositDetailRepo.updateClearedById(br.getDocumentId(), bdd.getCleared());
                }*/

                response.setModelId(newBr.getId());
                response.setSuccessMessage("BR successfully processed!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconListDto> findAll() {
        List<BankReconListDto> returnVouchers = new ArrayList<>();
        List<Object[]> chequeList = chequeRepo.findByReleasedWithCheckVoucherAndAmount(true);
        List<Object[]> otherDepositList = otherDepositRepo.findOtherDeposits();
//        List<Object[]> bankDepositList = bankDepositRepo.findBankDepositsByStatusId(DocumentStatus.APPROVED.getId());

        if (!Checker.collectionIsEmpty(chequeList) || !Checker.collectionIsEmpty(otherDepositList)) {
            for(Object[] line : chequeList) {
                BankReconListDto bankReconListDto = new BankReconListDto();
                bankReconListDto.setDocumentId((Integer) line[0]);
                bankReconListDto.setCheckNo(line[1].toString());
                bankReconListDto.setAmount(new BigDecimal(line[2].toString()));
                bankReconListDto.setLocalCode(line[3].toString());
                bankReconListDto.setVoucherDate((Date) line[4]);
                bankReconListDto.setParticulars(line[5].toString());
                bankReconListDto.setAccount(line[6].toString());
                bankReconListDto.setStatus(line[7].equals(true) ? "Cleared" : "Not Cleared");
                bankReconListDto.setStatusId(line[7].equals(true) ? 1 : 0);
                bankReconListDto.setType(BankReconType.RC.getCode());
                bankReconListDto.setTypeId(BankReconType.RC.getId());
                bankReconListDto.setTransId((Integer) line[8]);
                BankReconciliation bankRecon = bankReconciliationRepo.findOneByTransactionIdAndDocumentId(bankReconListDto.getTransId(), bankReconListDto.getDocumentId());
                if(bankRecon != null){
                    bankReconListDto.setId(bankRecon.getId());
                }
                bankReconListDto.setSegmentBankAccountId((Integer) line[9]);
                bankReconListDto.setAccountId((Integer) line[10]);

                returnVouchers.add(bankReconListDto);
            }

            for(Object[] line : otherDepositList) {
                BankReconListDto bankReconListDto = new BankReconListDto();
                bankReconListDto.setDocumentId((Integer) line[0]);
                bankReconListDto.setCheckNo(line[1].toString());
                bankReconListDto.setAmount(new BigDecimal(line[2].toString()));
                bankReconListDto.setLocalCode(line[3].toString());
                bankReconListDto.setVoucherDate((Date) line[4]);
                bankReconListDto.setParticulars(line[5].toString());
                bankReconListDto.setAccount(line[6].toString());
                bankReconListDto.setStatus(line[7].equals(true) ? "Cleared" : "Not Cleared");
                bankReconListDto.setStatusId(line[7].equals(true) ? 1 : 0);
                bankReconListDto.setType(BankReconType.OD.getCode());
                bankReconListDto.setTypeId(BankReconType.OD.getId());
                bankReconListDto.setTransId((Integer) line[8]);
                BankReconciliation bankRecon = bankReconciliationRepo.findOneByTransactionIdAndDocumentId(bankReconListDto.getTransId(), bankReconListDto.getDocumentId());
                if(bankRecon != null){
                    bankReconListDto.setId(bankRecon.getId());
                }
                bankReconListDto.setAccountId((Integer) line[9]);

                returnVouchers.add(bankReconListDto);
            }

            /*for(Object[] line : bankDepositList) {
                BankReconListDto bankReconListDto = new BankReconListDto();
                bankReconListDto.setDocumentId((Integer) line[0]);
                bankReconListDto.setCheckNo(line[1].toString());
                bankReconListDto.setAmount(new BigDecimal(line[2].toString()));
                bankReconListDto.setLocalCode(line[3].toString());
                bankReconListDto.setVoucherDate((Date) line[4]);
                bankReconListDto.setParticulars(line[5].toString());
                bankReconListDto.setAccount(line[6].toString());
                bankReconListDto.setStatus(line[7].equals(true) ? "Reflected" : "Not Reflected");
                bankReconListDto.setStatusId(line[7].equals(true) ? 1 : 0);
                bankReconListDto.setType(BankReconType.DT.getCode());
                bankReconListDto.setTypeId(BankReconType.DT.getId());
                bankReconListDto.setTransId((Integer) line[8]);
                BankReconciliation bankRecon = bankReconciliationRepo.findOneByTransactionIdAndDocumentId(bankReconListDto.getTransId(), bankReconListDto.getDocumentId());
                if(bankRecon != null){
                    bankReconListDto.setId(bankRecon.getId());
                }
                bankReconListDto.setSegmentBankAccountId((Integer) line[9]);
                bankReconListDto.setAccountId((Integer) line[10]);

                returnVouchers.add(bankReconListDto);
            }*/
        }
        return returnVouchers;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public OtherDepositDto findByOdId(Integer odId) {
        OtherDeposit otherDeposit =  otherDepositRepo.findById(odId).orElse(null);
        OtherDepositDto odDto = new OtherDepositDto();

        if (otherDeposit != null) {
            odDto.setId(otherDeposit.getId());
            odDto.setParticulars(otherDeposit.getParticulars());
            odDto.setLocalCode(otherDeposit.getCode());
            odDto.setTransId(otherDeposit.getTransaction().getId());

            Account account = accountRepo.findById(otherDeposit.getAccount().getId()).orElse(null);

            odDto.setAccount(account);
            odDto.setVoucherDate(otherDeposit.getVoucherDate());
            odDto.setCreated(otherDeposit.getCreatedAt());
            odDto.setLastUpdated(otherDeposit.getUpdatedAt());
            odDto.setAmount(otherDeposit.getAmount());
        }

        return  odDto;
    }
}
