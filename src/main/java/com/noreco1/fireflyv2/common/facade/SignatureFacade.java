package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.enums.DocumentType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 11/28/2016.
 */
public interface SignatureFacade {
    HashMap getDocumentSignature(HashMap<String, Object> parameters, DocumentType docType, Object document);
}
