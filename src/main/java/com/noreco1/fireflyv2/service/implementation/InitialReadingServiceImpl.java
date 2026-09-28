package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.model.Meter;
import com.noreco1.fireflyv2.model.enums.MeterStatus;
import com.noreco1.fireflyv2.mssql_repo.MssqlMeterRepo;
import com.noreco1.fireflyv2.repo.MeterRepo;
import com.noreco1.fireflyv2.service.InitialReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InitialReadingServiceImpl implements InitialReadingService {

    private static final Set<Integer> BLOCKED_STATUSES = Set.of(
            MeterStatus.CONNECTED.getId(),
            MeterStatus.DISCONNECTED.getId(),
            MeterStatus.REMOVED.getId(),
            MeterStatus.DISPOSED.getId()
    );

    private final MeterRepo meterRepo;
    private final MssqlMeterRepo mssqlMeterRepo;

    @Override
    public Meter getMeterBySerialNo(String serialNo) {
        Meter meter = meterRepo.findFirstBySerialNoOrderByIdDesc(serialNo)
                .orElseThrow(() -> new BusinessException(
                        "Meter with serial no. '" + serialNo + "' not found.",
                        HttpStatus.NOT_FOUND));

        Integer statusId = statusId(meter);
        if (statusId != null && BLOCKED_STATUSES.contains(statusId)) {
            throw new BusinessException(
                    "Meter " + serialNo + " is already " + resolveStatusDescription(statusId) + " and cannot accept an initial reading.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return meter;
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_UNCOMMITTED)
    public Page<Meter> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return meterRepo.findAllByOrderByUpdatedAtDesc(pageable);
        }
        return meterRepo.findBySerialNoContainingIgnoreCaseOrderByUpdatedAtDesc(q.trim(), pageable);
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_UNCOMMITTED)
    public Meter getById(Integer id) {
        return meterRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional("chainedTransactionManager")
    public PostResponse saveReading(Meter meter, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {
            Meter existing = meterRepo.findFirstBySerialNoOrderByIdDesc(meter.getSerialNo())
                    .orElse(null);

            if (existing == null) {
                response.setFailureMessage("Meter with serial no. '" + meter.getSerialNo() + "' not found.");
                return response;
            }

            Integer statusId = statusId(existing);
            if (statusId != null && BLOCKED_STATUSES.contains(statusId)) {
                response.setFailureMessage("Meter " + meter.getSerialNo() + " is already " + resolveStatusDescription(statusId) + " and cannot accept an initial reading.");
                return response;
            }

            existing.setPresentReading(meter.getPresentReading());
            existing.setReadingDate(meter.getReadingDate());
            existing.setMeterStatus(receivedStatus());
            existing.setUpdatedAt(new Date());

            Meter saved = meterRepo.save(existing);
            syncToMssql(saved);

            response.setModelId(saved.getId());
            response.setSuccessMessage("Initial reading successfully saved.");

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
    }

    private void syncToMssql(Meter mysqlMeter) {
        com.noreco1.fireflyv2.mssql_model.Meter mssqlMeter =
                mssqlMeterRepo.findById(mysqlMeter.getId())
                        .orElse(new com.noreco1.fireflyv2.mssql_model.Meter());

        if (mssqlMeter.getId() == null) {
            mssqlMeter.setId(mysqlMeter.getId());
            mssqlMeter.setSerialNo(mysqlMeter.getSerialNo());
            mssqlMeter.setMultiplier(mysqlMeter.getMultiplier());
            mssqlMeter.setCreatedAt(mysqlMeter.getCreatedAt());
        }

        mssqlMeter.setPresentReading(mysqlMeter.getPresentReading());
        mssqlMeter.setReadingDate(mysqlMeter.getReadingDate());
        if (mysqlMeter.getMeterStatus() != null) {
            com.noreco1.fireflyv2.mssql_model.MeterStatus mssqlStatus = new com.noreco1.fireflyv2.mssql_model.MeterStatus();
            mssqlStatus.setId(mysqlMeter.getMeterStatus().getId());
            mssqlMeter.setMeterStatus(mssqlStatus);
        }
        mssqlMeter.setUpdatedAt(mysqlMeter.getUpdatedAt());

        mssqlMeterRepo.save(mssqlMeter);
    }

    private Integer statusId(Meter meter) {
        return meter.getMeterStatus() != null ? meter.getMeterStatus().getId() : null;
    }

    private com.noreco1.fireflyv2.model.MeterStatus receivedStatus() {
        com.noreco1.fireflyv2.model.MeterStatus status = new com.noreco1.fireflyv2.model.MeterStatus();
        status.setId(MeterStatus.RECEIVED.getId());
        return status;
    }

    private String resolveStatusDescription(Integer statusId) {
        if (statusId == null) return null;
        for (MeterStatus s : MeterStatus.values()) {
            if (s.getId().equals(statusId)) return s.getDescription();
        }
        return null;
    }
}
