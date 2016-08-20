package net.honarnama.base.utils;

import java.util.Calendar;
import java.util.Date;

public class JalaliCalendar {
    private static int gregorianDaysInMonth[] = {31, 28, 31, 30, 31, 30, 31,
            31, 30, 31, 30, 31};
    private static int jalaliDaysInMonth[] = {31, 31, 31, 31, 31, 31, 30, 30,
            30, 30, 30, 29};

    public static Date getGregorianDate(String jalaliDate) {
        String[] arr = jalaliDate.split("/");

        Calendar cal = jalaliToGregorian(Integer.parseInt(arr[0]),
                Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));

        return cal.getTime();
    }

    public static String getJalaliDate(Date gDate) {

        Calendar gCal = Calendar.getInstance();

        gCal.clear();

        gCal.setTime(gDate);
        Date dt = gCal.getTime();
        int myYear = gCal.get(Calendar.YEAR);
        int myMonth = gCal.get(Calendar.MONTH);
        int myDay = gCal.get(Calendar.DAY_OF_MONTH);

        JalaliDate jDate = gregorianToJalali(myYear, myMonth+1, myDay);

        return jDate.toString();
    }

    public static JalaliDate gregorianToJalali(int gYear, int gMonth, int gDay) {
        int gy, gm, gd;
        int jy, jm, jd;
        long g_day_no, j_day_no;
        int j_np;
        int i;

        gy = gYear - 1600;
        gm = gMonth - 1;
        gd = gDay - 1;

        g_day_no = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400;
        for (i = 0; i < gm; ++i)
            g_day_no += gregorianDaysInMonth[i];
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)))
			/* leap and after Feb */
            ++g_day_no;
        g_day_no += gd;

        j_day_no = g_day_no - 79;

        j_np = new Long(j_day_no / 12053).intValue();
        j_day_no %= 12053;

        jy = new Long(979 + 33 * j_np + 4 * (j_day_no / 1461)).intValue();
        j_day_no %= 1461;

        if (j_day_no >= 366) {
            jy += (j_day_no - 1) / 365;
            j_day_no = (j_day_no - 1) % 365;
        }

        for (i = 0; i < 11 && j_day_no >= jalaliDaysInMonth[i]; ++i) {
            j_day_no -= jalaliDaysInMonth[i];
        }
        jm = i + 1;
        jd = new Long(j_day_no + 1).intValue();
        return new JalaliDate(jy, jm, jd);
    }

    public static Calendar jalaliToGregorian(int jYear, int jMonth, int jDay) {
        int gy, gm, gd;
        int jy, jm, jd;
        long g_day_no, j_day_no;
        boolean leap;

        int i;

        jy = jYear - 979;
        jm = jMonth - 1;
        jd = jDay - 1;

        j_day_no = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4;
        for (i = 0; i < jm; ++i)
            j_day_no += jalaliDaysInMonth[i];

        j_day_no += jd;

        g_day_no = j_day_no + 79;

        gy = new Long(1600 + 400 * (g_day_no / 146097)).intValue(); /*
																	 * 146097 =
																	 * 365*400 +
																	 * 400/4 -
																	 * 400/100 +
																	 * 400/400
																	 */
        g_day_no = g_day_no % 146097;

        leap = true;
        if (g_day_no >= 36525) /* 36525 = 365*100 + 100/4 */
        {
            g_day_no--;
            gy += 100 * (g_day_no / 36524); /* 36524 = 365*100 + 100/4 - 100/100 */
            g_day_no = g_day_no % 36524;

            if (g_day_no >= 365)
                g_day_no++;
            else
                leap = false;
        }

        gy += 4 * (g_day_no / 1461); /* 1461 = 365*4 + 4/4 */
        g_day_no %= 1461;

        if (g_day_no >= 366) {
            leap = false;

            g_day_no--;
            gy += g_day_no / 365;
            g_day_no = g_day_no % 365;
        }

        for (i = 0; g_day_no >= gregorianDaysInMonth[i] + ((i == 1 && leap) ? 1 : 0); i++)
            g_day_no -= gregorianDaysInMonth[i] + ((i == 1 && leap) ? 1 : 0);
        gm = i + 1;
        gd = new Long(g_day_no + 1).intValue();

        Calendar calendar = Calendar.getInstance();
        calendar.set(gy, gm - 1, gd);
        return calendar;
    }

    public static class JalaliDate {
        public int year;
        public int month;
        public int day;

        public JalaliDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public JalaliDate(JalaliDate that) {
            this(that.year, that.month, that.day);
        }

        public void set(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public void set(JalaliDate that) {
            set(that.year, that.month, that.day);
        }

        public String toString() {
            return year + "/" + month + "/" + day;
        }
    }
}