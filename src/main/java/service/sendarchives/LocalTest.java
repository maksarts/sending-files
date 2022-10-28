package service.sendarchives;

import usr.lib.global.DateLib;
import usr.lib.global.StringLib;

import java.util.Calendar;
import java.util.Date;

public class LocalTest {
    public static void main(String[] args){
        String dtStr = "2022-10-09";
        String dtStrEnd = "2022-10-10";

        while(!dtStr.equals(dtStrEnd)){
            Date prevDate;
            Date dt = StringLib.toDateThrow(dtStr, "yyyy-MM-dd");
            System.out.println("dt="+dt);

            if (dt==null) {
                prevDate = DateLib.addToDate(new Date(), Calendar.DAY_OF_YEAR, -1);
            } else {
                prevDate = dt;
            }

            System.out.println("prevDate="+prevDate);

            // получаем следующую
            long nextDateMilliSeconds = prevDate.getTime() + 24 * 60 * 60 * 1000;
            Date newDate = new Date(nextDateMilliSeconds);

            System.out.println("newDate="+newDate);

            String newDateStr = StringLib.toString(newDate, "yyyy-MM-dd");

            System.out.println("newDateStr="+newDateStr);
            dtStr = newDateStr;
        }
    }
}
