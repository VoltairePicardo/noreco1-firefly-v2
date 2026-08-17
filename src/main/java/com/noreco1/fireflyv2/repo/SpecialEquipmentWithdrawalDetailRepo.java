package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SpecialEquipmentWithdrawalDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Tri-Nvent on 5/27/2020.
 */
public interface SpecialEquipmentWithdrawalDetailRepo extends JpaRepository<SpecialEquipmentWithdrawalDetail, Integer> {

    @Query(value = "SELECT sewd.id, " +
            "i.id as itemId, " +
            "i.code, " +
            "i.description, " +
            "it.FK_inventoryLocationId, " +
            "i.FK_inventoryCategoryId, " +
            "count(*) as quantity, " +
            "um.code as uniCode, " +
            "um.id as unitId, " +
            "its.id as itemStockId, " +
            "its.unitCost, " +
            "its.quantity as inventoryBalance " +
            "FROM SpecialEquipmentWithdrawalDetail sewd " +
            "LEFT JOIN AssignSpecialEquipment ase ON sewd.FK_assignSpecialEquipmentId = ase.id " +
            "LEFT JOIN SpecialEquipment se ON ase.FK_specialEquipmentId = se.id " +
            "LEFT JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "LEFT JOIN Item i ON itd.FK_itemId = i.id " +
            "LEFT JOIN ItemTesting it ON itd.FK_itemTestingId = it.id " +
            "LEFT JOIN UnitMeasure um ON i.FK_unitId = um.id " +
            "LEFT JOIN ItemStock its ON i.id = its.FK_itemId " +
            "LEFT JOIN StockWithdrawalSpecialEquipmentDetail swded ON sewd.id = swded.FK_specialEquipmentWithdrawalDetailId " +
            "WHERE i.id is not null " +
            "AND swded.id is null " +
            "AND it.FK_inventoryLocationId = :inventoryLocationId " +
            "AND i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "GROUP BY it.id " +
            "ORDER BY i.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipmentWithdrawalDetail sewd " +
                    "LEFT JOIN AssignSpecialEquipment ase ON sewd.FK_assignSpecialEquipmentId = ase.id " +
                    "LEFT JOIN SpecialEquipment se ON ase.FK_specialEquipmentId = se.id " +
                    "LEFT JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
                    "LEFT JOIN Item i ON itd.FK_itemId = i.id " +
                    "LEFT JOIN ItemTesting it ON itd.FK_itemTestingId = it.id " +
                    "LEFT JOIN UnitMeasure um ON i.FK_unitId = um.id " +
                    "LEFT JOIN ItemStock its ON i.id = its.FK_itemId " +
                    "LEFT JOIN StockWithdrawalSpecialEquipmentDetail swded ON sewd.id = swded.FK_specialEquipmentWithdrawalDetailId " +
                    "WHERE i.id is not null " +
                    "AND swded.id is null " +
                    "AND it.FK_inventoryLocationId = :inventoryLocationId " +
                    "AND i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "GROUP BY it.id",
            nativeQuery = true)
    Page<Object[]> findAllForStockWithdrawal(@Param("inventoryLocationId") Integer inventoryLocationId,
                                             @Param("inventoryCategoryId") Integer inventoryCategoryId,
                                             Pageable pageable);

    @Query(value = "SELECT sewd.id, " +
            "i.id as itemId, " +
            "i.code, " +
            "i.description, " +
            "it.FK_inventoryLocationId, " +
            "i.FK_inventoryCategoryId, " +
            "count(*) as quantity, " +
            "um.code as uniCode, " +
            "um.id as unitId, " +
            "its.id as itemStockId, " +
            "its.unitCost, " +
            "its.quantity as inventoryBalance " +
            "FROM SpecialEquipmentWithdrawalDetail sewd " +
            "LEFT JOIN AssignSpecialEquipment ase ON sewd.FK_assignSpecialEquipmentId = ase.id " +
            "LEFT JOIN SpecialEquipment se ON ase.FK_specialEquipmentId = se.id " +
            "LEFT JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "LEFT JOIN Item i ON itd.FK_itemId = i.id " +
            "LEFT JOIN ItemTesting it ON itd.FK_itemTestingId = it.id " +
            "LEFT JOIN UnitMeasure um ON i.FK_unitId = um.id " +
            "LEFT JOIN ItemStock its ON i.id = its.FK_itemId " +
            "LEFT JOIN StockWithdrawalSpecialEquipmentDetail swded ON sewd.id = swded.FK_specialEquipmentWithdrawalDetailId " +
            "WHERE i.id is not null " +
            "AND swded.id is null " +
            "AND it.FK_inventoryLocationId = :inventoryLocationId " +
            "AND i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND (i.code LIKE :query " +
            "OR i.description LIKE :query) " +
            "GROUP BY it.id " +
            "ORDER BY i.description  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM SpecialEquipmentWithdrawalDetail sewd " +
                    "LEFT JOIN AssignSpecialEquipment ase ON sewd.FK_assignSpecialEquipmentId = ase.id " +
                    "LEFT JOIN SpecialEquipment se ON ase.FK_specialEquipmentId = se.id " +
                    "LEFT JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
                    "LEFT JOIN Item i ON itd.FK_itemId = i.id " +
                    "LEFT JOIN ItemTesting it ON itd.FK_itemTestingId = it.id " +
                    "LEFT JOIN UnitMeasure um ON i.FK_unitId = um.id " +
                    "LEFT JOIN ItemStock its ON i.id = its.FK_itemId " +
                    "LEFT JOIN StockWithdrawalSpecialEquipmentDetail swded ON sewd.id = swded.FK_specialEquipmentWithdrawalDetailId " +
                    "WHERE i.id is not null " +
                    "AND swded.id is null " +
                    "AND it.FK_inventoryLocationId = :inventoryLocationId " +
                    "AND i.FK_inventoryCategoryId = :inventoryCategoryId " +
                    "AND (i.code LIKE :query " +
                    "OR i.description LIKE :query) " +
                    "GROUP BY it.id",
            nativeQuery = true)
    Page<Object[]> findAllForStockWithdrawalByQuery(@Param("query") String query,
                                                    @Param("inventoryLocationId") Integer inventoryLocationId,
                                                    @Param("inventoryCategoryId") Integer inventoryCategoryId,
                                                    Pageable pageable);

    @Query(value = "SELECT sewd.* " +
            "FROM SpecialEquipmentWithdrawalDetail sewd " +
            "LEFT JOIN AssignSpecialEquipment ase ON sewd.FK_assignSpecialEquipmentId = ase.id " +
            "LEFT JOIN SpecialEquipment se ON ase.FK_specialEquipmentId = se.id " +
            "LEFT JOIN ItemTestingDetail itd ON se.FK_itemTestingDetailId = itd.id " +
            "LEFT JOIN Item i ON itd.FK_itemId = i.id " +
            "LEFT JOIN ItemTesting it ON itd.FK_itemTestingId = it.id " +
            "LEFT JOIN UnitMeasure um ON i.FK_unitId = um.id " +
            "LEFT JOIN ItemStock its ON i.id = its.FK_itemId " +
            "WHERE i.id is not null " +
            "AND it.FK_inventoryLocationId = :inventoryLocationId " +
            "AND i.FK_inventoryCategoryId = :inventoryCategoryId " +
            "AND i.id = :itemId ", nativeQuery = true)
    List<SpecialEquipmentWithdrawalDetail> findAllByItemAndInventoryLocationAndInventoryCategory(@Param("itemId") Integer itemId,
                                                     @Param("inventoryLocationId") Integer inventoryLocationId,
                                                     @Param("inventoryCategoryId") Integer inventoryCategoryId);

}
