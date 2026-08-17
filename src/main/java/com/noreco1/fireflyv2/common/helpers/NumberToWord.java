package com.noreco1.fireflyv2.common.helpers;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class NumberToWord {

    private static final String[] tensNames = new String[]{"", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"};
    private static final String[] numNames = new String[]{" ", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve", " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"};

    private static String convertLessThanOneThousand(int number) {
        String soFar;
        if(number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;
            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }

        return number == 0?soFar:numNames[number] + " Hundred" + soFar;
    }

    public static String convert(BigDecimal number) {
        String strNum = number.toString();
        int decimalDigit = 0;
        String strDecimalDigit = null;
        String[] numberArr = strNum.split("\\.");
        long wholeDigit = Long.parseLong(numberArr[0]);
        if(numberArr.length > 1) {
            decimalDigit = Integer.parseInt(numberArr[1]);
            strDecimalDigit = numberArr[1].toString();
        }

        if(wholeDigit <= 0L && decimalDigit <= 0) {
            return " ";
        } else {
            String snumber = Long.toString(wholeDigit);
            String mask = "000000000000";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(wholeDigit);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            String tradBillions;
            switch(billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }

            String tradMillions;
            switch(millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }

            String result = tradBillions + tradMillions;
            String tradHundredThousands;
            switch(hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }

            result = result + tradHundredThousands;
            String tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            if(numberArr.length > 1 && decimalDigit > 0) {
                if(wholeDigit <= 0L) {
                    result = strDecimalDigit + "/100 Cents";
                } else {
                    result += " and " + strDecimalDigit + "/100 Cents";
                }
            }

            return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
        }
    }
}
