package ru.khavantsev.ziczac.navigator.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;

public class StringUtils {
    public static String metersForDisplay(int meters) {
        String text;
        if (meters < 1000) {
            text = String.valueOf(meters) + "m";
        } else {

            BigDecimal bd = BigDecimal.valueOf(meters).divide(new BigDecimal(1000));
            bd.setScale(0, RoundingMode.CEILING );

            double kilometers = bd.doubleValue();
            text = ZzMath.round(kilometers, 1) + "km";
        }

        return text;
    }
}
