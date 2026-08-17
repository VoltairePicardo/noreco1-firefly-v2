package com.noreco1.fireflyv2.controller.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RetireAssetForm {

    private Integer id;
    private String retirementRemarks;
    private List<Map> assetDetails = new ArrayList<>();

    public RetireAssetForm() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRetirementRemarks() {
        return retirementRemarks;
    }

    public void setRetirementRemarks(String retirementRemarks) {
        this.retirementRemarks = retirementRemarks;
    }

    public List<Map> getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(List<Map> assetDetails) {
        this.assetDetails = assetDetails;
    }
}
