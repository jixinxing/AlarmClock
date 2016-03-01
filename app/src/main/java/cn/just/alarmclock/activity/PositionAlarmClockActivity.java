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
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.style.UpdateAppearance;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PositionAlarmClockActivity extends Activity {
	public static final String PREFERENCES = "PositionAlarmClock";
	private LayoutInflater mFactory;
	private Cursor mCursor;
	private ListView mAlarmsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mFactory = LayoutInflater.from(this);
		mCursor = Alarms.getAlarmsCursor(getContentResolver(), 2);

		initViews();

	}

	private void initViews() {
		setContentView(R.layout.activity_position_alarmclock);
		mAlarmsList = (ListView) findViewById(R.id.position_alarms_list);
		ImageView addPositionAalarmIV = (ImageView) findViewById(R.id.position_add_clock);

		PositionAlarmAdapter positionAlarmAdapter = new PositionAlarmAdapter(
				this, mCursor);
		mAlarmsList.setAdapter(positionAlarmAdapter);
		mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnCreateContextMenuListener(this);

		addPositionAalarmIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PositionAlarmClockActivity.this,
						AddPositionAlarmClockActivity.class);
				intent.putExtra(Alarms.ALARM_ID, -1);
				startActivity(intent);
			}
		});
		addPositionAalarmIV
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						v.setSelected(hasFocus);
					}
				});
	}

	void updateIndicatorAndAlarm(boolean enabled, ImageView bar,
			AlarmClock alarmClock) {
		bar.setImageResource(enabled ? R.mipmap.ic_indicator_on
				: R.mipmap.ic_indicator_off);
		Alarms.enableAlarm(this, alarmClock.id, enabled,alarmClock);
		

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		mCursor.close();
	}

	/**
	 * listView适配器
	 * 
	 * @author jixinxing
	 *
	 */

	private class PositionAlarmAdapter extends CursorAdapter {

		public PositionAlarmAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View ret = mFactory.inflate(
					R.layout.position_alarmclock_listview_item, parent, false);

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

			indicator.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					clockOnOff.toggle();
					updateIndicatorAndAlarm(clockOnOff.isChecked(), barOnOff,
							alarm);
				}
			});
			LinearLayout layout = (LinearLayout) view
					.findViewById(R.id.listview_item_llayout);
			TextView discribleTV = (TextView) view.findViewById(R.id.discrible);
			TextView labelTV = (TextView) view.findViewById(R.id.label);
			TextView daysOfWeekTV = (TextView) view
					.findViewById(R.id.daysOfWeek);

			String discribleStr = alarm.position;
			String labelStr = alarm.label;
			String daysOfWeekStr = alarm.daysOfWeek.toString(
					PositionAlarmClockActivity.this, false);

			if (discribleStr != null && !discribleStr.equals("")) {
				discribleTV.setText(discribleStr);
			} else {
				discribleTV.setText("默认");
			}

			if (daysOfWeekStr != null && !daysOfWeekStr.equals("")) {
				daysOfWeekTV.setText(daysOfWeekStr);
				daysOfWeekTV.setVisibility(View.VISIBLE);
			} else {
				daysOfWeekTV.setVisibility(View.GONE);
			}

			if (labelStr != null && !labelStr.equals("")) {
				labelTV.setText(labelStr);
				labelTV.setVisibility(View.VISIBLE);
			} else {
				labelTV.setVisibility(View.GONE);
			}

			final LinearLayout deleteLLayout = (LinearLayout) view
					.findViewById(R.id.delete_alarm_llayout);
			final LinearLayout cancleLLayout = (LinearLayout) view
					.findViewById(R.id.cancle_alarm_llayout);

			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							AddPositionAlarmClockActivity.class);
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
