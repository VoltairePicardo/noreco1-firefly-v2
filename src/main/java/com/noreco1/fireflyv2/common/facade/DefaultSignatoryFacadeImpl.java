package com.noreco1.fireflyv2.common.facade;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.repo.SettingRepo;
import com.noreco1.fireflyv2.repo.SlEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by TSI Admin on 8/11/2015.
 */

@Component
public class DefaultSignatoryFacadeImpl implements SignatoryFacade {

    private final String APV_SIGNATORIES = "APV_SIGNATORIES";
    private final String APV_SIGNATORIES_DESC = "Default signatories for creating APV";

    private final String CV_SIGNATORIES = "CV_SIGNATORIES";
    private final String CV_SIGNATORIES_DESC = "Default signatories for creating CV";

    private final String JV_SIGNATORIES = "JV_SIGNATORIES";
    private final String JV_SIGNATORIES_DESC = "Default signatories for creating JV";

    private final String RV_SIGNATORIES = "RV_SIGNATORIES";
    private final String RV_SIGNATORIES_DESC = "Default signatories for creating RV";

    private final String PO_SIGNATORIES = "PO_SIGNATORIES";
    private final String PO_SIGNATORIES_DESC = "Default signatories for creating PO";

    private final String JO_SIGNATORIES = "JO_SIGNATORIES";
    private final String JO_SIGNATORIES_DESC = "Default signatories for creating JO";

    private final String JO_ACCEPTANCE_SIGNATORIES = "JO_ACCEPTANCE_SIGNATORIES";
    private final String JO_ACCEPTANCE_SIGNATORIES_DESC = "Default signatories for creating JO ACCEPTANCE";

    private final String CA_SIGNATORIES = "CA_SIGNATORIES";
    private final String CA_SIGNATORIES_DESC = "Default signatories for creating Cash Advance";

    private final String CAL_SIGNATORIES = "CAL_SIGNATORIES";
    private final String CAL_SIGNATORIES_DESC = "Default signatories for creating Cash Advance Liquidation";

    private final String PR_SIGNATORIES = "PR_SIGNATORIES";
    private final String PR_SIGNATORIES_DESC = "Default signatories for creating Payment Request";

    private final String CRV_SIGNATORIES = "CRV_SIGNATORIES";
    private final String CRV_SIGNATORIES_DESC = "Default signatories for creating Cash Receipts";

    private final String SV_SIGNATORIES = "SV_SIGNATORIES";
    private final String SV_SIGNATORIES_DESC = "Default signatories for creating Sales Vouchers / Energy Sales";

    private final String BAD_SIGNATORIES = "BAD_SIGNATORIES";
    private final String BAD_SIGNATORIES_DESC = "Default signatories for creating Bank Deposits";

    private final String BUDG_SIGNATORIES = "BUDG_SIGNATORIES";
    private final String BUDG_SIGNATORIES_DESC = "Default signatories for creating Budget";

    private final String PCV_SIGNATORIES = "PCV_SIGNATORIES";
    private final String PCV_SIGNATORIES_DESC = "Default signatories for creating Petty Cash Voucher";

    private final String MIV_SIGNATORIES = "MIV_SIGNATORIES";
    private final String MIV_SIGNATORIES_DESC = "Default signatories for creating Material Issue Voucher";

    private final String PCV_SUMMARY_SIGNATORIES = "PCV_SUMMARY_SIGNATORIES";
    private final String PCV_SUMMARY_SIGNATORIES_DESC = "Default signatories for PCV Summary";

    private final String CANVASS_SIGNATORIES = "CANVASS_SIGNATORIES";
    private final String CANVASS_SIGNATORIES_DESC = "Default signatories for creating Canvass";

    private final String AJ_SIGNATORIES = "AJ_SIGNATORIES";
    private final String AJ_SIGNATORIES_DESC = "Default signatories for creating AJ";

    private final String CASHFLOW_STATEMENT_SIGNATORIES = "CASHFLOW_STATEMENT_SIGNATORIES";
    private final String CASHFLOW_STATEMENT_SIGNATORIES_DESC = "Default signatories for Cashflow Statement";

    private final String BIR_FORM_1601E_SIGNATORIES = "BIR_FORM_1601E_SIGNATORIES";
    private final String BIR_FORM_1601E_SIGNATORIES_DESC = "Default signatories for BIR FORM 1601E";

    private final String RR_SIGNATORIES = "RR_SIGNATORIES";
    private final String RR_SIGNATORIES_DESC = "Default signatories for Receiving Reports";

    private final String GL_INQUIRY_SUMMARY_SIGNATORIES = "GL_INQUIRY_SUMMARY_SIGNATORIES";
    private final String GL_INQUIRY_SUMMARY_SIGNATORIES_DESC = "Default signatories for GL Inquiry Summary";

    private final String SW_SIGNATORIES = "SW_SIGNATORIES";
    private final String SW_SIGNATORIES_DESC = "Default signatories for Stock Withdrawal";

    private final String SR_SIGNATORIES = "SR_SIGNATORIES";
    private final String SR_SIGNATORIES_DESC = "Default signatories for Stock Release";

    private final String MCT_SIGNATORIES = "MCT_SIGNATORIES";
    private final String MCT_SIGNATORIES_DESC = "Default signatories for Material Credit Ticket";

    private final String SA_SIGNATORIES = "SA_SIGNATORIES";
    private final String SA_SIGNATORIES_DESC = "Default signatories for Stock Adjustment";

    private final String MST_SIGNATORIES = "MST_SIGNATORIES";
    private final String MST_SIGNATORIES_DESC = "Default signatories for Material Salvage Ticket";

    private final String ST_SIGNATORIES = "ST_SIGNATORIES";
    private final String ST_SIGNATORIES_DESC = "Default signatories for Stock Transfer";

    private final String SRC_SIGNATORIES = "SRC_SIGNATORIES";
    private final String SRC_SIGNATORIES_DESC = "Default signatories for Stock Receive";

    private final String QUOTATION_SIGNATORIES = "QUOTATION_SIGNATORIES";
    private final String QUOTATION_SIGNATORIES_DESC = "Default signatories for Summary Of Quotation";

    private final String SITE_INSPECTION_REPORT_SIGNATORIES = "SITE_INSPECTION_REPORT_SIGNATORIES";
    private final String SITE_INSPECTION_REPORT_SIGNATORIES_DESC = "Default signatories for Site Inspection Report";

    private final String CE_SIGNATORIES = "CE_SIGNATORIES";
    private final String CE_SIGNATORIES_DESC = "Default signatories for Cost Estimate";

    private final String BILL_OF_MATERIAL_SIGNATORIES = "BILL_OF_MATERIAL_SIGNATORIES";
    private final String BILL_OF_MATERIAL_SIGNATORIES_DESC = "Default signatories for Bill of Materials";

	private final String PROJECT_ACCEPTANCE_SIGNATORIES = "PROJECT_ACCEPTANCE_SIGNATORIES";
    private final String PROJECT_ACCEPTANCE_SIGNATORIES_DESC = "Default signatories for creating PROJECT ACCEPTANCE";

    private final String PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES= "PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES";
    private final String PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES_DESC = "Default signatories for creating PROJECT ACCEPTANCE CERTIFICATION";

    private final String BUDGET_LINE_ITEM_SIGNATORIES = "BUDGET_LINE_ITEM_SIGNATORIES";
    private final String BUDGET_LINE_ITEM_SIGNATORIES_DESC = "Default signatories for budget line item";

    private final String PETTY_CASH_LIQUIDATION_SIGNATORIES = "PETTY_CASH_LIQUIDATION_SIGNATORIES";
    private final String PETTY_CASH_LIQUIDATION_SIGNATORIES_DESC = "Default signatories for creating Petty Cash Liquidaiton";

    private final String MEMORANDUM_RECEIPT_SIGNATORIES = "MEMORANDUM_RECEIPT_SIGNATORIES";
    private final String MEMORANDUM_RECEIPT_SIGNATORIES_DESC = "Default signatories for memorandum receipt";

    private final String CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES = "CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES";
    private final String CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES_DESC = "Default signatories for credit card purchase request";

    @Autowired
    private SettingFacade settingFacade;

    @Autowired
    private SettingRepo settingRepo;

    @Autowired
    private SlEntityRepo slEntityRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Override
    public void apv(AccountsPayableVoucher apv) {

        try {
            Map<String,Integer> map = new HashMap<>();
            map.put("checkedByAccountNo", apv.getChecker().getAccountNo());
            map.put("approvedByAccountNo", apv.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.APV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(apv.getCreatedBy());
                setting.setCode(this.APV_SIGNATORIES);
                setting.setDescription(this.APV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void cv(CheckVoucher cv) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("budgetedByAccountNo", cv.getBudgetOfficer().getAccountNo());
            map.put("recommendedByAccountNo", cv.getRecommendingOfficer().getAccountNo());
            map.put("auditedByAccountNo", cv.getAuditingOfficer().getAccountNo());
            map.put("checkedByAccountNo", cv.getChecker().getAccountNo());
            map.put("checkPrintedByAccountNo", cv.getCheckPrinter().getAccountNo());
            map.put("approvedByAccountNo", cv.getApprovingOfficer().getAccountNo());
            map.put("secondCheckSignAccountNo", cv.getSecondCheckSign() != null ? cv.getSecondCheckSign().getAccountNo() : 0);

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(cv.getCreatedBy());
                setting.setCode(this.CV_SIGNATORIES);
                setting.setDescription(this.CV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void jv(JournalVoucher jv) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("auditedByAccountNo", jv.getAuditingOfficer().getAccountNo());
            map.put("budgetedByAccountNo", jv.getBudgetOfficer().getAccountNo());
            map.put("recommendedByAccountNo", jv.getRecommendingOfficer().getAccountNo());
            map.put("checkedByAccountNo", jv.getChecker().getAccountNo());
            map.put("approvedByAccountNo", jv.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.JV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(jv.getCreatedBy());
                setting.setCode(this.JV_SIGNATORIES);
                setting.setDescription(this.JV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void rv(PurchaseRequest rv) {
        try {
            Map<String,Integer> map = new HashMap<>();

            Setting exSetting = settingRepo.findOneByCode(this.RV_SIGNATORIES);
            if (exSetting != null) {

                String json = exSetting.getValue();
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);

                TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                HashMap<String,Object> rvSigns = mapper.readValue(json, typeRef);

                rvSigns.put("approvedByAccountNo", rv.getApprovingOfficer().getAccountNo());
                rvSigns.put("inventoryCheckedByAccountNo", rv.getInventoryCheckedBy() != null ? rv.getInventoryCheckedBy().getAccountNo() : null);
                rvSigns.put("reviewedAcceptedByAccountNo", rv.getReviewedAcceptedBy().getAccountNo());

                String settingValue = new ObjectMapper().writeValueAsString(rvSigns);
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());

                settingRepo.save(exSetting);

            } else {
                map.put("approvedByAccountNo", rv.getApprovingOfficer().getAccountNo());
                map.put("inventoryCheckedByAccountNo", rv.getInventoryCheckedBy() != null ? rv.getInventoryCheckedBy().getAccountNo() : null);
                map.put("reviewedAcceptedByAccountNo", rv.getReviewedAcceptedBy().getAccountNo());

                String settingValue = new ObjectMapper().writeValueAsString(map);

                Setting setting = new Setting();
                setting.setCreatedBy(rv.getCreatedBy());
                setting.setCode(this.RV_SIGNATORIES);
                setting.setDescription(this.RV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void po(PurchaseOrder po) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("budgetCheckedByAccountNo", po.getBudgetCheckedBy().getAccountNo());
            map.put("checkedByAccountNo", po.getCheckedBy().getAccountNo());
            map.put("approvedByAccountNo", po.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.PO_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(po.getCreatedBy());
                setting.setCode(this.PO_SIGNATORIES);
                setting.setDescription(this.PO_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void quotation(Quotation quotation) {
        try {
            Map<String,Integer> map = new HashMap<>();

            if(quotation.getApprovingOfficer() != null) {
                map.put("approvedByFinanceManagerAccountNo", quotation.getApprovingOfficer().getAccountNo());
            }

            if(quotation.getApprovedByGeneralManager() != null) {
                map.put("approvedByGeneralManagerAccountNo", quotation.getApprovedByGeneralManager().getAccountNo());
            }

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.QUOTATION_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(quotation.getCreatedBy());
                setting.setCode(this.QUOTATION_SIGNATORIES);
                setting.setDescription(this.QUOTATION_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    @Override
//    public void canvass(Canvass canvass) {
//        try {
//            Map<String,Integer> map = new HashMap<>();
//
//            map.put("approvedByAccountNo", canvass.getApprovingOfficer().getAccountNo());
//
//            String settingValue = new ObjectMapper().writeValueAsString(map);
//
//            Setting exSetting = settingRepo.findOneByCode(this.CANVASS_SIGNATORIES);
//            if (exSetting != null) {
//                exSetting.setValue(settingValue);
//                exSetting.setUpdatedAt(new Date());
//                settingRepo.save(exSetting);
//            } else {
//                Setting setting = new Setting();
//                setting.setCreatedBy(canvass.getCreatedBy());
//                setting.setCode(this.CANVASS_SIGNATORIES);
//                setting.setDescription(this.CANVASS_SIGNATORIES_DESC);
//                setting.setValue(settingValue);
//                setting.setCreatedAt(new Date());
//                setting.setUpdatedAt(new Date());
//
//                settingRepo.save(setting);
//            }
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    @Override
    public void aj(AdjustmentJournal aj) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("recommendedByAccountNo", aj.getRecommendingOfficer().getAccountNo());
//            map.put("auditedByAccountNo", aj.getAuditor().getAccountNo());
            map.put("checkedByAccountNo", aj.getChecker().getAccountNo());
            map.put("approvedByAccountNo", aj.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.AJ_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(aj.getCreatedBy());
                setting.setCode(this.AJ_SIGNATORIES);
                setting.setDescription(this.AJ_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void jo(JobOrder jo) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("budgetCheckedByAccountNo", jo.getBudgetCheckedBy().getAccountNo());
            map.put("checkedByAccountNo", jo.getCheckedBy().getAccountNo());
            map.put("approvedByAccountNo", jo.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.JO_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(jo.getCreatedBy());
                setting.setCode(this.JO_SIGNATORIES);
                setting.setDescription(this.JO_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void joAcceptance(JoAcceptance joAcceptance) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("inspectedByAccountNo", joAcceptance.getInspectedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.JO_ACCEPTANCE_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(joAcceptance.getCreatedBy());
                setting.setCode(this.JO_ACCEPTANCE_SIGNATORIES);
                setting.setDescription(this.JO_ACCEPTANCE_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Map defaultSignatories(DocumentType documentType) {
        Map data = new HashMap();
        if (documentType != null) {
            String documentTypeCode = documentType.getCode();
            switch (documentTypeCode) {
                case "APV": {
                    Map codeMap = settingFacade.getByCode(this.APV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer approvedByAccountNo = Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("checkedBy", checker);
                        data.put("approvedBy", approvingOfficer);

                    }
                    break;
                }
                case "CV": {
                    Map codeMap = settingFacade.getByCode(this.CV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer budgetedByAccountNo = Integer.parseInt(codeMap.get("budgetedByAccountNo").toString());
                        Integer recommendedByAccountNo = Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer auditedByAccountNo = Integer.parseInt(codeMap.get("auditedByAccountNo").toString());
                        Integer checkPrintedByAccountNo = Integer.parseInt(codeMap.get("checkPrintedByAccountNo").toString());
                        Integer approvedByAccountNo = Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity budgetOfficer = slEntityRepo.findOneByAccountNo(budgetedByAccountNo);
                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
                        SlEntity auditedBy = slEntityRepo.findOneByAccountNo(auditedByAccountNo);
                        SlEntity checkPrinter = slEntityRepo.findOneByAccountNo(checkPrintedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("checkedBy", checker);
                        data.put("budgetOfficer", budgetOfficer);
                        data.put("recommendedBy", recommendedBy);
                        data.put("auditedBy", auditedBy);
                        data.put("checkPrinter", checkPrinter);
                        data.put("approvedBy", approvingOfficer);

                    }
                    break;
                }
                case "JV": {
                    Map codeMap = settingFacade.getByCode(this.JV_SIGNATORIES);
                    if (codeMap != null) {
                        Integer budgetedByAccountNo = Integer.parseInt(codeMap.get("budgetedByAccountNo").toString());
                        Integer auditedByAccountNo = Integer.parseInt(codeMap.get("auditedByAccountNo").toString());
                        Integer recommendedByAccountNo = Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer checkedByAccountNo = Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer approvedByAccountNo = Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity auditedBy = slEntityRepo.findOneByAccountNo(auditedByAccountNo);
                        SlEntity budgetOfficer = slEntityRepo.findOneByAccountNo(budgetedByAccountNo);
                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("checkedBy", checker);
                        data.put("budgetOfficer", budgetOfficer);
                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendedBy);
                        data.put("auditedBy", auditedBy);
                    }
                    break;
                }
                case "RV": {
                    Map codeMap = settingFacade.getByCode(this.RV_SIGNATORIES);
                    if (codeMap != null) {


                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") != null ? Integer.parseInt(codeMap.get("approvedByAccountNo").toString()):0;
                        Integer budgetCheckedByAccountNo = codeMap.get("budgetCheckedByAccountNo") != null ? Integer.parseInt(codeMap.get("budgetCheckedByAccountNo").toString()):0;
                        Integer inventoryCheckedByAccountNo = codeMap.get("inventoryCheckedByAccountNo") != null ? Integer.parseInt(codeMap.get("inventoryCheckedByAccountNo").toString()):0;
                        Integer reviewedAcceptedByAccountNo = codeMap.get("reviewedAcceptedByAccountNo") != null ? Integer.parseInt(codeMap.get("reviewedAcceptedByAccountNo").toString()):0;

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity budgetCheckedBy = slEntityRepo.findOneByAccountNo(budgetCheckedByAccountNo);
                        SlEntity inventoryCheckedBy = slEntityRepo.findOneByAccountNo(inventoryCheckedByAccountNo);
                        SlEntity reviewedAcceptedBy = slEntityRepo.findOneByAccountNo(reviewedAcceptedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("budgetCheckedBy", budgetCheckedBy);
                        data.put("inventoryCheckedBy", inventoryCheckedBy);
                        data.put("reviewedAcceptedBy", reviewedAcceptedBy);

                    }
                    break;
                }
                case "PO": {
                    Map codeMap = settingFacade.getByCode(this.PO_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer budgetCheckedByAccountNo = codeMap.get("budgetCheckedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("budgetCheckedByAccountNo").toString());

                        SlEntity budgetCheckedBy = slEntityRepo.findOneByAccountNo(budgetCheckedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checkedBy", checkedBy);
                        data.put("budgetCheckedBy", budgetCheckedBy);
                    }
                    break;
                }
                case "JO": {
                    Map codeMap = settingFacade.getByCode(this.JO_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer budgetCheckedByAccountNo = codeMap.get("budgetCheckedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("budgetCheckedByAccountNo").toString());

                        SlEntity budgetCheckedBy = slEntityRepo.findOneByAccountNo(budgetCheckedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checkedBy", checkedBy);
                        data.put("budgetCheckedBy", budgetCheckedBy);

                    }
                    break;
                }
                case "JOA": {
                    Map codeMap = settingFacade.getByCode(this.JO_ACCEPTANCE_SIGNATORIES);
                    if (codeMap != null) {

                        Integer notedByAccountNo = codeMap.get("notedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("notedByAccountNo").toString());

                        SlEntity notedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);

                        data.put("notedBy", notedBy);
                    }
                    break;
                }
                case "CA": {
                    Map codeMap = settingFacade.getByCode(this.CA_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer notedByAccountNo = codeMap.get("recommendedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer budgetOfficerAccountNo = codeMap.get("budgetOfficerAccountNo") == null ? 0:Integer.parseInt(codeMap.get("budgetOfficerAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);
                        SlEntity budgetOfficer = slEntityRepo.findOneByAccountNo(budgetOfficerAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendedBy);
                        data.put("budgetOfficer", budgetOfficer);
                    }
                    break;
                }
                case "CAL": {
                    Map codeMap = settingFacade.getByCode(this.CAL_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer notedByAccountNo = codeMap.get("recommendedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendedBy);
                    }
                    break;
                }
                case "PR": {
                    Map codeMap = settingFacade.getByCode(this.PR_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer notedByAccountNo = codeMap.get("notedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("notedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity notedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checker", checker);
                        data.put("notedBy", notedBy);
                    }
                    break;
                }
                case "CRV": {
                    Map codeMap = settingFacade.getByCode(this.CRV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer recommendedByAccountNo = codeMap.get("recommendedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity recommendingOfficer = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendingOfficer);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "SV": {
                    Map codeMap = settingFacade.getByCode(this.SV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                    }
                    break;
                }
                case "BAD": {
                    Map codeMap = settingFacade.getByCode(this.BAD_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "BUDG": {
                    Map codeMap = settingFacade.getByCode(this.BUDG_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        data.put("approvedBy", approvingOfficer);
                    }
                    break;
                }
                case "PCV": {
                    Map codeMap = settingFacade.getByCode(this.PCV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer releasedByAccountNo = codeMap.get("releasedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("releasedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity releasedBy = slEntityRepo.findOneByAccountNo(releasedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("releasedBy", releasedBy);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "MR": {
                    Map codeMap = settingFacade.getByCode(this.MIV_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer recApprovedByAccountNo = codeMap.get("recApprovedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recApprovedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity recApprovingOfficer = slEntityRepo.findOneByAccountNo(recApprovedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("recApprovedBy", recApprovingOfficer);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "PCV_SUMMARY": {
                    Map codeMap = settingFacade.getByCode(this.PCV_SUMMARY_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer replenishedByAcctNo = codeMap.get("replenishedByAcctNo") == null ? 0:Integer.parseInt(codeMap.get("replenishedByAcctNo").toString());

                        SlEntity replenishedBy = slEntityRepo.findOneByAccountNo(replenishedByAcctNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("replenishedBy", replenishedBy);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "CF": {
                    Map codeMap = settingFacade.getByCode(this.CANVASS_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                    }
                    break;
                }
                case "AJ": {
                    Map codeMap = settingFacade.getByCode(this.AJ_SIGNATORIES);
                    if (codeMap != null) {
                        Integer recommendedByAccountNo = Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
//                        Integer auditedByAccountNo = Integer.parseInt(codeMap.get("auditedByAccountNo").toString());
                        Integer checkedByAccountNo = Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer approvedByAccountNo = Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
//                        SlEntity auditor = slEntityRepo.findOneByAccountNo(auditedByAccountNo);
                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("checkedBy", checker);
                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendedBy);
//                        data.put("auditedBy", auditor);
                    }
                    break;
                }
                case "CASHFLOW_STATEMENT": {
                    Map codeMap = settingFacade.getByCode(this.CASHFLOW_STATEMENT_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer notedByAcctNo = codeMap.get("notedByAcctNo") == null ? 0:Integer.parseInt(codeMap.get("notedByAcctNo").toString());

                        SlEntity notedBy = slEntityRepo.findOneByAccountNo(notedByAcctNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("notedBy", notedBy);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "BIR_FORM_1601E": {
                    Map codeMap = settingFacade.getByCode(this.BIR_FORM_1601E_SIGNATORIES);
                    if (codeMap != null) {

                        Map taxPayerMap = (Map) codeMap.get("taxPayer");
                        Integer taxPayerAccountNo = Integer.parseInt(taxPayerMap.get("accountNo").toString());

                        SlEntity taxPayer = slEntityRepo.findOneByAccountNo(taxPayerAccountNo);

                        data.put("taxPayer", taxPayer);
                    }
                    break;
                }
                case "RR": {
                    Map codeMap = settingFacade.getByCode(this.RR_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "GL_INQUIRY_SUMMARY": {
                    Map codeMap = settingFacade.getByCode(this.GL_INQUIRY_SUMMARY_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("checkedBy", checkedBy);
                    }
                    break;
                }
                case "SW": {
                    Map codeMap = settingFacade.getByCode(this.SW_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer notedByAccountNo = codeMap.get("notedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("notedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity notedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checkedBy", checkedBy);
                        data.put("notedBy", notedBy);
                    }
                    break;
                }
                case "SRL": {
                    Map codeMap = settingFacade.getByCode(this.SR_SIGNATORIES);
                    if (codeMap != null) {

                        Integer auditedByAccountNo = codeMap.get("auditedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("auditedByAccountNo").toString());
                        Integer receivedByAccountNo = codeMap.get("receivedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("receivedByAccountNo").toString());

                        SlEntity auditor = slEntityRepo.findOneByAccountNo(auditedByAccountNo);
                        SlEntity receivedBy = slEntityRepo.findOneByAccountNo(receivedByAccountNo);

                        data.put("auditor", auditor);
                        data.put("receivedBy", receivedBy);
                    }
                    break;
                }
                case "MCT": {
                    Map codeMap = settingFacade.getByCode(this.MCT_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer requestedByAccountNo = codeMap.get("requestedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("requestedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity requester = slEntityRepo.findOneByAccountNo(requestedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("requester", requester);
                    }
                    break;
                }
                case "SA": {
                    Map codeMap = settingFacade.getByCode(this.SA_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checker", checkedBy);
                    }
                    break;
                }
                case "MST": {
                    Map codeMap = settingFacade.getByCode(this.MST_SIGNATORIES);
                    if (codeMap != null) {

                        Integer returnedByAccountNo = codeMap.get("returnedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("returnedByAccountNo").toString());
                        Integer receivedByAccountNo = codeMap.get("receivedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("receivedByAccountNo").toString());

                        SlEntity returnedBy = slEntityRepo.findOneByAccountNo(returnedByAccountNo);
                        SlEntity receivedBy = slEntityRepo.findOneByAccountNo(receivedByAccountNo);

                        data.put("returnedBy", returnedBy);
                        data.put("receivedBy", receivedBy);
                    }
                    break;
                }
                case "ST": {
                    Map codeMap = settingFacade.getByCode(this.ST_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);

                    }
                    break;
                }
                case "SRC": {
                    Map codeMap = settingFacade.getByCode(this.SRC_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("checker", checkedBy);
                    }
                    break;
                }
                case "QUOTATION_SUMMARY": {
                    Map codeMap = settingFacade.getByCode(this.QUOTATION_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByFinanceManagerAccountNo = codeMap.get("approvedByFinanceManagerAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByFinanceManagerAccountNo").toString());
                        Integer approvedByGeneralManagerAccountNo = codeMap.get("approvedByGeneralManagerAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByGeneralManagerAccountNo").toString());

                        SlEntity financeManager = slEntityRepo.findOneByAccountNo(approvedByFinanceManagerAccountNo);
                        SlEntity generalManager = slEntityRepo.findOneByAccountNo(approvedByGeneralManagerAccountNo);

                        data.put("approvedByFinanceManager", financeManager);
                        data.put("approvedByGeneralManger", generalManager);
                    }
                    break;
                }
                case "SITE_INSPECTION_REPORT": {
                    Map codeMap = settingFacade.getByCode(this.SITE_INSPECTION_REPORT_SIGNATORIES);
                    if (codeMap != null) {

                        Integer checkedByAccountNo = Integer.parseInt(codeMap.get("checkedByAccountNo").toString());
                        Integer notedByAccountNo = Integer.parseInt(codeMap.get("notedByAccountNo").toString());
                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity checker = slEntityRepo.findOneByAccountNo(checkedByAccountNo);
                        SlEntity notedBy = slEntityRepo.findOneByAccountNo(notedByAccountNo);
                        SlEntity approvedBy = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("checkedBy", checker);
                        data.put("notedBy", notedBy);
                        data.put("approvedBy", approvedBy);

                    }
                    break;
                }
                case "CE": {
                    Map codeMap = settingFacade.getByCode(this.CE_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer recommendedByAccountNo = codeMap.get("recommendedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer checkedByAccountNo = codeMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("checkedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity recommendedBy = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
                        SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("recommendedBy", recommendedBy);
                        data.put("checker", checkedBy);
                    }
                    break;
                }
                case "PAR": {

                    Map codeMap = settingFacade.getByCode(this.PROJECT_ACCEPTANCE_SIGNATORIES);
                    if (codeMap != null) {

                        Integer inspector1AccountNo = codeMap.get("inspector1AccountNo") == null ? 0:Integer.parseInt(codeMap.get("inspector1AccountNo").toString());
                        Integer inspector2AccountNo = codeMap.get("inspector2AccountNo") == null ? 0:Integer.parseInt(codeMap.get("inspector2AccountNo").toString());
                        Integer inspector3AccountNo = codeMap.get("inspector3AccountNo") == null ? 0:Integer.parseInt(codeMap.get("inspector3AccountNo").toString());

                        SlEntity inspector1 = slEntityRepo.findOneByAccountNo(inspector1AccountNo);
                        SlEntity inspector2 = slEntityRepo.findOneByAccountNo(inspector2AccountNo);
                        SlEntity inspector3 = slEntityRepo.findOneByAccountNo(inspector3AccountNo);

                        data.put("inspector1", inspector1);
                        data.put("inspector2", inspector2);
                        data.put("inspector3", inspector3);
                    }
                    break;
                }
                case "PROJECT_ACCEPTANCE_CERTIFICATION": {

                    Map codeMap = settingFacade.getByCode(this.PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvingOfficerAccountNo = codeMap.get("approvingOfficerAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvingOfficerAccountNo").toString());
                        Integer recommendingOfficerAccountNo = codeMap.get("recommendingOfficerAccountNo") == null ? 0:Integer.parseInt(codeMap.get("recommendingOfficerAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvingOfficerAccountNo);
                        SlEntity recommendingOfficer = slEntityRepo.findOneByAccountNo(recommendingOfficerAccountNo);

                        data.put("approvingOfficer", approvingOfficer);
                        data.put("recommendedBy", recommendingOfficer);

                    }
                    break;
                }
                case "MRTE": {
                    Map codeMap = settingFacade.getByCode(this.MEMORANDUM_RECEIPT_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                    }
                    break;
                }
                case "BUDGET_LINE_ITEM": {

                    Map codeMap = settingFacade.getByCode(this.BUDGET_LINE_ITEM_SIGNATORIES);
                    if (codeMap == null) {
                        break;
                    }

                    Integer checkedByAccountNo  = parseInteger(codeMap.get("checkedByAccountNo"));
                    Integer verifiedByAccountNo = parseInteger(codeMap.get("verifiedByAccountNo"));
                    Integer approvedByAccountNo = parseInteger(codeMap.get("approvedByAccountNo"));

                    Integer loggedInUserAccountNo =
                            authenticationFacade.getLoggedIn().getAccountNo();

                    Integer departmentManagerAccountNo =
                            employeeRepo.getDepartmentManagerAccountNoByLoggedInUser(loggedInUserAccountNo);

                    Employee employee =
                            employeeRepo.findOneByAccountNumber(loggedInUserAccountNo);

                    // Determine checking account number based on department
                    Integer checkingAccountNo = employee.getDepartment().getId().equals(GlobalConstant.DEPARTMENT_OGM)
                            ? checkedByAccountNo
                            : departmentManagerAccountNo;

                    SlEntity checkingOfficer  = findSlEntity(checkingAccountNo);
                    SlEntity verifyingOfficer = findSlEntity(verifiedByAccountNo);
                    SlEntity approvingOfficer = findSlEntity(approvedByAccountNo);

                    data.put("checkedBy", checkingOfficer);
                    data.put("verifiedBy", verifyingOfficer);
                    data.put("approvedBy", approvingOfficer);

                    break;
                }
                case "PETTY_CASH_LIQUIDATION": {
                    Map codeMap = settingFacade.getByCode(this.PETTY_CASH_LIQUIDATION_SIGNATORIES);
                    if (codeMap != null) {

                        Integer approvedByAccountNo = codeMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("approvedByAccountNo").toString());
                        Integer receivedByAccountNo = codeMap.get("receivedByAccountNo") == null ? 0:Integer.parseInt(codeMap.get("receivedByAccountNo").toString());

                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                        SlEntity receivedBy = slEntityRepo.findOneByAccountNo(receivedByAccountNo);

                        data.put("approvedBy", approvingOfficer);
                        data.put("receivedBy", receivedBy);
                    }
                    break;
                }
                case "CCPR": {
                    Map codeMap = settingFacade.getByCode(this.CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES);
                    if (codeMap != null) {

                        Integer recommendedByAccountNo = Integer.parseInt(codeMap.get("recommendedByAccountNo").toString());
                        Integer approvedByAccountNo = Integer.parseInt(codeMap.get("approvedByAccountNo").toString());

                        SlEntity recommendingOfficer = slEntityRepo.findOneByAccountNo(recommendedByAccountNo);
                        SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);

                        data.put("recommendingOfficer", recommendingOfficer);
                        data.put("approvingOfficer", approvingOfficer);

                    }
                    break;
                }
            }
        }
        return data;
    }



    @Override
    public void miv(MaterialIssueRegister issueRegister) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", issueRegister.getApprovingOfficer().getAccountNo());
            map.put("recApprovedByAccountNo", issueRegister.getRecommendingOfficer().getAccountNo());
            map.put("checkedByAccountNo", issueRegister.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.MIV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(issueRegister.getCreatedBy());
                setting.setCode(this.MIV_SIGNATORIES);
                setting.setDescription(this.MIV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void pcvSummary(Integer checkedByAcctNo, Integer replenishedByAcctNo, User createdBy) {
        try {
            Map<String,Integer> map = new HashMap<>();

            Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());

            Integer officeId = 0;

            if(employee != null){
                if(employee.getOffice() != null){
                    officeId = employee.getOffice().getId();
                }
            }

            map.put("replenishedByAcctNo", replenishedByAcctNo);
            map.put("checkedByAccountNo", checkedByAcctNo);

            Map codeMap = settingFacade.getByCode(this.PCV_SUMMARY_SIGNATORIES);
            if(codeMap == null) {
                codeMap = new HashMap();
            }
            codeMap.put(officeId.toString(), map);

            String settingValue = new ObjectMapper().writeValueAsString(codeMap);

            Setting exSetting = settingRepo.findOneByCode(this.PCV_SUMMARY_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(createdBy);
                setting.setCode(this.PCV_SUMMARY_SIGNATORIES);
                setting.setDescription(this.PCV_SUMMARY_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void pcv(PettyCashTrans pettyCashVoucher) {
        try {

            Integer officeId = pettyCashVoucher.getOffice().getId();

            Map<String,Integer> map = new HashMap<>();
            map.put("approvedByAccountNo", pettyCashVoucher.getApprovingOfficer().getAccountNo());
            map.put("checkedByAccountNo", pettyCashVoucher.getChecker().getAccountNo());
            map.put("releasedByAccountNo", pettyCashVoucher.getReleasingOfficer().getAccountNo());

            Map codeMap = settingFacade.getByCode(this.PCV_SIGNATORIES);
            if(codeMap == null) {
                codeMap = new HashMap();
            }
            codeMap.put(officeId.toString(), map);

            String settingValue = new ObjectMapper().writeValueAsString(codeMap);

            Setting exSetting = settingRepo.findOneByCode(this.PCV_SIGNATORIES);
            if (exSetting != null) {

                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(pettyCashVoucher.getCreatedBy());
                setting.setCode(this.PCV_SIGNATORIES);
                setting.setDescription(this.PCV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void pettyCashLiquidation(PettyCashLiquidation pettyCashLiquidation) {
        try {
            Integer officeId = pettyCashLiquidation.getOffice().getId();

            Map<String,Integer> map = new HashMap<>();
            map.put("approvedByAccountNo", pettyCashLiquidation.getApprovingOfficer().getAccountNo());
            map.put("receivedByAccountNo", pettyCashLiquidation.getReceivingOfficer().getAccountNo());

            Map codeMap = settingFacade.getByCode(this.PETTY_CASH_LIQUIDATION_SIGNATORIES);
            if(codeMap == null) {
                codeMap = new HashMap();
            }
            codeMap.put(officeId.toString(), map);

            String settingValue = new ObjectMapper().writeValueAsString(codeMap);

            Setting exSetting = settingRepo.findOneByCode(this.PETTY_CASH_LIQUIDATION_SIGNATORIES);
            if (exSetting != null) {

                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(pettyCashLiquidation.getCreatedBy());
                setting.setCode(this.PETTY_CASH_LIQUIDATION_SIGNATORIES);
                setting.setDescription(this.PETTY_CASH_LIQUIDATION_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void budget(Budget budget) {
        try {
            Map<String,Integer> map = new HashMap<>();

//            map.put("approvedByAccountNo", budget.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.BUDG_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(budget.getCreatedBy());
                setting.setCode(this.BUDG_SIGNATORIES);
                setting.setDescription(this.BUDG_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


   /* @Override
    public void bad(BankDeposit bankDeposit) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", bankDeposit.getApprovingOfficer().getAccountNo());
            map.put("checkedByAccountNo", bankDeposit.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.BAD_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(bankDeposit.getCreatedBy());
                setting.setCode(this.BAD_SIGNATORIES);
                setting.setDescription(this.BAD_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/


    @Override
    public void sv(SalesVoucher salesVoucher) { // energy sales
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", salesVoucher.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(salesVoucher.getCreatedBy());
                setting.setCode(this.SV_SIGNATORIES);
                setting.setDescription(this.SV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void crv(CashReceipts crv) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", crv.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CRV_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(crv.getCreatedBy());
                setting.setCode(this.CRV_SIGNATORIES);
                setting.setDescription(this.CRV_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void ca(CashAdvance cashAdvance) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", cashAdvance.getApprovingOfficer().getAccountNo());
            map.put("recommendedByAccountNo", cashAdvance.getRecommendedBy().getAccountNo());
            map.put("budgetOfficerAccountNo", cashAdvance.getBudgetOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CA_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(cashAdvance.getCreatedBy());
                setting.setCode(this.CA_SIGNATORIES);
                setting.setDescription(this.CA_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void cal(CashAdvanceLiquidation cashAdvanceLiquidation) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", cashAdvanceLiquidation.getApprovingOfficer().getAccountNo());
            map.put("recommendedByAccountNo", cashAdvanceLiquidation.getRecommendedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CAL_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(cashAdvanceLiquidation.getCreatedBy());
                setting.setCode(this.CAL_SIGNATORIES);
                setting.setDescription(this.CAL_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void pr(PaymentRequest paymentRequest) {
        try {
            Map<String,Integer> map = new HashMap<>();

//            map.put("approvedByAccountNo", paymentRequest.getApprovingOfficer().getAccountNo());
//            map.put("checkedByAccountNo", paymentRequest.getChecker() == null? 0 :paymentRequest.getChecker().getAccountNo());
//            map.put("notedByAccountNo", paymentRequest.getNotedBy() == null? 0 :paymentRequest.getNotedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.PR_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(paymentRequest.getCreatedBy());
                setting.setCode(this.PR_SIGNATORIES);
                setting.setDescription(this.PR_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void cashflowStatement(Integer checkedByAcctNo, Integer notedByAcctNo, User createdBy) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("notedByAcctNo", notedByAcctNo);
            map.put("checkedByAccountNo", checkedByAcctNo);

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CASHFLOW_STATEMENT_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(createdBy);
                setting.setCode(this.CASHFLOW_STATEMENT_SIGNATORIES);
                setting.setDescription(this.CASHFLOW_STATEMENT_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void birForm1601E(Integer accountNo, User createdBy) {
        try {
            Map<String,Map> map = new HashMap<>();

            Employee employee = employeeRepo.findOneByAccountNumber(accountNo);

            Map taxPayerMap = new HashMap();
            taxPayerMap.put("accountNo", accountNo);
            taxPayerMap.put("name", employee.getName());
            taxPayerMap.put("position", employee.getPosition() != null ? employee.getPosition().getName() : "");
            taxPayerMap.put("tin", StringFormatter.getValueOrBlank(employee.getTin()));

            map.put("taxPayer", taxPayerMap);

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.BIR_FORM_1601E_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(createdBy);
                setting.setCode(this.BIR_FORM_1601E_SIGNATORIES);
                setting.setDescription(this.BIR_FORM_1601E_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void rr(ReceivingReport rr) {

        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", rr.getApprovingOfficer().getAccountNo());
            map.put("checkedByAccountNo", rr.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.RR_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(rr.getCreatedBy());
                setting.setCode(this.RR_SIGNATORIES);
                setting.setDescription(this.RR_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void glInquirySummary(Integer accountNo, User createdBy) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("checkedByAccountNo", accountNo);

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.GL_INQUIRY_SUMMARY_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(createdBy);
                setting.setCode(this.GL_INQUIRY_SUMMARY_SIGNATORIES);
                setting.setDescription(this.GL_INQUIRY_SUMMARY_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sw(StockWithdrawal sw) {

        try {

            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", sw.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SW_SIGNATORIES);

            if (exSetting != null) {

                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);

            } else {

                Setting setting = new Setting();
                setting.setCreatedBy(sw.getCreatedBy());
                setting.setCode(this.SW_SIGNATORIES);
                setting.setDescription(this.SW_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sr(StockRelease sr) {

        try {

            Map<String,Integer> map = new HashMap<>();

//            map.put("approvedByAccountNo", sr.getApprovingOfficer().getAccountNo());

            map.put("receivedByAccountNo", sr.getReceivedBy().getAccountNo());
            if(sr.getAuditor() != null) {
                map.put("auditedByAccountNo", sr.getAuditor().getAccountNo());
            }

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SR_SIGNATORIES);

            if (exSetting != null) {

                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);

            } else {

                Setting setting = new Setting();
                setting.setCreatedBy(sr.getCreatedBy());
                setting.setCode(this.SR_SIGNATORIES);
                setting.setDescription(this.SR_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mct(MaterialCreditTicket materialCreditTicket) {

        try {

            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", materialCreditTicket.getApprovingOfficer().getAccountNo());
            map.put("requestedByAccountNo", materialCreditTicket.getRequester().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.MCT_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(materialCreditTicket.getCreatedBy());
                setting.setCode(this.MCT_SIGNATORIES);
                setting.setDescription(this.MCT_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sa(StockAdjustment stockAdjustment) {

        try {

            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", stockAdjustment.getApprovingOfficer().getAccountNo());
            map.put("checkedByAccountNo", stockAdjustment.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SA_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(stockAdjustment.getCreatedBy());
                setting.setCode(this.SA_SIGNATORIES);
                setting.setDescription(this.SA_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mst(MaterialSalvageTicket materialSalvageTicket) {
        try {

            Map<String,Integer> map = new HashMap<>();

            map.put("returnedByAccountNo", materialSalvageTicket.getReturnedBy().getAccountNo());
            map.put("receivedByAccountNo", materialSalvageTicket.getReceivedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.MST_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(materialSalvageTicket.getCreatedBy());
                setting.setCode(this.MST_SIGNATORIES);
                setting.setDescription(this.MST_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void st(StockTransfer stockTransfer) {
        try {

            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", stockTransfer.getApprovingOfficer().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.ST_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(stockTransfer.getCreatedBy());
                setting.setCode(this.ST_SIGNATORIES);
                setting.setDescription(this.ST_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void src(StockReceive src) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", src.getApprovingOfficer().getAccountNo());
            map.put("checkedByAccountNo", src.getCheckedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SRC_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {

                Setting setting = new Setting();
                setting.setCreatedBy(src.getCreatedBy());
                setting.setCode(this.SRC_SIGNATORIES);
                setting.setDescription(this.SRC_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void summaryOfQuotation(Integer validatedByAcctNo, Integer approvedByAccountNo, User createdBy) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("validatedByAcctNo", validatedByAcctNo);
            map.put("approvedByAccountNo", approvedByAccountNo);

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.QUOTATION_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(createdBy);
                setting.setCode(this.QUOTATION_SIGNATORIES);
                setting.setDescription(this.QUOTATION_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void siteInspectionReport(SiteInspectionReport report) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("notedByAccountNo", report.getNotedBy().getAccountNo());
            map.put("checkedByAccountNo", report.getChecker().getAccountNo());
            map.put("approvedByAccountNo", report.getApprovedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.SITE_INSPECTION_REPORT_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(report.getCreatedBy());
                setting.setCode(this.SITE_INSPECTION_REPORT_SIGNATORIES);
                setting.setDescription(this.SITE_INSPECTION_REPORT_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void ce(CostEstimate costEstimate) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", costEstimate.getApprovingOfficer().getAccountNo());
            map.put("recommendedByAccountNo", costEstimate.getRecommendedBy().getAccountNo());
            map.put("checkedByAccountNo", costEstimate.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CE_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {

                Setting setting = new Setting();
                setting.setCreatedBy(costEstimate.getCreatedBy());
                setting.setCode(this.CE_SIGNATORIES);
                setting.setDescription(this.CE_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void bom(BillOfMaterial billOfMaterial) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvedByAccountNo", billOfMaterial.getApprovingOfficer().getAccountNo());
            map.put("recommendedByAccountNo", billOfMaterial.getRecommendedBy().getAccountNo());
            map.put("checkedByAccountNo", billOfMaterial.getChecker().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.BILL_OF_MATERIAL_SIGNATORIES);

            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {

                Setting setting = new Setting();
                setting.setCreatedBy(billOfMaterial.getCreatedBy());
                setting.setCode(this.BILL_OF_MATERIAL_SIGNATORIES);
                setting.setDescription(this.BILL_OF_MATERIAL_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void projectAcceptanceReport(ProjectAcceptanceReport projectAcceptanceReport) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("inspector1AccountNo", projectAcceptanceReport.getInspector1().getAccountNo());
            map.put("inspector2AccountNo", projectAcceptanceReport.getInspector2().getAccountNo());
            map.put("inspector3AccountNo", projectAcceptanceReport.getInspector3().getAccountNo());
            map.put("notedByAccountNo", projectAcceptanceReport.getNotedBy().getAccountNo());
            map.put("recommendedByAccountNo", projectAcceptanceReport.getRecommendedBy().getAccountNo());
            map.put("approvedByAccountNo", projectAcceptanceReport.getApprovedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.PROJECT_ACCEPTANCE_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(projectAcceptanceReport.getCreatedBy());
                setting.setCode(this.PROJECT_ACCEPTANCE_SIGNATORIES);
                setting.setDescription(this.PROJECT_ACCEPTANCE_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void projectAcceptanceCertification(ProjectAcceptanceCertification projectAcceptanceCertification) {
        try {
            Map<String,Integer> map = new HashMap<>();

            map.put("approvingOfficerAccountNo", projectAcceptanceCertification.getApprovingOfficer().getAccountNo());
            map.put("recommendingOfficerAccountNo", projectAcceptanceCertification.getRecommendedBy().getAccountNo());

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(projectAcceptanceCertification.getCreatedBy());
                setting.setCode(this.PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES);
                setting.setDescription(this.PROJECT_ACCEPTANCE_CERTIFICATION_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void budgetLineItem(BudgetLineItem budgetLineItem) {

        try {

            Map<String,Integer> map = new HashMap<>();

            if(budgetLineItem.getCheckedBy() != null) {
                map.put("checkedByAccountNo", budgetLineItem.getCheckedBy().getAccountNo());
            }

            if(budgetLineItem.getVerifiedBy() != null) {
                map.put("verifiedByAccountNo", budgetLineItem.getVerifiedBy().getAccountNo());
            }

            if(budgetLineItem.getApprovingOfficer() != null) {
                map.put("approvedByAccountNo", budgetLineItem.getApprovingOfficer().getAccountNo());
            }

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.BUDGET_LINE_ITEM_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(budgetLineItem.getCreatedBy());
                setting.setCode(this.BUDGET_LINE_ITEM_SIGNATORIES);
                setting.setDescription(this.BUDGET_LINE_ITEM_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void memorandumReceipt(MemorandumReceipt memorandumReceipt) {

        try {

            Map<String,Integer> map = new HashMap<>();

            if(memorandumReceipt.getApprovingOfficer() != null) {
                map.put("approvedByAccountNo", memorandumReceipt.getApprovingOfficer().getAccountNo());
            }

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.MEMORANDUM_RECEIPT_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(memorandumReceipt.getCreatedBy());
                setting.setCode(this.MEMORANDUM_RECEIPT_SIGNATORIES);
                setting.setDescription(this.MEMORANDUM_RECEIPT_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void creditCardPurchaseRequest(CreditCardPurchaseRequest creditCardPurchaseRequest) {

        try {

            Map<String,Integer> map = new HashMap<>();

            if(creditCardPurchaseRequest.getRecommendingOfficer() != null) {
                map.put("recommendedByAccountNo", creditCardPurchaseRequest.getRecommendingOfficer().getAccountNo());
            }

            if(creditCardPurchaseRequest.getApprovingOfficer() != null) {
                map.put("approvedByAccountNo", creditCardPurchaseRequest.getApprovingOfficer().getAccountNo());
            }

            String settingValue = new ObjectMapper().writeValueAsString(map);

            Setting exSetting = settingRepo.findOneByCode(this.CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES);
            if (exSetting != null) {
                exSetting.setValue(settingValue);
                exSetting.setUpdatedAt(new Date());
                settingRepo.save(exSetting);
            } else {
                Setting setting = new Setting();
                setting.setCreatedBy(creditCardPurchaseRequest.getCreatedBy());
                setting.setCode(this.CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES);
                setting.setDescription(this.CREDIT_CARD_PURCHASE_REQUEST_SIGNATORIES_DESC);
                setting.setValue(settingValue);
                setting.setCreatedAt(new Date());
                setting.setUpdatedAt(new Date());

                settingRepo.save(setting);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public Map defaultSignatories(DocumentType documentType, Map params) {
        Map data = new HashMap();
        if (documentType != null) {
            String documentTypeCode = documentType.getCode();
            switch (documentTypeCode) {
                case "PCV": {
                    Map codeMap = settingFacade.getByCode(this.PCV_SIGNATORIES);
                    if (codeMap != null) {

                        Object officeIdObj = params.get("officeId");
                        if(officeIdObj != null) {

                            Map signMap = (Map) codeMap.get(officeIdObj.toString());

                            if(signMap != null) {

                                Integer checkedByAccountNo = signMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(signMap.get("checkedByAccountNo").toString());
                                Integer approvedByAccountNo = signMap.get("approvedByAccountNo") == null ? 0:Integer.parseInt(signMap.get("approvedByAccountNo").toString());
                                Integer releasedByAccountNo = signMap.get("releasedByAccountNo") == null ? 0:Integer.parseInt(signMap.get("releasedByAccountNo").toString());

                                SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                                SlEntity releasingOfficer = slEntityRepo.findOneByAccountNo(releasedByAccountNo);
                                SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                                data.put("approvedBy", approvingOfficer);
                                data.put("checkedBy", checkedBy);
                                data.put("releasedBy", releasingOfficer);
                            }
                        }
                    }
                    break;
                }
                case "PETTY_CASH_LIQUIDATION": {
                    Map codeMap = settingFacade.getByCode(this.PETTY_CASH_LIQUIDATION_SIGNATORIES);
                    if (codeMap != null) {

                        Object officeIdObj = params.get("officeId");
                        if(officeIdObj != null) {

                            Map signMap = (Map) codeMap.get(officeIdObj.toString());

                            if(signMap != null) {

                                Integer approvedByAccountNo = signMap.get("approvedByAccountNo") == null ? 0 : Integer.parseInt(signMap.get("approvedByAccountNo").toString());
                                Integer receivedByAccountNo = signMap.get("receivedByAccountNo") == null ? 0 : Integer.parseInt(signMap.get("receivedByAccountNo").toString());

                                SlEntity approvingOfficer = slEntityRepo.findOneByAccountNo(approvedByAccountNo);
                                SlEntity receivedBy = slEntityRepo.findOneByAccountNo(receivedByAccountNo);

                                data.put("approvedBy", approvingOfficer);
                                data.put("receivedBy", receivedBy);
                            }
                        }
                    }
                    break;
                }
                case "PCV_SUMMARY": {
                    Map codeMap = settingFacade.getByCode(this.PCV_SUMMARY_SIGNATORIES);
                    if (codeMap != null) {

                        Object officeIdObj = params.get("officeId");
                        if(officeIdObj != null) {

                            Map signMap = (Map) codeMap.get(officeIdObj.toString());

                            if(signMap != null) {

                                Integer checkedByAccountNo = signMap.get("checkedByAccountNo") == null ? 0:Integer.parseInt(signMap.get("checkedByAccountNo").toString());
                                Integer replenishedByAcctNo = signMap.get("replenishedByAcctNo") == null ? 0:Integer.parseInt(signMap.get("replenishedByAcctNo").toString());

                                SlEntity replenishedBy = slEntityRepo.findOneByAccountNo(replenishedByAcctNo);
                                SlEntity checkedBy = slEntityRepo.findOneByAccountNo(checkedByAccountNo);

                                data.put("replenishedBy", replenishedBy);
                                data.put("checkedBy", checkedBy);

                            }
                        }
                    }
                    break;
                }
            }
        }

        return data;
    }

    private Integer parseInteger(Object value) {
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    private SlEntity findSlEntity(Integer accountNo) {
        return accountNo != null ? slEntityRepo.findOneByAccountNo(accountNo) : null;
    }

}
