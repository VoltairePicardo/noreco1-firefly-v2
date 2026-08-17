package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.MaterialIssueRegister;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.MaterialIssueRegisterDetailDto;
import com.noreco1.fireflyv2.controller.response.MaterialIssueRegisterDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.reports.MaterialIssueRegisterBIRDto;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;
import com.noreco1.fireflyv2.mysql_model.*;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface MaterialIssueRegisterService extends VoucherService {

    @Transactional
    public List<MaterialIssueRegisterDto> findAll();

    @Transactional(readOnly = true)
    public MaterialIssueRegisterDto findById(Integer id);

    @Transactional
    public  List<MaterialIssueRegisterDetailDto> getDetails(Integer id);

    @Transactional
    public  List<MaterialIssueRegisterDto> getAllMaterialIssueRegister();
    public List<MaterialIssueRegisterBIRDto> findForRegisterByDateRange(String from, String to);
    public List<RegisterRecapDetail> findForRegisterRecapByDateRange(String from, String to);

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    public List<IomasMRHeader> findMRHeadersByDateRange(String from, String to);
    Page<IomasMRHeader> findMRHeadersByDateRange(String from, String to, Pageable paging);

    public List<IomasMRDetail> findMRDetailByMRHeaderId(Integer mrhId);
    public IomasMRHeader findByMRHeaderId(Integer mrhId);

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    @Transactional(readOnly = true)
    List<MaterialIssueRegisterDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<MaterialIssueRegisterDto> findByDateRangeAndStatusIdAndDocType(String from, String to, Integer id, String invDocumentType);

    @Transactional(readOnly = true)
    List<MaterialIssueRegisterDto> findByDateRange(String from, String to);

    @Transactional(readOnly = true)
    List<MaterialIssueRegisterDto> findByDateRangeAndDocType(String from, String to, String invDocumentType);

    // Start: Multiple line comment for removing IOMAS related functions/methods

    /*

    // from SQL server
    List<IomasADHeader> findInventoryAdjustmentsByDateRange(String from, String to);
    Page<IomasADHeader> findInventoryAdjustmentsByDateRange(String from, String to, Pageable paging);

    List<IomasMCHeader> findMaterialCreditTicketsByDateRange(String from, String to);
    Page<IomasMCHeader> findMaterialCreditTicketsByDateRange(String from, String to, Pageable paging);

    List<IomasMSHeader> findMaterialSalvageTicketsByDateRange(String from, String to);
    Page<IomasMSHeader> findMaterialSalvageTicketsByDateRange(String from, String to, Pageable paging);

    List<IomasSAHeader> findSalvageInventoryAdjustmentByDateRange(String from, String to);
    Page<IomasSAHeader> findSalvageInventoryAdjustmentByDateRange(String from, String to, Pageable paging);

    List<IomasSMHeader> findSalvageMaterialRequisitionByDateRange(String from, String to);
    Page<IomasSMHeader> findSalvageMaterialRequisitionByDateRange(String from, String to, Pageable paging);

    List<IomasJMHeader> findJunkMaterialsTicketByDateRange(String from, String to);
    Page<IomasJMHeader> findJunkMaterialsTicketByDateRange(String from, String to, Pageable paging);

    List<IomasJAHeader> findJunkMaterialsAdjustmentByDateRange(String from, String to);
    Page<IomasJAHeader> findJunkMaterialsAdjustmentByDateRange(String from, String to, Pageable paging);

    List<IomasJRHeader> findJunkMaterialsReleasingByDateRange(String from, String to);
    Page<IomasJRHeader> findJunkMaterialsReleasingByDateRange(String from, String to, Pageable paging);

    List<IomasHWIHeader> findHouseWiringMaterialsByDateRange(String from, String to);
    Page<IomasHWIHeader> findHouseWiringMaterialsByDateRange(String from, String to, Pageable paging);

    List<IomasSOAHeader> findStatementOfAccountByDateRange(String from, String to);
    Page<IomasSOAHeader> findStatementOfAccountByDateRange(String from, String to, Pageable paging);

    List<IomasLIHeader> findLostItemsByDateRange(String from, String to);
    Page<IomasLIHeader> findLostItemsByDateRange(String from, String to, Pageable paging);

    IomasADHeader findInventoryAdjustments(Integer id);
    IomasMCHeader findMaterialCreditTicket(Integer id);
    IomasMSHeader findMaterialSalvageTicket(Integer id);
    IomasSAHeader findSalvageInventoryAdjustment(Integer id);
    IomasSMHeader findSalvageMaterialRequisition(Integer id);
    IomasJMHeader findJunkMaterialsTicket(Integer id);
    IomasJAHeader findJunkMaterialsAdjustment(Integer id);
    IomasJRHeader findJunkMaterialsReleasing(Integer id);
    IomasHWIHeader findHouseWiringMaterials(Integer id);
    IomasSOAHeader findStatementOfAccount(Integer id);
    IomasLIHeader findLostItems(Integer id);

    List<Map> findInventoryAdjustmentDetails(Integer id);
    List<Map> findMaterialCreditTicketDetails(Integer id);
    List<Map> findMaterialSalvageTicketDetails(Integer id);
    List<Map> findSalvageInventoryAdjustmentDetails(Integer id);
    List<Map> findSalvageMaterialRequisitionDetails(Integer id);
    List<Map> findJunkMaterialsTicketDetails(Integer id);
    List<Map> findJunkMaterialsAdjustmentDetails(Integer id);
    List<Map> findJunkMaterialsReleasingDetails(Integer id);
    List<Map> findHouseWiringMaterialsDetails(Integer id);
    List<Map> findStatementOfAccountDetails(Integer id);
    List<Map> findLostItemsDetails(Integer id);

    */

    // End: Multiple line comment for removing IOMAS related functions/methods

    Boolean inventoryDocumentHasVoucher(Integer documentId, String documentType);

    Page<InventoryDocumentDto> findAll(Pageable pageable);

    Page<InventoryDocumentDto> findByInvDocTypeQuery(String type, String query, Pageable pageable);

    Page<InventoryDocumentDto> findByQuery(String query, Pageable pageable);

    @Transactional
    PostResponse updateEntries(MaterialIssueRegister materialIssueRegister, BindingResult bindingResult, MessageSource messageSource);
}
