package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.controller.response.reports.MaterialIssueRegisterBIRDto;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;
import com.noreco1.fireflyv2.service.MaterialIssueRegisterService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.mysql_model.*;
import com.noreco1.fireflyv2.mysql_repo.*;
import com.noreco1.fireflyv2.validator.MaterialIssueRegisterValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by nsutgio2015 on 4/29/2015.
 * <p>
 * MCRCT
 */
@Service(value = "mirServiceImpl")
public class MaterialIssueRegisterServiceImpl implements MaterialIssueRegisterService, PrintableVoucher {

    private MaterialIssueRegister model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    MaterialIssueRegisterRepo mirRepo;

    @Autowired
    MaterialIssueRegisterDetailRepo mirDetailRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    @Autowired
    IomasMRHeaderRepo mrHeaderRepo;

    @Autowired
    IomasMRDetailRepo mrDetailRepo;

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    @Autowired
    MrctRepo mrctRepo;

    @Autowired
    InventoryAdjustmentRepo inventoryAdjustmentRepo;

    @Autowired
    InventoryAdjustmentDetailRepo inventoryAdjustmentDetailRepo;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    MaterialCreditTicketDetailRepo materialCreditTicketDetailRepo;

    @Autowired
    MaterialSalvageTicketRepo materialSalvageTicketRepo;

    @Autowired
    MaterialSalvageTicketDetailRepo materialSalvageTicketDetailRepo;

    @Autowired
    SalvageInventoryAdjustmentRepo salvageInventoryAdjustmentRepo;

    @Autowired
    SalvageInventoryAdjustmentDetailRepo salvageInventoryAdjustmentDetailRepo;

    @Autowired
    SalvageMaterialRequisitionRepo salvageMaterialRequisitionRepo;

    @Autowired
    SalvageMaterialRequisitionDetailRepo salvageMaterialRequisitionDetailRepo;

    @Autowired
    JunkMaterialsTicketRepo junkMaterialsTicketRepo;

    @Autowired
    JunkMaterialsTicketDetailRepo junkMaterialsTicketDetailRepo;

    @Autowired
    JunkMaterialsAdjustmentRepo junkMaterialsAdjustmentRepo;

    @Autowired
    JunkMaterialsAdjustmentDetailRepo junkMaterialsAdjustmentDetailRepo;

    @Autowired
    JunkMaterialsReleasingReceiptRepo junkMaterialsReleasingReceiptRepo;

    @Autowired
    JunkMaterialsReleasingReceiptDetailRepo junkMaterialsReleasingReceiptDetailRepo;

    @Autowired
    HouseWiringMaterialsIssuanceTicketRepo houseWiringMaterialsIssuanceTicketRepo;

    @Autowired
    HouseWiringMaterialsIssuanceTicketDetailRepo houseWiringMaterialsIssuanceTicketDetailRepo;

    @Autowired
    LostItemRepo lostItemRepo;

    @Autowired
    LostItemDetailRepo lostItemDetailRepo;

    @Autowired
    StatementOfAccountRepo statementOfAccountRepo;

    @Autowired
    StatementOfAccountDetailRepo statementOfAccountDetailRepo;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    // sql server data reader
    @Autowired
    IomasADHeaderRepo iomasADHeaderRepo;

    @Autowired
    IomasADDetailRepo iomasADDetailRepo;

    @Autowired
    IomasMCHeaderRepo iomasMCHeaderRepo;

    @Autowired
    IomasMCDetailRepo iomasMCDetailRepo;

    @Autowired
    IomasMSHeaderRepo iomasMSHeaderRepo;

    @Autowired
    IomasMSDetailRepo iomasMSDetailRepo;

    @Autowired
    IomasSAHeaderRepo iomasSAHeaderRepo;

    @Autowired
    IomasSADetailRepo iomasSADetailRepo;

    @Autowired
    IomasSMHeaderRepo iomasSMHeaderRepo;

    @Autowired
    IomasSMDetailRepo iomasSMDetailRepo;

    @Autowired
    IomasJMHeaderRepo iomasJMHeaderRepo;

    @Autowired
    IomasJMDetailRepo iomasJMDetailRepo;

    @Autowired
    IomasJAHeaderRepo iomasJAHeaderRepo;

    @Autowired
    IomasJADetailRepo iomasJADetailRepo;

    @Autowired
    IomasJRHeaderRepo iomasJRHeaderRepo;

    @Autowired
    IomasJRDetailRepo iomasJRDetailRepo;

    @Autowired
    IomasHWIHeaderRepo iomasHWIHeaderRepo;

    @Autowired
    IomasHWIDetailRepo iomasHWIDetailRepo;

    @Autowired
    IomasSOAHeaderRepo iomasSOAHeaderRepo;

    @Autowired
    IomasSOADetailRepo iomasSOADetailRepo;

    @Autowired
    IomasLIHeaderRepo iomasLIHeaderRepo;

    @Autowired
    IomasLIDetailRepo iomasLIDetailRepo;

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    ItemTransactionDetailRepo itemTransactionDetailRepo;

    @Autowired
    StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    StockReceiveRepo stockReceiveRepo;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        MaterialIssueRegister mir = mirRepo.findById(postData.getDocumentId()).orElse(null);

        if (mir != null && mir.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldMIVMap = this.forLogMapMain(mir);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            com.noreco1.fireflyv2.model.DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(mir, mir.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mir.setDocumentStatus(afterActionDocumentStatus);
            mir.setUpdatedAt(null);
            mir = mirRepo.save(mir);

            // for logging
            Map newMIVMap = this.forLogMapMain(mir);
            newMIVMap.put("remarks", postData.getRemarks());

            if (mir != null) {

                documentProcessingFacade.processAction(mir.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(mir.getTransaction(), authenticationFacade.getLoggedIn(), oldMIVMap, newMIVMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public List<MaterialIssueRegisterDto> findAll() {
        return null;
    }

    @Override
    public MaterialIssueRegisterDto findById(Integer id) {

        MaterialIssueRegister mir = mirRepo.findById(id).orElse(null);
        MaterialIssueRegisterDto dto = new MaterialIssueRegisterDto();

        if (mir != null) {
            dto.setId(mir.getId());
            dto.setParticulars(mir.getParticulars());
            dto.setLocalCode(mir.getCode());
            dto.setTransId(mir.getTransaction().getId());

            SlEntity approvingOfficer = slEntityRepo.findById(mir.getApprovingOfficer().getAccountNo()).orElse(null);
            SlEntity recommendingOfficer = slEntityRepo.findById(mir.getRecommendingOfficer().getAccountNo()).orElse(null);
            SlEntity checker = slEntityRepo.findById(mir.getChecker().getAccountNo()).orElse(null);

            dto.setsLApprovingOfficer(approvingOfficer);
            dto.setsLRecommendingOfficer(recommendingOfficer);
            dto.setsLChecker(checker);
            dto.setVoucherDate(mir.getVoucherDate());
            dto.setStatus(mir.getDocumentStatus().getStatus());
            dto.setDocumentStatus(mir.getDocumentStatus());
            dto.setVoucherDate(mir.getVoucherDate());
            dto.setLastUpdated(mir.getUpdatedAt());
            dto.setAmount(mir.getAmount());
            dto.setInventoryDocType(mir.getInventoryDocType());
            dto.setCreatedBy(mir.getCreatedBy());
            dto.setOffice(mir.getOffice());

            if(mir.getInventoryDocType() != null) {
                switch (mir.getInventoryDocType()) {
                    case "MCRT":
                        MaterialCreditTicket materialCreditTicket = materialCreditTicketRepo.findOneByTransactionId(mir.getInvDocTransactionId());

                        if (materialCreditTicket != null) {

                            System.out.println(materialCreditTicket.getId());
                            InventoryDocumentDto invDocDto = new InventoryDocumentDto();
                            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(materialCreditTicket.getTransaction().getId());

                            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                            for (StockTransactionDetail d : details) {
                                detailsDto.add(d.toDto());
                            }

                            invDocDto.setName(com.noreco1.fireflyv2.model.enums.DocumentType.MCT.getDescription());
                            invDocDto.setDate(materialCreditTicket.getVoucherDate());
                            invDocDto.setCode(materialCreditTicket.getCode());
                            invDocDto.setPurpose(materialCreditTicket.getRemarks());
                            invDocDto.setDetails(detailsDto);
                            invDocDto.setTransId(materialCreditTicket.getTransaction().getId());

                            dto.setInventoryDocument(invDocDto);
                        }
                        break;
                    case "SA":
                        StockAdjustment stockAdjustment = stockAdjustmentRepo.findOneByTransactionId(mir.getInvDocTransactionId());
                        if (stockAdjustment != null) {
                            InventoryDocumentDto invDocDto = new InventoryDocumentDto();
                            ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(mir.getInvDocTransactionId());

                            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                            for (ItemTransactionDetail d : details) {
                                d.setQuantity(d.getAdjustment());
                                d.setTotalCost(d.getAdjustment().multiply(d.getUnitCost()));
                                detailsDto.add(d.toDto());
                            }

                            invDocDto.setName(com.noreco1.fireflyv2.model.enums.DocumentType.SA.getDescription());
                            invDocDto.setDate(stockAdjustment.getVoucherDate());
                            invDocDto.setCode(stockAdjustment.getCode());
                            invDocDto.setPurpose(stockAdjustment.getRemarks());
                            invDocDto.setDetails(detailsDto);
                            invDocDto.setTransId(stockAdjustment.getTransaction().getId());

                            dto.setInventoryDocument(invDocDto);
                        }
                        break;
                    case "MST":
                        MaterialSalvageTicket materialSalvageTicket = materialSalvageTicketRepo.findOneByTransactionId(mir.getInvDocTransactionId());
                        if (materialSalvageTicket != null) {
                            InventoryDocumentDto invDocDto = new InventoryDocumentDto();
                            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(materialSalvageTicket.getTransaction().getId());

                            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                            for (StockTransactionDetail d : details) {
                                detailsDto.add(d.toDto());
                            }

                            invDocDto.setName(com.noreco1.fireflyv2.model.enums.DocumentType.MST.getDescription());
                            invDocDto.setDate(materialSalvageTicket.getVoucherDate());
                            invDocDto.setCode(materialSalvageTicket.getCode());
                            invDocDto.setPurpose(materialSalvageTicket.getPurpose());
                            invDocDto.setDetails(detailsDto);
                            invDocDto.setTransId(materialSalvageTicket.getTransaction().getId());

                            dto.setInventoryDocument(invDocDto);
                        }
                        break;
                    case "MCT":
                        StockRelease materialChargeTicket = stockReleaseRepo.findOneByTransactionId(mir.getInvDocTransactionId());
                        if (materialChargeTicket != null) {
                            InventoryDocumentDto invDocDto = new InventoryDocumentDto();
                            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(materialChargeTicket.getTransaction().getId());

                            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                            for (StockTransactionDetail d : details) {
                                detailsDto.add(d.toDto());
                            }

                            invDocDto.setName("Material Charge Ticket");
                            invDocDto.setDate(materialChargeTicket.getVoucherDate());
                            invDocDto.setCode(materialChargeTicket.getCode());
                            invDocDto.setPurpose(materialChargeTicket.getDescription());
                            invDocDto.setDetails(detailsDto);
                            invDocDto.setTransId(materialChargeTicket.getTransaction().getId());

                            dto.setInventoryDocument(invDocDto);
                        }
                        break;
                    case "STR":
                        StockReceive stockReceive = stockReceiveRepo.findOneByTransactionId(mir.getInvDocTransactionId());
                        if (stockReceive != null) {
                            InventoryDocumentDto invDocDto = new InventoryDocumentDto();
                            ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(stockReceive.getTransaction().getId());

                            ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                            for (StockTransactionDetail d : details) {
                                detailsDto.add(d.toDto());
                            }

                            invDocDto.setName("Stock Receive");
                            invDocDto.setDate(stockReceive.getVoucherDate());
                            invDocDto.setCode(stockReceive.getCode());
                            invDocDto.setPurpose(stockReceive.getDescription());
                            invDocDto.setDetails(detailsDto);
                            invDocDto.setTransId(stockReceive.getTransaction().getId());

                            dto.setInventoryDocument(invDocDto);
                        }
                        break;
                }
            }

            dto.setJournalEntries(ledgerDtoers.getVoucherLedgerLines(mir.getTransaction().getId()));
        }

        return dto;
    }

    @Override
    public List<MaterialIssueRegisterDetailDto> getDetails(Integer id) {
        return null;
    }

    @Override
    public List<MaterialIssueRegisterDto> getAllMaterialIssueRegister() {

        List<MaterialIssueRegister> regs = mirRepo.findAll();

        return this.makeMirListDto(regs);
    }

    @Override
    public List<MaterialIssueRegisterBIRDto> findForRegisterByDateRange(String from, String to) {
        List<Object[]> rows = mirRepo.findForRegisterByDateRange(from, to, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
        return this.makeCommonRegisterDetail(rows);
    }

    @Override
    public List<RegisterRecapDetail> findForRegisterRecapByDateRange(String from, String to) {
        List<Object[]> rows = mirRepo.findForRegisterRecapByDateRange(from, to, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());
        return this.makeRegisterRecapDetail(rows);
    }

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    @Override
    public Page<IomasMRHeader> findMRHeadersByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return mrHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasMRHeader> findMRHeadersByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return mrHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public List<IomasMRDetail> findMRDetailByMRHeaderId(Integer mrhId) {
        return mrDetailRepo.findAllByMrhId(mrhId);
    }

    @Override
    public IomasMRHeader findByMRHeaderId(Integer mrhId) {
        return mrHeaderRepo.findById(mrhId).orElse(null);
    }

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    @Override
    public List<MaterialIssueRegisterDto> findByDateRangeAndStatusId(String from, String to, Integer id) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<MaterialIssueRegister> vouchers = mirRepo.findByDocumentStatusIdAndVoucherDateBetween(id, fromDate, toDate);
            return this.makeMirListDto(vouchers);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MaterialIssueRegisterDto> findByDateRangeAndStatusIdAndDocType(String from, String to, Integer id, String invDocumentType) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            List<MaterialIssueRegister> vouchers = mirRepo.findByDocumentStatusIdAndInventoryDocTypeAndVoucherDateBetween(id, invDocumentType, fromDate, toDate);
            return this.makeMirListDto(vouchers);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MaterialIssueRegisterDto> findByDateRange(String from, String to) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<MaterialIssueRegister> vouchers = mirRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(ids));
            return this.makeMirListDto(vouchers);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MaterialIssueRegisterDto> findByDateRangeAndDocType(String from, String to, String invDocumentType) {
        try {
            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            Integer[] ids = {
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                    com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
            };

            List<MaterialIssueRegister> vouchers = mirRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInAndInventoryDocType(fromDate, toDate, Arrays.asList(ids), invDocumentType);
            return this.makeMirListDto(vouchers);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    @Override
    public List<IomasADHeader> findInventoryAdjustmentsByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasADHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasADHeader> findInventoryAdjustmentsByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasADHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasMCHeader> findMaterialCreditTicketsByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasMCHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasMCHeader> findMaterialCreditTicketsByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasMCHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasMSHeader> findMaterialSalvageTicketsByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasMSHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasMSHeader> findMaterialSalvageTicketsByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasMSHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasSAHeader> findSalvageInventoryAdjustmentByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasSAHeader> findSalvageInventoryAdjustmentByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasSMHeader> findSalvageMaterialRequisitionByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSMHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasSMHeader> findSalvageMaterialRequisitionByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSMHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasJMHeader> findJunkMaterialsTicketByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJMHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasJMHeader> findJunkMaterialsTicketByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJMHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasJAHeader> findJunkMaterialsAdjustmentByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasJAHeader> findJunkMaterialsAdjustmentByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasJRHeader> findJunkMaterialsReleasingByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJRHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasJRHeader> findJunkMaterialsReleasingByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasJRHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasHWIHeader> findHouseWiringMaterialsByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasHWIHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasHWIHeader> findHouseWiringMaterialsByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasHWIHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public List<IomasSOAHeader> findStatementOfAccountByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSOAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasSOAHeader> findStatementOfAccountByDateRange(String from, String to, Pageable paging) {

        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasSOAHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);

    }

    @Override
    public List<IomasLIHeader> findLostItemsByDateRange(String from, String to) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasLIHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end);
    }

    @Override
    public Page<IomasLIHeader> findLostItemsByDateRange(String from, String to, Pageable paging) {
        String format = "yyyy-MM-dd";
        Date start = DateHelper.strToDateOrToday(from, format);
        Date end = DateHelper.strToDateOrToday(to, format);
        return iomasLIHeaderRepo.findAllByDateBetweenOrderByDateAscNumberAsc(start, end, paging);
    }

    @Override
    public IomasADHeader findInventoryAdjustments(Integer id) {
        return iomasADHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasMCHeader findMaterialCreditTicket(Integer id) {
        return iomasMCHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasMSHeader findMaterialSalvageTicket(Integer id) {
        return iomasMSHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasSAHeader findSalvageInventoryAdjustment(Integer id) {
        return iomasSAHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasSMHeader findSalvageMaterialRequisition(Integer id) {
        return iomasSMHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasJMHeader findJunkMaterialsTicket(Integer id) {
        return iomasJMHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasJAHeader findJunkMaterialsAdjustment(Integer id) {
        return iomasJAHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasJRHeader findJunkMaterialsReleasing(Integer id) {
        return iomasJRHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasHWIHeader findHouseWiringMaterials(Integer id) {
        return iomasHWIHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasSOAHeader findStatementOfAccount(Integer id) {
        return iomasSOAHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public IomasLIHeader findLostItems(Integer id) {
        return iomasLIHeaderRepo.findById(id).orElse(null);
    }

    @Override
    public List<Map> findInventoryAdjustmentDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasADDetail> adDetails = iomasADDetailRepo.findAllByAdhId(id);

        if (!adDetails.isEmpty()) {
            for (IomasADDetail row : adDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAverageCost(), row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findMaterialCreditTicketDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasMCDetail> mcDetails = iomasMCDetailRepo.findAllByMchId(id);

        if (!mcDetails.isEmpty()) {
            for (IomasMCDetail row : mcDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAverageCost(), row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findMaterialSalvageTicketDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasMSDetail> msDetails = iomasMSDetailRepo.findAllByMshId(id);

        if (!msDetails.isEmpty()) {
            for (IomasMSDetail row : msDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }

        return details;
    }

    @Override
    public List<Map> findSalvageInventoryAdjustmentDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasSADetail> saDetails = iomasSADetailRepo.findAllBySahId(id);

        if (!saDetails.isEmpty()) {
            for (IomasSADetail row : saDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }

        return details;
    }

    @Override
    public List<Map> findSalvageMaterialRequisitionDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasSMDetail> smDetails = iomasSMDetailRepo.findAllBySmhId(id);

        if (!smDetails.isEmpty()) {
            for (IomasSMDetail row : smDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAveCost(), row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findJunkMaterialsTicketDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasJMDetail> iomasDetails = iomasJMDetailRepo.findAllByHId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasJMDetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }

        return details;
    }

    @Override
    public List<Map> findJunkMaterialsAdjustmentDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasJADetail> iomasDetails = iomasJADetailRepo.findAllByHId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasJADetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }

        return details;
    }

    @Override
    public List<Map> findJunkMaterialsReleasingDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasJRDetail> iomasDetails = iomasJRDetailRepo.findAllByHId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasJRDetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAverageCost(), row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findHouseWiringMaterialsDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasHWIDetail> iomasDetails = iomasHWIDetailRepo.findAllByMrhId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasHWIDetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAveCost(), row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findStatementOfAccountDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasSOADetail> iomasDetails = iomasSOADetailRepo.findAllByHId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasSOADetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), BigDecimal.ZERO, row.getTotal()));
            }
        }

        return details;
    }

    @Override
    public List<Map> findLostItemsDetails(Integer id) {
        List<Map> details = new ArrayList<>();

        List<IomasLIDetail> iomasDetails = iomasLIDetailRepo.findAllByHId(id);

        if (!iomasDetails.isEmpty()) {
            for (IomasLIDetail row : iomasDetails) {
                details.add(this.makeItemDetails(row.getId(), row.getMatId(), row.getQuantity(), row.getAveCost(), row.getTotal()));
            }
        }

        return details;
    }

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    @Override
    public Boolean inventoryDocumentHasVoucher(Integer documentId, String documentType) {

        Boolean hasVoucher = false;
        switch (documentType) {

            case "MRHeader":
                Mrct m = mrctRepo.findByDocId(documentId);
                if (m != null) {
                    hasVoucher = true;
                }
                break;
            case "ADHeader":

                InventoryAdjustment m1 = inventoryAdjustmentRepo.findByDocId(documentId);
                if (m1 != null) {
                    hasVoucher = true;
                }

                break;

            case "MCHeader":

                MaterialCreditTicket m2 = materialCreditTicketRepo.findByDocumentStatusId(documentId);
                if (m2 != null) {
                    hasVoucher = true;
                }

                break;

            case "MSHeader":

//                MaterialSalvageTicket m3 = materialSalvageTicketRepo.findByDocId(documentId);
//                if (m3 != null) {
//                    hasVoucher = true;
//                }

                break;

            case "SAHeader":

                SalvageInventoryAdjustment m4 = salvageInventoryAdjustmentRepo.findByDocId(documentId);
                if (m4 != null) {
                    hasVoucher = true;
                }

                break;

            case "SMHeader":

                SalvageMaterialRequisition m5 = salvageMaterialRequisitionRepo.findByDocId(documentId);
                if (m5 != null) {
                    hasVoucher = true;
                }

                break;

            case "JMHeader":

                JunkMaterialsTicket m6 = junkMaterialsTicketRepo.findByDocId(documentId);
                if (m6 != null) {
                    hasVoucher = true;
                }

                break;

            case "JAHeader":

                JunkMaterialsAdjustment m7 = junkMaterialsAdjustmentRepo.findByDocId(documentId);
                if (m7 != null) {
                    hasVoucher = true;
                }

                break;

            case "JRHeader":

                JunkMaterialsReleasingReceipt m8 = junkMaterialsReleasingReceiptRepo.findByDocId(documentId);
                if (m8 != null) {
                    hasVoucher = true;
                }

                break;

            case "HWIHeader":

                HouseWiringMaterialsIssuanceTicket m9 = houseWiringMaterialsIssuanceTicketRepo.findByDocId(documentId);
                if (m9 != null) {
                    hasVoucher = true;
                }

                break;

            case "SOAHeader":

                StatementOfAccount m10 = statementOfAccountRepo.findByDocId(documentId);
                if (m10 != null) {
                    hasVoucher = true;
                }

                break;

            case "LIHeader":

//                LostItem m11 = lostItemRepo.findByDocId(documentId);
//                this.findInventoryAdjustments(1);
//                if (m11 != null) {
//                    hasVoucher = true;
//                }

                break;
        }

        return hasVoucher;
    }

    @Override
    public Page<InventoryDocumentDto> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<InventoryDocumentDto> findByInvDocTypeQuery(String type, String query, Pageable pageable) {
        switch (type) {
            case "MCRT":
                Page<MaterialCreditTicket> materialCreditTickets;
                if (query != null) {
                    materialCreditTickets = materialCreditTicketRepo.findByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseAndTransactionIdNotInOrderByCodeAsc("%" + query.toUpperCase() + "%", pageable);
                } else {
                    materialCreditTickets = materialCreditTicketRepo.findByTransactionIdNotIn(pageable);
                }
                return materialCreditTickets.map(entity -> {
                    BigDecimal grandTotal = BigDecimal.ZERO;
                    InventoryDocumentDto dto = new InventoryDocumentDto();
                    ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());
                    ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                    for (StockTransactionDetail d : details) {
                        detailsDto.add(d.toDto());
                        grandTotal = grandTotal.add(d.getTotalCost());
                    }
                    dto.setDate(entity.getVoucherDate());
                    dto.setCode(entity.getCode());
                    dto.setPurpose(entity.getRemarks());
                    dto.setDetails(detailsDto);
                    dto.setTransId(entity.getTransaction().getId());
                    dto.setGrandTotal(grandTotal);
                    return dto;
                });
            case "SA":
                Page<StockAdjustment> stockAdjustments;
                if (query != null) {
                    stockAdjustments = stockAdjustmentRepo.findByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseOrderByCodeAsc(query, pageable);
                } else {
                    stockAdjustments = stockAdjustmentRepo.findByTransactionIdNotIn(pageable);
                }
                return stockAdjustments.map(entity -> {
                    BigDecimal grandTotal = BigDecimal.ZERO;
                    InventoryDocumentDto dto = new InventoryDocumentDto();
                    ArrayList<ItemTransactionDetail> details = itemTransactionDetailRepo.findByTransactionId(entity.getTransaction().getId());
                    ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                    for (ItemTransactionDetail d : details) {
                        d.setQuantity(d.getAdjustment());
                        d.setTotalCost(d.getAdjustment().multiply(d.getUnitCost()));
                        detailsDto.add(d.toDto());
                        grandTotal = grandTotal.add(d.getTotalCost());
                    }
                    dto.setDate(entity.getVoucherDate());
                    dto.setCode(entity.getCode());
                    dto.setPurpose(entity.getRemarks());
                    dto.setDetails(detailsDto);
                    dto.setTransId(entity.getTransaction().getId());
                    dto.setGrandTotal(grandTotal);
                    return dto;
                });
            case "MST":
                Page<MaterialSalvageTicket> materialSalvageTickets;
                if (query != null) {
                    materialSalvageTickets = materialSalvageTicketRepo.findByCodeContainingIgnoreCaseOrPurposeContainingIgnoreCaseOrderByCodeAsc(query, pageable);
                } else {
                    materialSalvageTickets = materialSalvageTicketRepo.findByTransactionIdNotIn(pageable);
                }
                return materialSalvageTickets.map(entity -> {
                        BigDecimal grandTotal = BigDecimal.ZERO;
                        InventoryDocumentDto dto = new InventoryDocumentDto();
                        ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

                        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                        for (StockTransactionDetail d : details) {
                            detailsDto.add(d.toDto());
                            grandTotal = grandTotal.add(d.getTotalCost());
                        }

                        dto.setDate(entity.getVoucherDate());
                        dto.setCode(entity.getCode());
                        dto.setPurpose(entity.getPurpose());
                        dto.setDetails(detailsDto);
                        dto.setTransId(entity.getTransaction().getId());
                        dto.setGrandTotal(grandTotal);

                        return dto;
                    });
            case "MCT":
                Page<StockRelease> stockReleases;
                if (query != null) {
                    stockReleases = stockReleaseRepo.findByCodeContainingIgnoreCaseOrDescriptionContainingAndTypeIgnoreCaseOrderByCodeAsc(query, StockReleaseType.MCT.getId(), pageable);
                } else {
                    stockReleases = stockReleaseRepo.findByTransactionIdNotIn(StockReleaseType.MCT.getId(), pageable);
                }
                return stockReleases.map(entity -> {
                        BigDecimal grandTotal = BigDecimal.ZERO;
                        InventoryDocumentDto dto = new InventoryDocumentDto();
                        ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

                        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                        for (StockTransactionDetail d : details) {
                            detailsDto.add(d.toDto());
                            grandTotal = grandTotal.add(d.getTotalCost());
                        }

                        dto.setDate(entity.getVoucherDate());
                        dto.setCode(entity.getCode());
                        dto.setPurpose(entity.getDescription());
                        dto.setDetails(detailsDto);
                        dto.setTransId(entity.getTransaction().getId());
                        dto.setGrandTotal(grandTotal);

                        return dto;
                    });
            case "STR":
                Page<StockReceive> stockReceives;
                if (query != null) {
                    stockReceives = stockReceiveRepo.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCodeAsc(query, pageable);
                } else {
                    stockReceives = stockReceiveRepo.findByTransactionIdNotIn(pageable);
                }
                return stockReceives.map(entity -> {
                        BigDecimal grandTotal = BigDecimal.ZERO;
                        InventoryDocumentDto dto = new InventoryDocumentDto();
                        ArrayList<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(entity.getTransaction().getId());

                        ArrayList<ItemTransactionDetailDto> detailsDto = new ArrayList<>();
                        for (StockTransactionDetail d : details) {
                            detailsDto.add(d.toDto());
                            grandTotal = grandTotal.add(d.getTotalCost());
                        }

                        dto.setDate(entity.getVoucherDate());
                        dto.setCode(entity.getCode());
                        dto.setPurpose(entity.getDescription());
                        dto.setDetails(detailsDto);
                        dto.setTransId(entity.getTransaction().getId());
                        dto.setGrandTotal(grandTotal);

                        return dto;
                    });
        }
        return null;
    }

    @Override
    public Page<InventoryDocumentDto> findByQuery(String query, Pageable pageable) {
        return null;
    }

    @Override
    public PostResponse updateEntries(MaterialIssueRegister mir, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            Boolean insertMode = Checker.isValidId(mir.getId());

            if(insertMode){

                MaterialIssueRegister existingMir = this.mirRepo.findById(mir.getId()).orElse(null);

                if(existingMir.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                    existingMir.setAmount(mir.getAmount());

                    this.model = mirRepo.save(existingMir);

                    if (this.model != null) {

                        ledgerFacade.postGeneralLedger(this.model.getTransaction(), mir.getGeneralLedgerLines(), mir.getSubLedgerLines(), this.model.getVoucherDate());

                        response.setSuccessMessage("Entries successfully saved!");
                        response.setSuccess(true);

                    }

                } else {

                    response.setFailureMessage("Invalid user or update is restricted!!");
                    response.setSuccess(false);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    private List<MaterialIssueRegisterDto> makeMirListDto(List<MaterialIssueRegister> vouchers) {

        List<MaterialIssueRegisterDto> ret = new ArrayList<>();

        try {

            if (!vouchers.isEmpty()) {

                for (MaterialIssueRegister voucher : vouchers) {
                    MaterialIssueRegisterDto dto = new MaterialIssueRegisterDto();

                    String code = voucher.getCode();
                    String particulars = voucher.getParticulars();
                    String userStr = voucher.getCreatedBy().getFullName();
                    String checkerStr = voucher.getChecker().getFullName();
                    String approvingOfficerStr = voucher.getApprovingOfficer().getFullName();
                    String status = voucher.getDocumentStatus().getStatus();
                    java.sql.Date voucherDate = (java.sql.Date) voucher.getVoucherDate();
                    Integer id = voucher.getId();

                    dto.setLocalCode(code);
                    dto.setParticulars(particulars);
                    dto.setVoucherDate(voucherDate);

                    User user = new User();
                    User checker = new User();
                    User approvingOfficer = new User();

                    user.setFullName(userStr);
                    checker.setFullName(checkerStr);
                    approvingOfficer.setFullName(approvingOfficerStr);

                    dto.setCreatedBy(user);
                    dto.setChecker(checker);
                    dto.setApprovingOfficer(approvingOfficer);
                    dto.setStatus(status);
                    dto.setId(id);
                    dto.setInventoryDocType(voucher.getInventoryDocType());
//                    dto.setDocId(voucher.getDocId());

                    if(voucher.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId())
                            && voucher.getApprovingOfficer().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){
                        dto.setEnableCheckBox(Boolean.TRUE);
                        dto.setSelected(Boolean.TRUE);
                    } else {
                        dto.setEnableCheckBox(Boolean.FALSE);
                        dto.setSelected(Boolean.FALSE);
                    }

                    dto.setDocumentCode(com.noreco1.fireflyv2.model.enums.DocumentType.MR.getCode());

                    ret.add(dto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Transactional
    public boolean detailsSaved(MaterialIssueRegister mir, MaterialIssueRegister materialIssueRegister) {
        try {
            ArrayList<MaterialIssueRegisterDetail> mirDetails = new ArrayList<>();

//            switch (materialIssueRegister.getInventoryDocType()) {
//
//                case "MRHeader":
//
//                    for(IomasMRDetail mrDetail:mir.getMrDetails()) {
//                        Item item = itemRepo.findByMatId(mrDetail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, mrDetail.getTotal(), mrDetail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//
//                    break;
//
//                case "ADHeader":
//
//                    for(IomasADDetail detail:mir.getAdDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "MCHeader":
//
//                    for(IomasMCDetail detail:mir.getMcDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, detail.getTotal(), detail.getQuantity(), detail.getUnitCost()));
//                        }
//                    }
//                    break;
//
//                case "MSHeader":
//
//                    for(IomasMSDetail detail:mir.getMsDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, BigDecimal.ZERO, detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "SAHeader":
//
//                    for(IomasSADetail detail:mir.getSaDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, BigDecimal.ZERO, detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "SMHeader":
//
//                    for(IomasSMDetail detail:mir.getSmDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item, detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "JMHeader":
//
//                    for(IomasJMDetail detail:mir.getJmDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  BigDecimal.ZERO, detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "JAHeader":
//
//                    for(IomasJADetail detail:mir.getJaDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  BigDecimal.ZERO, detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "JRHeader":
//
//                    for(IomasJRDetail detail:mir.getJrDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "HWIHeader":
//
//                    for(IomasHWIDetail detail:mir.getHwiDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "SOAHeader":
//
//                    for(IomasSOADetail detail:mir.getSoaDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//
//                case "LIHeader":
//
//                    for(IomasLIDetail detail:mir.getLiDetails()) {
//                        Item item = itemRepo.findByMatId(detail.getMatId());
//                        if(item != null) {
//                            mirDetails.add(this.makeMIRDetails(item,  detail.getTotal(), detail.getQuantity(), BigDecimal.ZERO));
//                        }
//                    }
//                    break;
//            }

            List<MaterialIssueRegisterDetail> list = mirDetailRepo.saveAll(mirDetails);
            return list.size() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private List<MaterialIssueRegisterBIRDto> makeCommonRegisterDetail(List<Object[]> rows) {
        List<MaterialIssueRegisterBIRDto> data = new ArrayList<>();

        for (Object[] row : rows) {
            Integer mirId = Integer.parseInt(row[0].toString());
            String mirNumber = String.valueOf(row[1]);
            Date voucherDate = (Date) row[2];
            Integer transId = Integer.parseInt(row[3].toString());
            String segmentAccountCode = String.valueOf(row[4]);
            String title = String.valueOf(row[10]);

            List<MaterialIssueRegisterDetail> details = mirDetailRepo.findByMaterialIssueRegisterId(mirId);
            int counter = 1;
            if (!Checker.collectionIsEmpty(details)) {
                for (MaterialIssueRegisterDetail detail : details) {
                    String description = detail.getDescription();
                    Integer quantity = detail.getQuantity();
                    BigDecimal unitPrice = detail.getUnitPrice();
                    BigDecimal amount = detail.getAmount();

                    MaterialIssueRegisterBIRDto mirDetail = new MaterialIssueRegisterBIRDto();
                    if (counter == 1) {
                        mirDetail.setLocalCode(mirNumber);
                        mirDetail.setDate(voucherDate);
                    } else {
                        mirDetail.setLocalCode(null);
                        mirDetail.setDate(null);
                    }

                    mirDetail.setTitle(title);
                    mirDetail.setAccount(segmentAccountCode);
                    mirDetail.setDescription(description);
//                    mirDetail.setUnit(unit);
                    mirDetail.setQuantity(quantity);
                    mirDetail.setUnitPrice(unitPrice);
                    mirDetail.setAmount(amount);

                    data.add(mirDetail);

                    counter++;
                }
            }
        }

        return data;
    }

    private List<RegisterRecapDetail> makeRegisterRecapDetail(List<Object[]> rows) {
        List<RegisterRecapDetail> details = new ArrayList<>();

        if (!Checker.collectionIsEmpty(rows)) {
            for (Object[] row : rows) {
                BigDecimal glDebit = (BigDecimal) row[0];
                BigDecimal glCredit = (BigDecimal) row[1];
                Integer glId = (Integer) row[2];
                BigDecimal slDebit = (BigDecimal) row[3];
                BigDecimal slCredit = (BigDecimal) row[4];
                String accountNo = String.valueOf(row[5]);
                String glAccountCode = (String) row[6];
                String glAccountTitle = (String) row[7];
                String slEntityName = (String) row[8];

                RegisterRecapDetail detail = new RegisterRecapDetail();
                detail.setSlAccountTitle(slEntityName);
                detail.setSlAccountCode(accountNo == "null" ? "" : accountNo);
                detail.setSlDebit(slDebit);
                detail.setSlCredit(slCredit);

                detail.setGlAccountTitle(glAccountTitle);
                detail.setGlAccountCode(glAccountCode);
                detail.setGlDebit(glDebit);
                detail.setGlCredit(glCredit);

                details.add(detail);
            }
        }

        return details;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        MaterialIssueRegister mir = (MaterialIssueRegister) v;
        return this.processCreate(mir, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {

        MaterialIssueRegister mir = (MaterialIssueRegister) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        MaterialIssueRegisterValidator validator = new MaterialIssueRegisterValidator();
        validator.setLegderFacade(this.ledgerFacade);
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.validate(mir, bindingResult);

        try {

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                response = messageFormatter.getResponse();
            } else {
                User createdBy = authenticationFacade.getLoggedIn();
                MaterialIssueRegister existingMir = null;

                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(mir.getVoucherDate()));
                User approvingOfficer = userRepo.findOneByAccountNo(mir.getApprovingOfficer().getAccountNo());
                User checker = userRepo.findOneByAccountNo(mir.getChecker().getAccountNo());
                User recApp = userRepo.findOneByAccountNo(mir.getRecommendingOfficer().getAccountNo());

                Boolean insertMode = mir.getId() == null;
                if (insertMode) { // insert mode

                    Object latestMirCode = mirRepo.findLatestMaterialIssueRegisterCodeByYear(voucherYear);
                    mir.setCode(generatorFacade.voucherCodeNoOffice("MIV", (latestMirCode == null ? "" : String.valueOf(latestMirCode)), mir.getVoucherDate(), GlobalConstant.COUNTER_PAD_4));

                    com.noreco1.fireflyv2.model.DocumentStatus documentStatus = new com.noreco1.fireflyv2.model.DocumentStatus();
                    documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    Workflow wf = new Workflow();
                    wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MIV.getId());

                    mir.setDocumentStatus(documentStatus);
                    mir.setCreatedBy(createdBy);
                    mir.setTransaction(generatorFacade.transaction());
                    mir.setWorkflow(wf);

                    existingMir = mir;
                } else {
                    existingMir = mirRepo.findById(mir.getId()).orElse(null);

                    List<Integer> statusAllowed = new ArrayList();
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                    statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                    if (statusAllowed.indexOf(existingMir.getDocumentStatus().getId()) < 0) {

                        ArrayList<String> messages = new ArrayList();
                        messages.add("Action is not allowed");

                        response.setNotAuthorized(true);
                        response.setMessages(messages);
                        response.setSuccess(false);

                        return response;
                    }
                }

                // use for document logging
                Map oldMIVMap = this.forLogMapMain(existingMir);

                // editable fields
                existingMir.setParticulars(mir.getParticulars());
                existingMir.setVoucherDate(mir.getVoucherDate());
                existingMir.setApprovingOfficer(approvingOfficer);
                existingMir.setRecommendingOfficer(recApp);
                existingMir.setChecker(checker);
                existingMir.setYear(voucherYear);
                existingMir.setAmount(mir.getAmount());

                this.model = mirRepo.save(existingMir);

                if (this.model != null) {
                    // start: update default signatories
                    signatoryFacade.miv(this.model);
                    // end: update default signatories

                    ledgerFacade.postGeneralLedger(this.model.getTransaction(), mir.getGeneralLedgerLines(), mir.getSubLedgerLines(), this.model.getVoucherDate());

                    if (insertMode) { // log action only when adding document
                        documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                        oldMIVMap = null;
                    }

                    // generic document logging here
                    // old value only
                    DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldMIVMap, null);

                    response.setLogId(log != null ? log.getId() : 0);
                    response.setModelId(this.model.getId());

                    response.setModelId(this.model.getId());
                    response.setSuccessMessage("MIV successfully saved!");
                    response.setSuccess(true);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {

        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            MaterialIssueRegister miv = mirRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (miv != null) {
                Map map = forLogMapMain(miv);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }

    }

    @Override
    public Map defaultSignatories() {
        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.MR);
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        MaterialIssueRegister mir = mirRepo.findById(vid).orElse(null);

        if (mir != null) {
            params.put("VOUCHER_NO", mir.getCode());
            params.put("V_DATE", mir.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(mir.getAmount()));
            params.put("TOTAL", mir.getAmount());
            params.put("EXPLANATION", mir.getParticulars());
            params.put("REMARKS", "");
            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.MR, mir);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        MaterialIssueRegister voucher = mirRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource,
                                      HttpServletRequest request, List<Map> fileToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public List<com.noreco1.fireflyv2.model.DocumentStatus> getDocumentsStatuses() {
        return documentDtoer.getDocumentStatuses(com.noreco1.fireflyv2.model.enums.Workflow.MIV.getId());
    }

    private Map forLogMapMain(MaterialIssueRegister miv) {
        return documentLoggerFacade.makeLog(miv);
    }

    private Map makeItemDetails(Integer detailId, Integer matId, Integer qty, BigDecimal aveCost, BigDecimal total) {
        Map map = new HashMap();

        Item item = itemRepo.findByMatId(matId);
        if (item != null) {
            map.put("item", item);
        }

        map.put("id", detailId);
        map.put("matId", matId);
        map.put("quantity", qty);
        map.put("averageCost", aveCost);
        map.put("total", total);

        return map;
    }

    private MaterialIssueRegisterDetail makeMIRDetails(Item item, BigDecimal total, Integer qty, BigDecimal unitPrice) {

        MaterialIssueRegisterDetail mirDetail = new MaterialIssueRegisterDetail();

        mirDetail.setDescription(item.getDescription());
        mirDetail.setAmount(total);
        mirDetail.setQuantity(qty);
        mirDetail.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);
        mirDetail.setMaterialIssueRegister(this.model);
        mirDetail.setItem(item);
        mirDetail.setUnitMeasure(item.getUnit());

        return mirDetail;
    }
}
