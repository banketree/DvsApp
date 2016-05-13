package com.dvsapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.dvsapp.utils.CoreEventUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.TActivity;
import com.treecore.activity.TActivityManager;
import com.treecore.utils.TActivityUtils;

public class BaseActivity extends TActivity {

	@Override
	public void processEventByInner(Intent intent) {
		super.processEventByInner(intent);

		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0);
		int subEvent = intent.getIntExtra(TBroadcastByInner.EVENT, 0);

		if (mainEvent == CoreEventUtils.Activity_Self_Destory) {
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// if (this instanceof Login || this instanceof Splash) {// 登录或闪屏不处理
		// return;
		// } else {
		// if (Account.getInstance().isDataValid()
		// && Account.getInstance().isAccountValid()) {
		// } else {
		// TActivityUtils.jumpToNewTopActivity(mContext, Login.class);
		// TBroadcastByInner
		// .sentEvent(CoreEventUtils.Activity_Self_Destory);
		// return;
		// }
		// }
		//
		// if (TActivityManager.getInstance().getSizeOfActivityStack() != 1) {
		// return;
		// }
		//
		// if (this instanceof MainPage || this instanceof PlantList) {//
		// 登录或闪屏不处理
		// return;
		// } else {
		// if (UserUtils.isAdmin()) {
		// TActivityUtils.jumpToNewActivity(mContext, CompanyList.class);
		// } else {
		// TActivityUtils.jumpToNewActivity(mContext, MainPage.class,
		// Account.getInstance().getCompany());
		// }
		// }
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}
}