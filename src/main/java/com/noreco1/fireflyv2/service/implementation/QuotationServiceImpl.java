package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.QuotationDetail;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.QuotationDetailService;
import com.noreco1.fireflyv2.service.QuotationService;
import com.noreco1.fireflyv2.validator.QuotationValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import static java.util.Collections.*;

@Service(value = "quotationServiceImpl")
public class QuotationServiceImpl implements QuotationService, PrintableVoucher {

    private Quotation model;

    @Autowired
    QuotationRepo quotationRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    QuotationDetailRepo quotationDetailRepo;

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    QuotationDetailService quotationDetailService;

    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    QuotationItemRepo quotationItemRepo;

    @Autowired
    QuotationItemDetailRepo quotationItemDetailRepo;

    @Autowired
    PurchaseRequestDetailRepo purchaseRequestDetailRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    QuotationTermRepo quotationTermRepo;

    @Autowired
     SlEntityRepo slEntityRepo;

    Map meta = new HashMap();

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public QuotationDto findById(Integer quotationId) {

        QuotationDto quotationDto = new QuotationDto();

        try {

            Quotation quotation =  quotationRepo.findById(quotationId).orElse(null);

            if (quotation != null) {
                quotationDto.setId(quotation.getId());
                quotationDto.setCode(quotation.getCode());
                quotationDto.setTransId(quotation.getTransaction().getId());
                quotationDto.setDate(quotation.getDate());
                quotationDto.setParticular(quotation.getParticular());
                quotationDto.setPurchaseRequest(quotation.getPurchaseRequest());
                quotationDto.setDocumentStatus(quotation.getDocumentStatus().getStatus());
                quotationDto.setPreparedBy(quotation.getCreatedBy().getFullName());
                quotationDto.setApprovedByFinanceManager(quotation.getApprovingOfficer() == null ? "" : quotation.getApprovingOfficer().getFullName());
                quotationDto.setApprovedByGeneralManager(quotation.getApprovedByGeneralManager() == null ? "" : quotation.getApprovedByGeneralManager().getFullName());
                if (quotation.getApprovingOfficer() != null) {
                    Map<String, Object> officerMap = new HashMap<>();
                    officerMap.put("accountNo", quotation.getApprovingOfficer().getAccountNo());
                    officerMap.put("fullName", quotation.getApprovingOfficer().getFullName());
                    quotationDto.setApprovingOfficerObj(officerMap);
                }
                if (quotation.getApprovedByGeneralManager() != null) {
                    Map<String, Object> gmMap = new HashMap<>();
                    gmMap.put("accountNo", quotation.getApprovedByGeneralManager().getAccountNo());
                    gmMap.put("fullName", quotation.getApprovedByGeneralManager().getFullName());
                    quotationDto.setGeneralManagerObj(gmMap);
                }
                quotationDto.setCreatedAt(quotation.getCreatedAt());
                quotationDto.setUpdatedAt(quotation.getUpdatedAt());

                quotationDto.setSuppliers(this.quotationItemDetailRepo.findDistinctSuppliersByQuotationId(quotationId));

                List<QuotationTerm> quotationTerms = this.getTerms(quotationId);
                quotationDto.setTerms(quotationTerms);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return  quotationDto;

    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationListDto> findAll() {
        List<Quotation> quotations = quotationRepo.findAll();

        List<QuotationListDto> returnQuotations = new ArrayList<>();
        if (!Checker.collectionIsEmpty(quotations)) {
            for(Quotation quotation : quotations) {
                QuotationListDto quotationListDto = new QuotationListDto();
                quotationListDto.setId(quotation.getId());
                quotationListDto.setCode(quotation.getCode());
                quotationListDto.setDate(quotation.getDate());
                quotationListDto.setRequisitionVoucherCode(getPrCodesForQuotation(quotation.getId()));
                quotationListDto.setDocumentStatus(quotation.getDocumentStatus().getStatus());

                quotationListDto.setPreparedBy(quotation.getCreatedBy().getFullName());

                returnQuotations.add(quotationListDto);
            }

            return returnQuotations;
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<QuotationListDto> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            List<Quotation> quotations = quotationRepo.findByDocumentStatusIdAndDateBetween(id, fromDate, toDate);

            List<QuotationListDto> returnQuotations = new ArrayList<>();
            if (!Checker.collectionIsEmpty(quotations)) {
                for(Quotation quotation : quotations) {
                    QuotationListDto quotationListDto = new QuotationListDto();
                    quotationListDto.setId(quotation.getId());
                    quotationListDto.setCode(quotation.getCode());
                    quotationListDto.setDate(quotation.getDate());
                    quotationListDto.setRequisitionVoucherCode(getPrCodesForQuotation(quotation.getId()));
                    quotationListDto.setDocumentStatus(quotation.getDocumentStatus().getStatus());

                    quotationListDto.setPreparedBy(quotation.getCreatedBy().getFullName());

                    returnQuotations.add(quotationListDto);
                }

                return returnQuotations;
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<QuotationListDto> findByDateRangePending(String from, String to) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new java.util.Date(0);
            }

            if (toDate == null) {
                toDate = new java.util.Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<Quotation> quotations = quotationRepo.findByDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));

            List<QuotationListDto> returnQuotations = new ArrayList<>();
            if (!Checker.collectionIsEmpty(quotations)) {
                for(Quotation quotation : quotations) {
                    QuotationListDto quotationListDto = new QuotationListDto();
                    quotationListDto.setId(quotation.getId());
                    quotationListDto.setCode(quotation.getCode());
                    quotationListDto.setDate(quotation.getDate());
                    quotationListDto.setRequisitionVoucherCode(getPrCodesForQuotation(quotation.getId()));
                    quotationListDto.setDocumentStatus(quotation.getDocumentStatus().getStatus());

                    quotationListDto.setPreparedBy(quotation.getCreatedBy().getFullName());

                    returnQuotations.add(quotationListDto);
                }

                return returnQuotations;
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<QuotationListDto> findByDateRange(String from, String to) {
        try {
            java.util.Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            java.util.Date toDate   = DateHelper.strToDate(to,   "yyyy-MM-dd");

            if (fromDate == null) fromDate = new java.util.Date(0);
            if (toDate   == null) toDate   = new java.util.Date();

            List<Quotation> quotations = quotationRepo.findAllByDateRange(fromDate, toDate);

            List<QuotationListDto> returnQuotations = new ArrayList<>();
            if (!Checker.collectionIsEmpty(quotations)) {
                for (Quotation quotation : quotations) {
                    QuotationListDto dto = new QuotationListDto();
                    dto.setId(quotation.getId());
                    dto.setCode(quotation.getCode());
                    dto.setDate(quotation.getDate());
                    dto.setRequisitionVoucherCode(getPrCodesForQuotation(quotation.getId()));
                    dto.setDocumentStatus(quotation.getDocumentStatus().getStatus());
                    dto.setPreparedBy(quotation.getCreatedBy().getFullName());
                    returnQuotations.add(dto);
                }
            }
            return returnQuotations;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> getForQuotationSummary(Integer id) {

        Quotation quotation = quotationRepo.findById(id).orElse(null);

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> data = new ArrayList<>();

        List<QuotationDetail> quotationDetails = quotationDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(quotation.getPurchaseRequest().getId());

        Map detailsMap = new HashMap<>();
        Map suppliersIndexMap = new HashMap<>();

        if (!quotationDetails.isEmpty()) {

            com.noreco1.fireflyv2.controller.response.reports.QuotationDetail quotationDetailSuppliers = new com.noreco1.fireflyv2.controller.response.reports.QuotationDetail();
            int supplierCounter = 0;
            for (int idx=0; idx<quotationDetails.size(); idx++) {

                QuotationDetail quotationDetail = quotationDetails.get(idx);

                Supplier supplier = quotationDetail.getSupplier();
                if(supplier == null) continue; // Canvass havent accomplished by supplier yet

                // check if unique
                Object sIdx = suppliersIndexMap.get(quotationDetail.getSupplier().getAccountNumber());
                if(sIdx == null) {

                    if(supplierCounter == 0) {
                        quotationDetailSuppliers.setSupplier1(supplier.getName());
                    }
                    else if(supplierCounter == 1) {
                        quotationDetailSuppliers.setSupplier2(supplier.getName());
                    }
                    else if(supplierCounter == 2) {
                        quotationDetailSuppliers.setSupplier3(supplier.getName());
                    }
                    else if(supplierCounter == 3) {
                        quotationDetailSuppliers.setSupplier4(supplier.getName());
                    }

                    suppliersIndexMap.put(supplier.getAccountNumber(), supplierCounter);
                    supplierCounter++;
                }

                if(supplierCounter == 4) break; // up 4 suppliers only
            }

            if(supplierCounter > 0) data.add(quotationDetailSuppliers);  // suppliers only

            // items
            for (QuotationDetail line : quotationDetails) {

                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
                if(o != null) {
                    QuotationDetail detailFromMap  = (QuotationDetail) o;

                    if(detailFromMap.getPriceSupplier2() == null || detailFromMap.getPriceSupplier2().compareTo(BigDecimal.ZERO) == 0) {

                        detailFromMap.setPriceSupplier2(line.getPrice());

                    } else if(detailFromMap.getPriceSupplier3() == null || detailFromMap.getPriceSupplier3().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier3(line.getPrice());
                    } else if(detailFromMap.getPriceSupplier4() == null || detailFromMap.getPriceSupplier4().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier4(line.getPrice());
                    }

                    detailsMap.put(line.getPurchaseRequestDetail().getId(), detailFromMap); // put back to the map

                } else {
                    line.setPriceSupplier1(line.getPrice());
                    detailsMap.put(line.getPurchaseRequestDetail().getId(), line);
                }
            }

            int counter = 1;
            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                QuotationDetail quotationDetail = (QuotationDetail) pair.getValue();

                com.noreco1.fireflyv2.controller.response.reports.QuotationDetail quotationDetailPrices = new com.noreco1.fireflyv2.controller.response.reports.QuotationDetail();

                quotationDetailPrices.setId(counter++);
                quotationDetailPrices.setDescription(quotationDetail.getPurchaseRequestDetail().getItem() != null ? quotationDetail.getPurchaseRequestDetail().getItem().getDescription():quotationDetail.getPurchaseRequestDetail().getJoDescription());
                quotationDetailPrices.setQuantity(quotationDetail.getPurchaseRequestDetail().getQuantity());

                if(quotationDetail.getPriceSupplier1() != null) {
                    quotationDetailPrices.setSupplier1(decimalFormat.format(quotationDetail.getPriceSupplier1()));
                }
                if(quotationDetail.getPriceSupplier2() != null) {
                    quotationDetailPrices.setSupplier2(decimalFormat.format(quotationDetail.getPriceSupplier2()));
                }

                if(quotationDetail.getPriceSupplier3() != null) {
                    quotationDetailPrices.setSupplier3(decimalFormat.format(quotationDetail.getPriceSupplier3()));
                }

                if(quotationDetail.getPriceSupplier4() != null) {
                    quotationDetailPrices.setSupplier4(decimalFormat.format(quotationDetail.getPriceSupplier4()));
                }

                data.add(quotationDetailPrices);
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> getRvDetailByRvId(Integer rivId) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> data = new ArrayList<>();

        List<CanvassDetail> canvassDetails = canvassDetailRepo.findForQoutationByPurchaseRequestId(rivId);

        Map detailsMap = new HashMap<>();
        Map suppliersIndexMap = new HashMap<>();

        if (!canvassDetails.isEmpty()) {

            com.noreco1.fireflyv2.controller.response.reports.QuotationDetail quotationDetailSuppliers = new com.noreco1.fireflyv2.controller.response.reports.QuotationDetail();
            int supplierCounter = 0;
            for (int idx=0; idx<canvassDetails.size(); idx++) {

                CanvassDetail canvassDetail = canvassDetails.get(idx);

                Supplier supplier = canvassDetail.getSupplier();
                if(supplier == null) continue; // Canvass havent accomplished by supplier yet

                // check if unique
                Object sIdx = suppliersIndexMap.get(canvassDetail.getSupplier().getAccountNumber());
                if(sIdx == null) {

                    if(supplierCounter == 0) {
                        quotationDetailSuppliers.setSupplier1(supplier.getName());
                    }
                    else if(supplierCounter == 1) {
                        quotationDetailSuppliers.setSupplier2(supplier.getName());
                    }
                    else if(supplierCounter == 2) {
                        quotationDetailSuppliers.setSupplier3(supplier.getName());
                    }

                    suppliersIndexMap.put(supplier.getAccountNumber(), supplierCounter);
                    supplierCounter++;
                }

                if(supplierCounter == 3) break; // up 3 suppliers only
            }

            if(supplierCounter > 0) data.add(quotationDetailSuppliers);  // suppliers only

            // items
            for (CanvassDetail line : canvassDetails) {

                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
                if(o != null) {
                    CanvassDetail detailFromMap  = (CanvassDetail) o;

                    if(detailFromMap.getPriceSupplier2() == null || detailFromMap.getPriceSupplier2().compareTo(BigDecimal.ZERO) == 0) {

                        detailFromMap.setPriceSupplier2(line.getUnitPrice());

                    } else if(detailFromMap.getPriceSupplier3() == null || detailFromMap.getPriceSupplier3().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier3(line.getUnitPrice());
                    }

                    detailsMap.put(line.getPurchaseRequestDetail().getId(), detailFromMap); // put back to the map

                } else {
                    line.setPriceSupplier1(line.getUnitPrice());
                    detailsMap.put(line.getPurchaseRequestDetail().getId(), line);
                }
            }

            // when opening an RV for SOQ, the item listing should be the same as the sequence in RV
            List<Integer> sortedKeys = new ArrayList(detailsMap.keySet());
            sort(sortedKeys);

            int counter = 1;
            for(Integer key:sortedKeys) {
                Object o = detailsMap.get(key);

                CanvassDetail canvassDetail = (CanvassDetail) o;

                com.noreco1.fireflyv2.controller.response.reports.QuotationDetail quotationDetailPrices = new com.noreco1.fireflyv2.controller.response.reports.QuotationDetail();

                quotationDetailPrices.setId(counter++);
                quotationDetailPrices.setPurchaseRequestDetailId(canvassDetail.getPurchaseRequestDetail().getId());
                quotationDetailPrices.setDescription(canvassDetail.getPurchaseRequestDetail().getItem() != null ? canvassDetail.getPurchaseRequestDetail().getItem().getDescription():canvassDetail.getPurchaseRequestDetail().getJoDescription());
                quotationDetailPrices.setQuantity(canvassDetail.getPurchaseRequestDetail().getQuantity());
                quotationDetailPrices.setAvailable(true);

                if(canvassDetail.getPriceSupplier1() != null) {
                    quotationDetailPrices.setSupplier1(decimalFormat.format(canvassDetail.getPriceSupplier1()));
                }
                if(canvassDetail.getPriceSupplier2() != null) {
                    quotationDetailPrices.setSupplier2(decimalFormat.format(canvassDetail.getPriceSupplier2()));
                }

                if(canvassDetail.getPriceSupplier3() != null) {
                    quotationDetailPrices.setSupplier3(decimalFormat.format(canvassDetail.getPriceSupplier3()));
                }

                data.add(quotationDetailPrices);
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> getRvItemsForQuotation(Integer rivId) {

        List<com.noreco1.fireflyv2.controller.response.reports.QuotationDetail> data = new ArrayList<>();

        List<CanvassDetail> canvassDetails = canvassDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(rivId);
        if(Checker.collectionIsNotEmpty(canvassDetails)) {

            int counter = 1;
            for(CanvassDetail detail: canvassDetails) {

                com.noreco1.fireflyv2.controller.response.reports.QuotationDetail quotationDetailPrices = new com.noreco1.fireflyv2.controller.response.reports.QuotationDetail();

                quotationDetailPrices.setId(counter++);
                quotationDetailPrices.setDescription(detail.getPurchaseRequestDetail().getItem() != null ? detail.getPurchaseRequestDetail().getItem().getDescription():detail.getPurchaseRequestDetail().getJoDescription());
                quotationDetailPrices.setQuantity(detail.getPurchaseRequestDetail().getQuantity());
                quotationDetailPrices.setPurchaseRequestDetailId(detail.getPurchaseRequestDetail().getId());

                data.add(quotationDetailPrices);
            }

        }

        return data;
    }

    @Override
    public Map reportMeta() {
        return this.meta;
    }

    @Override
    public List<Map> datasourceAbstractOfQuotation(Integer id) {

        List<Map> data = new ArrayList<>();
        HashMap<String, List<Map>> awards = new HashMap<>();

        List<QuotationItem> quotationItemList = this.quotationItemRepo.findAllByQuotationId(id);
        if(Checker.collectionIsNotEmpty(quotationItemList)) {

            int itemCount = 0;
            for(QuotationItem quotationItem: quotationItemList) {

                PurchaseRequestDetail purchaseRequestDetail = quotationItem.getPurchaseRequestDetail();

                if(purchaseRequestDetail != null) {

                    Map row = new HashMap();

                    row.put("count", ++itemCount);
                    row.put("quantity", purchaseRequestDetail.getQuantity());
                    row.put("unit", purchaseRequestDetail.getUnitMeasure().getCode());
                    row.put("particulars", purchaseRequestDetail.getNewItem() == null ? purchaseRequestDetail.getJoDescription(): purchaseRequestDetail.getNewItem().getDescription());
//                    row.put("estimatedUnitPrice", purchaseRequestDetail.getEstimatedPrice());
//                    row.put("estimatedAmount", purchaseRequestDetail.getEstimatedPrice().multiply(rvDetail.getQuantity()));

                    // suppliers and prices
                    List<QuotationItemDetail> details = this.quotationItemDetailRepo.findAllByQuotationItemId(quotationItem.getId());

                    if(Checker.collectionIsNotEmpty(details)) {

                        int supplierCount = 1;

                        for(QuotationItemDetail quotationItemDetail: details) {

                            Supplier supplier = quotationItemDetail.getSupplier();

                            this.meta.put("SUPPLIER"+supplierCount, supplier.getName());
                            String baseKey = "supplier"+supplierCount++;

                            row.put(baseKey+"AwardedTo", quotationItemDetail.getIsAwarded());
                            row.put(baseKey+"Brand", quotationItemDetail.getBrand() != null ? quotationItemDetail.getBrand().getName() : "");
                            row.put(baseKey+"UnitCost", quotationItemDetail.getPrice());
                            row.put(baseKey+"Amount", quotationItemDetail.getPrice().multiply(purchaseRequestDetail.getQuantity()));

                            if(quotationItemDetail.getIsAwarded() != null && quotationItemDetail.getIsAwarded()) {
                                String key = supplier.getId()+"";

                                List<Map> awardedItemsBySupplier = awards.get(key);
                                if(Checker.collectionIsEmpty(awardedItemsBySupplier)) {
                                    awardedItemsBySupplier = new ArrayList<>();
                                }

                                Map itemBySupplierMap = new HashMap();
                                itemBySupplierMap.put("supplier", supplier.getName());
                                itemBySupplierMap.put("item", itemCount);
                                itemBySupplierMap.put("total", quotationItemDetail.getPrice().multiply(purchaseRequestDetail.getQuantity()));

                                awardedItemsBySupplier.add(itemBySupplierMap);

                                awards.put(key, awardedItemsBySupplier);
                            }
                        }

                    }
                    data.add(row);

                }
            }
        }

        // terms
        List<QuotationTerm> quotationTerms = this.quotationTermRepo.findAllByQuotationId(id);
        if(Checker.collectionIsNotEmpty(quotationTerms)) {
            int supplierCount = 1;

            for(QuotationTerm term: quotationTerms) {
                this.meta.put("TERMS"+supplierCount++, term);
            }
        }

        // awarded supplier and its items
        List<Map> awardedSupplierItems = new ArrayList<>();

        for (Map.Entry<String, List<Map>> entry : awards.entrySet()) {
            List<Map> listPerSupplier = entry.getValue();

            BigDecimal grandTotal = BigDecimal.ZERO;
            Map row = new HashMap();
            StringBuffer stringBuffer = new StringBuffer();

            for (Map line : listPerSupplier) {

                BigDecimal total = new BigDecimal(line.get("total").toString());
                String itemNumber =  line.get("item").toString();

                stringBuffer.append(itemNumber);
                stringBuffer.append(", ");
                grandTotal = grandTotal.add(total);

                row.put("supplier", line.get("supplier").toString());
            }

            row.put("item", stringBuffer.deleteCharAt(stringBuffer.length()-2));    // remove trailing ", "
            row.put("total", grandTotal);

            awardedSupplierItems.add(row);

        }

        this.meta.put("AWARDED_SUPPLIER_ITEMS_DS", new JRBeanCollectionDataSource(awardedSupplierItems));

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<QuotationTerm> getTerms(Integer id) {
        return this.quotationTermRepo.findAllByQuotationId(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getAwardedTermsByPr(Integer prId) {
        QuotationTerm term = quotationTermRepo.findAwardedTermByPurchaseRequestId(prId);
        if (term == null) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("termsOfPayment", term.getTermsOfPayment());
        result.put("placeOfDelivery", term.getPlaceOfDelivery());
        result.put("deliveryTimeAndCompletion", term.getDeliveryTimeAndCompletion());
        return result;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map getDefaultSignatoryMoreThen100k() {

        Map map = new HashMap();

        try {

            Quotation quotation = quotationRepo.findFirstByApprovedByGeneralManagerNotNullOrderByIdDesc();

            if(quotation != null){

                SlEntity approvedByGeneralManager = slEntityRepo.findOneByAccountNo(quotation.getApprovedByGeneralManager().getAccountNo());

                map.put("approvedByGeneralManager", approvedByGeneralManager);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return map;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map getDefaultSignatoryMoreThen300k() {

        Map map = new HashMap();

        try {

            Quotation quotation = quotationRepo.findFirstByApprovedByFinanceManagerNotNullOrderByIdDesc();

            if(quotation != null){

                SlEntity approvedByFinanceManager = slEntityRepo.findOneByAccountNo(quotation.getApprovedByFinanceManager().getAccountNo());

                map.put("approvedByFinanceManager", approvedByFinanceManager);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return map;

    }

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        Quotation quotation = quotationRepo.findById(postData.getDocumentId()).orElse(null);

        if (quotation != null) {
            // --- Item resolution gate ---
            List<QuotationItem> quotationItems = quotationItemRepo.findAllByQuotationId(quotation.getId());
            List<String> notChildItems = new ArrayList<>();
            List<String> notApprovedItems = new ArrayList<>();

            for (QuotationItem qi : quotationItems) {
                PurchaseRequestDetail prd = qi.getPurchaseRequestDetail();
                if (prd == null) continue;
                Item resolved = prd.getNewItem();
                if (resolved == null) continue;

                if (resolved.getParentItem() == null) {
                    notChildItems.add(resolved.getDescription());
                } else if (resolved.getDocumentStatus() == null
                        || !Integer.valueOf(7).equals(resolved.getDocumentStatus().getId())) {
                    notApprovedItems.add(resolved.getDescription());
                }
            }

            if (!notChildItems.isEmpty() || !notApprovedItems.isEmpty()) {
                StringBuilder msg = new StringBuilder("Cannot process: ");
                if (!notChildItems.isEmpty()) {
                    msg.append("parent items must be replaced with a child item: [")
                       .append(String.join(", ", notChildItems)).append("]. ");
                }
                if (!notApprovedItems.isEmpty()) {
                    msg.append("items pending approval: [")
                       .append(String.join(", ", notApprovedItems)).append("].");
                }
                response.setFailureMessage(msg.toString());
                return response;
            }
            // --- end gate ---

            // for logging
            Map oldMap = this.forLogMapMain(quotation);
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(quotation, quotation.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            quotation.setDocumentStatus(afterActionDocumentStatus);
            quotation.setUpdatedAt(null);
            quotation = quotationRepo.save(quotation);

            // for logging
            Map newMap = this.forLogMapMain(quotation);
            newMap.put("remarks", postData.getRemarks());

            if (quotation != null) {
                documentProcessingFacade.processAction(quotation.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(quotation.getTransaction(), authenticationFacade.getLoggedIn(), oldMap, newMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    @Transactional
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        Quotation quotation = (Quotation) v;
        return this.processCreate(quotation, bindingResult, messageSource);
    }

    @Override
    @Transactional
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        Quotation quotation = (Quotation) v;
        PostResponse response = new PostResponse();

        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        QuotationValidator validator = new QuotationValidator();
        validator.setService(this);
        validator.validate(quotation, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            User approvedByFinanceManager = quotation.getApprovingOfficer() != null ? userRepo.findOneByAccountNo(quotation.getApprovingOfficer().getAccountNo()):null;
            User approvedByGeneralManager = quotation.getApprovedByGeneralManager() != null ? userRepo.findOneByAccountNo(quotation.getApprovedByGeneralManager().getAccountNo()):null;

            Quotation existingQuotation;

            Integer quotationYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(quotation.getDate()));

            Boolean insertMode = quotation.getId() == null || quotation.getId() == 0;
            if (insertMode) { // insert mode
                Object latestCanvassCode = quotationRepo.findLatestQuotationCodeByYear(quotationYear);
                quotation.setCode(generatorFacade.voucherCodeNoOffice("SOQ", (latestCanvassCode == null ? "" : String.valueOf(latestCanvassCode)), quotation.getDate(), GlobalConstant.COUNTER_PAD_4));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                quotation.setDocumentStatus(documentStatus);

                quotation.setTransaction(generatorFacade.transaction());
                quotation.setCreatedBy(createdBy);
                existingQuotation = quotation;
            } else {
                existingQuotation = quotationRepo.findById(quotation.getId()).orElse(null);
            }
            // use for document logging (skip for insert: entity not in DB yet, findById(null) would poison the TX)
            Map oldMap = insertMode ? null : documentLoggerFacade.makeLog(existingQuotation);

            Workflow wf = new Workflow();

            if(quotation.getApprovedByGeneralManager() != null){
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.QUOTATION_SUMMARY_LEVEL_1.getId());
            } else {
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.QUOTATION_SUMMARY.getId());
            }

            existingQuotation.setWorkflow(wf);
            existingQuotation.setDate(quotation.getDate());
            existingQuotation.setParticular(quotation.getParticular());
            existingQuotation.setCreatedBy(authenticationFacade.getLoggedIn());
            existingQuotation.setApprovingOfficer(approvedByFinanceManager);
            existingQuotation.setApprovedByGeneralManager(approvedByGeneralManager);
            existingQuotation.setYear(quotationYear);

            this.model = quotationRepo.save(existingQuotation);

            if (this.model != null) {

                // start: update default signatories
                signatoryFacade.quotation(this.model);
                // end: update default signatories

                if (!insertMode) {
                    this.quotationItemDetailRepo.deleteByQuotationItemQuotationId(existingQuotation.getId());
                    this.quotationItemRepo.deleteByQuotationId(existingQuotation.getId());
                    this.quotationTermRepo.deleteByQuotationId(existingQuotation.getId());
                }

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                }

                ArrayList<QuotationItemDto> details = quotation.getQuotationDetails();
                for(QuotationItemDto detail: details) {

                    QuotationItem quotationItem = new QuotationItem();

                    quotationItem.setIsAvailable(detail.getAvailable());
                    quotationItem.setQuotation(this.model);
                    quotationItem.setPurchaseRequestDetail(new PurchaseRequestDetail(detail.getPurchaseRequestDetailId()));

                    quotationItemRepo.save(quotationItem);

                    if (detail.getNewItemId() != null && detail.getNewItemId() > 0) {
                        purchaseRequestDetailRepo.findById(detail.getPurchaseRequestDetailId()).ifPresent(prd -> {
                            prd.setNewItem(new Item(detail.getNewItemId().intValue()));
                            purchaseRequestDetailRepo.save(prd);
                        });
                    }

                    List<QuotationItemDetailDto> priceDetails = detail.getDetails();
                    for(QuotationItemDetailDto priceDetail: priceDetails) {

                        QuotationItemDetail quotationItemDetail = new QuotationItemDetail();

                        quotationItemDetail.setBrand(priceDetail.getBrand());
                        quotationItemDetail.setIsAwarded(priceDetail.getAwarded());
                        quotationItemDetail.setPrice(priceDetail.getPrice());
                        quotationItemDetail.setQuotationItem(quotationItem);
                        quotationItemDetail.setSupplier(priceDetail.getSupplier());

                        quotationItemDetailRepo.save(quotationItemDetail);

                    }

                }

                ArrayList<QuotationTerm> terms = quotation.getTerms();
                for(QuotationTerm term: terms) {

                    term.setQuotation(this.model);
                    this.quotationTermRepo.save(term);

                }

                // snapshot AFTER items/terms saved so the log captures full data
                Map newMap = insertMode ? documentLoggerFacade.makeLog(quotationRepo.findById(this.model.getId()).orElse(this.model)) : null;

                // generic document logging here
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), createdBy, oldMap, newMap);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());
                response.setSuccessMessage("Quotation successfully saved!");

            }
        }

        return response;

    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            Quotation doc = quotationRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (doc != null) {
                Map map = forLogMapMain(doc);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.QUOTATION_SUMMARY);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(filesToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = this.processCreate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        Quotation quotation = quotationRepo.findFirstByOrderByIdAsc();
        if (quotation != null) {
            return documentDtoer.getDocumentStatuses(quotation.getWorkflow().getId());
        }

        return null;
    }

    private Map forLogMapMain(Quotation quotation) {
        return documentLoggerFacade.makeLog(quotation);
    }

    private String getPrCodesForQuotation(Integer quotationId) {
        try {
            List<String> codes = quotationItemRepo.findDistinctPrCodesByQuotationId(quotationId);
            if (codes != null && !codes.isEmpty()) return String.join(", ", codes);
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        this.meta = new HashMap();
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        Quotation quotation = quotationRepo.findById(id).orElse(null);

        if(quotation != null) {

            params.put("REPORT_TITLE", "SUMMARY OF QUOTATIONS");
            java.net.URL subreportUrl = getClass().getResource("/jasper/vouchers/sub_reports/");
            params.put("SUBREPORT_DIR", subreportUrl != null ? subreportUrl.toString() + "/" : "jasper/vouchers/sub_reports/");

            params.put("DATE_PREPARED", quotation.getCreatedAt());
            params.put("VOUCHER_NO", quotation.getCode());
            params.put("WORKFLOW", quotation.getWorkflow().getId());

            params = this.signatureFacade.getDocumentSignature(params, DocumentType.QUOTATION_SUMMARY, quotation);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<QuotationItemDto> summary = quotationDetailService.getQuotationDetails(id);
        return new JRBeanCollectionDataSource(summary);
    }

}
