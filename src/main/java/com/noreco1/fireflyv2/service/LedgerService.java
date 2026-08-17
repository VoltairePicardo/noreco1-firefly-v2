package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;

import java.util.List;

public interface LedgerService {
    public List<GeneralLedgerLineDto> getGLEntries(Integer transId);
    public List<SubLedgerDto> getSLEntries(Integer glId);
    PostResponse checkSegmentsValidity(Integer tempBatchId, String voucherDate);
}
