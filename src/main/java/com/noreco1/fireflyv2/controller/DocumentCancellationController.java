package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.service.DocumentCancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/document-cancellation")
public class DocumentCancellationController {

    @Autowired
    DocumentCancellationService service;

    @Autowired
    DocumentDtoer documentDtoer;

    private final String PARTIALS_PATH = "cancel-document/partials/";

    @GetMapping("/{documentType}/{from}/{to}")
    public List<Map> search(@PathVariable String documentType,
                            @PathVariable String from,
                            @PathVariable String to,
                            @RequestParam(required = false) String cancelled) {
        DocumentType type = DocumentType.valueOf(documentType);
        return documentDtoer.getVouchersForCancellation(from, to, type, cancelled);
    }

    @GetMapping("/document-types")
    public List<Map<String, Object>> documentTypes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DocumentType dt : DocumentType.values()) {
            if (dt.isCancellable()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",          dt.getId());
                m.put("description", dt.getDescription());
                m.put("code",        dt.getCode());
                m.put("module",      dt.getModule());
                m.put("name",        dt.name());
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> String.valueOf(m.get("module"))));
        return list;
    }

    @PostMapping("/cancel/{documentType}/{transId}")
    public PostResponse cancel(@PathVariable String documentType,
                               @PathVariable Integer transId,
                               @RequestParam(required = false, defaultValue = "") String remarks) {
        DocumentType type = DocumentType.valueOf(documentType);
        return service.cancel(transId, type, remarks);
    }

    @PostMapping("/restore/{documentType}/{transId}")
    public PostResponse restore(@PathVariable String documentType,
                                @PathVariable Integer transId) {
        DocumentType type = DocumentType.valueOf(documentType);
        return service.restore(transId, type);
    }

    @GetMapping("/details/{transId}")
    public Map getCancellationDetails(@PathVariable Integer transId) {
        return service.getCancellationDetails(transId);
    }
}
