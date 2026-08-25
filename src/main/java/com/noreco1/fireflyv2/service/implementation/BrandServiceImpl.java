package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Brand;
import com.noreco1.fireflyv2.repo.BrandRepo;
import com.noreco1.fireflyv2.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepo brandRepo;

    @Autowired
    private AuthenticationFacade authFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Brand> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return q.isBlank()
                ? brandRepo.findAll(pageable)
                : brandRepo.findByNameContainingIgnoreCase(q, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Brand findById(Integer id) {
        return brandRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Brand brand) {
        brand.setId(null);
        PostResponse res = new PostResponse();
        try {
            brand.setCreatedBy(authFacade.getLoggedIn());
            brand.setCreatedAt(new Date());
            brand.setUpdatedAt(new Date());
            Brand saved = brandRepo.save(brand);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Brand successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Brand brand) {
        PostResponse res = new PostResponse();
        try {
            Brand existing = brandRepo.findById(brand.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Brand not found."); return res; }
            existing.setName(brand.getName());
            existing.setUpdatedAt(new Date());
            existing.setCreatedBy(authFacade.getLoggedIn());
            brandRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Brand successfully updated!");
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
            brandRepo.deleteById(id);
            res.setSuccessMessage("Brand successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
