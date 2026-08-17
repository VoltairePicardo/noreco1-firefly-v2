package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileUploadRepo extends JpaRepository<FileUpload, Integer> {
    FileUpload findOneByFilename(String filename);

    @Query(value = "SELECT " +
            "FileUpload.*, " +
            "DocumentFile.updatedAt, " +
            "DocumentFile.prefix " +
            "FROM DocumentFile " +
            "JOIN FileUpload ON DocumentFile.FK_fileId = FileUpload.id " +
            "WHERE DocumentFile.FK_transactionId = :transId", nativeQuery = true)
    public List<Object[]> findAllByDocumentTransId(@Param("transId") Integer transId);
}