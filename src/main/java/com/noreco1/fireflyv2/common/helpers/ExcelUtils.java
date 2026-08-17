package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.controller.response.reports.CommonRegisterDetail;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExcelUtils {

    public static void generateHeaderData(Workbook workbook, Sheet sheet, Map<String, Object> map, String reportType){

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

        try {

            //Header style
            CellStyle headerRowStyle = workbook.createCellStyle();
            headerRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerRowStyle.setAlignment(HorizontalAlignment.CENTER);
            headerRowStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerRowStyle.setBorderBottom(BorderStyle.THIN);
            headerRowStyle.setBorderTop(BorderStyle.THIN);
            headerRowStyle.setBorderRight(BorderStyle.THIN);
            headerRowStyle.setBorderLeft(BorderStyle.THIN);

            //Header Cell style
            CellStyle allCellStyle = workbook.createCellStyle();
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setAlignment(HorizontalAlignment.LEFT);
            allCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            allCellStyle.setBorderBottom(BorderStyle.THIN);
            allCellStyle.setBorderTop(BorderStyle.THIN);
            allCellStyle.setBorderRight(BorderStyle.THIN);
            allCellStyle.setBorderLeft(BorderStyle.THIN);

            //First header row
            Row firstHeader = sheet.createRow(0);
            firstHeader.createCell(0).setCellValue("ILOILO I ELECTRIC COOPERATIVE, INC");
            firstHeader.getCell(0).setCellStyle(headerRowStyle);

            //Second header row
            Row secondHeader = sheet.createRow(2);
            Date fromDate = DateHelper.strToDate(map.get("from").toString(), "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(map.get("to").toString(), "yyyy-MM-dd");
            secondHeader.createCell(0).setCellValue("From " + DATE_FORMAT.format(fromDate) + " to " + DATE_FORMAT.format(toDate));
            secondHeader.getCell(0).setCellStyle(headerRowStyle);

            if (map.size() > 3){
                secondHeader.createCell(1).setCellValue(map.get("docType").toString());
                secondHeader.getCell(1).setCellStyle(headerRowStyle);
            }

            //Third header row
            Row header = sheet.createRow(4);
            header.createCell(0).setCellValue("Date");
            header.getCell(0).setCellStyle(allCellStyle);
            header.createCell(1).setCellValue("Reference");
            header.getCell(1).setCellStyle(allCellStyle);

            if (Objects.equals(reportType, DocumentType.JV.getCode()) || Objects.equals(reportType, DocumentType.SV.getCode()) || Objects.equals(reportType, DocumentType.CRV.getCode()) || Objects.equals(reportType, DocumentType.AJ.getCode())) {

                header.createCell(2).setCellValue("Explanation");
                header.getCell(2).setCellStyle(allCellStyle);
                header.createCell(3).setCellValue("Code");
                header.getCell(3).setCellStyle(allCellStyle);
                header.createCell(4).setCellValue("Account Title");
                header.getCell(4).setCellStyle(allCellStyle);
                header.createCell(5).setCellValue("Debit");
                header.getCell(5).setCellStyle(allCellStyle);
                header.createCell(6).setCellValue("Credit");
                header.getCell(6).setCellStyle(allCellStyle);

            } else {

                if (Objects.equals(reportType, DocumentType.CV.getCode()) || Objects.equals(reportType, DocumentType.APV.getCode())) {

                    header.createCell(2).setCellValue("Payee");
                    header.getCell(2).setCellStyle(allCellStyle);

                } else if (Objects.equals(reportType, DocumentType.MR.getCode())) {

                    header.createCell(2).setCellValue("Document Number");
                    header.getCell(2).setCellStyle(allCellStyle);

                }

                header.createCell(3).setCellValue("Explanation");
                header.getCell(3).setCellStyle(allCellStyle);
                header.createCell(4).setCellValue("Code");
                header.getCell(4).setCellStyle(allCellStyle);
                header.createCell(5).setCellValue("Account Title");
                header.getCell(5).setCellStyle(allCellStyle);
                header.createCell(6).setCellValue("Debit");
                header.getCell(6).setCellStyle(allCellStyle);
                header.createCell(7).setCellValue("Credit");
                header.getCell(7).setCellStyle(allCellStyle);

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void generateRowData(Workbook workbook, Sheet sheet, List<CommonRegisterDetail> commonRegisterDetails, Map<String, Object> map, String reportType, List<RegisterRecapDetail> recap){

        try {

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
            DataFormat format = workbook.createDataFormat();

            //Date Cell style
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setBorderBottom(BorderStyle.THIN);
            dateCellStyle.setBorderTop(BorderStyle.THIN);
            dateCellStyle.setBorderRight(BorderStyle.THIN);
            dateCellStyle.setBorderLeft(BorderStyle.THIN);
            dateCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dateCellStyle.setAlignment(HorizontalAlignment.LEFT);

            //Reference Cell style
            CellStyle referenceCellStyle = workbook.createCellStyle();
            referenceCellStyle.cloneStyleFrom(dateCellStyle);

            //Document Number Cell style
            CellStyle docNumberCellStyle = workbook.createCellStyle();
            docNumberCellStyle.cloneStyleFrom(referenceCellStyle);

            //Payee Cell style
            CellStyle payeeCellStyle = workbook.createCellStyle();
            payeeCellStyle.cloneStyleFrom(docNumberCellStyle);

            //Explanation cell
            CellStyle explanationCellStyle = workbook.createCellStyle();
            explanationCellStyle.setBorderBottom(BorderStyle.THIN);
            explanationCellStyle.setBorderTop(BorderStyle.THIN);
            explanationCellStyle.setBorderRight(BorderStyle.THIN);
            explanationCellStyle.setBorderLeft(BorderStyle.THIN);
            explanationCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            explanationCellStyle.setWrapText(true);

            //Code Cell style
            CellStyle codeCellStyle = workbook.createCellStyle();
            codeCellStyle.cloneStyleFrom(explanationCellStyle);

            //Title cell
            CellStyle titleCellStyle = workbook.createCellStyle();
            titleCellStyle.cloneStyleFrom(codeCellStyle);

            //Debit cell
            CellStyle debitCellStyle = workbook.createCellStyle();
            debitCellStyle.setBorderBottom(BorderStyle.THIN);
            debitCellStyle.setBorderTop(BorderStyle.THIN);
            debitCellStyle.setBorderRight(BorderStyle.THIN);
            debitCellStyle.setBorderLeft(BorderStyle.THIN);
            debitCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            debitCellStyle.setDataFormat(format.getFormat("#,##0.00"));
            debitCellStyle.setWrapText(true);

            //Credit cell
            CellStyle creditCellStyle = workbook.createCellStyle();
            creditCellStyle.cloneStyleFrom(debitCellStyle);

            //Total Cell style
            CellStyle totalCellStyle = workbook.createCellStyle();
            totalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalCellStyle.setBorderBottom(BorderStyle.THIN);
            totalCellStyle.setBorderTop(BorderStyle.THIN);
            totalCellStyle.setBorderRight(BorderStyle.THIN);
            totalCellStyle.setBorderLeft(BorderStyle.THIN);
            totalCellStyle.setDataFormat(format.getFormat("#,##0.00"));

            int rowNum = 5;

            if (!commonRegisterDetails.isEmpty()) {

                BigDecimal debits = BigDecimal.ZERO;
                BigDecimal credits = BigDecimal.ZERO;

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                String pattern = "#,##0.0#";
                DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
                decimalFormat.setParseBigDecimal(true);

                int getPhysicalNumberOfCells = 0;

                for (CommonRegisterDetail commonRegisterDetail : commonRegisterDetails){

                    //Getting Grand Total of Debit and Credit
                    debits = debits.add(commonRegisterDetail.getDebit());
                    credits = credits.add(commonRegisterDetail.getCredit());

                    Row rowData = sheet.createRow(rowNum++);

                    rowData.createCell(0).setCellValue(commonRegisterDetail.getVoucherDate() == null ? "" : DATE_FORMAT.format(commonRegisterDetail.getVoucherDate()));
                    rowData.getCell(0).setCellStyle(dateCellStyle);

                    rowData.createCell(1).setCellValue(commonRegisterDetail.getReference());
                    rowData.getCell(1).setCellStyle(referenceCellStyle);

                    if (Objects.equals(commonRegisterDetail.getReportType(), DocumentType.JV.getCode()) || Objects.equals(commonRegisterDetail.getReportType(), DocumentType.SV.getCode()) || Objects.equals(commonRegisterDetail.getReportType(), DocumentType.CRV.getCode()) || Objects.equals(commonRegisterDetail.getReportType(), DocumentType.AJ.getCode())) {

                        rowData.createCell(2).setCellValue(commonRegisterDetail.getExplanation());
                        rowData.getCell(2).setCellStyle(explanationCellStyle);

                        rowData.createCell(3).setCellValue(commonRegisterDetail.getCode());
                        rowData.getCell(3).setCellStyle(codeCellStyle);

                        rowData.createCell(4).setCellValue(commonRegisterDetail.getTitle());
                        rowData.getCell(4).setCellStyle(titleCellStyle);

                        BigDecimal bigDecimalDebit = (BigDecimal) decimalFormat.parse(commonRegisterDetail.getsDebit());
                        BigDecimal bigDecimalCredit = (BigDecimal) decimalFormat.parse(commonRegisterDetail.getsCredit());

                        double doubleDebits = Double.parseDouble(String.valueOf(bigDecimalDebit));
                        rowData.createCell(5).setCellValue(doubleDebits);
                        rowData.getCell(5).setCellStyle(debitCellStyle);

                        double doubleCredits = Double.parseDouble(String.valueOf(bigDecimalCredit));
                        rowData.createCell(6).setCellValue(doubleCredits);
                        rowData.getCell(6).setCellStyle(creditCellStyle);

                    } else {

                        if (Objects.equals(commonRegisterDetail.getReportType(), DocumentType.CV.getCode()) || Objects.equals(commonRegisterDetail.getReportType(), DocumentType.APV.getCode())) {
                            rowData.createCell(2).setCellValue(commonRegisterDetail.getPayee());
                            rowData.getCell(2).setCellStyle(payeeCellStyle);
                        } else if (Objects.equals(commonRegisterDetail.getReportType(), DocumentType.MR.getCode())) {
                            rowData.createCell(2).setCellValue(commonRegisterDetail.getDocNumber());
                            rowData.getCell(2).setCellStyle(docNumberCellStyle);
                        }

                        rowData.createCell(3).setCellValue(commonRegisterDetail.getExplanation());
                        rowData.getCell(3).setCellStyle(explanationCellStyle);

                        rowData.createCell(4).setCellValue(commonRegisterDetail.getCode());
                        rowData.getCell(4).setCellStyle(codeCellStyle);

                        rowData.createCell(5).setCellValue(commonRegisterDetail.getTitle());
                        rowData.getCell(5).setCellStyle(titleCellStyle);

                        BigDecimal bigDecimalDebit = (BigDecimal) decimalFormat.parse(commonRegisterDetail.getsDebit());
                        BigDecimal bigDecimalCredit = (BigDecimal) decimalFormat.parse(commonRegisterDetail.getsCredit());

                        double doubleDebits = Double.parseDouble(String.valueOf(bigDecimalDebit));
                        rowData.createCell(6).setCellValue(doubleDebits);
                        rowData.getCell(6).setCellStyle(debitCellStyle);

                        double doubleCredits = Double.parseDouble(String.valueOf(bigDecimalCredit));
                        rowData.createCell(7).setCellValue(doubleCredits);
                        rowData.getCell(7).setCellStyle(creditCellStyle);

                    }

                    getPhysicalNumberOfCells = rowData.getPhysicalNumberOfCells();

                }

                //Footer for Total
                Row total = sheet.createRow(rowNum);

                if (getPhysicalNumberOfCells == 7){

                    total.createCell(4).setCellValue("TOTAL");
                    total.getCell(4).setCellStyle(totalCellStyle);

                    double doubleDebits = Double.parseDouble(String.valueOf(debits));
                    total.createCell(5).setCellValue((doubleDebits));
                    total.getCell(5).setCellStyle(totalCellStyle);

                    double doubleCredit = Double.parseDouble(String.valueOf(credits));
                    total.createCell(6).setCellValue(doubleCredit);
                    total.getCell(6).setCellStyle(totalCellStyle);

                } else if (getPhysicalNumberOfCells == 8){

                    total.createCell(5).setCellValue("TOTAL");
                    total.getCell(5).setCellStyle(totalCellStyle);

                    double doubleDebits = Double.parseDouble(String.valueOf(debits));
                    total.createCell(6).setCellValue((doubleDebits));
                    total.getCell(6).setCellStyle(totalCellStyle);

                    double doubleCredit = Double.parseDouble(String.valueOf(credits));
                    total.createCell(7).setCellValue(doubleCredit);
                    total.getCell(7).setCellStyle(totalCellStyle);

                }

            }

            if(!recap.isEmpty()){
                ExcelUtils.generateRecapHeaderData(rowNum+1, workbook, sheet, map, reportType, recap);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void generateRecapHeaderData(int rowStart, Workbook workbook, Sheet sheet, Map<String, Object> map, String reportType, List<RegisterRecapDetail> recap){

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

        try {

            //Header style
            CellStyle headerRowStyle = workbook.createCellStyle();
            headerRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerRowStyle.setAlignment(HorizontalAlignment.CENTER);
            headerRowStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerRowStyle.setBorderBottom(BorderStyle.THIN);
            headerRowStyle.setBorderTop(BorderStyle.THIN);
            headerRowStyle.setBorderRight(BorderStyle.THIN);
            headerRowStyle.setBorderLeft(BorderStyle.THIN);

            //Header Cell style
            CellStyle allCellStyle = workbook.createCellStyle();
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setAlignment(HorizontalAlignment.LEFT);
            allCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            allCellStyle.setBorderBottom(BorderStyle.THIN);
            allCellStyle.setBorderTop(BorderStyle.THIN);
            allCellStyle.setBorderRight(BorderStyle.THIN);
            allCellStyle.setBorderLeft(BorderStyle.THIN);

            //Header Cell style
            CellStyle allCellStyleHead = workbook.createCellStyle();
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setAlignment(HorizontalAlignment.CENTER);
            allCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            allCellStyle.setBorderBottom(BorderStyle.THIN);
            allCellStyle.setBorderTop(BorderStyle.THIN);
            allCellStyle.setBorderRight(BorderStyle.THIN);
            allCellStyle.setBorderLeft(BorderStyle.THIN);

            //First header row
            Row firstHeader = sheet.createRow(rowStart++);
            firstHeader.createCell(0).setCellValue("RECAP ENTRIES");
            firstHeader.getCell(0).setCellStyle(headerRowStyle);

            //Second header row
            Row secondHeader = sheet.createRow(rowStart++);
            Date fromDate = DateHelper.strToDate(map.get("from").toString(), "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(map.get("to").toString(), "yyyy-MM-dd");
            secondHeader.createCell(0).setCellValue("From " + DATE_FORMAT.format(fromDate) + " to " + DATE_FORMAT.format(toDate));
            secondHeader.getCell(0).setCellStyle(headerRowStyle);

            sheet.addMergedRegion(new CellRangeAddress(rowStart, rowStart, 0, 5));
            sheet.addMergedRegion(new CellRangeAddress(rowStart, rowStart, 6, 9));

            //Third header row
            Row thirdHeader = sheet.createRow(rowStart++);
            thirdHeader.createCell(0).setCellValue("General Ledger");
            thirdHeader.getCell(0).setCellStyle(allCellStyle);
            thirdHeader.createCell(6).setCellValue("Subsidiary Ledger");
            thirdHeader.getCell(6).setCellStyle(allCellStyle);

            //Fourth header row
            Row header = sheet.createRow(rowStart++);
            header.createCell(0).setCellValue("Account Code");
            header.getCell(0).setCellStyle(allCellStyle);
            header.createCell(1).setCellValue("Account Title");
            header.getCell(1).setCellStyle(allCellStyle);

            header.createCell(2).setCellValue("Debit");
            header.getCell(2).setCellStyle(allCellStyle);
            header.createCell(3).setCellValue("Credit");
            header.getCell(3).setCellStyle(allCellStyle);

            header.createCell(4).setCellValue("Debit");
            header.getCell(4).setCellStyle(allCellStyle);
            header.createCell(5).setCellValue("Credit");
            header.getCell(5).setCellStyle(allCellStyle);

            header.createCell(6).setCellValue("Name");
            header.getCell(6).setCellStyle(allCellStyle);
            header.createCell(7).setCellValue("Account Code");
            header.getCell(7).setCellStyle(allCellStyle);
            header.createCell(8).setCellValue("Debit");
            header.getCell(8).setCellStyle(allCellStyle);
            header.createCell(9).setCellValue("Credit");
            header.getCell(9).setCellStyle(allCellStyle);

            generateRecapRowData(rowStart, workbook, sheet, recap);

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void generateRecapRowData(int rowStart, Workbook workbook, Sheet sheet, List<RegisterRecapDetail> recapDetails){

        try {

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
            DataFormat format = workbook.createDataFormat();

            //Date Cell style
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setBorderBottom(BorderStyle.THIN);
            dateCellStyle.setBorderTop(BorderStyle.THIN);
            dateCellStyle.setBorderRight(BorderStyle.THIN);
            dateCellStyle.setBorderLeft(BorderStyle.THIN);
            dateCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dateCellStyle.setAlignment(HorizontalAlignment.LEFT);

            //Reference Cell style
            CellStyle referenceCellStyle = workbook.createCellStyle();
            referenceCellStyle.cloneStyleFrom(dateCellStyle);

            //Document Number Cell style
            CellStyle docNumberCellStyle = workbook.createCellStyle();
            docNumberCellStyle.cloneStyleFrom(referenceCellStyle);

            //Payee Cell style
            CellStyle payeeCellStyle = workbook.createCellStyle();
            payeeCellStyle.cloneStyleFrom(docNumberCellStyle);

            //Explanation cell
            CellStyle explanationCellStyle = workbook.createCellStyle();
            explanationCellStyle.setBorderBottom(BorderStyle.THIN);
            explanationCellStyle.setBorderTop(BorderStyle.THIN);
            explanationCellStyle.setBorderRight(BorderStyle.THIN);
            explanationCellStyle.setBorderLeft(BorderStyle.THIN);
            explanationCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            explanationCellStyle.setWrapText(true);

            //Code Cell style
            CellStyle codeCellStyle = workbook.createCellStyle();
            codeCellStyle.cloneStyleFrom(explanationCellStyle);

            //Title cell
            CellStyle titleCellStyle = workbook.createCellStyle();
            titleCellStyle.cloneStyleFrom(codeCellStyle);

            //Debit cell
            CellStyle debitCellStyle = workbook.createCellStyle();
            debitCellStyle.setBorderBottom(BorderStyle.THIN);
            debitCellStyle.setBorderTop(BorderStyle.THIN);
            debitCellStyle.setBorderRight(BorderStyle.THIN);
            debitCellStyle.setBorderLeft(BorderStyle.THIN);
            debitCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            debitCellStyle.setDataFormat(format.getFormat("#,##0.00"));
            debitCellStyle.setWrapText(true);

            //Credit cell
            CellStyle creditCellStyle = workbook.createCellStyle();
            creditCellStyle.cloneStyleFrom(debitCellStyle);

            //Total Cell style
            CellStyle totalCellStyle = workbook.createCellStyle();
            totalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalCellStyle.setBorderBottom(BorderStyle.THIN);
            totalCellStyle.setBorderTop(BorderStyle.THIN);
            totalCellStyle.setBorderRight(BorderStyle.THIN);
            totalCellStyle.setBorderLeft(BorderStyle.THIN);
            totalCellStyle.setDataFormat(format.getFormat("#,##0.00"));

            int rowNum = rowStart;

            if (!recapDetails.isEmpty()) {

                BigDecimal transDebits = BigDecimal.ZERO;
                BigDecimal transCredits = BigDecimal.ZERO;

                BigDecimal glDebits = BigDecimal.ZERO;
                BigDecimal glCredits = BigDecimal.ZERO;

                BigDecimal slDebits = BigDecimal.ZERO;
                BigDecimal slCredits = BigDecimal.ZERO;

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                String pattern = "#,##0.0#";
                DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
                decimalFormat.setParseBigDecimal(true);

                int getPhysicalNumberOfCells = 0;

                for (RegisterRecapDetail recapDetail : recapDetails){

                    //Getting Grand Total of Debit and Credit
                    transDebits = transDebits.add(recapDetail.getTransDebit() != null ? recapDetail.getTransDebit() : BigDecimal.ZERO);
                    transCredits = transCredits.add(recapDetail.getTransCredit() != null ? recapDetail.getTransCredit() : BigDecimal.ZERO);

                    glDebits = glDebits.add(recapDetail.getGlDebit() != null ? recapDetail.getGlDebit() : BigDecimal.ZERO);
                    glCredits = glCredits.add(recapDetail.getGlCredit() != null ? recapDetail.getGlCredit() : BigDecimal.ZERO);

                    slDebits = slDebits.add(recapDetail.getSlDebit() != null ? recapDetail.getSlDebit() : BigDecimal.ZERO);
                    slCredits = slCredits.add(recapDetail.getSlCredit() != null ? recapDetail.getSlCredit() : BigDecimal.ZERO);

                    Row rowData = sheet.createRow(rowNum++);

                    rowData.createCell(0).setCellValue(recapDetail.getGlAccountCode() != null ? recapDetail.getGlAccountCode() : "");
                    rowData.getCell(0).setCellStyle(codeCellStyle);

                    rowData.createCell(1).setCellValue(recapDetail.getGlAccountTitle() != null ? recapDetail.getGlAccountTitle() : "");
                    rowData.getCell(1).setCellStyle(titleCellStyle);

                    double doubleTransDebits = recapDetail.getTransDebit() != null ? Double.parseDouble(String.valueOf(recapDetail.getTransDebit())) : 0;
                    rowData.createCell(2).setCellValue(doubleTransDebits);
                    rowData.getCell(2).setCellStyle(debitCellStyle);

                    double doubleTransCredits = recapDetail.getTransCredit() != null ? Double.parseDouble(String.valueOf(recapDetail.getTransCredit())) : 0;
                    rowData.createCell(3).setCellValue(doubleTransCredits);
                    rowData.getCell(3).setCellStyle(creditCellStyle);

                    double doubleGlDebits = recapDetail.getGlDebit() != null ? Double.parseDouble(String.valueOf(recapDetail.getGlDebit())) : 0;
                    rowData.createCell(4).setCellValue(doubleGlDebits);
                    rowData.getCell(4).setCellStyle(debitCellStyle);

                    double doubleGlCredits = recapDetail.getGlCredit() != null ? Double.parseDouble(String.valueOf(recapDetail.getGlCredit())) : 0;
                    rowData.createCell(5).setCellValue(doubleGlCredits);
                    rowData.getCell(5).setCellStyle(creditCellStyle);

                    rowData.createCell(6).setCellValue(recapDetail.getSlAccountTitle() != null ? recapDetail.getSlAccountTitle() : "");
                    rowData.getCell(6).setCellStyle(codeCellStyle);

                    rowData.createCell(7).setCellValue(recapDetail.getSlAccountCode() != null ? recapDetail.getSlAccountCode() : "");
                    rowData.getCell(7).setCellStyle(titleCellStyle);

                    double doubleSlDebits = recapDetail.getSlDebit() != null ? Double.parseDouble(String.valueOf(recapDetail.getSlDebit())) : 0;
                    rowData.createCell(8).setCellValue(doubleSlDebits);
                    rowData.getCell(8).setCellStyle(debitCellStyle);

                    double doubleSlCredits = recapDetail.getSlCredit() != null ? Double.parseDouble(String.valueOf(recapDetail.getSlCredit())) : 0;
                    rowData.createCell(9).setCellValue(doubleSlCredits);
                    rowData.getCell(9).setCellStyle(creditCellStyle);

                    getPhysicalNumberOfCells = rowData.getPhysicalNumberOfCells();

                }

                //Footer for Total
                Row total = sheet.createRow(rowNum);

                double doubleTransDebits = Double.parseDouble(String.valueOf(transDebits));
                total.createCell(2).setCellValue((doubleTransDebits));
                total.getCell(2).setCellStyle(totalCellStyle);

                double doubleTransCredit = Double.parseDouble(String.valueOf(transCredits));
                total.createCell(3).setCellValue(doubleTransCredit);
                total.getCell(3).setCellStyle(totalCellStyle);

                double doubleGlDebits = Double.parseDouble(String.valueOf(glDebits));
                total.createCell(4).setCellValue((doubleGlDebits));
                total.getCell(4).setCellStyle(totalCellStyle);

                double doubleGlCredit = Double.parseDouble(String.valueOf(glCredits));
                total.createCell(5).setCellValue(doubleGlCredit);
                total.getCell(5).setCellStyle(totalCellStyle);

                double doubleSlDebits = Double.parseDouble(String.valueOf(slDebits));
                total.createCell(8).setCellValue((doubleSlDebits));
                total.getCell(8).setCellStyle(totalCellStyle);

                double doubleSlCredit = Double.parseDouble(String.valueOf(slCredits));
                total.createCell(9).setCellValue(doubleSlCredit);
                total.getCell(9).setCellStyle(totalCellStyle);

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void generateHeaderDataTB(Workbook workbook, Sheet sheet, Map<String, Object> model, Map reportData){

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("MM/dd/yyyy");

        try {

            //Header style
            CellStyle headerRowStyle = workbook.createCellStyle();
            headerRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerRowStyle.setAlignment(HorizontalAlignment.CENTER);
            headerRowStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerRowStyle.setBorderBottom(BorderStyle.THIN);
            headerRowStyle.setBorderTop(BorderStyle.THIN);
            headerRowStyle.setBorderRight(BorderStyle.THIN);
            headerRowStyle.setBorderLeft(BorderStyle.THIN);

            //Header style
            CellStyle subHeaderRowStyle = workbook.createCellStyle();
            subHeaderRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subHeaderRowStyle.setAlignment(HorizontalAlignment.CENTER);
            subHeaderRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subHeaderRowStyle.setBorderBottom(BorderStyle.THIN);
            subHeaderRowStyle.setBorderTop(BorderStyle.THIN);
            subHeaderRowStyle.setBorderRight(BorderStyle.THIN);
            subHeaderRowStyle.setBorderLeft(BorderStyle.THIN);

            //Header Cell style
            CellStyle allCellStyle = workbook.createCellStyle();
            allCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            allCellStyle.setAlignment(HorizontalAlignment.LEFT);
            allCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            allCellStyle.setBorderBottom(BorderStyle.THIN);
            allCellStyle.setBorderTop(BorderStyle.THIN);
            allCellStyle.setBorderRight(BorderStyle.THIN);
            allCellStyle.setBorderLeft(BorderStyle.THIN);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            //First header row
            Row firstHeader = sheet.createRow(0);
            firstHeader.createCell(0).setCellValue("ILOILO I ELECTRIC COOPERATIVE, INC");
            firstHeader.getCell(0).setCellStyle(headerRowStyle);

            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            Row thirdHeader = sheet.createRow(1);
            thirdHeader.createCell(0).setCellValue("Trial Balance - NEA");
            thirdHeader.getCell(0).setCellStyle(headerRowStyle);

            //Second header row
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));
            Row secondHeader = sheet.createRow(2);
            Date fromDate = DateHelper.strToDate(reportData.get("from").toString(), "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(reportData.get("to").toString(), "yyyy-MM-dd");

            Date begDate = DateHelper.strToDate(reportData.get("begDate").toString(), "yyyy-MM-dd");
            Date endDate = DateHelper.strToDate(reportData.get("endDate").toString(), "yyyy-MM-dd");

            secondHeader.createCell(0).setCellValue("From " + DATE_FORMAT.format(fromDate) + " to " + DATE_FORMAT.format(toDate));
            secondHeader.getCell(0).setCellStyle(headerRowStyle);

            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 3));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 4, 5));
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 6, 7));

            //Third header row
            Row header = sheet.createRow(4);
            header.createCell(0).setCellValue("");
            header.getCell(0).setCellStyle(subHeaderRowStyle);
            header.createCell(1).setCellValue("");
            header.getCell(1).setCellStyle(subHeaderRowStyle);
            header.createCell(2).setCellValue("Beginning Balance");
            header.getCell(2).setCellStyle(subHeaderRowStyle);
            header.createCell(3).setCellValue("");
            header.getCell(3).setCellStyle(subHeaderRowStyle);
            header.createCell(4).setCellValue("Transactions");
            header.getCell(4).setCellStyle(subHeaderRowStyle);
            header.createCell(5).setCellValue("");
            header.getCell(5).setCellStyle(subHeaderRowStyle);
            header.createCell(6).setCellValue("Ending Balance");
            header.getCell(6).setCellStyle(subHeaderRowStyle);
            header.createCell(7).setCellValue("");
            header.getCell(7).setCellStyle(subHeaderRowStyle);

            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 3));
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 4, 5));
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 6, 7));

            //Third header row
            Row dateHeader = sheet.createRow(5);
            dateHeader.createCell(0).setCellValue("");
            dateHeader.getCell(0).setCellStyle(subHeaderRowStyle);
            header.createCell(1).setCellValue("");
            header.getCell(1).setCellStyle(subHeaderRowStyle);
            dateHeader.createCell(2).setCellValue(DATE_FORMAT_2.format(begDate));
            dateHeader.getCell(2).setCellStyle(subHeaderRowStyle);
            header.createCell(3).setCellValue("");
            header.getCell(3).setCellStyle(subHeaderRowStyle);
            dateHeader.createCell(4).setCellValue("");
            dateHeader.getCell(4).setCellStyle(subHeaderRowStyle);
            header.createCell(5).setCellValue("");
            header.getCell(5).setCellStyle(subHeaderRowStyle);
            dateHeader.createCell(6).setCellValue(DATE_FORMAT_2.format(endDate));
            dateHeader.getCell(6).setCellStyle(subHeaderRowStyle);
            header.createCell(7).setCellValue("");
            header.getCell(7).setCellStyle(subHeaderRowStyle);

            //Third header row
            Row subHeader = sheet.createRow(6);
            subHeader.createCell(0).setCellValue("Account Code");
            subHeader.getCell(0).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(1).setCellValue("Account Title");
            subHeader.getCell(1).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(2).setCellValue("DEBIT");
            subHeader.getCell(2).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(3).setCellValue("CREDIT");
            subHeader.getCell(3).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(4).setCellValue("DEBIT");
            subHeader.getCell(4).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(5).setCellValue("CREDIT");
            subHeader.getCell(5).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(6).setCellValue("DEBIT");
            subHeader.getCell(6).setCellStyle(subHeaderRowStyle);
            subHeader.createCell(7).setCellValue("CREDIT");
            subHeader.getCell(7).setCellStyle(subHeaderRowStyle);

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static void generateRowDataTB(Workbook workbook, Sheet sheet, List<Map> details, Map<String, Object> map, Map reportData){

        try {

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
            DataFormat format = workbook.createDataFormat();

            //Date Cell style
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setBorderBottom(BorderStyle.THIN);
            dateCellStyle.setBorderTop(BorderStyle.THIN);
            dateCellStyle.setBorderRight(BorderStyle.THIN);
            dateCellStyle.setBorderLeft(BorderStyle.THIN);
            dateCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dateCellStyle.setAlignment(HorizontalAlignment.LEFT);

            //Reference Cell style
            CellStyle referenceCellStyle = workbook.createCellStyle();
            referenceCellStyle.cloneStyleFrom(dateCellStyle);

            //Document Number Cell style
            CellStyle docNumberCellStyle = workbook.createCellStyle();
            docNumberCellStyle.cloneStyleFrom(referenceCellStyle);

            //Payee Cell style
            CellStyle payeeCellStyle = workbook.createCellStyle();
            payeeCellStyle.cloneStyleFrom(docNumberCellStyle);

            //Explanation cell
            CellStyle explanationCellStyle = workbook.createCellStyle();
            explanationCellStyle.setBorderBottom(BorderStyle.THIN);
            explanationCellStyle.setBorderTop(BorderStyle.THIN);
            explanationCellStyle.setBorderRight(BorderStyle.THIN);
            explanationCellStyle.setBorderLeft(BorderStyle.THIN);
            explanationCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            explanationCellStyle.setWrapText(true);

            //Code Cell style
            CellStyle codeCellStyle = workbook.createCellStyle();
            codeCellStyle.cloneStyleFrom(explanationCellStyle);

            //Title cell
            CellStyle titleCellStyle = workbook.createCellStyle();
            titleCellStyle.cloneStyleFrom(codeCellStyle);

            //Debit cell
            CellStyle debitCellStyle = workbook.createCellStyle();
            debitCellStyle.setBorderBottom(BorderStyle.THIN);
            debitCellStyle.setBorderTop(BorderStyle.THIN);
            debitCellStyle.setBorderRight(BorderStyle.THIN);
            debitCellStyle.setBorderLeft(BorderStyle.THIN);
            debitCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            debitCellStyle.setDataFormat(format.getFormat("#,##0.00;(#,##0.00)"));
            debitCellStyle.setWrapText(true);

            //Credit cell
            CellStyle creditCellStyle = workbook.createCellStyle();
            creditCellStyle.cloneStyleFrom(debitCellStyle);

            //Total Cell style
            CellStyle totalCellStyle = workbook.createCellStyle();
            totalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalCellStyle.setBorderBottom(BorderStyle.THIN);
            totalCellStyle.setBorderTop(BorderStyle.THIN);
            totalCellStyle.setBorderRight(BorderStyle.THIN);
            totalCellStyle.setBorderLeft(BorderStyle.THIN);
            totalCellStyle.setDataFormat(format.getFormat("#,##0.00;(#,##0.00)"));

            //Total Summary Cell style
            CellStyle totalSummaryCellStyle = workbook.createCellStyle();
            totalSummaryCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            totalSummaryCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalSummaryCellStyle.setWrapText(true);

            CellStyle totalSummaryCellAmountStyle = workbook.createCellStyle();
            totalSummaryCellAmountStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            totalSummaryCellAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalSummaryCellAmountStyle.setDataFormat(format.getFormat("#,##0.00;(#,##0.00)"));

            //Signatory Cell style
            CellStyle signatoryCellStyle = workbook.createCellStyle();
            signatoryCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            signatoryCellStyle.setAlignment(HorizontalAlignment.LEFT);
            signatoryCellStyle.setWrapText(true);

            //Signatory NAme style
            CellStyle signatoryNameCellStyle = workbook.createCellStyle();
            signatoryNameCellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
            signatoryNameCellStyle.setAlignment(HorizontalAlignment.CENTER);
            signatoryNameCellStyle.setWrapText(true);

            //Signatory position style
            CellStyle signatoryPosCellStyle = workbook.createCellStyle();
            signatoryPosCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            signatoryPosCellStyle.setAlignment(HorizontalAlignment.CENTER);
            signatoryPosCellStyle.setBorderTop(BorderStyle.THIN);
            signatoryPosCellStyle.setWrapText(true);

            int rowNum = 7;

            if (!details.isEmpty()) {

                BigDecimal begDebitTotal = BigDecimal.ZERO;
                BigDecimal begCreditTotal = BigDecimal.ZERO;

                BigDecimal transDebitTotal = BigDecimal.ZERO;
                BigDecimal transCreditTotal = BigDecimal.ZERO;

                BigDecimal endDebitTotal = BigDecimal.ZERO;
                BigDecimal endCreditTotal = BigDecimal.ZERO;

                BigDecimal grandTotalAsset = BigDecimal.ZERO;
                BigDecimal grandTotalLiabAndEqui = BigDecimal.ZERO;

                BigDecimal grandTotalRevenue = BigDecimal.ZERO;
                BigDecimal grandTotalCostAndExp = BigDecimal.ZERO;

                BigDecimal grandTotalTransRevenue = BigDecimal.ZERO;
                BigDecimal grandTotalTransCostAndExp = BigDecimal.ZERO;

                BigDecimal grandTotalBegRevenue = BigDecimal.ZERO;
                BigDecimal grandTotalBegCostAndExp = BigDecimal.ZERO;

                BigDecimal netIncomePrevMonth = BigDecimal.ZERO;
                BigDecimal netIncomeCurrMonth = BigDecimal.ZERO;

                BigDecimal yearToDateIncome = BigDecimal.ZERO;
                BigDecimal totalIncome = BigDecimal.ZERO;
                BigDecimal liaEqui = BigDecimal.ZERO;
                BigDecimal variance = BigDecimal.ZERO;

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                symbols.setDecimalSeparator('.');
                String pattern = "#,##0.0#";
                DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
                decimalFormat.setParseBigDecimal(true);

                int getPhysicalNumberOfCells = 0;

                for (Map detail : details){

                    Row rowData = sheet.createRow(rowNum++);

                    String accountcode = (String) detail.get("code");
                    String accountTitle = (String) detail.get("title");

                    BigDecimal begDebit = (BigDecimal) detail.get("begDebit");
                    BigDecimal begCredit = (BigDecimal) detail.get("begCredit");

                    BigDecimal transDebit = (BigDecimal) detail.get("transDebit");
                    BigDecimal transCredit = (BigDecimal) detail.get("transCredit");

                    BigDecimal endDebit = (BigDecimal) detail.get("endingDebit");
                    BigDecimal endCredit = (BigDecimal) detail.get("endingCredit");

                    BigDecimal totalAsset = (BigDecimal) detail.get("totalAsset");
                    BigDecimal totalLiabAndEqui = (BigDecimal) detail.get("totalLiabAndEqui");
                    BigDecimal totalRev = (BigDecimal) detail.get("totalRevenue");
                    BigDecimal totalCostAndExp = (BigDecimal) detail.get("totalCostAndExp");
                    BigDecimal totalTransRev = (BigDecimal) detail.get("totalTransRevenue");
                    BigDecimal totalTransCostAndExp = (BigDecimal) detail.get("totalTransCostAndExp");
                    BigDecimal totalBegRev = (BigDecimal) detail.get("totalBegRevenue");
                    BigDecimal totalBegCostAndExp = (BigDecimal) detail.get("totalBegCostAndExp");

                    grandTotalAsset = grandTotalAsset.add(totalAsset != null ? totalAsset : BigDecimal.ZERO);
                    grandTotalLiabAndEqui = grandTotalLiabAndEqui.add(totalLiabAndEqui != null ? totalLiabAndEqui : BigDecimal.ZERO);
                    grandTotalRevenue = grandTotalRevenue.add(totalRev != null ? totalRev : BigDecimal.ZERO);
                    grandTotalCostAndExp = grandTotalCostAndExp.add(totalCostAndExp != null ? totalCostAndExp : BigDecimal.ZERO);
                    grandTotalTransRevenue = grandTotalTransRevenue.add(totalTransRev != null ? totalTransRev : BigDecimal.ZERO);
                    grandTotalTransCostAndExp = grandTotalTransCostAndExp.add(totalTransCostAndExp != null ? totalTransCostAndExp : BigDecimal.ZERO);
                    grandTotalBegRevenue = grandTotalBegRevenue.add(totalBegRev != null ? totalBegRev : BigDecimal.ZERO);
                    grandTotalBegCostAndExp = grandTotalBegCostAndExp.add(totalBegCostAndExp != null ? totalBegCostAndExp : BigDecimal.ZERO);

                    Boolean isHeader = (Boolean) detail.get("isHeader");

                    rowData.createCell(0).setCellValue(accountcode);
                    rowData.getCell(0).setCellStyle(codeCellStyle);

                    rowData.createCell(1).setCellValue(accountTitle);
                    rowData.getCell(1).setCellStyle(titleCellStyle);

                    if(begDebit != null){

                        if(isHeader != null && !isHeader){
                            begDebitTotal = begDebitTotal.add(begDebit);
                        }

                        double doubleBegDebit = Double.parseDouble(String.valueOf(begDebit));
                        rowData.createCell(2).setCellValue(doubleBegDebit);
                        rowData.getCell(2).setCellStyle(debitCellStyle);
                    } else {
                        rowData.createCell(2).setCellValue("");
                        rowData.getCell(2).setCellStyle(titleCellStyle);
                    }

                    if(begCredit != null){
                        if(isHeader != null && !isHeader){
                            begCreditTotal = begCreditTotal.add(begCredit);
                        }
                        double doubleBegCredit = Double.parseDouble(String.valueOf(begCredit));
                        rowData.createCell(3).setCellValue(doubleBegCredit);
                        rowData.getCell(3).setCellStyle(creditCellStyle);
                    } else {
                        rowData.createCell(3).setCellValue("");
                        rowData.getCell(3).setCellStyle(titleCellStyle);
                    }

                    if( isHeader != null && !isHeader && transDebit != null){

                        transDebitTotal = transDebitTotal.add(transDebit);

                        double doubleTransDebit = Double.parseDouble(String.valueOf(transDebit));
                        rowData.createCell(4).setCellValue(doubleTransDebit);
                        rowData.getCell(4).setCellStyle(debitCellStyle);
                    } else {
                        rowData.createCell(4).setCellValue("");
                        rowData.getCell(4).setCellStyle(titleCellStyle);
                    }

                    if( isHeader != null && !isHeader && transCredit != null){

                        transCreditTotal = transCreditTotal.add(transCredit);

                        double doubleTransCredit = Double.parseDouble(String.valueOf(transCredit));
                        rowData.createCell(5).setCellValue(doubleTransCredit);
                        rowData.getCell(5).setCellStyle(creditCellStyle);
                    } else {
                        rowData.createCell(5).setCellValue("");
                        rowData.getCell(5).setCellStyle(titleCellStyle);
                    }

                    if(endDebit != null){
                        if(isHeader != null && !isHeader){
                            endDebitTotal = endDebitTotal.add(endDebit);
                        }
                        double doubleEndDebit = Double.parseDouble(String.valueOf(endDebit));
                        rowData.createCell(6).setCellValue(doubleEndDebit);
                        rowData.getCell(6).setCellStyle(debitCellStyle);
                    } else {
                        rowData.createCell(6).setCellValue("");
                        rowData.getCell(6).setCellStyle(titleCellStyle);
                    }

                    if(endCredit != null){
                        if(isHeader != null && !isHeader){
                            endCreditTotal = endCreditTotal.add(endCredit);
                        }
                        double doubleEndCredit = Double.parseDouble(String.valueOf(endCredit));
                        rowData.createCell(7).setCellValue(doubleEndCredit);
                        rowData.getCell(7).setCellStyle(creditCellStyle);
                    } else {
                        rowData.createCell(7).setCellValue("");
                        rowData.getCell(7).setCellStyle(titleCellStyle);
                    }

                    getPhysicalNumberOfCells = rowData.getPhysicalNumberOfCells();

                }

                //Footer for Total
                Row total = sheet.createRow(rowNum);

                total.createCell(1).setCellValue("TOTAL");
                total.getCell(1).setCellStyle(totalCellStyle);

                total.createCell(1).setCellValue("TOTAL");
                total.getCell(1).setCellStyle(totalCellStyle);

                double doubleBegDebitTotal = Double.parseDouble(String.valueOf(begDebitTotal));
                total.createCell(2).setCellValue((doubleBegDebitTotal));
                total.getCell(2).setCellStyle(totalCellStyle);

                double doubleBegCreditTotal = Double.parseDouble(String.valueOf(begCreditTotal));
                total.createCell(3).setCellValue(doubleBegCreditTotal);
                total.getCell(3).setCellStyle(totalCellStyle);

                double doubleTransDebitTotal = Double.parseDouble(String.valueOf(transDebitTotal));
                total.createCell(4).setCellValue((doubleTransDebitTotal));
                total.getCell(4).setCellStyle(totalCellStyle);

                double doubleTransCreditTotal = Double.parseDouble(String.valueOf(transCreditTotal));
                total.createCell(5).setCellValue(doubleTransCreditTotal);
                total.getCell(5).setCellStyle(totalCellStyle);

                double doubleEndDebitTotal = Double.parseDouble(String.valueOf(endDebitTotal));
                total.createCell(6).setCellValue((doubleEndDebitTotal));
                total.getCell(6).setCellStyle(totalCellStyle);

                double doubleEndCreditTotal = Double.parseDouble(String.valueOf(endCreditTotal));
                total.createCell(7).setCellValue(doubleEndCreditTotal);
                total.getCell(7).setCellStyle(totalCellStyle);

                rowNum++;
                rowNum++;

                netIncomeCurrMonth = grandTotalTransRevenue.subtract(grandTotalTransCostAndExp);
                netIncomePrevMonth = grandTotalBegRevenue.subtract(grandTotalBegCostAndExp);
                yearToDateIncome = netIncomeCurrMonth.add(netIncomePrevMonth);
                totalIncome = grandTotalLiabAndEqui.add(netIncomeCurrMonth). add(netIncomePrevMonth);
                liaEqui = grandTotalLiabAndEqui.add(grandTotalRevenue).subtract(grandTotalCostAndExp);
                variance = grandTotalAsset.subtract(liaEqui);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row assetTotalRow = sheet.createRow(rowNum++);
                assetTotalRow.createCell(0).setCellValue("ASSET TOTALS:");
                assetTotalRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleGrandTotalAsset = Double.parseDouble(String.valueOf(grandTotalAsset));
                assetTotalRow.createCell(7).setCellValue(doubleGrandTotalAsset);
                assetTotalRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row liabEquiTotalRow = sheet.createRow(rowNum++);
                liabEquiTotalRow.createCell(0).setCellValue("LIABILITIES & EQUITIES TOTALS:");
                liabEquiTotalRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleGrandTotalLiabEqui = Double.parseDouble(String.valueOf(grandTotalLiabAndEqui));
                liabEquiTotalRow.createCell(7).setCellValue(doubleGrandTotalLiabEqui);
                liabEquiTotalRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row netCurrTotalRow = sheet.createRow(rowNum++);
                netCurrTotalRow.createCell(0).setCellValue("NET INCOME - CURRENT MONTH:");
                netCurrTotalRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleNetCurr = Double.parseDouble(String.valueOf(netIncomeCurrMonth));
                netCurrTotalRow.createCell(7).setCellValue(doubleNetCurr);
                netCurrTotalRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row netPrevTotalRow = sheet.createRow(rowNum++);
                netPrevTotalRow.createCell(0).setCellValue("NET INCOME - PREVIOUS MONTH:");
                netPrevTotalRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleNetPrev = Double.parseDouble(String.valueOf(netIncomePrevMonth));
                netPrevTotalRow.createCell(7).setCellValue(doubleNetPrev);
                netPrevTotalRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row yearToDateIncomeRow = sheet.createRow(rowNum++);
                yearToDateIncomeRow.createCell(0).setCellValue("YEAR TO DATE INCOME:");
                yearToDateIncomeRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleYearToDateIncome = Double.parseDouble(String.valueOf(yearToDateIncome));
                yearToDateIncomeRow.createCell(7).setCellValue(doubleYearToDateIncome);
                yearToDateIncomeRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row totalLiaEquiIncomeRow = sheet.createRow(rowNum++);
                totalLiaEquiIncomeRow.createCell(0).setCellValue("TOTAL LIABILITIES, EQUITIES & INCOME:");
                totalLiaEquiIncomeRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleTotalLiaEquiIncome = Double.parseDouble(String.valueOf(totalIncome));
                totalLiaEquiIncomeRow.createCell(7).setCellValue(doubleTotalLiaEquiIncome);
                totalLiaEquiIncomeRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));

                Row varianceRow = sheet.createRow(rowNum++);
                varianceRow.createCell(0).setCellValue("VARIANCE:");
                varianceRow.getCell(0).setCellStyle(totalSummaryCellStyle);

                double doubleVariance = Double.parseDouble(String.valueOf(variance));
                varianceRow.createCell(7).setCellValue(doubleVariance);
                varianceRow.getCell(7).setCellStyle(totalSummaryCellAmountStyle);

                Row signatoryLabelRow = sheet.createRow(rowNum++);
                signatoryLabelRow.createCell(0).setCellValue("Prepared By:");
                signatoryLabelRow.getCell(0).setCellStyle(signatoryCellStyle);

                rowNum++;
                rowNum++;
                rowNum++;

                Row signatoryNameRow = sheet.createRow(rowNum++);
                signatoryNameRow.createCell(0).setCellValue(String.valueOf(reportData.get("preparedBy")));
                signatoryNameRow.getCell(0).setCellStyle(signatoryNameCellStyle);

                Row signatoryPosRow = sheet.createRow(rowNum);
                signatoryPosRow.createCell(0).setCellValue(String.valueOf(reportData.get("preparedByPos")));
                signatoryPosRow.getCell(0).setCellStyle(signatoryPosCellStyle);

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
