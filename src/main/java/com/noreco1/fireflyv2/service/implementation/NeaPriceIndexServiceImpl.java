package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.NeaPriceIndex;
import com.noreco1.fireflyv2.model.NeaPriceIndexDetail;
import com.noreco1.fireflyv2.repo.NeaPriceIndexDetailRepo;
import com.noreco1.fireflyv2.repo.NeaPriceIndexRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.NeaPriceIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class NeaPriceIndexServiceImpl implements NeaPriceIndexService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private NeaPriceIndexRepo neaPriceIndexRepo;

    @Autowired
    private NeaPriceIndexDetailRepo neaPriceIndexDetailRepo;

    @Override
    public Page<NeaPriceIndex> list(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("effectivityDate").descending());
        return neaPriceIndexRepo.findAllByOrderByEffectivityDateDesc(pageable);
    }

    @Override
    public NeaPriceIndex findById(Integer id) {
        NeaPriceIndex neaPriceIndex = neaPriceIndexRepo.findById(id).orElse(null);
        if (neaPriceIndex != null && Checker.isValidId(neaPriceIndex.getId())) {
            List<NeaPriceIndexDetail> details = neaPriceIndexDetailRepo.findAllByNeaPriceIndexIdOrderByItemDescriptionAsc(neaPriceIndex.getId());
            if (Checker.collectionIsNotEmpty(details)) {
                neaPriceIndex.setNeaPriceIndexDetails(details);
            }
        }
        return neaPriceIndex;
    }

    @Override
    public NeaPriceIndex findByIdWithPrice(Integer id) {
        NeaPriceIndex neaPriceIndex = neaPriceIndexRepo.findById(id).orElse(null);
        if (neaPriceIndex != null && Checker.isValidId(neaPriceIndex.getId())) {
            List<NeaPriceIndexDetail> details = neaPriceIndexDetailRepo.findAllByNeaPriceIndexIdAndPriceGreaterThanOrderByItemDescriptionAsc(neaPriceIndex.getId(), BigDecimal.ZERO);
            if (Checker.collectionIsNotEmpty(details)) {
                neaPriceIndex.setNeaPriceIndexDetails(details);
            }
        }
        return neaPriceIndex;
    }

    @Override
    public NeaPriceIndexDetail getItemNeaPriceIndex(Integer itemId) {
        return neaPriceIndexDetailRepo.findFirstByItemId(itemId);
    }

    @Override
    @Transactional
    public PostResponse create(NeaPriceIndex neaPriceIndex) {
        neaPriceIndex.setId(null);
        PostResponse res = new PostResponse();
        try {
            neaPriceIndex.setEncodedBy(authenticationFacade.getLoggedIn());
            neaPriceIndex.setCreatedAt(new Date());
            neaPriceIndex.setUpdatedAt(new Date());
            NeaPriceIndex saved = neaPriceIndexRepo.save(neaPriceIndex);
            saveDetails(saved, neaPriceIndex.getNeaPriceIndexDetails());
            res.setModelId(saved.getId());
            res.setSuccessMessage("NEA price index successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(NeaPriceIndex neaPriceIndex) {
        PostResponse res = new PostResponse();
        try {
            NeaPriceIndex existing = neaPriceIndexRepo.findById(neaPriceIndex.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("NEA price index not found."); return res; }
            existing.setEffectivityDate(neaPriceIndex.getEffectivityDate());
            existing.setDescription(neaPriceIndex.getDescription());
            existing.setEncodedBy(authenticationFacade.getLoggedIn());
            existing.setUpdatedAt(new Date());
            neaPriceIndexRepo.save(existing);
            updateDetails(neaPriceIndex.getId(), neaPriceIndex.getNeaPriceIndexDetails());
            res.setModelId(existing.getId());
            res.setSuccessMessage("NEA price index successfully updated!");
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
            neaPriceIndexRepo.deleteById(id);
            res.setSuccessMessage("NEA price index successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    private void saveDetails(NeaPriceIndex header, List<NeaPriceIndexDetail> details) {
        if (details == null) return;
        for (NeaPriceIndexDetail d : details) {
            NeaPriceIndexDetail detail = new NeaPriceIndexDetail();
            detail.setNeaPriceIndex(header);
            detail.setItem(d.getItem());
            detail.setPrice(d.getPrice());
            neaPriceIndexDetailRepo.save(detail);
        }
    }

    private void updateDetails(Integer headerId, List<NeaPriceIndexDetail> details) {
        if (details == null) return;
        for (NeaPriceIndexDetail d : details) {
            NeaPriceIndexDetail toEdit = neaPriceIndexDetailRepo.findByNeaPriceIndexIdAndItemId(headerId, d.getItem().getId());
            if (toEdit != null && Checker.isValidId(toEdit.getId())) {
                toEdit.setPrice(d.getPrice());
                neaPriceIndexDetailRepo.save(toEdit);
            }
        }
    }
}
