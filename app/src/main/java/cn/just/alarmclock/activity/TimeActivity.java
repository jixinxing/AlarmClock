package cn.just.alarmclock.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.just.alarmclock.R;

/**
 *时间
 */

public class TimeActivity extends Activity {
	ImageView default_clock_second, default_clock_minute, default_clock_hour;
	int hour;
	int minute;
	int second;
	Handler handler = new Handler();
	Time t = new Time();
	Button xiugaisystemtime;
	RotateAnimation houranimation, minuteanimation, secondanimation;
	TextView londomtime, newyorktime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time);
		default_clock_second = (ImageView) this
				.findViewById(R.id.default_clock_secondtime);
		default_clock_minute = (ImageView) this
				.findViewById(R.id.default_clock_minutetime);
		default_clock_hour = (ImageView) this
				.findViewById(R.id.default_clock_hourtime);
		xiugaisystemtime = (Button) this.findViewById(R.id.xiugaisystemtime);
		londomtime = (TextView) this.findViewById(R.id.londomtime);
		newyorktime = (TextView) this.findViewById(R.id.newyorktime);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		handler.post(runnable);
		xiugaisystemtime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent("/");
				ComponentName cm = new ComponentName("com.android.settings",
						"com.android.settings.DateTimeSettingsSetupWizard");
				intent.setComponent(cm);
				intent.setAction("android.intent.action.VIEW");
				startActivity(intent);
			}
		});
		super.onStart();
	}

	float fromDegreeshour = 0;
	float fromDegreesminute = 0;
	float fromDegreessecond = 0;

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			t.setToNow(); // ȡ��ϵͳʱ�䡣
			hour = t.hour; // 0-23
			minute = t.minute;
			second = t.second;
			handler.postDelayed(runnable, 200);

			houranimation = new RotateAnimation(fromDegreeshour,
					fromDegreeshour, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			houranimation.setDuration(100);
			houranimation.setFillAfter(true);
			default_clock_hour.startAnimation(houranimation);
			fromDegreeshour = hour * 15 / 2;

			minuteanimation = new RotateAnimation(fromDegreesminute,
					fromDegreesminute, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			minuteanimation.setDuration(100);
			minuteanimation.setFillAfter(true);
			default_clock_minute.startAnimation(minuteanimation);
			fromDegreesminute = minute * 6;

			secondanimation = new RotateAnimation(fromDegreessecond,
					fromDegreessecond, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			secondanimation.setDuration(100);
			secondanimation.setFillAfter(true);
			default_clock_second.startAnimation(secondanimation);
			fromDegreessecond = second * 6;

			String a = String.valueOf(minute);

			if (a.length() == 1) {
				a = "0" + a;
			}
			if (hour >= 7) {
				londomtime.setText(hour - 7 + ":" + a);
			} else {
				londomtime.setText(24 - 7 + hour + ":" + a);
			}
			if (hour >= 12) {
				newyorktime.setText(hour - 12 + ":" + a);
			} else {
				newyorktime.setText(24 - 12 + hour + ":" + a);
			}
		}
	};
}
