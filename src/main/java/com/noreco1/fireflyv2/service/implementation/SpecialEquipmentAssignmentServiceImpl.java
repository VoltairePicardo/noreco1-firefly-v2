package com.noreco1.fireflyv2.service.implementation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.InventoryCategory;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.SpecialEquipmentAssignmentService;
import com.noreco1.fireflyv2.mysql_model.*;
import com.noreco1.fireflyv2.mysql_model.Town;
import com.noreco1.fireflyv2.mysql_repo.*;
import com.noreco1.fireflyv2.validator.SpecialEquipmentAssignmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.*;

import static com.noreco1.fireflyv2.common.GlobalConstant.SPECIAL_EQUIPMENT_TYPES;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
@Service(value = "specialEquipmentAssignmentServiceImpl")
public class SpecialEquipmentAssignmentServiceImpl implements SpecialEquipmentAssignmentService {

    @Autowired
    SpecialEquipmentAssignmentRepo specialEquipmentAssignmentRepo;

    @Autowired
    SpecialEquipmentAssignmentDetailRepo specialEquipmentAssignmentDetailRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    private SettingFacade settingFacade;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    TurnOnOrderWithdrawalDetailRepo turnOnOrderWithdrawalDetailRepo;

    @Autowired
    TurnOnOrderRepo turnOnOrderRepo;

    @Autowired
    TurnOnAccomplishmentRepo turnOnAccomplishmentRepo;

    @Autowired
    ConsumerRepo consumerRepo;

    @Autowired
    MSSQLTownRepo townRepo;

    @Autowired
    BarangayRepo barangayRepo;

    @Autowired
    SitioRepo sitioRepo;

    @Autowired
    SpecialEquipmentAssignmentLogRepo specialEquipmentAssignmentLogRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Override
    public Page<SpecialEquipmentAssignment> findAll(String startDate, String endDate, Pageable pageable) {
        Date formattedStartDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date formattedEndDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return specialEquipmentAssignmentRepo.findAllByDateBetweenOrderByDateAsc(formattedStartDate, formattedEndDate, pageable);
    }

    @Override
    public PostResponse update(SpecialEquipmentAssignment specialEquipmentAssignment, BindingResult bindingResult, MessageSource messageSource) {
        return this.create(specialEquipmentAssignment, bindingResult, messageSource);
    }

    @Override
    public PostResponse create(SpecialEquipmentAssignment specialEquipmentAssignment, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            SpecialEquipmentAssignmentValidator validator = new SpecialEquipmentAssignmentValidator();
            validator.setService(this);
            validator.validate(specialEquipmentAssignment, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save Special Equipment Assignment");
            }else {
                SpecialEquipmentAssignment savedSpecialEquipmentAssignment = null;

                Boolean toBeInserted = !Checker.isValidId(specialEquipmentAssignment.getId());

                if (toBeInserted) {

                    specialEquipmentAssignment.setCreatedBy(authenticationFacade.getLoggedIn());

                    if(!specialEquipmentAssignment.isSoleOwner()){
                        if(specialEquipmentAssignment.getTown() != null){
                            specialEquipmentAssignment.setTownId(specialEquipmentAssignment.getTown().getId());
                        }
                        if(specialEquipmentAssignment.getBarangay() != null){
                            specialEquipmentAssignment.setBarangayId(specialEquipmentAssignment.getBarangay().getId());
                        }
                        if(specialEquipmentAssignment.getSitio() != null){
                            specialEquipmentAssignment.setSitioId(specialEquipmentAssignment.getSitio().getSitioID());
                        }
                    }

                    savedSpecialEquipmentAssignment = specialEquipmentAssignmentRepo.save(specialEquipmentAssignment);
                    if(savedSpecialEquipmentAssignment != null){

                        this.saveSpecialEquipmentAssignmentDetail(savedSpecialEquipmentAssignment, specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails());

                        response.setSuccess(true);
                        response.setModelId(savedSpecialEquipmentAssignment.getId());
                        response.setSuccessMessage("New Special Equipment Assignment has been successfully created");

                    } else{
                        response.setFailureMessage("Failed to create New Special Equipment Assignment");
                    }

                } else{

                    SpecialEquipmentAssignment toBeUpdated = specialEquipmentAssignmentRepo.findById(specialEquipmentAssignment.getId()).orElse(null);

                    if(!specialEquipmentAssignment.isSoleOwner()){
                        if(specialEquipmentAssignment.getTown() != null){
                            specialEquipmentAssignment.setTownId(specialEquipmentAssignment.getTown().getId());
                        }
                        if(specialEquipmentAssignment.getBarangay() != null){
                            specialEquipmentAssignment.setBarangayId(specialEquipmentAssignment.getBarangay().getId());
                        }
                        if(specialEquipmentAssignment.getSitio() != null){
                            specialEquipmentAssignment.setSitioId(specialEquipmentAssignment.getSitio().getSitioID());
                        }
                    }

                    if(toBeUpdated != null){

                        savedSpecialEquipmentAssignment = specialEquipmentAssignmentRepo.save(specialEquipmentAssignment);

                        if(savedSpecialEquipmentAssignment != null){

                            savedSpecialEquipmentAssignment.setConsumer(specialEquipmentAssignment.getConsumer());

                            specialEquipmentAssignmentDetailRepo.deleteAllBySpecialEquipmentAssignmentId(savedSpecialEquipmentAssignment.getId());
                            this.saveSpecialEquipmentAssignmentDetail(savedSpecialEquipmentAssignment, specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails());

                            response.setSuccess(true);
                            response.setModelId(savedSpecialEquipmentAssignment.getId());
                            response.setSuccessMessage("Special Equipment Assignment has been successfully updated");

                        } else{
                            response.setFailureMessage("Failed to update Special Equipment Assignment");
                        }
                    } else {
                        response.setFailureMessage("Special Equipment Assignment is not available.");
                    }

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public PostResponse revoke(SpecialEquipmentAssignment specialEquipmentAssignment) {
        PostResponse response = new PostResponse();
        List<SpecialEquipmentAssignmentDetail> details = specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails();
        List<Integer> specEquipIds = new ArrayList<>();
        for (SpecialEquipmentAssignmentDetail detail : details) {
            detail = specialEquipmentAssignmentDetailRepo.findById(detail.getId()).orElse(null);
            if (detail != null) {
                specialEquipmentAssignmentDetailRepo.deleteById(detail.getId());
                specEquipIds.add(detail.getSpecialEquipmentAssignment().getId());
            } else {
                response.setFailureMessage("Special Equipment Assignment item is not available.");
                return response;
            }
        }

        //logging
        Set<Integer> filteredList = new HashSet<Integer>(specEquipIds);
        for (Integer id : filteredList) {
            SpecialEquipmentAssignment savedSpecialEquipmentAssignment = specialEquipmentAssignmentRepo.findById(id).orElse(null);
            savedSpecialEquipmentAssignment.setSpecialEquipmentAssignmentDetails(specialEquipmentAssignmentDetailRepo.findAllBySpecialEquipmentAssignmentId(savedSpecialEquipmentAssignment.getId()));
            savedSpecialEquipmentAssignment.setRevoke(true);
            documentLoggerFacade.log(savedSpecialEquipmentAssignment, authenticationFacade.getLoggedIn());
        }

        response.setSuccess(true);
        response.setSuccessMessage("Special Equipment Assignment items has been successfully revoked");

        return response;
    }

    @Override
    public SpecialEquipmentAssignment findById(Integer id) {
        SpecialEquipmentAssignment specialEquipmentAssignment = specialEquipmentAssignmentRepo.findById(id).orElse(null);
        boolean hasAccomplishment = false;
        if(specialEquipmentAssignment != null){
            List<SpecialEquipmentAssignmentDetail> details = specialEquipmentAssignmentDetailRepo.findAllBySpecialEquipmentAssignmentId(specialEquipmentAssignment.getId());
            if(Checker.collectionIsNotEmpty(details)){

                if(specialEquipmentAssignment.isHasConnectOrder()){
                    for(SpecialEquipmentAssignmentDetail detail : details){
                        TurnOnOrder turnOnOrder = turnOnOrderRepo.findById(detail.getTurnOnOrderId()).orElse(null);
                        if(turnOnOrder != null) {

                            List<TurnOnAccomplishment> turnOnAccomplishments = turnOnAccomplishmentRepo.findAllByTurnOnOrderId(turnOnOrder.getId());
                            detail.setTurnOnOrder(turnOnOrder);
                            detail.setHasAccomplishment(Checker.collectionIsNotEmpty(turnOnAccomplishments));
                            detail.setInitialReading(detail.getSpecialEquipment().getInitialReading());

                            if(Checker.collectionIsNotEmpty(turnOnAccomplishments)){
                                hasAccomplishment = true;
                            }
                        }

                    }
                } else{
                    SpecialEquipmentAssignmentDetail specialEquipmentAssignmentDetail = details.get(0);
                    Consumer consumer = null;

                    if(specialEquipmentAssignment.isSoleOwner()){
                        if(Checker.isValidId(specialEquipmentAssignmentDetail.getConsumerId())){
                            consumer = consumerRepo.findById(specialEquipmentAssignmentDetail.getConsumerId()).orElse(null);
                            if(consumer != null){
                                specialEquipmentAssignment.setConsumer(consumer);
                            }
                        }
                    } else{

                        if(Checker.isValidId(specialEquipmentAssignment.getTownId())){
                            Town town = townRepo.findById(specialEquipmentAssignment.getTownId()).orElse(null);
                            if(town != null){
                                specialEquipmentAssignment.setTown(town);
                            }
                        }

                        if(Checker.isValidId(specialEquipmentAssignment.getBarangayId())){
                            Barangay barangay = barangayRepo.findById(specialEquipmentAssignment.getBarangayId()).orElse(null);
                            if(barangay != null){
                                specialEquipmentAssignment.setBarangay(barangay);
                            }
                        }

                        if(Checker.isValidId(specialEquipmentAssignment.getSitioId())){
                            Sitio sitio = sitioRepo.findById(specialEquipmentAssignment.getSitioId()).orElse(null);
                            if(sitio != null){
                                specialEquipmentAssignment.setSitio(sitio);
                            }
                        }

                    }

                    for(SpecialEquipmentAssignmentDetail detail : details){

                        detail.setConsumer(consumer);
                        if(detail.getStockTransactionDetail() != null) {
                            detail.setDescription(detail.getStockTransactionDetail().getItemStock().getItem().getDescription());
                            detail.setItemId(detail.getStockTransactionDetail().getItemStock().getItem().getId());
                        }
                        detail.setSerialNo(detail.getSpecialEquipment().getSerialNo());
                    }
                }

                specialEquipmentAssignment.setHasAccomplishment(hasAccomplishment);
                specialEquipmentAssignment.setSpecialEquipmentAssignmentDetails(details);
            }

            List<SpecialEquipmentAssignmentLog> equipmentAssignmentLogs = this.specialEquipmentAssignmentLogRepo.findBySpecialEquipmentAssignmentIdOrderByIdDesc(specialEquipmentAssignment.getId());
            specialEquipmentAssignment.setSpecialEquipmentAssignmentLogs(equipmentAssignmentLogs);
        }

        return specialEquipmentAssignment;
    }

    @Override
    public List<Map> findByConsumerId(Integer consumerId) {
        List<Map> data = new ArrayList<>();

        List<Object[]> issuanceList = this.specialEquipmentAssignmentRepo.findAllByConsumerId(consumerId);
        if(Checker.collectionIsNotEmpty(issuanceList)) {
            for(Object[] row: issuanceList) {

                Map rowMap = new HashMap();
                rowMap.put("docNo", row[0]);
                rowMap.put("date", row[1]);
                rowMap.put("serial", row[2]);
                rowMap.put("remarks", row[3]);
                rowMap.put("item", row[4]);
                rowMap.put("time", row[5]);
                rowMap.put("id", row[6]);
                rowMap.put("status", row[7]);

                data.add(rowMap);
            }
        }

        return data;
    }

    @Override
    public List<Map> findBySerial(String serialNo) {
        List<Map> data = new ArrayList<>();
        SpecialEquipment specialEquipment = this.specialEquipmentRepo.findBySerialNo(serialNo);
        if(specialEquipment != null) {

            List<Object[]> rows = this.specialEquipmentRepo.findSpecialEquipmentTransactionsByEquipmentId(specialEquipment.getId());
            if(Checker.collectionIsNotEmpty(rows)) {
                for (Object [] row:rows) {

                    Map rowMap = new HashMap();
                    rowMap.put("docNo", row[0]);
                    rowMap.put("date", row[1]);
                    rowMap.put("address", row[2]);
                    rowMap.put("remarks", row[3]);
                    rowMap.put("time", row[5]);
                    rowMap.put("newAcctNo", "");
                    rowMap.put("oldAcctNo", "");
                    rowMap.put("name", "");

                    if(row[4] != null) {
                        Integer consumerId = (Integer) row[4];

                        Consumer consumer = this.consumerRepo.findById(consumerId).orElse(null);
                        if(consumer != null) {

                            rowMap.put("name", consumer.getAcctName());
                            rowMap.put("newAcctNo", consumer.getAcctNo());
                            rowMap.put("oldAcctNo", consumer.getOldAccountNo());
                            rowMap.put("address", consumer.getAddress());
                        }
                    }

                    data.add(rowMap);
                }
            }
        }

        return data;
    }

    @Override
    public Page<SpecialEquipment> findAllSpecialEquipment(Pageable pageable) {
        Map specialEquipmentTypeMap = settingFacade.getByCode(SPECIAL_EQUIPMENT_TYPES);
        int meterSpecialEquipmentTypeId = (int) specialEquipmentTypeMap.get("meterSpecialEquipmentTypeId");

        return specialEquipmentRepo.findAllSpecialEquipmentAssignmentPaged(InventoryCategory.SPECIAL_EQUIPMENT.getId(), meterSpecialEquipmentTypeId, pageable);
    }

    @Override
    public Page<SpecialEquipment> findAllSpecialEquipmentByQuery(String query, Pageable pageable) {
        Map specialEquipmentTypeMap = settingFacade.getByCode(SPECIAL_EQUIPMENT_TYPES);
        int meterSpecialEquipmentTypeId = (int) specialEquipmentTypeMap.get("meterSpecialEquipmentTypeId");
        query = "%"+query+"%";

        return specialEquipmentRepo.findAllSpecialEquipmentAssignmentByQueryPaged(query.trim(), InventoryCategory.SPECIAL_EQUIPMENT.getId(), meterSpecialEquipmentTypeId, pageable);
    }

    @Override
    public List<SpecialEquipmentAssignmentDetail> setDetailsWithDefaultSpecialEquipment(Integer stockWithdrawalId) {
        List<SpecialEquipmentAssignmentDetail> specialEquipmentAssignmentDetails = new ArrayList<>();
        StockWithdrawal stockWithdrawal = stockWithdrawalRepo.findById(stockWithdrawalId).orElse(null);
        if (stockWithdrawal == null) {
            stockWithdrawal = stockWithdrawalRepo.findOneByTransactionId(stockWithdrawalId);
        }
        if(stockWithdrawal != null){
            if(Checker.isValidId(stockWithdrawal.getTurnOnOrderWithdrawalId())){
                List<TurnOnOrderWithdrawalDetail> turnOnOrderWithdrawalDetails = turnOnOrderWithdrawalDetailRepo.findAllByTurnOnOrderWithdrawalId(stockWithdrawal.getTurnOnOrderWithdrawalId());

                if(Checker.collectionIsNotEmpty(turnOnOrderWithdrawalDetails)){
                    Map specialEquipmentTypeMap = settingFacade.getByCode(SPECIAL_EQUIPMENT_TYPES);
                    int meterSpecialEquipmentTypeId = (int) specialEquipmentTypeMap.get("meterSpecialEquipmentTypeId");

                    List<SpecialEquipment> specialEquipments = specialEquipmentRepo.findAllDefaultForSpecialEquipmentAssignment(InventoryCategory.SPECIAL_EQUIPMENT.getId(), meterSpecialEquipmentTypeId, turnOnOrderWithdrawalDetails.size());

                    int count = 0;

                    for(TurnOnOrderWithdrawalDetail turnOnOrderWithdrawalDetail : turnOnOrderWithdrawalDetails){
                        SpecialEquipmentAssignmentDetail specialEquipmentAssignmentDetail = new SpecialEquipmentAssignmentDetail();
                        List<TurnOnAccomplishment> turnOnAccomplishments = turnOnAccomplishmentRepo.findAllByTurnOnOrderId(turnOnOrderWithdrawalDetail.getTurnOnOrder().getId());

                        specialEquipmentAssignmentDetail.setTurnOnOrderId(turnOnOrderWithdrawalDetail.getTurnOnOrder().getId());
                        specialEquipmentAssignmentDetail.setTurnOnOrder(turnOnOrderWithdrawalDetail.getTurnOnOrder());
                        specialEquipmentAssignmentDetail.setSpecialEquipment(Checker.collectionIsEmpty(specialEquipments) ? null : specialEquipments.get(count));
                        specialEquipmentAssignmentDetail.setHasAccomplishment(Checker.collectionIsNotEmpty(turnOnAccomplishments));
                        specialEquipmentAssignmentDetails.add(specialEquipmentAssignmentDetail);
                        count++;

                    }

                }
            }
        }

        return specialEquipmentAssignmentDetails;
    }

    @Override
    public List<SpecialEquipment> findAllDefaultForSpecialEquipmentAssignmentNoTurnOn(Integer itemId, Integer noOfItems) {
        return specialEquipmentRepo.findAllDefaultForSpecialEquipmentAssignmentNoTurnOn(itemId, noOfItems);
    }

    @Override
    public Page<SpecialEquipment> findAllSpecialEquipmentNoConnectOrder(Integer itemId,Pageable pageable) {
        return specialEquipmentRepo.findAllSpecialEquipmentAssignmentNoConnectOrderPaged(itemId, pageable);
    }

    @Override
    public Page<SpecialEquipment> findAllSpecialEquipmentByQueryNoConnectOrder(String query, Integer itemId, Pageable pageable) {
        query = "%"+query+"%";
        return specialEquipmentRepo.findAllSpecialEquipmentAssignmentNoConnectOrderByQueryPaged(query.trim(), itemId, pageable);

    }

    @Override
    public List<SpecialEquipmentAssignmentLog> findAllLogsById(Integer id) {
        List<SpecialEquipmentAssignmentLog> logs = this.specialEquipmentAssignmentLogRepo.findBySpecialEquipmentAssignmentIdOrderByIdDesc(id);

        if(Checker.collectionIsNotEmpty(logs)) {
            for(SpecialEquipmentAssignmentLog log: logs) {

                String logDetails = log.getDetails();
                if(!Checker.isStringNullOrEmpty(logDetails)) {

                    try {
                        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
                        Object[] rowDetails = mapper.readValue(logDetails, Object[].class);

                        if(rowDetails != null && rowDetails.length > 0)  {
                            for(Object row: rowDetails) {
                                log.getDetailsMapList().add((Map)row);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return logs;
    }

    private void saveSpecialEquipmentAssignmentDetail(SpecialEquipmentAssignment savedSpecialEquipmentAssignment, List<SpecialEquipmentAssignmentDetail> specialEquipmentAssignmentDetails) {
        if (!Checker.collectionIsEmpty(specialEquipmentAssignmentDetails)) {
            for (SpecialEquipmentAssignmentDetail specialEquipmentAssignmentDetail : specialEquipmentAssignmentDetails) {
                specialEquipmentAssignmentDetail.setSpecialEquipmentAssignment(savedSpecialEquipmentAssignment);

               if(!savedSpecialEquipmentAssignment.isHasConnectOrder() && savedSpecialEquipmentAssignment.isSoleOwner()){
                   if(savedSpecialEquipmentAssignment.getConsumer() != null){
                       specialEquipmentAssignmentDetail.setConsumerId(savedSpecialEquipmentAssignment.getConsumer().getId());
                   }
               }

                specialEquipmentAssignmentDetailRepo.save(specialEquipmentAssignmentDetail);

                SpecialEquipment toUpdate = specialEquipmentRepo.findById(specialEquipmentAssignmentDetail.getSpecialEquipment().getId()).orElse(null);
                if(toUpdate != null){
                    toUpdate.setInitialReading(specialEquipmentAssignmentDetail.getInitialReading());
                    specialEquipmentRepo.save(toUpdate);
                }
            }
        }
    }
}
