/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.BusinessSegment;
import com.noreco1.fireflyv2.model.Item;

import com.noreco1.fireflyv2.model.enums.AccountClassification;
import com.noreco1.fireflyv2.repo.DocumentRepo;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.controller.response.SegmentAccountDto;
import com.noreco1.fireflyv2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TSI Admin
 */

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    @Autowired
    ItemService itemService;

    @Autowired
    BusinessSegmentService businessSegmentService;

    @Autowired
    AccountService accountService;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    PrepaymentService prepaymentService;

    @GetMapping(value = "/items")
    public Page<Item> items(Pageable pageable) {
        return itemService.findAll(pageable);
    }

    @GetMapping(value = "/business-segments")
    public ResponseEntity<List<BusinessSegment>> businessSegments() {
        List<BusinessSegment> businessSegments = businessSegmentService.findAll();

        return new ResponseEntity<>(businessSegments, HttpStatus.OK);
    }

    @GetMapping(value = "/account-by-segment")
    public ResponseEntity<List<SegmentAccountDto>> getAccountBySegments(@RequestParam(value = "segmentIds") String[] segmentIds) {
        List<SegmentAccountDto> list = accountService.findAllBySegment(segmentIds);

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping(value = "/segment-account-by-id")
    public ResponseEntity<SegmentAccountDto> getSegmentAccount(@RequestParam(value = "segmentIds") Integer segmentId) {
        SegmentAccountDto dto = accountService.findSegmentAccountById(segmentId);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/with-segment-and-allocation-factor")
    public ResponseEntity<List<AccountDto>> getAccountWithSegmentAndAllocationFactor(HttpServletRequest $request) {
        List<AccountDto> accountDtos = new ArrayList<>();
        String classF = $request.getParameter("classf");

        if (classF == null) {
            accountDtos = accountService.findAllWithSegmentAndAllocationFactor(AccountClassification.NEA.toString());
        } else {
            accountDtos = accountService.findAllWithSegmentAndAllocationFactor(classF);
        }

        return new ResponseEntity<>(accountDtos, HttpStatus.OK);
    }

    @GetMapping(value = "/vouchers-for-asset-linking/{assetAccountNo}")
    public Page<Object[]> vouchersForAssetLinking(@PathVariable Integer assetAccountNo,
                                                   @RequestParam(value = "q", required = false) String query,
                                                   Pageable pageable) {

        if (query == null) {
            query = "";
        } else {
            query = query.trim();
        }
        return documentRepo.findAllForAssetLinking(assetAccountNo, query, pageable);
    }

    @GetMapping(value = "/vouchers-for-prepayment-linking/{accountNo}")
    public Page<Object[]> vouchersForPrepaymentLinking(@PathVariable Integer accountNo,
                                                        @RequestParam(value = "q", required = false) String query,
                                                        Pageable pageable) {

        if (query == null) {
            query = "";
        } else {
            query = query.trim();
        }

        return prepaymentService.vouchersForPrepaymentLinking(accountNo, query, pageable);
    }

    @GetMapping(value = "/vouchers-for-maintenance-record/{accountNo}")
    public Page<Object[]> vouchersForMaintenanceRecord(@PathVariable Integer accountNo,
                                                        @RequestParam(value = "q", required = false) String query,
                                                        Pageable pageable) {

        if (query == null) {
            query = "";
        } else {
            query = query.trim();
        }
        return documentRepo.findAllForMaintenanceRecord(accountNo, query, pageable);
    }

}
