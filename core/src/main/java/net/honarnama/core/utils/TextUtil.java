package net.honarnama.core.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by elnaz on 2/29/16.
 */
public class TextUtil {

    public static String convertEnNumberToFa(String rawString) {
//        char[] farsiChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        String formattedString;
        formattedString = rawString.replace("0", "۰");
        formattedString = formattedString.replace("1", "۱");
        formattedString = formattedString.replace("2", "۲");
        formattedString = formattedString.replace("3", "۳");
        formattedString = formattedString.replace("4", "۴");
        formattedString = formattedString.replace("5", "۵");
        formattedString = formattedString.replace("6", "۶");
        formattedString = formattedString.replace("7", "۷");
        formattedString = formattedString.replace("8", "۸");
        formattedString = formattedString.replace("9", "۹");
        return formattedString;
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < rawString.length(); i++) {
//            Log.e("inja char at " + i + " is: ", rawString.charAt(i) + "");
//            if (Character.isDigit(rawString.charAt(i))) {
//                builder.append(farsiChars[(int) (rawString.charAt(i)) - 48]);
//            } else {
//                builder.append(rawString.charAt(i));
//            }
//        }
//        return builder.toString();
    }

    public static String convertFaNumberToEn(String rawString) {
        String formattedString;
        formattedString = rawString.replace("۰", "0");
        formattedString = formattedString.replace("۱", "1");
        formattedString = formattedString.replace("۲", "2");
        formattedString = formattedString.replace("۳", "3");
        formattedString = formattedString.replace("۴", "4");
        formattedString = formattedString.replace("۵", "5");
        formattedString = formattedString.replace("۶", "6");
        formattedString = formattedString.replace("۷", "7");
        formattedString = formattedString.replace("۸", "8");
        formattedString = formattedString.replace("۹", "9");
        return formattedString;
    }

    public static NumberFormat getPriceNumberFormmat(Locale locale) {
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        DecimalFormat format = ((DecimalFormat) NumberFormat.getInstance(locale));
        format.setGroupingSize(3);
        if ("fa".equalsIgnoreCase(lang) && !"TJ".equalsIgnoreCase(country))
            format.setDecimalFormatSymbols(new DecimalFormatSymbols());
        return format;
    }
}
