package com.noreco1.fireflyv2.common.helpers;

import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import com.noreco1.fireflyv2.common.GlobalConstant;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportUtil {
    public static HashMap setupSharedReportHeaders(HttpServletRequest request) {

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("COMP_NAME", "NEGROS ORIENTAL I ELECTRIC COOPERATIVE, INC");
        parameters.put("COMP_ADDR", "Tinaogan, Bindoy, Negros Oriental, Philippines");
        parameters.put("COMP_CONTACT", "");
        parameters.put("PURCHASING_CONTACT", "");
        parameters.put("LOGO_PATH", request.getSession().getServletContext().getRealPath("/resources/images/main/noreco1-logo.png"));
        java.net.URL subreportUrl = ReportUtil.class.getResource("/jasper/vouchers/sub_reports/");
        parameters.put("SUBREPORT_DIR", subreportUrl != null ? subreportUrl.toString() + "/" : "jasper/vouchers/sub_reports/");
        return parameters;
    }

    public static HashMap setupSharedReportHeaders() {

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("COMP_NAME", "Your name");
        parameters.put("COMP_ADDRESS", "Your address");
        parameters.put("COMP_CONTACT", "Your contact");

        return parameters;
    }

    public static List<AbstractColumn> getTrialBalanceCols(Style amountStyle) {
        List<AbstractColumn> cols = new ArrayList<>();

        AbstractColumn columnCode = ColumnBuilder.getNew()
                .setColumnProperty("code", String.class.getName())
                .setTitle("Account Code")
                .setWidth(70)
                .build();

        AbstractColumn columnTitle = ColumnBuilder.getNew()
                .setColumnProperty("title", String.class.getName())
                .setTitle("Account Title")
                .setWidth(200)
                .build();

        AbstractColumn columnAllocationFactor = ColumnBuilder.getNew()
                .setColumnProperty("allocationFactor", String.class.getName())
                .setTitle("Factor")
                .setWidth(25)
                .build();

        AbstractColumn columnPerAcam = ColumnBuilder.getNew()
                .setColumnProperty("perAcam", String.class.getName())
                .setTitle("per ACAM")
                .setWidth(25)
                .build();

        AbstractColumn columnTotalAmount = ColumnBuilder.getNew()
                .setColumnProperty("totalAmount", BigDecimal.class.getName())
                .setTitle("Total Company")
                .setWidth(70)
                .setStyle(amountStyle)
                .build();

        cols.add(columnCode);
        cols.add(columnTitle);
        cols.add(columnPerAcam);
        cols.add(columnAllocationFactor);
        cols.add(columnTotalAmount);

        return cols;
    }

    public static List<AbstractColumn> getBalanceSheetCols(Style amountStyle) {
        List<AbstractColumn> cols = new ArrayList<>();

        AbstractColumn columnTitle = ColumnBuilder.getNew()
                .setColumnProperty("title", String.class.getName())
                .setTitle("Particular")
                .setWidth(200)
                .build();

        cols.add(columnTitle);

        return cols;
    }

    public static List<AbstractColumn> getBalanceSheetBSUPCols(Style amountStyle) {
        List<AbstractColumn> cols = new ArrayList<>();

        AbstractColumn columnTitle = ColumnBuilder.getNew()
                .setColumnProperty("title", String.class.getName())
                .setTitle("Particular")
                .setWidth(190)
                .build();

        cols.add(columnTitle);

        return cols;
    }

    public static List<AbstractColumn> getTrialBalanceNEACols(Style amountStyle) {
        List<AbstractColumn> cols = new ArrayList<>();

        AbstractColumn columnCode = ColumnBuilder.getNew()
                .setColumnProperty("code", String.class.getName())
                .setTitle("Account Code")
                .setWidth(70)
                .build();

        AbstractColumn columnTitle = ColumnBuilder.getNew()
                .setColumnProperty("title", String.class.getName())
                .setTitle("Account Title")
                .setWidth(120)
                .build();

        AbstractColumn columnTotalAmount = ColumnBuilder.getNew()
                .setColumnProperty("totalAmount", BigDecimal.class.getName())
                .setTitle("Total Amount")
                .setWidth(70)
                .setStyle(amountStyle)
                .build();

        cols.add(columnCode);
        cols.add(columnTitle);
        cols.add(columnTotalAmount);

        return cols;
    }
}
