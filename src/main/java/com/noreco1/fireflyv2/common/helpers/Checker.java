package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.model.Transaction;
import com.noreco1.fireflyv2.model.enums.AssetVoucherLinkType;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class Checker {

    public static boolean collectionIsEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean collectionIsNotEmpty(Collection<?> collection) {
        return !Checker.collectionIsEmpty(collection);
    }

    public static boolean itemSyncRunning(Map itemSyncSetting) {
        try {
            if (itemSyncSetting != null) {
                return !Boolean.parseBoolean(itemSyncSetting.get("value").toString());
            }
        } finally {}
        return false;
    }

    public static boolean validTransaction(Transaction transaction) {
        return transaction != null && transaction.getId() != null && transaction.getId() > 0;
    }

    public static boolean isStringNullAndEmpty(String str) {
        return !(str != null && str.trim().length() > 0);
    }
    public static boolean isStringNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public  static boolean isDocumentAttachmentAllowed( MultipartFile file) {
        if (!file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType.toLowerCase().equals("image/jpg") || contentType.toLowerCase().equals("image/jpeg") ||
                    contentType.toLowerCase().equals("image/png") || contentType.toLowerCase().equals("application/pdf") ||
                    contentType.toLowerCase().equals("application/vnd.ms-excel") || contentType.toLowerCase().equals("application/msword") ||
                    contentType.toLowerCase().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || contentType.toLowerCase().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ) {

                return true;
            }
        }
        return false;
    }

    public static boolean documentSaved(PostResponse response) {
        return response != null && response.isSuccess() && response.getLogId() > 0;
    }

    public static boolean isValidId(Integer id) {
        return id != null && id > 0;
    }

    public static boolean isAmountGreaterThanZero(Object amountObj) {

        try {

            if(amountObj == null) {
                return false;
            } else if(amountObj instanceof BigDecimal) {

                BigDecimal amount = (BigDecimal) amountObj;

                return amount.compareTo(BigDecimal.ZERO) > 0;

            } else {

                BigDecimal amount = new BigDecimal(amountObj.toString());

                return amount.compareTo(BigDecimal.ZERO) > 0;

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean amountIsZero(Object amountObj) {

        try {

            if(amountObj == null) {
                return false;
            } else if(amountObj instanceof BigDecimal) {

                BigDecimal amount = (BigDecimal) amountObj;

                return amount.compareTo(BigDecimal.ZERO) == 0;

            } else {

                BigDecimal amount = new BigDecimal(amountObj.toString());

                return amount.compareTo(BigDecimal.ZERO) == 0;

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isMajorRepair(Integer linkTypeId) {
        return linkTypeId.equals(AssetVoucherLinkType.MAJOR_REPAIR.getId());
    }

    public static boolean isAssetAdjustment(Integer linkTypeId) {
        return linkTypeId != null && linkTypeId.equals(AssetVoucherLinkType.ADJUSTMENT.getId());
    }

    public static boolean isAssetAcquisition(Integer linkTypeId) {
        return linkTypeId.equals(AssetVoucherLinkType.ACQUISITION.getId());
    }

    public static boolean isMinorRepair(Integer linkTypeId) {
        return linkTypeId.equals(AssetVoucherLinkType.MINOR_REPAIR.getId());
    }

    public static boolean isAssetRetirement(Integer linkTypeId) {
        return linkTypeId.equals(AssetVoucherLinkType.RETIREMENT.getId());
    }

    public  static boolean isItemImageAllowed( MultipartFile file) {
        if (!file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType.toLowerCase().equals("image/jpg") ||
                contentType.toLowerCase().equals("image/jpeg") ||
                contentType.toLowerCase().equals("image/png")) {

                return true;

            }
        }
        return false;
    }

}
