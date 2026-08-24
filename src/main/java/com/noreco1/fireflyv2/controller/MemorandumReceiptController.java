package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.MemorandumReceiptDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.MemorandumReceipt;
import com.noreco1.fireflyv2.model.MemorandumReceiptDetail;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.model.StockWithdrawalDetail;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MemorandumReceiptService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memorandum-receipt")
public class MemorandumReceiptController {

    @Autowired
    @Qualifier("memorandumReceiptServiceImpl")
    private MemorandumReceiptService mrService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<MemorandumReceipt> list() {
        Page<MemorandumReceipt> page = mrService.findAll("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31",
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/list/{from}/{to}")
    public List<MemorandumReceipt> listByDateRange(@PathVariable String from, @PathVariable String to) {
        Page<MemorandumReceipt> page = mrService.findAll(from, to,
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/stock-withdrawals")
    public List<StockWithdrawal> stockWithdrawals(@RequestParam(required = false, defaultValue = "") String q) {
        return mrService.getStockWithdrawals(q);
    }

    @GetMapping("/stock-withdrawal-balance/{detailId}")
    public List<Map> stockWithdrawalBalance(@PathVariable Integer detailId) {
        return mrService.getStockWithdrawalBalance(detailId);
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return mrService.defaultSignatories();
    }

    @GetMapping("/sw-employees/{swId}")
    public ArrayList<SlEntity> swEmployees(@PathVariable Integer swId) {
        return mrService.getStockWithdrawalEmployees(swId);
    }

    @GetMapping("/returned-memos/{accountNo}")
    public ArrayList<MemorandumReceipt> returnedMemos(@PathVariable Integer accountNo) {
        return mrService.getAllEmployeesMemorandumReceipt(accountNo, false);
    }

    @GetMapping("/{id}")
    public MemorandumReceiptDto getById(@PathVariable Integer id) {
        return mrService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            MemorandumReceipt mr = buildMrFromPayload(payload);
            BindingResult br = new BeanPropertyBindingResult(mr, "mr");
            response = mrService.create(mr, br, messageSource);
        } catch (Exception e) {
            response.setFailureMessage("Failed to save: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) { response.setFailureMessage("ID is required."); return response; }
        try {
            MemorandumReceipt mr = buildMrFromPayload(payload);
            mr.setId(((Number) idObj).intValue());
            BindingResult br = new BeanPropertyBindingResult(mr, "mr");
            response = mrService.update(mr, br, messageSource);
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    private MemorandumReceipt buildMrFromPayload(Map<String, Object> payload) {
        MemorandumReceipt mr = new MemorandumReceipt();
        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            mr.setDate(java.sql.Date.valueOf(dateStr));
        }
        Object swObj = payload.get("stockWithdrawal");
        if (swObj instanceof Map swMap && swMap.get("id") != null) {
            StockWithdrawal sw = new StockWithdrawal();
            sw.setId(((Number) swMap.get("id")).intValue());
            mr.setStockWithdrawal(sw);
        }
        Object aoObj = payload.get("approvingOfficer");
        if (aoObj instanceof Map aoMap && aoMap.get("accountNo") != null) {
            User ao = new User();
            ao.setAccountNo(((Number) aoMap.get("accountNo")).intValue());
            mr.setApprovingOfficer(ao);
        }
        Object detailsObj = payload.get("memorandumReceiptDetails");
        if (detailsObj instanceof List<?> detailsList) {
            List<MemorandumReceiptDetail> details = new ArrayList<>();
            for (Object item : detailsList) {
                if (item instanceof Map<?, ?> dm) {
                    MemorandumReceiptDetail d = new MemorandumReceiptDetail();
                    Object swdObj = dm.get("stockWithdrawalDetail");
                    if (swdObj instanceof Map<?, ?> swdMap && swdMap.get("id") != null) {
                        StockWithdrawalDetail swd = new StockWithdrawalDetail();
                        swd.setId(((Number) swdMap.get("id")).intValue());
                        d.setStockWithdrawalDetail(swd);
                    }
                    Object qty = dm.get("quantity");
                    if (qty != null) d.setQuantity(((Number) qty).intValue());
                    Object reassigned = dm.get("reassignedQuantity");
                    d.setReassignedQuantity(reassigned != null ? new BigDecimal(String.valueOf(reassigned)) : BigDecimal.ZERO);
                    details.add(d);
                }
            }
            mr.setMemorandumReceiptDetails(details);
        }
        return mr;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return mrService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("memorandumReceiptServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/MemorandumReceipt.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
