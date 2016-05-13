package com.dvsapp.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PhoneShowService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("PhoneShowService: onBind");
		return null;
	}

	@Override
	public void onCreate() {
		System.out.println("PhoneShowService: onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// return super.onStartCommand(intent, flags, startId);
		System.out.println("PhoneShowService: onStartCommand");
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		System.out.println("PhoneShowService: onDestroy");
		super.onDestroy();
		LoopAlarm.alarm(this, 0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		System.out.println("PhoneShowService: onStart");
		LoopAlarm.alarm(this, 5 * 2 * 60);
		super.onStart(intent, startId);
	}
}
