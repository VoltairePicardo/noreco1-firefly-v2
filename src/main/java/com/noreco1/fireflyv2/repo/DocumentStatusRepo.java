package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by TSI on 9/19/2023.
 */
public interface DocumentStatusRepo extends JpaRepository<DocumentStatus, Integer> {

    @Query(value = "SELECT " +
            "DISTINCTROW DocumentStatus.* " +
            "FROM (SELECT " +
            "FK_afterActionDocumentStatusId as dId " +
            "FROM DocumentWorkflowActionMap " +
            "where FK_workflowId IN (4, 2, 5 , 1, 18, 6, 7, 8, 17) " +
            "UNION " +
            "SELECT " +
            "FK_beforeActionDocumentStatusId as dId " +
            "FROM DocumentWorkflowActionMap " +
            "where FK_workflowId IN (4, 2, 5 , 1, 18, 6, 7, 8, 17) " +
            ") as docIds " +
            "JOIN DocumentStatus ON docIds.dId = DocumentStatus.id " +
            "WHERE documentstatus.id != 26 " +
            "ORDER BY DocumentStatus.`status`", nativeQuery = true)
    List<DocumentStatus> findAllVoucherDocumentStatus();

}
