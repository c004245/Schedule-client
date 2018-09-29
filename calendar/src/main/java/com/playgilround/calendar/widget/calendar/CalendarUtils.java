package com.playgilround.calendar.widget.calendar;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 18-05-28
 * 달력 작업
 * CalendarUtils 객체 인스턴스 한번만 생성(Singleton)
 */
public class CalendarUtils {

    static final String TAG = CalendarUtils.class.getSimpleName();

    private static CalendarUtils sUtils;
    private Map<String, int[]> sAllHolidays = new HashMap<>(); //휴일Map

    private Map<String, List<Integer>> sMonthTaskHint = new HashMap<>();
    //캘린더작업 인스턴스 얻기
    public static synchronized CalendarUtils getInstance(Context context) {
        if (sUtils == null) {
            sUtils = new CalendarUtils();
            sUtils.initAllHolidays(context);
        }
        return sUtils;
    }

    //휴일 객체생성
    private void initAllHolidays(Context context) {
        try {
            //holiday.json에 2018년 1월~2019년 2월까지 임의로 생성함.
            InputStream is = context.getAssets().open("holiday.json"); //휴일 json
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int i;
            while ((i = is.read()) != -1) {//읽기
                baos.write(i); //기록
            }

            //java json변환 gson library

            //바로 타입을 준 후, 리스트에 담기
            sAllHolidays = new Gson().fromJson(baos.toString(), new TypeToken<Map<String, int[]>>() {

            }.getType());

            Log.d(TAG, "initAllHolidays -->" + sAllHolidays);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //holiday json int 배열 얻기
    public int[] getHolidays(int year, int month) {
        int holidays[];

        if (sUtils.sAllHolidays != null) {
            holidays = sUtils.sAllHolidays.get(year + "" + month);

            if (holidays == null) {
                holidays = new int[42];
            }
        } else {
            holidays = new int[42];
        }

        return holidays;
    }


    public static int getWeekRow(int year, int month, int day) {
        int week = getFirstDayWeek(year, month);
        Log.d(TAG, "getWeekRow -->" + week); //3? 수욜

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        int lastWeek = calendar.get(Calendar.DAY_OF_WEEK); //

        Log.d(TAG, "lastWeek -->" + lastWeek);
        if (lastWeek == 7) {
            day--;
        }
//24 + 6 / 7
        return (day + week -1) /7;

    }

    /**
     * Get two dates away for a few weeks
     */
    public static int getWeeksAgo(int lastYear, int lastMonth, int lastDay, int year, int month, int day) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        Log.d(TAG, "getWeeksAgo -->" + lastYear + "/" + lastMonth + "/" + lastDay + "/" + year + "/" + month + "/" + day);
        start.set(lastYear, lastMonth, lastDay);
        end.set(year, month, day);

        int week = start.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "getWeeksAgo week ->" + week);
        start.add(Calendar.DATE, -week);

        week = end.get(Calendar.DAY_OF_WEEK);
        end.add(Calendar.DATE, 7 - week);

        float v = (end.getTimeInMillis() - start.getTimeInMillis() / (3600 * 1000 * 24 * 7 * 1.0f));
        return (int) (v - 1);
    }
    /**
     * Get two dates away from several months
     * @param lastYear
     * @param lastMonth
     * @param year
     * @param month
     * @return
     */
    public static int getMonthsAgo(int lastYear, int lastMonth, int year, int month) {
        Log.d(TAG, "getMonthsAgo -->" + lastYear + "/" + lastMonth + "/" + year + "/" + month);
        return (year - lastYear) * 12 + (month - lastMonth);
    }
    /**
     * 년과 월을 통해 날짜 얻기
     *
     * @params year
     * @params month
     * @return
     */
    public static int getMonthDays(int year, int month) {
        month++;

        Log.d(TAG, "Month..->"  +month);
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;

            case 4:
            case 6:
            case 9:
            case 11:
                return 30;

            case 2:
                //윤년 생각해야함.
                Log.d(TAG, "year-->" + year); //2016, 2020...
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return -1;

        }
    }

    /**
     * 해당 이번연월이 몇주일지 계
     * @param year
     * @param month
     * @return
     */
    public static int getMonthRows(int year, int month) {
        int size = getFirstDayWeek(year, month) + getMonthDays(year, month) -1; //6(토요일)  + 30(11월)
        return size % 7 == 0 ? size /7 : (size / 7) + 1; //5주 아니면 6주..

    }

    /**
     * 요일에 현재 월 번호 1을 반환합니다.
     * 선택된 연 월에 매 1일 이 몇요일인지 판단
     */
    public static int getFirstDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);//현재 요일 (일요일은 1, 토요일은 7)
    }

    /**
     * 각각 국가공휴일/휴가 설정 해야함..
     * 추후에 서버에서
     */
    public static String getHolidayFromSolar(int year, int month, int day) {
        String message = "";

        //month = SelYear -1 이므로 1월1
        if (month == 0 && day == 1) {
            message = "새해";
        } else if (month == 1 && day == 14) { //2.14
            message = "발렌타인데이";
        } else if (month == 4 && day == 1) { //5.1
            message = "근로자의 날";
        } else if (month == 4 && day == 5) {// 5.5
            message = "어린이 날";
        } else if (month == 11 && day == 25) { //12.25
            message = "크리스마스";
        }
        return message;
    }

    //할 일 얻기
    public List<Integer> getTaskHints(int year, int month) {
        String key = hashKey(year, month);
        Log.d(TAG, "key --->" + key);

        List<Integer> hints = sUtils.sMonthTaskHint.get(key);

        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        }
        return hints;
    }

    //추가 테스크
    public boolean addTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);

        if (hints == null) {
            hints = new ArrayList<>();
            hints.add(day);
            sUtils.sMonthTaskHint.put(key, hints);
            return true;
        } else {
            if (!hints.contains(day)) {
                hints.add(day);
                return true;
            } else {
                return false;
            }
        }
    }
    //삭제 테스크
    public boolean removeTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);

        List<Integer> hints = sUtils.sMonthTaskHint.get(key);

        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            //태스크가 있으면.
            if (hints.contains(day)) {
                Iterator<Integer> i = hints.iterator();

                while (i.hasNext()) {
                    Integer next = i.next();
                    if (next == day) {
                        i.remove();
                        break;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //해당날짜에
    public List<Integer> addTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);

        if (hints == null) {
            hints = new ArrayList<>();
            hints.removeAll(days);
            hints.addAll(days);
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.addAll(days);
        }
        return hints;
    }

    public List<Integer> removeTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.removeAll(days);
        }
        return hints;
    }
    //hashKey
    private static String hashKey(int year, int month) {
        return String.format("%s:%s", year, month);
    }
}
