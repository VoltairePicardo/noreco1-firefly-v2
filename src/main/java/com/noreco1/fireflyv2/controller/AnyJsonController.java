package com.noreco1.fireflyv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.dtoers.EmployeeDtoer;
import com.noreco1.fireflyv2.dtoers.WorkflowDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.MenuService;
import com.noreco1.fireflyv2.service.PageService;
import com.noreco1.fireflyv2.service.SlEntityService;
import com.noreco1.fireflyv2.mysql_model.*;
import com.noreco1.fireflyv2.mysql_repo.BarangayRepo;
import com.noreco1.fireflyv2.repo.TownRepo;
import com.noreco1.fireflyv2.mysql_repo.SitioRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/json")
public class AnyJsonController {

    @Autowired
    MenuService menuService;

    @Autowired
    PageService pageService;

    @Autowired
    SlEntityService slEntityService;

    @Autowired
    WorkflowDtoer workflowDtoer;

    @Autowired
    TaxCodeRepo taxCodeRepo;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    SLEntityClassificationRepo slEntityClassificationRepo;

    @Autowired
    OfficeRepo officeRepo;

    @Autowired
    InventoryCategoryRepo inventoryCategoryRepo;

    @Autowired
    InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    TownRepo townRepo;

    @Autowired
    BarangayRepo barangayRepo;

    @Autowired
    SitioRepo sitioRepo;

    @Autowired
    SpecialEquipmentTypeRepo specialEquipmentTypeRepo;

    @Autowired
    EmployeeDtoer employeeDtoer;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private DivisionRepo divisionRepo;

    @Autowired
    private BudgetTypeRepo budgetTypeRepo;

    @Autowired
    private ProjectTypeRepo projectTypeRepo;

    @Autowired
    private BankTransactionTypeRepo bankTransactionTypeRepo;

    @Autowired
    private BudgetItemClassificationRepo budgetItemClassificationRepo;

    @Autowired
    private DeliveryDateRepo deliveryDateRepo;

    @Autowired
    private FundingSourceRepo fundingSourceRepo;

    @Autowired
    private VehicleRepo vehicleRepo;

    @Autowired
    private PurposeRepo purposeRepo;

    @Autowired
    private SlEntityRepo slEntityRepo;

    @Autowired
    DocumentStatusRepo documentStatusRepo;

    @Autowired
    private ProjectTypeBudgetLineItemRepo projectTypeBudgetLineItemRepo;

    @Autowired
    private GeneralClassificationRepo generalClassificationRepo;

    @Autowired
    private DivisionActivityRepo divisionActivityRepo;

    @Autowired
    private StrategicInitiativeRepo strategicInitiativeRepo;

    @Autowired
    private ModeRepo modeRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    private BudgetSubItemRepo budgetSubItemRepo;

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Autowired
    private CostEstimateRepo costEstimateRepo;

    @GetMapping(value = "/entities")

    public List<SlEntity> getEntities(@RequestParam(value = "entityTypes", required = false) Integer[] entityTypes) {
        if (entityTypes != null && entityTypes.length > 0) {
            return slEntityService.findAllByType(entityTypes);
        } else {
            return slEntityService.findAll();
        }
    }

    @GetMapping(value = "/entities/search")
    public org.springframework.data.domain.Page<SlEntity> searchEntities(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "entityTypes", required = false) Integer[] entityTypes,
            @RequestParam(value = "classification", required = false) String classification) {
        PageRequest pageRequest = PageRequest.of(page, size);
        boolean hasMarkers        = entityTypes != null && entityTypes.length > 0;
        boolean hasClassification = classification != null && !classification.isEmpty();
        String  query             = q.isEmpty() ? null : q;
        if (hasMarkers && hasClassification) {
            return slEntityService.findByClassificationQueryAndTypes(classification, query, entityTypes, pageRequest);
        } else if (hasMarkers) {
            return slEntityService.findByQueryAndTypes(query, entityTypes, pageRequest);
        } else if (hasClassification) {
            return slEntityService.findByClassificationQuery(classification, query, pageRequest);
        } else {
            return slEntityService.findByQuery(q, pageRequest);
        }
    }

    @GetMapping(value = "/sl-entity-classifications")
    public List<SLEntityClassification> getSlEntityClassifications() {
        return slEntityClassificationRepo.findAll();
    }

    @GetMapping(value = "/menus")
    
    public List<MenuDto> getMenus() {
        return menuService.findAll();
    }

    @GetMapping(value = "/pages")
    
    public List<Page> getPages() {
        return pageService.findAllWithComponents();
    }

    @GetMapping(value = "/page-components/{pageId}")
    
    public List<PageComponentDto> getPageComponents(@PathVariable Integer pageId) {
        return pageService.getPageComponents(pageId);
    }

    @GetMapping(value = "/workflow-actions/{transId}")
    
    public List<WorkflowActionsDto> getWorkflowActions(@PathVariable Integer transId) {
        return workflowDtoer.getWorkflowActionsDtoByWfId(transId);
    }

    @Autowired
    DocumentWorkflowActionMapRepo dwfMapRepo;

    @GetMapping(value = "/workflow-actions/all/{workflowId}")
    
    public List<WorkflowActionsDto> getAllWorkflowActions(@PathVariable Integer workflowId) {
        List<WorkflowActionsDto> workflowActionsDtos = new ArrayList<>();
        int biggestSequence = 0;

        List<DocumentWorkflowActionMap> actions = dwfMapRepo.findByWorkflowId(workflowId);
        for (DocumentWorkflowActionMap a : actions) {
            WorkflowActionsDto dto = new WorkflowActionsDto();
            dto.setActionMapId(a.getId());
            dto.setActionId(a.getWorkflowAction().getId());
            dto.setAction(a.getWorkflowAction().getAction());
            dto.setSequence(a.getSequence());

            workflowActionsDtos.add(dto);

            if (dto.getSequence() > biggestSequence) {
                biggestSequence = dto.getSequence();
            }
        }
        return workflowActionsDtos;
    }

    @GetMapping(value = "/tax-codes")
    
    public List<TaxCode> getTaxCodes() {
        return taxCodeRepo.findAll();
    }

    @GetMapping(value = "/document-logs/{transId}")
    public List<Map<String, Object>> logs(@PathVariable Integer transId) {
        List<DocumentLog> rawLogs = documentLogRepo.findAllByTransactionIdOrderByCreatedAtDesc(transId);
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentLog log : rawLogs) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id",        log.getId());
            dto.put("createdAt", log.getCreatedAt());

            // map loggedBy → createdBy
            Map<String, Object> createdBy = new HashMap<>();
            if (log.getLoggedBy() != null) {
                createdBy.put("fullName", log.getLoggedBy().getFullName());
                createdBy.put("username", log.getLoggedBy().getUsername());
            }
            dto.put("createdBy", createdBy);

            // parse newValue JSON to extract action (documentStatus) and remarks
            String action  = null;
            String remarks = null;
            if (log.getNewValue() != null && !log.getNewValue().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> newValueMap = mapper.readValue(log.getNewValue(), Map.class);
                    action  = (String) newValueMap.get("documentStatus");
                    remarks = (String) newValueMap.get("remarks");
                } catch (Exception ignored) { }
            }
            dto.put("action",  action  != null ? action  : "");
            dto.put("remarks", remarks != null ? remarks : "");

            result.add(dto);
        }
        return result;
    }

    @GetMapping(value = "/setting/{code}")
    
    public Map logs(@PathVariable String code) {
        Map m = new HashMap();
        try {
            m = settingFacade.getByCode(code);
        } finally {}

        return m;
    }

    @GetMapping(value = "/this-week-range")
    
    public Map thisWeekRange() {
        return DateHelper.thisWeekRange();
    }

    @GetMapping(value = "/sl-entity-classification/{isSubLedger}")
    
    public List<SLEntityClassification> getWorkflowActions(@PathVariable Boolean isSubLedger) {
        List<SLEntityClassification> classifications;
        if (!isSubLedger) {
            classifications = slEntityClassificationRepo.findAll();
        } else {
            classifications = slEntityClassificationRepo.findByIsSubLedger(isSubLedger);
        }
        return classifications;
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/offices")
    public List<Office> getOffices() {
        return officeRepo.findAll();
    }

    @GetMapping(value = "/inventory-categories")
    
    public List<InventoryCategory> getItemCategories() {
        return inventoryCategoryRepo.findAll();
    }

    @GetMapping(value = "/office-user")
    
    public Office getUserOffice() {
        return employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo()).getOffice();
    }

    @GetMapping(value = "/inventory-locations")
    
    public List<InventoryLocation> getInventoryLocations() {
        return inventoryLocationRepo.findAll();
    }

    @GetMapping(value = "/town-list")
    
    public List<com.noreco1.fireflyv2.model.Town> getTowns() {
        return townRepo.findAll();
    }

    @GetMapping(value = "/barangay-by-town/{townId}")
    
    public List<Barangay> getAllBarangayByTown(@PathVariable Integer townId) {
        return barangayRepo.findAllByTownIdOrderByBrgyName(townId);
    }

    @GetMapping(value = "/sitio-by-barangay/{brgyId}")
    
    public List<Sitio> getAllSitioByBarangay(@PathVariable Integer brgyId) {
        return sitioRepo.findAllByBrgyIdOrderBySitioName(brgyId);
    }

    @GetMapping(value = "/special-equipment-type")
    
    public List<SpecialEquipmentType> getTypes() {
        return specialEquipmentTypeRepo.findAll();
    }

    @GetMapping(value = "/departments")
    
    public List<DepartmentDto> getDepartments() {
        return employeeDtoer.getDepartments();
    }

    @GetMapping(value = "/divisions/{departmentId}")
    
    public List<DivisionDto> getDivisions(@PathVariable Integer departmentId) {
        return employeeDtoer.getDivisions(departmentId);
    }

    @GetMapping(value = "/departments-by-user")
    
    public List<DepartmentDto> getDepartmentsByUser() {
        return employeeDtoer.getDepartmentsByUser();
    }

    @GetMapping(value = "/divisions-by-user")
    
    public List<DivisionDto> getDivisionsByUser() {
        return employeeDtoer.getDivisionsByUser();
    }

    @GetMapping(value = "/sections/{divisionId}")
    
    public List<SectionDto> getSectionsByDivision(@PathVariable Integer divisionId) {
        return employeeDtoer.getSectionsByDivision(divisionId);
    }

    @GetMapping(value = "/division-activities/{divisionId}")
    
    public List<DivisionActivity> getDivisionActivityByDivision(@PathVariable Integer divisionId) {
        return divisionActivityRepo.findAllByDivisionId(divisionId);
    }

    @GetMapping(value = "/sections")
    
    public List<SectionDto> getSections() {
        return employeeDtoer.getSections();
    }

    @GetMapping(value = "/positions/{deptId}/{divId}/{sectionId}")
    
    public List<Position> getPositions(@PathVariable Integer deptId, @PathVariable Integer divId, @PathVariable Integer sectionId) {
        return employeeDtoer.getPositions(deptId, divId, sectionId);
    }

    @GetMapping(value = "/banks")
    
    public List<Bank> getAllBanks() {
        return bankRepo.findAll();
    }

    @GetMapping(value = "/bank-transaction-types")
    
    public List<BankTransactionType> getAllBankTransactionTypes() {
        return bankTransactionTypeRepo.findAll();
    }

    @GetMapping(value = "/divisions")
    
    public List<Division> getDivisions() {
        return divisionRepo.findAllByOrderByNameAsc();
    }

    @GetMapping(value = "/user-department")
    
    public Department getUserDepartment() {
        Employee employee = this.employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        if (employee == null || employee.getDepartment() == null) {
            return null;
        }
        return departmentRepo.findById(employee.getDepartment().getId()).orElse(null);

    }

    @GetMapping(value = "/user-division")
    
    public Division getUserDivision() {
        Employee employee = this.employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        if (employee == null || employee.getDivision() == null) {
            return null;
        }
        return divisionRepo.findById(employee.getDivision().getId()).orElse(null);
    }

    @GetMapping(value = "/employee/{accountNumber}")
    
    public Employee getEmployee(@PathVariable Integer accountNumber) {
        return employeeRepo.findOneByAccountNumber(accountNumber);
    }

    @GetMapping(value = "/budget-types")
    
    public List<BudgetType> getBudgetTypes() {
        return this.budgetTypeRepo.findAll();
    }

    @GetMapping(value = "/project-types")
    
    public List<ProjectType> getProjectTypes() {
        return this.projectTypeRepo.findAll();
    }

    @GetMapping(value = "/project-types-for-budget-line-item")
    
    public List<ProjectTypeBudgetLineItem> getProjectTypesForBudgetLineItem() {
        return this.projectTypeBudgetLineItemRepo.findAll();
    }

    @GetMapping(value = "/strategic-initiatives-for-budget-line-item")
    
    public List<StrategicInitiative> getStrategicInitiativesForBudgetLineItem() {
        User loggedInUser = authenticationFacade.getLoggedIn();

        if (!Checker.isValidId(loggedInUser.getId())) {
            // log.warn("Invalid user ID for logged-in user");
            return Collections.emptyList();
        }

        Employee employee = employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());

        if (employee != null && employee.getDepartment() != null
                && Checker.isValidId(employee.getDepartment().getId())) {
            Integer departmentId = employee.getDepartment().getId();
            return strategicInitiativeRepo.findAllByDepartmentIdOrderByDescriptionAsc(departmentId);
        }

        // Fallback to all if no department is associated
        return strategicInitiativeRepo.findAll();
    }

    @GetMapping(value = "/general-classification")
    
    public List<GeneralClassification> getGeneralClassifications() {
        return this.generalClassificationRepo.findAll();
    }

    @GetMapping(value = "/budget-item-classifications")
    
    public List<BudgetItemClassification> getBudgetItemClassifications() {
        return budgetItemClassificationRepo.findAll();
    }

    @GetMapping(value = "/delivery-dates")
    
    public List<DeliveryDate> getDeliveryDates() {
        return deliveryDateRepo.findAll();
    }

    @GetMapping(value = "/funding-sources")
    
    public List<FundingSource> getFundingSources() {
        return fundingSourceRepo.findAll();
    }

    @GetMapping(value = "/vehicles")
    
    public List<Vehicle> getVehicles() {
        return vehicleRepo.findAll();
    }

    @RequestMapping(value = "/server-date")
    
    public Date defaultDate() {
        return DateHelper.getServerDate();
    }

    @GetMapping(value = "/purposes")
    
    public List<Purpose> getPurposes() {
        return purposeRepo.findAll();
    }

    @GetMapping(value = "/login-employee")
    
    public SlEntity getLoginEmployee() {
        return slEntityRepo.findOneByAccountNo(authenticationFacade.getLoggedIn().getAccountNo());
    }

    @GetMapping(value = "/document-statuses")
    
    public List<DocumentStatus> getDocumentStatuses() {
        return documentStatusRepo.findAllVoucherDocumentStatus();
    }

    @GetMapping(value = "/modes")

    public List<Mode> getModes() {
        return modeRepo.findAll();
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/budget-line-items")
    public List<Map<String, Object>> getBudgetLineItems() {
        return budgetLineItemDetailRepo.findAllByBudgetLineItemDocumentStatusIdOrderByCodeAsc(7)
            .stream()
            .map(b -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", b.getId());
                m.put("code", b.getCode());
                m.put("title", b.getTitle());
                m.put("hasSubItems", b.getHasSubItems());
                return m;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/budget-sub-items/{id}")
    public List<Map<String, Object>> getBudgetSubItems(@PathVariable Integer id) {
        return budgetSubItemRepo.findAllByBudgetLineItemDetailId(id)
            .stream()
            .map(b -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",          b.getId());
                m.put("description", b.getDescription());
                m.put("amount",      b.getAmount());
                return m;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/budget-line-item-balance/{id}")
    public Map<String, Object> getBudgetLineItemBalance(@PathVariable Integer id) {
        int reviewed  = com.noreco1.fireflyv2.model.enums.DocumentStatus.REVIEWED_AND_ACCEPTED.getId();
        int cancelled = com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("quantityBalance",  budgetLineItemDetailRepo.getBudgetLineItemDetailQuantityBalance(reviewed,  id));
        m.put("amountBalanceCV",  budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalanceCV(cancelled, id));
        m.put("amountBalancePOJO", budgetLineItemDetailRepo.getBudgetLineItemDetailAmountBalancePOJO(cancelled, id));
        return m;
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/budget-sub-item-balance/{id}")
    public Map<String, Object> getBudgetSubItemBalance(@PathVariable Integer id) {
        int cancelled = com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("amountBalanceCV",   budgetSubItemRepo.getBudgetSubItemAmountBalanceCV(cancelled,  id));
        m.put("amountBalancePOJO", budgetSubItemRepo.getBudgetSubItemAmountBalancePOJO(cancelled, id));
        return m;
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/work-orders")
    public List<Map<String, Object>> getOpenWorkOrders() {
        return workOrderRepo.findByIsClosed(false)
            .stream()
            .map(w -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", w.getId());
                m.put("code", w.getCode());
                m.put("description", w.getDescription());
                return m;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/cost-estimates")
    public List<Map<String, Object>> getCostEstimatesForPR() {
        return costEstimateRepo.findIdCodeAndProjectByDocumentStatusId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId())
            .stream()
            .map(row -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",          row[0]);
                m.put("code",        row[1]);
                m.put("projectCode", row[2]);
                m.put("projectName", row[3]);
                return m;
            })
            .collect(Collectors.toList());
    }

    @Autowired
    private BusinessSegmentRepo businessSegmentRepo;

    @Autowired
    private AccountGroupRepo accountGroupRepo;

    @Autowired
    private AccountTypeRepo accountTypeRepo;

    @Transactional(readOnly = true)
    @GetMapping(value = "/business-segments")
    public List<BusinessSegment> getBusinessSegments() {
        return businessSegmentRepo.findAll();
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/account-groups")
    public List<AccountGroup> getAccountGroups() {
        return accountGroupRepo.findAll();
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/account-types")
    public List<AccountType> getAccountTypes() {
        return accountTypeRepo.findAll();
    }
}
