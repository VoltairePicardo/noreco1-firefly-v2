package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.common.facade.FileFacade;
import com.noreco1.fireflyv2.model.Transaction;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceUtil {

    public static boolean hasFiles(HttpServletRequest request, List<String> prefixes) {

        boolean ok = false;
        try {

            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = mRequest.getFileMap();

            for (String prefix: prefixes) {

                boolean alright = false;

                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {

                    String key = entry.getKey();

                    if (key.startsWith(prefix)) {
                        alright = true;
                    }
                }

                if(!alright) {
                    return false;
                } else {
                    ok = alright;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok;
    }

    public static void attachFiles(FileFacade fileFacade, HttpServletRequest request, Transaction transaction, String prefix) {

        try {

            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (mRequest.getFileMap() != null) {
                fileFacade.saveDocumentAttachment(mRequest.getFileMap(), transaction.getId(), prefix);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFiles(FileFacade fileFacade, HttpServletRequest request, Transaction transaction, List<Map> fileToRemove) {

        try {

            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (mRequest.getFileMap() != null) {
                fileFacade.removeDocumentAttachment(fileToRemove, transaction.getId());
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static com.noreco1.fireflyv2.model.DocumentStatus getDocumentStatusModel(DocumentStatus statusEnum) {

        com.noreco1.fireflyv2.model.DocumentStatus statusModel = new com.noreco1.fireflyv2.model.DocumentStatus();
        statusModel.setId(statusEnum.getId());
        statusModel.setStatus(StringFormatter.statusToHuman(statusEnum.toString()));

        return statusModel;
    }
    public static Workflow getWorkflowModel(com.noreco1.fireflyv2.model.enums.Workflow workflowEnum) {

        Workflow workflowModel = new Workflow();
        workflowModel.setId(workflowEnum.getId());

        return workflowModel;
    }

    public static BigDecimal getLastGlShare(BigDecimal glShare, BigDecimal glTotal, BigDecimal glAmount, Integer counter, Integer lastIdx) {
        if (glTotal.compareTo(glAmount) > 0) {
            BigDecimal i01 = glTotal.subtract(glAmount);
            glShare = glShare.subtract(i01);
        } else if (counter == lastIdx) {
            if (glTotal.compareTo(glAmount) < 0) {
                BigDecimal i01 = glAmount.subtract(glTotal);
                glShare = glShare.add(i01);
            }
        }

        return glShare;
    }

    public static void stringArrayToIntegerArray(Integer[] intArray, String[] strArray){

        for(int i = 0;i < strArray.length; i++)
        {
            intArray[i] = Integer.parseInt(strArray[i]);
        }

    }

    public static List<Integer> strArrayToList(String[] strArray){

        List<Integer> ids = new ArrayList<>();

        for(int i = 0;i < strArray.length; i++) ids.add(Integer.parseInt(strArray[i]));

        return ids;
    }

    public static void saveItemImage(FileFacade fileFacade, HttpServletRequest request, Integer itemId) {

        try {

            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (mRequest.getFileMap() != null) {
                fileFacade.saveItemImage(mRequest.getFileMap(), itemId);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
