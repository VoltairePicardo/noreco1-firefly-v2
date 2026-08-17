package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentFileRepo extends JpaRepository<DocumentFile, Integer> {
    DocumentFile findOneByFileId(Integer fileId);
    List<DocumentFile> findByTransactionId(Integer transId);
    List<DocumentFile> findByPrefixAndTransactionId(String prefix, Integer transId);
}
