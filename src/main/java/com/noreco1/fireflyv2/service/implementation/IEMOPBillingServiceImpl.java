package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.model.IEMOPBilling;
import com.noreco1.fireflyv2.model.SubSupplier;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.IEMOPBillingRepo;
import com.noreco1.fireflyv2.repo.SubSupplierRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.IEMOPBillingService;
import org.apache.commons.io.FilenameUtils;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class IEMOPBillingServiceImpl implements IEMOPBillingService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private IEMOPBillingRepo iemopBillingRepo;

    @Autowired
    private SubSupplierRepo subSupplierRepo;

    @Override
    public IEMOPBilling findById(Integer id) {
        return iemopBillingRepo.findById(id).orElse(null);
    }

    @Override
    public Page<IEMOPBilling> findAllForListing(String query, Pageable pageable) {
        if(Checker.isStringNullOrEmpty(query)){
            return iemopBillingRepo.findAll(pageable);

        } else {
            return iemopBillingRepo.findByStlIdContainingIgnoreCaseOrBillingIdContainingIgnoreCaseOrderByStlIdAscBillingIdAsc(query, query, pageable);

        }
    }

    @Override
    public Page<IEMOPBilling> findAllForCVPaged(String query, Pageable pageable) {
        if(Checker.isStringNullOrEmpty(query)){
            return iemopBillingRepo.findIEMOPBillingNotInCV(pageable);

        } else {
            return iemopBillingRepo.findIEMOPBillingNotInCVByQuery(query, pageable);

        }
    }

    @Override
    public List<IEMOPBilling> findAllForCV(String query) {
        if(Checker.isStringNullOrEmpty(query)){
            return iemopBillingRepo.findIEMOPBillingNotInCV();

        } else {
            return iemopBillingRepo.findIEMOPBillingNotInCVByQuery(query);

        }
    }

    @Override
    public PostResponse processUpload(MultipartFile excelFile, String date, String refNumber) {
        PostResponse response = new PostResponse();
        XSSFWorkbook workbook = null;

        String extension = FilenameUtils.getExtension(excelFile.getOriginalFilename());

        try {

            if(extension.contains("csv")){
                workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("sheet1");
                String currentLine=null;
                int RowNum=0;
                BufferedReader br = new BufferedReader(new InputStreamReader(excelFile.getInputStream()));
                while ((currentLine = br.readLine()) != null) {
                    System.out.println(currentLine);
                    String str[] = currentLine.split(",");
                    RowNum++;
                    XSSFRow currentRow=sheet.createRow(RowNum);
                    for(int i=0;i<str.length;i++){
                        currentRow.createCell(i).setCellValue(str[i]);
                    }
                }

            } else {
                InputStream inputStream =  new BufferedInputStream(excelFile.getInputStream());
                workbook = new XSSFWorkbook(inputStream);
            }

            if(workbook != null){
                XSSFSheet worksheet = workbook.getSheetAt(0);

                int startingIndex = extension.contains("csv") ? 3 : 2;

                for (int i = startingIndex; i < worksheet.getPhysicalNumberOfRows(); i++) {

                    XSSFRow row = worksheet.getRow(i);
                    if(row != null) {

                        XSSFCell stlIdCell = row.getCell(0);
                        XSSFCell billingIdCell = row.getCell(1);
                        XSSFCell ftCell = row.getCell(2);
                        XSSFCell wht = row.getCell(3);
                        XSSFCell iht = row.getCell(4);
                        XSSFCell nvt= row.getCell(5);
                        XSSFCell zrt = row.getCell(6);
                        XSSFCell vs = row.getCell(7);
                        XSSFCell zrs = row.getCell(8);
                        XSSFCell zres = row.getCell(9);
                        XSSFCell vos = row.getCell(10);
                        XSSFCell vp = row.getCell(11);
                        XSSFCell zrp = row.getCell(12);
                        XSSFCell zrep = row.getCell(13);
                        XSSFCell vop = row.getCell(14);
                        XSSFCell ewts = row.getCell(15);
                        XSSFCell ewtp = row.getCell(16);
                        XSSFCell remarks = row.getCell(17);

                        if(stlIdCell == null){
                            continue;
                        }

                        stlIdCell.setCellType(CellType.STRING);
                        billingIdCell.setCellType(CellType.STRING);
                        ftCell.setCellType(CellType.STRING);
                        wht.setCellType(CellType.STRING);
                        iht.setCellType(CellType.STRING);
                        nvt.setCellType(CellType.STRING);
                        zrt.setCellType(CellType.STRING);
                        remarks.setCellType(CellType.STRING);

                        boolean strikeOutStatus= stlIdCell.getCellStyle().getFont().getStrikeout();

                        if(!strikeOutStatus){
                            IEMOPBilling iemopBilling = new IEMOPBilling();

                            SubSupplier subSupplier = subSupplierRepo.findByStlId(stlIdCell.getStringCellValue());
                            User createdBy = authenticationFacade.getLoggedIn();

                            if(subSupplier != null) {
                                iemopBilling.setSubSupplier(subSupplier);
                            }

                            iemopBilling.setStlId(stlIdCell.getStringCellValue());
                            iemopBilling.setBillingId(billingIdCell.getStringCellValue());
                            iemopBilling.setFacilityType(ftCell.getStringCellValue());
                            iemopBilling.setWhtAgent(wht.getStringCellValue().equalsIgnoreCase("Y"));
                            iemopBilling.setIthTag(iht.getStringCellValue().equalsIgnoreCase("Y"));
                            iemopBilling.setNonVatable(nvt.getStringCellValue().equalsIgnoreCase("Y"));
                            iemopBilling.setZeroRated(zrt.getStringCellValue().equalsIgnoreCase("Y"));
                            iemopBilling.setCreatedBy(createdBy);
                            iemopBilling.setReferenceNumber(refNumber);
                            iemopBilling.setTransDate(DateHelper.strToDate(date, "yyyy-MM-dd"));

                            if(extension.contains("csv")){
                                vs.setCellType(CellType.STRING);
                                zrs.setCellType(CellType.STRING);
                                zres.setCellType(CellType.STRING);
                                vos.setCellType(CellType.STRING);
                                vp.setCellType(CellType.STRING);
                                zrp.setCellType(CellType.STRING);
                                zrep.setCellType(CellType.STRING);
                                vop.setCellType(CellType.STRING);
                                ewts.setCellType(CellType.STRING);
                                ewtp.setCellType(CellType.STRING);

                                System.out.println(vs.getStringCellValue());

                                iemopBilling.setVatableSales(new BigDecimal(vs.getStringCellValue()));
                                iemopBilling.setZeroRatedSales(new BigDecimal(zrs.getStringCellValue()));
                                iemopBilling.setZeroRatedEcoSales(new BigDecimal(zres.getStringCellValue()));
                                iemopBilling.setVatOnSales(new BigDecimal(vos.getStringCellValue()));
                                iemopBilling.setVatablePurchases(new BigDecimal(vp.getStringCellValue()));
                                iemopBilling.setZeroRatedPurchases(new BigDecimal(zrp.getStringCellValue()));
                                iemopBilling.setZeroRatedEcoPurchases(new BigDecimal(zrep.getStringCellValue()));
                                iemopBilling.setVatOnPurchases(new BigDecimal(vop.getStringCellValue()));
                                iemopBilling.setEwtSales(new BigDecimal(ewts.getStringCellValue()));
                                iemopBilling.setEwtPurchases(new BigDecimal(ewtp.getStringCellValue()));
                            } else {
                                iemopBilling.setVatableSales(BigDecimal.valueOf(vs.getNumericCellValue()));
                                iemopBilling.setZeroRatedSales(BigDecimal.valueOf(zrs.getNumericCellValue()));
                                iemopBilling.setZeroRatedEcoSales(BigDecimal.valueOf(zres.getNumericCellValue()));
                                iemopBilling.setVatOnSales(BigDecimal.valueOf(vos.getNumericCellValue()));
                                iemopBilling.setVatablePurchases(BigDecimal.valueOf(vp.getNumericCellValue()));
                                iemopBilling.setZeroRatedPurchases(BigDecimal.valueOf(zrp.getNumericCellValue()));
                                iemopBilling.setZeroRatedEcoPurchases(BigDecimal.valueOf(zrep.getNumericCellValue()));
                                iemopBilling.setVatOnPurchases(BigDecimal.valueOf(vop.getNumericCellValue()));
                                iemopBilling.setEwtSales(BigDecimal.valueOf(ewts.getNumericCellValue()));
                                iemopBilling.setEwtPurchases(BigDecimal.valueOf(ewtp.getNumericCellValue()));
                            }

                            iemopBilling.setRemarks(remarks.getStringCellValue());

                            iemopBillingRepo.save(iemopBilling);

                        }
                    } else {
                        continue;
                    }
                }

                response.setSuccessMessage("IEMOP Billing Data successfully uploaded!");
                response.setSuccess(true);
            } else {
                response.setSuccessMessage("Uploading Failed!");
                response.setSuccess(false);
            }

        } catch (Exception e) {
            response.setSuccessMessage("Uploading Failed!");
            response.setSuccess(false);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return response;
    }

}
