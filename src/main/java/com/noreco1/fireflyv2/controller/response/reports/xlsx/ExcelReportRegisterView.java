package com.noreco1.fireflyv2.controller.response.reports.xlsx;

import com.noreco1.fireflyv2.common.helpers.ExcelUtils;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.controller.response.reports.CommonRegisterDetail;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class ExcelReportRegisterView extends AbstractXlsView{

    @Override
    protected void buildExcelDocument(Map<String, Object> map, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {

            @SuppressWarnings("unchecked")
            Map registerData = (Map) map.get("registerData");

            List<CommonRegisterDetail> commonRegisterDetails = (List<CommonRegisterDetail>) registerData.get("commonRegisterDetails");
            List<RegisterRecapDetail> recap = (List<RegisterRecapDetail>) registerData.get("recapDetails");

            if (commonRegisterDetails.size() != 0){

                String reportType = commonRegisterDetails.get(0).getReportType();

                DocumentType documentType = DocumentType.valueOf(DocumentType.class, reportType);

                String excelFileName = documentType.getDescription();
                excelFileName += " Register";

                Sheet sheet = workbook.createSheet(excelFileName);
                sheet.setDefaultColumnWidth(40);

                ExcelUtils.generateHeaderData(workbook, sheet, map, reportType);

                ExcelUtils.generateRowData(workbook, sheet, commonRegisterDetails, map, reportType, recap);

                response.setHeader("Content-disposition", "attachment; filename=" + excelFileName.replace(" ", "") + ".xls");

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
