package cn.just.alarmclock.broadcast;


import cn.just.alarmclock.R;
import cn.just.alarmclock.activity.AlarmAlert;
import cn.just.alarmclock.activity.AlarmAlertFullScreen;
import cn.just.alarmclock.controller.AlarmAlertWakeLock;
import cn.just.alarmclock.controller.Alarms;
import cn.just.alarmclock.model.AlarmClock;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

public class PositionAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmClock alarm = null;
		final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
		if (data != null) {
			Parcel in = Parcel.obtain();
			in.unmarshall(data, 0, data.length);
			in.setDataPosition(0);
			alarm = AlarmClock.CREATOR.createFromParcel(in);
		}
		Alarms.disableSnoozeAlert(context, alarm.id);
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		// 监听Home健 ，关闭所有的dialog
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.sendBroadcast(closeDialogs);
		Class c = AlarmAlert.class;
		// 对锁屏进行管理
		KeyguardManager km = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		// 判断当前是否属于锁屏状态
		if (km.inKeyguardRestrictedInputMode()) {
			c = AlarmAlertFullScreen.class;
		}
		// 启动service
		Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
		playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		context.startService(playAlarm);
		
		Intent notify = new Intent(context, AlarmAlert.class);
		notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);

		PendingIntent pendingNotify = PendingIntent.getActivity(context,
				alarm.id, notify, 0);
		String label = alarm.getLabelOrDefault1(context);
		Notification n = new Notification.Builder(context)
				.setContentTitle(label)
				.setSmallIcon(R.mipmap.stat_notify_alarm)
				.setContentInfo(context.getString(R.string.alarm_notify_text))
				.build();

//		Notification n = new Notification(R.mipmap.stat_notify_alarm, label,
//				alarm.time);
//		n.setLatestEventInfo(context, label,
//				context.getString(R.string.alarm_notify_text), pendingNotify);
		n.flags |= Notification.FLAG_SHOW_LIGHTS
				| Notification.FLAG_ONGOING_EVENT;
		n.defaults |= Notification.DEFAULT_LIGHTS;
		
		// 定义要操作的intent
		Intent alarmAlert = new Intent(context, c);
		alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		// 传递一个新的任务标记
		alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		
		n.fullScreenIntent = PendingIntent.getActivity(context, alarm.id,
				alarmAlert, 0);
		NotificationManager nm = getNotificationManager(context);
		nm.notify(alarm.id, n);
	}
	
	private NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

}
