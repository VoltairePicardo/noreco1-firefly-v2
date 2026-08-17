package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.MaterialChargeTicket;
import com.noreco1.fireflyv2.model.MaterialCreditTicket;
import com.noreco1.fireflyv2.repo.MaterialCreditTicketRepo;
import com.noreco1.fireflyv2.service.MaterialChargeTicketService;
import com.noreco1.fireflyv2.service.MaterialCreditTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mct")
public class MaterialCreditTicketController {

    @Autowired
    @Qualifier("materialCreditTicketServiceImpl")
    private MaterialCreditTicketService mctService;

    @Autowired
    private MaterialCreditTicketRepo mctRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return mctService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return mctService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return mctService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return mctService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public MaterialCreditTicket getById(@PathVariable Integer id) {
        return mctService.findById(id);
    }

    @GetMapping("/details/{transId}")
    public List<?> getDetails(@PathVariable Integer transId) {
        return mctService.getItems(transId);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            MaterialCreditTicket mct = new MaterialCreditTicket();
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                mct.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                mct.setYear(year);
            }
            mct.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            Date now = new Date();
            mct.setCreatedAt(now);
            mct.setUpdatedAt(now);
            mct.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            mct.setDocumentStatus(ds);
            mct.setTransaction(generatorFacade.transaction());
            BindingResult br = new BeanPropertyBindingResult(mct, "mct");
            return mctService.processCreate(mct, br, messageSource);
        } catch (Exception e) {
            response.setFailureMessage("Failed to save: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) { response.setFailureMessage("ID is required."); return response; }
        Integer id = ((Number) idObj).intValue();
        MaterialCreditTicket mct = mctRepo.findById(id).orElse(null);
        if (mct == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                mct.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            mct.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            mct.setUpdatedAt(new Date());
            BindingResult br = new BeanPropertyBindingResult(mct, "mct");
            return mctService.processCreate(mct, br, messageSource);
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return mctService.process(dto, br, messageSource);
    }

    @Autowired
    MaterialChargeTicketService materialChargeTicketService;

    @GetMapping(value = "/list-paged/{from}/{to}")
    public Page<MaterialChargeTicket> byDateRangeAndCode(Pageable pageable,
                                                         @PathVariable String from, @PathVariable String to,
                                                         @RequestParam(value = "q", required = false) String query) {

        return materialChargeTicketService.findByDateRangeAndCode(from, to, query, pageable);
    }

}
