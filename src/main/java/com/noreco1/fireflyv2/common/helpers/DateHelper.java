package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.model.Voucher;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateHelper {

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    public static Map getTimeDiff(Date d1, Date d2) {

        long l = (d1.getTime() - d2.getTime()) / 1000;
        int hours = (int) (l / 3600);
        l = l % 3600;
        int mins = (int) (l / 60);
        long secs = l % 60;

        Map m = new HashMap();
        m.put("hours", hours);
        m.put("minutes", mins);
        m.put("seconds", secs);

        return m;
    }

    /**
     *
     * @param dateOfBirth
     * @return
     */
    public static int getAge(Date dateOfBirth) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR)) age--;
        return age;
    }

    public static int getThisYear() {
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        return now.get(Calendar.YEAR);
    }

    public static int getYear(String yearStr) {
        Integer thisYear = getThisYear();
        try {
            thisYear  = Integer.parseInt(yearStr);
        } finally {
            return thisYear;
        }
    }

    public static Date lastDateOfMonth(String strDate) throws ParseException {
        Calendar c = DateHelper.parseDate(strDate);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static Date firstDateOfMonth(String strDate) throws ParseException {
        Calendar c = DateHelper.parseDate(strDate);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static Date firstDateOfMonth(Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static java.sql.Date firstDateOfMonth(java.sql.Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));

        return new java.sql.Date(c.getTimeInMillis());
    }

    public static Date yesterday(Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        return c.getTime();
    }

    public static java.sql.Date yesterday(java.sql.Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);

        return new java.sql.Date(c.getTimeInMillis());
    }

    public static java.sql.Date lastMonth(java.sql.Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);

        return new java.sql.Date(c.getTimeInMillis());
    }

    public static java.sql.Date lastDateOfPreviousMonth(java.sql.Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);

        return new java.sql.Date(c.getTimeInMillis());
    }

    public static java.sql.Date firstDateOfPreviousMonth(java.sql.Date date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DATE, 1);

        return new java.sql.Date(c.getTimeInMillis());
    }

    public static String dateToSQL(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public static String fistDateOfMonthSQLFormat(String strDate) throws ParseException {
        Date date = DateHelper.firstDateOfMonth(strDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public static String lastDateOfMonthSQLFormat(String strDate) throws ParseException {
        Date date = DateHelper.lastDateOfMonth(strDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public static int toYear(String strDate) throws ParseException {
       Calendar cal = DateHelper.parseDate(strDate);
       return cal.get(Calendar.YEAR);
    }

    public static Calendar parseDate(String strDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = dateFormat.parse(strDate);
        Calendar c = Calendar.getInstance();
        c.setTime(convertedDate);

        return c;
    }

    public static Date strToDate(String strDate, String existingFormat) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(existingFormat);
            Date date = dateFormat.parse(strDate);
            return date;
        }catch (Exception ex) {
            Debug.print(ex.getMessage());
        }
        return null;
    }

    public static Date strToDateOrToday(String strDate, String existingFormat) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(existingFormat);
            return dateFormat.parse(strDate);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Date();
    }

    public static Map thisWeekRange() {
        Map dateMap = new HashMap();

        try {
            // set the date
            Calendar cal = Calendar.getInstance();

            // "calculate" the start date of the week
            Calendar first = (Calendar) cal.clone();
            first.add(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));

            // and add six days to the end date
            Calendar last = (Calendar) first.clone();
            last.add(Calendar.DAY_OF_YEAR, 6);

            dateMap.put("start", first.getTime());
            dateMap.put("end", last.getTime());

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateMap;
    }

    public static String dateToLongDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return dateFormat.format(date);
    }

    public static int thisYear(Date date) {

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c.get(Calendar.YEAR);
    }

    public static int monthNum(Date date) {

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c.get(Calendar.MONTH);
    }

    public static Long mills(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static java.sql.Date startOfTime() {
        String jan1 ="1970-01-01";

        Calendar cal = null;
        try {
            cal = parseDate(jan1);
        } catch (ParseException e) {
            e.printStackTrace();
            cal = Calendar.getInstance();
        }
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public static Map getVoucherDateOrNow(Voucher voucher) {

        Map d = new HashMap();

        if (voucher != null) {
            d.put("date", DateHelper.dateToSQL(voucher.getVoucherDate()));
        } else {
            d.put("date", new java.sql.Date(new Date().getTime()));
        }

        return d;
    }

    public static String formatDateRange(String from, String to) {
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
        DateFormat humanFormat = new SimpleDateFormat("MMMM dd, yyyy");
        try {
            Date dateFrom = formatter.parse(from);
            Date dateTo = formatter.parse(to);

            return humanFormat.format(dateFrom) + " to " + humanFormat.format(dateTo);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Date getServerDate() {
        return new Date();
    }

    public static double getDayDifferential(Date firstDate, Date secondDate){
        long differenceInTime = Math.abs(firstDate.getTime() - secondDate.getTime());
        return Math.floor(differenceInTime / (1000 * 60 * 60 * 24));
    }

}
