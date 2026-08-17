package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.form.PrepaymentVoucherLinkForm;
import com.noreco1.fireflyv2.model.Prepayment;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.PrepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pre-payment")
public class PrepaymentController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("ppServiceImpl")
    PrepaymentService prepaymentService;

    @GetMapping("/list")
    public List<?> list() {
        return prepaymentService.findAll();
    }

    @GetMapping("/list/{start}/{end}")
    public List<?> listByDateRange(@PathVariable String start, @PathVariable String end) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date startDate = sdf.parse(start);
            java.util.Date endDate   = sdf.parse(end);
            return prepaymentService.findByStartAndEndDate(startDate, endDate);
        } catch (Exception e) {
            return prepaymentService.findAll();
        }
    }

    @GetMapping("/list/{status}/{start}/{end}")
    public List<?> listByStatusAndDateRange(@PathVariable String status,
                                             @PathVariable String start,
                                             @PathVariable String end) {
        return prepaymentService.findByStatusAndDateRange(status, start, end);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return prepaymentService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Prepayment pp) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pp, "prepayment");
        return prepaymentService.processCreate(pp, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Prepayment pp) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pp, "prepayment");
        return prepaymentService.processUpdate(pp, bindingResult, messageSource);
    }

    /**
     * Returns prepayments due for processing in the given month/year.
     * Also creates a TemporaryBatch for the period (side effect).
     */
    @GetMapping("/process-monthly")
    public List<?> getProcessMonthly(@RequestParam String month, @RequestParam String year) {
        return prepaymentService.findByStartDateCreatedAndMonthYear(month, year);
    }

    /**
     * Calculates totalCost, monthlyCost, and balance based on voucher debit amount.
     */
    @GetMapping("/calculate-cost")
    public Map calculateCost(@RequestParam Integer prepaymentId,
                             @RequestParam Integer voucherTransId,
                             @RequestParam Integer prepaymentAccountId) {
        return prepaymentService.calculateCost(prepaymentId, voucherTransId, prepaymentAccountId);
    }

    /**
     * Links a prepayment to a voucher/transaction and updates cost fields.
     */
    @PostMapping("/link-voucher")
    public PostResponse linkVoucher(@RequestBody PrepaymentVoucherLinkForm form) {
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "prepaymentVoucherLinkForm");
        return prepaymentService.saveLink(form, bindingResult, messageSource);
    }
}
