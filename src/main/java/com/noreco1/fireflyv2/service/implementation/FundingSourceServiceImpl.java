package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.FundingSource;
import com.noreco1.fireflyv2.repo.FundingSourceRepo;
import com.noreco1.fireflyv2.service.FundingSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FundingSourceServiceImpl implements FundingSourceService {

    @Autowired
    private FundingSourceRepo fundingSourceRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<FundingSource> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        return q.isBlank()
                ? fundingSourceRepo.findAll(pageable)
                : fundingSourceRepo.findByDescriptionContainingIgnoreCase(q, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<FundingSource> listAll() {
        return fundingSourceRepo.findByOrderByDescriptionAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public FundingSource findById(Integer id) {
        return fundingSourceRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public FundingSource findByDescription(String description) {
        return fundingSourceRepo.findByDescriptionContainingIgnoreCase(description.trim());
    }

    @Override
    @Transactional
    public PostResponse create(FundingSource fundingSource) {
        fundingSource.setId(null);
        PostResponse res = new PostResponse();
        try {
            FundingSource saved = fundingSourceRepo.save(fundingSource);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Funding source successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(FundingSource fundingSource) {
        PostResponse res = new PostResponse();
        try {
            FundingSource existing = fundingSourceRepo.findById(fundingSource.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Funding source not found."); return res; }
            existing.setDescription(fundingSource.getDescription());
            fundingSourceRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Funding source successfully updated!");
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
            fundingSourceRepo.deleteById(id);
            res.setSuccessMessage("Funding source successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
