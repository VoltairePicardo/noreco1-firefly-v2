package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.AllocationFactorRepo;
import com.noreco1.fireflyv2.repo.DateRangeRepo;
import com.noreco1.fireflyv2.repo.FactorPercentageDistroRepo;
import com.noreco1.fireflyv2.repo.SegmentAccountRepo;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.controller.response.AllocationFactorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.Date;

/**
 * Created by TSI Admin on 5/19/2015.
 */

@Component
public class AllocationFactorDtoerImpl implements AllocationFactorDtoer {

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Autowired
    SegmentAccountRepo segmentAccountRepo;

    @Autowired
    DateRangeRepo dateRangeRepo;

    @Override
    public List<AllocationFactorDto> findAll() {
        List<AllocationFactorDto> factorDtos = new ArrayList<>();
        List<Object[]> factors = allocationFactorRepo.findAllCustom();

        if (!Checker.collectionIsEmpty(factors)) {

            Integer prevAccountId = 0;
            for(Object[] factorRow:factors) {

                Integer accountId = (Integer) factorRow[1];
                BigDecimal percentage = (BigDecimal) factorRow[11];
                Integer segmentId = (Integer) factorRow[4];
                String segmentDesc = (String) factorRow[5];

                AllocationFactorDto dto = new AllocationFactorDto();

                if (!prevAccountId.equals(accountId)) {

                    Integer factorId = (Integer) factorRow[0];
                    String accountCode = (String) factorRow[2];
                    String accountTitle = (String) factorRow[3];
                    Date rangeStart = (Date) factorRow[6];
                    Date rangeEnd = (Date) factorRow[7];
                    Date createdAt = (Date) factorRow[8];
                    Date updateAt = (Date) factorRow[9];
                    Integer effectDateId = (Integer) factorRow[10];
                    Integer acctTypeId = (Integer) factorRow[12];
                    Integer acctGroupId = (Integer) factorRow[13];

                    AccountDto accountDto = new AccountDto();
                    accountDto.setId(accountId);
                    accountDto.setCode(accountCode);
                    accountDto.setTitle(accountTitle);

                    DateRange dateRange = new DateRange();
                    dateRange.setId(effectDateId);
                    dateRange.setStart(rangeStart);
                    dateRange.setEnd(rangeEnd);

                    dto.setAccount(accountDto);
                    dto.setId(factorId);
                    dto.setCreated(createdAt);
                    dto.setLastUpdated(updateAt);
                    dto.setEffectivity(dateRange);
                    dto.setAccountTypeId(acctTypeId);
                    dto.setAccountGroupId(acctGroupId);

                    factorDtos.add(dto);
                    prevAccountId = accountDto.getId();
                }

                int lastFactorIdx = factorDtos.size() - 1;
                AllocationFactorDto lastFactor = factorDtos.get(lastFactorIdx);
                List<Map> lastFactorSegmentPercentage = lastFactor.getSegmentPercentage();

                BusinessSegment segment = new BusinessSegment();
                segment.setId(segmentId);
                segment.setDescription(segmentDesc);

                Map lastFactorSegmentPercentageMap = new HashMap();
                lastFactorSegmentPercentageMap.put("segment", segment);
                lastFactorSegmentPercentageMap.put("value", percentage);
                lastFactorSegmentPercentage.add(lastFactorSegmentPercentageMap);

                lastFactor.setSegmentPercentage(lastFactorSegmentPercentage);
                factorDtos.set(lastFactorIdx, lastFactor);
            }
        }
        return factorDtos;
    }

    @Override
    public AllocationFactorDto findOne(Integer accountId, Integer effectId) {
        List<Object[]> factors = allocationFactorRepo.findOneCustom(accountId, effectId);
        AllocationFactorDto allocationFactorDto = null;

        if (!Checker.collectionIsEmpty(factors)) {

            allocationFactorDto = new AllocationFactorDto();
            Object[] row = factors.get(0);

            String accountCode = (String) row[2];
            String accountTitle = (String) row[3];
            Date rangeStart = (Date) row[6];
            Date rangeEnd = (Date) row[7];
            Date createdAt = (Date) row[8];
            Date updateAt = (Date) row[9];
            Integer effectDateId = (Integer) row[10];

            AccountDto accountDto = new AccountDto();
            accountDto.setId(accountId);
            accountDto.setCode(accountCode);
            accountDto.setTitle(accountTitle);

            DateRange dateRange = new DateRange();
            dateRange.setId(effectDateId);
            dateRange.setStart(rangeStart);
            dateRange.setEnd(rangeEnd);

            allocationFactorDto.setAccount(accountDto);
            allocationFactorDto.setCreated(createdAt);
            allocationFactorDto.setLastUpdated(updateAt);
            allocationFactorDto.setEffectivity(dateRange);

            for(Object[] factorRow:factors) {

                Integer segmentId = (Integer) factorRow[4];
                String segmentDesc = (String) factorRow[5];
                BigDecimal percentage = (BigDecimal) factorRow[11];

                List<Map> lastFactorSegmentPercentage = allocationFactorDto.getSegmentPercentage();
                Map segmentPercentageMap = new HashMap();
                segmentPercentageMap.put("id", segmentId);
                segmentPercentageMap.put("description", segmentDesc);
                segmentPercentageMap.put("value", percentage);
                lastFactorSegmentPercentage.add(segmentPercentageMap);

                allocationFactorDto.setSegmentPercentage(lastFactorSegmentPercentage);
            }
        }
        return allocationFactorDto;
    }

    @Override
    public List<Map> findLatestOne(Integer accountId) {

        // get the segments according to latest validity date
        List<DateRange> dateRanges = dateRangeRepo.findAllByOrderByEndDescStartDesc();
        return this.makeSegmentPercentageMap(dateRanges, accountId);
    }

    @Override
    public List<AllocationFactorDto> findByEffectivityDateId(Integer effDateId) {
        List<AllocationFactorDto> factorDtos = new ArrayList<>();
        List<Object[]> factors = allocationFactorRepo.findAllCustomByEffectivityDateId(effDateId);

        if (!Checker.collectionIsEmpty(factors)) {

            Integer prevAccountId = 0;
            for(Object[] factorRow:factors) {

                Integer accountId = (Integer) factorRow[1];
                BigDecimal percentage = (BigDecimal) factorRow[11];
                Integer segmentId = (Integer) factorRow[4];
                String segmentDesc = (String) factorRow[5];

                AllocationFactorDto dto = new AllocationFactorDto();

                if (!prevAccountId.equals(accountId)) {

                    Integer factorId = (Integer) factorRow[0];
                    String accountCode = (String) factorRow[2];
                    String accountTitle = (String) factorRow[3];
                    Date rangeStart = (Date) factorRow[6];
                    Date rangeEnd = (Date) factorRow[7];
                    Date createdAt = (Date) factorRow[8];
                    Date updateAt = (Date) factorRow[9];
                    Integer effectDateId = (Integer) factorRow[10];
                    Integer acctTypeId = (Integer) factorRow[12];
                    Integer acctGroupId = (Integer) factorRow[13];

                    AccountDto accountDto = new AccountDto();
                    accountDto.setId(accountId);
                    accountDto.setCode(accountCode);
                    accountDto.setTitle(accountTitle);

                    DateRange dateRange = new DateRange();
                    dateRange.setId(effectDateId);
                    dateRange.setStart(rangeStart);
                    dateRange.setEnd(rangeEnd);

                    dto.setAccount(accountDto);
                    dto.setId(factorId);
                    dto.setCreated(createdAt);
                    dto.setLastUpdated(updateAt);
                    dto.setEffectivity(dateRange);
                    dto.setAccountTypeId(acctTypeId);
                    dto.setAccountGroupId(acctGroupId);

                    factorDtos.add(dto);
                    prevAccountId = accountDto.getId();
                }

                int lastFactorIdx = factorDtos.size() - 1;
                AllocationFactorDto lastFactor = factorDtos.get(lastFactorIdx);
                List<Map> lastFactorSegmentPercentage = lastFactor.getSegmentPercentage();

                BusinessSegment segment = new BusinessSegment();
                segment.setId(segmentId);
                segment.setDescription(segmentDesc);

                Map lastFactorSegmentPercentageMap = new HashMap();
                lastFactorSegmentPercentageMap.put("segment", segment);
                lastFactorSegmentPercentageMap.put("value", percentage);
                lastFactorSegmentPercentage.add(lastFactorSegmentPercentageMap);

                lastFactor.setSegmentPercentage(lastFactorSegmentPercentage);
                factorDtos.set(lastFactorIdx, lastFactor);
            }
        }
        return factorDtos;
    }

    @Override
    public List<Map> findAccountAndVoucherDate(Integer accountId, String voucherDate) {
        List<Map> maps = new ArrayList<>();

        try {

            Calendar voucherDateCal = DateHelper.parseDate(voucherDate);
            java.sql.Date sqlDate = new java.sql.Date(voucherDateCal.getTimeInMillis());

            List<DateRange> dateRanges = dateRangeRepo.findByEndGreaterThanEqualAndStartLessThanEqual(sqlDate, sqlDate);

            maps = this.makeSegmentPercentageMap(dateRanges, accountId);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return maps;
    }

    private List<Map> makeSegmentPercentageMap(List<DateRange> dateRanges, Integer accountId) {

        List<Map> maps = new ArrayList<>();

        if(Checker.collectionIsNotEmpty(dateRanges)) {

            AllocationFactor allocationFactor = allocationFactorRepo.findOneByAccountId(accountId);

            for(DateRange range:dateRanges) {

                Set<FactorPercentageDistro> percentageDistros = factorPercentageDistroRepo.findByFactorIdAndValidityDateId(allocationFactor.getFactor().getId(), range.getId());

                if (Checker.collectionIsNotEmpty(percentageDistros)) {

                    for(FactorPercentageDistro percent:percentageDistros) {

                        SegmentAccount segmentAccount = segmentAccountRepo.findOneByAccountIdAndBusinessSegmentId(accountId, percent.getBusinessSegment().getId());

                        if(segmentAccount != null) {

                            Map segmentPercentageMap = new HashMap();

                            segmentPercentageMap.put("segmentAccountId", segmentAccount.getId());
                            segmentPercentageMap.put("segmentId", percent.getBusinessSegment().getId());
                            segmentPercentageMap.put("segmentDescription", percent.getBusinessSegment().getDescription());
                            segmentPercentageMap.put("percentage", percent.getPercentage().multiply(new BigDecimal(100)));

                            maps.add(segmentPercentageMap);
                        }
                    }

                    break;
                }
            }
        }

        return maps;
    }
}
