package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.enums.EntitySystem;
import com.noreco1.fireflyv2.model.enums.EntityType;
import com.noreco1.fireflyv2.mysql_model.GlobalEntityAccountNo;
import com.noreco1.fireflyv2.mysql_repo.GlobalEntityAccountNoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GlobalEntityAcctNoFacadeImpl implements GlobalEntityAcctNoFacade {

    private final GlobalEntityAccountNoRepo  repo;
    private final AuthenticationFacade       authenticationFacade;
    private final PlatformTransactionManager mysqlTransactionManager;

    @Override
    public GlobalEntityAccountNo generate(String entityType, Integer entityId,
                                          String entitySystem, String displayName) {
        TransactionTemplate tt = new TransactionTemplate(mysqlTransactionManager);
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                return tt.execute(status -> doGenerate(entityType, entityId, entitySystem, displayName));
            } catch (DataIntegrityViolationException ignored) {
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Failed to generate a unique account number after 5 attempts.");
    }

    private GlobalEntityAccountNo doGenerate(String entityType, Integer entityId,
                                              String entitySystem, String displayName) {
        EntityType type   = EntityType.fromCode(entityType);
        EntitySystem sys  = EntitySystem.fromCode(entitySystem);

        int max = repo.findMaxAccountNo();
        int candidate = max > 0 ? max + 1 : 100000;
        while (!isValidMod11(candidate)) {
            candidate++;
        }

        Date now = new Date();
        GlobalEntityAccountNo record = new GlobalEntityAccountNo();
        record.setAccountNo(candidate);
        record.setEntityType(type.getCode());
        record.setEntityId(entityId != null ? entityId : 0);
        record.setEntitySystem(sys.getCode());
        record.setDisplayName(displayName != null ? displayName : "");
        record.setIsActive(true);
        record.setCreatedByUserId(authenticationFacade.getLoggedIn().getId());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        return repo.save(record);
    }

    @Override
    public Optional<GlobalEntityAccountNo> getByAccountNo(Integer accountNo) {
        return repo.findByAccountNo(accountNo);
    }

    @Override
    public Optional<GlobalEntityAccountNo> getByEntity(String entityType, Integer entityId, String entitySystem) {
        EntityType type  = EntityType.fromCode(entityType);
        EntitySystem sys = EntitySystem.fromCode(entitySystem);
        return repo.findByEntityTypeAndEntityIdAndEntitySystem(type.getCode(), entityId, sys.getCode());
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public GlobalEntityAccountNo link(Integer accountNo, Integer entityId, String entitySystem) {
        EntitySystem sys = EntitySystem.fromCode(entitySystem);
        GlobalEntityAccountNo record = findOrThrow(accountNo);
        record.setEntityId(entityId);
        record.setEntitySystem(sys.getCode());
        record.setUpdatedAt(new Date());
        return repo.save(record);
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public GlobalEntityAccountNo updateEntity(Integer accountNo, String entityType,
                                              Integer entityId, String entitySystem) {
        EntityType type  = EntityType.fromCode(entityType);
        EntitySystem sys = EntitySystem.fromCode(entitySystem);
        GlobalEntityAccountNo record = findOrThrow(accountNo);
        record.setEntityType(type.getCode());
        record.setEntityId(entityId);
        record.setEntitySystem(sys.getCode());
        record.setUpdatedAt(new Date());
        return repo.save(record);
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public GlobalEntityAccountNo setActive(Integer accountNo, boolean active) {
        GlobalEntityAccountNo record = findOrThrow(accountNo);
        record.setIsActive(active);
        record.setUpdatedAt(new Date());
        return repo.save(record);
    }

    @Override
    public Page<Map<String, Object>> getPagedList(String entityType, String q, Pageable pageable) {
        String typeCode = (entityType != null && !entityType.isBlank())
                ? EntityType.fromCode(entityType).getCode()
                : null;
        String safeQ = (q != null && !q.isBlank()) ? q.trim() : null;
        return repo.getPagedList(typeCode, safeQ, pageable);
    }

    @Override
    @Transactional("mysqlTransactionManager")
    public GlobalEntityAccountNo register(String entityType, Integer entityId,
                                          String entitySystem, String displayName,
                                          Integer existingAccountNo) {
        EntityType type  = EntityType.fromCode(entityType);
        EntitySystem sys = EntitySystem.fromCode(entitySystem);

        Optional<GlobalEntityAccountNo> byAccountNo = repo.findByAccountNo(existingAccountNo);
        if (byAccountNo.isPresent()) return byAccountNo.get();

        Optional<GlobalEntityAccountNo> byEntity =
                repo.findByEntityTypeAndEntityIdAndEntitySystem(type.getCode(), entityId, sys.getCode());
        if (byEntity.isPresent()) return byEntity.get();

        // Repo insert: manually set id = accountNo = existingAccountNo
        repo.insertWithId(
                existingAccountNo,
                existingAccountNo,
                type.getCode(),
                entityId,
                sys.getCode(),
                displayName != null ? displayName : "",
                authenticationFacade.getLoggedIn().getId()
        );

        return repo.findByAccountNo(existingAccountNo).orElseThrow();
    }

    private boolean isValidMod11(int number) {
        String digits   = String.valueOf(number);
        int    total    = 0;
        int    weight   = digits.length();
        for (char c : digits.toCharArray()) {
            total += Character.getNumericValue(c) * weight--;
        }
        return (total % 11) == 0;
    }

    private GlobalEntityAccountNo findOrThrow(Integer accountNo) {
        return repo.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account number " + accountNo + " not found."));
    }
}
