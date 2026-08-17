package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.CashflowItem;
import com.noreco1.fireflyv2.model.UnitMeasure;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.controller.response.AccountDto;

import java.math.BigDecimal;

public class StringFormatter {

    public static String ucFirst(String text) {
        if (text.trim().length() == 0) {
            return text;
        } else {
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }

    public static String buildAccountCode(String accountTypeCode, String groupCode,
                                          String GLAccount, String SLAccount,
                                          String auxAccount) {

        String dash = "-";
        String accountCode = accountTypeCode + groupCode + dash + GLAccount + dash
                + SLAccount + dash + auxAccount;
        return accountCode;
    }

    public static String buildAccountCode(String GLAccount, String SLAccount,
                                          String auxAccount) {

        String dash = "-";
        String accountCode = GLAccount + dash + SLAccount + dash  + auxAccount;
        return accountCode;
    }

    public static String removeBaseFromRoute(String route) {
        if (route != null) {
            int startIndex = route.indexOf("#") + 1;
            return route.substring(startIndex, route.length());
        } else {
            return "";
        }
    }

    public static String replaceRouteParamWithPlaceholder(String uri, String... params) {
        if (params.length > 0) {
            for (String p : params) {
                uri = uri.replace(p, GlobalConstant.ROUTE_PARAM_PLACEHOLDER);
            }
        }
        return uri;
    }

    public static String[] breakTIN(String tin) {
        String[] tinArr = {"", "", "", ""};
        if (tin != null) {
            tinArr  = tin.split("-");
        }
        return tinArr;
    }

    public static String[] breakTIN(String tin, String separator) {

        String[] tinArr = {"", "", "", ""};

        if (tin != null && tin.length() > 0) {

            String[] tinParts = tin.split(separator);
            if (tinParts.length > 0) {
                tinArr[0] = tinParts[0];
            }
            if (tinParts.length > 1) {
                tinArr[1] = tinParts[1];
            }
            if (tinParts.length > 2) {
                tinArr[2] = tinParts[2];
            }
            if (tinParts.length > 3) {
                tinArr[3] = tinParts[3];
            }
        }
        return tinArr;
    }

    public static String getFilenameExtension(String filename) {
        if (filename != null) {
            int startOfExt = filename.lastIndexOf(".");
            return filename.substring(startOfExt);
        }
        return null;
    }

    public static String getValueOrBlank(Object o) {
        return (o != null) ? String.valueOf(o):"";
    }

    public static String getValueOrZero(Object o) {
        return (o != null) ? String.valueOf(o):"0";
    }

    public static Integer getIntValueOrZero(Object o) {
        try {

            if(o != null) {
                if( o instanceof Integer) {
                    return (Integer) o;
                }

                if( o instanceof String) {
                    return Integer.parseInt(o.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getMonthFromNumber(Integer n) {

        if (n != null) {
            String[] months = {
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
            };
            return months[n]; // January at 0
        }
        return null;
    }

    public static String getStrElseBlank(String str) {
        return str == null ? "":str.trim();
    }

    public static String getStrElseBlank(Object str) {
        return str == null ? "":str.toString().trim();
    }

    public static String accountSearchText(Account a) {
        return (a.getCode()+ " " + a.getTitle());
    }

    public static String accountSearchText(AccountDto a) {
        return (a.getCode()+ " " + a.getTitle());
    }

    public static String accountSearchText(String code, String title) {
        return (code+ " " + title);
    }

    public static String statusIdToStr(Integer id) {
        if(id != null)  {

            DocumentStatus documentStatus = DocumentStatus.typeFromInt(id);

            if(documentStatus != null) {
                String s = documentStatus.toString();
                return s.replace("_", " ");
            }
        }

        return null;
    }

    public static String statusToHuman(String str) {
        String result = "";

        String[] strings = str.split("_");
        if(strings.length > 0) {
            for (String s : strings) {

                String ucFirst = StringFormatter.ucFirst(s.toLowerCase());

                result += " " + ucFirst;
            }
        }

        return result.trim();
    }

    public static String blankOrNewLine(int counter, int length) {

        String returnVal = "";

        if(counter < length - 1) {
            returnVal = "\n";
        }
        return returnVal;

    }

    public static String addSpacingBetweenChars(String str, int numOfSpaces) {

        if(Checker.isStringNullAndEmpty(str)) return "";

        String newStr = "";
        for (int i=0; i<str.length(); i++) {

            char c = str.charAt(i);

            if(i == 0) {
                newStr += c;
            } else {

                for (int k=0; k<numOfSpaces; k++) {
                    newStr += " ";
                }

                newStr += c;

            }
        }

        return newStr.trim();
    }

    public static String getUnitCode(UnitMeasure unitMeasure, Integer quantity) {

        if(unitMeasure != null) {
            if(quantity > 1 && !Checker.isStringNullAndEmpty(unitMeasure.getPlural())) {
                return unitMeasure.getPlural();
            } else {
                return unitMeasure.getCode();
            }
        }

        return "";
    }

    public static String getUnitCode(UnitMeasure unitMeasure, BigDecimal quantity) {

        if(unitMeasure != null) {
            if(quantity.compareTo(BigDecimal.ONE) > 0 && !Checker.isStringNullAndEmpty(unitMeasure.getPlural())) {
                return unitMeasure.getPlural();
            } else {
                return unitMeasure.getCode();
            }
        }

        return "";
    }

    public static String name(Object last, Object first, Object middle, Object extension) {
        String name = "";
        if(last != null) {
            name = last.toString();
            if(!isBlank(first)) {
                name += ", "+first.toString()+" "+ blankIfNull(middle)+" "+ blankIfNull(extension);
            }
        }

        return name.trim();
    }

    public static String blankIfNull(Object str) {
        if(str == null) {
            return "";
        }
        return str.toString().trim();
    }

    public static boolean isBlank(Object object) {
        return object == null || (object instanceof String && object.toString().isEmpty());
    }

    public static String toFullTextSearchFormat(String str) {

        StringBuilder queryBuilder = new StringBuilder();

        if(str != null) {
            String[] tokens = str.split(" ");
            if(tokens.length > 0) {
                for (String token:tokens) {
                    if(token.trim().length() > 0) {
                        queryBuilder.append(token).append("*");
                    }
                }
            }
        }

        return queryBuilder.toString();

    }

    public static boolean numbersOnly(String str){

        if(str != null) {
            String pattern = "((-|\\+)?[0-9]+(\\.[0-9]+)?)+";
            return (str.matches(pattern));
        }

        return false;

    }

    public static String getParentCashflowItemName(CashflowItem parentItem) {
        if (parentItem != null) {
            if (parentItem.getParentCashflowItem() != null) {
                return parentItem.getName() + "-->" + getParentCashflowItemName(parentItem.getParentCashflowItem());
            } else {
                return parentItem.getName();
            }
        } else {
            return "";
        }
    }

    public static String reverseString(String parentCashFlowItemName) {

        // Split the string into an array of substrings using "-"
        String[] substrings = parentCashFlowItemName.split("-->");

        // Reverse the array
        for (int i = 0; i < substrings.length / 2; i++) {
            String temp = substrings[i];
            substrings[i] = substrings[substrings.length - 1 - i];
            substrings[substrings.length - 1 - i] = temp;
        }

        // Join the reversed array back into a single string using "-"

        return String.join("-->", substrings);

    }

}
