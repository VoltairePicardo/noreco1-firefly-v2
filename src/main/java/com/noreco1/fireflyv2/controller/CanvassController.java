package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.CanvassDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.Canvass;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.CanvassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/canvass")
public class CanvassController {

    @Autowired
    @Qualifier("cnvsServiceImpl")
    private CanvassService canvassService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return canvassService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return canvassService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return canvassService.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Canvass canvass) {
        BindingResult bindingResult = new BeanPropertyBindingResult(canvass, "canvass");
        return canvassService.processCreate(canvass, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Canvass canvass) {
        BindingResult bindingResult = new BeanPropertyBindingResult(canvass, "canvass");
        return canvassService.processUpdate(canvass, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return canvassService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public CanvassDto getById(@PathVariable Integer id) {
        return canvassService.findById(id);
    }
}
