package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.Office;
import com.noreco1.fireflyv2.model.PettyCashTransDetail;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.repo.OfficeRepo;
import com.noreco1.fireflyv2.repo.PettyCashTransDetailRepo;
import com.noreco1.fireflyv2.controller.response.reports.PCVDetail;
import com.noreco1.fireflyv2.service.PettyCashTransDetailService;
import com.noreco1.fireflyv2.service.PrintableSummary;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value = "pettyCashTransDetailServiceImpl")
public class PettyCashTransDetailServiceImpl implements PettyCashTransDetailService, PrintableSummary {

    @Autowired
    PettyCashTransDetailRepo pettyCashTransDetailRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    OfficeRepo officeRepo;

    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        PettyCashTransDetail pettyCashTransDetail = pettyCashTransDetailRepo.findById(id).orElse(null);

        if (pettyCashTransDetail != null) {
            map = composeHashMap(pettyCashTransDetail);
        }

        return map;
    }

    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();
        List<PettyCashTransDetail> pettyCashTransDetails = pettyCashTransDetailRepo.findAll();

        if (!Checker.collectionIsEmpty(pettyCashTransDetails)) {
            for (PettyCashTransDetail detail : pettyCashTransDetails) {
                mapList.add(composeHashMap(detail));
            }
        }

        return mapList;
    }

    @Override
    public List<PettyCashTransDetail> findByPCVId(Integer pcvId) {
        List<PettyCashTransDetail> transDetails = pettyCashTransDetailRepo.findByPettyCashTransId(pcvId);

        for(PettyCashTransDetail detail: transDetails) {
            detail.setPettyCashTrans(null);
        }

        return transDetails;

        /*List<HashMap> mapList = new ArrayList<>();
        List<PettyCashTransDetail> pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVId(pcvId);

        if (!Checker.collectionIsEmpty(pettyCashTransDetails)) {
            for (PettyCashTransDetail detail : pettyCashTransDetails) {
                mapList.add(composeHashMap(detail));
            }
        }

        return mapList;*/
    }

    private HashMap composeHashMap(PettyCashTransDetail pettyCashTransDetail) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> pettyCashTrans;

        try {
            // Set petty cash trans object.
            pettyCashTrans = new HashMap<>();

            pettyCashTrans.put("id", pettyCashTransDetail.getPettyCashTrans().getId());
            pettyCashTrans.put("accountNo", pettyCashTransDetail.getPettyCashTrans().getAccountNo());
            pettyCashTrans.put("code", pettyCashTransDetail.getPettyCashTrans().getCode());
            pettyCashTrans.put("documentStatusId", pettyCashTransDetail.getPettyCashTrans().getDocumentStatus().getId());
            pettyCashTrans.put("transactionId", pettyCashTransDetail.getPettyCashTrans().getTransaction().getId());
            pettyCashTrans.put("pettyCashDate", pettyCashTransDetail.getPettyCashTrans().getPettyCashDate());
            pettyCashTrans.put("payee", pettyCashTransDetail.getPettyCashTrans().getPayee());
            pettyCashTrans.put("amount", pettyCashTransDetail.getPettyCashTrans().getAmount());
            pettyCashTrans.put("createdByUserId", pettyCashTransDetail.getPettyCashTrans().getCreatedBy().getId());
            pettyCashTrans.put("approvedByUserId", pettyCashTransDetail.getPettyCashTrans().getApprovingOfficer().getId());
            pettyCashTrans.put("createdAt", pettyCashTransDetail.getPettyCashTrans().getCreatedAt());
            pettyCashTrans.put("updatedAt", pettyCashTransDetail.getPettyCashTrans().getUpdatedAt());

            hm.put("id", pettyCashTransDetail.getId());
            hm.put("pettyCashTrans", pettyCashTrans);
            hm.put("expenseAmount", pettyCashTransDetail.getAmount());
            hm.put("remarks", pettyCashTransDetail.getRemarks());
            hm.put("balance", pettyCashTransDetail.getBalance());

        } catch (Exception ex) {
            Logger.getLogger(PettyCashTransDetailServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    @Override
    public HashMap reportParameters(HttpServletRequest request, Integer checkedByAcctNo, Integer replenishedByAcctNo, String from, String to, Integer documentStatusId, Integer officeId, Integer batchId) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        User user = authenticationFacade.getLoggedIn();

        Employee preparedBy = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        Employee checker = employeeRepo.findOneByAccountNumber(checkedByAcctNo);
        Employee replenishBy = employeeRepo.findOneByAccountNumber(replenishedByAcctNo);

        Office office = officeRepo.findById(officeId).orElse(null);

        params.put("PREPARAR", preparedBy.getName());
        params.put("CHECKER", checker.getName());
        params.put("REPLENISHEDBY", replenishBy.getName());
        params.put("CHECKER_POS", checker.getPosition().getName());
        params.put("PREPARAR_POS", preparedBy.getPosition().getName());
        params.put("REPLENISHEDBY_POS", replenishBy.getPosition().getName());
        params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/summaries/");

        boolean noDocumentStatusId = documentStatusId == null || documentStatusId == 0;
        boolean noOfficeId = officeId == null || officeId == 0;

        List <Object[]> dateRanges = null;

        if(noDocumentStatusId){
            if(noOfficeId){
                dateRanges = pettyCashTransDetailRepo.findDateRangeByPCVBatchID(batchId);
            } else{
                dateRanges = pettyCashTransDetailRepo.findDateRangeByPCVBatchIDAndOfficeId(batchId, officeId);
            }
        }else{
            if(noOfficeId){
                dateRanges = pettyCashTransDetailRepo.findDateRangeByPCVBatchIDAndDocumentStatus(batchId, documentStatusId);
            } else{
                dateRanges = pettyCashTransDetailRepo.findDateRangeByPCVBatchIDAndDocumentStatusAndOfficeId(batchId, documentStatusId, officeId);
            }
        }

        Object[] dateRange = dateRanges.get(0);

        params.put("OFFICE", office != null ? office.getName() : "");
        params.put("RANGE", DateHelper.formatDateRange(dateRange[0].toString(), dateRange[1].toString()));

        // default signatories
        try {
            signatoryFacade.pcvSummary(checkedByAcctNo, replenishedByAcctNo, user);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Short year) {
        return null;
    }

    @Override
    public JRDataSource datasource(Integer batch, Integer documentStatusId, Integer officeId) {
        List<PCVDetail> details = new ArrayList<>();
        List<Object[]> pettyCashTransDetails = null;

        boolean noDocumentStatusId = documentStatusId == null || documentStatusId == 0;
        boolean noOfficeId = officeId == null || officeId == 0;

        if(noDocumentStatusId){
            if(noOfficeId){
                pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVBatchID(batch);
            } else{
                pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVBatchIDAndOfficeId(batch, officeId);
            }
        }else{
            if(noOfficeId){
                pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVBatchIDAndDocumentStatus(batch, documentStatusId);
            } else{
                pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVBatchIDAndDocumentStatusAndOfficeId(batch, documentStatusId, officeId);
            }
        }

        if (!Checker.collectionIsEmpty(pettyCashTransDetails)) {
            Integer count = 0;
            String prevCode = "";
            for (Object[] dto : pettyCashTransDetails) {
                PCVDetail d = new PCVDetail();

                Integer docStatus = (Integer)dto[5];
                d.setCode(dto[0].toString());
                d.setAccountNumber(docStatus == DocumentStatus.CANCELLED.getId() ? "" : dto[1].toString());
                d.setPayee(docStatus == DocumentStatus.CANCELLED.getId() ? "-- CANCELLED --" : dto[2].toString());
                d.setAmount(docStatus == DocumentStatus.CANCELLED.getId() ? null : (BigDecimal)dto[3]);
                d.setVoucherDate((Date) dto[4]);
                d.setNatureOfPayment("");
                d.setDocumentStatus((Integer)dto[5]);
                d.setBatchId((Integer)dto[6]);
                d.setRemarks(docStatus == DocumentStatus.CANCELLED.getId() ? "" : dto[7].toString());
                d.setTotalAmount(docStatus == DocumentStatus.CANCELLED.getId() ? null : (BigDecimal)dto[8]);

                if(docStatus != DocumentStatus.CANCELLED.getId()){
                    if(!d.getCode().equals(prevCode)) {
                        count++;
                    }
                }
                prevCode = d.getCode();
                d.setCount(count);

                details.add(d);
            }
        }

        return new JRBeanCollectionDataSource(details, false);
    }
}
