package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.BankAccount;
import com.noreco1.fireflyv2.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-account")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/all")
    public List<BankAccount> getAll() {
        return bankAccountService.getAllBankAccounts();
    }

    @GetMapping("/list")
    public Page<BankAccount> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bankAccountService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getById(@PathVariable Integer id) {
        BankAccount ba = bankAccountService.findById(id);
        return ba != null ? ResponseEntity.ok(ba) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody BankAccount bankAccount) {
        return bankAccountService.create(bankAccount);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody BankAccount bankAccount) {
        return bankAccountService.update(bankAccount);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return bankAccountService.deleteById(id);
    }
}
