package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ReturnMemorandumReceiptDto;
import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import com.noreco1.fireflyv2.model.ReturnMemorandumReceiptDetail;
import com.noreco1.fireflyv2.repo.MemorandumReceiptDetailRepo;
import com.noreco1.fireflyv2.repo.ReturnMemorandumReceiptDetailRepo;
import com.noreco1.fireflyv2.service.ReturnMemorandumReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/return-memorandum-receipt")
public class ReturnMemorandumReceiptController {

    private final MessageSource messageSource;
    private final ReturnMemorandumReceiptService returnMemorandumReceiptService;
    private final MemorandumReceiptDetailRepo memorandumReceiptDetailRepo;
    private final ReturnMemorandumReceiptDetailRepo returnMemorandumReceiptDetailRepo;

    public ReturnMemorandumReceiptController(MessageSource messageSource, ReturnMemorandumReceiptService returnMemorandumReceiptService, MemorandumReceiptDetailRepo memorandumReceiptDetailRepo, ReturnMemorandumReceiptDetailRepo returnMemorandumReceiptDetailRepo) {
        this.messageSource = messageSource;
        this.returnMemorandumReceiptService = returnMemorandumReceiptService;
        this.memorandumReceiptDetailRepo = memorandumReceiptDetailRepo;
        this.returnMemorandumReceiptDetailRepo = returnMemorandumReceiptDetailRepo;
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public ReturnMemorandumReceiptDto get(@PathVariable Integer id) {
        return returnMemorandumReceiptService.findById(id);
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public Page<Map<String, Object>> list(Pageable pageable,
                                           @RequestParam(value = "s") String startDate,
                                           @RequestParam(value = "e") String endDate,
                                           @RequestParam(value = "q", required = false) String query,
                                           @RequestParam(value = "em", required = false) Integer employeeAccountNo) {

        Page<ReturnMemorandumReceipt> returnMemorandumReceipts = Checker.isStringNullOrEmpty(query)
                ? (Checker.isValidId(employeeAccountNo)
                    ? returnMemorandumReceiptService.findAllByEmployee(employeeAccountNo, startDate, endDate, pageable)
                    : returnMemorandumReceiptService.findAll(startDate, endDate, pageable))
                : (Checker.isValidId(employeeAccountNo)
                    ? returnMemorandumReceiptService.findAllByQueryAndEmployee(query, employeeAccountNo, startDate, endDate, pageable)
                    : returnMemorandumReceiptService.findAllByQuery(query, startDate, endDate, pageable));

        return returnMemorandumReceipts.map(returnMemorandumReceipt -> {
            Map<String, Object> dto = new HashMap<>();

            dto.put("id", returnMemorandumReceipt.getId());
            dto.put("code", returnMemorandumReceipt.getCode());
            dto.put("date", returnMemorandumReceipt.getDate());
            dto.put("office", returnMemorandumReceipt.getOffice());
            dto.put("employee", returnMemorandumReceipt.getMemorandumReceipt().getEmployee());
            dto.put("status", returnMemorandumReceipt.getDocumentStatus().getStatus());

            return dto;
        });
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public PostResponse create(@Valid @RequestBody ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult) {
        return returnMemorandumReceiptService.create(returnMemorandumReceipt, bindingResult, messageSource);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public PostResponse update(@Valid @RequestBody ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult, HttpServletRequest request) {
        return returnMemorandumReceiptService.update(returnMemorandumReceipt, bindingResult, messageSource);
    }

    @PostMapping(value = "/process")
    @ResponseBody
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return this.returnMemorandumReceiptService.process(postData, bindingResult, messageSource);
    }

    @GetMapping(value = "/list-for-reassignment")
    @ResponseBody
    public Page<Map<String, Object>> getReturnMemorandumReceiptForReassignMemorandumReceipt(@RequestParam(value = "q", required = false) String query, Pageable pageable) {
        String trimmedQuery = query == null ? "" : query.trim();

        Page<ReturnMemorandumReceipt> docs = returnMemorandumReceiptService.findAllForReassignment(trimmedQuery, pageable);

        return docs.map(returnMemorandumReceipt -> {
            Map<String, Object> dto = new HashMap<>();

            dto.put("id", returnMemorandumReceipt.getId());
            dto.put("code", returnMemorandumReceipt.getCode());
            dto.put("date", returnMemorandumReceipt.getDate());
            dto.put("employee", returnMemorandumReceipt.getMemorandumReceipt().getEmployee() != null ? returnMemorandumReceipt.getMemorandumReceipt().getEmployee().getName() : null);
            dto.put("office", returnMemorandumReceipt.getOffice() != null ? returnMemorandumReceipt.getOffice().getName() : null);
            dto.put("remarks", returnMemorandumReceipt.getRemarks());

            ArrayList<ReturnMemorandumReceiptDetail> details = returnMemorandumReceiptService.findAllByReturnMR(returnMemorandumReceipt.getId());

            for (ReturnMemorandumReceiptDetail detail : details) {
                detail.setReassignedQuantity(memorandumReceiptDetailRepo.getReassignedQuantity(detail.getStockWithdrawalDetail().getId()));
            }

            dto.put("items", details);

            return dto;
        });
    }

    @GetMapping(value = "/return-memorandum-receipt-detail-for-mst")
    @ResponseBody
    public Page<Map<String, Object>> getMemorandumReceiptDetailForMST(Pageable pageable, @RequestParam(value = "q", required = false) String query) {
        Page<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails = returnMemorandumReceiptDetailRepo.findAllForMaterialSalvageTicket(query, pageable);

        return returnMemorandumReceiptDetails.map(returnMemorandumReceiptDetail -> {
            Map<String, Object> dto = new HashMap<>();

            BigDecimal reassignedQuantity = memorandumReceiptDetailRepo.getReassignedQuantity(returnMemorandumReceiptDetail.getStockWithdrawalDetail().getId());
            BigDecimal balance = returnMemorandumReceiptDetail.getReturnedQuantity().subtract(reassignedQuantity);

            dto.put("returnMemorandumReceiptDetail", returnMemorandumReceiptDetail);
            dto.put("item", returnMemorandumReceiptDetail.getStockWithdrawalDetail().getItem());
            dto.put("quantity", returnMemorandumReceiptDetail.getQuantity());
            dto.put("returnedQuantity", returnMemorandumReceiptDetail.getReturnedQuantity());
            dto.put("reassignedQuantity", reassignedQuantity);
            dto.put("balance", balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balance);
            dto.put("usable", returnMemorandumReceiptDetail.getUsable());

            return dto;
        });
    }
}
