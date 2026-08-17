package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
public interface SpecialEquipmentAssignmentRepo extends JpaRepository<SpecialEquipmentAssignment, Integer> {

    Page<SpecialEquipmentAssignment> findAllByDateBetweenOrderByDateAsc(Date startDate, Date endDate, Pageable pageable);

    @Query(value = "SELECT " +
            "SpecialEquipmentAssignment.id, " +
            "SpecialEquipmentAssignment.`date`, " +
            "SpecialEquipment.serialNo, " +
            "ItemTestingDetail.remarks, " +
            "Item.description, " +
            "SpecialEquipmentAssignment.createdAt, " +
            "SpecialEquipmentAssignmentDetail.id AS detId, " +
            "(SELECT status FROM SpecialEquipmentAssignmentLog WHERE FK_specialEquipmentAssignmentId = SpecialEquipmentAssignment.id ORDER BY id DESC LIMIT 1) AS stat " +
            "FROM SpecialEquipmentAssignment " +
            "JOIN SpecialEquipmentAssignmentDetail ON  SpecialEquipmentAssignment.id = SpecialEquipmentAssignmentDetail.FK_specialEquipmentAssignmentId " +
            "JOIN SpecialEquipment ON SpecialEquipmentAssignmentDetail.FK_specialEquipmentId = SpecialEquipment.id " +
            "LEFT JOIN ItemTestingDetail ON SpecialEquipment.FK_itemTestingDetailId = ItemTestingDetail.id " +
            "LEFT JOIN Item ON ItemTestingDetail.FK_itemId = Item.id " +
            "WHERE SpecialEquipmentAssignmentDetail.FK_consumerId = :consumerId " +
            "ORDER BY SpecialEquipmentAssignment.`date`", nativeQuery = true)
    List<Object []> findAllByConsumerId(@Param("consumerId") Integer consumerId);
}
