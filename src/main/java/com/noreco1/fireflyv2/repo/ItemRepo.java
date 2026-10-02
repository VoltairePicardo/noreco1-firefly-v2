package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Integer> {
    @Transactional
    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount", "parentItem"})
    Page<Item> findAllByOrderByDescriptionAsc(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount", "parentItem"})
    java.util.Optional<Item> findById(Integer id);

    List<Item> findAllByOrderByDescriptionAsc();

    Item findOneByDescription(String desc);

    boolean existsByDescriptionIgnoreCase(String description);
    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Integer id);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Integer id);

    Item findOneByTransactionId(Integer transactionId);

    Item findByMatId(Integer matId);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByDescriptionContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByDescriptionAsc(String q1, String q2, Pageable pageable);

    Page<Item> findByAssetAccountIdOrExpenseAccountIdOrderByDescriptionAsc(Integer assetAccountId, Integer expenseAccountId, Pageable pageable);

    List<Item> findAllByInventoryCategoryIdOrderByDescriptionAsc(Integer inventoryCategoryId);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByInventoryCategoryIdOrderByDescriptionAsc(Integer inventoryCategoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByInventoryCategoryIdAndDescriptionContainingIgnoreCaseOrInventoryCategoryIdAndCodeContainingIgnoreCaseOrderByDescriptionAsc(Integer catId1, String q1, Integer catId2, String q2, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByIdNotOrderByDescriptionAsc(Integer excludeId, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory", "assetAccount", "expenseAccount"})
    Page<Item> findByIdNotAndDescriptionContainingIgnoreCaseOrIdNotAndCodeContainingIgnoreCaseOrderByDescriptionAsc(Integer excludeId1, String q1, Integer excludeId2, String q2, Pageable pageable);

    // parentItem IS NULL — used for PR item selection (child items not selectable)
    @EntityGraph(attributePaths = {"unit", "inventoryCategory"})
    Page<Item> findByParentItemIsNullOrderByDescriptionAsc(Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory"})
    @Query("SELECT i FROM Item i WHERE i.parentItem IS NULL AND (LOWER(i.description) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY i.description ASC")
    Page<Item> findByParentItemIsNullAndSearch(@Param("q") String q, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory"})
    @Query("SELECT i FROM Item i WHERE i.parentItem IS NULL AND i.inventoryCategory.id = :categoryId ORDER BY i.description ASC")
    Page<Item> findByParentItemIsNullAndCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"unit", "inventoryCategory"})
    @Query("SELECT i FROM Item i WHERE i.parentItem IS NULL AND i.inventoryCategory.id = :categoryId AND (LOWER(i.description) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY i.description ASC")
    Page<Item> findByParentItemIsNullAndCategoryIdAndSearch(@Param("categoryId") Integer categoryId, @Param("q") String q, Pageable pageable);

}
