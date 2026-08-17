package com.noreco1.fireflyv2.common.helpers;

import java.math.BigDecimal;

public class CurrencyIntoWords {

    public static String convert(BigDecimal amount) {
        return NumberToWord.convert(amount);
    }
}
