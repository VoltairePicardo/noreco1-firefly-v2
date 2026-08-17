package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.DocumentWorkflowActionMapRepo;
import com.noreco1.fireflyv2.repo.DocumentWorkflowLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by TSI Admin on 5/6/2015.
 */

@Component
public class DocumentProcessingFacadeImpl implements DocumentProcessingFacade {

    @Autowired
    DocumentWorkflowLogRepo documentWorkflowLogRepo;

    @Autowired
    DocumentWorkflowActionMapRepo documentWorkflowActionMapRepo;

    @Override
    @Transactional
    public DocumentWorkflowLog processAction(Transaction transaction, DocumentWorkflowActionMap actionMap, Workflow workflow, User executedBy) {
        if (actionMap == null) {
            List<DocumentWorkflowActionMap> workflowActionMaps = documentWorkflowActionMapRepo.findDocumentCreatedAndWorkflowId(workflow.getId());
            if (workflowActionMaps != null && workflowActionMaps.size() > 0) {
                actionMap = workflowActionMaps.get(0);
            }
        }

        DocumentWorkflowLog documentWorkflowLog = new DocumentWorkflowLog();
        documentWorkflowLog.setTransaction(transaction);
        documentWorkflowLog.setDocumentWorkflowActionMap(actionMap);
        documentWorkflowLog.setExecutedAt(new Date());

        return documentWorkflowLogRepo.save(documentWorkflowLog);
    }

    @Override
    public DocumentWorkflowLog processAction(Transaction transaction, DocumentWorkflowActionMap actionMap, Workflow workflow, User executedBy, String tableName) {
        if (actionMap == null) {
            List<DocumentWorkflowActionMap> workflowActionMaps = documentWorkflowActionMapRepo.findDocumentCreatedAndWorkflowId(workflow.getId());
            if (workflowActionMaps != null && workflowActionMaps.size() > 0) {
                actionMap = workflowActionMaps.get(0);
            }
        }

        DocumentWorkflowLog documentWorkflowLog = new DocumentWorkflowLog();
        documentWorkflowLog.setTransaction(transaction);
        documentWorkflowLog.setDocumentWorkflowActionMap(actionMap);
        documentWorkflowLog.setExecutedAt(new Date());

        return documentWorkflowLogRepo.save(documentWorkflowLog);
    }
}
