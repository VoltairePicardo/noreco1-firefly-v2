package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.AllocationFactor;
import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.model.FactorPercentageDistro;
import com.noreco1.fireflyv2.model.TemporaryGeneralLedger;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.DateRangeRepo;
import com.noreco1.fireflyv2.repo.FactorPercentageDistroRepo;
import com.noreco1.fireflyv2.repo.TemporaryGeneralLedgerRepo;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import com.noreco1.fireflyv2.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

@Service
public class LedgerServiceImpl implements LedgerService {

    @Autowired
    TemporaryGeneralLedgerRepo temporaryGeneralLedgerRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<GeneralLedgerLineDto> getGLEntries(Integer transId) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<SubLedgerDto> getSLEntries(Integer glId) {
        return null;
    }

    @Override
    public PostResponse checkSegmentsValidity(Integer tempBatchId, String voucherDate) {

        PostResponse response = new PostResponse();
        boolean hasError = false;
        try {

            Calendar calendar = DateHelper.parseDate(voucherDate);
            Date date = calendar.getTime();

            List<TemporaryGeneralLedger> ledgers = temporaryGeneralLedgerRepo.findByTemporaryBatchId(tempBatchId);

            if(Checker.collectionIsNotEmpty(ledgers)) {

                // get all account id temp ledger entries have
                List<Integer> accountIds = new ArrayList<>();

                for (TemporaryGeneralLedger temp: ledgers) {
                    Integer accountId = temp.getAccount().getId();
                    if(accountIds.indexOf(accountId) < 0) {
                        accountIds.add(accountId);
                    }
                }

                if(Checker.collectionIsNotEmpty(accountIds)) {

                    loopAccount:
                    for (Integer accountId: accountIds) {

                        boolean accountHasSegments = false;

                        // get all validity periods available given the voucher date
                        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                        List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);

                        if(Checker.collectionIsNotEmpty(dateRanges)) {

                            loopRange:
                            for (DateRange range : dateRanges) {

                                // check account has allocation factor and has valid date range

                                AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(accountId);
                                if(allocationFactor != null) {

                                    Set<FactorPercentageDistro> percentages = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                                    accountHasSegments = Checker.collectionIsNotEmpty(percentages);

                                    if(accountHasSegments) {
                                        break loopRange;   // next account
                                    }

                                } else {    // no allocation factor assigned
                                    break loopAccount;
                                }
                            }
                        }

                        if(!accountHasSegments) {
                            hasError = true;
                            break loopAccount;
                        }
                    }
                }
            }

            if(!hasError) {
                response.setSuccessMessage("All accounts have valid allocation factor");
            }

        } catch (ParseException e) {
            response.setFailureMessage("Something went wrong!");
            e.printStackTrace();
        }

        return response;
    }
}
