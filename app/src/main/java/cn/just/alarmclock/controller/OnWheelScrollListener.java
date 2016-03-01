package cn.just.alarmclock.controller;

import cn.just.alarmclock.util.WheelView;

public interface OnWheelScrollListener {

	void onScrollingStarted(WheelView wheel);

	void onScrollingFinished(WheelView wheel);
}
