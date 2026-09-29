package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.controller.response.MeterTestingRecordDto;
import com.noreco1.fireflyv2.controller.response.MeterTestingResultDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.mssql_model.Consumer;
import com.noreco1.fireflyv2.mssql_model.MeterModel;
import com.noreco1.fireflyv2.mssql_repo.MssqlConsumerRepo;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.service.MeterTestingService;
import com.noreco1.fireflyv2.validator.MeterTestingValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterTestingServiceImpl implements MeterTestingService {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final String NO_HEADER_LABEL = "No.";
    private static final String TESTER_PREFIX = "Tester:";
    private static final String DATE_TESTED_PREFIX = "Date Tested:";
    private static final String PASSED_LABEL = "PASSED";
    private static final String FAILED_LABEL = "FAILED";

    private final MeterTestingRepo meterTestingRepo;
    private final MeterTestingDetailRepo meterTestingDetailRepo;
    private final MeterTestingOptionDetailRepo meterTestingOptionDetailRepo;
    private final MeterTestingOptionRepo meterTestingOptionRepo;
    private final AuthenticationFacade authenticationFacade;
    private final MeterRepo meterRepo;
    private final com.noreco1.fireflyv2.mssql_repo.MssqlMeterRepo mssqlMeterRepo;
    private final MeterModelRepo meterModelRepo;
    private final com.noreco1.fireflyv2.mssql_repo.MssqlUserRepo mssqlUserRepo;
    private final UserRepo userRepo;
    private final MssqlConsumerRepo mssqlConsumerRepo;
    private final EmployeeRepo employeeRepo;

    @Override
    @Transactional("chainedTransactionManager")
    public PostResponse create(MeterTesting meterTesting, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {
            MeterTestingValidator validator = new MeterTestingValidator();
            validator.validate(meterTesting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            List<MeterTestingDetail> details = meterTesting.getDetails();

            if (meterTesting.getMeterCalibrator() != null && meterTesting.getMeterCalibrator().getAccountNo() != null) {
                meterTesting.setMeterCalibrator(userRepo.findOneByAccountNo(meterTesting.getMeterCalibrator().getAccountNo()));
            } else {
                meterTesting.setMeterCalibrator(null);
            }

            meterTesting.setId(null);
            meterTesting.setCreatedBy(authenticationFacade.getLoggedIn());
            meterTesting.setCreatedAt(new Date());
            meterTesting.setUpdatedAt(new Date());
            MeterTesting saved = meterTestingRepo.save(meterTesting);

            meterModelRepo.findById(meterTesting.getMeterModel().getId())
                    .orElseThrow(() -> new BusinessException("Meter Model with id: "+meterTesting.getMeterModel().getId()+" not found."));

            List<String> serialNos = details.stream()
                    .map(MeterTestingDetail::getMeterSerialNo)
                    .collect(Collectors.toList());
            Set<String> existingSerialNos = meterRepo.findBySerialNoIn(serialNos).stream()
                    .map(Meter::getSerialNo)
                    .collect(Collectors.toSet());

            List<MeterTestingDetail> detailList = new ArrayList<>();
            List<Meter> meterList = new ArrayList<>();
            List<Map<String, Object>> duplicates = new ArrayList<>();

            for (MeterTestingDetail detail : details) {
                detail.setId(null);
                detail.setMeterTesting(saved);

                detailList.add(detail);

                if (existingSerialNos.contains(detail.getMeterSerialNo())) {
                    Map<String, Object> duplicate = new HashMap<>();
                    duplicate.put("meterSerialNo", detail.getMeterSerialNo());
                    duplicates.add(duplicate);
                    continue;
                }

                Meter meter = new Meter();
                meter.setSerialNo(detail.getMeterSerialNo());
                meter.setMeterModel(meterTesting.getMeterModel());
                meter.setMultiplier(meterTesting.getMultiplier());
                meter.setCreatedBy(authenticationFacade.getLoggedIn());
                meter.setCreatedAt(new Date());
                meter.setUpdatedAt(new Date());

                meterList.add(meter);
            }

            meterTestingDetailRepo.saveAll(detailList);
            List<Meter> savedMeters = meterRepo.saveAll(meterList);

            syncMetersToMssql(savedMeters);

            response.setModelId(saved.getId());
            response.setDuplicates(duplicates);

            if (duplicates.isEmpty()) {
                response.setSuccessMessage("Meter Testing successfully saved.");
            } else {
                response.setSuccessMessage("Meter Testing successfully saved. " + duplicates.size()
                        + " meter(s) already exist and were not re-created.");
            }

        } catch(BusinessException e) {
            throw new BusinessException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
    }

    @Override
    @Transactional()
    public PostResponse createIndividual(MeterTesting meterTesting, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        try {
            MeterTestingValidator validator = new MeterTestingValidator();
            validator.validate(meterTesting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            List<MeterTestingDetail> details = meterTesting.getDetails();
            List<MeterTestingOptionDetail> optionDetails = meterTesting.getOptionDetails();

            if (meterTesting.getMeterCalibrator() != null && meterTesting.getMeterCalibrator().getAccountNo() != null) {
                User meterCalibrator = userRepo.findOneByAccountNo(meterTesting.getMeterCalibrator().getAccountNo());
                if (meterCalibrator == null) {
                    throw new BusinessException("Meter calibrator with account no: " + meterTesting.getMeterCalibrator().getAccountNo() + " not found.");
                }
                meterTesting.setMeterCalibrator(meterCalibrator);
                meterTesting.setTestedBy(meterCalibrator.getFullName());
            } else {
                meterTesting.setMeterCalibrator(null);
            }

            meterModelRepo.findById(meterTesting.getMeterModel().getId())
                    .orElseThrow(() -> new BusinessException("Meter Model with id: " + meterTesting.getMeterModel().getId() + " not found."));

            if (meterTesting.getMeter() != null && meterTesting.getMeter().getId() != null) {
                Meter existingMeter = meterRepo.findById(meterTesting.getMeter().getId())
                        .orElseThrow(() -> new BusinessException("Meter with id: " + meterTesting.getMeter().getId() + " not found."));
                meterTesting.setMeter(existingMeter);
            } else if (isPrivatelyOwned(meterTesting)) {
                meterTesting.setMeter(savePrivateMeter(meterTesting));
            } else {
                meterTesting.setMeter(null);
            }

            meterTesting.setId(null);
            meterTesting.setCreatedBy(authenticationFacade.getLoggedIn());
            meterTesting.setCreatedAt(new Date());
            meterTesting.setUpdatedAt(new Date());
            MeterTesting saved = meterTestingRepo.save(meterTesting);

            for (MeterTestingDetail detail : details) {
                detail.setId(null);
                detail.setMeterTesting(saved);
            }
            meterTestingDetailRepo.saveAll(details);

            if (!Checker.collectionIsEmpty(optionDetails)) {
                for (MeterTestingOptionDetail optionDetail : optionDetails) {
                    optionDetail.setId(null);
                    optionDetail.setMeterTesting(saved);
                }
                meterTestingOptionDetailRepo.saveAll(optionDetails);
            }

            response.setModelId(saved.getId());
            response.setSuccessMessage("Meter Testing successfully saved.");

        } catch(BusinessException e) {
            throw new BusinessException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
    }

    private boolean isPrivatelyOwned(MeterTesting meterTesting) {
        return meterTesting.getOwner() != null && !meterTesting.getOwner().isBlank();
    }

    private Meter savePrivateMeter(MeterTesting meterTesting) {
        String serialNo = Checker.collectionIsEmpty(meterTesting.getDetails())
                ? null
                : meterTesting.getDetails().getFirst().getMeterSerialNo();

        if (serialNo == null || serialNo.isBlank()) {
            throw new BusinessException("Meter serial number is required for a privately-owned meter.");
        }

        Meter meter = meterRepo.findFirstBySerialNoOrderByIdDesc(serialNo).orElse(null);

        if (meter == null) {
            meter = new Meter();
            meter.setSerialNo(serialNo);
            meter.setCreatedBy(authenticationFacade.getLoggedIn());
            meter.setCreatedAt(new Date());
        }

        meter.setMeterModel(meterTesting.getMeterModel());
        meter.setMultiplier(meterTesting.getMultiplier());
        meter.setPresentReading(meterTesting.getPresentReading());
        meter.setReadingDate(meterTesting.getDate());
        meter.setOwner(meterTesting.getOwner());
        meter.setOwnerAddress(meterTesting.getOwnerAddress());
        meter.setUpdatedAt(new Date());

        return meterRepo.save(meter);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MeterTesting> findAll(Pageable pageable) {
        return meterTestingRepo.findAllByOrderByDateDesc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<MeterTesting> find(String testedBy, Pageable pageable) {
        return meterTestingRepo.findAllByTestedByContainingIgnoreCaseOrderByDateDesc(testedBy.trim(), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public MeterTesting findById(Integer id) {
        MeterTesting meterTesting = meterTestingRepo.findById(id).orElse(null);
        if (meterTesting != null) {
            meterTesting.setDetails(meterTestingDetailRepo.findByMeterTestingId(id));
            meterTesting.setOptionDetails(meterTestingOptionDetailRepo.findByMeterTestingId(id));
        }
        return meterTesting;
    }

    @Transactional(readOnly = true)
    @Override
    public List<MeterTestingOption> findActiveOptions() {
        return meterTestingOptionRepo.findByActiveTrueOrderByOptionTypeIdAscOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public HashMap<String, Object> meterTestingParameters(HttpServletRequest request, Integer id) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MeterTesting meterTesting = meterTestingRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Meter testing not found."));

        List<MeterTestingDetail> details = meterTestingDetailRepo.findByMeterTestingId(id);
        Meter meter = meterTesting.getMeter();
        com.noreco1.fireflyv2.model.MeterModel meterModel = meter != null && meter.getMeterModel() != null
                ? meter.getMeterModel()
                : meterTesting.getMeterModel();
        String serialNo = meter != null
                ? meter.getSerialNo()
                : details.stream().map(MeterTestingDetail::getMeterSerialNo).filter(Objects::nonNull).findFirst().orElse(null);
        Consumer mssqlConsumer = meterTesting.getAccountNo() != null
                ? mssqlConsumerRepo.findByAccountNo(meterTesting.getAccountNo())
                : null;

        List<MeterTestingOptionDetail> optionDetails = meterTestingOptionDetailRepo.findByMeterTestingId(id);

        List<Integer> reasons = optionDetails.stream()
                .filter(detail -> Objects.equals(detail.getMeterTestingOption().getOptionType().getId(), com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.REASON.getId()))
                .map(detail -> detail.getMeterTestingOption().getId())
                .toList();

        List<Integer> remarks = optionDetails.stream()
                .filter(detail -> Objects.equals(detail.getMeterTestingOption().getOptionType().getId(), com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.REMARK.getId()))
                .map(detail -> detail.getMeterTestingOption().getId())
                .toList();

        List<Integer> recommendations = optionDetails.stream()
                .filter(detail -> Objects.equals(detail.getMeterTestingOption().getOptionType().getId(), com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.RECOMMENDATION.getId()))
                .map(detail -> detail.getMeterTestingOption().getId())
                .toList();

        params.put("BILLING_NAME", mssqlConsumer != null ? mssqlConsumer.getAccountName() : meterTesting.getOwner());
        params.put("ADDRESS", mssqlConsumer != null ? mssqlConsumer.getAddress() : meterTesting.getOwnerAddress());
        params.put("ACCOUNT_NO", mssqlConsumer != null ? mssqlConsumer.getAccountNo() + " / " + mssqlConsumer.getOldAccountNo().trim() : null);
        params.put("SERIAL_NO", serialNo);
        params.put("CLASS", meterModel != null && meterModel.getAccuracyClass() != null ? meterModel.getAccuracyClass().getDescription() : null);
        params.put("DATE", meterTesting.getDate());
        params.put("READING", meterTesting.getPresentReading());
        params.put("METER", meterModel != null ? meterModel.getModelName() : null);
        params.put("METER_CALIBRATOR", meterTesting.getMeterCalibrator() != null ? meterTesting.getMeterCalibrator().getFullName() : null);

        //Reason
        params.put("REASON_1", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.HIGH_CONSUMPTION.getId()));
        params.put("REASON_2", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.BURNT.getId()));
        params.put("REASON_3", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.RE_CALIBRATION.getId()));
        params.put("REASON_4", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.NOT_RUNNING.getId()));
        params.put("REASON_5", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.BROKEN_COVER.getId()));
        params.put("REASON_6", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.NEW_KWH_METER.getId()));
        params.put("REASON_7", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.FILLED_WITH_WATER.getId()));
        params.put("REASON_8", reasons.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.OTHERS.getId()));
        params.put("REASON_REMARK", otherRemarksByOptionType(optionDetails, com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.REASON.getId()));

        //Remarks
        params.put("REMARK_1", remarks.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.NORMAL.getId()));
        params.put("REMARK_2", remarks.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.DEFECTIVE.getId()));
        params.put("REMARK_3", remarks.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.OTHERS_2.getId()));
        params.put("OTHER_REMARK", otherRemarksByOptionType(optionDetails, com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.REMARK.getId()));

        //Recommendation
        params.put("RECO_1", recommendations.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.FOR_AVERAGING.getId()));
        params.put("RECO_2", recommendations.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.READY_FOR_INSTALLATION.getId()));
        params.put("RECO_3", recommendations.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.OTHERS_3.getId()));
        params.put("RECO_4", recommendations.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.FOR_REPLACEMENT.getId()));
        params.put("RECO_5", recommendations.contains(com.noreco1.fireflyv2.model.enums.MeterTestingOption.READY_FOR_REINSTALLATION.getId()));
        params.put("RECO_REMARK", otherRemarksByOptionType(optionDetails, com.noreco1.fireflyv2.model.enums.MeterTestingOptionType.RECOMMENDATION.getId()));

        return params;
    }

    private String otherRemarksByOptionType(List<MeterTestingOptionDetail> optionDetails, int optionTypeId) {
        return optionDetails.stream()
                .filter(detail -> detail.getMeterTestingOption().getOptionType().getId() == optionTypeId)
                .map(MeterTestingOptionDetail::getOtherRemarks)
                .filter(remark -> remark != null && !remark.isBlank())
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource datasourceMeterTesting(Integer id) {
        List<MeterTestingDetail> meterTestingDetails = meterTestingDetailRepo.findByMeterTestingId(id);
        return new JRBeanCollectionDataSource(meterTestingDetails);
    }

    private void syncMetersToMssql(List<Meter> mysqlMeters) {
        if (mysqlMeters.isEmpty()) {
            return;
        }

        List<Integer> ids = mysqlMeters.stream().map(Meter::getId).collect(Collectors.toList());
        Map<Integer, com.noreco1.fireflyv2.mssql_model.Meter> existingById = mssqlMeterRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(com.noreco1.fireflyv2.mssql_model.Meter::getId, m -> m));

        List<Integer> creatorAccountNumbers = mysqlMeters.stream()
                .map(Meter::getCreatedBy)
                .filter(Objects::nonNull)
                .map(User::getAccountNo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, com.noreco1.fireflyv2.mssql_model.User> mssqlUserByAccountNumber = mssqlUserRepo.findAllByAccountNumberIn(creatorAccountNumbers).stream()
                .collect(Collectors.toMap(com.noreco1.fireflyv2.mssql_model.User::getAccountNumber, u -> u));

        List<com.noreco1.fireflyv2.mssql_model.Meter> mssqlMeters = new ArrayList<>();

        for (Meter mysqlMeter : mysqlMeters) {
            com.noreco1.fireflyv2.mssql_model.Meter mssqlMeter = existingById.get(mysqlMeter.getId());

            if (mssqlMeter == null) {
                mssqlMeter = new com.noreco1.fireflyv2.mssql_model.Meter();
                mssqlMeter.setId(mysqlMeter.getId());
                mssqlMeter.setCreatedAt(mysqlMeter.getCreatedAt());

                if (mysqlMeter.getCreatedBy() != null) {
                    com.noreco1.fireflyv2.mssql_model.User mssqlUser = mssqlUserByAccountNumber.get(mysqlMeter.getCreatedBy().getAccountNo());

                    mssqlMeter.setCreatedBy(mssqlUser != null ? mssqlUser.getId() : null);
                }
            }

            MeterModel meterModel = new MeterModel();
            meterModel.setId(mysqlMeter.getMeterModel().getId());

            mssqlMeter.setSerialNo(mysqlMeter.getSerialNo());
            mssqlMeter.setMeterModel(meterModel);
            mssqlMeter.setMultiplier(mysqlMeter.getMultiplier());
            mssqlMeter.setUpdatedAt(mysqlMeter.getUpdatedAt());

            mssqlMeters.add(mssqlMeter);
        }

        mssqlMeterRepo.saveAll(mssqlMeters);
    }

    @Override
    public MeterTestingResultDto extractMeterTestingData(MultipartFile excelFile) {

        MeterTestingResultDto result = new MeterTestingResultDto();
        List<MeterTestingRecordDto> records = new ArrayList<>();

        try (InputStream inputStream = new BufferedInputStream(excelFile.getInputStream());
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            String specs = getStringCellValue(getCell(sheet, 1, 0));

            String temp = null;
            String rh = null;

            for (String spec : specs.split("\\s+")) {
                if (spec.startsWith("Temp:")) {
                    temp = spec.substring(5);
                } else if (spec.startsWith("R.H.:")) {
                    rh = spec.substring(5);
                }
            }

            result.setReportTitle(getStringCellValue(getCell(sheet, 0, 0)));
            result.setSpecifications(specs);
            result.setTemperature(temp);
            result.setRelativeHumidity(rh);

            int headerRowIndex = findHeaderRowIndex(sheet);
            int dataStartRowIndex = headerRowIndex + 3;

            for (int i = dataStartRowIndex; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String noText = getStringCellValue(row.getCell(0));
                String meterSerialNo = getStringCellValue(row.getCell(2));

                if (noText.isBlank() || meterSerialNo.isBlank() || "0".equals(meterSerialNo)) {
                    captureFooterMetadata(result, row);
                    continue;
                }

                MeterTestingRecordDto record = new MeterTestingRecordDto();
                record.setNo(parseInteger(noText));
                record.setActualDateOfTesting(getStringCellValue(row.getCell(1)));
                record.setMeterSerialNo(meterSerialNo);
                record.setSealNo(getStringCellValue(row.getCell(3)));
                record.setError(getNumericCellValue(row.getCell(4)));
                record.setSta(parsePassedBoolean(row.getCell(5)));
                record.setCrp(parsePassedBoolean(row.getCell(6)));
                record.setVoltageTest(parsePassedBoolean(row.getCell(7)));
                record.setResult(parsePassedBoolean(row.getCell(8)));

                records.add(record);
            }

        } catch (IOException ex) {
            throw new RuntimeException("Failed to read the meter testing excel file", ex);
        }

        result.setRecords(records);
        result.setTotalCount(records.size());
        result.setPassedCount((int) records.stream().filter(r -> Boolean.TRUE.equals(r.getResult())).count());
        result.setFailedCount(result.getTotalCount() - result.getPassedCount());

        return result;
    }

    private void captureFooterMetadata(MeterTestingResultDto result, Row row) {
        for (Cell cell : row) {
            String value = getStringCellValue(cell);
            if (value.isBlank()) {
                continue;
            }
            if (value.startsWith(TESTER_PREFIX)) {
                result.setTester(value.substring(TESTER_PREFIX.length()).trim());
            } else if (value.startsWith(DATE_TESTED_PREFIX)) {
                result.setDateTested(value.substring(DATE_TESTED_PREFIX.length()).trim());
            }
        }
    }

    private int findHeaderRowIndex(Sheet sheet) {
        int lastScanRow = Math.min(sheet.getLastRowNum(), 10);
        for (int i = 0; i <= lastScanRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null && NO_HEADER_LABEL.equalsIgnoreCase(getStringCellValue(row.getCell(0)))) {
                return i;
            }
        }
        return 2;
    }

    private Cell getCell(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? null : row.getCell(colIndex);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private Double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }
            String value = getStringCellValue(cell);
            return value.isBlank() ? null : Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean parsePassedBoolean(Cell cell) {
        String value = getStringCellValue(cell);
        if (PASSED_LABEL.equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if (FAILED_LABEL.equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
