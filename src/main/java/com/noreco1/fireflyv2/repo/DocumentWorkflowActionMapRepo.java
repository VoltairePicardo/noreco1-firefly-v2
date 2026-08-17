package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DocumentWorkflowActionMap;
import com.noreco1.fireflyv2.model.DocumentWorkflowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentWorkflowActionMapRepo extends JpaRepository<DocumentWorkflowActionMap, Integer> {

    @Transactional(readOnly = true)
    @Query(value = "select * from (select " +
            "DocumentWorkflowActionMap.id as actionMapId, " +
            "WorkflowAction.`id` as actionId, " +
            "WorkflowAction.`action` as 'action', " +
            "DocumentWorkflowActionMap.`sequence` as 'sequence' " +
            "from DocumentWorkflowActionMap " +
            "JOIN WorkflowAction ON FK_workflowActionId = WorkflowAction.id " +
            "WHERE FK_workflowId = :wfId  " +
            "AND sequence < :sequence " +
            "AND FK_beforeActionDocumentStatusId > 0 ORDER BY sequence) as backwardActions " +
            "UNION  " +
            "SELECT * FROM(select " +
            "DocumentWorkflowActionMap.id as actionMapId, " +
            "WorkflowAction.`id` as actionId, " +
            "WorkflowAction.`action` as 'action', " +
            "DocumentWorkflowActionMap.`sequence` as 'sequence' " +
            "from DocumentWorkflowActionMap " +
            "JOIN WorkflowAction ON FK_workflowActionId = WorkflowAction.id " +
            "WHERE FK_workflowId = :wfId  " +
            "AND sequence = (:sequence + 1) " +
            "AND FK_beforeActionDocumentStatusId > 0 " +
            "ORDER BY sequence ASC) as forwardActions", nativeQuery = true)
    public List<Object[]> getActionsByWorkflowIdAndSequence(@Param("wfId") Integer wfId, @Param("sequence") Integer sequence);

    @Transactional(readOnly = true)
    @Query(value = "SELECT * FROM DocumentWorkflowActionMap " +
            "WHERE FK_workflowId = :workflowId " +
            "AND FK_beforeActionDocumentStatusId = 0 ", nativeQuery = true)
    public List<DocumentWorkflowActionMap> findDocumentCreatedAndWorkflowId(@Param("workflowId") Integer workflowId);

    @Query(value = "SELECT " +
            "DISTINCTROW DocumentStatus.* " +
            "FROM (SELECT " +
            "FK_afterActionDocumentStatusId as dId " +
            "FROM DocumentWorkflowActionMap " +
            "where FK_workflowId = :workflowId " +
            "UNION " +
            "SELECT " +
            "FK_beforeActionDocumentStatusId as dId " +
            "FROM DocumentWorkflowActionMap " +
            "where FK_workflowId = :workflowId " +
            "UNION " +
            "SELECT " +
            "id as dId " +
            "FROM DocumentStatus " +
            "where id = :cancelledStatusId) as docIds " +
            "JOIN DocumentStatus ON docIds.dId = DocumentStatus.id " +
            "ORDER BY DocumentStatus.`status`", nativeQuery = true)
    List<Object[]> findDocumentStatusByWorkflowIdAndDocumentStatusCancelled(@Param("workflowId") Integer workflowId, @Param("cancelledStatusId") Integer cancelledStatusId);

    List<DocumentWorkflowActionMap> findByWorkflowId(Integer workflowId);
    List<DocumentWorkflowActionMap> findByWorkflowIdOrderBySequenceDesc(Integer workflowId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT docStatusId, MAX(executedAt) as executedAt FROM(SELECT " +
            "dwfam.FK_afterActionDocumentStatusId as 'docStatusId', " +
            "dwfl.executedAt " +
            "FROM DocumentWorkflowActionMap dwfam " +
            "JOIN WorkflowAction wfa ON dwfam.FK_workflowActionId = wfa.id " +
            "JOIN DocumentWorkflowLog dwfl ON dwfl.FK_documentWorkflowActionMapId = dwfam.id " +
            "WHERE dwfam.FK_workflowId = :wfId " +
            "AND dwfam.sequence < (:sequence + 1) " +
            "AND dwfam.FK_beforeActionDocumentStatusId > 0 " +
            "AND dwfl.FK_documentTransactionId = :transId) " +
            "AS forwardActions GROUP BY docStatusId", nativeQuery = true)
    public List<Object[]> getAfterDocumentStatusesByWorkflowIdAndSequenceAndTransId(@Param("wfId") Integer wfId, @Param("sequence") Integer sequence,
                                                                                  @Param("transId") Integer transId);

    DocumentWorkflowActionMap findByWorkflowIdAndWorkflowActionId(Integer workflowId, Integer workflowActionId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT docStatusId, MAX(executedAt) as executedAt FROM(SELECT " +
            "dwfam.FK_afterActionDocumentStatusId as 'docStatusId', " +
            "dwfl.executedAt " +
            "FROM DocumentWorkflowActionMap dwfam " +
            "JOIN WorkflowAction wfa ON dwfam.FK_workflowActionId = wfa.id " +
            "JOIN DocumentWorkflowLog dwfl ON dwfl.FK_documentWorkflowActionMapId = dwfam.id " +
            "WHERE dwfam.FK_workflowId = :wfId " +
            "AND dwfam.sequence < (:sequence + 1) " +
            "AND dwfl.FK_documentTransactionId = :transId) " +
            "AS forwardActions GROUP BY docStatusId", nativeQuery = true)
    public List<Object[]> getCurrentDocumentStatusesByWorkflowIdAndSequenceAndTransId(@Param("wfId") Integer wfId, @Param("sequence") Integer sequence,
                                                                                    @Param("transId") Integer transId);
}
