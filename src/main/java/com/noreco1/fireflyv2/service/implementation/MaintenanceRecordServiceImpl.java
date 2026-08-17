package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.MaintenanceRecordService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.MaintenanceRecordValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Tri-Nvent on 3/2/2020.
 */
@Service(value = "maintenanceRecordServiceImpl")
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService, PrintableVoucher {

    @Autowired
    MaintenanceRecordRepo maintenanceRecordRepo;

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    VehicleInformationRepo vehicleInformationRepo;

    @Autowired
    AssetWorkRepo assetWorkRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    MaintenanceRecordWorkRepo maintenanceRecordWorkRepo;

    @Autowired
    MaintenanceRecordOtherItemRepo maintenanceRecordOtherItemRepo;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    MaintenanceRecordMaterialReleaseRepo maintenanceRecordMaterialReleaseRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    MaintenanceRecordMaterialReleaseItemRepo maintenanceRecordMaterialReleaseItemRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Override
    public Page<MaintenanceRecord> findAll(String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return maintenanceRecordRepo.findAllByMaintenanceDateBetweenOrderByCode(fromDate, toDate, pageable);
    }

    @Override
    public Page<MaintenanceRecord> findAllByQuery(String query, String startDate, String endDate, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return maintenanceRecordRepo.findAllByMaintenanceDateBetweenAndAssetDescriptionContainsIgnoreCaseOrderByCode(fromDate, toDate, query, pageable);
    }

    @Override
    public Page<MaintenanceRecord> findAllByDateRangeAndAssetTypeId(String startDate, String endDate, Integer assetTypeId, Pageable pageable) {
        Date fromDate = DateHelper.strToDate(startDate, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(endDate, "yyyy-MM-dd");
        return maintenanceRecordRepo.findAllByMaintenanceDateBetweenAndAssetAssetTypeId(fromDate, toDate, assetTypeId, pageable);
    }

    @Override
    public MaintenanceRecord findById(Integer id) {

        MaintenanceRecord maintenanceRecord = maintenanceRecordRepo.findById(id).orElse(null);
        List<MaintenanceRecordWork> works = maintenanceRecordWorkRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
        maintenanceRecord.setMaintenanceRecordWorks(works);

        return maintenanceRecord;
    }

    @Override
    public PostResponse update(MaintenanceRecord maintenanceRecord, BindingResult bindingResult, MessageSource messageSource) {
        return this.create(maintenanceRecord, bindingResult, messageSource);
    }

    @Override
    public PostResponse create(MaintenanceRecord maintenanceRecord, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try{

            MaintenanceRecordValidator validator = new MaintenanceRecordValidator();
            validator.setService(this);
            validator.validate(maintenanceRecord, bindingResult);

            if(bindingResult.hasErrors()){
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
                response.setFailureMessage("Failed to save Maintenance Order");
            }else {
                MaintenanceRecord savedMaintenanceRecord = null;

                List<MaintenanceRecordWork> maintenanceRecordWorks = maintenanceRecord.getMaintenanceRecordWorks();
                List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases = maintenanceRecord.getMaintenanceRecordMaterialReleases();
                List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems  = maintenanceRecord.getMaintenanceRecordOtherItems();

                Boolean toBeInserted = !Checker.isValidId(maintenanceRecord.getId());

                if (toBeInserted) {

                    Integer maintenanceRecordYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(maintenanceRecord.getMaintenanceDate()));
                    Object latestCode = maintenanceRecordRepo.findLatestCodeByYear(maintenanceRecordYear);
                    String code = generatorFacade.voucherCodeNoOffice("AMO", (latestCode == null ? "" : String.valueOf(latestCode)), maintenanceRecord.getMaintenanceDate(), GlobalConstant.COUNTER_PAD_4);

                    maintenanceRecord.setCode(code);
                    maintenanceRecord.setDate(new Date());

                    savedMaintenanceRecord = maintenanceRecordRepo.save(maintenanceRecord);

                    if(savedMaintenanceRecord != null){

                        if (!Checker.collectionIsEmpty(maintenanceRecordWorks)) {
                            for (MaintenanceRecordWork work : maintenanceRecordWorks) {
                                work.setMaintenanceRecord(savedMaintenanceRecord);
                                maintenanceRecordWorkRepo.save(work);
                            }
                        }
                        if (!Checker.collectionIsEmpty(maintenanceRecordOtherItems)) {
                            for (MaintenanceRecordOtherItem item : maintenanceRecordOtherItems) {
                                item.setMaintenanceRecord(savedMaintenanceRecord);
                                maintenanceRecordOtherItemRepo.save(item);
                            }
                        }
                        if (!Checker.collectionIsEmpty(maintenanceRecordMaterialReleases)) {
                            this.saveStockReleaseItems(maintenanceRecord, maintenanceRecordMaterialReleases, savedMaintenanceRecord);
                        }

                        response.setSuccess(true);
                        response.setModelId(savedMaintenanceRecord.getId());
                        response.setSuccessMessage("New Maintenance Order has been successfully created");
                    } else{
                        response.setFailureMessage("Failed to create New Maintenance Order");
                    }

                } else{

                    MaintenanceRecord toBeUpdated = maintenanceRecordRepo.findById(maintenanceRecord.getId()).orElse(null);
                    if(toBeUpdated != null){

                        toBeUpdated.setAsset(maintenanceRecord.getAsset());
                        toBeUpdated.setMaintenanceDate(maintenanceRecord.getMaintenanceDate());
                        toBeUpdated.setNextPmsDate(maintenanceRecord.getNextPmsDate());
                        toBeUpdated.setOdometerReading(maintenanceRecord.getOdometerReading());
                        toBeUpdated.setVoucherTransaction(maintenanceRecord.getVoucherTransaction());

                        savedMaintenanceRecord = maintenanceRecordRepo.save(toBeUpdated);
                        if(savedMaintenanceRecord != null){

                            maintenanceRecordWorkRepo.deleteAllByMaintenanceRecordId(savedMaintenanceRecord.getId());
                            maintenanceRecordMaterialReleaseRepo.deleteAllByMaintenanceRecordId(savedMaintenanceRecord.getId());
                            maintenanceRecordOtherItemRepo.deleteAllByMaintenanceRecordId(savedMaintenanceRecord.getId());
                            maintenanceRecordMaterialReleaseItemRepo.deleteAllByMaintenanceRecordId(savedMaintenanceRecord.getId());

                            if (!Checker.collectionIsEmpty(maintenanceRecordWorks)) {
                                for (MaintenanceRecordWork work : maintenanceRecordWorks) {
                                    work.setMaintenanceRecord(savedMaintenanceRecord);
                                    maintenanceRecordWorkRepo.save(work);
                                }
                            }

                            if (!Checker.collectionIsEmpty(maintenanceRecordMaterialReleases)) {
                                this.saveStockReleaseItems(maintenanceRecord, maintenanceRecordMaterialReleases, savedMaintenanceRecord);
                            }

                            if (!Checker.collectionIsEmpty(maintenanceRecordOtherItems)) {
                                for (MaintenanceRecordOtherItem item : maintenanceRecordOtherItems) {
                                    item.setMaintenanceRecord(savedMaintenanceRecord);
                                    maintenanceRecordOtherItemRepo.save(item);
                                }
                            }

                            response.setSuccess(true);
                            response.setModelId(savedMaintenanceRecord.getId());
                            response.setSuccessMessage("Maintenance Order has been successfully updated");
                        } else{
                            response.setFailureMessage("Failed to update Maintenance Order");
                        }
                    } else {
                        response.setFailureMessage("Maintenance Order is not available.");
                    }

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public MaintenanceRecordDto findOne(Integer id) {
        MaintenanceRecord maintenanceRecord =  maintenanceRecordRepo.findById(id).orElse(null);
        MaintenanceRecordDto maintenanceRecordDto = new MaintenanceRecordDto();

        if(maintenanceRecord != null){

            maintenanceRecordDto.setId(maintenanceRecord.getId());
            maintenanceRecordDto.setCode(maintenanceRecord.getCode());
            maintenanceRecordDto.setAsset(maintenanceRecord.getAsset());
            maintenanceRecordDto.setDate(maintenanceRecord.getDate());
            maintenanceRecordDto.setMaintenanceDate(maintenanceRecord.getMaintenanceDate());
            maintenanceRecordDto.setOdometerReading(maintenanceRecord.getOdometerReading());
            maintenanceRecordDto.setNextPmsDate(maintenanceRecord.getNextPmsDate());
            maintenanceRecordDto.setVoucherTransaction(maintenanceRecord.getVoucherTransaction());

            VoucherDto voucherDto = null;

            List<MaintenanceRecordWork> works = maintenanceRecordWorkRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
            maintenanceRecordDto.setMaintenanceRecordWorks(works);

            List<MaintenanceRecordOtherItem> otherItems = maintenanceRecordOtherItemRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
            maintenanceRecordDto.setMaintenanceRecordOtherItems(otherItems);

            if(maintenanceRecord.getVoucherTransaction() != null){

                Object[] voucher = documentRepo.findLinkedVoucherForMaintenanceRecord(maintenanceRecord.getVoucherTransaction().getId()).get(0);
                if(voucher != null){
                    voucherDto = new VoucherDto();
                    voucherDto.setTransactionId((Integer) voucher[0]);
                    voucherDto.setCode((String) voucher[1]);
                    voucherDto.setDate((Date) voucher[2]);
                    voucherDto.setParticulars((String) voucher[3]);
                }
            }

            List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases = maintenanceRecordMaterialReleaseRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
            ArrayList<MaintenanceRecordMaterialReleaseDto> maintenanceRecordMaterialReleaseDtos = new ArrayList<>();

            if(Checker.collectionIsNotEmpty(maintenanceRecordMaterialReleases)){

                for(MaintenanceRecordMaterialRelease materialRelease : maintenanceRecordMaterialReleases){

                    MaintenanceRecordMaterialReleaseDto maintenanceRecordMaterialReleaseDto = new MaintenanceRecordMaterialReleaseDto();

                    ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(materialRelease.getStockRelease().getTransaction().getId());
                    List<MaintenanceRecordMaterialReleaseItem> materialReleaseItems = maintenanceRecordMaterialReleaseItemRepo.findAllByMaintenanceRecordIdAndStockReleaseId(maintenanceRecord.getId(), materialRelease.getStockRelease().getId());

                    maintenanceRecordMaterialReleaseDto.setId(materialRelease.getStockRelease().getId());
                    maintenanceRecordMaterialReleaseDto.setCode(materialRelease.getStockRelease().getCode());
                    maintenanceRecordMaterialReleaseDto.setDescription(materialRelease.getStockRelease().getDescription());
                    maintenanceRecordMaterialReleaseDto.setVoucherDate(materialRelease.getStockRelease().getVoucherDate());
                    maintenanceRecordMaterialReleaseDto.setStockRelease(materialRelease.getStockRelease());
                    maintenanceRecordMaterialReleaseDto.setItems(details);
                    maintenanceRecordMaterialReleaseDto.setStockReleaseItems(materialReleaseItems);

                    maintenanceRecordMaterialReleaseDtos.add(maintenanceRecordMaterialReleaseDto);
                }

            }

            maintenanceRecordDto.setMaintenanceRecordMaterialReleases(maintenanceRecordMaterialReleaseDtos);

            maintenanceRecordDto.setVoucher(voucherDto);

            VehicleInformation vehicleInformation = vehicleInformationRepo.findByAssetId(maintenanceRecord.getAsset().getId());

            if (vehicleInformation != null){

                maintenanceRecordDto.setVehicleInformation(vehicleInformation);

            }

        }

        return maintenanceRecordDto;
    }

    @Override
    public HashMap reportParameters(Integer id, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MaintenanceRecord maintenanceRecord = maintenanceRecordRepo.findById(id).orElse(null);

        if (maintenanceRecord != null) {
            User loggedIn = authenticationFacade.getLoggedIn();

            Asset asset = assetRepo.findById(maintenanceRecord.getAsset().getId()).orElse(null);
            Employee mechanic = employeeRepo.findOneByAccountNumber(loggedIn.getAccountNo());
            VehicleInformation vehicleInformation = vehicleInformationRepo.findByAssetId(asset.getId());

            List<HashMap> materials = new ArrayList<>();
            List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems = maintenanceRecordOtherItemRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());

            List<Map> linkedVouchers = new ArrayList<>();

            if(maintenanceRecord.getVoucherTransaction() != null){
                Map voucherDto  = new HashMap();
                Object[] voucher = documentRepo.findLinkedVoucherForMaintenanceRecord(maintenanceRecord.getVoucherTransaction().getId()).get(0);
                if(voucher != null){
                    voucherDto.put("code", voucher[1]);
                    linkedVouchers.add(voucherDto);
                }
            }

            BigDecimal materialsTotalAmount = BigDecimal.ZERO;
            BigDecimal laborsTotalAmount = BigDecimal.ZERO;
            BigDecimal totalLaborAmount = maintenanceRecordWorkRepo.totalLaborAmount(maintenanceRecord.getId());

            if(Checker.isAmountGreaterThanZero(totalLaborAmount)){
                laborsTotalAmount = totalLaborAmount;
            }

            List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases = maintenanceRecordMaterialReleaseRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
            if(Checker.collectionIsNotEmpty(maintenanceRecordMaterialReleases)){

                for(MaintenanceRecordMaterialRelease materialRelease : maintenanceRecordMaterialReleases){
                    Map materialReleaseDto  = new HashMap();
                    materialReleaseDto.put("code", materialRelease.getStockRelease().getCode());
                    linkedVouchers.add(materialReleaseDto);
                }

            }
            List<MaintenanceRecordMaterialReleaseItem> materialReleaseItems = maintenanceRecordMaterialReleaseItemRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
            if(Checker.collectionIsNotEmpty(materialReleaseItems)){

                for(MaintenanceRecordMaterialReleaseItem releaseItem : materialReleaseItems){

                    HashMap<String, Object> material = new HashMap<>();

                    material.put("description", releaseItem.getDescription());
                    material.put("amount", releaseItem.getAmount());

                    materialsTotalAmount = materialsTotalAmount.add(releaseItem.getAmount());

                    materials.add(material);
                }
            }

            // add amount of other items
            if(Checker.collectionIsNotEmpty(maintenanceRecordOtherItems)){
                for(MaintenanceRecordOtherItem item : maintenanceRecordOtherItems){
                    materialsTotalAmount = materialsTotalAmount.add(item.getAmount());
                }
            }

            params.put("CODE", maintenanceRecord.getCode());
            params.put("VEHICLE_CODE", vehicleInformation != null ? vehicleInformation.getCode() : "");
            params.put("ODOMETER_READING", maintenanceRecord.getOdometerReading());
            params.put("DATE", maintenanceRecord.getMaintenanceDate());
            params.put("NEXT_PMS_DATE", maintenanceRecord.getNextPmsDate());
            params.put("MECHANIC", mechanic.getName().toUpperCase());
            params.put("MECHANIC_POS", mechanic.getPosition() == null ? "":mechanic.getPosition().getName());

            params.put("SUB_REPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/support-modules/sub/");
            params.put("MATERIALS", new JRBeanCollectionDataSource(materials));
            params.put("OTHER_ITEMS", new JRBeanCollectionDataSource(maintenanceRecordOtherItems));
            params.put("VOUCHERS", new JRBeanCollectionDataSource(linkedVouchers));
            params.put("GRAND_TOTAL_AMOUNT", laborsTotalAmount.add(materialsTotalAmount));

        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer id) {
        List<MaintenanceRecordWork> maintenanceRecordWorks = new ArrayList<>();
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepo.findById(id).orElse(null);
        if (maintenanceRecord != null) {
            maintenanceRecordWorks = maintenanceRecordWorkRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
        }
        return new JRBeanCollectionDataSource(maintenanceRecordWorks);
    }

    private boolean itemIsExcluded(List<Map> excludedStockReleaseItems, StockRelease stockRelease, ItemStock itemStock) {

        if(Checker.collectionIsNotEmpty(excludedStockReleaseItems)) {

            for(Map row: excludedStockReleaseItems) {

                Integer stockReleaseId = (Integer) row.get("stockReleaseId");
                Integer itemStockId = (Integer) row.get("itemStockId");

                if(stockRelease.getId().equals(stockReleaseId) && itemStock.getId().equals(itemStockId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void saveStockReleaseItems(MaintenanceRecord fromFrontEndRecord, List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases, MaintenanceRecord savedMaintenanceRecord) {

        for (MaintenanceRecordMaterialRelease materialRelease : maintenanceRecordMaterialReleases) {

            materialRelease.setMaintenanceRecord(savedMaintenanceRecord);
            maintenanceRecordMaterialReleaseRepo.save(materialRelease);

            StockRelease stockRelease = stockReleaseRepo.findById(materialRelease.getStockRelease().getId()).orElse(null);
            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockRelease.getTransaction().getId());

            for(StockTransactionDetail detail: details) {

                // check if User opted not to include SR items
                if( ! this.itemIsExcluded(fromFrontEndRecord.getExcludedStockReleaseItems(), materialRelease.getStockRelease(), detail.getItemStock()) ) {

                    MaintenanceRecordMaterialReleaseItem item = new MaintenanceRecordMaterialReleaseItem();
                    item.setAmount(detail.getTotalCost());
                    item.setDescription(detail.getItemStock().getItem().getDescription());
                    item.setItem(detail.getItemStock().getItem());
                    item.setMaintenanceRecord(savedMaintenanceRecord);
                    item.setStockRelease(materialRelease.getStockRelease());

                    maintenanceRecordMaterialReleaseItemRepo.save(item);
                }
            }
        }
    }
}
