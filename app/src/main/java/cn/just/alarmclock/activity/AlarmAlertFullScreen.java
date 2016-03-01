package cn.just.alarmclock.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import java.util.Calendar;

import cn.just.alarmclock.R;
import cn.just.alarmclock.broadcast.AlarmReceiver;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.model.AlarmClock;
import cn.just.alarmclock.util.DigitalClock;


public class AlarmAlertFullScreen extends Activity {
	private static final String DEFAULT_SNOOZE = "10";
	private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
	protected static final String SCREEN_OFF = "screen_off";

	protected AlarmClock mAlarm;
	private int mVolumeBehavior;
	private Button snoozeBtn;
	private Button dismissBtn;
	private TextView titleTV;
	private TextView discribleTV;
	private DigitalClock digitalClockDC;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
				snooze();
			} else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
				dismiss(false);
			} else {
				AlarmClock alarm = intent
						.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
				if (alarm != null && mAlarm.id == alarm.id) {
					dismiss(true);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
		mAlarm = Alarms.getAlarm(getContentResolver(), mAlarm.id);
		final String vol = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
						DEFAULT_VOLUME_BEHAVIOR);
		Log.v("tag", "vol=" + vol);
		mVolumeBehavior = Integer.parseInt(vol);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}

		updateLayout();
		IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
		filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
		filter.addAction(Alarms.ALARM_DISMISS_ACTION);
		registerReceiver(mReceiver, filter);
	}

	private void setTitle() {
		String str = "";
		if (mAlarm.type == 1) {
			str = mAlarm.getLabelOrDefault1(this);
		}
		if (mAlarm.type == 2) {
			str = mAlarm.getLabelOrDefault2(this);
			digitalClockDC.setVisibility(View.GONE);
			discribleTV.setPadding(0, 8, 0, 8);
			discribleTV.setVisibility(View.VISIBLE);
			discribleTV.setText(mAlarm.position);
			
		}
		if (mAlarm.type == 3) {
			str = mAlarm.getLabelOrDefault3(this);

			discribleTV.setVisibility(View.VISIBLE);
			discribleTV.setText(mAlarm.position);

		}
		titleTV.setText(str);
	}

	private void updateLayout() {
		LayoutInflater inflater = LayoutInflater.from(this);

		setContentView(inflater.inflate(R.layout.alarm_alert, null));
		digitalClockDC=(DigitalClock) findViewById(R.id.digitalClock);
		titleTV = (TextView) findViewById(R.id.alertTitle);
		discribleTV = (TextView) findViewById(R.id.discrible_tv);// 描述
		snoozeBtn = (Button) findViewById(R.id.snooze);// 暂停
		snoozeBtn.requestFocus();
		snoozeBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				snooze();
			}
		});

		/* dismiss button: close notification */
		dismissBtn = (Button) findViewById(R.id.dismiss);// 取消
		dismissBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				dismiss(false);
			}
		});
		setTitle();
	}

	private void snooze() {
		if (!snoozeBtn.isEnabled()) {
			dismiss(false);
			return;
		}
		final String snooze = PreferenceManager.getDefaultSharedPreferences(
				this).getString(SettingsActivity.KEY_ALARM_SNOOZE,
				DEFAULT_SNOOZE);
		int snoozeMinutes = Integer.parseInt(snooze);

		final long snoozeTime = System.currentTimeMillis()
				+ (1000 * 60 * snoozeMinutes);
		Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id, snoozeTime);
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(snoozeTime);
		String label = mAlarm.getLabelOrDefault1(this);
		label = getString(R.string.alarm_notify_snooze_label, label);
		Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
		cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
		cancelSnooze.putExtra(Alarms.ALARM_ID, mAlarm.id);
		PendingIntent broadcast = PendingIntent.getBroadcast(this, mAlarm.id,
				cancelSnooze, 0);
		NotificationManager nm = getNotificationManager();
		Notification n = new Notification.Builder(this)
				.setContentTitle(label)
				.setSmallIcon(R.mipmap.stat_notify_alarm)
				.setContentInfo(this.getString(R.string.alarm_notify_snooze_text,Alarms.formatTime(this, c)))
				.build();

//		Notification n = new Notification(R.mipmap.stat_notify_alarm, label,
//				0);
//		n.setLatestEventInfo(
//				this,
//				label,
//				getString(R.string.alarm_notify_snooze_text,
//						Alarms.formatTime(this, c)), broadcast);
		n.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		nm.notify(mAlarm.id, n);

		String displayTime = getString(R.string.alarm_alert_snooze_set,
				snoozeMinutes);
		Toast.makeText(AlarmAlertFullScreen.this, displayTime,
				Toast.LENGTH_LONG).show();
		stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
		finish();
	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	private void dismiss(boolean killed) {
		if (!killed) {
			NotificationManager nm = getNotificationManager();
			nm.cancel(mAlarm.id);
			stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
		}
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

		setTitle();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
			snoozeBtn.setEnabled(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean up = event.getAction() == KeyEvent.ACTION_UP;
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_FOCUS:
			if (up) {
				switch (mVolumeBehavior) {
				case 1:
					snooze();
					break;

				case 2:
					dismiss(false);
					break;

				default:
					break;
				}
			}
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		return;
	}
}
