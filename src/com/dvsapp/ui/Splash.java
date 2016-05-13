package com.dvsapp.ui;

import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.treecore.utils.TActivityUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends BaseActivity {
	private static long SPLASH_TIME = 3000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		new Handler().postDelayed(r, SPLASH_TIME);
	}

	@Override
	public void processEventByInner(Intent intent) {

	}

	private Runnable r = new Runnable() {
		public void run() {
			if (Account.getInstance().isValid()) {
				TActivityUtils.jumpToNewTopActivity(mContext, Main.class);
			} else {
				TActivityUtils.jumpToNewTopActivity(mContext, Login.class);
			}

			finish();
		}
	};

}