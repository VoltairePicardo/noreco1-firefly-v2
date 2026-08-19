package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BankDeposit;
import com.noreco1.fireflyv2.service.BankDepositService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/bank-deposit")
public class BankDepositController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("bankDepositServiceImpl")
    BankDepositService service;

    @Autowired
    @Qualifier("bankDepositServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<?> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody BankDeposit entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "bankDeposit");
        return service.processCreate(entity, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody BankDeposit entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "bankDeposit");
        return service.processUpdate(entity, bindingResult, messageSource);
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/BankDeposit2.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
