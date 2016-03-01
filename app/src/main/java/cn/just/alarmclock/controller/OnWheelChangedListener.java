package cn.just.alarmclock.controller;

import cn.just.alarmclock.util.WheelView;

public interface OnWheelChangedListener {

	void onChanged(WheelView wheel, int oldValue, int newValue);
}
