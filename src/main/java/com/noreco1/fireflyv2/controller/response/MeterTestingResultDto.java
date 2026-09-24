package com.noreco1.fireflyv2.controller.response;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MeterTestingResultDto implements Serializable {

    private String reportTitle;
    private String specifications;
    private String temperature;
    private String relativeHumidity;
    private String tester;
    private String dateTested;
    private int totalCount;
    private int passedCount;
    private int failedCount;
    private List<MeterTestingRecordDto> records = new ArrayList<>();

}