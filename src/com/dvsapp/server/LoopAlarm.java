package com.dvsapp.server;

import com.treecore.utils.log.TLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class LoopAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, PhoneShowService.class));
	}

	public static void alarm(Context context, int renew_time) {
		TLog.i("OneShotAlarm2", "alarm " + renew_time);
		Intent intent = new Intent(context, LoopAlarm.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
		if (renew_time > 0)
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + renew_time * 1000, sender);
	}
}
