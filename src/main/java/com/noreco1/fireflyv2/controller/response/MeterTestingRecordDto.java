package com.noreco1.fireflyv2.controller.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeterTestingRecordDto implements Serializable {

    private Integer no;
    private String actualDateOfTesting;
    private String meterSerialNo;
    private String sealNo;
    private Double error;
    private Boolean sta;
    private Boolean crp;
    private Boolean voltageTest;
    private Boolean result;

}