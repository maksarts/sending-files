package service.lib;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class StringLib {
	//Константы
	public final static String FORMAT_DATE = "dd.MM.yyyy";
	public final static String FORMAT_TIME = "HH:mm:ss";
	public final static String FORMAT_TIMEMSEC = FORMAT_TIME + ",SSS";
	public final static String FORMAT_DATETIME = FORMAT_DATE + " " + FORMAT_TIME;
	public final static String FORMAT_DATETIMEMSEC = FORMAT_DATE + " " + FORMAT_TIMEMSEC;
	
	public final static Integer CHAR_CODE_TAB = 9;
	public final static Integer CHAR_CODE_CR = 13;
	public final static Integer CHAR_CODE_LF = 10;
	public final static Integer CHAR_CODE_SPACE = 32;
	
	public final static char CR = (char)13;
	public final static char LF = (char)10;
	public final static String CRLF = String.valueOf(CR) + String.valueOf(LF);
	
	private final static String SEP = ",";
	private final static String CHAR_DIGIT = "0123456789";
	private final static String PRINT_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	public static String getCRLF() {
		String res = String.valueOf(CR) + String.valueOf(LF);
		return res;
	}
	
	public static String trimAll(String str) {
    	if (str==null) {
    		return null;
    	}
    	StringBuffer stringBuffer = new StringBuffer();
    	Integer strLen = str.length();
    	for (int i=0; i<strLen; i++) {
    		char ch = str.charAt(i);
    		if ((ch!=CR) && (ch!=LF)) {
    			stringBuffer.append(ch);    			
    		}
    	}
    	return stringBuffer.toString();
	}
	
    public static List<String> toArray(String str) {
		return toArray(str, SEP);
	}
    
    public static Boolean isDigit(String str) {
    	if (str==null) {
    		return false;
    	}
    	Integer strLen = str.length();
    	for (int i=0; i<strLen; i++) {
    		char ch = str.charAt(i);
    		if (CHAR_DIGIT.indexOf(ch)==-1) {
    			return false;
    		}
    	}
    	return true;
    }
    
	public static String replaceNonDigitCharacters(String str, String newChar) {
		if (str==null) {
			return null;
		}
		Integer strLen = str.length();
		StringBuffer strBuffer = new StringBuffer(strLen);
		for (int i=0; i<strLen; i++) {
			char ch = str.charAt(i);
			if (CHAR_DIGIT.indexOf(ch)==-1) {
				strBuffer.append(newChar);
			} else {
				strBuffer.append(ch);
			}
		}
		return strBuffer.toString();
	}

    public static String fillChar(String str, String ch, Integer strLen, Integer direction) {
    	if (str==null) {
    		return null;
    	}
    	String res = "";
    	Integer count = strLen - str.length();
    	for (int i=0; i<count; i++) {
    		res = res + ch;
    	}
    	if (direction.equals(1)) {
    		res = str + res;
    	} else {
    		res = res + str;
    	}
    	return res;
    }

	public static String endString(String str, Integer len) {
		if (str == null) {
			return null;
		}
		Integer strLen = str.length();
		Integer startCopy = strLen-len;
		if (startCopy<0) {
			startCopy = 0;
		}
		return str.substring(startCopy);
	}
	
	public static String startString(String str, Integer len) {
		if (str == null) {
			return null;
		}
		Integer strLen = str.length();
		if (strLen<len) {
			return str;
		}
		return str.substring(0, len);
	}

    public static List<String> toArray(String str, String SEP) {
		List<String> res = new ArrayList<String>();
		if ((str==null) || (str.trim().isEmpty())) {
			return res; 
		}		
		int lenSep = SEP.length();
		String s = str;
		String s1;
		int x;
		do {
			x = s.indexOf(SEP);
			if (x<0) {
				res.add(s);
				break;
			}
			s1 = s.substring(0, x);
			s = s.substring(x+lenSep);
			res.add(s1);
		} while (true);
		return res;
	}

    public static String toString(List<String> arr) {
		return toString(arr, SEP);
	}

    public static String toString(List<String> arr, String SEP) {
    	if (arr==null) {
    		return "";
    	}
		String res = "";
		for (int i=0; i<arr.size(); i++) {
			if (res=="") {
				res = String.valueOf(arr.get(i));
			} else {
				res = res + SEP + String.valueOf(arr.get(i));
			}
		}
		return res;
	}

    public static String toString(Date date, String format) {
    	if ((date==null || date.equals(new Date(0)))) {
    		return null;
    	}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

    public static String toString(Double value) {
    	return toString(value, 2);	
    }
    
    public static String toString(Double value, Integer decimalCount) {
    	if (value==null) {
    		return null;
    	}
		DecimalFormat decimalFormat = new DecimalFormat();
		String decimalPattern = fillChar("", "0", decimalCount, 0);
		decimalFormat.applyPattern("0." + decimalPattern);
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
		String res = decimalFormat.format(value);
		return res;
	}

    public static String toString(Double value, Integer decimalCount, char decimalSep, char thousandsSep) {
    	if (value==null) {
    		return null;
    	}
		DecimalFormat decimalFormat = new DecimalFormat();
		String decimalPattern = fillChar("", "0", decimalCount, 0);
		decimalFormat.applyPattern("#,##0." + decimalPattern);
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator(decimalSep);
		decimalFormatSymbols.setGroupingSeparator(thousandsSep);
		decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
		String res = decimalFormat.format(value);
		return res;
	}

    public static String toString(Long value, char thousandsSep) {
    	if (value==null) {
    		return null;
    	}
		DecimalFormat decimalFormat = new DecimalFormat();
		String decimalPattern = fillChar("", "0", 0, 0);
		decimalFormat.applyPattern("#,##0" + decimalPattern);
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setGroupingSeparator(thousandsSep);
		decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
		String res = decimalFormat.format(value);
		return res;
	}
    
    public static Date toDate(String str, String format) {
    	try {
	    	if (str==null || str.trim().isEmpty()) {
	    		return null;
	    	}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
			return simpleDateFormat.parse(str);
    	} catch (Exception ex) {
    		throw new RuntimeException(ex);
    	}
	}
    
    public static Date toDateThrow(String str, String format) {
    	try {
    		return toDate(str, format);
    	} catch (Exception ex) {
    		return null;
    	}
    }
    
    public static Boolean compareMask(String mask, String str) {
		/* Пример маски (".*_.*xml" = "*_*xml"):
	    ^ - признак начала строки;
	    . - один символ;
	    .* - множество символов;
	    \. - точка;
	    $ - признак конца строки;
	    | - логическое ИЛИ;
		*/
		Pattern pattern;
		pattern = Pattern.compile(mask);		
		Boolean res = pattern.matcher(str).matches();
    	return res;
    }
    
    /*
    public static String getHashMD5(String str) {
		try {
			if (str==null || str.isEmpty()) {
	    		return null;
	    	}
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(str.getBytes(XMLConst.XML_ENCODING_UTF_8));
			StringBuilder sb = new StringBuilder(2*digest.length);
			for(byte b : digest){
				sb.append(String.format("%02x", b&0xff));
			}
			return sb.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
    }
    */
    
    public static String getPrintChar(Integer ind) {
    	return PRINT_CHAR.substring(ind, ind+1);
    }
    
    public static String getHashMD5(String string) {
    	return getHashMD5(string, "UTF-8");
    }
    
    public static String getHashMD5(String string, String encoding) {
    	try {
    		java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
    		byte[] array = md.digest(string.getBytes(encoding));
    		StringBuffer sb = new StringBuffer();
    		for (int i = 0; i < array.length; ++i) {
    			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
    		}
    		return sb.toString();
    	} catch (Exception e) {
    	}
    	return null;
    }
}
