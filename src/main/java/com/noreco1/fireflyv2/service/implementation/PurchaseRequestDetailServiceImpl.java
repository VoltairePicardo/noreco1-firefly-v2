package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.RvType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.RvDetailDto;
import com.noreco1.fireflyv2.service.PurchaseRequestDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 4/29/2015.
 */
@Service
public class PurchaseRequestDetailServiceImpl implements PurchaseRequestDetailService {

    @Autowired
    PurchaseRequestDetailRepo purchaseRequestDetailRepo;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    UnitMeasureRepo unitMeasureRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    QuotationDetailRepo quotationDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetails(Integer rvId) {
        List<PurchaseRequestDetail> purchaseRequestDetails = purchaseRequestDetailRepo.findByPurchaseRequestId(rvId);

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (purchaseRequestDetails != null) {
            for (PurchaseRequestDetail line : purchaseRequestDetails) {
                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setId(line.getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(0);
                if(line.getItem() != null) {
                    lineDto.setItemCode(line.getItem().getCode());
                    lineDto.setItemDescription(line.getItem().getDescription());
                    lineDto.setItemId(line.getItem().getId());
                    lineDto.setInventoryCategoryId(line.getItem().getInventoryCategory().getId());
                }
                lineDto.setUnitId(line.getUnitMeasure().getId());
                lineDto.setUnitCode(line.getUnitMeasure().getCode());
                lineDto.setRvId(rvId);
                lineDto.setJoDescription(line.getJoDescription());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsByStatus(Integer statusId) {
        List<PurchaseRequestDetail> purchaseRequestDetails = purchaseRequestDetailRepo.findPurchaseRequestDetailsByPurchaseRequestDocumentStatusId(statusId);

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (purchaseRequestDetails != null) {
            for (PurchaseRequestDetail line : purchaseRequestDetails) {
                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setId(line.getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(0);
                if(line.getItem() != null) {
                    lineDto.setItemCode(line.getItem().getCode());
                    lineDto.setItemDescription(line.getItem().getDescription());
                    lineDto.setItemId(line.getItem().getId());
                }
                lineDto.setUnitId(line.getUnitMeasure().getId());
                lineDto.setUnitCode(line.getUnitMeasure().getCode());
                lineDto.setRvId(line.getPurchaseRequest().getId());
                lineDto.setJoDescription(line.getJoDescription());
                lineDto.setRvNumber(line.getPurchaseRequest().getCode());
                lineDto.setRvDate(line.getPurchaseRequest().getVoucherDate());
                lineDto.setPoQuantity(line.getPoQuantity());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForPo(Integer cancelledPoId) {
        List<PurchaseRequestDetail> purchaseRequestDetails = this.purchaseRequestDetailRepo.findPurchaseRequestDetailsByCancelledPOId(cancelledPoId);
        List<RvDetailDto> rvDetailDtos = this.composeRvDetailDtoForPo(purchaseRequestDetails);

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getPrDetailsForPo(Integer prId, Integer supplierAccountNumber) {

        List<PurchaseRequestDetail> rvDetails = purchaseRequestDetailRepo.findPrDetailsByTypesAndStatusId(prId, supplierAccountNumber);

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (rvDetails != null) {
            for (PurchaseRequestDetail line : rvDetails) {

                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setId(line.getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(0);
                if(line.getItem() != null) {
                    lineDto.setItemCode(line.getItem().getCode());
                    lineDto.setItemDescription(line.getItem().getDescription());
                    lineDto.setItemId(line.getItem().getId());
                }
                lineDto.setUnitId(line.getUnitMeasure().getId());
                lineDto.setUnitCode(line.getUnitMeasure().getCode());
                lineDto.setRvId(line.getPurchaseRequest().getId());
                lineDto.setJoDescription(line.getJoDescription());
                lineDto.setRvNumber(line.getPurchaseRequest().getCode());
                lineDto.setRvDate(line.getPurchaseRequest().getVoucherDate());
                lineDto.setPoQuantity(line.getPoQuantity());
                lineDto.setAcceptedQuantity(line.getPoQuantity());
                lineDto.setRemainingQuantity(line.getQuantity().subtract(line.getPoQuantity()));
                lineDto.setDeliveryDate(line.getPurchaseRequest().getDeliveryDate());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getPrDetailsForCanvass(Integer prId) {

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        try {

            List types = new ArrayList();
            types.add(RvType.FOR_PO.getId());
            types.add(RvType.FOR_IT.getId());
            types.add(RvType.FOR_LAB.getId());
            types.add(RvType.FOR_REP.getId());

            List<Object[]> rows = purchaseRequestDetailRepo.findPrdForCanvass(types, DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), DocumentStatus.CANCELLED.getId(), prId);

            if (rows != null) {
                for (Object[] row : rows) {
                    rvDetailDtos.add(mapCanvassRow(row));
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return rvDetailDtos;

    }

    private List<RvDetailDto> composeRvDetailDtoForPo(List<PurchaseRequestDetail> purchaseRequestDetails) {

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (purchaseRequestDetails != null) {
            for (PurchaseRequestDetail line : purchaseRequestDetails) {
                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setId(line.getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(0);
                if(line.getItem() != null) {
                    lineDto.setItemCode(line.getItem().getCode());
                    lineDto.setItemDescription(line.getItem().getDescription());
                    lineDto.setItemId(line.getItem().getId());
                }
                lineDto.setUnitId(line.getUnitMeasure().getId());
                lineDto.setUnitCode(line.getUnitMeasure().getCode());
                lineDto.setRvId(line.getPurchaseRequest().getId());
                lineDto.setJoDescription(line.getJoDescription());
                lineDto.setRvNumber(line.getPurchaseRequest().getCode());
                lineDto.setRvDate(line.getPurchaseRequest().getVoucherDate());
                lineDto.setPoQuantity(line.getPoQuantity());
                lineDto.setAcceptedQuantity(line.getPoQuantity());
                lineDto.setRemainingQuantity(line.getQuantity().subtract(line.getPoQuantity()));

                QuotationDetail quotationDetail = quotationDetailRepo.findFirstByPurchaseRequestDetailId(line.getId());
                if(quotationDetail != null) lineDto.setQuotationId(quotationDetail.getQuotation().getId());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForPo() {
        List<PurchaseRequestDetail> rvDetails = purchaseRequestDetailRepo.findRvDetailsByTypesAndStatusId(RvType.FOR_PO.getId(), RvType.FOR_IT.getId());

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (rvDetails != null) {
            for (PurchaseRequestDetail line : rvDetails) {

                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setId(line.getId());
                lineDto.setQuantity(line.getQuantity());
                lineDto.setItemId(0);
                if(line.getItem() != null) {
                    lineDto.setItemCode(line.getItem().getCode());
                    lineDto.setItemDescription(line.getItem().getDescription());
                    lineDto.setItemId(line.getItem().getId());
                }
                lineDto.setUnitId(line.getUnitMeasure().getId());
                lineDto.setUnitCode(line.getUnitMeasure().getCode());
                lineDto.setRvId(line.getPurchaseRequest().getId());
                lineDto.setJoDescription(line.getJoDescription());
                lineDto.setRvNumber(line.getPurchaseRequest().getCode());
                lineDto.setRvDate(line.getPurchaseRequest().getVoucherDate());
                lineDto.setPoQuantity(line.getPoQuantity());
                lineDto.setAcceptedQuantity(line.getPoQuantity());
                lineDto.setRemainingQuantity(line.getQuantity().subtract(line.getPoQuantity()));
                lineDto.setDeliveryDate(line.getPurchaseRequest().getDeliveryDate());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForPoRo(Integer supplierAcctNo) {

        Supplier supplier = supplierRepo.findOneByAccountNumber(supplierAcctNo);
        List<Object[]> rvDetails = null;

        if(supplier != null){
            rvDetails = purchaseRequestDetailRepo.findNoQuotationPurchaseRequestDetailsByTypesAndStatusIdAndSupplierId(RvType.FOR_PO.getId(), RvType.FOR_IT.getId(), DocumentStatus.APPROVED.getId(), supplier.getId());
        }else{
            rvDetails = purchaseRequestDetailRepo.findNoQuotationPurchaseRequestDetailsByTypesAndStatusId(RvType.FOR_PO.getId(), RvType.FOR_IT.getId(), DocumentStatus.APPROVED.getId());
        }

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (rvDetails != null) {
            for (Object[] line : rvDetails) {
                RvDetailDto lineDto = new RvDetailDto();

                PurchaseRequest purchaseRequest = PurchaseRequestRepo.findById((Integer) line[1]).orElse(null);
                Item item = itemRepo.findById((Integer) line[2]).orElse(null);
                UnitMeasure unitMeasure = unitMeasureRepo.findById((Integer) line[3]).orElse(null);

                lineDto.setId((Integer) line[0]);
                lineDto.setQuantity((BigDecimal) line[4]);
                lineDto.setItemId(0);
                if(item != null) {
                    lineDto.setItemCode(item.getCode());
                    lineDto.setItemDescription(item.getDescription());
                    lineDto.setItemId(item.getId());
                }
                lineDto.setUnitId(unitMeasure.getId());
                lineDto.setUnitCode(unitMeasure.getCode());
                lineDto.setRvId(purchaseRequest.getId());
                lineDto.setJoDescription((String) line[6]);
                lineDto.setRvNumber(purchaseRequest.getCode());
                lineDto.setRvDate(purchaseRequest.getVoucherDate());
                lineDto.setPoQuantity((BigDecimal) line[5]);
                lineDto.setAcceptedQuantity((BigDecimal) line[5]);
                lineDto.setRemainingQuantity(((BigDecimal) line[4]).subtract((BigDecimal) line[5]));
                lineDto.setUnitPrice((BigDecimal) line[7]);
                lineDto.setVat((BigDecimal) line[8]);

                QuotationDetail quotationDetail = quotationDetailRepo.findFirstByPurchaseRequestDetailId(lineDto.getId());
                if(quotationDetail != null) lineDto.setQuotationId(quotationDetail.getQuotation().getId());

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map<String, Object>> getPurchaseRequestsForJo() {
        List<Integer> types = List.of(RvType.FOR_REP.getId(), RvType.FOR_LAB.getId());
        List<Object[]> rows = purchaseRequestDetailRepo.findPurchaseRequestsForJo(types);
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",           row[0]);
                map.put("code",         row[1]);
                map.put("voucherDate",  row[2]);
                map.put("purpose",      row[3]);
                map.put("deliveryDate", row[4]);
                map.put("preparedBy",   row[5]);
                result.add(map);
            }
        }
        return result;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map<String, Object>> getPurchaseRequestsForPo() {
        List<Integer> types = List.of(RvType.FOR_PO.getId(), RvType.FOR_IT.getId());
        List<Object[]> rows = purchaseRequestDetailRepo.findPurchaseRequestsForPo(types);
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",           row[0]);
                map.put("code",         row[1]);
                map.put("voucherDate",  row[2]);
                map.put("purpose",      row[3]);
                map.put("deliveryDate", row[4]);
                map.put("preparedBy",   row[5]);
                result.add(map);
            }
        }
        return result;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForJo() {
        List<Integer> types = List.of(RvType.FOR_REP.getId(), RvType.FOR_LAB.getId());
        List<Object[]> rows = purchaseRequestDetailRepo.findPurchaseRequestDetailsForJo(types);

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                RvDetailDto dto = new RvDetailDto();
                dto.setId((Integer) row[0]);
                dto.setQuantity((java.math.BigDecimal) row[1]);
                dto.setPoQuantity((java.math.BigDecimal) row[2]);
                dto.setJoDescription((String) row[3]);
                dto.setItemId(0);
                if (row[4] != null) {
                    dto.setItemId((Integer) row[4]);
                    dto.setItemCode((String) row[5]);
                    dto.setItemDescription((String) row[6]);
                } else {
                    dto.setItemDescription((String) row[3]);
                }
                dto.setUnitId((Integer) row[7]);
                dto.setUnitCode((String) row[8]);
                dto.setRvId((Integer) row[9]);
                dto.setRvNumber((String) row[10]);
                dto.setRvDate((java.util.Date) row[11]);
                dto.setRvPurpose((String) row[12]);
                rvDetailDtos.add(dto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForCanvass() {

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        try {

            List types = new ArrayList();
            types.add(RvType.FOR_PO.getId());
            types.add(RvType.FOR_IT.getId());
            types.add(RvType.FOR_LAB.getId());
            types.add(RvType.FOR_REP.getId());

            List<Object[]> rows = purchaseRequestDetailRepo.findAllPrdForCanvass(
                    types, DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), DocumentStatus.CANCELLED.getId());

            if (rows != null) {
                for (Object[] row : rows) {
                    rvDetailDtos.add(mapCanvassRow(row));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rvDetailDtos;
    }

    private RvDetailDto mapCanvassRow(Object[] row) {
        RvDetailDto dto = new RvDetailDto();
        dto.setId((Integer) row[0]);
        dto.setQuantity((java.math.BigDecimal) row[1]);
        dto.setPoQuantity((java.math.BigDecimal) row[2]);
        dto.setJoDescription((String) row[3]);
        dto.setItemId(0);
        if (row[4] != null) {
            dto.setItemId((Integer) row[4]);
            dto.setItemCode((String) row[5]);
            dto.setItemDescription((String) row[6]);
        } else {
            dto.setItemDescription((String) row[3]); // joDescription as fallback
        }
        dto.setUnitId((Integer) row[7]);
        dto.setUnitCode((String) row[8]);
        dto.setRvId((Integer) row[9]);
        dto.setRvNumber((String) row[10]);
        dto.setRvDate((java.util.Date) row[11]);
        dto.setRequestedBy(row[12] != null ? (String) row[12] : "");
        return dto;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsWithItemGroup(Integer rvId) {
        List<PurchaseRequestDetail> purchaseRequestDetails = purchaseRequestDetailRepo.findByPurchaseRequestIdOrderByItemGroupAndId(rvId);

        List<RvDetailDto> rvDetailDtos = new ArrayList<>();

        if (purchaseRequestDetails != null) {
            for (PurchaseRequestDetail line : purchaseRequestDetails) {
                RvDetailDto lineDto = new RvDetailDto();
                lineDto.setQuantity(line.getQuantity());
                if(line.getItem() != null) {
                    lineDto.setItemDescription(line.getItem().getDescription());
                }
                lineDto.setUnitCode(StringFormatter.getUnitCode(line.getUnitMeasure(), line.getQuantity()));
                lineDto.setJoDescription(line.getJoDescription());
                if(line.getItem() != null && line.getItem().getId() > 0) {
                    lineDto.setItemGroup(1);
                    lineDto.setItemGroupName("Items/Materials");
                } else {
                    lineDto.setItemGroup(2);
                    lineDto.setItemGroupName("Labor");
                }

                rvDetailDtos.add(lineDto);
            }
        }

        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<RvDetailDto> getRvDetailsForQuotation() {
        List<RvDetailDto> rvDetailDtos = new ArrayList<>();
        try {
            List types = new ArrayList();
            types.add(RvType.FOR_PO.getId());
            types.add(RvType.FOR_IT.getId());
            types.add(RvType.FOR_LAB.getId());
            types.add(RvType.FOR_REP.getId());

            List<Object[]> rows = purchaseRequestDetailRepo.findAllPrdForQuotation(
                    types, DocumentStatus.REVIEWED_AND_ACCEPTED.getId(), DocumentStatus.CANCELLED.getId());

            if (rows != null) {
                for (Object[] row : rows) {
                    rvDetailDtos.add(mapCanvassRow(row));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rvDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getRvDetailsForWithdrawal(Integer rvId, Integer invLocId, Integer invCatId) {
        List<Map> data = new ArrayList<>();
        List<Object[]> rows = purchaseRequestDetailRepo.findForWithdrawal(rvId, invLocId, invCatId);

        if (!Checker.collectionIsEmpty(rows)){
            for(Object[] row:rows){
                Map m = new HashMap();
                m.put("quantityReleased", 0);
                m.put("inventoryBalance", row[0]);
                m.put("itemStockId", row[1]);
                m.put("itemId", row[2]);
                m.put("itemCode", row[3]);
                m.put("unitCode", row[4]);
                m.put("unitId", row[5]);
                m.put("unitCost", row[6]);
                m.put("itemDescription", row[7]);
                m.put("inventoryLocationId", row[8]);
                m.put("quantity", row[10]);
                m.put("rvBalance", row[10]);

                if(row[13] == null) {   // RR without PO
                    BigDecimal quantity = (BigDecimal) row[9];
                    BigDecimal withdrawQuantity = (BigDecimal) row[12];

                    BigDecimal balance = quantity.subtract(withdrawQuantity);

                    m.put("quantity", balance);
                    m.put("rvBalance", balance);
                }

                data.add(m);
            }
        }

        return data;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getRvDetailsForRR(Integer rvId) {
        List<Map> data = new ArrayList<>();
        List<Object[]> rows = purchaseRequestDetailRepo.findForRR(rvId);

        if (!Checker.collectionIsEmpty(rows)){
            for(Object[] row:rows){
                Map m = new HashMap();
                m.put("rvDetailId", row[0]);
                m.put("quantity", row[1]);
                m.put("deliveredQuantity", row[2]);
                m.put("itemId", row[3]);
                m.put("itemCode", row[4]);
                m.put("itemDescription", row[5]);
                m.put("unitCode", row[6]);
                m.put("unitPrice", 0);
                m.put("itemAmount", 0);
                m.put("adjustment", 0);
                m.put("netAmount", 0);
                m.put("discount", 0);
                m.put("id", 0);

                data.add(m);
            }
        }

        return data;
    }
}
