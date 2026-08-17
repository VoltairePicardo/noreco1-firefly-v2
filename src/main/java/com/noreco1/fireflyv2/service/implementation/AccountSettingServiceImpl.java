package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.AccountSettingDetailDto;
import com.noreco1.fireflyv2.controller.response.ApvPurchasingDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AccountSettingService;
import com.noreco1.fireflyv2.validator.AccountSettingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountSettingServiceImpl implements AccountSettingService {

    private AccountSetting model;

    @Autowired
    private AccountSettingRepo accountSettingRepo;

    @Autowired
    private StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    private ItemStockDetailRepo itemStockDetailRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private ReceivingReportRepo receivingReportRepo;

    @Override
    public List<AccountSetting> findByDateRangeAndStatusId(String from, String to) {

        List<AccountSetting> accountSettings = new ArrayList<>();

        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            accountSettings = accountSettingRepo.findAllByDateBetweenAndCreatedByIdOrderByDate(
                    fromDate,
                    toDate,
                    authenticationFacade.getLoggedIn().getId());

            if(Checker.collectionIsNotEmpty(accountSettings)){

                for (AccountSetting accountSetting : accountSettings){

                    String code = "";

                    if(accountSetting.getReceivingReport() != null){
                        code = accountSetting.getReceivingReport().getCode();
                    }

                    if(accountSetting.getStockReceive() != null){
                        code = accountSetting.getStockReceive().getCode();
                    }

                    if(accountSetting.getMaterialCreditTicket() != null){
                        code = accountSetting.getMaterialCreditTicket().getCode();
                    }

                    if(accountSetting.getStockRelease() != null){
                        code = accountSetting.getStockRelease().getCode();
                    }

                    if(accountSetting.getMaterialSalvageTicket() != null){
                        code = accountSetting.getMaterialSalvageTicket().getCode();
                    }

                    if(accountSetting.getStockAdjustment() != null){
                        code = accountSetting.getStockAdjustment().getCode();
                    }

                    accountSetting.setCode(code);

                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return accountSettings;

    }

    @Override
    public AccountSetting findById(Integer id) {

        AccountSetting accountSetting = accountSettingRepo.findById(id).orElse(null);

        try {

            if(accountSetting != null){

                Integer transactionId = null;

                if(accountSetting.getReceivingReport() != null){

                    transactionId = accountSetting.getReceivingReport().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getReceivingReport().getId());
                    detail.put("voucherDate", accountSetting.getReceivingReport().getDeliveryDate());
                    detail.put("localCode", accountSetting.getReceivingReport().getCode());
                    detail.put("particulars", accountSetting.getReceivingReport().getRemarks());
                    detail.put("netAmount", accountSetting.getReceivingReport().getTotalAmount());
                    detail.put("quantity", accountSetting.getReceivingReport().getTotalQuantity());

                    accountSetting.setDocumentDetail(detail);

                }

                if(accountSetting.getStockReceive() != null){

                    transactionId = accountSetting.getStockReceive().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getStockReceive().getId());
                    detail.put("voucherDate", accountSetting.getStockReceive().getVoucherDate());
                    detail.put("localCode", accountSetting.getStockReceive().getCode());
                    detail.put("particulars", accountSetting.getStockReceive().getDescription());
                    detail.put("netAmount", BigDecimal.ZERO);
                    detail.put("quantity", this.getTotalQuantity(transactionId));

                    accountSetting.setDocumentDetail(detail);

                }

                if(accountSetting.getMaterialCreditTicket() != null){

                    transactionId = accountSetting.getMaterialCreditTicket().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getMaterialCreditTicket().getId());
                    detail.put("voucherDate", accountSetting.getMaterialCreditTicket().getVoucherDate());
                    detail.put("localCode", accountSetting.getMaterialCreditTicket().getCode());
                    detail.put("particulars", accountSetting.getMaterialCreditTicket().getRemarks());
                    detail.put("netAmount", BigDecimal.ZERO);
                    detail.put("quantity", this.getTotalQuantity(transactionId));

                    accountSetting.setDocumentDetail(detail);

                }

                if(accountSetting.getStockRelease() != null){

                    transactionId = accountSetting.getStockRelease().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getStockRelease().getId());
                    detail.put("voucherDate", accountSetting.getStockRelease().getVoucherDate());
                    detail.put("localCode", accountSetting.getStockRelease().getCode());
                    detail.put("particulars", accountSetting.getStockRelease().getDescription());
                    detail.put("netAmount", BigDecimal.ZERO);
                    detail.put("quantity", this.getTotalQuantity(transactionId));

                    accountSetting.setDocumentDetail(detail);

                }

                if(accountSetting.getMaterialSalvageTicket() != null){

                    transactionId = accountSetting.getMaterialSalvageTicket().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getMaterialSalvageTicket().getId());
                    detail.put("voucherDate", accountSetting.getMaterialSalvageTicket().getVoucherDate());
                    detail.put("localCode", accountSetting.getMaterialSalvageTicket().getCode());
                    detail.put("particulars", accountSetting.getMaterialSalvageTicket().getPurpose());
                    detail.put("netAmount", BigDecimal.ZERO);
                    detail.put("quantity", this.getTotalQuantity(transactionId));

                    accountSetting.setDocumentDetail(detail);

                }

                if(accountSetting.getStockAdjustment() != null){

                    transactionId = accountSetting.getStockAdjustment().getTransaction().getId();

                    Map detail = new HashMap();

                    detail.put("id", accountSetting.getStockAdjustment().getId());
                    detail.put("voucherDate", accountSetting.getStockAdjustment().getVoucherDate());
                    detail.put("localCode", accountSetting.getStockAdjustment().getCode());
                    detail.put("particulars", accountSetting.getStockAdjustment().getRemarks());
                    detail.put("netAmount", BigDecimal.ZERO);
                    detail.put("quantity", this.getTotalQuantity(transactionId));

                    accountSetting.setDocumentDetail(detail);

                }

                ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(transactionId);

                if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                    ArrayList<AccountSettingDetailDto> accountSettingDetailDtos = new ArrayList<>();

                    for(StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                        AccountSettingDetailDto detailDto = new AccountSettingDetailDto();

                        detailDto.setItemStockDetailId(stockTransactionDetail.getItemStockDetail().getId());
                        detailDto.setItemCode(stockTransactionDetail.getItemStock().getItem().getCode());
                        detailDto.setItemDescription(stockTransactionDetail.getItemStock().getItem().getDescription());
                        detailDto.setUnitCode(stockTransactionDetail.getItemStock().getItem().getUnit().getCode());
                        detailDto.setDebitAccount(stockTransactionDetail.getItemStockDetail().getDebitAccount());
                        detailDto.setCreditAccount(stockTransactionDetail.getItemStockDetail().getCreditAccount());

                        accountSettingDetailDtos.add(detailDto);

                    }

                    accountSetting.setAccountSettingDetails(accountSettingDetailDtos);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return accountSetting;

    }

    @Override
    public PostResponse processCreate(AccountSetting accountSetting, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        AccountSettingValidator validator = new AccountSettingValidator();
        validator.setSettingService(this);
        validator.validate(accountSetting, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {

            AccountSetting existingAccountSetting = new AccountSetting();

            Boolean insertMode = accountSetting.getId() == null || accountSetting.getId() == 0;

            if (insertMode) { // insert mode
                existingAccountSetting.setCreatedBy(authenticationFacade.getLoggedIn());
            } else {
                existingAccountSetting = accountSettingRepo.findById(accountSetting.getId()).orElse(null);
            }

            // editable fields
            existingAccountSetting.setDate(accountSetting.getDate());
            existingAccountSetting.setReceivingReport(accountSetting.getReceivingReport());
            existingAccountSetting.setStockReceive(accountSetting.getStockReceive());
            existingAccountSetting.setMaterialCreditTicket(accountSetting.getMaterialCreditTicket());
            existingAccountSetting.setStockRelease(accountSetting.getStockRelease());
            existingAccountSetting.setMaterialSalvageTicket(accountSetting.getMaterialSalvageTicket());
            existingAccountSetting.setStockAdjustment(accountSetting.getStockAdjustment());
            existingAccountSetting.setAccountSettingDetails(accountSetting.getAccountSettingDetails());
            existingAccountSetting.setRemarks(accountSetting.getRemarks());

            if(Checker.collectionIsNotEmpty(existingAccountSetting.getAccountSettingDetails())){

                for (AccountSettingDetailDto accountSettingDetailDto : existingAccountSetting.getAccountSettingDetails()){

                    ItemStockDetail itemStockDetail = itemStockDetailRepo.findById(accountSettingDetailDto.getItemStockDetailId()).orElse(null);

                    itemStockDetail.setDebitAccount(accountSettingDetailDto.getDebitAccount());
                    itemStockDetail.setCreditAccount(accountSettingDetailDto.getCreditAccount());

                    itemStockDetailRepo.save(itemStockDetail);

                }

            }

            if(accountSetting.getReceivingReport() != null){

                ReceivingReport receivingReport = receivingReportRepo.findById(accountSetting.getReceivingReport().getId()).orElse(null);
                if(receivingReport != null){
                    if(accountSetting.getRrConfirmedForJv()){
                        receivingReport.setConfirmedForJv(true);
                    } else {
                        receivingReport.setConfirmedForJv(false);
                    }
                    receivingReportRepo.save(receivingReport);
                }

            }

            this.model = accountSettingRepo.save(existingAccountSetting);

            if (Checker.isValidId(this.model.getId())) {
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Account setting successfully saved!");
                response.setSuccess(true);
            }

        }

        return response;

    }

    @Override
    public PostResponse processUpdate(AccountSetting accountSetting, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(accountSetting, bindingResult, messageSource);
    }

    @Override
    public ArrayList<AccountSettingDetailDto> getAllAccountSettingDetail(Integer transactionId) {

        ArrayList<AccountSettingDetailDto> accountSettingDetailDtos = new ArrayList<>();

        try {

            ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(transactionId);

            if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                for(StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                    AccountSettingDetailDto detailDto = new AccountSettingDetailDto();

                    detailDto.setItemStockDetailId(stockTransactionDetail.getItemStockDetail().getId());
                    detailDto.setItemCode(stockTransactionDetail.getItemStock().getItem().getCode());
                    detailDto.setItemDescription(stockTransactionDetail.getItemStock().getItem().getDescription());
                    detailDto.setUnitCode(stockTransactionDetail.getItemStock().getItem().getUnit().getCode());
                    detailDto.setDebitAccount(stockTransactionDetail.getItemStock().getItem().getAssetAccount());
                    detailDto.setCreditAccount(stockTransactionDetail.getItemStock().getItem().getAssetAccount());

                    accountSettingDetailDtos.add(detailDto);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return accountSettingDetailDtos;

    }

    @Override
    public Map getAccountSettingRRlinkedDetail(Integer rrId) {
        Map rrDetailMap = new HashMap();
        List<Object[]> rrDetails = receivingReportRepo.detailsForAccountSetting(rrId);

        if (!Checker.collectionIsEmpty(rrDetails)) {

            Object[] rrDetail = rrDetails.get(0);

            if(rrDetail != null){

                Integer rrid = (Integer) rrDetail[0];
                String cvCode = (String) rrDetail[1];
                BigDecimal cvAmount = (BigDecimal) rrDetail[2];
                Integer calId = (Integer) rrDetail[3];
                String calCode = (String) rrDetail[4];
                BigDecimal amount = (BigDecimal) rrDetail[5];
                String caCode = (String) rrDetail[6];
                boolean useCreditCard = (boolean) rrDetail[7];

                rrDetailMap.put("rrId", rrid);
                rrDetailMap.put("cvCode", cvCode);
                rrDetailMap.put("cvAmount", cvAmount);
                rrDetailMap.put("calId", calId);
                rrDetailMap.put("calCode", calCode);
                rrDetailMap.put("amount", amount);
                rrDetailMap.put("caCode", caCode);
                rrDetailMap.put("useCreditCard", useCreditCard);

                String remarks = "";

                if(cvCode != null){
                    remarks = "Advance Payment " + rrDetail[1] + " - ₱" + rrDetail[2];
                }

                if(calId != null){

                    if(remarks.length() > 0){
                        remarks += "\nCash Advance " + rrDetail[6] + ", CA Liquidation " + rrDetail[4];
                    } else {
                        remarks = "Cash Advance " + rrDetail[6] + ", CA Liquidation " + rrDetail[4];
                    }

                }

                if(useCreditCard){

                    if(remarks.length() > 0){
                        remarks += "\nPurchased through credit card";
                    } else {
                        remarks = "Purchased through credit card";
                    }

                }

                rrDetailMap.put("remarks", remarks);

            }
        }
        return rrDetailMap;
    }

    private BigDecimal getTotalQuantity(Integer transactionId){

        BigDecimal totalQuantity = BigDecimal.ZERO;

        try {

            ArrayList<StockTransactionDetail> stockTransactionDetails = stockTransactionDetailRepo.findByStockTransactionTransactionId(transactionId);

            if(Checker.collectionIsNotEmpty(stockTransactionDetails)){

                for (StockTransactionDetail stockTransactionDetail : stockTransactionDetails){

                    totalQuantity = totalQuantity.add(stockTransactionDetail.getQuantity());

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return  totalQuantity;

    }

}
