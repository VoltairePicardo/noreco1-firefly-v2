package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Bank;
import com.noreco1.fireflyv2.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @GetMapping("/all")
    public List<Bank> listAll() {
        return bankService.listAll();
    }

    @GetMapping("/list")
    public Page<Bank> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bankService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bank> getById(@PathVariable Integer id) {
        Bank bank = bankService.findById(id);
        return bank != null ? ResponseEntity.ok(bank) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Bank bank) {
        return bankService.create(bank);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Bank bank) {
        return bankService.update(bank);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return bankService.deleteById(id);
    }
}
