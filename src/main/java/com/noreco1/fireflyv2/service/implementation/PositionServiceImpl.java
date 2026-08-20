package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.PositionRepo;
import com.noreco1.fireflyv2.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepo positionRepo;
    private final AuthenticationFacade authFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Position> list() {
        return positionRepo.findAll().stream()
                .sorted(Comparator.comparing(Position::getName))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Position findById(Integer id) {
        return positionRepo.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PostResponse create(Position position) {
        position.setId(null);
        PostResponse res = new PostResponse();
        try {
            position.setCreatedBy(authFacade.getLoggedIn());
            position.setCreatedAt(new Date());
            position.setUpdatedAt(new Date());
            resolveNullableRefs(position);
            Position saved = positionRepo.save(position);
            res.setModelId(saved.getId());
            res.setSuccessMessage("Position successfully saved!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    @Transactional
    public PostResponse update(Position position) {
        PostResponse res = new PostResponse();
        try {
            Position existing = positionRepo.findById(position.getId()).orElse(null);
            if (existing == null) { res.setFailureMessage("Position not found."); return res; }

            existing.setCode(position.getCode());
            existing.setName(position.getName());
            existing.setDepartment(position.getDepartment() != null ? position.getDepartment() : blankDepartment());
            existing.setDivision(position.getDivision()   != null ? position.getDivision()   : blankDivision());
            existing.setSection(position.getSection()     != null ? position.getSection()     : blankSection());
            existing.setUpdatedAt(new Date());

            positionRepo.save(existing);
            res.setModelId(existing.getId());
            res.setSuccessMessage("Position successfully updated!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public PostResponse deleteById(Integer id) {
        PostResponse res = new PostResponse();
        try {
            positionRepo.deleteById(id);
            res.setSuccessMessage("Position successfully deleted!");
            res.setSuccess(true);
        } catch (Exception e) {
            res.setFailureMessage(e.getMessage());
        }
        return res;
    }

    private void resolveNullableRefs(Position p) {
        if (p.getDepartment() == null) p.setDepartment(blankDepartment());
        if (p.getDivision()   == null) p.setDivision(blankDivision());
        if (p.getSection()    == null) p.setSection(blankSection());
    }

    private Department blankDepartment() { Department d = new Department(); d.setId(0); return d; }
    private Division   blankDivision()   { Division   d = new Division();   d.setId(0); return d; }
    private Section    blankSection()    { Section    s = new Section();    s.setId(0); return s; }
}
