package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DocumentWorkflowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentWorkflowLogRepo extends JpaRepository<DocumentWorkflowLog, Integer> {
    DocumentWorkflowLog findOneByTransactionId(Integer transId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "FK_workflowId, " +
            "sequence " +
            "from DocumentWorkflowLog " +
            "JOIN DocumentWorkflowActionMap ON DocumentWorkflowActionMap.id = DocumentWorkflowLog.FK_documentWorkflowActionMapId " +
            "where DocumentWorkflowLog.FK_documentTransactionId = :transId " +
            "ORDER BY DocumentWorkflowLog.id DESC LIMIT 1", nativeQuery = true)
    public List<Object[]> findLatestDocumentLogByTransactionId(@Param("transId") Integer transId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "FK_workflowId, " +
            "sequence " +
            "FROM DocumentWorkflowLog  " +
            "JOIN DocumentWorkflowActionMap ON DocumentWorkflowLog.FK_documentWorkflowActionMapId = DocumentWorkflowActionMap.id " +
            "WHERE FK_documentTransactionId = :transId " +
            "AND sequence = (SELECT MAX(sequence) as maxSequence FROM DocumentWorkflowActionMap) " +
            "ORDER BY DocumentWorkflowLog.id DESC LIMIT 1", nativeQuery = true)
    public List<Object[]> findLogTopSequenceTransactionId(@Param("transId") Integer transId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "FK_workflowId, " +
            "sequence " +
            "FROM DocumentWorkflowLog  " +
            "JOIN DocumentWorkflowActionMap ON DocumentWorkflowLog.FK_documentWorkflowActionMapId = DocumentWorkflowActionMap.id " +
            "WHERE FK_documentTransactionId = :transId " +
            "AND sequence = (SELECT MAX(sequence) as maxSequence FROM DocumentWorkflowActionMap WHERE FK_workflowId = :workflowId) " +
            "ORDER BY DocumentWorkflowLog.id DESC LIMIT 1", nativeQuery = true)
    public List<Object[]> findLogTopSequenceTransactionIdAndWorkflowId(@Param("transId") Integer transId, @Param("workflowId") Integer workflowId);

    @Query(value = "SELECT dwfl.executedAt FROM DocumentWorkflowLog dwfl " +
            "INNER JOIN DocumentWorkflowActionMap dwfam ON dwfam.id = dwfl.FK_documentWorkflowActionMapId  " +
            "WHERE dwfam.FK_afterActionDocumentStatusId = :docStatId " +
            "AND dwfam.FK_workflowId = :wfId " +
            "AND dwfl.FK_documentTransactionId = :transId " +
            "ORDER BY dwfl.id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestDocumentLogByTransactionIdAndDocumentStatusIdAndWorkFlowId(@Param("transId") Integer transId,
                                                                                          @Param("docStatId") Integer docStatId,
                                                                                          @Param("wfId") Integer wfId);
}
