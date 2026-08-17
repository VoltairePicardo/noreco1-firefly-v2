package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.DocInqDetailDto;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 8/4/2015.
 */
public interface DocumentInquiryService {
    @Transactional
    public List<DocInqListDto> findAll();

    @Transactional(readOnly = true)
    public  List<DocInqDetailDto> findDocDetailsByIdAndTypeId(Integer docId, Integer docTypeId);

    @Transactional(readOnly = true)
    public List<DocInqListDto> findDocumentsByTypeIdStartDateEndDate(Integer docTypeId, String tableName,
                                                                     Date startDate, Date endDate, String particulars,
                                                                     Integer supplierId, String dueDate,
                                                                     String code, String entryAmount, String totals, Integer d);

    @Transactional(readOnly = true)
    public List<DocInqListDto> findDocumentsByRvdId(Integer rvdId);

    @Transactional(readOnly = true)
    public List<DocInqListDto> findDocumentsByTransId(Integer transId);

    @Transactional(readOnly = true)
    public List<DocInqListDto> findDocumentsByUserId(Integer userId, Integer docTypeId, String tableName,
                                                     Date startDate, Date endDate, String particulars);

    @Transactional(readOnly = true)
    List<DocInqListDto> findDocumentsByStatusId(Integer status);

    @Transactional(readOnly = true)
    List<DocInqListDto> findDocumentsPending();

    @Transactional(readOnly = true)
    List<DocInqListDto> findDocumentsByQuery(String query);

    @Transactional(readOnly = true)
    public List<DocInqListDto> findInventoryDocumentsByTransId(Integer transId);

}
