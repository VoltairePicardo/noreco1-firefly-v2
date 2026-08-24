package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.ItemTestingService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.ItemTestingValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
@Service(value = "itemTestingServiceImpl")
public class ItemTestingServiceImpl implements ItemTestingService, PrintableVoucher {

    @Autowired
    ItemTestingRepo itemTestingRepo;

    @Autowired
    ItemTestingDetailRepo itemTestingDetailRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    private com.noreco1.fireflyv2.repo.InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private com.noreco1.fireflyv2.repo.PoDetailRepo poDetailRepo;

    @Autowired
    private com.noreco1.fireflyv2.repo.ItemRepo itemRepo;

    @Autowired
    private com.noreco1.fireflyv2.repo.PurchaseOrderRepo purchaseOrderRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<ItemTesting> findAll(String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return itemTestingRepo.findAllByDateBetweenOrderByDate(fromDate, toDate, pageable);
    }

    @Override
    public PostResponse update(ItemTesting itemTesting, BindingResult bindingResult, MessageSource messageSource) {
        return this.create(itemTesting, bindingResult, messageSource);
    }

    @Override
    public PostResponse create(ItemTesting itemTesting, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            ItemTestingValidator validator = new ItemTestingValidator();
            validator.setService(this);
            validator.validate(itemTesting, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save item for testing");
            }else {
                ItemTesting savedItemTesting = null;

                List<ItemTestingDetail> itemTestingDetails = itemTesting.getItemTestingDetails();
                Boolean toBeInserted = !Checker.isValidId(itemTesting.getId());

                Supplier supplier = null;

                if(itemTesting.getSupplier() != null) {
                    supplier = supplierRepo.findOneByAccountNumber(itemTesting.getSupplier().getAccountNumber());
                }

                itemTesting.setSupplier(supplier);

                // Resolve transient associations to managed entities before saving
                if (itemTesting.getInventoryLocation() != null && itemTesting.getInventoryLocation().getId() != null) {
                    itemTesting.setInventoryLocation(inventoryLocationRepo.findById(itemTesting.getInventoryLocation().getId()).orElse(null));
                }
                if (itemTesting.getPurchaseOrder() != null && itemTesting.getPurchaseOrder().getId() != null) {
                    itemTesting.setPurchaseOrder(purchaseOrderRepo.findById(itemTesting.getPurchaseOrder().getId()).orElse(null));
                }

                if (toBeInserted) {

                    itemTesting.setCreatedBy(authenticationFacade.getLoggedIn());
                    savedItemTesting = itemTestingRepo.save(itemTesting);

                    if(savedItemTesting != null){

                        this.saveItemTestingDetail(savedItemTesting, itemTestingDetails);

                        response.setSuccess(true);
                        response.setModelId(savedItemTesting.getId());
                        response.setSuccessMessage("New item for testing has been successfully created");

                    } else{
                        response.setFailureMessage("Failed to create new item for testing");
                    }

                } else{

                    ItemTesting toBeUpdated = itemTestingRepo.findById(itemTesting.getId()).orElse(null);

                    if(toBeUpdated != null){

                        toBeUpdated.setUpdatedAt(new Date());
                        toBeUpdated.setInventoryLocation(itemTesting.getInventoryLocation());
                        toBeUpdated.setSupplier(itemTesting.getSupplier());
                        toBeUpdated.setPurchaseOrder(itemTesting.getPurchaseOrder());
                        toBeUpdated.setDate(itemTesting.getDate());
                        savedItemTesting = itemTestingRepo.save(toBeUpdated);

                        if(savedItemTesting != null){

                            itemTestingDetailRepo.deleteAllByItemTestingId(savedItemTesting.getId());
                            this.saveItemTestingDetail(savedItemTesting, itemTestingDetails);

                            response.setSuccess(true);
                            response.setModelId(savedItemTesting.getId());
                            response.setSuccessMessage("Item for testing has been successfully updated");
                        } else{
                            response.setFailureMessage("Failed to update item for testing");
                        }

                    } else {
                        response.setFailureMessage("Item for testing is not available.");
                    }

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public ItemTestingDto findById(Integer id) {
        ItemTesting itemTesting =  itemTestingRepo.findById(id).orElse(null);
        ItemTestingDto itemTestingDto = new ItemTestingDto();
        if(itemTesting != null){

            itemTestingDto.setId(itemTesting.getId());
            itemTestingDto.setDate(itemTesting.getDate());
            itemTestingDto.setInventoryLocation(itemTesting.getInventoryLocation());
            itemTestingDto.setCreatedBy(itemTesting.getCreatedBy());
            itemTestingDto.setCreatedAt(itemTesting.getCreatedAt());
            itemTestingDto.setUpdatedAt(itemTesting.getUpdatedAt());
            itemTestingDto.setSupplier(itemTesting.getSupplier());
            itemTestingDto.setPurchaseOrder(itemTesting.getPurchaseOrder());

            List<ItemTestingDetail> items = itemTestingDetailRepo.findAllByItemTestingId(itemTesting.getId());

            if(Checker.collectionIsNotEmpty(items)){

                List<ItemTestingDetail> itemTestingDetails = new ArrayList<>();

                for(ItemTestingDetail itemTestingDetail : items){
                    Item item       = itemTestingDetail.getItem();
                    PoDetail pod    = itemTestingDetail.getPoDetail();
                    if (item == null) continue;

                    ItemTestingDetail detail = new ItemTestingDetail();

                    BigDecimal balance = BigDecimal.ZERO;
                    if (itemTesting.getInventoryLocation() != null) {
                        ItemStock itemStock = itemStockRepo.findByItemIdAndInventoryLocationId(item.getId(), itemTesting.getInventoryLocation().getId());
                        if (itemStock != null) balance = itemStock.getQuantity();
                    }

                    BigDecimal totalItemQuantityTested = (pod != null)
                            ? itemTestingDetailRepo.getTotalQuantityTestedByPoDetailId(pod.getId())
                            : BigDecimal.ZERO;
                    if (totalItemQuantityTested == null) totalItemQuantityTested = BigDecimal.ZERO;

                    detail.setId(itemTestingDetail.getId());
                    detail.setItem(item);
                    detail.setItemTesting(itemTestingDetail.getItemTesting());
                    detail.setBalance(balance);
                    detail.setItemCode(item.getCode());
                    detail.setUnitCode(item.getUnit() != null ? item.getUnit().getCode() : null);
                    detail.setItemDescription(item.getDescription());
                    detail.setPoDetail(pod);
                    detail.setQuantity(pod != null ? pod.getQuantity() : BigDecimal.ZERO);
                    detail.setQuantityReceived(itemTestingDetail.getQuantity());
                    detail.setDeliveredQuantity(totalItemQuantityTested);
                    detail.setUnitsReceivedQuantity(itemTestingDetail.getUnitsReceivedQuantity());
                    detail.setUnitsRejectedQuantity(itemTestingDetail.getUnitsRejectedQuantity());
                    detail.setRemarks(itemTestingDetail.getRemarks());

                    itemTestingDetails.add(detail);

                }

                itemTestingDto.setItemTestingDetails(itemTestingDetails);

            }

        }

        return itemTestingDto;
    }

    @Override
    @Transactional
    public PostResponse delete(Integer id) {
        PostResponse response = new PostResponse();

        try {
            ItemTesting itemTesting = itemTestingRepo.findById(id).orElse(null);
            if(itemTesting != null) {
                itemTestingDetailRepo.deleteAllByItemTestingId(itemTesting.getId());
                itemTestingRepo.delete(itemTesting);
                response.setSuccessMessage("Item for Testing has been deleted");
            } else {
                response.setFailureMessage("Item for Testing is not available");
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> getItemTestingDetails(Integer id) {

        List<Map> data = new ArrayList<>();

        try {

            ItemTesting itemTesting = itemTestingRepo.findById(id).orElse(null);

            if (itemTesting != null) {

                List<ItemTestingDetail> itemTestingDetails = itemTestingDetailRepo.findAllByItemTestingId(itemTesting.getId());

                if(Checker.collectionIsNotEmpty(itemTestingDetails)) {

                    int counter = 1;

                    for (ItemTestingDetail detail:itemTestingDetails) {

                        if (detail.getItem() == null) continue;

                        Map detailMap = new HashMap();

                        String poNo = (detail.getPoDetail() != null && detail.getPoDetail().getPurchaseOrder() != null)
                                ? detail.getPoDetail().getPurchaseOrder().getCode() : "";

                        detailMap.put("id", counter++);
                        detailMap.put("code", detail.getItem().getCode());
                        detailMap.put("description", detail.getItem().getDescription());
                        detailMap.put("quantity", detail.getQuantity());
                        detailMap.put("unitReceivedQuantity", detail.getUnitsReceivedQuantity());
                        detailMap.put("unitRejectedQuantity", detail.getUnitsRejectedQuantity());
                        detailMap.put("remarks", detail.getRemarks());
                        detailMap.put("poNo", poNo);

                        data.add(detailMap);

                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    @Override
    public List<com.noreco1.fireflyv2.model.InventoryLocation> getInventoryLocations() {
        return inventoryLocationRepo.findAllByOrderByDescriptionAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    @Override
    public List<com.noreco1.fireflyv2.model.PoDetail> getPurchaseOrderDetailsForItemTesting(Integer poId) {
        return poDetailRepo.findByPurchaseOrderId(poId);
    }

    private void saveItemTestingDetail(ItemTesting savedItemTesting, List<ItemTestingDetail> itemTestingDetails) {
        if (Checker.collectionIsNotEmpty(itemTestingDetails)) {
            for (ItemTestingDetail incoming : itemTestingDetails) {
                // Build a fresh entity to avoid transient reference issues from deserialized JSON objects
                ItemTestingDetail fresh = new ItemTestingDetail();
                fresh.setItemTesting(savedItemTesting);

                if (incoming.getItem() != null && incoming.getItem().getId() != null) {
                    fresh.setItem(itemRepo.findById(incoming.getItem().getId()).orElse(null));
                }
                if (incoming.getPoDetail() != null && incoming.getPoDetail().getId() != null) {
                    fresh.setPoDetail(poDetailRepo.findById(incoming.getPoDetail().getId()).orElse(null));
                }

                BigDecimal unitsReceived = incoming.getUnitsReceivedQuantity() != null ? incoming.getUnitsReceivedQuantity() : BigDecimal.ZERO;
                BigDecimal accepted      = incoming.getQuantityReceived()      != null ? incoming.getQuantityReceived()      : BigDecimal.ZERO;

                fresh.setQuantity(accepted);
                fresh.setUnitsReceivedQuantity(unitsReceived);
                fresh.setUnitsRejectedQuantity(unitsReceived.subtract(accepted));
                fresh.setRemarks(incoming.getRemarks());

                itemTestingDetailRepo.save(fresh);
            }
        }
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        ItemTesting itemTesting = itemTestingRepo.findById(id).orElse(null);

        if (itemTesting != null) {

            params.put("TITLE", "ITEM TESTING ACKNOWLEDGEMENT");
            params.put("ITEM_TESTING_DATE", itemTesting.getDate());
            params.put("INV_LOCATION", itemTesting.getInventoryLocation() != null ? itemTesting.getInventoryLocation().getDescription() : "");
            params.put("PO_NO", itemTesting.getPurchaseOrder() != null ? itemTesting.getPurchaseOrder().getCode() : "");

            if (itemTesting.getSupplier() != null) {
                Supplier supplier = supplierRepo.findById(itemTesting.getSupplier().getId()).orElse(null);
                params.put("SUPPLIER", supplier != null ? supplier.getName() : "");
                params.put("SUPPLIER_ADDRESS", supplier != null ? supplier.getAddress() : "");
            } else {
                params.put("SUPPLIER", "");
                params.put("SUPPLIER_ADDRESS", "");
            }

            if (itemTesting.getCreatedBy() != null) {
                Employee preparar = employeeRepo.findOneByAccountNumber(itemTesting.getCreatedBy().getAccountNo());
                params.put("PREPARAR", preparar != null ? preparar.getName() : "");
                params.put("PREPARAR_POS", preparar != null && preparar.getPosition() != null ? preparar.getPosition().getName() : "");
            } else {
                params.put("PREPARAR", "");
                params.put("PREPARAR_POS", "");
            }

        }

        return params;

    }

    @Override
    public JRDataSource datasource(Integer id) {
        return new JRBeanCollectionDataSource(this.getItemTestingDetails(id));
    }
}
