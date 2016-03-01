package cn.just.alarmclock.activity;

import java.util.Calendar;

import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.controller.ToastMaster;
import cn.just.alarmclock.model.AlarmClock;
import cn.just.alarmclock.util.DigitalClock;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IntelligenceAlarmClockActivity extends Activity  {

	private RelativeLayout addIntelligenceAlarmRLayout;
	private LayoutInflater mFactory;
	private Cursor mCursor;
	private ListView mAlarmsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFactory = LayoutInflater.from(this);
		mCursor = Alarms.getAlarmsCursor(getContentResolver(), 3);
		initViews();
	}

	private void initViews() {
		setContentView(R.layout.activity_intelligence_alarmclock);
		
		mAlarmsList = (ListView) findViewById(R.id.intelligence_alarms_list);
		IntelligenceAlarmAdapter intelligenceAlarmAdapter=new IntelligenceAlarmAdapter(this, mCursor);
		mAlarmsList.setAdapter(intelligenceAlarmAdapter);
		mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnCreateContextMenuListener(this);
		
		addIntelligenceAlarmRLayout = (RelativeLayout) findViewById(R.id.intelligence_alarm_add_layout);
		addIntelligenceAlarmRLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(IntelligenceAlarmClockActivity.this,
						AddIntelligenceAlarmClockActivity.class);
				intent.putExtra(Alarms.ALARM_ID, -1);
				startActivity(intent);
			}
		});
		addIntelligenceAlarmRLayout
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						v.setSelected(hasFocus);
					}
				});

	}

	
	
	private void updateIndicatorAndAlarm(boolean enabled, ImageView bar,
			AlarmClock alarm) {
		bar.setImageResource(enabled ? R.mipmap.ic_indicator_on
				: R.mipmap.ic_indicator_off);
		Alarms.enableAlarm(this, alarm.id, enabled,alarm);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		mCursor.close();
	}

	private class IntelligenceAlarmAdapter extends CursorAdapter {

		public IntelligenceAlarmAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View ret = mFactory.inflate(
					R.layout.intelligence_alarmclock_listview_item, parent,
					false);
			DigitalClock digitalClock = (DigitalClock) ret
					.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			return ret;
		}

		@Override
		public void bindView(View view, final Context context, Cursor cursor) {

			final AlarmClock alarm = new AlarmClock(cursor);
			View indicator = view.findViewById(R.id.indicator);
			final ImageView barOnOff = (ImageView) indicator
					.findViewById(R.id.bar_onoff);
			barOnOff.setImageResource(alarm.enabled ? R.mipmap.ic_indicator_on
					: R.mipmap.ic_indicator_off);
			final CheckBox clockOnOff = (CheckBox) indicator
					.findViewById(R.id.clock_onoff);
			clockOnOff.setChecked(alarm.enabled);

			// 对checkBox设置监听，使里外一致
			indicator.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					clockOnOff.toggle();
					updateIndicatorAndAlarm(clockOnOff.isChecked(), barOnOff,
							alarm);
				}
			});
			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			Log.v("tag","hour="+alarm.hour+" ,minutes="+alarm.minutes);
			digitalClock.updateTime(c);
			digitalClock.setTypeface(Typeface.DEFAULT);

			TextView daysOfWeekView = (TextView) digitalClock
					.findViewById(R.id.daysOfWeek);
			String daysOfWeekStr = alarm.daysOfWeek.toString(
					IntelligenceAlarmClockActivity.this, false);
			if (daysOfWeekStr != null & daysOfWeekStr.length() != 0) {
				daysOfWeekView.setText(daysOfWeekStr);
				daysOfWeekView.setVisibility(View.VISIBLE);
			}else{
				daysOfWeekView.setVisibility(View.GONE);
			}
			
			TextView positionTV=(TextView) view.findViewById(R.id.position);
			String positionStr=alarm.position;
			if(positionStr!=null&&positionStr.length()!=0){
				positionTV.setText(positionStr);
				positionTV.setVisibility(View.VISIBLE);
			}else{
				positionTV.setText("默认");
			}
			
			TextView labelView=(TextView) view.findViewById(R.id.label);
			String labelStr=alarm.label;
			if(labelStr!=null&&labelStr.length()!=0){
				labelView.setText(labelStr);
				labelView.setVisibility(View.VISIBLE);
			}else{
				labelView.setVisibility(View.GONE);
			}
			
			final LinearLayout deleteLLayout = (LinearLayout) view
					.findViewById(R.id.delete_alarm_llayout);
			final LinearLayout cancleLLayout = (LinearLayout) view
					.findViewById(R.id.cancle_alarm_llayout);

			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							AddIntelligenceAlarmClockActivity.class);
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
