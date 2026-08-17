package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.SubSupplier;
import com.noreco1.fireflyv2.model.Supplier;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.SubSupplierRepo;
import com.noreco1.fireflyv2.repo.SupplierRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.SubSupplierService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class SubSupplierServiceImpl implements SubSupplierService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private SubSupplierRepo subSupplierRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Override
    public SubSupplier findById(Integer id) {
        return subSupplierRepo.findById(id).orElse(null);
    }

    @Override
    public List<SubSupplier> findAllBySupplier(Integer suppId) {
        return subSupplierRepo.findAllBySupplierIdOrderByParticipantName(suppId);
    }

    @Override
    public Page<SubSupplier> findAllForListing(String query, Integer suppId, Pageable pageable) {
        if(Checker.isStringNullOrEmpty(query)){

            if(Checker.isValidId(suppId)){
                return subSupplierRepo.findAllBySupplierIdOrderByParticipantName(suppId, pageable);
            } else {
                return subSupplierRepo.findAll(pageable);
            }

        } else {
            if(Checker.isValidId(suppId)){
                return subSupplierRepo.findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrParticipantNameContainingIgnoreCaseOrTradeNameContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseAndSupplierIdOrderByParticipantName(query, query, query, query, query,suppId, pageable);
            } else {
                return subSupplierRepo.findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrParticipantNameContainingIgnoreCaseOrTradeNameContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseOrderByParticipantName(query, query, query, query, query, pageable);
            }
        }
    }

    @Override
    public PostResponse processUpload(MultipartFile excelFile, Integer supplierId) {
        PostResponse response = new PostResponse();
        XSSFWorkbook workbook = null;

        try {

            InputStream inputStream =  new BufferedInputStream(excelFile.getInputStream());
            workbook = new XSSFWorkbook(inputStream);
            XSSFSheet worksheet = workbook.getSheetAt(0);

            Supplier supplier = supplierRepo.findById(supplierId).orElse(null);

            if(supplier != null){
                for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {

                    XSSFRow row = worksheet.getRow(i);
                    if(row != null) {

                        XSSFCell stlIdCell = row.getCell(3);
                        XSSFCell billingIdCell = row.getCell(4);
                        XSSFCell participantNameCell = row.getCell(2);
                        XSSFCell tradeNameCell = row.getCell(6);
                        XSSFCell tinCell= row.getCell(8);
                        XSSFCell addressCell = row.getCell(9);

                        if(stlIdCell == null){
                            continue;
                        }

                        boolean strikeOutStatus= stlIdCell.getCellStyle().getFont().getStrikeout();

                        if(!strikeOutStatus){

                        stlIdCell.setCellType(CellType.STRING);
                        billingIdCell.setCellType(CellType.STRING);
                        participantNameCell.setCellType(CellType.STRING);
                        tradeNameCell.setCellType(CellType.STRING);
                        tinCell.setCellType(CellType.STRING);
                        addressCell.setCellType(CellType.STRING);

//                        String stlIdCellString= stlIdCell.getStringCellValue();
//                        String billingIdCellString= billingIdCell.getStringCellValue();
//                        String participantNameCellString= participantNameCell.getStringCellValue();
//                        String tradeNameCellString= tradeNameCell.getStringCellValue();
//                        String tinCellString= tinCell.getStringCellValue();
//                        String addressCellString= addressCell.getStringCellValue();

                            SubSupplier subSupplier = subSupplierRepo.findBySupplierIdAndStlIdEquals(supplierId, stlIdCell.getStringCellValue());

                            if (subSupplier == null) {
                                subSupplier = new SubSupplier();
                                subSupplier.setSupplier(supplier);
                            }

                            User createdBy = authenticationFacade.getLoggedIn();
                            subSupplier.setStlId(stlIdCell.getStringCellValue());
                            subSupplier.setBillingId(billingIdCell.getStringCellValue());
                            subSupplier.setParticipantName(participantNameCell.getStringCellValue());
                            subSupplier.setTradeName(tradeNameCell.getStringCellValue());
                            subSupplier.setTin(tinCell.getStringCellValue());
                            subSupplier.setAddress(addressCell.getStringCellValue());
                            subSupplier.setCreatedBy(createdBy);
                            subSupplierRepo.save(subSupplier);

                        }
                    } else {
                        continue;
                    }
                }
                response.setSuccessMessage("Sub-suppliers successfully uploaded!");
                response.setSuccess(true);
            } else {
                response.setSuccessMessage("Supplier not found!");
                response.setSuccess(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return response;
    }

}
