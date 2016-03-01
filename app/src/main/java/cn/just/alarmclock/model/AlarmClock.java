package cn.just.alarmclock.model;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import cn.just.alarmclock.R;
import cn.just.alarmclock.controller.Alarms;


import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

public final class AlarmClock implements Parcelable {
	public int id;// 主键
	public boolean enabled; // 是否启动闹钟（1或0）
	public int hour;// 小时
	public int minutes;// 分钟
	public DaysOfWeek daysOfWeek;// 星期几
	public long time;// 与目前时间相隔多少毫秒
	public double length;//目的地和现在位置之间的距离
	public boolean vibrate;// 是否振动（1或0）
	public String label;// 标签说明
	public Uri alert;// 铃声
	public boolean silent;// 是否静音（1或0）
	public String position;// 位置描述
	public double longitude;// 经度
	public double latitude;// 纬度
	public int distance;// 距离
	public int type;// 类型 1-普通闹钟 2-位置闹钟 3-智能闹钟
	public static final Creator<AlarmClock> CREATOR = new Creator<AlarmClock>() {
		public AlarmClock createFromParcel(Parcel p) {
			return new AlarmClock(p);
		}

		public AlarmClock[] newArray(int size) {
			return new AlarmClock[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
		p.writeDouble(length);
		p.writeInt(vibrate ? 1 : 0);
		p.writeString(label);
		p.writeParcelable(alert, flags);
		p.writeInt(silent ? 1 : 0);
		p.writeString(position);
		p.writeDouble(longitude);
		p.writeDouble(latitude);
		p.writeInt(distance);
		p.writeInt(type);
	}

	public AlarmClock(Cursor c) {
		id = c.getInt(Columns.ALARM_ID_INDEX);
		enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
		hour = c.getInt(Columns.ALARM_HOUR_INDEX);
		minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
		daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
		time = c.getLong(Columns.ALARM_TIME_INDEX);
		length=c.getDouble(Columns.ALARM_LENGTH_INDEX);
		vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
		label = c.getString(Columns.ALARM_MESSAGE_INDEX);
		position = c.getString(Columns.ALARM_POSITION_INDEX);
		longitude = c.getDouble(Columns.ALARM_LONGITUDE_INDEX);
		latitude = c.getDouble(Columns.ALARM_LATITUDE_INDEX);
		distance = c.getInt(Columns.ALARM_DISTANCE_INDEX);
		type = c.getInt(Columns.ALARM_TYPE_INDEX);
		String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
		if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
			if (true) {
				Log.v("wangxianming", "Alarm is marked as silent");
			}
			silent = true;
		} else {
			if (alertString != null && alertString.length() != 0) {
				alert = Uri.parse(alertString);
			}
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
	}

	public AlarmClock(Parcel p) {
		id = p.readInt();
		enabled = p.readInt() == 1;
		hour = p.readInt();
		minutes = p.readInt();
		daysOfWeek = new DaysOfWeek(p.readInt());
		time = p.readLong();
		length=p.readDouble();
		vibrate = p.readInt() == 1;
		label = p.readString();
		alert = (Uri) p.readParcelable(null);
		silent = p.readInt() == 1;
		position = p.readString();
		longitude = p.readDouble();
		latitude = p.readDouble();
		distance = p.readInt();
		type = p.readInt();
	}

	public AlarmClock(int type) {
		id = -1;
		if (type == 1 || type == 3) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			hour = c.get(Calendar.HOUR_OF_DAY);
			minutes = c.get(Calendar.MINUTE);

		}
		position = "默认";
		vibrate = true;
		daysOfWeek = new DaysOfWeek(0);
		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

	}

	//type=1的时候获取label
	public String getLabelOrDefault1(Context context) {
		if (label == null || label.length() == 0) {
			return context.getString(R.string.default_label);//普通闹钟
		}
		return label;
	}
	//type=1的时候获取label
		public String getLabelOrDefault2(Context context) {
			if (label == null || label.length() == 0) {
				return context.getString(R.string.default_position);//位置闹钟
			}
			return label;
		}
	//type=3的时候获取position的描述
	public String getLabelOrDefault3(Context context){
		if(label==null||label.length()==0){
			return context.getString(R.string.default_intelligence);//智能闹钟
		}
		return label;
	}
	
	

	public static class Columns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.cn.daming.deskclock/alarm");
		public static final String HOUR = "hour";
		public static final String MINUTES = "minutes";
		public static final String DAYS_OF_WEEK = "daysofweek";
		public static final String ALARM_TIME = "alarmtime";
		public static final String ALARM_LENGTH="length";
		public static final String ENABLED = "enabled";
		public static final String VIBRATE = "vibrate";
		public static final String MESSAGE = "message";
		public static final String ALERT = "alert";
		public static final String POSITION = "position";
		public static final String LONITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String DISTANCE = "distance";
		public static final String TYPE = "type";
		public static final String DEFAULT_SORT_ORDER = HOUR + ", " + MINUTES
				+ " ASC";
		public static final String WHERE_ENABLED = ENABLED + "=1";
		public static final String[] ALARM_QUERY_COLUMNS = { _ID, HOUR,
				MINUTES, DAYS_OF_WEEK, ALARM_TIME,ALARM_LENGTH, ENABLED, VIBRATE, MESSAGE,
				ALERT, POSITION, LONITUDE, LATITUDE, DISTANCE, TYPE };
		public static final int ALARM_ID_INDEX = 0;
		public static final int ALARM_HOUR_INDEX = 1;
		public static final int ALARM_MINUTES_INDEX = 2;
		public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
		public static final int ALARM_TIME_INDEX = 4;
		public static final int ALARM_LENGTH_INDEX=5;
		public static final int ALARM_ENABLED_INDEX = 6;
		public static final int ALARM_VIBRATE_INDEX = 7;
		public static final int ALARM_MESSAGE_INDEX = 8;
		public static final int ALARM_ALERT_INDEX = 9;
		public static final int ALARM_POSITION_INDEX = 10;
		public static final int ALARM_LONGITUDE_INDEX = 11;
		public static final int ALARM_LATITUDE_INDEX = 12;
		public static final int ALARM_DISTANCE_INDEX = 13;
		public static final int ALARM_TYPE_INDEX = 14;
	}

	public static final class DaysOfWeek {
		/**
		 * 星期日为一周的第一天 SUN MON TUE WED THU FRI SAT DAY_OF_WEEK返回值 1 2 3 4 5 6 7
		 * 星期一为一周的第一天 MON TUE WED THU FRI SAT SUN DAY_OF_WEEK返回值 1 2 3 4 5 6 7
		 **/
		// 2,3,4,5,6,7,1
		private static int[] DAY_MAP = new int[] { Calendar.MONDAY,
				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };

		// Bitmask of all repeating days
		private int mDays;

		public DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ? context.getText(R.string.never).toString()
						: "";
			}

			// every day 127
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1)
					dayCount++;
				days >>= 1;// 右移一位
			}

			// short or long form?
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs
					.getWeekdays();

			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					ret.append(dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(context.getText(R.string.day_concat));
				}
			}
			return ret.toString();
		}

		private boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

		public void set(DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public int getCoded() {
			return mDays;
		}

		public boolean[] getBooleanArray() {
			boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

		public int getNextAlarm(Calendar c) {
			if (mDays == 0) {
				return -1;
			}
			/**
			 * 周一 周二 周三 周四 周五 周六 周日
			 *  0    1   2   3   4    5   6
			 */
			//如果今天是周五（today=4），选择的是周六（day=5）,那么相距（dayCount=1）天
			int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            //选择的是周几对应的数字
			int day = 0;
			//今天距离选择的还有几天
			int dayCount = 0;
			for (; dayCount < 7; dayCount++) {
				day = (today + dayCount) % 7;
				if (isSet(day)) {
					break;
				}
			}
			Log.v("tag", "c.get(Calendar.DAY_OF_WEEK)" +c.get(Calendar.DAY_OF_WEEK));
			Log.v("tag", "day=" + day);
			Log.v("tag", "today=" + today);
			Log.v("tag", "dayCount=" + dayCount);
			return dayCount;
		}
	}
}
