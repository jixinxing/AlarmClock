package cn.just.alarmclock.controller;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;

import cn.just.alarmclock.activity.AlarmClockActivity;
import cn.just.alarmclock.broadcast.AlarmReceiver;
import cn.just.alarmclock.broadcast.PositionAlarmReceiver;
import cn.just.alarmclock.model.AlarmClock;

public class Alarms {
	public static final String ALARM_ALERT_ACTION = "com.cn.daming.deskclock.ALARM_ALERT";
	public static final String ALARM_DONE_ACTION = "com.cn.daming.deskclock.ALARM_DONE";
	public static final String ALARM_SNOOZE_ACTION = "com.cn.daming.deskclock.ALARM_SNOOZE";
	public static final String ALARM_DISMISS_ACTION = "com.cn.daming.deskclock.ALARM_DISMISS";
	public static final String ALARM_KILLED = "alarm_killed";
	public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";
	public static final String ALARM_ALERT_SILENT = "silent";
	public static final String CANCEL_SNOOZE = "cancel_snooze";
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";
	public static final String ALARM_ID = "alarm_id";

	final static String PREF_SNOOZE_ID = "snooze_id";
	final static String PREF_SNOOZE_TIME = "snooze_time";

	private final static String DM12 = "E h:mm aa";
	private final static String DM24 = "E k:mm";

	private final static String M12 = "h:mm aa";
	public final static String M24 = "kk:mm";

	private static LocationClient locationClient;

	// 增加数据
	public static long addAlarm(Context context, AlarmClock alarm) {
		ContentValues values = createContentValues(alarm);
		// 执行增加操作
		Uri uri = context.getContentResolver().insert(
				AlarmClock.Columns.CONTENT_URI, values);
		// 解析id
		alarm.id = (int) ContentUris.parseId(uri);
		long timeInMillis = 0;
		// 计算设定的时间的毫秒数
		timeInMillis = calculateAlarm(alarm);
		Log.v("tag", "timeInMillis=  " + timeInMillis);
		// 如果启动了闹钟
		if (alarm.enabled) {
			clearSnoozeIfNeeded(context, timeInMillis);
		}
		
		if(alarm.type==1||alarm.type==3){
			setNextAlert(context);
		}
		
		if(alarm.type==2&&alarm.enabled==true ){
			getLocationInfo(context,alarm);
			//enablePositionAlert(context, alarm);
		}
		return timeInMillis;
	}

	// 更新
	public static long setAlarm(Context context, AlarmClock alarm) {
		ContentValues values = createContentValues(alarm);
		ContentResolver resolver = context.getContentResolver();
		resolver.update(ContentUris.withAppendedId(
				AlarmClock.Columns.CONTENT_URI, alarm.id), values, null, null);
		long timeInMillis = 0;
		timeInMillis = calculateAlarm(alarm);
		if (alarm.enabled) {
			disableSnoozeAlert(context, alarm.id);
			clearSnoozeIfNeeded(context, timeInMillis);
		}
		if(alarm.type==1||alarm.type==3){
			setNextAlert(context);
		}
		
		if(alarm.type==2&&alarm.enabled==true){
			getLocationInfo(context,alarm);
		}
		return timeInMillis;
	}

	// 删除数据
	public static void deleteAlarm(Context context, int alarmId) {
		if (alarmId == -1)
			return;

		ContentResolver contentResolver = context.getContentResolver();
		disableSnoozeAlert(context, alarmId);

		Uri uri = ContentUris.withAppendedId(AlarmClock.Columns.CONTENT_URI,
				alarmId);
		contentResolver.delete(uri, "", null);

		setNextAlert(context);
	}

	// 查询 根据时间升序排列
	public static Cursor getAlarmsCursor(ContentResolver contentResolver,
			int type) {
		return contentResolver.query(AlarmClock.Columns.CONTENT_URI,
				AlarmClock.Columns.ALARM_QUERY_COLUMNS, AlarmClock.Columns.TYPE
						+ "=" + type, null,
				AlarmClock.Columns.DEFAULT_SORT_ORDER);
	}

	// 查询 启动闹钟 enabled=1
	private static Cursor getFilteredAlarmsCursor(
			ContentResolver contentResolver) {
		return contentResolver.query(AlarmClock.Columns.CONTENT_URI,
				AlarmClock.Columns.ALARM_QUERY_COLUMNS,
				AlarmClock.Columns.WHERE_ENABLED, null, null);
	}

	// AlarmClock类转换成ContentValues类
	private static ContentValues createContentValues(AlarmClock alarm) {
		ContentValues values = new ContentValues(8);
		if (!alarm.daysOfWeek.isRepeatSet()) {
		}

		values.put(AlarmClock.Columns.ENABLED, alarm.enabled ? 1 : 0);
		values.put(AlarmClock.Columns.HOUR, alarm.hour);
		values.put(AlarmClock.Columns.MINUTES, alarm.minutes);
		values.put(AlarmClock.Columns.ALARM_TIME, alarm.time);
		values.put(AlarmClock.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
		values.put(AlarmClock.Columns.VIBRATE, alarm.vibrate);
		values.put(AlarmClock.Columns.MESSAGE, alarm.label);
		values.put(AlarmClock.Columns.TYPE, alarm.type);
		values.put(
				AlarmClock.Columns.ALERT,
				alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert
						.toString());
		values.put(AlarmClock.Columns.POSITION, alarm.position);
		values.put(AlarmClock.Columns.LONITUDE, alarm.longitude);
		values.put(AlarmClock.Columns.LATITUDE, alarm.latitude);
		values.put(AlarmClock.Columns.DISTANCE, alarm.distance);
		return values;
	}

	private static void clearSnoozeIfNeeded(Context context, long alarmTime) {
		SharedPreferences prefs = context.getSharedPreferences(
				AlarmClockActivity.PREFERENCES, 0);
		long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
		Log.v("tag", "snoozeTime=" + snoozeTime + "  ,alarmTime=" + alarmTime);
		if (alarmTime < snoozeTime) {
			clearSnoozePreference(context, prefs);
		}
	}

	// 根据AlarmClock 的ID查询
	public static AlarmClock getAlarm(ContentResolver contentResolver,
			int alarmId) {
		Cursor cursor = contentResolver.query(ContentUris.withAppendedId(
				AlarmClock.Columns.CONTENT_URI, alarmId),
				AlarmClock.Columns.ALARM_QUERY_COLUMNS, null, null, null);
		AlarmClock alarm = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				alarm = new AlarmClock(cursor);
			}
			cursor.close();
		}
		return alarm;
	}

	// 修改闹钟的状态
	public static void enableAlarm(final Context context, final int id,
			boolean enabled,AlarmClock alarm) {
		enableAlarmInternal(context, id, enabled);
		if(alarm.type==1||alarm.type==3){
			setNextAlert(context);
		}
		if(alarm.type==2||alarm.enabled==true){
			Log.v("aa", "选中");
			getLocationInfo(context, alarm);
		}
	}

	private static void enableAlarmInternal(final Context context,
			final int id, boolean enabled) {
		enableAlarmInternal(context,
				getAlarm(context.getContentResolver(), id), enabled);
	}

	// 设定的时间已经过了 该做什么
	private static void enableAlarmInternal(final Context context,
			final AlarmClock alarm, boolean enabled) {
		if (alarm == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();

		ContentValues values = new ContentValues(2);
		values.put(AlarmClock.Columns.ENABLED, enabled ? 1 : 0);
		if (enabled) {
			long time = 0;
			// 没有勾选星期几
			if (!alarm.daysOfWeek.isRepeatSet()) {
				time = calculateAlarm(alarm);
			}
			values.put(AlarmClock.Columns.ALARM_TIME, time);
		} else {
			disableSnoozeAlert(context, alarm.id);
		}

		resolver.update(ContentUris.withAppendedId(
				AlarmClock.Columns.CONTENT_URI, alarm.id), values, null, null);
	}

	// 计算毫秒数 比较 得到离当前时间最短的设定的时间
	public static AlarmClock calculateNextAlert(final Context context) {
		AlarmClock alarm = null;
		long minTime = Long.MAX_VALUE;// MAX_VALUE=9223372036854776000
		long now = System.currentTimeMillis();
		Log.v("tag", "now  = " + now);
		// 查询 启动了闹钟的 数据 enabled=1
		Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					AlarmClock a = new AlarmClock(cursor);
					if (a.type == 1 || a.type == 3) {
						if (a.time == 0) {
							// 计算毫秒数
							a.time = calculateAlarm(a);
							Log.v("tag", "a.time =" + a.time);
						} else if (a.time < now) {// 毫秒数比当前时间小
							enableAlarmInternal(context, a, false);
							continue;// 结束本次循环
						}
						if (a.time < minTime) {
							minTime = a.time;
							alarm = a;
						}
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		// Log.v("tag", "最接近的闹钟：alarm.hour=" + alarm.hour + "  ,alarm.minutes="
		// + alarm.minutes);
		return alarm;
	}

	public static AlarmClock calculateNextPositionAlert(final Context context) {
		AlarmClock alarm = null;
		double minLength = Double.MAX_VALUE;// 1.79769313486231570e+308
		// 查询 启动了闹钟的 数据 enabled=1
		Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					AlarmClock a = new AlarmClock(cursor);
					if (a.type == 2) {
						if (a.length == 0) {
							// 目的地距离现在位置的距离
							//a.length = getLocationInfo(context, a);
							Log.v("bb", "a.lentgth = " + a.length);

							if (a.length <= minLength) {
								minLength = a.length;
								alarm = a;
							}
						}
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		// Log.v("tag", "最接近的闹钟：alarm.hour=" + alarm.hour + "  ,alarm.minutes="
		// + alarm.minutes);
		return alarm;
	}

	public static void disableExpiredAlarms(final Context context) {
		// 查询enabled=1的闹钟
		Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
		long now = System.currentTimeMillis();
		if (cur.moveToFirst()) {
			do {
				AlarmClock alarm = new AlarmClock(cur);
				if (alarm.time != 0 && alarm.time < now) {// 时间过了
					enableAlarmInternal(context, alarm, false);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	
	public static void setNextAlert(final Context context) {
		Log.v("tag", "enableSnoozeAlert(context)=  "
				+ enableSnoozeAlert(context));
		if (!enableSnoozeAlert(context)) {// !false=true
			AlarmClock alarm0 = calculateNextAlert(context);
			if (alarm0 != null) {
				if (alarm0.type == 3) {
					 getLocationInfo(context, alarm0);
				}
				if (alarm0.type == 1) {
					enableAlert(context, alarm0, alarm0.time);
				}
			} else {
				disableAlert(context);
			}
		}
	}

	// 时间匹配 相应闹钟
	private static void enableAlert(Context context, final AlarmClock alarm,
			final long atTimeInMillis) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		if (true) {
			Log.v("tag", "setAlert id " + alarm.id + " atTime "
					+ atTimeInMillis);
		}

		// 启动广播 AlarmReceiver
		Intent intent = new Intent(ALARM_ALERT_ACTION);
		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(ALARM_RAW_DATA, out.marshall());

		// 发送广播
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (alarm.type == 1 || alarm.type == 3) {

			// AlarmManager.RTC_WAKEUP：表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟使用绝对时间，状态值为0；
			am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
		}
		if (alarm.type == 2) {
			Log.v("aa", "位置alarm");
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000, sender);
		}

		setStatusBarIcon(context, true);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(atTimeInMillis);
		String timeString = formatDayAndTime(context, c);
		Log.v("tag", "timeString= " + timeString + "  ,atTimeInMillis  ="
				+ atTimeInMillis);
		saveNextAlarm(context, timeString);
	}

	static void disableAlert(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		// 启动广播
		PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				new Intent(ALARM_ALERT_ACTION),
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		setStatusBarIcon(context, false);
		saveNextAlarm(context, "");
	}

	@SuppressLint("NewApi")
	public static void saveSnoozeAlert(final Context context, final int id,
			final long time) {
		SharedPreferences prefs = context.getSharedPreferences(
				AlarmClockActivity.PREFERENCES, 0);
		Log.v("tag", "saveSnoozeAlert:   " + id);
		if (id == -1) {
			clearSnoozePreference(context, prefs);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			ed.putInt(PREF_SNOOZE_ID, id);
			ed.putLong(PREF_SNOOZE_TIME, time);
			ed.apply();
		}
		setNextAlert(context);
	}

	public static void disableSnoozeAlert(final Context context, final int id) {
		SharedPreferences prefs = context.getSharedPreferences(
				AlarmClockActivity.PREFERENCES, 0);
		int snoozeId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (snoozeId == -1) {
			return;
		} else if (snoozeId == id) {
			clearSnoozePreference(context, prefs);
		}
	}

	// 清空SharedPreferences
	@SuppressLint("NewApi")
	private static void clearSnoozePreference(final Context context,
			final SharedPreferences prefs) {
		final int alarmId = prefs.getInt(PREF_SNOOZE_ID, -1);
		Log.v("tag", "clearSnoozePreference:  alarmId=" + alarmId);
		if (alarmId != -1) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(alarmId);// 删除之前的设置
		}

		final SharedPreferences.Editor ed = prefs.edit();
		ed.remove(PREF_SNOOZE_ID);
		ed.remove(PREF_SNOOZE_TIME);
		ed.apply();
	};

	private static boolean enableSnoozeAlert(final Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				AlarmClockActivity.PREFERENCES, 0);

		int id = prefs.getInt(PREF_SNOOZE_ID, -1);

		if (id == -1) {
			return false;
		}
		long time = prefs.getLong(PREF_SNOOZE_TIME, -1);
		Log.v("tag", "enableSnoozeAlert:id= " + id + "  ,time=" + time);
		final AlarmClock alarm = getAlarm(context.getContentResolver(), id);
		if (alarm == null) {
			return false;
		}
		alarm.time = time;
		enableAlert(context, alarm, time);
		return true;
	}

	private static void setStatusBarIcon(Context context, boolean enabled) {
		Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
		alarmChanged.putExtra("alarmSet", enabled);
		context.sendBroadcast(alarmChanged);
	}

	// 获取设定的时间距离1970年1月1号0时0分0秒的毫秒数
	private static long calculateAlarm(AlarmClock alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek)
				.getTimeInMillis();
	}

	// 将设定的时间存入Calendar
	public static Calendar calculateAlarm(int hour, int minute,
			AlarmClock.DaysOfWeek daysOfWeek) {

		Calendar c = Calendar.getInstance();
		// 当前的时间到1970年1月1号0时0分0秒的毫秒数
		c.setTimeInMillis(System.currentTimeMillis());

		// 当前的小时（24小时制）和分钟
		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);

		Log.v("jixinxing", c.getTimeInMillis() + "   毫秒");
		Log.v("jixinxing", nowHour + "  时");
		Log.v("jixinxing", nowMinute + "  分钟");
		Log.v("jixinxing", Calendar.DAY_OF_YEAR + "  天");

		// 如果设定的时间比当前时间小 ，天数加1
		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0)
			c.add(Calendar.DAY_OF_WEEK, addDays);
		return c;
	}

	// 设置时间的子标题 如：1：00上午 这种格式
	public static String formatTime(final Context context, int hour,
			int minute, AlarmClock.DaysOfWeek daysOfWeek) {
		Calendar c = calculateAlarm(hour, minute, daysOfWeek);
		return formatTime(context, c);
	}

	public static String formatTime(final Context context, Calendar c) {
		String format = get24HourMode(context) ? M24 : M12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	private static String formatDayAndTime(final Context context, Calendar c) {
		String format = get24HourMode(context) ? DM24 : DM12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	static void saveNextAlarm(final Context context, String timeString) {
		boolean flag = Settings.System.putString(context.getContentResolver(),
				Settings.System.NEXT_ALARM_FORMATTED, timeString);
		Log.v("tag", "saveNextAlarm:  flag=" + flag);// true
	}

	public static boolean get24HourMode(final Context context) {
		return DateFormat.is24HourFormat(context);
	}

	// 计算两点之间距离
	public static double getDistance(double startLon, double startLat,
			double endLon, double endLat) {
		double lat1 = (Math.PI / 180) * startLat;// 纬度
		double lat2 = (Math.PI / 180) * endLat;

		double lon1 = (Math.PI / 180) * startLon;// 经度
		double lon2 = (Math.PI / 180) * endLon;

		// 地球半径km
		double R = 6371;
		// 两点间距离 km，如果想要米的话，结果*1000就可以了
		double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.cos(lon2 - lon1))
				* R;
		return d * 1000;// 米
	}

	// 定位 不断获得当前位置的经纬度
	@SuppressWarnings("unused")
	private static void getLocationInfo(final Context context,
			final AlarmClock alarm) {

		locationClient = new LocationClient(context);

		// 设置定位条件
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 是否打开gps
		option.setCoorType("bd0911");// 设置返回值的坐标类型
		// option.setPriority(LocationClientOption.GpsFirst);// 设置定位优先级
		option.setScanSpan(2000);// 设置定时定位的时间间隔 单位毫秒
		locationClient.setLocOption(option);
		locationClient.start();

		// 注册位置监听器
		locationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceivePoi(BDLocation arg0) {

			}

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null) {
					return;
				}
				Log.v("aa", "纬度：" + location.getLatitude() + "  ,经度："
						+ location.getLongitude());
				LatLng latLng = new LatLng(location.getLatitude(), location
						.getLongitude());
				double startLat = latLng.latitude;
				double startlon = latLng.longitude;
				double endLat = alarm.latitude;
				double endlon = alarm.longitude;
				double distiance = getDistance(startlon, startLat, endlon, endLat);
				Log.v("aa", "distiance =" + distiance
						+ " ,alarm.distance= " + alarm.distance);
				if (distiance - alarm.distance <= 0) {
					Log.v("aa", "true");
					if(alarm.type==3){
						enableAlert(context, alarm, alarm.time);
					}
					if(alarm.type==2){
						enablePositionAlert(context,alarm);
					}
				}
			}
		});
	}
	
	public static void enablePositionAlert(final Context context,AlarmClock alarm){
		Intent intent=new Intent(context,PositionAlarmReceiver.class);
		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(ALARM_RAW_DATA, out.marshall());
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
		Log.v("aa", "设置位置闹钟成功！");
	}

}
