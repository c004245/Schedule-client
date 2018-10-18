package com.playgilround.schedule.client.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.calendar.OnCalendarClickListener;
import com.playgilround.schedule.client.calendar.month.MonthCalendarView;
import com.playgilround.schedule.client.calendar.month.MonthView;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.playgilround.schedule.client.utils.DateUtils.date2TimeStamp;

/**
 * 18-06-11
 * 이미지 시계 버튼 클릭 시
 * 스케줄 입력 전 시작 시간을 정할 수있는 다이얼로그
 */
public class SelectDateDialog extends Dialog implements View.OnClickListener, OnCalendarClickListener {

    static final String TAG = SelectDateDialog.class.getSimpleName();
    private OnSelectDateListener mOnSelectDateListener;
    private String[] mMonthText;

    private TextView tvDate;
    private MonthCalendarView monthCalendar;

    private EditText etTime;

    private int mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay;
    public SelectDateDialog(Context context, OnSelectDateListener onSelectDateListener,
                            int year, int month, int day, int position) {

        super(context, R.style.DialogFullScreen);

        mOnSelectDateListener = onSelectDateListener;
        initView();

        Log.d(TAG, "month constructor -->" + month);

        initDate(year, month, day, position);
    }

    private void initView() {
        setContentView(R.layout.dialog_select_date);

        //월 배열 추가
        mMonthText = getContext().getResources().getStringArray(R.array.calendar_month);
        findViewById(R.id.tvCancel).setOnClickListener(this);
        findViewById(R.id.tvConfirm).setOnClickListener(this);

        tvDate = (TextView) findViewById(R.id.tvDate); //스케줄 달력 월표시
        monthCalendar = (MonthCalendarView) findViewById(R.id.sMonthCalendar);

        Log.d(TAG, "monthCalendar ->" + monthCalendar);
        monthCalendar.setOnCalendarClickListener(this);

        etTime = (EditText) findViewById(R.id.etTime);
        etTime.addTextChangedListener(mTextWatcher);

    }

    //http://egloos.zum.com/killins/v/3008925  --> TextWatcher EditText변경 탐지
    private TextWatcher mTextWatcher = new TextWatcher() {

        //start 지점에서 시작되는 count 갯수만큼의 글자들이
        //after 길이만큼의 글자로 대치되려고 할 때 호출된다
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (count == 3) {
                if (after == 4) {
                    etTime.removeTextChangedListener(mTextWatcher);
                    etTime.setText(String.format("%s%s", s.charAt(0), s.charAt(1)));
                    etTime.setSelection(etTime.getText().length());
                    etTime.addTextChangedListener(mTextWatcher);

                }
            }
        }

        //start 지점에서 시작되는 before 갯수만큼의 글자들이
        // count 갯수만큼의 글자들로 대치되었을 때 호출된다.
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 3) {
                //시:분 중간에 ':' 표시
                if (s.charAt(2) == ':') {
                    etTime.setText(String.format("%s", String.format("%s%s", s.charAt(0), s.charAt(1))));
                    etTime.setSelection(etTime.getText().length()); //커서 위치
                } else {
                    etTime.setText(String.format("%s:%s", String.format("%s%s", s.charAt(0), s.charAt(1)), s.charAt(2)));
                    etTime.setSelection(etTime.getText().length());
                }
            }
        }

        //EditText의 텍스트가 변경되면 호출된다.
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 2) {
                //시만 입력.
                Log.d(TAG, "s.length -> " + s.toString());
                if (isNumber(s.toString())) {
                    Integer hour = Integer.parseInt(s.toString());

                    //23 이상 숫자입력불가능.
                    if (hour > 23) {
                        etTime.removeTextChangedListener(mTextWatcher);
                        etTime.setText(String.valueOf(s.charAt(0)));

                        etTime.setSelection(etTime.getText().length());
                        etTime.addTextChangedListener(mTextWatcher);
                    }
                } else {
                    Toast.makeText(getContext(), "숫자만 입력하세요.", Toast.LENGTH_LONG).show();
                }
            } else if (s.length() == 5) {
                Log.d(TAG, "s.resultLength -> " + s.toString());
                if (isNumber(s.toString())) {
                    Integer min = Integer.parseInt(String.format("%s%s", s.charAt(3), s.charAt(4)));
                    //59 이상 분입력불가
                    if (min > 59) {
                        etTime.removeTextChangedListener(mTextWatcher);
                        etTime.setText(s.toString().substring(0, s.length() - 1));
                        etTime.setSelection(etTime.getText().length());
                        etTime.addTextChangedListener(mTextWatcher);
                    }
                } else {
                    Toast.makeText(getContext(), "숫자만 입력하세요..", Toast.LENGTH_LONG).show();
                }
            } else if (s.length() > 5) {
                etTime.removeTextChangedListener(mTextWatcher);
                etTime.setText(s.toString().substring(0, 5));
                etTime.setSelection(etTime.getText().length());
                etTime.addTextChangedListener(mTextWatcher);
            }
        }
    };

    public boolean isNumber(String str) {
        return str.matches("[0-9][0-9]");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvCancel:
                dismiss();
                break;
            case R.id.tvConfirm:
                confirm();
                dismiss();
                break;
        }
    }

    //확인 버튼
    private void confirm() {
        if (mOnSelectDateListener != null) {
            long time;

            String text = etTime.getText().toString();
            Log.d(TAG, "confirm text ==> " + text);

            if (TextUtils.isEmpty(text)) {
                time = 0;
            } else {
                //정규 표현식 숫자만 허용.
                Pattern timePattern1 = Pattern.compile("[0-9][0-9]:[0-9][0-9]");
                Pattern timePattern2 = Pattern.compile("[0-9][0-9]:[0-9]");

                Matcher timeFormat1 = timePattern1.matcher(text); //정규 표현식에 맞는 입력값인지.
                Matcher timeFormat2 = timePattern2.matcher(text);

                if (timeFormat1.matches() || timeFormat2.matches()) {
                    time = date2TimeStamp(String.format("%s-%s-%s %s", mCurrentSelectYear, mCurrentSelectMonth +1, mCurrentSelectDay, text),
                            "yyyy-MM-dd HH:mm");
                    Log.d(TAG, "all match ->" + mCurrentSelectYear + "--" + mCurrentSelectMonth + "--" + mCurrentSelectDay + "--" +time);

                } else {
                    Pattern hourPattern1 = Pattern.compile("[0-9][0-9]");
                    Pattern hourPattern2 = Pattern.compile("[0-9]");

                    Matcher hourFormat1 = hourPattern1.matcher(text);
                    Matcher hourFormat2 = hourPattern2.matcher(text);

                    if (hourFormat1.matches() || hourFormat2.matches()) {
                        time = date2TimeStamp(String.format("%s-%s-%s %s", mCurrentSelectYear, mCurrentSelectMonth +1, mCurrentSelectDay, text),
                                "yyyy-MM-dd HH");
                        Log.d(TAG, "little match --> " + mCurrentSelectYear + "--" + mCurrentSelectMonth + "--" + mCurrentSelectDay + "--"  + time);
                    } else {
                        Log.d(TAG, "not not -->" +  mCurrentSelectYear + mCurrentSelectMonth + mCurrentSelectDay);

                        time = 0;
                    }
                }
            }

            Log.d(TAG, "mCurrentSelect ==> " +mCurrentSelectYear + "-----" + mCurrentSelectMonth + "--" + time);
            mOnSelectDateListener.onSelectDate(mCurrentSelectYear, mCurrentSelectMonth +1, mCurrentSelectDay, time, monthCalendar.getCurrentItem());
        }
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        Log.d(TAG, "onCLickDate ->" + year + "/" + month + "/" + day);
        //click date
        setCurrentSelectDate(year, month, day);
    }

    @Override
    public void onPageChange(int year, int month, int day) {
        //page change
    }

    //날짜 기본 초기화
    private void initDate(int year, int month, int day, int position) {
        Log.d(TAG, "initData ==" + month + "//" + day);
        setCurrentSelectDate(year, month, day);
        if (position != -1) {
            monthCalendar.setCurrentItem(position, false);
            //View 트리에서 글로벌 변경 알림을 받을 수 있는 리스너를 등록
            //ViewTree의 뷰가 그려질 때마다
            monthCalendar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    MonthView monthView = monthCalendar.getCurrentMonthView();
                    monthView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
                    monthView.invalidate();
                }
            });
        }
    }

    private void setCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;

        Calendar calendar = Calendar.getInstance();
        if (year == calendar.get(Calendar.YEAR)) {
            Log.d(TAG, "mMonthText month ->" + month);
            tvDate.setText(mMonthText[month]); //오늘연도
        } else {
            Log.d(TAG, "mMonthText else-->" + month);
            Log.d(TAG, "day is -> " +day);

            if (day == 0) {
                Log.d(TAG, "set current time zone");
                mCurrentSelectYear = calendar.get(Calendar.YEAR);
                mCurrentSelectMonth = calendar.get(Calendar.MONTH);
                mCurrentSelectDay = calendar.get(Calendar.DAY_OF_MONTH);
                Log.d(TAG, "set time -> " + mCurrentSelectYear + "--" + mCurrentSelectMonth + "--" + mCurrentSelectDay);
            }
//            tvDate.setText(String.format("%s%s", String.format(getContext().getString(R.string.calendar_year), mCurrentSelectYear),
//                    mMonthText[mCurrentSelectMonth]));
            tvDate.setText(mMonthText[mCurrentSelectMonth]);
        }
    }

    //스케줄 날짜 클릭
        public interface OnSelectDateListener {
            void onSelectDate(int year, int month, int day, long time, int position);
        }
}

