package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.PettyCashFund;
import com.noreco1.fireflyv2.service.PettyCashFundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/petty-cash-fund")
public class PettyCashFundController {

    @Autowired
    private PettyCashFundService pettyCashFundService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<PettyCashFund> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return pettyCashFundService.findAll(pageable);
        }
        return pettyCashFundService.find(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PettyCashFund> getById(@PathVariable Integer id) {
        PettyCashFund pettyCashFund = pettyCashFundService.findOne(id);
        return pettyCashFund != null ? ResponseEntity.ok(pettyCashFund) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody PettyCashFund pettyCashFund) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pettyCashFund, "pettyCashFund");
        return pettyCashFundService.processCreate(pettyCashFund, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody PettyCashFund pettyCashFund) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pettyCashFund, "pettyCashFund");
        return pettyCashFundService.processUpdate(pettyCashFund, bindingResult, messageSource);
    }
}
