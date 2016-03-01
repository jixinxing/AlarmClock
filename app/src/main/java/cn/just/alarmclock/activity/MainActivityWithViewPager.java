package cn.just.alarmclock.activity;

import java.util.ArrayList;

import cn.just.alarmclock.R;
import cn.just.alarmclock.model.SaveRun;
import cn.just.alarmclock.model.ScreenInfo;


import android.annotation.SuppressLint;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("deprecation")
public class MainActivityWithViewPager extends ActivityGroup {
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	Button naozhong,positionAlarmBTN,IntelligenceAlarmBTN, shijian, miaobiao, daoshi;
	ImageView youbiao;
	//Animation am;
	static SeekBar seekBar = null;
	int w;
	//float prearg1 = 0;
	float predree = 0.0f;
	int current = 0;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		viewPager = (ViewPager) findViewById(R.id.pager);
		youbiao = (ImageView) findViewById(R.id.youbiao);

		naozhong = (Button) findViewById(R.id.naozhong);
		positionAlarmBTN=(Button) findViewById(R.id.position_naozhong);
		IntelligenceAlarmBTN=(Button) findViewById(R.id.intelligence_naozhong);
		shijian = (Button) findViewById(R.id.shijian);
		miaobiao = (Button) findViewById(R.id.miaobiao);
		daoshi = (Button) findViewById(R.id.daoshi);

		Button btns[] = { naozhong, positionAlarmBTN,IntelligenceAlarmBTN,shijian, miaobiao, daoshi, };
		seekBar = new SeekBar(this);
		seekBar.setMax(5);
		InItView();
		viewPager.setAdapter(new myPagerView());

		ScreenInfo s = new ScreenInfo(MainActivityWithViewPager.this);
		w = s.getWidth() / 6;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) youbiao
				.getLayoutParams();
		lp.width = w;
		youbiao.setLayoutParams(lp);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				selsct(arg0);

			
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (arg1 != 0) {
					TranslateAnimation animation = new TranslateAnimation(
							predree * w + current, arg1 * w + arg0 * w, 0, 0);
					animation.setDuration(200);
					animation.setFillAfter(true);
					youbiao.startAnimation(animation);
					predree = arg1;
					current = arg0 * w;
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		for (int i = 0; i < 6; i++) {
			final int a = i;
			btns[a].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					viewPager.setCurrentItem(a);
					selsct(a);
				}
			});
		}
		selsct(0);
	}

	private void selsct(int i) {
		Button btns[] = { naozhong, positionAlarmBTN,IntelligenceAlarmBTN,shijian, miaobiao, daoshi, };
		for (int n = 0; n < 6; n++) {
			final int a = n;
			if (i == a) {
				btns[a].setTextColor(Color.parseColor("#ffffff"));
			} else {
				btns[a].setTextColor(Color.parseColor("#d6d6d6"));
			}
		}

	}

	@Override
	protected void onResume() {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress == 1) {
					getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				if (progress == 2) {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
		});
		super.onResume();
	}

	void InItView() {
		pageViews = new ArrayList<View>();
		View view00 = getLocalActivityManager().startActivity(
				"activity00",
				new Intent(this,
						AlarmClockActivity.class))
				.getDecorView();
		View view01 = getLocalActivityManager().startActivity(
				"activity01",
				new Intent(this,
						cn.just.alarmclock.activity.PositionAlarmClockActivity.class))
				.getDecorView();
		View view02 = getLocalActivityManager().startActivity(
				"activity02",
				new Intent(this,
						cn.just.alarmclock.activity.IntelligenceAlarmClockActivity.class))
				.getDecorView();

		View view03 = getLocalActivityManager().startActivity("activity03",
				new Intent(this, TimeActivity.class))
				.getDecorView();
		View view04 = getLocalActivityManager().startActivity("activity04",
				new Intent(this, cn.just.alarmclock.activity.StopwatchActivity.class))
				.getDecorView();
		View view05 = getLocalActivityManager().startActivity("activity05",
				new Intent(this, cn.just.alarmclock.activity.CountdownActivity.class))
				.getDecorView();
		pageViews.add(view00);
		pageViews.add(view01);
		pageViews.add(view02);
		pageViews.add(view03);
		pageViews.add(view04);
		pageViews.add(view05);
	}

	class myPagerView extends PagerAdapter {
		// 显示数目
		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (SaveRun.getisdaojishi() || SaveRun.getisjishi()) {
				new AlertDialog.Builder(this)
						.setTitle("提示")
						.setMessage("正在计时中，确定要退出吗？")
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								})
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										finish();
									}
								})
						.setNeutralButton("后台",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										Intent i = new Intent(
												Intent.ACTION_MAIN);
										i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										i.addCategory(Intent.CATEGORY_HOME);
										startActivity(i);
									}
								}).create().show();
			} else {
				finish();
			}
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
