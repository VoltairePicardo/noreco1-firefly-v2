package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ConsumerRepo extends JpaRepository<Consumer, Integer> {

    @Query(value = "select * " +
            "from Consumer " +
            "LEFT JOIN FREETEXTTABLE (Consumer, *, :query) as keyTbl ON Consumer.consumerid = keyTbl.[KEY] " +
            "where (freetext (Consumer.*, :query) OR CAST(Consumer.AcctNo as NVARCHAR(255)) = :query) " +
            "ORDER BY RANK DESC  \n-- #pageable\n",
            countQuery = "select count(*) from Consumer " +
                    "LEFT JOIN FREETEXTTABLE (Consumer, *, :query) as keyTbl ON Consumer.consumerid = keyTbl.[KEY] " +
                    "where (freetext (Consumer.*, :query) OR CAST(Consumer.AcctNo as NVARCHAR(255)) = :query) ",
            nativeQuery = true)
    Page<Consumer> findAllByQuery(@Param("query") String query, Pageable pageable);

    @Query(value = "select * " +
            "from Consumer " +
            "where Consumer.consumerid IN (:ids) " +
            "  \n-- #pageable\n",
            countQuery = "select count(*) from Consumer " +
                    "where Consumer.consumerid IN (:ids) ",
            nativeQuery = true)
    Page<Consumer> findByConsumerIds(@Param("ids") Collection<Integer> ids, Pageable pageable);

    @Query(value = "select * " +
            "from Consumer " +
            "LEFT JOIN FREETEXTTABLE (Consumer, *, :query) as keyTbl ON Consumer.consumerid = keyTbl.[KEY] " +
            "where (freetext (Consumer.*, :query) OR CAST(Consumer.AcctNo as NVARCHAR(255)) = :query) " +
            "AND Consumer.consumerid IN (:ids) " +
            "ORDER BY RANK DESC  \n-- #pageable\n",
            countQuery = "select count(*) from Consumer " +
                    "LEFT JOIN FREETEXTTABLE (Consumer, *, :query) as keyTbl ON Consumer.consumerid = keyTbl.[KEY] " +
                    "where (freetext (Consumer.*, :query) OR CAST(Consumer.AcctNo as NVARCHAR(255)) = :query) " +
                    "AND Consumer.consumerid IN (:ids)",
            nativeQuery = true)
    Page<Consumer> findAllByQueryAndConsumerIds(@Param("ids") Collection<Integer> ids, @Param("query") String query, Pageable pageable);

}
