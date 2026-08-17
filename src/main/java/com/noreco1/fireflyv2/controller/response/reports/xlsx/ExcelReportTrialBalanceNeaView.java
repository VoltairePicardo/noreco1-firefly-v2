package com.noreco1.fireflyv2.controller.response.reports.xlsx;

import com.noreco1.fireflyv2.common.helpers.ExcelUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI on 4/5/2023.
 */
public class ExcelReportTrialBalanceNeaView extends AbstractXlsView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {

            @SuppressWarnings("unchecked")
            Map reportData = (Map) model.get("reportData");

            List<Map> details = (List<Map>) reportData.get("details");

            if (details.size() != 0){

                String excelFileName = "Trial-Balance-NEA";

                Sheet sheet = workbook.createSheet(excelFileName);
                sheet.setDefaultColumnWidth(40);

                ExcelUtils.generateHeaderDataTB(workbook, sheet, model, reportData);

                ExcelUtils.generateRowDataTB(workbook, sheet, details, model, reportData);

                response.setHeader("Content-disposition", "attachment; filename=" + excelFileName.replace(" ", "") + ".xls");

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
