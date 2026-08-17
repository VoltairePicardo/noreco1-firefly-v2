package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CancelledDocument;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by yer on 8/4/2016.
 */
public interface CancelledDocumentRepo extends JpaRepository<CancelledDocument, Integer> {

    CancelledDocument findByTransactionId(Integer transId);
}
