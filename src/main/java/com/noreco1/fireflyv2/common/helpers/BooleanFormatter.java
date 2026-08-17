package com.noreco1.fireflyv2.common.helpers;

public class BooleanFormatter {
    public static Boolean convertIntToBoolean(Integer number) {
        if(number != null){
            if(number == 1){
                return true;
            }
        }
        return false;
    }
}
