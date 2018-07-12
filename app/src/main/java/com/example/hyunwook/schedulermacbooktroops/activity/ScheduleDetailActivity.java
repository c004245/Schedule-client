package com.example.hyunwook.schedulermacbooktroops.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.calendar.widget.calendar.CalendarUtils;
import com.example.common.base.app.BaseActivity;
import com.example.common.bean.EventSet;
import com.example.common.bean.Schedule;
import com.example.common.listener.OnTaskFinishedListener;
import com.example.common.util.ToastUtils;
import com.example.hyunwook.schedulermacbooktroops.R;
import com.example.hyunwook.schedulermacbooktroops.dialog.InputLocationDialog;
import com.example.hyunwook.schedulermacbooktroops.dialog.SelectDateDialog;
import com.example.hyunwook.schedulermacbooktroops.dialog.SelectEventSetDialog;
import com.example.hyunwook.schedulermacbooktroops.task.eventset.LoadEventSetMapTask;
import com.example.hyunwook.schedulermacbooktroops.task.schedule.UpdateScheduleTask;
import com.example.hyunwook.schedulermacbooktroops.utils.CalUtils;
import com.example.hyunwook.schedulermacbooktroops.utils.DateUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 18-06-29
 * 스케줄을 위치 등등, 자세하게 적을수있는 Activity
 */
public class ScheduleDetailActivity extends BaseActivity implements View.OnClickListener,
        OnTaskFinishedListener<Map<Integer, EventSet>>, SelectEventSetDialog.OnSelectEventSetListener, SelectDateDialog.OnSelectDateListener, InputLocationDialog.OnLocationBackListener {

    public static int UPDATE_SCHEDULE_CANCEL = 1;
    public static int UPDATE_SCHEDULE_FINISH = 2;

    private View vSchedule;
    private ImageView ivEventIcon;
    private EditText etTitle, etDesc;

    private TextView tvEventSet, tvTime, tvLocation;
    private Map<Integer, EventSet> mEventSetsMap;

    private Schedule mSchedule;
    public static String SCHEDULE_OBJ = "schedle.obj";
    public static String CALENDAR_POSITION = "calendar.position";

    private int mPosition = -1;

    private SelectEventSetDialog mSelectEventSetDialog;
    private SelectDateDialog mSelectDateDialog;
    private InputLocationDialog mInputLocationDialog;
    @Override
    protected void bindView() {
        setContentView(R.layout.activity_schedule_detail);
        TextView tvTitle = searchViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.schedule_event_detail_setting));

        searchViewById(R.id.tvCancel).setOnClickListener(this);
        searchViewById(R.id.tvFinish).setOnClickListener(this);
        searchViewById(R.id.llScheduleEventSet).setOnClickListener(this);
        searchViewById(R.id.llScheduleTime).setOnClickListener(this);
        searchViewById(R.id.llScheduleLocation).setOnClickListener(this);

        vSchedule = searchViewById(R.id.vScheduleColor);
        ivEventIcon = searchViewById(R.id.ivScheduleEventSetIcon);

        etTitle = searchViewById(R.id.etScheduleTitle);
        etDesc = searchViewById(R.id.etScheduleDesc);

        tvEventSet = searchViewById(R.id.tvScheduleEventSet);
        tvTime = searchViewById(R.id.tvScheduleTime);
        tvLocation = searchViewById(R.id.tvScheduleLocation);
    }

    @Override
    protected void initData() {
        super.initData();
        mEventSetsMap = new HashMap<>();
        mSchedule = (Schedule)getIntent().getSerializableExtra(SCHEDULE_OBJ);
        mPosition = getIntent().getIntExtra(CALENDAR_POSITION, -1);

        new LoadEventSetMapTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void bindData() {
        super.bindData();
        setScheduleData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvCancel:
                setResult(UPDATE_SCHEDULE_CANCEL);
                finish();
                break;
            case R.id.tvFinish:
                confirm();
                break;
            case R.id.llScheduleEventSet:
                //스케줄 제목적혀있는 레이아웃 클릭
                showSelectEventSetDialog();
                break;

            case R.id.llScheduleTime:
                //날짜 선택 레이아웃 클릭 시.
                showSelectDateDialog();
                break;

            case R.id.llScheduleLocation:
                //위치 선택 레이아웃 클릭
                showInputLocationDialog();
                break;

        }
    }

    //확인 버튼
    private void confirm() {
        if (etTitle.getText().length() != 0) {
            mSchedule.setTitle(etTitle.getText().toString());
            mSchedule.setDesc(etDesc.getText().toString());

            new UpdateScheduleTask(this, new OnTaskFinishedListener<Boolean>() {
                @Override
                public void onTaskFinished(Boolean data) {
                    setResult(UPDATE_SCHEDULE_FINISH);
                    finish();
                }
            }, mSchedule).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            ToastUtils.showShortToast(this, R.string.schedule_input_content_is_no_null);
        }
    }

    @Override
    public void onTaskFinished(Map<Integer, EventSet> data) {
        mEventSetsMap = data;
        EventSet eventSet = new EventSet();
        eventSet.setName(getString(R.string.menu_no_category));

        mEventSetsMap.put(eventSet.getId(), eventSet);

        EventSet current = mEventSetsMap.get(mSchedule.getEventSetId());
        if (current != null) {
            tvEventSet.setText(current.getName());

        }
    }

    //이벤트 설정 레이아웃클릭
    private void showSelectEventSetDialog() {
        if (mSelectEventSetDialog == null) {
            mSelectEventSetDialog = new SelectEventSetDialog(this, this, mSchedule.getEventSetId());
        }
        mSelectEventSetDialog.show();
    }


    //시간 설정 레이아웃클릭
    private void showSelectDateDialog() {
        if (mSelectDateDialog == null) {
            mSelectDateDialog = new SelectDateDialog(this, this, mSchedule.getYear(), mSchedule.getMonth(), mSchedule.getDay(), mPosition);
        }
        mSelectDateDialog.show();
    }

    //위치 설정 레이아웃 클릭
    private void showInputLocationDialog() {
        if (mInputLocationDialog == null) {
            mInputLocationDialog = new InputLocationDialog(this, this);
        }
        mInputLocationDialog.show();
    }

    private void setScheduleData() {
        vSchedule.setBackgroundResource(CalUtils.getEventSetColor(mSchedule.getColor()));//색상 설정
        ivEventIcon.setImageResource(mSchedule.getEventSetId() == 0 ? R.mipmap.ic_detail_category : R.mipmap.ic_detail_icon); //설정한 이벤트셋이 있다면.
        etTitle.setText(mSchedule.getTitle());
        etDesc.setText(mSchedule.getDesc()); //자세한 내용
        EventSet current = mEventSetsMap.get(mSchedule.getEventSetId());

        if (current != null) {
            tvEventSet.setText(current.getName()); //스케줄 이름
        }
        resetDateTimeUi();

        if (TextUtils.isEmpty(mSchedule.getLocation())) {
            tvLocation.setText(R.string.click_here_select_location);
        } else {
            tvLocation.setText(mSchedule.getLocation());
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectEventSetDialog.ADD_EVENT_SET_CODE) { //event set dialog에서 추가버튼을 누르고.
            if (resultCode == AddEventSetActivity.ADD_EVENT_SET_FINISH) {
                EventSet eventSet = (EventSet) data.getSerializableExtra(AddEventSetActivity.EVENT_SET_OBJ); //작업끝
                if (eventSet != null) {
                    mSelectEventSetDialog.addEventSet(eventSet);
                    /**
                     * 스케줄 분류 항목추가.
                     * 스케줄 분류 다이얼로그에서 항목을 추가했을 경우,
                     * Broadcast로 좌측메뉴에도 그 항목을 추가한다고 전송.
                     */
                    sendBroadcast(new Intent(MainActivity.ADD_EVENT_SET_ACTION).putExtra(AddEventSetActivity.EVENT_SET_OBJ, eventSet));
                }
            }
        }
    }

    private void resetDateTimeUi() {
        if (mSchedule.getTime() == 0) {
            if (mSchedule.getYear() != 0) {
                tvTime.setText(String.format(getString(R.string.date_format_no_time), mSchedule.getYear(), mSchedule.getMonth() +1, mSchedule.getDay()));
            } else {
                tvTime.setText(R.string.click_here_select_date);
            }
        } else {
            tvTime.setText(DateUtils.timeStamp2Date(mSchedule.getTime(), getString(R.string.date_format)));
        }
    }

    //스케줄 목록클릭
   @Override
    public void onSelectEventSet(EventSet eventSet) {
        mSchedule.setColor();
    }

}
