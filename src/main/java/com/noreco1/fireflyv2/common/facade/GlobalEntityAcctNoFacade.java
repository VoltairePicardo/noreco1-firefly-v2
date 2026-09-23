package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.mysql_model.GlobalEntityAccountNo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface GlobalEntityAcctNoFacade {

    /**
     * Generate the next valid global account number using the mod-11 checksum formula,
     * persist it, and return the saved record.
     * Pass entityId = 0 if the entity does not exist yet — call link() once it is saved.
     */
    GlobalEntityAccountNo generate(String entityType, Integer entityId,
                                   String entitySystem, String displayName);

    /** Find a record by its accountNo column. */
    Optional<GlobalEntityAccountNo> getByAccountNo(Integer accountNo);

    /**
     * Find the account number for an already-registered entity.
     * Returns empty if the entity has not been registered yet.
     */
    Optional<GlobalEntityAccountNo> getByEntity(String entityType, Integer entityId, String entitySystem);

    /**
     * After saving an entity, link its PK back to the pre-generated account number.
     * Used in 2-step flows: generate first → save entity → link.
     */
    GlobalEntityAccountNo link(Integer accountNo, Integer entityId, String entitySystem);

    /**
     * Update the entity reference on an existing account number.
     * Used when an entity transitions — e.g. APPLICANT → CONSUMER on approval.
     */
    GlobalEntityAccountNo updateEntity(Integer accountNo, String entityType, Integer entityId, String entitySystem);

    /** Activate or deactivate an account number. */
    GlobalEntityAccountNo setActive(Integer accountNo, boolean active);

    /** Paged list, optionally filtered by entityType and/or free-text search. */
    Page<Map<String, Object>> getPagedList(String entityType, String q, Pageable pageable);

    /**
     * Register an entity that already has a pre-existing account number (e.g. back-fill of legacy data).
     * Inserts with id = accountNo = existingAccountNo via native query.
     * No-op if the accountNo is already registered.
     */
    GlobalEntityAccountNo register(String entityType, Integer entityId,
                                   String entitySystem, String displayName,
                                   Integer existingAccountNo);
}
