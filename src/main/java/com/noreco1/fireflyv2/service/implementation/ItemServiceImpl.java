package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.repo.ItemRepo;
import com.noreco1.fireflyv2.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepo itemRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Item> findAll(Pageable pageable) {
        return itemRepo.findAllByOrderByDescriptionAsc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Item> list(String q, Integer accountId, Integer categoryId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        if (accountId != null) {
            return itemRepo.findByAssetAccountIdOrExpenseAccountIdOrderByDescriptionAsc(accountId, accountId, pageable);
        }
        if (categoryId != null) {
            return q.isBlank()
                    ? itemRepo.findByInventoryCategoryIdOrderByDescriptionAsc(categoryId, pageable)
                    : itemRepo.findByInventoryCategoryIdAndDescriptionContainingIgnoreCaseOrInventoryCategoryIdAndCodeContainingIgnoreCaseOrderByDescriptionAsc(categoryId, q, categoryId, q, pageable);
        }
        return q.isBlank()
                ? itemRepo.findAllByOrderByDescriptionAsc(pageable)
                : itemRepo.findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByDescriptionAsc(q, q, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Item findById(Integer id) {
        return itemRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Item findByDescription(String desc) {
        return itemRepo.findOneByDescription(desc);
    }

    @Override
    @Transactional
    public PostResponse create(Item item) {
        item.setId(null);
        PostResponse res = new PostResponse();
        try {
            Item saved = itemRepo.save(item);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Item successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Item item) {
        PostResponse res = new PostResponse();
        try {
            Item existing = itemRepo.findById(item.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Item not found."); return res; }
            existing.setCode(item.getCode());
            existing.setDescription(item.getDescription());
            existing.setUnit(item.getUnit());
            existing.setReorderPoint(item.getReorderPoint());
            existing.setIdealQty(item.getIdealQty());
            existing.setLocation(item.getLocation());
            existing.setIsActive(item.getIsActive());
            existing.setAssetAccount(item.getAssetAccount());
            existing.setExpenseAccount(item.getExpenseAccount());
            existing.setInventoryCategory(item.getInventoryCategory());
            existing.setHasSerialNumbers(item.getHasSerialNumbers());
            existing.setBarcode(item.getBarcode());
            itemRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Item successfully updated!");
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
            itemRepo.deleteById(id);
            res.setSuccessMessage("Item successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }
}
