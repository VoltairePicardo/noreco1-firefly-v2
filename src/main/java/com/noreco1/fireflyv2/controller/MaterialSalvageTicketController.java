package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialSalvageTicketDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.MaterialSalvageTicket;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.MaterialSalvageTicketRepo;
import com.noreco1.fireflyv2.resource.MaterialSalvageTicketDocumentResource;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MaterialSalvageTicketService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mst")
public class MaterialSalvageTicketController {

    private final MessageSource messageSource;
    private final MaterialSalvageTicketService materialSalvageTicketService;
    private final DownloadService downloadService;
    private final PrintableVoucher printableVoucher;

    public MaterialSalvageTicketController(MessageSource messageSource, MaterialSalvageTicketService materialSalvageTicketService, DownloadService downloadService, @Qualifier("stockAdjustmentServiceImpl") PrintableVoucher printableVoucher) {
        this.messageSource = messageSource;
        this.materialSalvageTicketService = materialSalvageTicketService;
        this.downloadService = downloadService;
        this.printableVoucher = printableVoucher;
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public List<MaterialSalvageTicket> list() {
        return materialSalvageTicketService.findAll();
    }

    @GetMapping(value = "/list/{from}/{to}/{officeId}")
    @ResponseBody
    public List<Map> listByDateAndStatusPending(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId) {
        return materialSalvageTicketService.findByDateRangePending(from, to, officeId);
    }

    @GetMapping(value = "/list/{from}/{to}/{status}/{officeId}")
    @ResponseBody
    public List<Map> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status, @PathVariable Integer officeId) {
        return materialSalvageTicketService.findByDateRangeAndStatusId(from, to, status, officeId);
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public PostResponse create(@Valid @RequestBody MaterialSalvageTicket materialSalvageTicket, BindingResult bindingResult) {
        PostResponse response = materialSalvageTicketService.processCreate(materialSalvageTicket, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            materialSalvageTicketService.logNewValue(response.getLogId());
        }
        return response;
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public MaterialSalvageTicket get(@PathVariable Integer id, HttpServletRequest request) {
        return materialSalvageTicketService.findById(id);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public PostResponse update(@Valid @RequestBody MaterialSalvageTicket materialSalvageTicket, BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = materialSalvageTicketService.processUpdate(materialSalvageTicket, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            materialSalvageTicketService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/process")
    @ResponseBody
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return materialSalvageTicketService.process(postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/default-signatories")
    @ResponseBody
    public Map defaultSignatories() {
        return materialSalvageTicketService.defaultSignatories();
    }

    @GetMapping(value = "/document-statuses")
    @ResponseBody
    public List<DocumentStatus> getWorkflowActions() {
        return materialSalvageTicketService.getDocumentsStatuses();
    }

    @GetMapping(value = "/summary/{from}/{to}")
    @ResponseBody
    public List<MaterialSalvageTicket> listForSummaryReport(@PathVariable String from, @PathVariable String to, HttpServletRequest request) {
        return materialSalvageTicketService.getListForSummaryReport(from, to, request);
    }

    @GetMapping(value = "/items/{transId}")
    @ResponseBody
    public List<ItemTransactionDetailDto> itemsPerMCRT(@PathVariable Integer transId) {
        return materialSalvageTicketService.getItems(transId);
    }

//    @RequestMapping(value = "/approved-paged", produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<MaterialSalvageTicketDocumentResource>> approvedListForMaterialSalvageTicketPaged(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                                                @RequestParam(value="q", required = false ) String query) {
//
//        Page<MaterialSalvageTicketDocumentDto> documents = materialSalvageTicketService.findAllApprovedForAccountSettingPaged(query, pageable);
//        return new ResponseEntity<PagedResources<MaterialSalvageTicketDocumentResource>>(assembler.toResource(documents), HttpStatus.OK);
//    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.materialSalvageTicketService.getReportMeta());   // items with serial no

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/MaterialSalvageTicket.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
