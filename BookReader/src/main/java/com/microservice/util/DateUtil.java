package com.microservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MatchResult;

public class DateUtil {
	public static Logger logger = Logger.getLogger(StringUtil.class);

	public static Date getTime(String strTime) {
		if (strTime == null)
			return null;

		Date date = null;
		SimpleDateFormat formats = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			String ft = getFormattedTime(strTime);
			date = formats.parse(ft);
		} catch (Exception e) {
			;
		}

		return date;
	}

	public static boolean isEarlyTime(String time1, String time2) {
		boolean isEarly = false;

		if (time1 == null || time2 == null)
			return false;

		SimpleDateFormat formats = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			String ft1 = getFormattedTime(time1);
			Date date1 = formats.parse(ft1);

			if (date1 == null)
				return false;

			String ft2 = getFormattedTime(time2);
			Date date2 = formats.parse(ft2);

			GregorianCalendar gc1 = new GregorianCalendar();
			gc1.setTime(date1);

			GregorianCalendar gc2 = new GregorianCalendar();
			gc2.setTime(date2);

			if (gc1.before(gc2))
				return true;
			else
				return false;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isEarly;
	}

	public static boolean isInTime(String time, int inDays)
			throws ParseException {
		boolean b = false;

		if (time == null)
			return b;

		time = getFormattedTime(time);
		if (time == null)
			return b;

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date articleDate = format.parse(time);

		Date today = new Date();

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(today);
		gc.add(Calendar.DAY_OF_MONTH, -inDays);

		Date inTime = gc.getTime();

		b = articleDate.after(inTime);

		return b;
	}

	public static boolean areSameTime(Date processTime, Date postTime) {
		boolean areSame = false;

		if (postTime == null)
			return false;

		try {
			SimpleDateFormat formats = new SimpleDateFormat("yyyyMMddHHmm");
			String strProcessTime = formats.format(processTime);
			String strPostTime = formats.format(postTime);

			if (strProcessTime.equals(strPostTime))
				return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return areSame;
	}

	public static String getBeforeFormattedTime(String time) {
		String strTime = null;
		if (time == null)
			return null;

		time = time.toLowerCase();

		SimpleDateFormat formats = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.setTime(new Date());

		if (time.indexOf("ago") != -1) {

			int dd = time.indexOf("day");
			int hh = time.indexOf("hour");
			int mm = time.indexOf("minute");
			if (dd != -1) {
				String strDay = time.substring(0, dd);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.DAY_OF_MONTH, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (hh != -1) {

				String strDay = time.substring(0, hh);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.HOUR, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (mm != -1) {

				String strDay = time.substring(0, mm);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.MINUTE, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			}
			return strTime;
		}

		if (time.indexOf("ǰ") != -1) {
			int dd = time.indexOf("��");
			int hh = time.indexOf("Сʱ");
			int mm = time.indexOf("����");
			int ss = time.indexOf("��");
			int mmTw = time.indexOf("���");
			int hhTW = time.indexOf("С�r");
			if (dd != -1) {
				String strDay = time.substring(0, dd);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.DAY_OF_MONTH, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (hh != -1) {

				String strDay = time.substring(0, hh);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.HOUR, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (hhTW != -1) {

				String strDay = time.substring(0, hhTW);
				int d = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.HOUR, -d);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (mm != -1) {

				String strDay = time.substring(0, mm);
				int m = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.MINUTE, -m);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (mmTw != -1) {

				String strDay = time.substring(0, mmTw);
				int m = Integer.parseInt(strDay.trim());
				gCalendar.add(GregorianCalendar.MINUTE, -m);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			} else if (ss != -1) {

				String strSecond = time.substring(0, ss);
				int s = Integer.parseInt(strSecond.trim());
				gCalendar.add(GregorianCalendar.SECOND, -s);

				Date date = gCalendar.getTime();
				strTime = formats.format(date);
			}
			return strTime;
		}

		return null;
	}

	public static String getFormattedTime(String strText) {
		String strTime = null;

		if (strText == null)
			return null;

		strText = strText.trim();

		SimpleDateFormat formats = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		if (strText.matches(".*?(GMT).*?")) {
			SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date gmtDate = sdf.parse(strText);
				strTime = formats.format(gmtDate);
			} catch (Exception e) {
				;
			}
		}

		if (strTime != null)
			return strTime;

		try {

			String strBeforeTimeRegex = "(?i)[\\d]+[\\s]*?(��|Сʱ|����|��|���|day|days|hour|hours|minute|minutes)[\\s]*?(ǰ|ago)";
			if (strText.matches(strBeforeTimeRegex)) {
				strTime = getBeforeFormattedTime(strText);
			}
		} catch (Exception e) {
			logger.error(e);
		}

		if (strTime != null)
			return strTime;

		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.setTime(new Date());

		String strTimeRegx = "(\\d{2,4})[��|\\-|/|.]{1}(\\d{1,2})[��|\\-|/|.](\\d{1,2})[��]{0,1}[\\s|��]*"
				+ "(\\d{1,2}){0,1}[��|:|ʱ]{0,1}(\\d{1,2}){0,1}[��|:|��]{0,1}(\\d{1,2}){0,1}";

		List<MatchResult> matchResults = null;
		String year = null, month = null, day = null, hour = null, minute = null, second = null;

		try {
			matchResults = RegexUtil.getAllMatchResult(strText, strTimeRegx);

			for (int i = 0; i < matchResults.size(); i++) {
				MatchResult matchResult = matchResults.get(i);

				year = null;
				month = null;
				day = null;
				hour = null;
				minute = null;
				second = null;
				year = matchResult.group(1);
				if (year != null) {
					int y = Integer.parseInt(year);
					String strYear = gCalendar.get(GregorianCalendar.YEAR) + "";
					int last2Year = Integer.parseInt(strYear.substring(2, 4));

					if (y > gCalendar.get(GregorianCalendar.YEAR)
							|| (y > last2Year && y < 100)
							|| (y > 100 && y < 2000) || y < 0) {
						year = null;
						continue;
					} else if (y >= 10 && y <= last2Year) {

						year = "20" + y;
					} else if (y >= 0 && y < 10) {

						year = "200" + y;
					}
				}

				month = matchResult.group(2);
				if (month != null) {
					int m = Integer.parseInt(month);
					if (m > 12 || m <= 0) {
						month = null;
						continue;
					}
				}

				day = matchResult.group(3);
				if (day != null) {
					int d = Integer.parseInt(day);
					if (d > 31 || d <= 0) {
						day = null;
						continue;
					}
				}

				hour = matchResult.group(4);
				if (hour != null) {
					int hh = Integer.parseInt(hour);
					if (hh > 24 || hh < 0)
						continue;
				}

				minute = matchResult.group(5);
				if (minute != null) {
					int mm = Integer.parseInt(minute);
					if (mm > 60 || mm < 0)
						continue;
				}

				second = matchResult.group(6);
				if (second != null) {
					int ss = Integer.parseInt(second);
					if (ss > 60 || ss < 0)
						continue;
				}

				strTime = year + "/" + month + "/" + day + " " + hour + ":"
						+ minute + ":" + second;

				if (month != null && day != null)
					break;
			}

		} catch (Exception e) {
			;
		}

		if (month == null || day == null)
			return null;

		strTime = year + "/" + month + "/" + day + " " + hour + ":" + minute
				+ ":" + second;

		if (year == null || year.length() == 0)
			year = gCalendar.get(GregorianCalendar.YEAR) + "";
		if (month == null || month.length() == 0)
			return null;
		if (day == null || day.length() == 0)
			return null;

		if (hour == null || hour.length() == 0)
			hour = gCalendar.get(GregorianCalendar.HOUR_OF_DAY) + "";
		if (minute == null || minute.length() == 0)
			minute = gCalendar.get(GregorianCalendar.MINUTE) + "";
		if (second == null || second.length() == 0)
			second = gCalendar.get(GregorianCalendar.SECOND) + "";

		strTime = year + "/" + month + "/" + day + " " + hour + ":" + minute
				+ ":" + second;

		Date d = null;
		try {
			d = formats.parse(strTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		strTime = formats.format(d);

		return strTime;
	}

	public static void main(String[] args) throws ParseException {
		String time = "2010/05/31 12:19:35";
		System.out.println(" ʱ���ʽ��time |" + time);
		boolean b = DateUtil.isInTime(time, 10);
		if (b) {
			System.out.println(" ����֮������ţ�");
		} else {
			System.out.println(" ����֮ǰ�����ţ�");
		}

		time = "2014��09��02�� 20:05";
		time = "2014.3.24";
		time = "2  Сʱ ǰ";
		time = " 1 day Ago";

		if (time.matches("(?i)[\\d]+[\\s]*?(��|Сʱ|����|��|���|day|days|hour|hours|minute|minutes)[\\s]*?(ǰ|ago)"))
			System.out.println("leagal Time format.");
		time = "11-24 05:13";

		time = "Fri, 25 Sep 2015 23:42:16 GMT";

		System.out.println("time=" + getFormattedTime(time));

	}
}
