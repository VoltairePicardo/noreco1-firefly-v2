package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ItemStockRepo extends JpaRepository<ItemStock, Integer> {
    ItemStock findByItemIdAndInventoryLocationId(Integer itemId, Integer invLocId);
    ItemStock findFirstByItemIdAndInventoryLocationIdOrderByIdAsc(Integer itemId, Integer invLocId);
    Page<ItemStock> findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(Integer invLocId, BigDecimal qty, Pageable paging);
    List<ItemStock> findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(Integer invLocId, BigDecimal qty);
    Page<ItemStock> findAllByInventoryLocationIdNotAndQuantityGreaterThanOrderByItemCode(Integer invLocId, BigDecimal qty, Pageable paging);

    @Query(value = "SELECT * " +
            "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE ii.FK_inventoryLocationId != :invLocId AND ii.quantity > :qty " +
            "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
            "ORDER BY i.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE ii.FK_inventoryLocationId != :invLocId AND ii.quantity > :qty " +
                    "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
                    "ORDER BY i.code",
            nativeQuery = true)
    Page<ItemStock> findAllByInventoryLocationIdNotAndQuantityGreaterThanAndItemCodeLikeOrItemDescriptionLikeOrderByItemCode(@Param("invLocId") Integer invLocId, @Param("qty") BigDecimal qty, @Param("query") String query, Pageable paging);

    @Query(value = "SELECT * " +
            "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE ii.FK_inventoryLocationId = :invLocId AND ii.quantity > :qty " +
            "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
            "ORDER BY i.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE ii.FK_inventoryLocationId = :invLocId AND ii.quantity > :qty " +
                    "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
                    "ORDER BY i.code",
            nativeQuery = true)
    Page<ItemStock> findAllByInventoryLocationIdAndTotalQuantityGreaterThanAndItemCodeIgnoreCaseLikeOrItemDescriptionIgnoreCaseLikeOrderByItemCode(@Param("invLocId") Integer invLocId, @Param("qty") BigDecimal qty, @Param("query") String query, Pageable paging);

    @Query("SELECT s FROM ItemStock s ORDER BY s.item.code ASC")
    Page<ItemStock> findAllOrderByItemCode(Pageable pageable);

    Page<ItemStock> findAllByQuantityGreaterThanOrderByItemCode(BigDecimal qty, Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE ii.quantity > :qty " +
            "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
            "ORDER BY i.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE ii.quantity > :qty " +
                    "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
                    "ORDER BY i.code",
            nativeQuery = true)
    Page<ItemStock> findAllByItemCodeLikeOrItemDescriptionLikeAndQuantityGreaterThanOrderByItemCode(@Param("query") String query, @Param("qty") BigDecimal qty, Pageable pageable);

    @Query(value = "SELECT vouchers.voucherDate, vouchers.code, vouchers.purpose, " +
            "IF(vouchers.docNo IN (1,2,4,5,7), std.quantity, NULL) AS ins, " +
            "IF(vouchers.docNo IN (3, 6), std.quantity, NULL) AS outs, " +
            "std.itemStockBalance FROM StockTransactionDetail std " +
            "INNER JOIN (SELECT st.id AS stockTransId, doc.code, doc.deliveryDate AS voucherDate, " +
            "doc.invoiceDescription AS purpose, doc.FK_transactionId, 1 AS docNo FROM ReceivingReport doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.deliveryDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.remarks AS purpose, doc.FK_transactionId, 2 AS docNo FROM MaterialCreditTicket doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.description AS purpose, doc.FK_transactionId, 3 AS docNo FROM StockRelease doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.purpose AS purpose, doc.FK_transactionId, 4 AS docNo FROM MaterialSalvageTicket doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.remarks AS purpose, doc.FK_transactionId, " +
            "IF(itd.adjustment < 0, 6, 5) AS docNo " +
            "FROM StockAdjustment doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "INNER JOIN ItemTransactionDetail itd ON itd.FK_transactionId = st.FK_transactionId " +
            "INNER JOIN StockTransactionDetail i ON i.FK_stockTransactionId = st.id " +
            "AND itd.FK_itemStockId = i.FK_itemStockId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "AND i.FK_itemStockId = :itemStockId " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.description AS purpose, doc.FK_transactionId, 7 AS docNo FROM StockReceive doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to) vouchers ON " +
            "vouchers.stockTransId = std.FK_stockTransactionId " +
            "WHERE std.FK_itemStockId = :itemStockId", nativeQuery = true)
    List<Object[]> findForBinCard(@Param("from") String from,
                                  @Param("to") String to,
                                  @Param("itemStockId") Integer itemStockId);

    @Query(value = "SELECT vouchers.voucherDate, vouchers.code, vouchers.purpose, " +
            "IF(vouchers.docNo IN (1,2,4,5,7), std.quantity, NULL) AS ins, " +
            "IF(vouchers.docNo IN (1,2,4,5,7), std.totalCost, NULL) AS insAmt, " +
            "IF(vouchers.docNo IN (3, 6), std.quantity, NULL) AS outs, " +
            "IF(vouchers.docNo IN (3, 6), std.totalCost, NULL) AS outsAmt, " +
            "std.itemStockBalance, std.itemStockAmountBalance FROM StockTransactionDetail std " +
            "INNER JOIN (SELECT st.id AS stockTransId, doc.code, doc.deliveryDate AS voucherDate, " +
            "doc.invoiceDescription AS purpose, doc.FK_transactionId, 1 AS docNo FROM ReceivingReport doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.deliveryDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.remarks AS purpose, doc.FK_transactionId, 2 AS docNo FROM MaterialCreditTicket doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.description AS purpose, doc.FK_transactionId, 3 AS docNo FROM StockRelease doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.purpose AS purpose, doc.FK_transactionId, 4 AS docNo FROM MaterialSalvageTicket doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.remarks AS purpose, doc.FK_transactionId, " +
            "IF(itd.adjustment < 0, 6, 5) AS docNo " +
            "FROM StockAdjustment doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "INNER JOIN ItemTransactionDetail itd ON itd.FK_transactionId = st.FK_transactionId " +
            "INNER JOIN StockTransactionDetail i ON i.FK_stockTransactionId = st.id " +
            "AND itd.FK_itemStockId = i.FK_itemStockId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to " +
            "AND i.FK_itemStockId = :itemStockId " +
            "UNION " +
            "SELECT st.id AS stockTransId, doc.code, doc.voucherDate AS voucherDate, " +
            "doc.description AS purpose, doc.FK_transactionId, 7 AS docNo FROM StockReceive doc " +
            "INNER JOIN StockTransaction st ON st.FK_transactionId = doc.FK_transactionId " +
            "WHERE doc.voucherDate BETWEEN :from AND :to) vouchers ON " +
            "vouchers.stockTransId = std.FK_stockTransactionId " +
            "WHERE std.FK_itemStockId = :itemStockId", nativeQuery = true)
    List<Object[]> findForStockCard(@Param("from") String from,
                                    @Param("to") String to,
                                    @Param("itemStockId") Integer itemStockId);

    Page<ItemStock> findAllByInventoryLocationIdAndItemInventoryCategoryIdAndTotalQuantityGreaterThanOrderByItemCode(Integer invLocId, Integer invCatId, BigDecimal bigDecimal, Pageable pageable);

    @Query(value = "SELECT " +
            "DISTINCT ItemStock.* " +
            "FROM ItemStockDetail " +
            "INNER JOIN ItemStock ON ItemStock.id = ItemStockDetail.FK_itemStockId " +
            "INNER JOIN Item ON Item.id = ItemStock.FK_itemId " +
            "WHERE ItemStock.FK_inventoryLocationId = :invLocId " +
            "AND Item.FK_inventoryCategoryId = :invCatId " +
            "AND ItemStock.totalQuantity > :qty " +
            "AND (upper(Item.code) LIKE :query OR upper(Item.description) LIKE :query) " +
            "AND (ItemStockDetail.FK_departmentId IS NULL OR ItemStockDetail.FK_departmentId = :devId) " +
            "ORDER BY ItemStockDetail.FK_departmentId DESC, ItemStockDetail.id ASC " +
            "\n#pageable\n",
            countQuery = "SELECT " +
                    "COUNT(*) " +
                    "FROM ItemStockDetail " +
                    "INNER JOIN ItemStock ON ItemStock.id = ItemStockDetail.FK_itemStockId " +
                    "INNER JOIN Item ON Item.id = ItemStock.FK_itemId " +
                    "WHERE ItemStock.FK_inventoryLocationId = :invLocId " +
                    "AND Item.FK_inventoryCategoryId = :invCatId " +
                    "AND ItemStock.totalQuantity > :qty " +
                    "AND (upper(Item.code) LIKE :query OR upper(Item.description) LIKE :query) " +
                    "AND (ItemStockDetail.FK_departmentId IS NULL OR ItemStockDetail.FK_departmentId = :devId) ",
            nativeQuery = true)
    Page<ItemStock> findAllItemStockByParams(@Param("invLocId") Integer invLocId, @Param("invCatId") Integer invCatId, @Param("qty") BigDecimal qty, @Param("query") String query, @Param("devId") Integer devId, Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE ii.FK_inventoryLocationId = :invLocId AND ii.totalQuantity > :qty " +
            "AND i.FK_inventoryCategoryId = :invCatId " +
            "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
            "ORDER BY i.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM ItemStock ii INNER JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE ii.FK_inventoryLocationId = :invLocId AND ii.totalQuantity > :qty " +
                    "AND i.FK_inventoryCategoryId = :invCatId " +
                    "AND (upper(i.code) LIKE :query OR upper(i.description) LIKE :query) " +
                    "ORDER BY i.code",
            nativeQuery = true)
    Page<ItemStock> findAllByInventoryLocationIdAndItemInventoryCategoryIdAndTotalQuantityGreaterThanAndItemCodeLikeOrItemDescriptionLikeOrderByItemCode(@Param("invLocId") Integer invLocId, @Param("invCatId") Integer invCatId, @Param("qty") BigDecimal qty, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT COALESCE(ii.id, 0) as id, " +
            "COALESCE(ii.FK_itemId, i.id) AS FK_itemId, " +
            "COALESCE(ii.FK_inventoryLocationId, NULL) AS FK_inventoryLocationId, " +
            "COALESCE(ii.quantity, 0) AS quantity, " +
            "COALESCE(ii.unitCost, 0) AS unitCost, " +
            "COALESCE(ii.createdAt, NULL) AS createdAt, " +
            "COALESCE(ii.updatedAt, NULL) AS updatedAt, i.* " +
            "FROM ItemStock ii RIGHT JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL " +
            "ORDER BY i.code  " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ItemStock ii RIGHT JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL",
            nativeQuery = true)
    Page<ItemStock> findAllByInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(@Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT COALESCE(ii.id, 0) as id, " +
            "COALESCE(ii.FK_inventoryLocationId, NULL) AS FK_inventoryLocationId, " +
            "COALESCE(ii.totalItemCost, 0) AS unitCost, " +
            "i.id as itemId, " +
            "i.code as itemCode, " +
            "i.description as itemDesc, " +
            "u.code as unit, " +
            "iL.description as invLocDesc, " +
            "ii.totalQuantity " +
            "FROM ItemStock ii RIGHT JOIN Item i ON i.id = ii.FK_itemId " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "LEFT JOIN InventoryLocation il ON il.id = ii.FK_inventoryLocationId " +
            "WHERE ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL " +
            "ORDER BY i.code",
            nativeQuery = true)
    List<Object[]> findAllByInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(@Param("invLocId") Integer invLocId);

    @Query(value = "SELECT COALESCE(ii.id, 0) as id, " +
            "COALESCE(ii.FK_itemId, i.id) AS FK_itemId, " +
            "COALESCE(ii.FK_inventoryLocationId, NULL) AS FK_inventoryLocationId, " +
            "COALESCE(ii.quantity, 0) AS quantity, " +
            "COALESCE(ii.unitCost, 0) AS unitCost, " +
            "COALESCE(ii.createdAt, NULL) AS createdAt, " +
            "COALESCE(ii.updatedAt, NULL) AS updatedAt, i.* " +
            "FROM ItemStock ii RIGHT JOIN Item i ON i.id = ii.FK_itemId " +
            "WHERE (ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL) " +
            "AND (UPPER(i.code) LIKE UPPER(:query) OR UPPER(i.description) LIKE UPPER(:query)) " +
            "ORDER BY i.code  " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ItemStock ii right JOIN Item i ON i.id = ii.FK_itemId " +
                    "WHERE (ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL) " +
                    "AND (UPPER(i.code) LIKE UPPER(:query) OR UPPER(i.description) LIKE UPPER(:query))",
            nativeQuery = true)
    Page<ItemStock> findAllByItemCodeLikeOrItemDescriptionLikeAndInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(@Param("query") String query, @Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT COALESCE(ii.id, 0) as id, " +
            "COALESCE(ii.FK_inventoryLocationId, NULL) AS FK_inventoryLocationId, " +
            "COALESCE(ii.totalItemCost, 0) AS unitCost, " +
            "i.id as itemId, " +
            "i.code as itemCode, " +
            "i.description as itemDesc, " +
            "u.code as unit, " +
            "iL.description as invLocDesc, " +
            "ii.totalQuantity " +
            "FROM ItemStock ii RIGHT JOIN Item i ON i.id = ii.FK_itemId " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "LEFT JOIN InventoryLocation il ON il.id = ii.FK_inventoryLocationId " +
            "WHERE (ii.FK_inventoryLocationId = :invLocId OR ii.FK_inventoryLocationId IS NULL) " +
            "AND (UPPER(i.code) LIKE UPPER(:query) OR UPPER(i.description) LIKE UPPER(:query)) " +
            "ORDER BY i.code",
            nativeQuery = true)
    List<Object[]> findAllByItemCodeLikeOrItemDescriptionLikeAndInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(@Param("query") String query, @Param("invLocId") Integer invLocId);

    List<ItemStock> findAllByInventoryLocationIdAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(Integer inventoryLocationId, Integer inventoryCategoryId);

    List<ItemStock> findAllByInventoryLocationIdAndItemInventoryCategoryIdAndTotalQuantityGreaterThanOrderByItemCode(Integer invLocId, Integer invCatId, BigDecimal qty);

    List<ItemStock> findAllByInventoryLocationIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(Integer inventoryLocationId);

    List<ItemStock> findAllByItemIdAndInventoryLocationIdNot(Integer itemId, Integer invLocId);

    @Query(value = "SELECT " +
            "* " +
            "FROM ItemStock s " +
            "WHERE s.FK_itemId = :itemId " +
            "AND s.FK_inventoryLocationId NOT IN(:invLocId, :invLocId2)",
            nativeQuery = true)
    List<ItemStock> findOtherItemStocksNotSEP(@Param("itemId") Integer itemId,
                                              @Param("invLocId") Integer invLocId,
                                              @Param("invLocId2") Integer invLocId2);

    @Query(value = "SELECT * FROM ItemStock " +
            "INNER JOIN Item ON Item.id = ItemStock.FK_itemId " +
            "INNER JOIN InventoryCategory ON InventoryCategory.id = Item.FK_inventoryCategoryId " +
            "WHERE (ItemStock.FK_inventoryLocationId = :inventoryLocationId) " +
            "AND (IF(:inventoryCategoryId = 0 OR :inventoryCategoryId = Item.FK_inventoryCategoryId, 1, 0) = 1) " +
            "AND (ItemStock.quantity <= Item.reorderPoint) " +
            "ORDER BY InventoryCategory.description, Item.description ",
            nativeQuery = true)
    List<ItemStock> findAllByInventoryLocationIdAndItemStockQuantityIsLessThanEqualItemReorderPointAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(@Param("inventoryLocationId")Integer inventoryLocationId, @Param("inventoryCategoryId")Integer inventoryCategoryId);

    @Query(value = "SELECT * FROM ItemStock " +
            "INNER JOIN Item ON Item.id = ItemStock.FK_itemId " +
            "INNER JOIN InventoryCategory ON InventoryCategory.id = Item.FK_inventoryCategoryId " +
            "WHERE (ItemStock.FK_inventoryLocationId = :inventoryLocationId) " +
            "AND (IF(:inventoryCategoryId = 0 OR :inventoryCategoryId = Item.FK_inventoryCategoryId, 1, 0) = 1) " +
            "AND (ItemStock.quantity < Item.idealQty) " +
            "ORDER BY InventoryCategory.description, Item.description ",
            nativeQuery = true)
    List<ItemStock> findAllByInventoryLocationIdAndItemStockQuantityIsLessThanItemIdealQuantityAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(@Param("inventoryLocationId")Integer inventoryLocationId, @Param("inventoryCategoryId")Integer inventoryCategoryId);

    @Query(value = "SELECT  " +
            "SUM(swd.quantity) - SUM(swd.quantityReleased) " +
            "FROM StockWithdrawal sw " +
            "JOIN StockWithdrawalDetail swd ON swd.FK_stockWithdrawalId = sw.id " +
            "WHERE sw.FK_inventoryLocationId = :invLocId " +
            "AND swd.FK_itemId = :itemId " +
            "AND (sw.FK_departmentId IS NULL OR sw.FK_departmentId = :devId) " +
            "GROUP BY swd.FK_itemId " +
            "HAVING SUM(swd.quantity) - SUM(swd.quantityReleased) != 0 OR SUM(swd.quantity) - SUM(swd.quantityReleased) < 0 ", nativeQuery = true)
    BigDecimal ItemStockBalance(@Param("invLocId") Integer invLocId,
                                @Param("itemId") Integer itemId,
                                @Param("devId") Integer devId);

}
