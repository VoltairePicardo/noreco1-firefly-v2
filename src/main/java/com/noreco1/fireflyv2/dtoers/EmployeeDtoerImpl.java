package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.DepartmentDto;
import com.noreco1.fireflyv2.controller.response.DivisionDto;
import com.noreco1.fireflyv2.controller.response.SectionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EmployeeDtoerImpl implements EmployeeDtoer{

    @Autowired
    DepartmentRepo departmentRepo;

    @Autowired
    DivisionRepo divisionRepo;

    @Autowired
    SectionRepo sectionRepo;

    @Autowired
    PositionRepo positionRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private SettingFacade settingFacade;

    @Override
    public List<DepartmentDto> getDepartments() {

        List<Department> departments = departmentRepo.findAll();
        List<DepartmentDto> ret = new ArrayList<>();

        for(Department obj : departments){

            Integer id = obj.getId();
            String name = obj.getName();
            String abbrev = obj.getAbbreviation();

            DepartmentDto dto = new DepartmentDto(id, name, abbrev);

            ret.add(dto);
        }

        return ret;
    }

    @Override
    public List<DivisionDto> getDivisions(Integer departmentId) {

        List<Division> divisions = divisionRepo.findAll();
        List<DivisionDto> ret = new ArrayList<>();

        List<Division> divisionsByDeptId = new ArrayList<>();

        for(Division div : divisions){
            if(div.getDepartment() != null) {
                if (div.getDepartment().getId() == departmentId) {
                    divisionsByDeptId.add(div);
                }
            }
        }

        for(Division obj : divisionsByDeptId){

            Integer id = obj.getId();
            String name = obj.getName();
            Integer deptId = obj.getDepartment().getId();
            Department department = departmentRepo.findById(deptId).orElse(null);

            DivisionDto dto = new DivisionDto(id, name, departmentId, department.getName());

            ret.add(dto);
        }

        return ret;
    }

    @Override
    public List<DepartmentDto> getDepartmentsByUser() {

        Employee employee = employeeRepo.findOneByAccountNumber(
                authenticationFacade.getLoggedIn().getAccountNo()
        );

        if (employee == null || employee.getPosition() == null) {
            return Collections.emptyList();
        }

        List<Department> departments = new ArrayList<>();

        Map<String, Object> gmSetting =
                settingFacade.getByCode("BUDGET_LINE_ITEM_GENERAL_MANAGER");
        Integer generalManagerPositionId = (Integer) gmSetting.get("id");

        Map<String, Object> fsdSetting =
                settingFacade.getByCode("BUDGET_LINE_ITEM_FSD_MANAGER");
        Integer fsdPositionId = (Integer) fsdSetting.get("id");

        Integer employeePositionId = employee.getPosition().getId();

        if (employeePositionId.equals(generalManagerPositionId) || employeePositionId.equals(fsdPositionId)) {
            departments = departmentRepo.findAll();
        } else if (employee.getDepartment() != null) {
            Department department = departmentRepo.findById(employee.getDepartment().getId()).orElse(null);
            if (department != null) {
                departments.add(department);
            }
        }

        List<DepartmentDto> result = new ArrayList<>();
        for (Department department : departments) {
            result.add(new DepartmentDto(
                    department.getId(),
                    department.getName(),
                    department.getAbbreviation()
            ));
        }

        return result;
    }

    @Override
    public List<DivisionDto> getDivisionsByUser() {

        Employee employee = employeeRepo.findOneByAccountNumber(
                authenticationFacade.getLoggedIn().getAccountNo()
        );

        if (employee == null || employee.getDepartment() == null) {
            return Collections.emptyList();
        }

        if (employee.getPosition() == null) {
            return Collections.emptyList();
        }

        List<Division> divisions = new ArrayList<>();
        Integer positionId = employee.getPosition().getId();

        // Load settings once
        Integer generalManagerPositionId =
                (Integer) settingFacade.getByCode("BUDGET_LINE_ITEM_GENERAL_MANAGER").get("id");
        Integer fsdPositionId =
                (Integer) settingFacade.getByCode("BUDGET_LINE_ITEM_FSD_MANAGER").get("id");

        // General Manager: access to all divisions
        if (positionId.equals(generalManagerPositionId)) {
            divisions = divisionRepo.findAll();
        }
        // FSD or department-level access
        else if (positionId.equals(fsdPositionId) || employee.getDivision() == null) {
            divisions = divisionRepo.findAllByDepartmentId(employee.getDepartment().getId());
        }
        // Division-level access
        else if (employee.getDivision() != null) {
            Division division = divisionRepo.findById(employee.getDivision().getId()).orElse(null);
            if (division != null) {
                divisions.add(division);
            }
        }

        List<DivisionDto> result = new ArrayList<>();
        for (Division d : divisions) {
            result.add(new DivisionDto(
                    d.getId(),
                    d.getName(),
                    d.getDepartment().getId(),
                    d.getDepartment().getName()
            ));
        }

        return result;

    }

    @Override
    public List<SectionDto> getSectionsByDivision(Integer divisionId) {

        List<Section> sections = sectionRepo.findAll();
        List<SectionDto> ret = new ArrayList<>();

        List<Section> sectionsByDivisionId = new ArrayList<>();

        for(Section sec : sections){
            if(sec.getDivision() != null) {
                if (sec.getDivision().getId() == divisionId) {
                    sectionsByDivisionId.add(sec);
                }
            }
        }

        for(Section obj : sectionsByDivisionId){

            Integer id = obj.getId();
            String name = obj.getName();
            Integer divId = obj.getDivision().getId();
            Division division = divisionRepo.findById(divId).orElse(null);

            SectionDto dto = new SectionDto(id, name, divisionId, division.getName());

            ret.add(dto);
        }

        return ret;
    }

    @Override
    public List<SectionDto> getSections() {

        List<Section> sections = sectionRepo.findAll();
        List<SectionDto> ret = new ArrayList<>();

        for(Section obj : sections){

            Integer id = obj.getId();
            String name = obj.getName();
            Integer divId = obj.getDivision().getId();
            Division division = divisionRepo.findById(divId).orElse(null);

            SectionDto dto = new SectionDto(id, name, divId, division.getName());

            ret.add(dto);
        }

        return ret;
    }

    @Override
    public List<Position> getPositions(Integer deptId, Integer divId, Integer sectionId) {
        List<Position> ret = new ArrayList<>();
        try{
            if(Checker.isValidId(deptId) && Checker.isValidId(divId) && Checker.isValidId(sectionId))
                ret = positionRepo.findByDepartmentIdAndDivisionIdAndSectionId(deptId, divId, sectionId);
            else if (Checker.isValidId(deptId) && Checker.isValidId(divId))
                ret = positionRepo.findByDepartmentIdAndDivisionId(deptId, divId);
            else if (Checker.isValidId(deptId))
                ret = positionRepo.findByDepartmentId(deptId);
            else
                ret = positionRepo.findAll();

        } catch (Exception ex) {
            Logger.getLogger(EmployeeDtoerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }
}
