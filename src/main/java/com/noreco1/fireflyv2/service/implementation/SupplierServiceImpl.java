package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.Supplier;
import com.noreco1.fireflyv2.repo.SupplierRepo;
import com.noreco1.fireflyv2.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepo supplierRepo;

    @Autowired
    private AuthenticationFacade authFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Override
    public Page<Supplier> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return q.isBlank()
                ? supplierRepo.findAll(pageable)
                : supplierRepo.findByNameContainingIgnoreCaseOrderByName(q, pageable);
    }

    @Override
    public Supplier findById(Integer id) {
        return supplierRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Supplier supplier) {
        PostResponse res = new PostResponse();
        try {
            supplier.setId(null);
            supplier.setCreatedBy(authFacade.getLoggedIn());
            supplier.setCreatedAt(new Date());
            supplier.setUpdatedAt(new Date());
            supplier.setAccountNumber(generatorFacade.entityAccountNumber());
            SLEntityClassification slec = new SLEntityClassification();
            slec.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.SUPPLIER.getId());
            supplier.setSlEntityClassification(slec);
            Supplier saved = supplierRepo.save(supplier);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Supplier successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Supplier supplier) {
        PostResponse res = new PostResponse();
        try {
            Supplier existing = supplierRepo.findById(supplier.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Supplier not found."); return res; }
            existing.setName(supplier.getName());
            existing.setAddress(supplier.getAddress());
            existing.setPhone(supplier.getPhone());
            existing.setFax(supplier.getFax());
            existing.setContactPerson(supplier.getContactPerson());
            existing.setContactPersonPosition(supplier.getContactPersonPosition());
            existing.setEmail(supplier.getEmail());
            existing.setTin(supplier.getTin());
            existing.setVatable(supplier.getVatable());
            existing.setBankAccountNumber(supplier.getBankAccountNumber());
            existing.setStatus(supplier.getStatus());
            existing.setCreditLimit(supplier.getCreditLimit());
            existing.setZip(supplier.getZip());
            existing.setRemarks(supplier.getRemarks());
            existing.setAccredited(supplier.getAccredited());
            existing.setUpdatedAt(new Date());
            existing.setCreatedBy(authFacade.getLoggedIn());
            supplierRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Supplier successfully updated!");
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
            supplierRepo.deleteById(id);
            res.setSuccessMessage("Supplier successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
