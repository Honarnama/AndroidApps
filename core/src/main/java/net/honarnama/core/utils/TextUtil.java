package net.honarnama.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by elnaz on 2/29/16.
 */
public class TextUtil {

    public static String convertEnNumberToFa(String enNo) {
        char[] farsiChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < enNo.length(); i++) {
            if (Character.isDigit(enNo.charAt(i))) {
                builder.append(farsiChars[(int) (enNo.charAt(i)) - 48]);
            } else {
                builder.append(enNo.charAt(i));
            }
        }
        return builder.toString();
    }

    public static NumberFormat getPriceNumberFormmat(Locale locale){
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        DecimalFormat format = ((DecimalFormat)NumberFormat.getInstance(locale));
        format.setGroupingSize(3);
        if("fa".equalsIgnoreCase(lang) && !"TJ".equalsIgnoreCase(country))
            format.setDecimalFormatSymbols(new DecimalFormatSymbols());
        return format;
    }
}
