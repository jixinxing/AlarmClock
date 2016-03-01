package cn.just.alarmclock.activity;

import java.util.Timer;
/**
 * 倒时
 */
import java.util.TimerTask;

import cn.just.alarmclock.R;
import cn.just.alarmclock.model.SaveRun;
import cn.just.alarmclock.model.ScreenInfo;
import cn.just.alarmclock.model.WheelMain;
import cn.just.alarmclock.util.SlipButton;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CountdownActivity extends Activity {
	WheelMain wheelMain;
	LinearLayout timepickerlin;
	Button btnselecttime, daojishijicubutton, startbuttondaoji;
	RelativeLayout listjishi;
	private Timer timer = null;
	private TimerTask task = null;
	private Handler handler = null;
	private Message msg = null;
	private ImageView min_progress, min_progress_hand, second_progress_hand,
			second_progress, hour_progress_hand, hour_progress;
	Animation rotateAnimation, secondrotateAnimation, hourrotateAnimation;
	float predegree = 0;
	float secondpredegree = 0;
	float hourpredegree = 0;
	LinearLayout hoursoflinear, startandcannellin;
	int mlCount = -1;
	TextView tvTime, hours;
	private SlipButton ringtixing, screenon;
	boolean ring = true;
	static boolean screen = true;
	MediaPlayer mediaPlayer;

	@SuppressLint({ "Wakelock", "HandlerLeak" })
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_countdown);
		timepickerlin = (LinearLayout) findViewById(R.id.timepickerlin);
		listjishi = (RelativeLayout) findViewById(R.id.daojishirelativ);
		
		btnselecttime = (Button) findViewById(R.id.daojishistartbutton);
		ringtixing = (SlipButton) findViewById(R.id.ringtixing);
		screenon = (SlipButton) findViewById(R.id.scroonlisht);
		screenon.setChecked(true);
		ringtixing.setChecked(true);
		


		screenon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (screen) {
					screenon.setChecked(false);
					screen = false;
				} else {
					screenon.setChecked(true);
					screen = true;
				}
			}
		});

		ringtixing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ring) {
					ringtixing.setChecked(false);
					ring = false;
				} else {
					ringtixing.setChecked(true);
					ring = true;
				}
			}
		});
		daojishijicubutton = (Button) findViewById(R.id.daojishijicubutton);
		startbuttondaoji = (Button) findViewById(R.id.startbuttondaoji);
		tvTime = (TextView) findViewById(R.id.daojishitvTime);
		hours = (TextView) findViewById(R.id.daojishihours);

		min_progress = (ImageView) this.findViewById(R.id.daojishimin_progress);
		min_progress_hand = (ImageView) this
				.findViewById(R.id.daojishimin_progress_hand);
		second_progress_hand = (ImageView) this
				.findViewById(R.id.daojishisecond_progress_hand);
		second_progress = (ImageView) this
				.findViewById(R.id.daojishisecond_progress);
		hour_progress_hand = (ImageView) this
				.findViewById(R.id.daojishihour_progress_hand);
		hour_progress = (ImageView) this
				.findViewById(R.id.daojishihour_progress);
		hoursoflinear = (LinearLayout) this
				.findViewById(R.id.daojishihoursoflinear);
		startandcannellin = (LinearLayout) this
				.findViewById(R.id.startandcannellin);
		LayoutInflater inflater = LayoutInflater.from(CountdownActivity.this);
		final View timepickerview = inflater.inflate(R.layout.timepicker, null);
		ScreenInfo screenInfo = new ScreenInfo(CountdownActivity.this);
		wheelMain = new WheelMain(timepickerview);
		wheelMain.screenheight = screenInfo.getHeight();
		wheelMain.initDateTimePicker(0, 0, 0);
		timepickerlin.addView(timepickerview);
		SaveRun.setisdaojishi(false);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					mlCount--;
					if (mlCount <= 0) {
						enddaojishi();
					}
					if (screen) {
						MainActivityWithViewPager.seekBar.setProgress(1);
					} else {
						MainActivityWithViewPager.seekBar.setProgress(2);
					}
					int totalSec = 0;
					int yushu = 0;
					totalSec = (int) (mlCount / 10);
					yushu = (int) (mlCount % 10);
					int min = (totalSec / 60);
					if (min >= 60) {
						hoursoflinear.setVisibility(View.VISIBLE);
						hours.setText(String.valueOf(min / 60));
						min = min % 60;
					} else {
						hoursoflinear.setVisibility(View.GONE);
					}
					int sec = (totalSec % 60);
					try {
						rotateAnimation = new RotateAnimation(predegree,
								(float) (0.6 * mlCount),
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						secondrotateAnimation = new RotateAnimation(
								secondpredegree, (float) (36.0 * mlCount),
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						hourrotateAnimation = new RotateAnimation(
								hourpredegree, (float) (mlCount / 100),
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						rotateAnimation.setDuration(100);
						secondrotateAnimation.setDuration(100);
						rotateAnimation.setFillAfter(true);
						hourrotateAnimation.setDuration(100);
						hourrotateAnimation.setFillAfter(true);
						secondrotateAnimation.setFillAfter(true);
						min_progress_hand.startAnimation(rotateAnimation);
						min_progress.startAnimation(rotateAnimation);
						second_progress_hand
								.startAnimation(secondrotateAnimation);
						second_progress.startAnimation(secondrotateAnimation);

						hour_progress_hand.startAnimation(hourrotateAnimation);
						hour_progress.startAnimation(hourrotateAnimation);

						tvTime.setText(String.format("%1$02d:%2$02d.%3$d", min,
								sec, yushu));
						predegree = (float) (0.6 * mlCount);
						secondpredegree = (float) (36.0 * mlCount);
						hourpredegree = (float) (mlCount / 100);
					} catch (Exception e) {
						tvTime.setText("" + min + ":" + sec + "." + yushu);
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	private void enddaojishi() {
		if (ring) {
			mediaPlayer.start();
		}

		startbuttondaoji.setVisibility(View.VISIBLE);
		startandcannellin.setVisibility(View.GONE);
		try {
			MainActivityWithViewPager.seekBar.setProgress(2);
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			handler.removeMessages(msg.what);
			new AlertDialog.Builder(CountdownActivity.this)
					.setTitle("提示 ")
					.setMessage("倒计时结束")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									listjishi.setVisibility(View.GONE);
									timepickerlin.setVisibility(View.VISIBLE);
									mlCount = -1;
									btnselecttime.setText("开始");
									SaveRun.setisdaojishi(false);
									mediaPlayer.release();
									mediaPlayer=null;
								}
							}).setCancelable(false).create().show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		daojishijicubutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startbuttondaoji.setVisibility(View.VISIBLE);
				startandcannellin.setVisibility(View.GONE);
				predegree = 0;
				secondpredegree = 0;
				hourpredegree = 0;
				listjishi.setVisibility(View.GONE);
				timepickerlin.setVisibility(View.VISIBLE);
				mlCount = -1;
				btnselecttime.setText("开始");
				SaveRun.setisdaojishi(false);
				try {
					if (task != null) {
						MainActivityWithViewPager.seekBar.setProgress(2);
						task.cancel();
						task = null;
						timer.cancel();
						timer.purge();
						timer = null;
						handler.removeMessages(msg.what);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btnselecttime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (null == timer) {
					if (mlCount == -1 || mlCount == 0) {
						mlCount = wheelMain.getwv_year() * 36000
								+ wheelMain.getwv_month() * 600
								+ wheelMain.getwv_day() * 10;
					}
					if (mlCount > 0) {
						SaveRun.setisdaojishi(true);
						btnselecttime.setText("暂停");
						listjishi.setVisibility(View.VISIBLE);
						timepickerlin.setVisibility(View.GONE);
						if (null == task) {
							task = new TimerTask() {
								@Override
								public void run() {
									if (null == msg) {
										msg = new Message();
									} else {
										msg = Message.obtain();
									}
									msg.what = 1;
									handler.sendMessage(msg);
								}
							};
						}
						timer = new Timer(true);
						timer.schedule(task, 100, 100);
					}
				} else {
					try {
						SaveRun.setisdaojishi(false);
						MainActivityWithViewPager.seekBar.setProgress(2);
						btnselecttime.setText("继续");
						task.cancel();
						task = null;
						timer.cancel();
						timer.purge();
						timer = null;
						handler.removeMessages(msg.what);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		startbuttondaoji.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (null == timer) {
					if (mlCount == -1 || mlCount == 0) {
						mlCount = wheelMain.getwv_year() * 36000
								+ wheelMain.getwv_month() * 600
								+ wheelMain.getwv_day() * 10;
					}
					if (mlCount > 0) {
						mediaPlayer = MediaPlayer.create(CountdownActivity.this, R.raw.fallbackring);
						mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer arg0) {
								mediaPlayer.start();
							}
						});
						startbuttondaoji.setVisibility(View.GONE);
						startandcannellin.setVisibility(View.VISIBLE);
						SaveRun.setisdaojishi(true);
						btnselecttime.setText("暂停");
						listjishi.setVisibility(View.VISIBLE);
						timepickerlin.setVisibility(View.GONE);
						if (null == task) {
							task = new TimerTask() {
								@Override
								public void run() {
									if (null == msg) {
										msg = new Message();
									} else {
										msg = Message.obtain();
									}
									msg.what = 1;
									handler.sendMessage(msg);
								}
							};
						}
						timer = new Timer(true);
						timer.schedule(task, 100, 100);
					}
				}
			}
		});
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.onDestroy();
	}
}