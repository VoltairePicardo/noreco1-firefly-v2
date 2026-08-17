package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Integer> {
    @Transactional
    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findAllByOrderByDescriptionAsc(Pageable pageable);

    List<Item> findAllByOrderByDescriptionAsc();

    Item findOneByDescription(String desc);

    Item findByMatId(Integer matId);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByDescriptionAsc(String q1, String q2, Pageable pageable);

    Page<Item> findByAssetAccountIdOrExpenseAccountIdOrderByDescriptionAsc(Integer assetAccountId, Integer expenseAccountId, Pageable pageable);

    List<Item> findAllByInventoryCategoryIdOrderByDescriptionAsc(Integer inventoryCategoryId);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByInventoryCategoryIdOrderByDescriptionAsc(Integer inventoryCategoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByInventoryCategoryIdAndDescriptionContainingIgnoreCaseOrInventoryCategoryIdAndCodeContainingIgnoreCaseOrderByDescriptionAsc(Integer catId1, String q1, Integer catId2, String q2, Pageable pageable);

}
