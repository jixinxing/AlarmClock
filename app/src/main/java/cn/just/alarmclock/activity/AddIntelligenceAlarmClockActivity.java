package cn.just.alarmclock.activity;


import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.model.AlarmClock;
import cn.just.alarmclock.util.AlarmPreference;
import cn.just.alarmclock.util.RepeatPreference;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AddIntelligenceAlarmClockActivity extends PreferenceActivity
		implements OnPreferenceChangeListener, OnTimeSetListener,View.OnClickListener {

	private CheckBoxPreference mEnabledPref;
	private Preference mTimePref;
	private EditTextPreference mPositionDiscriblePref;
	private Preference mPositionPref;
	private Preference mDistancePref;
	private RepeatPreference mRepeatPref;
	private AlarmPreference mAlarmPref;
	private CheckBoxPreference mVibratePref;
	private EditTextPreference mLabelPref;
	
	private Button saveBtn;
	private Button revertBtn;
	private Button deleteBtn;

	private int mId;
	private int mHour;
	private int mMinutes;
	private double longitudeStr;
	private double latitudeStr;
	private int seekBarValue = 0;
	private int type=3;
	private boolean mTimePickerCancelled;
	private AlarmClock mOriginalAlarm;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_intelligence_alarmclock);
		addPreferencesFromResource(R.xml.intelligent_alarm_prefs);
		// 启用闹钟
		mEnabledPref = (CheckBoxPreference) findPreference("enabled");
		mEnabledPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (!mEnabledPref.isChecked()) {
							Toast.makeText(AddIntelligenceAlarmClockActivity.this,
									"智能闹钟启动", Toast.LENGTH_SHORT).show();
						}
						return AddIntelligenceAlarmClockActivity.this
								.onPreferenceChange(preference, newValue);
					}
				});
		// 时间
		mTimePref = findPreference("time");
		// 位置描述
		mPositionDiscriblePref = (EditTextPreference) findPreference("discrible");
		mPositionDiscriblePref.setSummary("默认");
		mPositionDiscriblePref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String val = (String) newValue;
						preference.setSummary(val);
						if (val != null
								&& !val.equals(mPositionDiscriblePref.getText())) {
							return AddIntelligenceAlarmClockActivity.this
									.onPreferenceChange(preference, newValue);
						}
						return true;
					}
				});
		// 位置
		mPositionPref = findPreference("position");

		// 距离
		mDistancePref = findPreference("distance");
		// 重复
		mRepeatPref = (RepeatPreference) findPreference("setRepeat");
		mRepeatPref.setOnPreferenceChangeListener(this);
		// 铃声
		mAlarmPref = (AlarmPreference) findPreference("alarm");
		mAlarmPref.setOnPreferenceChangeListener(this);
		// 振动
		mVibratePref = (CheckBoxPreference) findPreference("vibrate");
		mVibratePref.setOnPreferenceChangeListener(this);

		// 标签
		mLabelPref = (EditTextPreference) findPreference("label");
		mLabelPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String val = (String) newValue;
						mLabelPref.setSummary(val);
						if (val != null && !val.equals(mLabelPref.getText())) {
							return AddIntelligenceAlarmClockActivity.this
									.onPreferenceChange(preference, newValue);
						}
						return true;
					}
				});
		Intent i=getIntent();
		mId=i.getIntExtra(Alarms.ALARM_ID, -1);
		
		AlarmClock alarm=null;
		if(mId==-1){
			alarm=new AlarmClock(type);
		}else{
			alarm=Alarms.getAlarm(getContentResolver(), mId);
			longitudeStr=alarm.longitude;
			latitudeStr=alarm.latitude;
			seekBarValue=alarm.distance;
			if(alarm==null){
				finish();
				return;
			}
		}
		
		mOriginalAlarm=alarm;
		updatePrefs(mOriginalAlarm);
		getListView().setItemsCanFocus(true);
		
		saveBtn=(Button) findViewById(R.id.alarm_save);
		revertBtn=(Button) findViewById(R.id.alarm_revert);
		deleteBtn=(Button) findViewById(R.id.alarm_delete);
		saveBtn.setOnClickListener(this);
		revertBtn.setOnClickListener(this);
		
		revertBtn.setEnabled(false);
		if(mId==-1){
			deleteBtn.setEnabled(false);
		}else{
			deleteBtn.setOnClickListener(this);
		}
		
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mTimePref) {
			showTimePicker();
		}
		if (preference == mPositionPref) {
			Intent intent = new Intent(AddIntelligenceAlarmClockActivity.this,
					LocationBaiduMapActivity.class);
			startActivityForResult(intent, 1);
		}
		if (preference == mDistancePref) {
			showSeekBarDialog();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference != mEnabledPref) {
			mEnabledPref.setChecked(true);
		}
		revertBtn.setEnabled(true);
		return true;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alarm_save:
			saveAlarm();
			finish();
			break;
		case R.id.alarm_revert:
			int newId=mId;
			updatePrefs(mOriginalAlarm);
			if(mOriginalAlarm.id==-1){
				Alarms.deleteAlarm(AddIntelligenceAlarmClockActivity.this, newId);
			}else{
				saveAlarm();
			}
			revertBtn.setEnabled(false);
			break;
		case R.id.alarm_delete:
			deleteAlarm();
			break;
		default:
			break;
		}
		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			longitudeStr = data.getDoubleExtra("longitude", 0);
			latitudeStr = data.getDoubleExtra("latitude", 0);
			String str = String
					.format("经度：%f,纬度：%f", longitudeStr, latitudeStr);
			mPositionPref.setSummary(str);
		}
	}
	
	private void updatePrefs(AlarmClock alarm){
		mId=alarm.id;
		mHour=alarm.hour;
		mMinutes=alarm.minutes;
		mEnabledPref.setChecked(alarm.enabled);
		mPositionDiscriblePref.setSummary(alarm.position);
		mPositionDiscriblePref.setText(alarm.position);
		String str=String.format("经度：%f,纬度：%f", alarm.longitude,alarm.latitude);
		mPositionPref.setSummary(str);
		mDistancePref.setSummary(alarm.distance+"");
		mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
		mAlarmPref.setAlert(alarm.alert);
		mVibratePref.setChecked(alarm.vibrate);
		mLabelPref.setSummary(alarm.label);
		mLabelPref.setText(alarm.label);
		updateTime();
	}

	private void showTimePicker() {
		new TimePickerDialog(this, this, mHour, mMinutes,
				DateFormat.is24HourFormat(this)).show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mTimePickerCancelled = false;
		mHour = hourOfDay;
		mMinutes = minute;
		updateTime();
		mEnabledPref.setChecked(true);

	}

	private void updateTime() {
		mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinutes,
				mRepeatPref.getDaysOfWeek()));
	}
	
	private long  saveAlarm(){
		AlarmClock alarm=new AlarmClock(type);
		alarm.id=mId;
		alarm.enabled=mEnabledPref.isChecked();
		alarm.hour=mHour;
		alarm.minutes=mMinutes;
		alarm.position=mPositionDiscriblePref.getText();
		alarm.longitude=longitudeStr;
		alarm.latitude=latitudeStr;
		alarm.distance=seekBarValue;
		alarm.daysOfWeek=mRepeatPref.getDaysOfWeek();
		alarm.alert=mAlarmPref.getAlert();
		alarm.vibrate=mVibratePref.isChecked();
		alarm.label=mLabelPref.getText();
		alarm.type=type;
		long time;
		if(alarm.id==-1){
			time=Alarms.addAlarm(this, alarm);
			mId=alarm.id;
		}else{
			time=Alarms.setAlarm(this, alarm);
		}
		return time;
	}
	
	private void deleteAlarm(){
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.delete_alarm))
		.setMessage(getString(R.string.delete_alarm_confirm))
		.setPositiveButton(android.R.string.ok,
				new OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						Alarms.deleteAlarm(AddIntelligenceAlarmClockActivity.this,
								mId);
						finish();
					}
				}).setNegativeButton(android.R.string.cancel, null)
		.show();
	}

	// 调整距离的seekBar
	private void showSeekBarDialog() {
		View view = View.inflate(AddIntelligenceAlarmClockActivity.this,
				R.layout.show_seekbar_dialog, null);
		AlertDialog dialog = new AlertDialog.Builder(
				AddIntelligenceAlarmClockActivity.this).setView(view)
				.setTitle("距离进度条")
				.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDistancePref.setSummary(seekBarValue + "");
					}
				}).setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
		dialog.show();
		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		final TextView valueTV = (TextView) view.findViewById(R.id.value);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			// 停止拖动
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			// 拖动中会调用此方法
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			// 拖动中值在改变
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				valueTV.setText("当前值：" + progress);
				seekBarValue = progress;

			}
		});
	}

	

}
