package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;

/**
 * Created by TSI Admin on 5/6/2015.
 */
public interface DocumentProcessingFacade {
    public DocumentWorkflowLog processAction(Transaction transaction, DocumentWorkflowActionMap actionMap, Workflow workflow, User executedBy);
    public DocumentWorkflowLog processAction(Transaction transaction, DocumentWorkflowActionMap actionMap, Workflow workflow, User executedBy, String documentAsTableName);
}
