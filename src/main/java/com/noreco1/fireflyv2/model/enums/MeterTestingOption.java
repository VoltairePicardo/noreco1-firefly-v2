package com.noreco1.fireflyv2.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeterTestingOption {

    HIGH_CONSUMPTION(1, "High Consumption", 1),
    BURNT(2, "Burnt", 1),
    RE_CALIBRATION(3, "Re-calibration", 1),
    NOT_RUNNING(4, "Not Running", 1),
    BROKEN_COVER(5, "Broken Cover", 1),
    NEW_KWH_METER(6, "New kWh Meter", 1),
    FILLED_WITH_WATER(7, "Filled with water", 1),
    OTHERS(8, "Others", 1),
    NORMAL(9, "Normal", 2),
    DEFECTIVE(10, "Defective (fast/slow/creeping/transient current/overload/corroded parts inside)", 2),
    OTHERS_2(11, "Others", 2),
    FOR_AVERAGING(12, "For Averaging / ADJUSTMENT", 3),
    READY_FOR_INSTALLATION(13, "Ready for Installation", 3),
    FOR_REPLACEMENT(14, "For Replacement", 3),
    READY_FOR_REINSTALLATION(15, "Ready for Re-installation", 3),
    OTHERS_3(16, "Others", 3);

    private Integer id;
    private String description;
    private Integer type;

    MeterTestingOption(Integer id, String description, Integer type) {
        this.id = id;
        this.description = description;
        this.type = type;
    }
}
