package com.playgilround.schedule.client.schedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.calendar.CalendarUtils;
import com.playgilround.schedule.client.calendar.OnCalendarClickListener;
import com.playgilround.schedule.client.month.MonthCalendarView;
import com.playgilround.schedule.client.month.MonthView;
import com.playgilround.schedule.client.week.WeekCalendarView;
import com.playgilround.schedule.client.week.WeekView;


import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

/**
 * 18-05-25
 * 스케줄 표시 레이아웃
 * 실질적 표시
 */
public class ScheduleLayout extends FrameLayout {

    private float mDownPosition[] = new float[2];
    private boolean mIsScrolling = false;
//    private MonthCalendarView monthView;
    static final String TAG = ScheduleLayout.class.getSimpleName();
    private final int DEFAULT_MONTH = 0;
    private final int DEFAULT_WEEK = 1;
    private int mDefaultView;
    private boolean mIsAutoChangeMonthRow;

    private boolean mCurrentRowsIsSix = true;

    private int mRowSize;
    private int mMinDistance;
    private int mAutoScrollDistance;

    private int mCurrentSelectYear;
    private int mCurrentSelectMonth;
    private int mCurrentSelectDay;

    private MonthCalendarView monthCalendar;
    private WeekCalendarView weekCalendar;
    //터치 이벤트 처리
    private GestureDetector mGestureDetector;

    private RelativeLayout rlMonthCalendar;
    private RelativeLayout rlScheduleList;

    private OnCalendarClickListener mOnCalendarClickListener;
    private ScheduleState mState;

    private ScheduleRecyclerView rvScheduleList;

    public ScheduleLayout(Context context) {
        this(context, null);
    }

    public ScheduleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ScheduleLayout));
        initDate();
        initGestureDetector();

    }

    //스케줄 사이즈 설정
    public void initAttrs(TypedArray array) {
        mDefaultView = array.getInt(R.styleable.ScheduleLayout_default_view, DEFAULT_MONTH);
        mIsAutoChangeMonthRow = array.getBoolean(R.styleable.ScheduleLayout_auto_change_month_row, false);
        array.recycle();

        //스케줄 상태
        mState = ScheduleState.OPEN;
        mRowSize = getResources().getDimensionPixelSize(R.dimen.week_calendar_height);
        mMinDistance = getResources().getDimensionPixelSize(R.dimen.calendar_min_distance);
        mAutoScrollDistance = getResources().getDimensionPixelSize(R.dimen.auto_scroll_distance);

    }

    //Date 얻기
    private void initDate() {
        Calendar calendar = Calendar.getInstance();
        resetCurrentSelectDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

    }

    //현재 선택한 날짜 재설정
    private void resetCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear  = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;

        Log.d(TAG, "current day -->" + mCurrentSelectYear + "-" + mCurrentSelectMonth + "-" + mCurrentSelectDay);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new OnScheduleScrollListener(this));
    }


    /**
     * xml 로 부터 모든 뷰를 inflate 를 끝내고 실행.
     * 대부분 이 함수에서는 각종 변수 초기화가 이루어짐.
     * super 메소드에서는 아무것도하지않기 때문에 쓰지 않는다.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        monthCalendar = (MonthCalendarView) findViewById(R.id.monthCalendar);
        weekCalendar = (WeekCalendarView) findViewById(R.id.weekCalendar);

        rlMonthCalendar = (RelativeLayout) findViewById(R.id.rlMonthCalendar);

        rlScheduleList = (RelativeLayout) findViewById(R.id.rlScheduleList);
        rvScheduleList = (ScheduleRecyclerView) findViewById(R.id.rvScheduleList);
        bindingMonthAndWeekCalendar();
    }


    //binding
    private void bindingMonthAndWeekCalendar() {
        monthCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
//        weekCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);

        //view init
        Calendar calendar = Calendar.getInstance();
        if (mIsAutoChangeMonthRow) {
            mCurrentRowsIsSix = CalendarUtils.getMonthRows(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) == 6;
        }

        //mDefaultView == 0
        if (mDefaultView == DEFAULT_MONTH) {
            Log.d(TAG, "binding mDefaultView is DEFAULT_MONTH");
//            weekCalendar.setVisibility(INVISIBLE);
            mState = ScheduleState.OPEN;

            if (!mCurrentRowsIsSix) {
                rlScheduleList.setY(rlScheduleList.getY() - mRowSize);
            }
        } else if (mDefaultView == DEFAULT_WEEK) {
            Log.d(TAG, "binding mDefaultView is DEFAULT_WEEK");
            weekCalendar.setVisibility(VISIBLE);
            mState = ScheduleState.CLOSE;
            int row = CalendarUtils.getWeekRow(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            rlMonthCalendar.setX(-row * mRowSize);
            rlScheduleList.setY(rlScheduleList.getY() - 5 * mRowSize);
        }
    }



    //MonthCalendarView 클릭
    private OnCalendarClickListener mMonthCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            Log.d(TAG, "onCLickDate MonthCalendarView..");
//            weekCalendar.setOnCalendarClickListener(null); //week는 클릭안되게.

            Log.d(TAG, "month calendar click listener -->" + mCurrentSelectYear + "/" + mCurrentSelectMonth + "/" + mCurrentSelectDay +"/" +year + "/" +month + "/" + day);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, day);
            //year,month, day 클릭된 날짜.
            Log.d(TAG, "Week -->" + weeks);
            resetCurrentSelectDate(year, month, day);

            int position = weekCalendar.getCurrentItem() + weeks;
            if (weeks != 0) {
                weekCalendar.setCurrentItem(position, false);
            }
            Log.d(TAG, "Month Position ->" + position);
//
            resetWeekView(position);
//            weekCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            computeCurrentRowsIsSix(year, month);
        }
    };

    //WeekCalendarView 클릭
     private OnCalendarClickListener mWeekCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            monthCalendar.setOnCalendarClickListener(null);

            Log.d(TAG, "week calendar click listener -->" + mCurrentSelectYear + "/" + mCurrentSelectMonth + "/" + year + "/" + month);
            int months = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);

            resetCurrentSelectDate(year, month, day);

            if (months != 0) {
                int position = monthCalendar.getCurrentItem() + months;
                monthCalendar.setCurrentItem(position, false);
            }

            resetMonthView();
            monthCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);

            if (mIsAutoChangeMonthRow) {
                mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6; //이번달은 6주인가 ?
            }
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            //page 변경
            if (mIsAutoChangeMonthRow) {
                if (mCurrentSelectMonth != month) {
                    mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6;
                }
            }
        }
    };

     public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
         mOnCalendarClickListener = onCalendarClickListener;
     }
    //MonthView 재설정
    private void resetMonthView() {
         MonthView monthView = monthCalendar.getCurrentMonthView();

         if (monthView != null) {
             monthView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
             monthView.invalidate();
         }

         if (mOnCalendarClickListener != null) {
             mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
         }

         resetCalendarPosition();
    }

    private void resetCalendarPosition() {
         Log.d(TAG, "resetCalendarPosition");
         if (mState == ScheduleState.OPEN) {
             rlMonthCalendar.setY(0);

             if (mCurrentRowsIsSix) {
                 rlScheduleList.setY(monthCalendar.getHeight());
             } else {
                 rlScheduleList.setY(monthCalendar.getHeight() - mRowSize);
             }
         } else {
             rlMonthCalendar.setY(-CalendarUtils.getWeekRow(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay) * mRowSize);
             rlScheduleList.setY(mRowSize);
         }
    }
    //WeekView 재설정
    private void resetWeekView(int position) {
        WeekView weekView = weekCalendar.getCurrentWeekView();

        if (weekView != null) {
            weekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            weekView.invalidate();
        } else {
            WeekView newWeekView = weekCalendar.getWeekAdapter().instanceWeekView(position);
            newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            newWeekView.invalidate();
            weekCalendar.setCurrentItem(position);
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }

    //현재 rows 계산
    private void computeCurrentRowsIsSix(int year, int month) {
        if (mIsAutoChangeMonthRow) {
            boolean isSixRow = CalendarUtils.getMonthRows(year, month) == 6;//최대 6주면 true
            if (mCurrentRowsIsSix != isSixRow) {
                mCurrentRowsIsSix = isSixRow;

                Log.d(TAG, "mRowSize compute -->" + mRowSize);
                if (mState == ScheduleState.OPEN) {
                    if (mCurrentRowsIsSix) {
                        AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, mRowSize);
                        rlScheduleList.startAnimation(animation);
                    } else {
                        AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, -mRowSize);
                        rlScheduleList.startAnimation(animation);
                    }
                }
            }
        }
    }

    //캘린더 스크롤 시..
    protected void onCalendarScroll(float distanceY) {
        MonthView monthView = monthCalendar.getCurrentMonthView();
        Log.d(TAG, "mcView getCurrentMonthView --> " + monthView);

        distanceY = Math.min(distanceY, mAutoScrollDistance);
        float calendarDistanceY = distanceY / (mCurrentRowsIsSix ? 5.0f : 4.0f);

        int row = monthView.getWeekRow() -1;
        Log.d(TAG, "onCalendarScroll row -->" + row);

        int calendarTop = -row * mRowSize;

        int scheduleTop = mRowSize;

        float calendarY = rlMonthCalendar.getY() - calendarDistanceY * row;
        calendarY = Math.min(calendarY, 0);
        calendarY = Math.max(calendarY, calendarTop);
        rlMonthCalendar.setY(calendarY);

        float scheduleY = rlScheduleList.getY() - distanceY;
        if (mCurrentRowsIsSix) {
            scheduleY = Math.min(scheduleY, monthCalendar.getHeight());
        } else {
            scheduleY = Math.min(scheduleY, monthCalendar.getHeight() - mRowSize);
        }
        scheduleY = Math.max(scheduleY, scheduleTop);
        rlScheduleList.setY(scheduleY);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int height = MeasureSpec.getSize(heightMeasureSpec);

         resetViewHeight(rlScheduleList, height - mRowSize);
         resetViewHeight(this, height);
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    //뷰 높이 재설정
    private void resetViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
         switch (ev.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
                 mDownPosition[0] = ev.getRawX();
                 mDownPosition[1] = ev.getRawY();
                 mGestureDetector.onTouchEvent(ev);
                 break;
         }
         return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
                 mDownPosition[0] = event.getRawX();
                 mDownPosition[1] = event.getRawY();
                 resetCalendarPosition();
                 return true;
             case MotionEvent.ACTION_MOVE:
                 transferEvent(event);
                 mIsScrolling = true;
                 return true;
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_CANCEL:
                 transferEvent(event);
                 changeCalendarState();
                 resetScrollingState();
                 return true;

         }
         return super.onTouchEvent(event);
    }

    private void transferEvent(MotionEvent event) {
         if (mState == ScheduleState.CLOSE) {
             monthCalendar.setVisibility(VISIBLE);
//             weekCalendar.setVisibility(INVISIBLE);
             mGestureDetector.onTouchEvent(event);
         } else {
             mGestureDetector.onTouchEvent(event);
         }
    }

    //캘린더 상태바꾸
    private void changeCalendarState() {
        if (rlScheduleList.getY() > mRowSize * 2 &&
                rlScheduleList.getY() < monthCalendar.getHeight() - mRowSize) { //middle
            ScheduleAnimation animation = new ScheduleAnimation(this, mState, mAutoScrollDistance);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    changeState();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else if (rlScheduleList.getY() <= mRowSize * 2) { //상단
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.OPEN, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.OPEN) {
                        changeState();
                    } else {
                        resetCalendar();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);


        } else {
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.CLOSE, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.CLOSE) {
                        mState = ScheduleState.OPEN;
                    }
                }


                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        }
    }

    private void resetCalendar() {
         if (mState == ScheduleState.OPEN) {
             monthCalendar.setVisibility(VISIBLE);
             weekCalendar.setVisibility(INVISIBLE);
         } else {
             monthCalendar.setVisibility(INVISIBLE);
             weekCalendar.setVisibility(VISIBLE);
         }
    }
    private void changeState() {

             //캘린더가 열려있으면 위로 닫는다.
             if (mState == ScheduleState.OPEN) {
                 mState = ScheduleState.CLOSE;

                 monthCalendar.setVisibility(INVISIBLE);
//                 weekCalendar.setVisibility(VISIBLE);
                 rlMonthCalendar.setY((1 - monthCalendar.getCurrentMonthView().getWeekRow()) * mRowSize);
//                 checkWeekCalendar();
             } else {
                 mState = ScheduleState.OPEN;
                 monthCalendar.setVisibility(VISIBLE);
//                 weekCalendar.setVisibility(INVISIBLE);
                 rlMonthCalendar.setY(0);
             }
     }

     private void resetScrollingState() {
         mDownPosition[0] = 0;
         mDownPosition[1] = 0;
         mIsScrolling = false;
     }

     private void checkWeekCalendar() {
         WeekView weekView = weekCalendar.getCurrentWeekView();
         DateTime start = weekView.getStartDate();
         DateTime end = weekView.getEndDate();
         DateTime current = new DateTime(mCurrentSelectYear, mCurrentSelectMonth + 1, mCurrentSelectDay, 23, 59, 59);

         int week = 0;
         while (current.getMillis() < start.getMillis()) {
             week--;
             start = start.plusDays(-7);
         }
         current = new DateTime(mCurrentSelectYear, mCurrentSelectMonth + 1, mCurrentSelectDay, 0, 0, 0);
         if (week == 0) {
             while (current.getMillis() > end.getMillis()) {
                 week++;
                 end = end.plusDays(7);
             }
         }
         if (week != 0) {
             int position = weekCalendar.getCurrentItem() + week;
             if (weekCalendar.getWeekViews().get(position) != null) {
                 weekCalendar.getWeekViews().get(position).setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
                 weekCalendar.getWeekViews().get(position).invalidate();
             } else {
                 WeekView newWeekView = weekCalendar.getWeekAdapter().instanceWeekView(position);
                 newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
                 newWeekView.invalidate();
             }
             weekCalendar.setCurrentItem(position, false);
         }
     }

    /**
     * ViewGroup의 dispatchTouchEvent의 로직을 대신 담당하여,
     * 자신에게 속한 하위뷰에게 이벤트를 전달할지 결정한다.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsScrolling) {
            return true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float distanceX = Math.abs(x - mDownPosition[0]);
                float distanceY = Math.abs(y - mDownPosition[1]);

                if (distanceY > mMinDistance && distanceY > distanceX * 2.0f) {
                    return (y > mDownPosition[1] && isRecyclerViewTouch()) ||
                            (y < mDownPosition[1] && mState == ScheduleState.OPEN);
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    //recycler touch
    private boolean isRecyclerViewTouch() {
        return mState == ScheduleState.CLOSE && (rvScheduleList.getChildCount() == 0 ||
            rvScheduleList.isScrollTop());
    }

    private void resetMonthViewDate(final int year, final int month, final int day, final int position) {
        if (monthCalendar.getMonthViews().get(position) == null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetMonthViewDate(year, month, day, position);
                }
            }, 50);
        } else {
            monthCalendar.getMonthViews().get(position).clickThisMonth(year, month, day);
        }
    }

    //초기 날짜
    public void initData(int year, int month, int day) {
        int monthDis = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
        int position = monthCalendar.getCurrentItem() + monthDis;
        monthCalendar.setCurrentItem(position);
        resetMonthViewDate(year, month, day, position);
    }

    /**
     * 태스크 추가
     */
    public void addTaskHints(List<Integer> hints ) {
        CalendarUtils.getInstance(getContext()).addTaskHints(mCurrentSelectYear, mCurrentSelectMonth, hints);
        if (monthCalendar.getCurrentMonthView() != null) {
            monthCalendar.getCurrentMonthView().invalidate();
        }

        if (weekCalendar.getCurrentWeekView() != null) {
            weekCalendar.getCurrentWeekView().invalidate();
        }
    }


    public void removeTaskHints(List<Integer> hints) {
        CalendarUtils.getInstance(getContext()).removeTaskHints(mCurrentSelectYear, mCurrentSelectMonth, hints);
        if (monthCalendar.getCurrentMonthView() != null) {
            monthCalendar.getCurrentMonthView().invalidate();
        }
        if (weekCalendar.getCurrentWeekView() != null) {
            weekCalendar.getCurrentWeekView().invalidate();
        }
    }
    /**
     * 태스크 삭제
     * @return
     */
    public void removeTaskHint(Integer day) {
        if (monthCalendar.getCurrentMonthView() != null) {
            if (monthCalendar.getCurrentMonthView().removeTaskHint(day)) {
                if (weekCalendar.getCurrentWeekView() != null) {
                    weekCalendar.getCurrentWeekView().invalidate();
                }
            }
        }
    }

    //add hint
    public void addTaskHint(Integer day) {
        if (monthCalendar.getCurrentMonthView() != null) {
            if (monthCalendar.getCurrentMonthView().addTaskHint(day)) {

                if (weekCalendar.getCurrentWeekView() != null) {
                    weekCalendar.getCurrentWeekView().invalidate();
                }
            }
        }
    }

    public ScheduleRecyclerView getSchedulerRecyclerView() {
         return rvScheduleList;
    }

    public MonthCalendarView getMonthCalendar() {
        return monthCalendar;
    }
}
