package net.honarnama.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by elnaz on 3/25/16.
 */
public class LocaleUtils {
    public final static char PERSIAN_ZERO = 0x06f0;
    public final static char PERSIAN_NINE = 0x06f9;
    public final static char PERSIAN_DECIMAL_POINT = 0x066b;
    public final static char DIGIT_DIFF = PERSIAN_ZERO - '0';
    public final static Locale PERSIAN_LOCAL = new Locale("fa", "IR");
    public final static char PERSIAN_DECIMAL_SEPRATOR = 0x066C;
    public final static char ENGLISH_DECIMAL_SEPERATOR = ((DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH)).getDecimalFormatSymbols().getGroupingSeparator();
    public final static DecimalFormatSymbols PERSIAN_DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();

    static {
        PERSIAN_DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator(PERSIAN_DECIMAL_SEPRATOR);
    }

    public static NumberFormat getPriceNumberFormmat(Locale locale) {
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        DecimalFormat format = ((DecimalFormat) NumberFormat.getInstance(locale));
        format.setGroupingSize(3);
        if ("fa".equalsIgnoreCase(lang) && !"TJ".equalsIgnoreCase(country))
            format.setDecimalFormatSymbols(PERSIAN_DECIMAL_FORMAT_SYMBOLS);
        return format;
    }

    public static String persianDigits(String str) {
        String result = "";
        char ch;
        int i;

        for (i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if ((ch >= '0') && (ch <= '9'))
                result += Character.toString((char) (DIGIT_DIFF + ch));
            else
                result += ch;
        }

        return result;
    }

    public static String persianDigitsIfPersian(String str, Locale locale) {
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if ("fa".equals(lang) && !"TJ".equals(country))
            return persianDigits(str);
        else
            return str;
    }

    public static String persianDigitsIfPersian(String str) {
        Locale locale = Locale.getDefault();
        return persianDigitsIfPersian(str, locale);
    }

    public static String persianDigitsIfPersian(int str) {
        return persianDigitsIfPersian(String.valueOf(str));
    }
}
