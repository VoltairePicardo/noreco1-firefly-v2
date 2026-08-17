package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Map;

public interface DocumentCancellationService {

    PostResponse cancel(Integer documentTransId, DocumentType documentType, String remarks);

    PostResponse restore(Integer documentTransId, DocumentType documentType);

    Map getCancellationDetails(Integer transId);
}
