/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.just.alarmclock.util;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Displays the time
 */
public class DigitalClock extends LinearLayout {

    private final static String M12 = "h:mm";

    private Calendar mCalendar;
    private String mFormat;
    private TextView mTimeDisplay;
    private AmPm mAmPm;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mLive && intent.getAction().equals(
                            Intent.ACTION_TIMEZONE_CHANGED)) {
                    mCalendar = Calendar.getInstance();
                }
                // Post a runnable to avoid blocking the broadcast.
                mHandler.post(new Runnable() {
                        public void run() {
                            updateTime();
                        }
                });
            }
        };

    private static Typeface sTypeface;

    static class AmPm {
        private TextView mAmPm;
        private String mAmString, mPmString;

        AmPm(View parent) {
            mAmPm = (TextView) parent.findViewById(R.id.am_pm);

            String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mAmString = ampm[0];
            mPmString = ampm[1];
        }

        void setShowAmPm(boolean show) {
            mAmPm.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void setIsMorning(boolean isMorning) {
            mAmPm.setText(isMorning ? mAmString : mPmString);
        }
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            setDateFormat();
            updateTime();
        }
    }

    public DigitalClock(Context context) {
        this(context, null);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        if (sTypeface == null) {
//            sTypeface = Typeface.createFromAsset(getContext().getAssets(),
//                "fonts/Clockopia.ttf");
//        }
        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
//        mTimeDisplay.setTypeface(sTypeface);
        mAmPm = new AmPm(this);
        mCalendar = Calendar.getInstance();

        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (true) Log.v("tag", "onAttachedToWindow " + this);

        if (mAttached) return;
        mAttached = true;

        if (mLive) {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
        }

        /* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        if (mLive) {
            getContext().unregisterReceiver(mIntentReceiver);
        }
        getContext().getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
    }


    public void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    public void updateTime() {
        if (mLive) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
        }

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        Log.v("tag", "DigitalClock  :"+newTime.toString());
        mTimeDisplay.setText(newTime);
        
        mAmPm.setIsMorning(mCalendar.get(Calendar.AM_PM) == 0);
    }

    private void setDateFormat() {
        mFormat = Alarms.get24HourMode(getContext()) ? Alarms.M24 : M12;
        mAmPm.setShowAmPm(mFormat == M12);
    }

    public void setLive(boolean live) {
        mLive = live;
    }

    public void setTypeface(Typeface tf) {
        mTimeDisplay.setTypeface(tf);
    }
}