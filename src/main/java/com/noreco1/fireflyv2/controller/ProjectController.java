package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.ProjectFunding;

import com.noreco1.fireflyv2.repo.ProjectFundingRepo;
import com.noreco1.fireflyv2.repo.ProjectRepo;
import com.noreco1.fireflyv2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ProjectFundingRepo projectFundingRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<Project> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (from == null) from = "2000-01-01";
        if (to == null)   to   = "2099-12-31";

        if (statusId != null && q != null && !q.isEmpty()) {
            return projectService.findAll(from, to, statusId, q, pageable);
        } else if (statusId != null) {
            return projectService.findAll(from, to, statusId, pageable);
        } else if (q != null && !q.isEmpty()) {
            return projectService.findAll(from, to, q, pageable);
        }
        return projectService.findAll(from, to, pageable);
    }

    @GetMapping("/{id}")
    public Project getById(@PathVariable Integer id) {
        return projectService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return projectService.getDocumentsStatuses();
    }

    @GetMapping("/funding-sources")
    public List<ProjectFunding> fundingSources() {
        return projectService.getProjectFunding();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        Project project = buildProject(payload);
        BindingResult bindingResult = new BeanPropertyBindingResult(project, "project");
        return projectService.processCreate(project, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) {
            response.setFailureMessage("ID is required for update.");
            return response;
        }
        Integer id = ((Number) idObj).intValue();
        Project existing = projectRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Project not found.");
            return response;
        }

        if (payload.get("name") != null)          existing.setName((String) payload.get("name"));
        if (payload.get("location") != null)       existing.setLocation((String) payload.get("location"));
        if (payload.get("purpose") != null)        existing.setPurpose((String) payload.get("purpose"));
        if (payload.get("paymentDetails") != null) existing.setPaymentDetails((String) payload.get("paymentDetails"));

        Object fsObj = payload.get("fundingSource");
        if (fsObj instanceof Map<?, ?> fsMap && fsMap.get("id") != null) {
            ProjectFunding pf = new ProjectFunding();
            pf.setId(((Number) fsMap.get("id")).intValue());
            existing.setProjectFunding(pf);
        }

        existing.setUpdatedAt(new Date());
        Project saved = projectRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Project successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update project.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto, HttpServletRequest request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return projectService.process(dto, bindingResult, messageSource, request);
    }

    private Project buildProject(Map<String, Object> payload) {
        Project p = new Project();
        p.setName((String) payload.get("name"));
        p.setLocation((String) payload.get("location"));
        p.setPurpose((String) payload.get("purpose"));
        p.setPaymentDetails((String) payload.get("paymentDetails"));

        Object fsObj = payload.get("fundingSource");
        if (fsObj instanceof Map<?, ?> fsMap && fsMap.get("id") != null) {
            ProjectFunding pf = new ProjectFunding();
            pf.setId(((Number) fsMap.get("id")).intValue());
            p.setProjectFunding(pf);
        }
        return p;
    }
}
