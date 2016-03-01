package cn.just.alarmclock.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.controller.ToastMaster;
import cn.just.alarmclock.model.AlarmClock;
import cn.just.alarmclock.util.DigitalClock;


import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 普通闹钟
 */
public class AlarmClockActivity extends Activity{

	public static final String PREFERENCES = "AlarmClock";
	static final boolean DEBUG = false;

	private LayoutInflater mFactory;
	private ListView mAlarmsList;
	//private LinearLayout deleteLLayout;
	private Cursor mCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 取自定义布局的LayoutInflater
		mFactory = LayoutInflater.from(this);
		// 获取闹钟的cursor 普通闹钟
		mCursor = Alarms.getAlarmsCursor(getContentResolver(), 1);
		SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES,
				0);
		Log.v("tag", "sharedPreferences: " + sharedPreferences);
		// 更新布局界面
		updateLayout();

	}

	// 加载更新界面布局
	private void updateLayout() {
		setContentView(R.layout.activity_alarmclock);
		
		mAlarmsList = (ListView) findViewById(R.id.alarms_list);
		AlarmTimeAdapter adapter = new AlarmTimeAdapter(this, mCursor);
		mAlarmsList.setAdapter(adapter);
		mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnCreateContextMenuListener(this);
		
		
		ImageView addAlarm = (ImageView) findViewById(R.id.addclock);
		addAlarm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addNewAlarm();
			}
		});
		addAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				v.setSelected(hasFocus);
			}
		});

	}

	private void addNewAlarm() {
		Intent intent = new Intent(AlarmClockActivity.this,
				AddAlarmClockActivity.class);
		startActivity(intent);
	}

	// 更新checkbox
	private void updateIndicatorAndAlarm(boolean enabled, ImageView bar,
			AlarmClock alarm) {
		bar.setImageResource(enabled ? R.mipmap.ic_indicator_on
				: R.mipmap.ic_indicator_off);
		Alarms.enableAlarm(this, alarm.id, enabled,alarm);
		if (enabled) {
			AddAlarmClockActivity.popAlarmSetToast(this, alarm.hour,
					alarm.minutes, alarm.daysOfWeek);
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		mCursor.close();
	}

	/**
	 * listview的适配器继承CursorAdapter
	 */
	private class AlarmTimeAdapter extends CursorAdapter {

		@SuppressWarnings("deprecation")
		public AlarmTimeAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View ret = mFactory.inflate(R.layout.alarmclock_listview_item,
					parent, false);

			DigitalClock digitalClock = (DigitalClock) ret
					.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			return ret;
		}

		// 把view绑定cursor的每一项
		public void bindView(final View view, final Context context, Cursor cursor) {
			final AlarmClock alarm = new AlarmClock(cursor);

			View indicatorView = view.findViewById(R.id.indicator);
			final ImageView barOnOffIV = (ImageView) indicatorView
					.findViewById(R.id.bar_onoff);
			barOnOffIV
					.setImageResource(alarm.enabled ? R.mipmap.ic_indicator_on
							: R.mipmap.ic_indicator_off);
			final CheckBox clockOnOffCB = (CheckBox) indicatorView
					.findViewById(R.id.clock_onoff);
			clockOnOffCB.setChecked(alarm.enabled);
			// 对checkbox设置监听，使里外一致
			indicatorView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					clockOnOffCB.toggle();
					updateIndicatorAndAlarm(clockOnOffCB.isChecked(),
							barOnOffIV, alarm);
				}
			});

			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			final Calendar c = Calendar.getInstance();

			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			digitalClock.updateTime(c);
			digitalClock.setTypeface(Typeface.DEFAULT);

			TextView daysOfWeekTV = (TextView) digitalClock
					.findViewById(R.id.daysOfWeek);
			final String daysOfWeekStr = alarm.daysOfWeek.toString(
					AlarmClockActivity.this, false);
			if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
				daysOfWeekTV.setText(daysOfWeekStr);
				daysOfWeekTV.setVisibility(View.VISIBLE);
			} else {
				daysOfWeekTV.setVisibility(View.GONE);
			}
			TextView labelTV = (TextView) view.findViewById(R.id.label);
			if (alarm.label != null && alarm.label.length() != 0) {
				labelTV.setText(alarm.label);
				labelTV.setVisibility(View.VISIBLE);
			} else {
				labelTV.setVisibility(View.GONE);
			}
			
			final LinearLayout deleteLLayout=(LinearLayout) view.findViewById(R.id.delete_alarm_llayout);
			final LinearLayout cancleLLayout=(LinearLayout) view.findViewById(R.id.cancle_alarm_llayout);
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, AddAlarmClockActivity.class);
					intent.putExtra(Alarms.ALARM_ID, (int) alarm.id);
					startActivity(intent);
					cancleLLayout.setVisibility(View.GONE);
					deleteLLayout.setVisibility(View.GONE);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					cancleLLayout.setVisibility(View.VISIBLE);
					deleteLLayout.setVisibility(View.VISIBLE);
					
					return false;
				}
			});
			cancleLLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					cancleLLayout.setVisibility(View.GONE);
					deleteLLayout.setVisibility(View.GONE);
				}
			});
			deleteLLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Alarms.deleteAlarm(context, alarm.id);
					cancleLLayout.setVisibility(View.GONE);
					deleteLLayout.setVisibility(View.GONE);
				}
			});
		
		}
	}

	

	
}