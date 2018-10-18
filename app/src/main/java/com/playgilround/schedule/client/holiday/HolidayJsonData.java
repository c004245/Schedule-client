package com.playgilround.schedule.client.holiday;

import com.google.gson.annotations.SerializedName;

/**
 * 18-09-18
 * Json에 맞춰 Data클래스 생성
 * year, month, day, name
 */
public class HolidayJsonData {

    @SerializedName("id")
    public int id;
    @SerializedName("year")
    public int year;

    @SerializedName("month")
    public int month;

    @SerializedName("day")
    public int day;

    @SerializedName("name")
    public String name;
}
