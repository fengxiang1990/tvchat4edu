package com.bizcom.util;

import android.text.TextUtils;

import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    /**
     * 此函数是通用的显示的消息时间  <br>
     * <ul>
     * today HH:mm:ss (时:分:秒)<br>
     * yesterday : yesterday (昨天) <br>
     * more before : yyyy-MM-dd HH:mm:ss <br>
     * </ul>
     *
     * @param longDate
     * @return
     */
    public static String getStringDate(long longDate) {
        return getSpecificFormatDate(longDate);
    }

    public static String[] getDateForTabFragmentConversation(long longDate) {
        String specificFormatDate = getSpecificFormatDate(longDate);
//        return specificFormatDate.split("_");
        return new String[]{specificFormatDate};
    }

    /**
     * 此函数是通用的显示的消息时间
     *
     * @param longDate
     * @return
     */
    public static String getSpecificFormatDate(long longDate) {
        SimpleDateFormat format;

        Date dates = new Date(longDate);
        Calendar cale = Calendar.getInstance();
        cale.setTime(dates);

        Date nowDates = new Date(GlobalConfig.getGlobalServerTime());
        Calendar currentCale = Calendar.getInstance();
        currentCale.setTime(nowDates);

        int days = cale.get(Calendar.DAY_OF_MONTH);
        int currentCaleDays = currentCale.get(Calendar.DAY_OF_MONTH);

        int loginMonth = cale.get(Calendar.MONTH);
        int currentMonth = currentCale.get(Calendar.MONTH);

        int loginYear = cale.get(Calendar.YEAR);
        int currentYear = currentCale.get(Calendar.YEAR);

        if (loginYear == currentYear) {
            if (currentMonth == loginMonth) {
                //今天
                if (currentCaleDays - days == 0) {
                    return getDateFormat(longDate, "HH:mm");
                }
                //昨天
                if (currentCaleDays - 1 == days) {
                    // need get context
                    return GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_yesterday);
                }
                //明天
                if (currentCaleDays + 1 == days) {
                    String tomorrow = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_tomorrow);
                    return tomorrow + getDateFormat(longDate, "HH:mm");
                }
                //上一周
                int week = cale.get(Calendar.WEEK_OF_MONTH);
                int currentWeek = currentCale.get(Calendar.WEEK_OF_MONTH);
                int day = cale.get(Calendar.DAY_OF_WEEK);
                if (day == 1) {
                    day = 7;
                    week = week - 1;
                } else {
                    day = day - 1;
                }

                int currentDay = currentCale.get(Calendar.DAY_OF_WEEK);
                if (currentDay == 1) {
                    currentWeek = week - 1;
                }

                if (week == currentWeek) {
                    String weekStr = null;
                    switch (day) {
                        case 1:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_monday);
                            break;
                        case 2:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_tuesday);
                            break;
                        case 3:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_wednesday);
                            break;
                        case 4:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_thursday);
                            break;
                        case 5:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_friday);
                            break;
                        case 6:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_saturday);
                            break;
                        case 7:
                            weekStr = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_week_sunday);
                            break;
                    }
                    return weekStr;
                }
            } else if (currentMonth - loginMonth == 1) {
                int maxDayNumber = cale.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (days == maxDayNumber && currentCaleDays == 1) {
                    return GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_yesterday);
                }
            }

            //本年的未来时间与过去时间
            if (dates.getTime() > nowDates.getTime()) {
//                String month = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_month_of_year);
//                String day = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_day_of_month);
                String targetDate = TextUtils.concat("MM-dd", " HH:mm:ss").toString();
                format = new SimpleDateFormat(targetDate);
            } else {
//                String month = GlobalConfig.APPLICATION_CONTEXT.getResources().
//                        getString(R.string.common_date_month_of_year);
//                String targetDate;
//                String day = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_day_of_month);
//                if("month".equals(month)){
//                    targetDate = TextUtils.concat("MMM","dd", day).toString();
//                } else {
//                    targetDate = TextUtils.concat("MM", month, "dd", day).toString();
//                }
                String targetDate = "MM-dd";
                format = new SimpleDateFormat(targetDate);
            }
            return format.format(longDate);
        }

        //其他年份的未来时间与过去时间
        if (dates.getTime() > nowDates.getTime()) {
//            String year = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_year);
//            String month = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_month_of_year);
//            String day = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_day_of_month);
//            String targetDate = TextUtils.concat("yyyy", year, "MM", month, "dd", day, " HH:mm").toString();
            String targetDate = "yyyy-MM-dd HH:mm";
            format = new SimpleDateFormat(targetDate);
        } else {
            String targetDate = "yyyy-MM-dd";
//            String year = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_year);
//            String month = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_month_of_year);
//            String day = GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_day_of_month);
//            String targetDate = TextUtils.concat("yyyy", year, "MM", month, "dd", day).toString();
            format = new SimpleDateFormat(targetDate);
        }
        return format.format(longDate);
    }

    /**
     * 此函数针对聊天界面中显示的消息时间 <br>
     * <ul>
     * today HH:mm:ss (时:分)<br>
     * yesterday : yesterday (昨天) <br>
     * more before : yyyy-MM-dd HH:mm:ss <br>
     * </ul>
     *
     * @param longDate
     * @return
     */
    public static String getDateForChatMsg(long longDate) {
        SimpleDateFormat format;

        Date dates = new Date(longDate);
        Calendar cale = Calendar.getInstance();
        cale.setTime(dates);

        Date nowDates = new Date(GlobalConfig.getGlobalServerTime());
        Calendar currentCale = Calendar.getInstance();
        currentCale.setTime(nowDates);

        int days = cale.get(Calendar.DAY_OF_MONTH);
        int currentCaleDays = currentCale.get(Calendar.DAY_OF_MONTH);

        int loginMonth = cale.get(Calendar.MONTH);
        int currentMonth = currentCale.get(Calendar.MONTH);

        if (currentMonth == loginMonth) {
            if (currentCaleDays - days == 0) {
                return getDateFormat(longDate, "HH:mm");
            }

            if (currentCaleDays - 1 == days) {
                // need get context
                return GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_yesterday)
                        + getDateFormat(longDate, "HH:mm");
            }

        } else if (currentMonth - loginMonth == 1) {
            int maxDayNumber = cale.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (days == maxDayNumber && currentCaleDays == 1) {
                return GlobalConfig.APPLICATION_CONTEXT.getResources().getString(R.string.common_date_yesterday)
                        + getDateFormat(longDate, "HH:mm");
            }
        }

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(longDate);
    }

    /**
     * get the time format , like HH:mm:ss
     *
     * @param mTimeLine
     * @return
     */
    public static String getDateFormat(long mTimeLine, String format) {

        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date currentTime = new Date(mTimeLine);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * get standard date time , like 2014-09-01 14:20:22
     *
     * @return
     */
    public static String getStandardDate(Date date) {

        if (date == null)
            throw new RuntimeException("Given date object is null...");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date.getTime());
    }

    /**
     * get the time format , like HH:mm:ss ; mm:ss ; ss ...
     *
     * @param times
     * @return
     * @see com.bizcom.vc.activity.message.VoiceMessageDetailActivity
     */
    public static String calculateTime(long times) {
        times = times / 1000;
        int hour = (int) times / 3600;
        int minute = (int) (times - (hour * 3600)) / 60;
        int second = (int) times - (hour * 3600 + minute * 60);
        if (minute <= 0 && hour <= 0) {
            return (second < 10 ? "0" + second : second) + "秒";
        } else if (hour <= 0) {
            return (minute < 10 ? "0" + minute : minute) + "分" + (second < 10 ? "0" + second : second) + "秒";
        } else {
            if (minute <= 0 && second > 0) {
                return (hour < 10 ? "0" + hour : hour) + "时" + (second < 10 ? "0" + second : second) + "秒";
            } else if (second <= 0 && minute > 0) {
                return (hour < 10 ? "0" + hour : hour) + "时" + (minute < 10 ? "0" + minute : minute) + "分";
            } else if (second <= 0 && minute <= 0) {
                return (hour < 10 ? "0" + hour : hour) + "时";
            } else {
                return (hour < 10 ? "0" + hour : hour) + "时" + (minute < 10 ? "0" + minute : minute) + "分"
                        + (second < 10 ? "0" + second : second) + "秒";
            }
        }
    }

    /**
     * Time format display when the p2p call..
     *
     * @return
     */
    public static String calculateFixedTime(long callTime) {
        int hour = (int) callTime / 3600;
        int minute = (int) (callTime - (hour * 3600)) / 60;
        int second = (int) callTime - (hour * 3600 + minute * 60);
        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":"
                + (second < 10 ? "0" + second : second);
    }
}
