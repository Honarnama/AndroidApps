package net.honarnama.core.utils;

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
}
