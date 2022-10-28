package service.lib;

import java.util.Calendar;
import java.util.Date;

public class DateLib {

	public static Date addToDate(Date dt, int field, int amount) {
		if (dt == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	public static Boolean dateInPeriod(Date dt, Date dateStart, Date dateEnd) {
		Boolean res = ((dt.after(dateStart)) & (dt.before(dateEnd)));
		return res;
	}

	public static Boolean timeStrInPeriod(Date dt, String timeStart, String timeEnd, String timeFormat) {
		String dtFormat = StringLib.FORMAT_DATE + " " + timeFormat;
		Date dateStart = StringLib.toDate(StringLib.toString(dt, StringLib.FORMAT_DATE) + " " + timeStart, dtFormat);
		Date dateEnd = StringLib.toDate(StringLib.toString(dt, StringLib.FORMAT_DATE) + " " + timeEnd, dtFormat);
		return dateInPeriod(dt, dateStart, dateEnd);
	}

    public static Integer getDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
    	Integer res = (calendar.get(Calendar.DAY_OF_WEEK)-1);
    	if (res.equals(0)) {
    		res = 7;
    	}
    	return res;
    }

    public static Integer getCalendarField(Date date, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
    	Integer res = calendar.get(field);
    	return res;
    }
    
    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
}
