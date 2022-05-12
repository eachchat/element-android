package im.vector.app.eachchat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import im.vector.app.R;
import im.vector.app.eachchat.base.BaseModule;
import timber.log.Timber;

/**
 * Created by zhouguanjie on 2019/8/21.
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/M/d");
    private static final SimpleDateFormat simpleDateFormatV2 = new SimpleDateFormat("yyyy/M/d");
    private static final SimpleDateFormat simpleDateFormatV3 = new SimpleDateFormat("M/d HH:mm");
    private static final SimpleDateFormat simpleDateFormatV4 = new SimpleDateFormat("M/d");
    private static final SimpleDateFormat simpleDateFormatV5 = new SimpleDateFormat("yyyy/M/d HH:mm");
    private static final SimpleDateFormat simpleDateFormatV6 = new SimpleDateFormat("yyyy/M/d HH:mm");
    private static final SimpleDateFormat simpleDateFormatV7 = new SimpleDateFormat("M/d HH:mm");
    private static final SimpleDateFormat simpleDateFormatV8 = new SimpleDateFormat("yyyyMd-HH:mm:ss");

    public static String getPrettyDatePattern(Context context, String time) {
        Configuration conf = context.getResources().getConfiguration();
        Locale locale;
        LocaleList locales = conf.getLocales();
        locale = locales.get(0);

        String prettyDate = "";
        if (!TextUtils.isEmpty(time)) {
            try {
                Date date = new Date(Long.parseLong(time));
                prettyDate = new SimpleDateFormat(getDatePattern2(context, date.getTime()), locale).format(date);
            } catch (Exception e) {
                Timber.e("Utils%s", e.getMessage());
            }
        }
        return prettyDate;
    }

    /**
     * 获取时间显示格式
     *
     * @param time 当前时间
     * @return
     */
    public static String getDatePattern2(Context context, long time) {
        Calendar calendar = Calendar.getInstance();
        long todaybegin = getTodayBegin(calendar);
        long yearbegin = getYearBegin(calendar);
        return getDatePattern2(context, yearbegin, todaybegin,
                time, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * 获取时间显示格式
     *
     * @param yearbegin 本年毫秒级起始点
     * @param daybegin  当天毫秒级起始点
     * @param time      当前时间
     * @param hour      当前时
     * @param week      当天周几
     * @return
     */
    public static String getDatePattern2(Context context, long yearbegin, long daybegin, long time, int hour, int week) {
        final int ONEDAY_TIMEINMILLIS = 1000 * 60 * 60 * 24;
        /**
         * week的值从1-7，代表星期日-星期六
         */
        if (time >= daybegin && time < daybegin + ONEDAY_TIMEINMILLIS) {
            return "HH:mm";
        } else if (time >= daybegin - ONEDAY_TIMEINMILLIS) {
            return context.getString(R.string.yesterday);
        } else if (time >= daybegin - (6) * ONEDAY_TIMEINMILLIS) {
            return "EEEE";
        } else if (time >= yearbegin) {
            return "yyyy/M/d";
        } else {
            return "yyyy/M/d";
        }
    }


    /**
     * 获取今天的起始毫秒
     *
     * @param calendar
     * @return
     */
    public static long getTodayBegin(Calendar calendar) {
        return new GregorianCalendar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE)).getTimeInMillis();
    }

    /**
     * 获取今年的起始毫秒
     *
     * @param calendar
     * @return
     */
    public static long getYearBegin(Calendar calendar) {
        return new GregorianCalendar(
                calendar.get(Calendar.YEAR),
                1 - 1, 1, 0, 0).getTimeInMillis();
    }

    public static final String FORMATTER = "yyyy/MM/dd HH:mm:ss";

    /**
     * Format a date time with {@link SimpleDateFormat} as {@link DateUtils#FORMATTER}
     *
     * @param context Context to get the user locale
     * @param time    {@link System#currentTimeMillis()} or {@link Date#getTime()}
     * @return A formatted string as {@link DateUtils#FORMATTER}
     */
    public static String formatDate(Context context, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMATTER
                , context.getResources().getConfiguration().locale);
        return sdf.format(time);
    }

    public static boolean isSameDate(long timeStamp, long otherTimeStamp) {
        Date date1 = new Date(timeStamp);
        Date date2 = new Date(otherTimeStamp);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    public static String getTime(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormat.format(date);
    }

    public static String getTime(long timeStamp, SimpleDateFormat format) {
        Date date = new Date(timeStamp);
        return format.format(date);
    }

    public static String getTimeV2(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV2.format(date);
    }

    public static String getTimeV3(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV3.format(date);
    }

    public static String getTimeV4(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV4.format(date);
    }

    public static String getTimeV5(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV5.format(date);
    }

    public static String getTimeV6(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV6.format(date);
    }

    public static String getTimeV7(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV7.format(date);
    }

    public static String getTimeV8(long timeStamp) {
        Date date = new Date(timeStamp);
        return simpleDateFormatV8.format(date);
    }


    public static String getTime(Date date) {
        if (date != null) {
            return new SimpleDateFormat("yyyy年M月d日 hh:mm").format(date);
        } else {
            return "";
        }
    }

    /**
     * 时间戳格式转换
     */
    static String dayNames[] = {BaseModule.getContext().getString(R.string.sunday),
            BaseModule.getContext().getString(R.string.monday),
            BaseModule.getContext().getString(R.string.tuesday),
            BaseModule.getContext().getString(R.string.wednesday),
            BaseModule.getContext().getString(R.string.thursday),
            BaseModule.getContext().getString(R.string.friday),
            BaseModule.getContext().getString(R.string.saturday)};

    public static String getNewChatTime(long timesamp) {
        String result;
        Calendar todayCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(timesamp);
        String timeFormat;
        String yearTimeFormat;
        String am_pm;
        int hour = otherCalendar.get(Calendar.HOUR_OF_DAY);
        boolean isAM = false;
        if (hour >= 0 && hour < 12) {
            am_pm = BaseModule.getContext().getString(R.string.am);
            timeFormat = "M/d a K:mm";
            yearTimeFormat = "yyyy/M/d a K:mm";
            isAM = true;
        } else {
            am_pm = BaseModule.getContext().getString(R.string.pm);
            timeFormat = "M/d a h:mm";
            yearTimeFormat = "yyyy/M/d a h:mm";
        }


        boolean yearTemp = todayCalendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR);
        if (yearTemp) {
            int todayMonth = todayCalendar.get(Calendar.MONTH);
            int otherMonth = otherCalendar.get(Calendar.MONTH);
            if (todayMonth == otherMonth) {//表示是同一个月
                int temp = todayCalendar.get(Calendar.DATE) - otherCalendar.get(Calendar.DATE);
                switch (temp) {
                    case 0:
                        result = getHourAndMin(timesamp, isAM);
                        break;
                    case 1:
                        result = BaseModule.getContext().getString(R.string.yesterday) + " " + getHourAndMin(timesamp, isAM);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        int dayOfMonth = otherCalendar.get(Calendar.WEEK_OF_MONTH);
                        int todayOfMonth = todayCalendar.get(Calendar.WEEK_OF_MONTH);
                        if (dayOfMonth == todayOfMonth) {//表示是同一周
                            int dayOfWeek = otherCalendar.get(Calendar.DAY_OF_WEEK);
                            if (dayOfWeek != 1) {//判断当前是不是星期日     如想显示为：周日 12:09 可去掉此判断
                                result = dayNames[otherCalendar.get(Calendar.DAY_OF_WEEK) - 1] + " " + getHourAndMin(timesamp, isAM);
                            } else {
                                result = getTime(timesamp, timeFormat);
                            }
                        } else {
                            result = getTime(timesamp, timeFormat);
                        }
                        break;
                    default:
                        result = getTime(timesamp, timeFormat);
                        break;
                }
            } else {
                result = getTime(timesamp, timeFormat);
            }
        } else {
            result = getYearTime(timesamp, yearTimeFormat);
        }
        return result;
    }

    /**
     * 当天的显示时间格式
     *
     * @param time
     * @return
     */
    public static String getHourAndMin(long time, boolean isAM) {
        if(isAM){
            SimpleDateFormat format = new SimpleDateFormat("a K:mm");
            return format.format(new Date(time));
        }else{
            SimpleDateFormat format = new SimpleDateFormat("a h:mm");
            return format.format(new Date(time));
        }
    }

    /**
     * 不同一周的显示时间格式
     *
     * @param time
     * @param timeFormat
     * @return
     */
    public static String getTime(long time, String timeFormat) {
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        return format.format(new Date(time));
    }

    /**
     * 不同年的显示时间格式
     *
     * @param time
     * @param yearTimeFormat
     * @return
     */
    public static String getYearTime(long time, String yearTimeFormat) {
        SimpleDateFormat format = new SimpleDateFormat(yearTimeFormat);
        return format.format(new Date(time));
    }

    public static long getWeeOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }
}
