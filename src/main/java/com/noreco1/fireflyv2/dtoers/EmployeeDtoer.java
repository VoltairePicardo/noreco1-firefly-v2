package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.model.Position;
import com.noreco1.fireflyv2.controller.response.DepartmentDto;
import com.noreco1.fireflyv2.controller.response.DivisionDto;
import com.noreco1.fireflyv2.controller.response.SectionDto;

import java.util.List;

public interface EmployeeDtoer {
    List<DepartmentDto> getDepartments();
    List<DivisionDto> getDivisions(Integer departmentId);
    List<DepartmentDto> getDepartmentsByUser();
    List<DivisionDto> getDivisionsByUser();
    List<SectionDto> getSectionsByDivision(Integer divisionId);
    List<SectionDto> getSections();
    List<Position> getPositions(Integer deptId, Integer divId, Integer sectionId);
}
