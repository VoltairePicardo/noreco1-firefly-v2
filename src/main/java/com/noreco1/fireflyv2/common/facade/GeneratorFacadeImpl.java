package com.noreco1.fireflyv2.common.facade;

import jakarta.persistence.*;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.EntityAccountNumber;
import com.noreco1.fireflyv2.model.Transaction;
import com.noreco1.fireflyv2.model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TSI Admin on 5/5/2015.
 */

@Component
public class GeneratorFacadeImpl implements GeneratorFacade {

    @Autowired
    AuthenticationFacade authenticationFacade;

    @PersistenceContext(unitName = "mysql")
    EntityManager entityManager;

    @Override
    public Integer entityAccountNumber() {
        User user = authenticationFacade.getLoggedIn();

        EntityAccountNumber accountNumber = EntityAccountNumber.builder().createdBy(user).build();

        entityManager.persist(accountNumber);
        entityManager.flush();

        return accountNumber.getId();
    }

    @Override
    public Transaction transaction() {
        User user = authenticationFacade.getLoggedIn();

        Transaction trans = new Transaction(user, new Date());

        entityManager.persist(trans);
        entityManager.flush();

        return trans;
    }

    @Override
    public String voucherCode(String prefix, String latestCode, Date voucherDate) {
        /*String code = null;
        NumberFormat ctrFormatter = new DecimalFormat("0000");

        try {
            String thisYear = GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate);

            if (latestCode == null || latestCode.equalsIgnoreCase("")) {  // maybe first time
                code = prefix + "-" + thisYear + "-" + "0001";
            } else {
                String[] latestCodeArr = latestCode.split("-");
                prefix = latestCodeArr[0];
                String counterPart = latestCodeArr[2];  // counter
                int intCounterPart = Integer.parseInt(counterPart); // convert counter

                intCounterPart++; // just another voucher of the year
                counterPart = ctrFormatter.format(intCounterPart); // format counter

                code = prefix + "-" + thisYear + "-" + counterPart;
            }
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return code;*/

        return this.voucherCodeWithMonth(prefix, latestCode, voucherDate);
    }

    @Override
    public String voucherCodeWithMonth(String prefix, String latestCode, Date voucherDate) {
        String code = null;
        NumberFormat ctrFormatter = new DecimalFormat("00000");

        SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy-MM");
        try {

            if (latestCode.equalsIgnoreCase("")) {
                String newYear = simpleDateformat.format(voucherDate);
                String newMonth = newYear.substring(5, newYear.length());
                newYear = newYear.substring(0, newYear.indexOf("-"));
                prefix += "-" + newYear + "-" + newMonth + "-" + "00001";
                return prefix;
            }

            String[] parts = latestCode.split("-");
            String docPart = prefix;  // form e.g. RR,APV,PO
            String yearPart = parts[2];  // year
            String monthPart = parts[3];
            int intYearPart = Integer.parseInt(yearPart); // convert year
            int intMonthPart = Integer.parseInt(monthPart);
            String counterPart = null;  // counter
            if(parts.length == 4){ // if code has no month part make counter part to zero and month part to current month
                String newYear = simpleDateformat.format(voucherDate);
                String newMonth = newYear.substring(5, newYear.length());
                intMonthPart = Integer.parseInt(newMonth);
                counterPart = "00000";
            } else {
                counterPart = parts[4];
            }
            int intCounterPart = Integer.parseInt(counterPart); // convert counter

            String newYear = simpleDateformat.format(voucherDate);
            newYear = newYear.substring(0, newYear.indexOf("-"));
            int intNewYear = Integer.parseInt(newYear);

            String newMonth = simpleDateformat.format(voucherDate);
            newMonth = newMonth.substring(newMonth.indexOf("-") + 1, newMonth.length());
            int intNewMonth = Integer.parseInt(newMonth);
            String strNewMonth = intNewMonth + "";

            if (newMonth.startsWith("0")) {
                strNewMonth = "0" + strNewMonth;
            }

            if (intMonthPart < intNewMonth) {
                if (newMonth.startsWith("0")) {
                    strNewMonth = "0" + intNewMonth + "";
                } else {
                    strNewMonth = intNewMonth + "";
                }
            }

            if (intYearPart < intNewYear) {
                counterPart = "00001";
                intYearPart = intNewYear;
            } else {
                intCounterPart++; // increment couter as new
                counterPart = ctrFormatter.format(intCounterPart); // format counter
            }
            code = docPart + "-" + intYearPart + "-" + strNewMonth + "-" + counterPart;
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return code;
    }

    @Override
    public String voucherCodeWithMonth2(String prefix, String latestCode, Date voucherDate) {
        String code = null;
        NumberFormat ctrFormatter = new DecimalFormat("000000");

        SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy-MM");
        try {

            if (latestCode.equalsIgnoreCase("")) {
                String newYear = simpleDateformat.format(voucherDate);
                String newMonth = newYear.substring(5, newYear.length());
                newYear = newYear.substring(0, newYear.indexOf("-"));
                prefix += "-" + newYear + "-" + newMonth + "-" + "00001";
                return prefix;
            }

            String[] parts = latestCode.split("-");
            String docPart = prefix;  // form e.g. RR,APV,PO
            String yearPart = parts[2];  // year
            String monthPart = parts[3];
            int intYearPart = Integer.parseInt(yearPart); // convert year
            int intMonthPart = Integer.parseInt(monthPart);
            String counterPart = null;  // counter
            if(parts.length == 4){ // if code has no month part make counter part to zero and month part to current month
                String newYear = simpleDateformat.format(voucherDate);
                String newMonth = newYear.substring(5, newYear.length());
                intMonthPart = Integer.parseInt(newMonth);
                counterPart = "00000";
            } else {
                counterPart = parts[4];
            }
            int intCounterPart = Integer.parseInt(counterPart); // convert counter

            String newYear = simpleDateformat.format(voucherDate);
            newYear = newYear.substring(0, newYear.indexOf("-"));
            int intNewYear = Integer.parseInt(newYear);

            String newMonth = simpleDateformat.format(voucherDate);
            newMonth = newMonth.substring(newMonth.indexOf("-") + 1, newMonth.length());
            int intNewMonth = Integer.parseInt(newMonth);
            String strNewMonth = intNewMonth + "";

            if (newMonth.startsWith("0")) {
                strNewMonth = "0" + strNewMonth;
            }

            if (intMonthPart < intNewMonth) {
                if (newMonth.startsWith("0")) {
                    strNewMonth = "0" + intNewMonth + "";
                } else {
                    strNewMonth = intNewMonth + "";
                }
            }

            if (intYearPart < intNewYear) {
                counterPart = "00001";
                intYearPart = intNewYear;
            } else {
                intCounterPart++; // increment couter as new
                counterPart = ctrFormatter.format(intCounterPart); // format counter
            }
            code = docPart + "-" + intYearPart + "-" + strNewMonth + "-" + counterPart;
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return code;
    }

    public String voucherCodeNoOffice(String prefix, String latestCode, Date voucherDate, String counterPad) {
        String code = null;
        NumberFormat ctrFormatter = new DecimalFormat(counterPad+"0");

        SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy-MM");
        try {

            if (latestCode.equalsIgnoreCase("")) {
                return this.generateCode(prefix, simpleDateformat, voucherDate, counterPad);
            }

            String[] parts = latestCode.split("-");
            String docPart = prefix;  // form e.g. RR,APV,PO
            String yearPart = parts[1];  // year
            String monthPart = parts[2];
            int intYearPart = Integer.parseInt(yearPart); // convert year
            int intMonthPart = Integer.parseInt(monthPart);
            String counterPart = parts[3];  // counter

            int intCounterPart = Integer.parseInt(counterPart); // convert counter

            String newYear = simpleDateformat.format(voucherDate);
            newYear = newYear.substring(0, newYear.indexOf("-"));
            int intNewYear = Integer.parseInt(newYear);

            String newMonth = simpleDateformat.format(voucherDate);
            newMonth = newMonth.substring(newMonth.indexOf("-") + 1, newMonth.length());
            int intNewMonth = Integer.parseInt(newMonth);
            String strNewMonth = intNewMonth + "";

            if (newMonth.startsWith("0")) {
                strNewMonth = "0" + strNewMonth;
            }

            if (intMonthPart < intNewMonth) {
                if (newMonth.startsWith("0")) {
                    strNewMonth = "0" + intNewMonth + "";
                } else {
                    strNewMonth = intNewMonth + "";
                }
            }

            if (intYearPart < intNewYear) {
                counterPart = counterPad+"1";
                intYearPart = intNewYear;
            } else {
                intCounterPart++; // increment counter as new
                counterPart = ctrFormatter.format(intCounterPart); // format counter
            }
            code = docPart + "-" + intYearPart + "-" + strNewMonth + "-" + counterPart;
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return code;
    }

    private String generateCode(String prefix, SimpleDateFormat simpleDateformat, Date voucherDate, String counterPad) {

        String code = prefix;

        String newYear = simpleDateformat.format(voucherDate);
        String newMonth = newYear.substring(5, newYear.length());
        newYear = newYear.substring(0, newYear.indexOf("-"));
        code += "-" + newYear + "-" + newMonth + "-" + counterPad+"1";
        return code;
    }

    private String generateCode(String prefix, SimpleDateFormat simpleDateformat, Date voucherDate, String town, String counterPad) {

        String code = prefix;

        String newYear = simpleDateformat.format(voucherDate);
        String newMonth = newYear.substring(5, newYear.length());
        newYear = newYear.substring(0, newYear.indexOf("-"));
        code += "-" + newYear + "-" + newMonth + "-" + town.toUpperCase() + "-" + counterPad+"1";
        return code;
    }

    @Override
    public String quotationCode(String prefix, String latestCode, Date voucherDate) {
        return this.voucherCodeWithMonth(prefix, latestCode, voucherDate);
    }

    @Override
    public String voucherCodeWithTown(String prefix, String latestCode, Date voucherDate, String townName, String counterPad) {
        String code = null;
        NumberFormat ctrFormatter = new DecimalFormat(counterPad+"0");

        SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy-MM");
        try {

            if (latestCode.equalsIgnoreCase("")) {
                return this.generateCode(prefix, simpleDateformat, voucherDate, townName, counterPad);
            }

            String[] parts = latestCode.split("-");
            String docPart = prefix;
            String yearPart = parts[1];  // year
            String monthPart = parts[2];
            int intYearPart = Integer.parseInt(yearPart); // convert year
            int intMonthPart = Integer.parseInt(monthPart);
            String counterPart = parts[4];  // counter

            int intCounterPart = Integer.parseInt(counterPart); // convert counter

            String newYear = simpleDateformat.format(voucherDate);
            newYear = newYear.substring(0, newYear.indexOf("-"));
            int intNewYear = Integer.parseInt(newYear);

            String newMonth = simpleDateformat.format(voucherDate);
            newMonth = newMonth.substring(newMonth.indexOf("-") + 1, newMonth.length());
            int intNewMonth = Integer.parseInt(newMonth);
            String strNewMonth = intNewMonth + "";

            if (newMonth.startsWith("0")) {
                strNewMonth = "0" + strNewMonth;
            }

            if (intMonthPart < intNewMonth) {
                if (newMonth.startsWith("0")) {
                    strNewMonth = "0" + intNewMonth + "";
                } else {
                    strNewMonth = intNewMonth + "";
                }
            }

            if (intYearPart < intNewYear) {
                counterPart = counterPad+"1";
                intYearPart = intNewYear;
            } else {
                intCounterPart++; // increment counter as new
                counterPart = ctrFormatter.format(intCounterPart); // format counter
            }
            code = docPart + "-" + intYearPart + "-" + strNewMonth + "-" + townName.toUpperCase() + "-" + counterPart;
        } catch (Exception ex) {
            Logger.getRootLogger().error(ex.getMessage());
        }
        return code;
    }

    @Override
    public StringBuilder budgetLineItemCodeForDepartmentAndDivisionOnly(String latestCode, String department, String division, Integer year) {
        StringBuilder code = new StringBuilder();

        try {
            if (latestCode == null || latestCode.isEmpty()) {
                // If latestCode is empty, start with "001"
                return code.append(department)
                        .append("-")
                        .append(division.trim().replace(" ", ""))
                        .append("-")
                        .append(year)
                        .append("-")
                        .append("001");
            } else {
                // Extract the last part (counter) from latestCode
                String[] parts = latestCode.split("-");
                String currentCounter = parts[parts.length - 1]; // e.g., "001"

                // Parse the current counter as an integer, increment it, and format it as a 3-digit string
                int nextCounter = Integer.parseInt(currentCounter) + 1;
                String formattedCounter = String.format("%03d", nextCounter);

                // Generate the new code with the incremented counter
                return code.append(department)
                        .append("-")
                        .append(division.trim().replace(" ", ""))
                        .append("-")
                        .append(year)
                        .append("-")
                        .append(formattedCounter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return code;
    }

    @Override
    public StringBuilder budgetLineItemCodeForDepartmentOnly(String latestCode, String department, Integer year) {
        StringBuilder code = new StringBuilder();

        try {
            if (latestCode == null || latestCode.isEmpty()) {
                // If latestCode is empty, start with "001"
                return code.append(department)
                        .append("-")
                        .append(year)
                        .append("-")
                        .append("001");
            } else {
                // Extract the last part (counter) from latestCode
                String[] parts = latestCode.split("-");
                String currentCounter = parts[parts.length - 1]; // e.g., "001"

                // Parse the current counter as an integer, increment it, and format it as a 3-digit string
                int nextCounter = Integer.parseInt(currentCounter) + 1;
                String formattedCounter = String.format("%03d", nextCounter);

                // Generate the new code with the incremented counter
                return code.append(department)
                        .append("-")
                        .append(year)
                        .append("-")
                        .append(formattedCounter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return code;
    }

}
