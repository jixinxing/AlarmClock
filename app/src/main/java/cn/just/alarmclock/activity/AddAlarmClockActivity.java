package cn.just.alarmclock.activity;

import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.controller.ToastMaster;
import cn.just.alarmclock.model.AlarmClock;
import cn.just.alarmclock.util.AlarmPreference;
import cn.just.alarmclock.util.RepeatPreference;


import android.R.integer;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddAlarmClockActivity extends PreferenceActivity implements
		TimePickerDialog.OnTimeSetListener,
		Preference.OnPreferenceChangeListener {

	private EditTextPreference mLabel;
	private CheckBoxPreference mEnabledPref;

	private Preference mTimePref;
	private AlarmPreference mAlarmPref;
	private CheckBoxPreference mVibratePref;
	private RepeatPreference mRepeatPref;

	private int mId;
	private int mHour;
	private int mMinutes;
	private boolean mTimePickerCancelled;
	private AlarmClock mOriginalAlarm;
	private int type = 1;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	
		setContentView(R.layout.activity_add_alarmclock);

		addPreferencesFromResource(R.xml.alarm_prefs);
		// 标签
		mLabel = (EditTextPreference) findPreference("label");
		mLabel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object newValue) {
				String val = (String) newValue;
				p.setSummary(val);
				if (val != null && !val.equals(mLabel.getText())) {
					return AddAlarmClockActivity.this.onPreferenceChange(p,
							newValue);
				}
				return true;
			}
		});
		// 启用闹钟
		mEnabledPref = (CheckBoxPreference) findPreference("enabled");
		mEnabledPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference p,
							Object newValue) {
						if (!mEnabledPref.isChecked()) {
							popAlarmSetToast(AddAlarmClockActivity.this, mHour,
									mMinutes, mRepeatPref.getDaysOfWeek());
						}
						return AddAlarmClockActivity.this.onPreferenceChange(p,
								newValue);
					}
				});
		// 时间
		mTimePref = findPreference("time");
		// 铃声
		mAlarmPref = (AlarmPreference) findPreference("alarm");
		mAlarmPref.setOnPreferenceChangeListener(this);

		// 振动
		mVibratePref = (CheckBoxPreference) findPreference("vibrate");
		mVibratePref.setOnPreferenceChangeListener(this);
		// 重复
		mRepeatPref = (RepeatPreference) findPreference("setRepeat");
		mRepeatPref.setOnPreferenceChangeListener(this);

		Intent i = getIntent();
		mId = i.getIntExtra(Alarms.ALARM_ID, -1);

		AlarmClock alarm = null;
		if (mId == -1) {
			alarm = new AlarmClock(type);
		} else {
			alarm = Alarms.getAlarm(getContentResolver(), mId);
			if (alarm == null) {
				finish();
				return;
			}
		}
		mOriginalAlarm = alarm;

		updatePrefs(mOriginalAlarm);
		getListView().setItemsCanFocus(true);

		Button b = (Button) findViewById(R.id.alarm_save);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAlarm();
				finish();
			}
		});
		final Button revert = (Button) findViewById(R.id.alarm_revert);
		revert.setEnabled(false);
		revert.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int newId = mId;
				updatePrefs(mOriginalAlarm);
				if (mOriginalAlarm.id == -1) {
					Alarms.deleteAlarm(AddAlarmClockActivity.this, newId);
				} else {
					saveAlarm();
				}
				revert.setEnabled(false);
			}
		});
		b = (Button) findViewById(R.id.alarm_delete);
		if (mId == -1) {
			b.setEnabled(false);
		} else {
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					deleteAlarm();
				}
			});
		}
		if (mId == -1) {
			mTimePickerCancelled = true;
		}
	}

	private static final Handler sHandler = new Handler();

	public boolean onPreferenceChange(final Preference p, Object newValue) {
		sHandler.post(new Runnable() {
			public void run() {
				if (p != mEnabledPref) {
					mEnabledPref.setChecked(true);
				}
				saveAlarmAndEnableRevert();
			}
		});
		return true;
	}

	private void updatePrefs(AlarmClock alarm) {
		mId = alarm.id;
		mEnabledPref.setChecked(alarm.enabled);
		mLabel.setText(alarm.label);
		mLabel.setSummary(alarm.label);
		mHour = alarm.hour;
		mMinutes = alarm.minutes;
		mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
		mVibratePref.setChecked(alarm.vibrate);
		mAlarmPref.setAlert(alarm.alert);
		updateTime();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mTimePref) {
			showTimePicker();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	// 时间选择器
	private void showTimePicker() {
		new TimePickerDialog(this, this, mHour, mMinutes,
				DateFormat.is24HourFormat(this)).show();
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mTimePickerCancelled = false;
		mHour = hourOfDay;
		mMinutes = minute;
		updateTime();
		mEnabledPref.setChecked(true);
		popAlarmSetToast(this, saveAlarm());
	}

	// 设置时间的小标题，如8：20上午
	private void updateTime() {
		mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinutes,
				mRepeatPref.getDaysOfWeek()));
	}

	private void saveAlarmAndEnableRevert() {
		final Button revert = (Button) findViewById(R.id.alarm_revert);
		revert.setEnabled(true);
	}

	// 保存闹钟
	private long saveAlarm() {
		AlarmClock alarm = new AlarmClock(type);
		alarm.id = mId;
		alarm.enabled = mEnabledPref.isChecked();
		alarm.hour = mHour;
		alarm.minutes = mMinutes;
		alarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
		alarm.vibrate = mVibratePref.isChecked();
		alarm.label = mLabel.getText();
		alarm.alert = mAlarmPref.getAlert();
		alarm.type = type;

		long time;
		// 新添加
		if (alarm.id == -1) {
			time = Alarms.addAlarm(this, alarm);
			mId = alarm.id;
		} else {
			// 修改
			time = Alarms.setAlarm(this, alarm);
		}
		return time;
	}

	// 删除闹钟
	private void deleteAlarm() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_alarm))
				.setMessage(getString(R.string.delete_alarm_confirm))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int w) {
								Alarms.deleteAlarm(AddAlarmClockActivity.this,
										mId);
								finish();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	static void popAlarmSetToast(Context context, int hour, int minute,
			AlarmClock.DaysOfWeek daysOfWeek) {
		popAlarmSetToast(context,
				Alarms.calculateAlarm(hour, minute, daysOfWeek)
						.getTimeInMillis());
	}

	public static void popAlarmSetToast(Context context, long timeInMillis) {
		String toastText = formatToast(context, timeInMillis);
		Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		ToastMaster.setToast(toast);
		toast.show();
	}

	static String formatToast(Context context, long timeInMillis) {
		long delta = timeInMillis - System.currentTimeMillis();
		long hours = delta / (1000 * 60 * 60);
		long minutes = delta / (1000 * 60) % 60;
		long days = hours / 24;
		hours = hours % 24;

		String daySeq = (days == 0) ? "" : (days == 1) ? context
				.getString(R.string.day) : context.getString(R.string.days,
				Long.toString(days));

		String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context
				.getString(R.string.minute) : context.getString(
				R.string.minutes, Long.toString(minutes));

		String hourSeq = (hours == 0) ? "" : (hours == 1) ? context
				.getString(R.string.hour) : context.getString(R.string.hours,
				Long.toString(hours));

		boolean dispDays = days > 0;
		boolean dispHour = hours > 0;
		boolean dispMinute = minutes > 0;

		int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0)
				| (dispMinute ? 4 : 0);

		String[] formats = context.getResources().getStringArray(
				R.array.alarm_set);
		return String.format(formats[index], daySeq, hourSeq, minSeq);
	}

}
