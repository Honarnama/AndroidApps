package net.honarnama.core.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by reza on 3/4/16.
 */
public class JalaliTest extends TestCase {
    int[][] vals = {
            {1394, 12, 14, 2016, 3, 4}
    };

    public void testJ2G() {
        for (int[] v: vals) {
            Calendar c = JalaliCalendar.jalaliToGregorian(v[0], v[1], v[2]);
            assertEquals("Failed for " + v[0] + "/" + v[1] + "/" + v[2] + " --> " + v[3] + "/" + v[4] + "/" + v[5], v[3], c.get(Calendar.YEAR));
            assertEquals("Failed for " + v[0] + "/" + v[1] + "/" + v[2] + " --> " + v[3] + "/" + v[4] + "/" + v[5], v[4], c.get(Calendar.MONTH)+1);
            assertEquals("Failed for " + v[0] + "/" + v[1] + "/" + v[2] + " --> " + v[3] + "/" + v[4] + "/" + v[5], v[5], c.get(Calendar.DATE));
        }
    }

    public void testG2J() {

    }

    public void testJ2G2J() {
        Random r = new Random();
        for(int i=0; i<1000; i++) {
            int d = r.nextInt(29) + 1;
            int m = r.nextInt(12) + 1;
            int y = r.nextInt(50) + 1350;

            Calendar c = JalaliCalendar.jalaliToGregorian(y, m, d);
            JalaliCalendar.JalaliDate jD = JalaliCalendar.gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE));

            assertEquals("Failed for " + y + "/" + m + "/" + d, jD.year, y);
            assertEquals("Failed for " + y + "/" + m + "/" + d, jD.month, m);
            assertEquals("Failed for " + y + "/" + m + "/" + d, jD.day, d);
        }
    }
}
