package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.BusinessSegment;
import com.noreco1.fireflyv2.model.DateRange;

import java.util.*;

/**
 * Created by TSI Admin on 5/19/2015.
 */
public class AllocationFactorDto {
    private Integer id;
    private Integer accountTypeId;
    private Integer accountGroupId;
    private AccountDto account;
    private List<Map> segmentPercentage = new ArrayList<>(); // posting
    private DateRange effectivity;
    private Date lastUpdated;
    private Date created;

    public AllocationFactorDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }


    public DateRange getEffectivity() {
        return effectivity;
    }

    public void setEffectivity(DateRange effectivity) {
        this.effectivity = effectivity;
    }


    public List<Map> getSegmentPercentage() {
        return segmentPercentage;
    }

    public void setSegmentPercentage(List<Map> segmentPercentage) {
        this.segmentPercentage = segmentPercentage;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Integer getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(Integer accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public Integer getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(Integer accountGroupId) {
        this.accountGroupId = accountGroupId;
    }
}
