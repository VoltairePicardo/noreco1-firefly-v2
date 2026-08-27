package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.FileFacadeImpl;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonSummaryDetail;
import com.noreco1.fireflyv2.controller.response.reports.PODetail;
import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.PurchaseOrder;
import com.noreco1.fireflyv2.model.enums.DeliveryTerm;
import com.noreco1.fireflyv2.repo.DocumentFileRepo;
import com.noreco1.fireflyv2.repo.PurchaseOrderRepo;
import com.noreco1.fireflyv2.resource.POResource;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final MessageSource messageSource;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final DocumentFileRepo documentFileRepo;
    private final FileFacadeImpl fileFacade;
    private final Environment env;
    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;

    public PurchaseOrderController(@Qualifier("poServiceImpl") PurchaseOrderService purchaseOrderService, MessageSource messageSource, PurchaseOrderRepo purchaseOrderRepo, DocumentFileRepo documentFileRepo, FileFacadeImpl fileFacade, Environment env, @Qualifier("poServiceImpl") PrintableVoucher printableVoucher, DownloadService downloadService) {
        this.purchaseOrderService = purchaseOrderService;
        this.messageSource = messageSource;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.documentFileRepo = documentFileRepo;
        this.fileFacade = fileFacade;
        this.env = env;
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return purchaseOrderService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return purchaseOrderService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return purchaseOrderService.getDocumentsStatuses();
    }

    @GetMapping("/approved-vendors")
    public List<Map> approvedVendors() {
        return purchaseOrderService.findApprovedVendors();
    }

    @GetMapping("/by-supplier/{accountNo}")
    public List<PoListDto> bySupplier(@PathVariable Integer accountNo) {
        return purchaseOrderService.findBySupplierAccountNo(accountNo);
    }

    @GetMapping("/delivery-terms")
    public List<Map<String, String>> deliveryTerms() {
        return Arrays.stream(DeliveryTerm.values())
                .map(dt -> Map.of("value", dt.name(), "label", dt.getDescription()))
                .toList();
    }

//    @PostMapping("/create")
//    public PostResponse create(@RequestBody PurchaseOrder purchaseOrder, HttpServletRequest request) {
//        BindingResult bindingResult = new BeanPropertyBindingResult(purchaseOrder, "purchaseOrder");
//        return purchaseOrderService.processCreate(purchaseOrder, bindingResult, messageSource, request);
//    }
//
//    @PostMapping("/update")
//    public PostResponse update(@RequestBody PurchaseOrder purchaseOrder, HttpServletRequest request) {
//        BindingResult bindingResult = new BeanPropertyBindingResult(purchaseOrder, "purchaseOrder");
//        return purchaseOrderService.processUpdate(purchaseOrder, bindingResult, messageSource, request, Collections.emptyList());
//    }
//
//    @PostMapping("/process")
//    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
//        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
//        return purchaseOrderService.process(dto, bindingResult, messageSource);
//    }

    @PostMapping("/supplier-received")
    public PostResponse supplierReceived(@RequestBody Document document) {
        BindingResult bindingResult = new BeanPropertyBindingResult(document, "document");
        return purchaseOrderService.processSupplierReceived(document, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public PoDto getById(@PathVariable Integer id) {
        return purchaseOrderService.findById(id);
    }

    // ─── File Attachments ────────────────────────────────────────────────────

    @GetMapping("/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Integer id) {
        PurchaseOrder po = purchaseOrderRepo.findById(id).orElse(null);
        if (po == null || po.getTransaction() == null) return Collections.emptyList();

        List<DocumentFile> files = documentFileRepo.findByTransactionId(po.getTransaction().getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentFile df : files) {
            if (df.getFile() == null) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",               df.getId());
            map.put("originalFilename", df.getFile().getOriginalFilename());
            map.put("mimeType",         df.getFile().getMimeType());
            result.add(map);
        }
        return result;
    }

    @PostMapping(value = "/{id}/upload", consumes = {"multipart/form-data"})
    public Map<String, Object> uploadFiles(@PathVariable Integer id, HttpServletRequest request) {
        PurchaseOrder po = purchaseOrderRepo.findById(id).orElse(null);
        if (po == null || po.getTransaction() == null) {
            return Map.of("success", false, "message", "Purchase order not found.");
        }
        if (request instanceof MultipartHttpServletRequest multipart) {
            Map<String, MultipartFile> fileMap = multipart.getFileMap();
            fileFacade.saveDocumentAttachment(fileMap, po.getTransaction().getId());
        }
        return Map.of("success", true);
    }

    @GetMapping("/file/{fileId}")
    public void downloadFile(@PathVariable Integer fileId, HttpServletResponse response) {
        DocumentFile df = documentFileRepo.findById(fileId).orElse(null);
        if (df == null || df.getFile() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            File file = new File(env.getProperty("path.attachments") + df.getFile().getFilename());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String mimeType      = df.getFile().getMimeType()         != null ? df.getFile().getMimeType()         : "application/octet-stream";
            String originalName  = df.getFile().getOriginalFilename() != null ? df.getFile().getOriginalFilename() : df.getFile().getFilename();
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + originalName + "\"");
            response.setContentLength((int) file.length());
            OutputStream os = response.getOutputStream();
            FileUtils.copyFile(file, os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/file/{fileId}")
    public Map<String, Object> deleteFile(@PathVariable Integer fileId) {
        DocumentFile df = documentFileRepo.findById(fileId).orElse(null);
        if (df == null) return Map.of("success", false, "message", "File not found.");
        fileFacade.deleteFile(df.getFile());
        documentFileRepo.delete(df);
        return Map.of("success", true);
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        List<PODetail> summary = this.purchaseOrderService.poDetails(id);

        params.putAll(this.purchaseOrderService.reportMeta()); // merge report parameters with report metadata
        JRDataSource dataSource = new JRBeanCollectionDataSource(summary);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PurchaseOrder.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }


    @GetMapping("/list")
    public List<PoListDto> poList() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/for-cv")
    public List<PoListDto> getPurchaseOrderForCV(HttpServletRequest request) {
        return purchaseOrderService.findForCV();
    }

    @GetMapping("/list/for-editing/{from}/{to}/{status}/{officeId}")
    public List<Map> listByDateAndStatusAndForEditing(@PathVariable String from, @PathVariable String to,
                                                      @PathVariable Integer status, @PathVariable Integer officeId) {
        return purchaseOrderService.findByDateRangeAndStatusIdAndForEditing(from, to, status, officeId);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart(value = "model") @Valid PurchaseOrder paymentRequest,
                               HttpServletRequest request,
                               BindingResult bindingResult) {

        PostResponse response = purchaseOrderService.processCreate(paymentRequest, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            purchaseOrderService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart(value = "model") @Valid PurchaseOrder paymentRequest, HttpServletRequest request,
                               BindingResult bindingResult) {

        PostResponse response = purchaseOrderService.processUpdate(paymentRequest, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            purchaseOrderService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return purchaseOrderService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return purchaseOrderService.defaultSignatories();
    }

    @GetMapping(value = "/{documentStatusId}/paged")
    public Page<PurchaseOrder> byStatusPagedForRR(@PathVariable Integer documentStatusId,
                                                  @RequestParam(value = "q", required = false) String filter,
                                                  Pageable pageable) {

        return purchaseOrderService.findByStatusAndFilter(documentStatusId, filter, pageable);
    }

    @GetMapping("/list/{status}")
    public List<PurchaseOrder> listByStatus(@PathVariable Integer status) {
        return purchaseOrderService.findByStatusId(status);
    }

//    @RequestMapping(value = "/check-editing-allowed")
//    @ResponseBody
//    public Boolean checkEditingAllowed() {
//        Map poEditorRole = settingFacade.getByCode("PO_EDITOR_ROLE");
//        if (poEditorRole != null) {
//            Integer poEditorRoleId = Integer.parseInt(poEditorRole.get("id").toString());
//            List<Object[]> roleObj = userRepo.findRolesByUserIdAndRoleId(authenticationFacade.getLoggedIn().getId(), poEditorRoleId);
//            if (!Checker.collectionIsEmpty(roleObj)) {
//                return true;
//            }
//        }
//        return false;
//    }

    @GetMapping(value = "/for-item-testing/{documentStatusId}/paged")
    public Page<PurchaseOrder> byStatusPagedForForItemTesting(@PathVariable Integer documentStatusId,
                                                       @RequestParam(value = "q", required = false) String filter,
                                                       Pageable pageable) {

        return purchaseOrderService.findPurchaseOrderForItemTestingByStatusAndFilter(documentStatusId, filter, pageable);
    }

//    @RequestMapping(value = "/with-item-testing-for-rr/paged", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<POResource>> byPagedPurchaseOrderWithItemTestingForRR(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                    @RequestParam(value = "q", required = false) String filter) {
//
//        Page<PurchaseOrder> items = purchaseOrderService.findPurchaseOrderWithItemTestingForRRByFilter(filter, pageable);
//
//        return new ResponseEntity<PagedResources<POResource>>(assembler.toResource(items), HttpStatus.OK);
//    }

    @GetMapping("/is-document-for-cash-flow-item-assignment/{transactionId}")
    public boolean isDocumentForCashFlowItemAssignment(@PathVariable Integer transactionId) {
        return purchaseOrderService.isDocumentForCashFlowItemAssignment(transactionId);
    }

    @PostMapping("/saveCashFlowItem")
    public PostResponse saveCashFlowItem(@Valid @RequestBody CashFlowItemDto dto, BindingResult bindingResult) {
        return purchaseOrderService.saveCashFlowItem(dto, bindingResult, messageSource);
    }

//    @RequestMapping(value = "/for-credit-card-purchase-request/{documentStatusId}/paged", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    HttpEntity<PagedResources<POResource>> byStatusPagedForCreditCardPurchaseRequest(Pageable pageable, PagedResourcesAssembler assembler,
//                                                                                     @PathVariable Integer documentStatusId,
//                                                                                     @RequestParam(value = "q", required = false) String filter) {
//
//        Page<PurchaseOrder> items = purchaseOrderService.findAllForCreditCardPurchaseRequestByStatusAndFilter(documentStatusId, filter, pageable);
//
//        return new ResponseEntity<PagedResources<POResource>>(assembler.toResource(items), HttpStatus.OK);
//    }

    @PostMapping("/processSupplierReceived")
    public PostResponse processSupplierReceived(@Valid @RequestBody PurchaseOrder purchaseOrder, BindingResult bindingResult) {
        return purchaseOrderService.processSupplierReceived(purchaseOrder, bindingResult, messageSource);
    }
}
