package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.JoAcceptanceDetailDto;
import com.noreco1.fireflyv2.controller.response.JoAcceptanceDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.JoAcceptance;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.JoAcceptanceDetailService;
import com.noreco1.fireflyv2.service.JoAcceptanceService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jo-acceptance")
public class JoAcceptanceController {

    @Autowired
    @Qualifier("joaServiceImpl")
    private JoAcceptanceService joAcceptanceService;

    @Autowired
    private JoAcceptanceDetailService joAcceptanceDetailService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return joAcceptanceService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return joAcceptanceService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return joAcceptanceService.getDocumentsStatuses();
    }

    /** Returns all line items for a given JOA. */
    @GetMapping("/detail/{joaId}")
    public List<JoAcceptanceDetailDto> detail(@PathVariable Integer joaId) {
        return joAcceptanceDetailService.getJoaDetails(joaId);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody JoAcceptance joAcceptance) {
        BindingResult bindingResult = new BeanPropertyBindingResult(joAcceptance, "joAcceptance");
        return joAcceptanceService.processCreate(joAcceptance, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody JoAcceptance joAcceptance) {
        BindingResult bindingResult = new BeanPropertyBindingResult(joAcceptance, "joAcceptance");
        return joAcceptanceService.processUpdate(joAcceptance, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return joAcceptanceService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public JoAcceptanceDto getById(@PathVariable Integer id) {
        return joAcceptanceService.findById(id);
    }

    @Autowired
    @Qualifier("joaServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/JoAcceptance1.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
