package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.FileFacadeImpl;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.FileUpload;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.repo.DocumentFileRepo;
import com.noreco1.fireflyv2.repo.FileUploadRepo;
import com.noreco1.fireflyv2.repo.ItemRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/file")
@RequiredArgsConstructor
public class FileController {

    private final Environment env;
    private final DocumentFileRepo fileRepo;
    private final FileUploadRepo fileUploadRepo;
    private final FileFacadeImpl fileFacade;
    private final ItemRepo itemRepo;

    @RequestMapping("/attached")
    public ResponseEntity<byte[]> getAttachment(@RequestParam(value = "f") String filename, @RequestParam(value = "o", required = false) String orgFileName) {

        final HttpHeaders headers = new HttpHeaders();
        if (!Checker.isSafeFilename(filename)) {
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
        }

        try {
            InputStream in = new FileInputStream(env.getProperty("path.attachments") + filename);
            MediaType mediaType = fileFacade.getMediaType(filename);
            if (mediaType != null) {
                headers.setContentType(mediaType);

                if (!fileFacade.isPreview()) {
                    headers.setContentDispositionFormData(filename, Checker.isStringNullOrEmpty(orgFileName) ? filename : orgFileName);
                }

                return new ResponseEntity<>(IOUtils.toByteArray(in), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException ex) {
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/attachments/{transId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getAttachments(@PathVariable Integer transId, @RequestParam(value = "prefix", required = false) String prefix) {
        List<DocumentFile> files = Checker.isStringNullOrEmpty(prefix)
                ? fileRepo.findByTransactionId(transId)
                : fileRepo.findByPrefixAndTransactionId(prefix, transId);

        List<Map<String, Object>> result = new ArrayList<>();
        if (!Checker.collectionIsEmpty(files)) {
            for (DocumentFile dFile : files) {
                FileUpload fileUpload = dFile.getFile();
                if (fileUpload == null) continue;

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", fileUpload.getId());
                map.put("originalFilename", fileUpload.getOriginalFilename());
                map.put("mimeType", fileUpload.getMimeType());
                map.put("prefix", dFile.getPrefix());

                result.add(map);
            }
        }
        return result;
    }


    @GetMapping("/{fileId}")
    public void downloadFile(@PathVariable Integer fileId, HttpServletResponse response) {
        FileUpload fileUpload = fileUploadRepo.findById(fileId).orElse(null);
        if (fileUpload == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            File file = new File(env.getProperty("path.attachments") + fileUpload.getFilename());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String mimeType = fileUpload.getMimeType() != null ? fileUpload.getMimeType() : "application/octet-stream";
            String originalName = fileUpload.getOriginalFilename() != null ? fileUpload.getOriginalFilename() : fileUpload.getFilename();

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + originalName + "\"");
            response.setContentLengthLong(file.length());

            OutputStream os = response.getOutputStream();
            FileUtils.copyFile(file, os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{fileId}")
    @ResponseBody
    public Map<String, Object> deleteFile(@PathVariable Integer fileId) {
        DocumentFile df = fileRepo.findOneByFileId(fileId);
        if (df == null) {
            return Map.of("success", false, "message", "File not found.");
        }

        fileRepo.delete(df);
        fileFacade.deleteFile(df.getFile());

        return Map.of("success", true);
    }

    @RequestMapping(value = "/item-image/{itemId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Map> getAttachments(@PathVariable Integer itemId, HttpServletRequest request) {

        List<Map> fileMap = new ArrayList<>();

        try {

            Item item = this.itemRepo.findById(itemId).orElse(null);

            if (item != null && Checker.isValidId(item.getId())){

                Map map = new LinkedHashMap();

                map.put("oFile", item.getFileName());
                map.put("url", GlobalConstant.FILE_DL_PATH + "?f=" + item.getFileName());

                fileMap.add(map);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return fileMap;

    }
}
