package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.BankTransactionType;
import com.noreco1.fireflyv2.repo.BankTransactionTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-transaction-type")
public class BankTransactionTypeController {

    @Autowired
    private BankTransactionTypeRepo bankTransactionTypeRepo;

    @GetMapping("/list")
    public List<BankTransactionType> list() {
        return bankTransactionTypeRepo.findAll();
    }
}
