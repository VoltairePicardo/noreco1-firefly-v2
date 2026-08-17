package com.noreco1.fireflyv2.controller.form;

import com.noreco1.fireflyv2.model.Asset;
import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssetVoucherLinkForm {

    private Asset asset;
    private Transaction voucherTransaction;
    private DocumentType documentType;
    private AssetVoucherLinkType linkType;
    private List<Map> assetDetails = new ArrayList<>();
    private String adjustmentType;

    public AssetVoucherLinkForm() {
    }

    public AssetVoucherLinkForm(Asset asset, Transaction voucherTransaction, AssetVoucherLinkType linkType,
                                List<Map> assetDetails, DocumentType documentType, String adjustmentType) {
        this.asset = asset;
        this.voucherTransaction = voucherTransaction;
        this.linkType = linkType;
        this.assetDetails = assetDetails;
        this.documentType = documentType;
        this.adjustmentType = adjustmentType;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Transaction getVoucherTransaction() {
        return voucherTransaction;
    }

    public void setVoucherTransaction(Transaction voucherTransaction) {
        this.voucherTransaction = voucherTransaction;
    }

    public AssetVoucherLinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(AssetVoucherLinkType linkType) {
        this.linkType = linkType;
    }

    public List<Map> getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(List<Map> assetDetails) {
        this.assetDetails = assetDetails;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }
}
