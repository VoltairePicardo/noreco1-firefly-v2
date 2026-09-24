package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.controller.response.MeterTestingRecordDto;
import com.noreco1.fireflyv2.controller.response.MeterTestingResultDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.model.Meter;
import com.noreco1.fireflyv2.model.MeterTesting;
import com.noreco1.fireflyv2.model.MeterTestingDetail;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.mssql_model.MeterModel;
import com.noreco1.fireflyv2.repo.MeterModelRepo;
import com.noreco1.fireflyv2.repo.MeterRepo;
import com.noreco1.fireflyv2.repo.MeterTestingDetailRepo;
import com.noreco1.fireflyv2.repo.MeterTestingRepo;
import com.noreco1.fireflyv2.service.MeterTestingService;
import com.noreco1.fireflyv2.validator.MeterTestingValidator;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final AuthenticationFacade authenticationFacade;
    private final MeterRepo meterRepo;
    private final com.noreco1.fireflyv2.mssql_repo.MssqlMeterRepo mssqlMeterRepo;
    private final MeterModelRepo meterModelRepo;
    private final com.noreco1.fireflyv2.mssql_repo.MssqlUserRepo mssqlUserRepo;

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

            meterTesting.setId(null);
            meterTesting.setCreatedBy(authenticationFacade.getLoggedIn());
            meterTesting.setCreatedAt(new Date());
            meterTesting.setUpdatedAt(new Date());
            MeterTesting saved = meterTestingRepo.save(meterTesting);

            meterModelRepo.findById(meterTesting.getMeterModel().getId())
                    .orElseThrow(() -> new BusinessException("Meter Model with id: "+meterTesting.getMeterModel().getId()+" not found."));

            List<MeterTestingDetail> detailList = new ArrayList<>();
            List<Meter> meterList = new ArrayList<>();

            for (MeterTestingDetail detail : details) {
                detail.setId(null);
                detail.setMeterTesting(saved);

                detailList.add(detail);

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
            response.setSuccessMessage("Meter Testing successfully saved.");

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
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
        }
        return meterTesting;
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
