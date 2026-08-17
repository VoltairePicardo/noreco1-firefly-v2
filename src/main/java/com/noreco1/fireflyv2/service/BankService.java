package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Bank;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankService {
    Page<Bank> list(String q, int page, int size);
    List<Bank> listAll();
    Bank findById(Integer id);
    PostResponse create(Bank bank);
    PostResponse update(Bank bank);
    PostResponse deleteById(Integer id);
}
