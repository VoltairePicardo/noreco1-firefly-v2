package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.QuotationDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDetailDto;
import com.noreco1.fireflyv2.controller.response.QuotationItemDto;
import com.noreco1.fireflyv2.service.QuotationDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by TSI on 7/1/2019.
 */
@Service
public class QuotationDetailServiceImpl implements QuotationDetailService {

    @Autowired
    QuotationDetailRepo quotationDetailRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    QuotationItemRepo quotationItemRepo;

    @Autowired
    QuotationItemDetailRepo quotationItemDetailRepo;

    @Autowired
    QuotationTermRepo quotationTermRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<QuotationItemDto> getQuotationDetails(Integer quotationId) {

        List<QuotationItemDto> data = new ArrayList<>();

        List<QuotationItem> quotationItems = quotationItemRepo.findAllByQuotationId(quotationId);

        for(QuotationItem quotationItem: quotationItems) {

            QuotationItemDto dto = new QuotationItemDto();

            dto.setAvailable(quotationItem.getIsAvailable());
            dto.setQuotationId(quotationId);
            dto.setPurchaseRequestDetailId(quotationItem.getPurchaseRequestDetail().getId());
            dto.setId(quotationItem.getId());
            dto.setItemDescription(quotationItem.getPurchaseRequestDetail().getItem() == null ? quotationItem.getPurchaseRequestDetail().getJoDescription() : quotationItem.getPurchaseRequestDetail().getItem().getDescription());
            dto.setRvNo(quotationItem.getPurchaseRequestDetail().getPurchaseRequest().getCode());
            dto.setQuantity(quotationItem.getPurchaseRequestDetail().getQuantity());
            dto.setUnitCode(quotationItem.getPurchaseRequestDetail().getUnitMeasure().getCode());

            List<QuotationItemDetailDto> details = new ArrayList<>();

            List<QuotationItemDetail> quotationItemDetails = quotationItemDetailRepo.findAllByQuotationItemId(quotationItem.getId());
            for(QuotationItemDetail quotationItemDetail: quotationItemDetails) {

                QuotationItemDetailDto detailDto = new QuotationItemDetailDto();

                detailDto.setAwarded(quotationItemDetail.getIsAwarded());
                detailDto.setBrand(quotationItemDetail.getBrand());
                detailDto.setPrice(quotationItemDetail.getPrice());

                Supplier supplier = new Supplier();
                supplier.setId(quotationItemDetail.getSupplier().getId());
                supplier.setName(quotationItemDetail.getSupplier().getName());
                supplier.setAccountNumber(quotationItemDetail.getSupplier().getAccountNumber());

                detailDto.setSupplier(supplier);

                details.add(detailDto);
            }

            dto.setDetails(details);

            data.add(dto);
        }

        return data;

//        List<QuotationDetail> quotationDetails = quotationDetailRepo.findAllQuotationDetailByQuotationId(quotationId);
//
//        List<QuotationDetailDto> quotationDetailDtos = new ArrayList<>();
//        Map detailsMap = new LinkedHashMap();
//
//        if (!quotationDetails.isEmpty()) {
//
//            ArrayList<Integer> supplierIds = new ArrayList<>();
//
//            // get unique suppliers first
//            for (QuotationDetail line : quotationDetails) {
//                Supplier supplier = line.getSupplier();
//                if(supplier != null && supplierIds.indexOf(supplier.getId()) < 0) {
//                    supplierIds.add(supplier.getId());
//                }
//            }
//
//            // align amount to suppliers
//            for (QuotationDetail line : quotationDetails) {
//
//                QuotationDetail lineFromMap = line;
//
//                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
//                if(o != null) {
//                    lineFromMap  = (QuotationDetail) o;
//                }
//
//                if(supplierIds.size() > 0) {    // has suppliers
//
//                    if(!Checker.amountIsZero(line.getPrice()) && line.getSupplier() != null) {
//
//                        Integer supplierId1 = supplierIds.get(0);
//                        if(supplierId1.equals(line.getSupplier().getId())) {    // map supplier ids
//                            lineFromMap.setPriceSupplier1(line.getPrice());
//                            lineFromMap.setAwardedToSupplier1(line.getAwardedToSupplier() != null);
//                        }
//
//                        if(supplierIds.size() > 1) {
//                            Integer supplierId2 = supplierIds.get(1);
//                            if(supplierId2.equals(line.getSupplier().getId())) {
//                                lineFromMap.setPriceSupplier2(line.getPrice());
//                                lineFromMap.setAwardedToSupplier2(line.getAwardedToSupplier() != null);
//                            }
//                        }
//
//                        if(supplierIds.size() > 2) {
//
//                            Integer supplierId3 = supplierIds.get(2);
//                            if(supplierId3.equals(line.getSupplier().getId())) {
//                                lineFromMap.setPriceSupplier3(line.getPrice());
//                                lineFromMap.setAwardedToSupplier3(line.getAwardedToSupplier() != null);
//                            }
//                        }
//
//                        if(supplierIds.size() > 3) {
//
//                            Integer supplierId3 = supplierIds.get(3);
//                            if(supplierId3.equals(line.getSupplier().getId())) {
//                                lineFromMap.setPriceSupplier4(line.getPrice());
//                                lineFromMap.setAwardedToSupplier4(line.getAwardedToSupplier() != null);
//                            }
//                        }
//                    }
//                }
//
//                detailsMap.put(line.getPurchaseRequestDetail().getId(), lineFromMap); // put back to the map
//            }
//
//            // finalize data
//            Iterator it = detailsMap.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry)it.next();
//                QuotationDetail line = (QuotationDetail) pair.getValue();
//
//                QuotationDetailDto lineDto = new QuotationDetailDto();
//
//                lineDto.setId(line.getId());
//                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
//                lineDto.setQuotationId(line.getQuotation().getId());
//                lineDto.setRvNumber(line.getPurchaseRequestDetail().getPurchaseRequest().getCode());
//                lineDto.setQuantity(line.getPurchaseRequestDetail().getQuantity());
//
//                Item item = line.getPurchaseRequestDetail().getItem();
//                if(item != null) {
//                    lineDto.setItemCode(item.getCode());
//                    lineDto.setItemDescription(item.getDescription());
//                } else {
//                    lineDto.setItemDescription(line.getPurchaseRequestDetail().getJoDescription());
//                }
//
//                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
//                lineDto.setPriceSupplier1(line.getPriceSupplier1());
//                lineDto.setPriceSupplier2(line.getPriceSupplier2());
//                lineDto.setPriceSupplier3(line.getPriceSupplier3());
//                lineDto.setPriceSupplier4(line.getPriceSupplier4());
//                lineDto.setAvailable(line.getAvailable());
//                lineDto.setAwardedToSupplier1(line.getAwardedToSupplier1());
//                lineDto.setAwardedToSupplier2(line.getAwardedToSupplier2());
//                lineDto.setAwardedToSupplier3(line.getAwardedToSupplier3());
//                lineDto.setAwardedToSupplier4(line.getAwardedToSupplier4());
//
//                quotationDetailDtos.add(lineDto);
//
//                it.remove(); // avoids a ConcurrentModificationException
//            }
//        }
//
//        return quotationDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getItemQuotationPrice(Integer supplierAccountNo, Integer rvDetailId) {
        Supplier supplier = supplierRepo.findOneByAccountNumber(supplierAccountNo);
        if(supplier != null) {

            QuotationDetail detail = quotationDetailRepo.findFirstBySupplierIdAndPurchaseRequestDetailIdAndQuotationDocumentStatusIdNotOrderByQuotationCreatedAtDesc(supplier.getId(), rvDetailId, DocumentStatus.CANCELLED.getId());
            if(detail != null) {
                return detail.getPrice();
            }
        }
        return BigDecimal.ZERO;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Map itemDetailForPO(Integer rvDetailId) {
        Map data = new HashMap();

        QuotationItemDetail itemDetail = this.quotationItemDetailRepo.findOneByQuotationItemPurchaseRequestDetailIdAndIsAwardedTrue(rvDetailId);
        if(itemDetail != null) {

            Supplier supplier = itemDetail.getSupplier();

            Map supplierMap = new HashMap();
            supplierMap.put("accountNo", supplier.getAccountNumber());
            supplierMap.put("name", supplier.getName());

            data.put("supplier", supplierMap);

            Map itemDetailMap = new HashMap();
            itemDetailMap.put("brand", itemDetail.getBrand());
            itemDetailMap.put("price", itemDetail.getPrice());

            data.put("itemDetail", itemDetailMap);

            List<QuotationTerm> quotationTerms = this.quotationTermRepo.findByRvDetailAndSupplier(rvDetailId, supplier.getId());

            if(Checker.collectionIsNotEmpty(quotationTerms)) {
                QuotationTerm quotationTerm = quotationTerms.get(0);
                // make it lean
                quotationTerm.setQuotation(null);
                quotationTerm.setSupplier(null);

                data.put("terms", quotationTerm);
            }
        }

        return data;
    }
}
