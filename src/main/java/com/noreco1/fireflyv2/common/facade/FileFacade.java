package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileFacade {

    void saveDocumentAttachment(Map<String, MultipartFile> fileMap, Integer transId);
    void saveUserSignature(Map<String, MultipartFile> fileMap, User user);
    void saveDocumentAttachment(Map<String, MultipartFile> fileMap, Integer transId, String prefix);
    void removeDocumentAttachment(List<Map> filesToRemove, Integer transId);
    MediaType getMediaType(String filename);
    boolean isPreview();

    void saveItemImage(Map<String, MultipartFile> fileMap, Integer itemId);

}
