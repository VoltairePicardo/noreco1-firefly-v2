package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.FileFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.model.enums.ProjectAttachmentPrefix;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.ItemService;
import com.noreco1.fireflyv2.service.JasperDatasourceService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private JasperDatasourceService datasource;

    @Autowired
    private FileFacade fileFacade;

    @Autowired
    private Environment env;

    @GetMapping("/list")
    public Page<Item> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return itemService.list(q, accountId, categoryId, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable Integer id) {
        Item item = itemService.findById(id);
        if (item == null) return ResponseEntity.notFound().build();
        if (!Checker.isStringNullOrEmpty(item.getFileName())) {
            try {
                item.setBase64Image("data:image/png;base64," +
                    Base64.getEncoder().encodeToString(
                        FileUtils.readFileToByteArray(
                            new File(env.getProperty("path.attachments") + item.getFileName()))));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Item item) {
        return itemService.create(item);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Item item) {
        return itemService.update(item);
    }

    @PostMapping("/upload-image")
    public PostResponse uploadImage(@RequestParam("file") MultipartFile file,
                                    @RequestParam("itemId") Integer itemId) {
        PostResponse response = new PostResponse();
        try {
            Map<String, MultipartFile> fileMap = new HashMap<>();
            fileMap.put(ProjectAttachmentPrefix.ITEM_IMAGE.getPrefix() + "0", file);
            fileFacade.saveItemImage(fileMap, itemId);
            response.setSuccessMessage("Image uploaded successfully.");
        } catch (Exception e) {
            response.setFailureMessage("Failed to upload image.");
        }
        return response;
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return itemService.deleteById(id);
    }

    @RequestMapping(value = "/export")
    public void downloadItemList(@RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders();
        String template = GlobalConstant.JASPER_BASE_PATH + "/item/ItemList.jrxml";
        downloadService.download(type, token, response, params, template, datasource.getItemDataSource());
    }
}
