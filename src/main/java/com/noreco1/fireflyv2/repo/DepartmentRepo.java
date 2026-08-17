package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<Department, Integer> {
    @Query(value = "SELECT " +
            "d.id, " +
            "d.abbreviation, " +
            "d.name " +
            "from Department d " +
            "LEFT JOIN Budget b on d.id = b.FK_departmentId and b.year = :year " +
            "WHERE b.FK_departmentId is null",
            nativeQuery = true)
    List<Department> findAllFilteredByYear(@Param("year") Integer year);
}
