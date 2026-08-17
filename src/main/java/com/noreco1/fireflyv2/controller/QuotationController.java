package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Quotation;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.QuotationDto;
import com.noreco1.fireflyv2.controller.response.QuotationListDto;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.QuotationService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotation")
public class QuotationController {

    @Autowired
    @Qualifier("quotationServiceImpl")
    private QuotationService quotationService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private com.noreco1.fireflyv2.service.QuotationDetailService quotationDetailService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @GetMapping("/list/{from}/{to}")
    public List<QuotationListDto> listPending(@PathVariable String from, @PathVariable String to) {
        return quotationService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<QuotationListDto> listByStatus(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable Integer statusId) {
        return quotationService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> getDocumentStatuses() {
        return quotationService.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Quotation quotation) {
        BindingResult bindingResult = new BeanPropertyBindingResult(quotation, "quotation");
        return quotationService.processCreate(quotation, bindingResult, messageSource, httpServletRequest);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Quotation quotation) {
        BindingResult bindingResult = new BeanPropertyBindingResult(quotation, "quotation");
        return quotationService.processUpdate(quotation, bindingResult, messageSource, httpServletRequest, new ArrayList<>());
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return quotationService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public QuotationDto getById(@PathVariable Integer id) {
        return quotationService.findById(id);
    }

    @GetMapping("/{id}/details")
    public List<com.noreco1.fireflyv2.controller.response.QuotationItemDto> getDetails(@PathVariable Integer id) {
        return quotationDetailService.getQuotationDetails(id);
    }

    @Autowired
    @Qualifier("quotationServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value="/export/{quotationId}")
    public void exportToPdf(@PathVariable Integer quotationId,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(quotationId, request);
        List<Map> summary = this.quotationService.datasourceAbstractOfQuotation(quotationId);

        params.putAll(this.quotationService.reportMeta()); // merge report parameters with report metadata
        JRDataSource dataSource = new JRBeanCollectionDataSource(summary);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/Quotation.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
