package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Bank;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.BankRepo;
import com.noreco1.fireflyv2.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private AuthenticationFacade authFacade;

    @Override
    public Page<Bank> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return q.isBlank()
                ? bankRepo.findAll(pageable)
                : bankRepo.findByNameContainingIgnoreCase(q, pageable);
    }

    @Override
    public List<Bank> listAll() {
        return bankRepo.findByOrderByNameAsc();
    }

    @Override
    public Bank findById(Integer id) {
        return bankRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Bank bank) {
        bank.setId(null);
        PostResponse res = new PostResponse();
        User user = authFacade.getLoggedIn();
        try {
            bank.setCreatedBy(authFacade.getLoggedIn());
            bank.setCreatedAt(new Date());
            bank.setCreatedBy(user);
            bank.setUpdatedAt(new Date());
            Bank saved = bankRepo.save(bank);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Bank successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Bank bank) {
        PostResponse res = new PostResponse();

        User user = authFacade.getLoggedIn();
        try {
            Bank existing = bankRepo.findById(bank.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Bank not found."); return res; }

            existing.setName(bank.getName());
            existing.setAddress(bank.getAddress());
            existing.setContactNumbers(bank.getContactNumbers());
            existing.setContactPersons(bank.getContactPersons());
            existing.setUpdatedAt(new Date());
            existing.setCreatedBy(user);

            bankRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Bank successfully updated!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public PostResponse deleteById(Integer id) {
        PostResponse res = new PostResponse();
        try {
            bankRepo.deleteById(id);
            res.setSuccessMessage("Bank successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
