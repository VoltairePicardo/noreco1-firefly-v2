package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Division;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DivisionRepo extends JpaRepository<Division, Integer> {

    List<Division> findAllByDepartmentId(int deptId);
    List<Division> findById(int divId);
    List<Division> findAllByOrderByNameAsc();

}
