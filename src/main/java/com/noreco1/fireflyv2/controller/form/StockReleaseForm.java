package com.noreco1.fireflyv2.controller.form;

import com.noreco1.fireflyv2.model.StockRelease;

import java.util.ArrayList;
import java.util.List;

public class StockReleaseForm {

    private List<StockRelease> documents = new ArrayList<>();

    public StockReleaseForm() {
    }

    public StockReleaseForm(List<StockRelease> documents) {
        this.documents = documents;
    }

    public List<StockRelease> getDocuments() {
        return documents;
    }

    public void setDocuments(List<StockRelease> documents) {
        this.documents = documents;
    }
}
