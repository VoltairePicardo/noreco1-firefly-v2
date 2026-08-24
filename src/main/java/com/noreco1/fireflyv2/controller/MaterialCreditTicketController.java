package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialCreditTicketDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.MaterialChargeTicket;
import com.noreco1.fireflyv2.model.MaterialCreditTicket;
import com.noreco1.fireflyv2.repo.MaterialCreditTicketRepo;
import com.noreco1.fireflyv2.resource.MaterialCreditTicketDocumentResource;
import com.noreco1.fireflyv2.service.MaterialChargeTicketService;
import com.noreco1.fireflyv2.service.MaterialCreditTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mct")
public class MaterialCreditTicketController {

    @Autowired
    @Qualifier("materialCreditTicketServiceImpl")
    private MaterialCreditTicketService materialCreditTicketService;

    @Autowired
    private MaterialCreditTicketRepo mctRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<MaterialCreditTicket> list() {
        return materialCreditTicketService.findAll();
    }

    @RequestMapping(value = "/list/{from}/{to}/{officeId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Map> listByDateAndStatusPending(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId) {
        return materialCreditTicketService.findByDateRangePending(from, to, officeId);
    }

    @RequestMapping(value = "/list/{from}/{to}/{status}/{officeId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Map> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status, @PathVariable Integer officeId) {
        return materialCreditTicketService.findByDateRangeAndStatusId(from, to, status, officeId);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse create(@Valid @RequestBody MaterialCreditTicket materialCreditTicket, BindingResult bindingResult) {
        PostResponse response = materialCreditTicketService.processCreate(materialCreditTicket, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            materialCreditTicketService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public MaterialCreditTicket get(@PathVariable Integer id, HttpServletRequest request) {
        return materialCreditTicketService.findById(id);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse update(@Valid @RequestBody MaterialCreditTicket materialCreditTicket, BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = materialCreditTicketService.processUpdate(materialCreditTicket, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            materialCreditTicketService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/process", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return materialCreditTicketService.process(postData, bindingResult, messageSource);
    }

    @RequestMapping(value = "/default-signatories")
    @ResponseBody
    public Map defaultSignatories() {
        return materialCreditTicketService.defaultSignatories();
    }

    @RequestMapping(value = "/document-statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<DocumentStatus> getWorkflowActions() {
        return materialCreditTicketService.getDocumentsStatuses();
    }

    @RequestMapping(value = "/summary/{from}/{to}", method = RequestMethod.GET)
    @ResponseBody
    public List<MaterialCreditTicket> listForSummaryReport(@PathVariable String from, @PathVariable String to, HttpServletRequest request) {
        return materialCreditTicketService.getListForSummaryReport(from, to, request);
    }

    @RequestMapping(value = "/items/{transId}", method = RequestMethod.GET)
    @ResponseBody
    public List<ItemTransactionDetailDto> itemsPerMCRT(@PathVariable Integer transId) {
        return materialCreditTicketService.getItems(transId);
    }

    @RequestMapping(value = "/list/inventory-location", method = RequestMethod.GET)
    @ResponseBody
    public List<InventoryLocation> getAllInventoryLocations() {
        return materialCreditTicketService.getAllInventoryLocations();
    }

//    @RequestMapping(value = "/approved-paged", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<MaterialCreditTicketDocumentResource>> approvedListForMaterialCreditTicketPaged(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                                              @RequestParam(value="q", required = false ) String query) {
//
//        Page<MaterialCreditTicketDocumentDto> documents = materialCreditTicketService.findAllApprovedForAccountSettingPaged(query, pageable);
//        return new ResponseEntity<PagedResources<MaterialCreditTicketDocumentResource>>(assembler.toResource(documents), HttpStatus.OK);
//    }
}
