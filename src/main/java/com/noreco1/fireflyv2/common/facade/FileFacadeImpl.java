package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.ProjectAttachmentPrefix;
import com.noreco1.fireflyv2.repo.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by TSI Admin on 8/11/2015.
 */

@Component
public class FileFacadeImpl implements FileFacade {

    @Autowired
    private FileUploadRepo uploadRepo;

    @Autowired
    private DocumentFileRepo fileRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    Environment env;

    @Autowired
    private ItemRepo itemRepo;

    private boolean isPreview = false;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Override
    @Transactional
    public void saveDocumentAttachment(Map<String, MultipartFile> fileMap, Integer transId) {
        try {
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet())
            {
                String key = entry.getKey();
                if (key.startsWith("file_")) {
                    MultipartFile file = entry.getValue();
                    if (Checker.isDocumentAttachmentAllowed(file)) {

                        String newFilename = transId + "~" + UUID.randomUUID() + StringFormatter.getFilenameExtension(file.getOriginalFilename());

                        FileUpload fileUpload = new FileUpload();
                        fileUpload.setOriginalFilename(file.getOriginalFilename());
                        fileUpload.setFilename(newFilename);
                        fileUpload.setMimeType(file.getContentType());

                        FileUpload sFile = uploadRepo.save(fileUpload);
                        if (sFile != null) {
                            File newFile = new File(env.getProperty("path.attachments") + newFilename);
                            file.transferTo(newFile);
                        }

                        // link document and attached file
                        Transaction trans = new Transaction();
                        trans.setId(transId);

                        DocumentFile documentFile = new DocumentFile();
                        documentFile.setTransaction(trans);
                        documentFile.setFile(sFile);
                        fileRepo.save(documentFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveUserSignature(Map<String, MultipartFile> fileMap, User user) {
        try {

            if (user != null) {
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    String key = entry.getKey();

                    FileUpload sFile = null;

                    if (key.startsWith("signFile_")) {
                        MultipartFile file = entry.getValue();
                        if (Checker.isDocumentAttachmentAllowed(file)) {

                            String newFilename = user.getId() + "~" + UUID.randomUUID() + StringFormatter.getFilenameExtension(file.getOriginalFilename());

                            FileUpload fileUpload = new FileUpload();
                            fileUpload.setOriginalFilename(file.getOriginalFilename());
                            fileUpload.setFilename(newFilename);
                            fileUpload.setMimeType(file.getContentType());

                            sFile = uploadRepo.save(fileUpload);
                            if (sFile != null) {
                                File newFile = new File(env.getProperty("path.attachments") + newFilename);
                                file.transferTo(newFile);
                            }
                        }
                    }

                    if (sFile != null && key.startsWith("signFile_")) {
                        if (user.getSignature() != null)  { // delete former
                            this.deleteFile(user.getSignature());
                        }

                        // replace new signature
                        user.setSignature(sFile);
                        userRepo.save(user);

                        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

                        if(employee != null){
                            employee.setSignature(sFile);
                            employeeRepo.save(employee);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDocumentAttachment(Map<String, MultipartFile> fileMap, Integer transId, String prefix) {
        try {
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet())
            {
                String key = entry.getKey();
                if (key.startsWith(prefix)) {
                    MultipartFile file = entry.getValue();
                    if (Checker.isDocumentAttachmentAllowed(file)) {

                        String newFilename = transId + "~" + UUID.randomUUID() + StringFormatter.getFilenameExtension(file.getOriginalFilename());

                        FileUpload fileUpload = new FileUpload();
                        fileUpload.setOriginalFilename(file.getOriginalFilename());
                        fileUpload.setFilename(newFilename);
                        fileUpload.setMimeType(file.getContentType());

                        FileUpload sFile = uploadRepo.save(fileUpload);
                        if (sFile != null) {
                            File newFile = new File(env.getProperty("path.attachments") + newFilename);
                            file.transferTo(newFile);
                        }

                        this.linkDocAndFile(transId, sFile, prefix);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDocumentAttachment(List<Map> filesToRemove, Integer transId) {
        if (!Checker.collectionIsEmpty(filesToRemove)) {
            for(Map f:filesToRemove) {
                try {
                    Object id = f.get("id");
                    DocumentFile documentFile = fileRepo.findOneByFileId((Integer) id);
                    if (documentFile != null) {
                        if (documentFile.getTransaction().getId().equals(transId)) {
                            fileRepo.delete(documentFile);  // detach file from voucher/document
                            uploadRepo.delete(documentFile.getFile());  // delete file detail from db

                            // delete actual file from disk
                            File file = new File(env.getProperty("path.attachments") + documentFile.getFile().getFilename());
                            FileUtils.deleteQuietly(file);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public MediaType getMediaType(String filename) {
        this.isPreview = false;
        
        String filenameExtension = StringFormatter.getFilenameExtension(filename);
        switch (filenameExtension) {
            case ".pdf":
                this.isPreview = true;
                return MediaType.parseMediaType("application/pdf");
            case ".doc":
                return MediaType.parseMediaType("application/msword");
            case ".docx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case ".xls":
                return MediaType.parseMediaType("application/vnd.ms-excel");
            case ".xlsx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case ".png":
                this.isPreview = true;
                return (MediaType.IMAGE_PNG);
            case ".jpeg":
            case ".jpg":
                this.isPreview = true;
                return (MediaType.IMAGE_JPEG);
        }
        return null;
    }

    @Override
    public boolean isPreview() {
        return this.isPreview;
    }

    @Override
    public void saveItemImage(Map<String, MultipartFile> fileMap, Integer itemId) {
        try {
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet())
            {
                String key = entry.getKey();
                if (key.startsWith(ProjectAttachmentPrefix.ITEM_IMAGE.getPrefix())) {
                    MultipartFile file = entry.getValue();
                    if (Checker.isDocumentAttachmentAllowed(file)) {

                        Item item = this.itemRepo.findById(itemId).orElse(null);

                        if (Checker.isValidId(item.getId())){

                            String filename = item.getId() + "~" + UUID.randomUUID() + ".jpg";

                            File newFile = new File(env.getProperty("path.attachments") + filename);
                            file.transferTo(newFile);

                            item.setFileName(filename);
                            this.itemRepo.save(item);

                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void linkDocAndFile(Integer transId, FileUpload sFile, String prefix) {

        // link document and attached file
        Transaction trans = new Transaction();
        trans.setId(transId);

        DocumentFile documentFile = new DocumentFile();
        documentFile.setTransaction(trans);
        documentFile.setFile(sFile);
        documentFile.setPrefix(prefix);

        fileRepo.save(documentFile);
    }

    public void deleteFile(FileUpload f) {
        try {
            uploadRepo.delete(f);  // delete file detail from db
        }catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // delete actual file from disk
            File file = new File(env.getProperty("path.attachments") + f.getFilename());
            FileUtils.deleteQuietly(file);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
