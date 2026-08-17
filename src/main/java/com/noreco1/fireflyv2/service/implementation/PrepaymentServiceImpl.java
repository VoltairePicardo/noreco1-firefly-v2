package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.controller.form.PrepaymentVoucherLinkForm;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.enums.PrepaymentFilterStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrepaymentService;
import com.noreco1.fireflyv2.validator.PrepaymentValidator;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Personal on 6/3/2015.
 */
@Service(value = "ppServiceImpl")
public class PrepaymentServiceImpl implements PrepaymentService {

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    PrepaymentRepo prepaymentRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    PrepaymentDetailRepo prepaymentDetailRepo;

    @Autowired
    TemporaryBatchRepo tempBatchRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    LedgerFacade ledgerFacade;

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    TemporarySubLedgerRepo temporarySubLedgerRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    SubLedgerRepo subLedgerRepo;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    PrepaymentVoucherRepo prepaymentVoucherRepo;

    @Override
    @Transactional(readOnly = true)
    public Prepayment findByDescription(String description) {
        List<Prepayment> prepayments = prepaymentRepo.findByDescription(description);

        if (!Checker.collectionIsEmpty(prepayments)) {
            return prepayments.get(0);
        } else return null;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Object v, BindingResult bindingResult, MessageSource messageSource) {
        Prepayment pp = (Prepayment) v;
        return this.processCreate(pp, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Object v, BindingResult bindingResult, MessageSource messageSource) {
        Prepayment pp = (Prepayment) v;
        PostResponse response = new PostResponse();
        response.setSuccess(false);

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        PrepaymentValidator validator = new PrepaymentValidator();
        validator.setService(this);
        validator.validate(pp, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            Transaction transaction = generatorFacade.transaction();
            Prepayment existingPp = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(new Date()));

            Boolean insertMode = pp.getId() == null;
            if (insertMode) { // insert mode
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                pp.setOffice(employee.getOffice());
                String offAcro = employee.getOffice().getAcronym();
                Object latestPpCode = prepaymentRepo.findLatestPpCodeByYear(voucherYear, "%-"+offAcro+"-%");
                pp.setCode(generatorFacade.voucherCode("PP-"+offAcro, (latestPpCode == null ? "" : String.valueOf(latestPpCode)), new Date()));
                pp.setTransaction(transaction);
                pp.setAccountNo(generatorFacade.entityAccountNumber());
                pp.setCreatedBy(createdBy);
                existingPp = pp;
            } else {
                existingPp = prepaymentRepo.findById(pp.getId()).orElse(null);
            }

            if(!pp.isHasPpd()) {
                existingPp.setDatePaid(pp.getDatePaid());
                existingPp.setDescription(pp.getDescription());
                existingPp.setPrepaymentAccount(pp.getPrepaymentAccount());
                existingPp.setExpenseAccount(pp.getExpenseAccount());
                existingPp.setTotalCost(pp.getTotalCost());
                existingPp.setNoOfMonths(pp.getNoOfMonths());
                existingPp.setMonthlyCost(pp.getMonthlyCost());
                existingPp.setAppliedCost(pp.getAppliedCost().setScale(2, RoundingMode.HALF_UP));
                existingPp.setBalance(pp.getBalance());
                existingPp.setYear(voucherYear);
                existingPp.setStartMonth(pp.getStartMonth()+1);  // js month index starts at 0
                existingPp.setStartYear(pp.getStartYear());

                SLEntityClassification slEntityClassification = new SLEntityClassification();
                slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.PREPAYMENT.getId());
                existingPp.setSlEntityClassification(slEntityClassification);

            } else {
                // processing prepayments
                BigDecimal appliedCost = existingPp.getAppliedCost().add(existingPp.getMonthlyCost());
                existingPp.setAppliedCost(appliedCost.setScale(2, RoundingMode.HALF_UP));
            }

            Prepayment newPp = prepaymentRepo.save(existingPp);

            if (newPp != null) {
                if(pp.isHasPpd()){
                    ArrayList<PrepaymentDetailDto> details = pp.getPpDetails();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.MONTH, details.get(0).getMonth()-1);
                    TemporaryBatch tempBatch = new TemporaryBatch();

                    TemporaryBatch existingTempBatch = tempBatchRepo.findById(pp.getTempBatch().getId()).orElse(null);
                    TemporaryBatch newTempBatch = null;

                    if(existingTempBatch.getId() == null || existingTempBatch.getId() == 0){
                        tempBatch.setDate(pp.getTempBatch().getDate());
                        tempBatch.setVoucherCreated(pp.getTempBatch().getVoucherCreated());
                        tempBatch.setTransaction(transaction);

                        DocumentType docType = new DocumentType();
                        docType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.JV.getId());
                        tempBatch.setDocumentType(docType);

                        tempBatch.setRemarks(pp.getTempBatch().getRemarks());

                        newTempBatch = tempBatchRepo.save(tempBatch);
                    }else{
                        newTempBatch = existingTempBatch;
                    }

                    Map<Integer, Map<Object, Object>> creditEntries =  new HashedMap();
                    Map<Integer, Map<Object, Object>> debitEntries =  new HashedMap();

                    for(PrepaymentDetailDto detailLine: details) {
                        PrepaymentDetail detail = new PrepaymentDetail();

                        detail.setPrepayment(newPp);
                        detail.setAccountNumber(detailLine.getAccountNo());
                        detail.setYear(detailLine.getYear());
                        detail.setMonth(detailLine.getMonth());
                        detail.setAmount(detailLine.getAmount());
                        detail.setBalance(detailLine.getBalance());

                        prepaymentDetailRepo.save(detail);

                        debitEntries = this.createLedgerEntriesMap(debitEntries, detail.getPrepayment().getExpenseAccount(), detail);
                        creditEntries = this.createLedgerEntriesMap(creditEntries, detail.getPrepayment().getPrepaymentAccount(), detail);

                    }

                    java.sql.Date date = new java.sql.Date(pp.getProcessDate().getTime());

                    this.savePrepaymentTempLedgerEntries(debitEntries, transaction, newTempBatch, pp, true, date);
                    this.savePrepaymentTempLedgerEntries(creditEntries, transaction, newTempBatch, pp, false, date);

                }
                response.setModelId(newPp.getId());
                response.setSuccessMessage("Prepayment successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrepaymentListDto> findAll() {
        List<Prepayment> vouchers = prepaymentRepo.findAll();

        List<PrepaymentListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(Prepayment pp : vouchers) {
                PrepaymentListDto dto = new PrepaymentListDto();
                dto.setId(pp.getId());
                dto.setCode(pp.getCode());
                dto.setDatePaid(pp.getDatePaid());
                dto.setDatePaid(pp.getDatePaid());
                dto.setDescription(pp.getDescription());
                dto.setTotalCost(pp.getTotalCost());
                dto.setMonthlyCost(pp.getMonthlyCost());
                dto.setAppliedCost(pp.getAppliedCost());
                dto.setBalance(pp.getBalance());
                dto.setNoOfMonths(pp.getNoOfMonths());

                returnVouchers.add(dto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    @Transactional
    public List<PrepaymentDto> findByStartDateCreatedAndMonthYear(String month, String year) {
        int monthInt = Integer.parseInt(month);
        String startDateCreated = year + "-" + String.format("%02d", monthInt) + "-01";
        String monthYear = year+""+monthInt;
        List<Prepayment> ppList = prepaymentRepo.findByStartDateCreatedAndMonthYear(startDateCreated, monthYear);
        List<PrepaymentDto> ppDtos = new ArrayList<>();

        if (!Checker.collectionIsEmpty(ppList)) {

            Transaction transaction = generatorFacade.transaction();
            TemporaryBatch tempBatch = new TemporaryBatch();
            tempBatch.setDate(new Date());
            tempBatch.setVoucherCreated(false);
            tempBatch.setTransaction(transaction);

            DocumentType docType = new DocumentType();
            docType.setId(com.noreco1.fireflyv2.model.enums.DocumentType.JV.getId());
            tempBatch.setDocumentType(docType);
            tempBatch.setRemarks("Prepayments for the month of " + new DateFormatSymbols().getMonths()[Integer.parseInt(month)-1] + " " + Integer.parseInt(year));

            TemporaryBatch newTempBatch = tempBatchRepo.save(tempBatch);

            for(Prepayment pp : ppList) {
                PrepaymentDto ppDto = new PrepaymentDto();
                ppDto.setId(pp.getId());
                ppDto.setCode(pp.getCode());
                ppDto.setDescription(pp.getDescription());
                ppDto.setAccountNo(pp.getAccountNo());
                ppDto.setTransId(pp.getTransaction().getId());
                ppDto.setDatePaid(pp.getDatePaid());
                ppDto.setCreated(pp.getCreatedAt());
                ppDto.setLastUpdated(pp.getUpdatedAt());
                ppDto.setPrepaymentAccount(pp.getPrepaymentAccount());
                ppDto.setExpenseAccount(pp.getExpenseAccount());
                ppDto.setTotalCost(pp.getTotalCost());
                ppDto.setNoOfMonths(pp.getNoOfMonths());
                ppDto.setMonthlyCost(pp.getMonthlyCost());
                ppDto.setAppliedCost(pp.getAppliedCost());
                ppDto.setBalance(pp.getBalance());
                ppDto.setTemporaryBatch(newTempBatch);

                ppDtos.add(ppDto);
            }
        }

        return  ppDtos;
    }

    @Override
    public List<PrepaymentDto> findByMonthAndYear(String month, String year) {
        List<Prepayment> ppList = prepaymentRepo.findByMonthAndYear(month, year);
        List<PrepaymentDetail> ppdList = prepaymentDetailRepo.findBalanceByMonthAndYear(month, year);
        List<PrepaymentDto> ppDtos = new ArrayList<>();

        if (!Checker.collectionIsEmpty(ppList)) {
            for(Prepayment pp : ppList) {
                PrepaymentDto ppDto = new PrepaymentDto();
                ppDto.setId(pp.getId());
                ppDto.setCode(pp.getCode());
                ppDto.setDescription(pp.getDescription());
                ppDto.setAccountNo(pp.getAccountNo());
                ppDto.setTransId(pp.getTransaction().getId());
                ppDto.setDatePaid(pp.getDatePaid());
                ppDto.setCreated(pp.getCreatedAt());
                ppDto.setLastUpdated(pp.getUpdatedAt());
                ppDto.setPrepaymentAccount(pp.getPrepaymentAccount());
                ppDto.setExpenseAccount(pp.getExpenseAccount());
                ppDto.setTotalCost(pp.getTotalCost());
                ppDto.setNoOfMonths(pp.getNoOfMonths());
                ppDto.setMonthlyCost(pp.getMonthlyCost());
                ppDto.setAppliedCost(pp.getAppliedCost());
                for(PrepaymentDetail ppd : ppdList) {
                    if(ppd.getPrepayment().getId() == pp.getId()) {
                        ppDto.setBalance(ppd.getBalance());
                    }
                }

                ppDtos.add(ppDto);
            }
        }

        return  ppDtos;
    }

    @Override
    public PrepaymentDto findById(Integer id) {
        Prepayment pp =  prepaymentRepo.findById(id).orElse(null);
        PrepaymentDto ppDto = new PrepaymentDto();

        if (pp != null) {
            ppDto.setId(pp.getId());
            ppDto.setDescription(pp.getDescription());
            ppDto.setTransId(pp.getTransaction().getId());
            ppDto.setAccountNo(pp.getAccountNo());

            SlEntity createdBy = slEntityRepo.findById(pp.getCreatedBy().getAccountNo()).orElse(null);

            ppDto.setCreatedBy(createdBy);
            ppDto.setDatePaid(pp.getDatePaid());
            ppDto.setCreated(pp.getCreatedAt());
            ppDto.setLastUpdated(pp.getUpdatedAt());
            ppDto.setPrepaymentAccount(pp.getPrepaymentAccount());
            ppDto.setExpenseAccount(pp.getExpenseAccount());
            ppDto.setTotalCost(pp.getTotalCost());
            ppDto.setNoOfMonths(pp.getNoOfMonths());
            ppDto.setMonthlyCost(pp.getMonthlyCost());
            ppDto.setAppliedCost(pp.getAppliedCost());
            ppDto.setBalance(pp.getBalance());
            ppDto.setStartMonth(pp.getStartMonth()-1); // js month starts at 0
            ppDto.setStartYear(pp.getStartYear());

            if(pp.getStartMonth() != null) {

                try {

                    SimpleDateFormat df = new SimpleDateFormat("MMMMM");
                    SimpleDateFormat dp = new SimpleDateFormat("yyyy-MM-dd");

                    String monthStr = df.format(dp.parse(pp.getStartYear() + "-" + pp.getStartMonth() + "-01"));

                    ppDto.setStartMonthStr(monthStr);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            List<PrepaymentDetail> ppdList = prepaymentDetailRepo.findByPrepaymentId(pp.getId());
            ppDto.setHasPpd(Checker.collectionIsNotEmpty(ppdList));
        }

        return  ppDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrepaymentListDto> findByStartAndEndDate(Date start, Date end) {
        List<Prepayment> vouchers = prepaymentRepo.findByStartAndEndDate(start, end);

        List<PrepaymentListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(Prepayment pp : vouchers) {
                PrepaymentListDto dto = new PrepaymentListDto();
                List<PrepaymentDetail> ppdList = prepaymentDetailRepo.findByPrepaymentId(pp.getId());
                dto.setId(pp.getId());
                dto.setCode(pp.getCode());
                dto.setDatePaid(pp.getDatePaid());
                dto.setDatePaid(pp.getDatePaid());
                dto.setDescription(pp.getDescription());
                dto.setTotalCost(pp.getTotalCost());
                dto.setMonthlyCost(pp.getMonthlyCost());
                dto.setAppliedCost(pp.getAppliedCost());
                dto.setBalance(pp.getBalance());
                dto.setNoOfMonths(pp.getNoOfMonths());
                if (!Checker.collectionIsEmpty(ppdList)) {
                    dto.setHasPpd(true);
                }

                returnVouchers.add(dto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public Map calculateCost(Integer prepaymentId, Integer voucherTransId, Integer prepaymentAccountId) {
        Map data = new HashMap();

        data.put("totalCost", BigDecimal.ZERO);
        data.put("monthlyCost", BigDecimal.ZERO);
        data.put("balance", BigDecimal.ZERO);

        Prepayment prepayment = prepaymentRepo.findById(prepaymentId).orElse(null);
        if(prepayment != null) {

            List<Object[]> objects = subLedgerRepo.sumDebitByTransNoAndAccountId(voucherTransId, prepaymentAccountId);
            if(Checker.collectionIsNotEmpty(objects)) {
                Object debitObj = objects.get(0);

                BigDecimal totalCost =  (BigDecimal) debitObj;
                data.put("totalCost", totalCost);

                BigDecimal balance = totalCost.subtract(prepayment.getAppliedCost());
                data.put("balance", balance);

                BigDecimal monthlyCost = totalCost.divide(new BigDecimal(prepayment.getNoOfMonths()), 2, RoundingMode.HALF_UP);
                data.put("monthlyCost", monthlyCost);
            }
        }

        return data;
    }

    @Override
    public Page<Object[]> vouchersForPrepaymentLinking(Integer prepaymentAccountNo, String query, Pageable pageable) {

        // get Prepayment.PrepaymentAccount
        Prepayment prepayment = prepaymentRepo.findByAccountNo(prepaymentAccountNo);
        if(prepayment != null) {

            Integer prepaymentAccountId = prepayment.getPrepaymentAccount().getId();

            return documentRepo.findAllForPrepaymentLinking(prepaymentAccountId, prepaymentAccountNo, query, pageable);
        }

        return null;
    }

    @Override
    public PostResponse saveLink(PrepaymentVoucherLinkForm form, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {

            Prepayment prepayment = prepaymentRepo.findById(form.getPrepayment().getId()).orElse(null);
            if(prepayment != null) {

                prepayment.setTotalCost(form.getTotalCost());
                prepayment.setMonthlyCost(form.getMonthlyCost());
                prepayment.setBalance(form.getBalance());

                prepaymentRepo.save(prepayment);

                PrepaymentVoucher prepaymentVoucher = new PrepaymentVoucher();
                prepaymentVoucher.setTransaction(form.getVoucherTransaction());
                prepaymentVoucher.setPrepayment(prepayment);
                prepaymentVoucher.setDocumentType(form.getDocumentType());
                prepaymentVoucher.setCreatedBy(authenticationFacade.getLoggedIn());
                prepaymentVoucher.setCreatedAt(new Date());

                prepaymentVoucherRepo.save(prepaymentVoucher);

                response.setSuccessMessage("Prepayment & Voucher linked successfully.");

            } else {
                response.setFailureMessage("Prepayment is not available.");
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public List<PrepaymentListDto> findByStatusAndDateRange(String status, String start, String end) {

        List<PrepaymentListDto> data = new ArrayList<>();

        try {
            Date fromDate = DateHelper.strToDate(start, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(end, "yyyy-MM-dd");

            // dates value: both are NULL or both have value
            if (fromDate == null && toDate != null) {
                fromDate = new Date(0);
            }

            if (toDate == null && fromDate != null) {
                toDate = new Date();
            }

            List<Prepayment> prepayments = new ArrayList<>();

            if(!Checker.isStringNullOrEmpty(status)) {
                if(status.equals(PrepaymentFilterStatus.OPEN.name())) {

                    // repayments with balance > 0 or no data yet in PrepaymentDetail (newly created with zero amt)

                    if(fromDate != null) {
                        prepayments = prepaymentRepo.findAllOpenByDateRange(fromDate, toDate);
                    } else {
                        prepayments = prepaymentRepo.findAllOpen();
                    }

                } else if(status.equals(PrepaymentFilterStatus.CLOSED.name())) {

                    // prepayments with balance=0 AND with data in PrepaymentDetail
                    if(fromDate != null) {
                        prepayments = prepaymentRepo.findAllClosedByDateRange(fromDate, toDate);
                    } else {
                        prepayments = prepaymentRepo.findAllClosed();
                    }
                }
            } else if(fromDate != null) {
                prepayments = prepaymentRepo.findByCreatedAtBetween(fromDate, toDate);
            } else {
                prepayments = prepaymentRepo.findAll();
            }

            if (Checker.collectionIsNotEmpty(prepayments)) {
                for(Prepayment prepayment : prepayments) {

                    List<PrepaymentDetail> ppdList = prepaymentDetailRepo.findByPrepaymentId(prepayment.getId());

                    PrepaymentListDto dto = new PrepaymentListDto();
                    dto.setId(prepayment.getId());
                    dto.setCode(prepayment.getCode());
                    dto.setDatePaid(prepayment.getDatePaid());
                    dto.setDatePaid(prepayment.getDatePaid());
                    dto.setDescription(prepayment.getDescription());
                    dto.setTotalCost(prepayment.getTotalCost());
                    dto.setMonthlyCost(prepayment.getMonthlyCost());
                    dto.setAppliedCost(prepayment.getAppliedCost());
                    dto.setBalance(prepayment.getBalance());
                    dto.setNoOfMonths(prepayment.getNoOfMonths());
                    dto.setHasPpd(Checker.collectionIsNotEmpty(ppdList));

                    data.add(dto);
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return data;
    }

    private Map createLedgerEntriesMap(Map<Integer, Map<Object, Object>> ledgerEntriesMap, Account account, PrepaymentDetail prepaymentDetail) {

        Map<Object, Object> entriesMap = ledgerEntriesMap.get(account.getId());

        if (entriesMap != null) {
            Object amountObj = entriesMap.get("amount");

            BigDecimal exAmount = amountObj == null ? BigDecimal.ZERO : (BigDecimal) amountObj;

            entriesMap.put("amount", exAmount.add(prepaymentDetail.getAmount()));
        } else {
            entriesMap = new HashedMap();
            entriesMap.put("account" , account);
            entriesMap.put("amount" , prepaymentDetail.getAmount());
        }

        Object prepaymentDetailsObj = entriesMap.get("scheduleDetails");

        List<PrepaymentDetail> prepaymentDetails = new ArrayList<>();

        if (prepaymentDetailsObj != null) {
            prepaymentDetails = (List<PrepaymentDetail>) prepaymentDetailsObj;
        }

        prepaymentDetails.add(prepaymentDetail);

        entriesMap.put("prepaymentDetails", prepaymentDetails);

        ledgerEntriesMap.put(account.getId(), entriesMap);

        return ledgerEntriesMap;
    }

    public void savePrepaymentTempLedgerEntries(Map ledgerEntriesMap, Transaction transaction, TemporaryBatch temporaryBatch, Prepayment prepayment, Boolean isDebit, java.sql.Date date) {
        Iterator it = ledgerEntriesMap.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry)it.next();

            Object value = pair.getValue();
            Map accountAndAmountMap = (Map) value;

            List<PrepaymentDetail> prepaymentDetails = (List<PrepaymentDetail>)accountAndAmountMap.get("prepaymentDetails");
            Account account = (Account)accountAndAmountMap.get("account");
            BigDecimal amount = (BigDecimal)accountAndAmountMap.get("amount");

            List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(date, date);
            if(Checker.collectionIsNotEmpty(dateRanges)) {

                for (DateRange range : dateRanges) {

                    // get percentage distribution
                    AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(account.getId());
                    Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                    if (Checker.collectionIsNotEmpty(percentages)) {

                        int counter = 0;
                        int lastIdx = percentages.size();
                        BigDecimal glTotal = BigDecimal.ZERO;

                        for(FactorPercentageDistro obj:percentages) {
                            counter++;  // used to check if end of loop

                            SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(account.getId(), obj.getBusinessSegment().getId());
                            BigDecimal percentage = obj.getPercentage();

                            BigDecimal glShare = (percentage.multiply(amount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            if (glShare == null || glShare.compareTo(BigDecimal.ZERO) == 0) {
                                continue;
                            }

                            TemporaryGeneralLedger temporaryGeneralLedger = new TemporaryGeneralLedger();
                            temporaryGeneralLedger.setTransaction(temporaryBatch.getTransaction());
                            temporaryGeneralLedger.setTemporaryBatch(temporaryBatch);
                            temporaryGeneralLedger.setSegmentAccount(segmentAccount);
                            temporaryGeneralLedger.setAccount(account);

                            glTotal = glTotal.add(glShare);
                            glShare = ServiceUtil.getLastGlShare(glShare, glTotal, amount, counter, lastIdx);

                            if(isDebit) {
                                temporaryGeneralLedger.setDebit(glShare);
                                temporaryGeneralLedger.setCredit(BigDecimal.ZERO);
                            } else {
                                temporaryGeneralLedger.setDebit(BigDecimal.ZERO);
                                temporaryGeneralLedger.setCredit(glShare);
                            }

                            TemporaryGeneralLedger ledger = temporaryGeneralLedgerRepo.save(temporaryGeneralLedger);

                            if (account.getHasSL() == 1) {
                                // sub ledger
                                for(PrepaymentDetail prepaymentDetail:prepaymentDetails) {

                                    TemporarySubLedger temporarySubLedger = new TemporarySubLedger();

                                    temporarySubLedger.setTemporaryBatch(temporaryBatch);
                                    temporarySubLedger.setTemporaryGeneralLedger(ledger);
                                    temporarySubLedger.setSegmentAccount(segmentAccount);
                                    temporarySubLedger.setTransaction(transaction);

                                    SlEntity slEntity = new SlEntity();
                                    slEntity.setAccountNo(prepaymentDetail.getPrepayment().getAccountNo());
                                    temporarySubLedger.setSlEntity(slEntity);

                                    if (isDebit) {
                                        temporarySubLedger.setDebit(glShare);
                                        temporarySubLedger.setCredit(BigDecimal.ZERO);

                                    } else {
                                        temporarySubLedger.setCredit(glShare);
                                        temporarySubLedger.setDebit(BigDecimal.ZERO);
                                    }

                                    temporarySubLedgerRepo.save(temporarySubLedger);
                                }
                            }

                        }
                    }
                }

            }
        }
    }

}