package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.DocumentProcessingFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.DocumentStatusRepo;
import com.noreco1.fireflyv2.repo.DocumentWorkflowActionMapRepo;
import com.noreco1.fireflyv2.repo.ItemRepo;
import com.noreco1.fireflyv2.repo.SettingRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
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

    @Autowired
    private DocumentStatusRepo documentStatusRepo;

    @Autowired
    private DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    private DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private SettingRepo settingRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationFacade authFacade;

    @Autowired
    private DocumentLoggerFacade documentLoggerFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Item> findAll(Pageable pageable) {
        return itemRepo.findAllByOrderByDescriptionAsc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Item> list(String q, Integer accountId, Integer categoryId, int page, int size, Integer excludeId, boolean noParent) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("description").ascending());
        if (noParent) {
            if (categoryId != null) {
                return q.isBlank()
                        ? itemRepo.findByParentItemIsNullAndCategoryId(categoryId, pageable)
                        : itemRepo.findByParentItemIsNullAndCategoryIdAndSearch(categoryId, q, pageable);
            }
            return q.isBlank()
                    ? itemRepo.findByParentItemIsNullOrderByDescriptionAsc(pageable)
                    : itemRepo.findByParentItemIsNullAndSearch(q, pageable);
        }
        if (accountId != null) {
            return itemRepo.findByAssetAccountIdOrExpenseAccountIdOrderByDescriptionAsc(accountId, accountId, pageable);
        }
        if (categoryId != null) {
            return q.isBlank()
                    ? itemRepo.findByInventoryCategoryIdOrderByDescriptionAsc(categoryId, pageable)
                    : itemRepo.findByInventoryCategoryIdAndDescriptionContainingIgnoreCaseOrInventoryCategoryIdAndCodeContainingIgnoreCaseOrderByDescriptionAsc(categoryId, q, categoryId, q, pageable);
        }
        if (excludeId != null) {
            return q.isBlank()
                    ? itemRepo.findByIdNotOrderByDescriptionAsc(excludeId, pageable)
                    : itemRepo.findByIdNotAndDescriptionContainingIgnoreCaseOrIdNotAndCodeContainingIgnoreCaseOrderByDescriptionAsc(excludeId, q, excludeId, q, pageable);
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
            if (itemRepo.existsByDescriptionIgnoreCase(item.getDescription())) {
                res.setFailureMessage("An item with the same description already exists.");
                return res;
            }
            if (item.getCode() != null && !item.getCode().isBlank() && itemRepo.existsByCodeIgnoreCase(item.getCode())) {
                res.setFailureMessage("An item with the same code already exists.");
                return res;
            }
            User currentUser = authFacade.getLoggedIn();
            item.setDocumentStatus(documentStatusRepo.getReferenceById(1)); // DOCUMENT_CREATED
            item.setCreatedBy(currentUser);
            item.setCreatedAt(new java.util.Date());
            item.setUpdatedAt(new java.util.Date());
            item.setTransaction(generatorFacade.transaction());
            Item saved = itemRepo.save(item);
            Workflow wf = new Workflow();
            wf.setId(1004);
            documentProcessingFacade.processAction(saved.getTransaction(), null, wf, currentUser);
            java.util.Map<String, Object> createLog = new java.util.HashMap<>();
            createLog.put("description", saved.getDescription());
            createLog.put("code", saved.getCode());
            createLog.put("documentStatus", saved.getDocumentStatus() != null ? saved.getDocumentStatus().getStatus() : null);
            documentLoggerFacade.log(saved.getTransaction(), currentUser, null, createLog);
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
            boolean isCreator = existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(authFacade.getLoggedIn() != null ? authFacade.getLoggedIn().getId() : null);
            boolean isIO = isInventoryOfficer();
            int statusId = existing.getDocumentStatus() != null ? existing.getDocumentStatus().getId() : 7;
            if (!isIO && !(isCreator && statusId == 1)) {
                res.setFailureMessage("Not authorized to edit this item.");
                return res;
            }
            if (itemRepo.existsByDescriptionIgnoreCaseAndIdNot(item.getDescription(), item.getId())) {
                res.setFailureMessage("An item with the same description already exists.");
                return res;
            }
            if (item.getCode() != null && !item.getCode().isBlank() && itemRepo.existsByCodeIgnoreCaseAndIdNot(item.getCode(), item.getId())) {
                res.setFailureMessage("An item with the same code already exists.");
                return res;
            }
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
            existing.setParentItem(item.getParentItem());
            existing.setUpdatedAt(new java.util.Date());
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

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto dto) {
        PostResponse res = new PostResponse();
        try {
            User currentUser = authFacade.getLoggedIn();
            Item item = itemRepo.findById(dto.getDocumentId()).orElse(null);
            if (item == null) { res.setFailureMessage("Item not found."); return res; }
            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(dto.getWorkflowActionsDto().getActionMapId()).orElse(null);
            if (actionMap == null) { res.setFailureMessage("Invalid workflow action."); return res; }
            // APPROVE=5, DISAPPROVE=6 require IO role
            int actionId = actionMap.getWorkflowAction() != null ? actionMap.getWorkflowAction().getId() : 0;
            if ((actionId == 5 || actionId == 6) && !isInventoryOfficer()) {
                res.setFailureMessage("Not authorized."); return res;
            }
            if (actionId == 5) { // APPROVE — record approvedBy
                item.setApprovedBy(currentUser);
            }
            item.setDocumentStatus(actionMap.getAfterActionDocumentStatus());
            item.setUpdatedAt(new java.util.Date());
            itemRepo.save(item);
            documentProcessingFacade.processAction(item.getTransaction(), actionMap, null, currentUser);
            java.util.Map<String, Object> processLog = new java.util.HashMap<>();
            processLog.put("documentStatus", item.getDocumentStatus() != null ? item.getDocumentStatus().getStatus() : null);
            processLog.put("remarks", dto.getRemarks());
            documentLoggerFacade.log(item.getTransaction(), currentUser, null, processLog);
            res.setModelId(item.getId());
            res.setSuccessMessage("Document successfully processed.");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public boolean isInventoryOfficer() {
        try {
            User user = authFacade.getLoggedIn();
            if (user == null) return false;
            Setting setting = settingRepo.findOneByCode("ITEM_APPROVING_ROLE");
            if (setting == null || setting.getValue() == null) return false;
            // value stored as JSON: {"id": 24}
            String raw = setting.getValue().trim().replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");
            Integer roleId = Integer.parseInt(raw);
            return !userRepo.findRolesByUserIdAndRoleId(user.getId(), roleId).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
