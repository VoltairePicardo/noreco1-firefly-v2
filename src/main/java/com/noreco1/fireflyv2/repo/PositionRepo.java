package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepo extends JpaRepository<Position, Integer> {

    List<Position> findByDepartmentIdAndDivisionIdAndSectionId(Integer deptId, Integer divId, Integer secId);
    List<Position> findByDepartmentIdAndDivisionId(Integer deptId, Integer divId);
    List<Position> findByDepartmentId(Integer deptId);
}
