package com.dvsapp.ui;

import com.dvsapp.utils.CoreEventUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.fragment.TFragment;
import com.treecore.activity.fragment.TFragmentActivity;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

public class BaseFragmentActivity extends TFragmentActivity {
	public static String TAG = BaseFragmentActivity.class.getCanonicalName();
	private TFragment mCurrentFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void processEventByInner(Intent intent) {
		super.processEventByInner(intent);

		if (intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0) == CoreEventUtils.Activity_Self_Destory) {
			if (mContext != null) {
				finish();
			}
			return;
		}
	}

	@Override
	public void processEventByProcess(Intent intent) {
		super.processEventByInner(intent);
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		if (mActivityTask != null && mActivityTask.equalTask(task)) {
			if (task.getTaskId() == CoreEventUtils.Activity_Self_Destory) { //
				if (event == TaskEvent.Work) {
					try {
						int second = Integer
								.valueOf(task.getParameter().get(0));
						Thread.sleep(second * 1000);
					} catch (Exception e) {
					}
				} else if (event == TaskEvent.Cancel) {
					if (mContext != null)
						finish();
				}
			}
		}
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

	protected void exitActivity(int second) {
		startTask(CoreEventUtils.Activity_Self_Destory, "" + second);
	}

	protected void switchContent(int layId, TFragment fragment) { // 替换Fragment，实现切�?
		if (fragment == null)
			return;

		if (mCurrentFragment == null) {
			getSupportFragmentManager().beginTransaction()
			// .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out)
					.replace(layId, fragment).commitAllowingStateLoss();
			mCurrentFragment = fragment;
		} else {
			switchContent(layId, mCurrentFragment, fragment);
		}
	}

	protected void switchContent(int layId, TFragment from, TFragment to) {
		if (from == null || to == null || from == to)
			return;

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		// if (!to.isAdded()) { // 先判断是否被add�?
		// transaction.setCustomAnimations(R.anim.push_left_in,
		// R.anim.push_left_out);
		transaction.hide(from).add(layId, to);
		transaction.remove(from).commitAllowingStateLoss();
		// } else {
		// // transaction.setCustomAnimations(R.anim.push_right_in,
		// // R.anim.push_right_out);
		// transaction.hide(from).show(to).commitAllowingStateLoss();
		// }
		mCurrentFragment = to;
	}

	public TFragment getCurFragment() {
		return mCurrentFragment;
	}
}
