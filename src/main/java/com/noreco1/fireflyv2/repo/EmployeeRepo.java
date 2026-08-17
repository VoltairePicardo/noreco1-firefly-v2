package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {
    Employee findOneByAccountNumber(Integer accountNo);
    Employee findFirstByPositionIdOrderByIdAsc(Integer positionId);
    Employee findFirstByPositionId(Integer positionId);

    @Transactional(readOnly = true)
    @Query(
            value = "SELECT dm.FK_accountNo " +
                    "FROM employee e " +
                    "JOIN employee dm " +
                    "  ON dm.FK_departmentId = e.FK_departmentId " +
                    "JOIN position p " +
                    "  ON dm.FK_positionId = p.id " +
                    "WHERE e.FK_accountNo = :accountNumber " +
                    "  AND p.FK_positionLevelId = 3 " +
                    "  AND dm.isActive = 1 " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Integer getDepartmentManagerAccountNoByLoggedInUser(@Param("accountNumber") Integer accountNumber);

}
