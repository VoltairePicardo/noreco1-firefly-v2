package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.controller.response.reports.CommonRegisterDetail;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/3/2015.
 */
public interface DocumentDtoer {
    List<Map> getForMainDashboard();
    List<Map> getApprovedVouchersForCashflow(Integer option, String from, String to);
    List<Map> getAllVouchersForCashflow(String from, String to);
    Map getVoucherForCashflowSetup(Integer voucherId, String documentTypeCode);
    List<Map> getVoucherCashflowDetail(Integer voucherId);
    List<DocumentStatus> getDocumentStatuses(Integer worfkflowId);
    List<Map> getVouchersForCancellation(String from, String to, DocumentType documentType, String cancelled);
    List<Map> getVouchersForInstantApproval();
    List<Map> getDocumentTypes();
    List<Map> getDocumentTypesForDocumentInquiry();
    List<Map> getApprovedForMainDashboard();
}
