package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PoDetailDto;
import com.noreco1.fireflyv2.service.PoDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Personal on 5/15/2015.
 */
@Service
public class PoDetailServiceImpl implements PoDetailService {

    @Autowired
    PoDetailRepo poDetailRepo;

    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Autowired
    ReceivingReportDetailRepo receivingReportDetailRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    ItemTestingDetailRepo itemTestingDetailRepo;

    @Autowired
    PurchaseOrderBudgetDetailRepo purchaseOrderBudgetDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PoDetailDto> getPoDetails(Integer poId) {
        List<PoDetail> poDetails = poDetailRepo.findByPurchaseOrderId(poId);

        List<PoDetailDto> poDetailDtos = new ArrayList<>();

        if (poDetails != null) {
            for (PoDetail line : poDetails) {
                PoDetailDto lineDto = new PoDetailDto();
                lineDto.setId(line.getId());
                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                lineDto.setPurchaseOrderId(line.getPurchaseOrder().getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(line.getPurchaseRequestDetail().getItem().getId());
                lineDto.setItemCode(line.getPurchaseRequestDetail().getItem().getCode());
                lineDto.setItemDescription(line.getPurchaseRequestDetail().getItem().getDescription());
                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getUnitPrice());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setRvdQuantity(line.getPurchaseRequestDetail().getQuantity());
                lineDto.setRemainingQuantity(line.getPurchaseRequestDetail().getQuantity().subtract(line.getPurchaseRequestDetail().getPoQuantity()));
                lineDto.setRequisitionVoucherCode(line.getPurchaseRequestDetail().getPurchaseRequest().getCode());
                lineDto.setBrand(line.getBrand());

                lineDto.setDeliveredQuantity(line.getDeliveredQuantity());

                poDetailDtos.add(lineDto);
            }
        }

        return poDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PoDetailDto> getPoDetailsForItemTesting(Integer poId) {
        List<PoDetail> poDetails = poDetailRepo.findByPurchaseOrderId(poId);

        List<PoDetailDto> poDetailDtos = new ArrayList<>();
        Integer[] nonPendingStatusIds = { // override this inside switch/case statement
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                DocumentStatus.CANCELLED.getId()
        };

        if (poDetails != null) {
            for (PoDetail line : poDetails) {
                ReceivingReportDetail receivingReportDetail = receivingReportDetailRepo.findByPoDetailIdAndReceivingReportDocumentStatusIdNotIn(line.getId(), Arrays.asList(nonPendingStatusIds));

                BigDecimal totalItemQuantityTested = itemTestingDetailRepo.getTotalQuantityTestedByPoDetailId(line.getId());

                PoDetailDto lineDto = new PoDetailDto();
                lineDto.setId(line.getId());
                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                lineDto.setPurchaseOrderId(line.getPurchaseOrder().getId());
                lineDto.setQuantity(line.getQuantity());
//                lineDto.setDeliveredQuantity(line.getDeliveredQuantity().add(receivingReportDetail != null ? receivingReportDetail.getQuantityReceived() : BigDecimal.ZERO));
                lineDto.setItemId(line.getPurchaseRequestDetail().getItem().getId());
                lineDto.setItemCode(line.getPurchaseRequestDetail().getItem().getCode());
                lineDto.setItemDescription(line.getPurchaseRequestDetail().getItem().getDescription());
                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getUnitPrice());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setRvdQuantity(line.getPurchaseRequestDetail().getQuantity());
                lineDto.setRemainingQuantity(line.getPurchaseRequestDetail().getQuantity().subtract(line.getPurchaseRequestDetail().getPoQuantity()));
                lineDto.setRequisitionVoucherCode(line.getPurchaseRequestDetail().getPurchaseRequest().getCode());

                lineDto.setSentForTestingQuantity(totalItemQuantityTested);

                poDetailDtos.add(lineDto);
            }
        }

        return poDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PoDetailDto> getPoDetailsWithItemTesting(Integer poId) {

        List<PoDetailDto> poDetailDtos = new ArrayList<>();

        try {

            List<PoDetail> poDetails = poDetailRepo.findPoDetailWithItemTestingByPoId(poId);

            if (poDetails != null) {

                for (PoDetail line : poDetails) {

                    PoDetailDto lineDto = new PoDetailDto();

                    lineDto.setId(line.getId());
                    lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                    lineDto.setPurchaseOrderId(line.getPurchaseOrder().getId());

                    BigDecimal totalItemQuantityTested = itemTestingDetailRepo.getTotalQuantityTestedByPoDetailId(line.getId());

                    lineDto.setQuantity(totalItemQuantityTested);
                    lineDto.setItemId(line.getPurchaseRequestDetail().getItem().getId());
                    lineDto.setItemCode(line.getPurchaseRequestDetail().getItem().getCode());
                    lineDto.setItemDescription(line.getPurchaseRequestDetail().getItem().getDescription());
                    lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                    lineDto.setUnitPrice(line.getUnitPrice());
                    lineDto.setDiscount(line.getDiscount());
                    lineDto.setItemAmount(line.getAmount());
                    lineDto.setRvdQuantity(line.getPurchaseRequestDetail().getQuantity());
                    lineDto.setRemainingQuantity(line.getPurchaseRequestDetail().getQuantity().subtract(line.getPurchaseRequestDetail().getPoQuantity()));
                    lineDto.setRequisitionVoucherCode(line.getPurchaseRequestDetail().getPurchaseRequest().getCode());
                    lineDto.setBrand(line.getBrand());

                    poDetailDtos.add(lineDto);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return poDetailDtos;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getItemCanvassPrice(Integer supplierAccountNo, Integer rvDetailId) {
        Supplier supplier = supplierRepo.findOneByAccountNumber(supplierAccountNo);
        if(supplier != null) {

            CanvassDetail detail = canvassDetailRepo.findBySupplierIdAndPurchaseRequestDetailId(supplier.getId(), rvDetailId);
            if(detail != null) {
                return detail.getUnitPrice();
            }
        }
        return BigDecimal.ZERO;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PurchaseOrderBudgetDetail> getPurchaseOrderBudgetDetail(Integer poId) {

        List<PurchaseOrderBudgetDetail> purchaseOrderBudgetDetails = new ArrayList<>();

        try {

            purchaseOrderBudgetDetails = this.purchaseOrderBudgetDetailRepo.findAllByPurchaseOrderId(poId);

            for (PurchaseOrderBudgetDetail purchaseOrderBudgetDetail : purchaseOrderBudgetDetails){

                purchaseOrderBudgetDetail.setParent(purchaseOrderBudgetDetail.getBudgetSubItem().getBudgetLineItemDetail().getTitle());

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return purchaseOrderBudgetDetails;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getCashFlowItemAmountBalanceByType(Integer cashFlowItemId, String type) {

        BigDecimal balance = BigDecimal.ZERO;

        try {

            if(Objects.equals(type, com.noreco1.fireflyv2.model.enums.DocumentType.CV.getCode())){
                balance = this.purchaseOrderBudgetDetailRepo.getCashFlowItemDetailAmountBalanceCV(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), cashFlowItemId);
            } else {
                balance = this.purchaseOrderBudgetDetailRepo.getCashFlowItemDetailAmountBalancePOJORFPCA(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId(), cashFlowItemId);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return balance.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : balance;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BigDecimal getDefaultEstimatedAmount(List<Integer> itemIds) {

        BigDecimal defaultAmount = BigDecimal.ZERO;

        try {

            for (Integer itemId : itemIds){
                PoDetail poDetail = this.poDetailRepo.findFirstByPurchaseRequestDetailItemIdOrderByIdDesc(itemId);
                if(poDetail != null && Checker.isValidId(poDetail.getId())){
                    defaultAmount = defaultAmount.add(poDetail.getAmount());
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return defaultAmount;

    }
}
