package com.dvsapp;

import com.baidu.mapapi.SDKInitializer;
import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsCfgVer;
import com.dvsapp.crash.EmailIntentSender;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.ui.Main;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.utils.DvsUtils;
import com.treecore.TApplication;
import com.treecore.TBroadcastByInner;
import com.treecore.crash.TCrash;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.network.TNetWorkUtil.netType;
import com.treecore.utils.network.TNetworkStateReceiver;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

public class MyApplication extends TApplication {
	private TTask mCfgVerTask, mAppTask;
	private int mServerCfgver = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		// 打开日志输出
		DvsAPI2.openLog(true);

		// 奔溃处理
		TCrash.getInstance().setReportSender(new EmailIntentSender(this));
		TNetworkStateReceiver.getInstance().initConfig(this);
		TNetworkStateReceiver.getInstance().registerObserver(this);

		SDKInitializer.initialize(this);

		Account.getInstance();
		appTask();
	}

	@Override
	public void appExit(Boolean isBackground) {
		super.appExit(isBackground);

		// if (!isBackground) {
		// Account.getInstance().release();
		// }

		if (mCfgVerTask != null)
			mCfgVerTask.stopTask();
		mCfgVerTask = null;

		if (mAppTask != null)
			mAppTask.stopTask();
		mAppTask = null;
	}

	public static MyApplication getInstance() { // 获取程序实例
		return (MyApplication) mThis;
	}

	@Override
	public void onAppCrash(String crashFile) {
		super.onAppCrash(crashFile);
	}

	@Override
	public void onConnect(netType type) {
		super.onConnect(type);

		getCfgVer();
	}

	@Override
	public void onDisConnect() {
		super.onDisConnect();
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mCfgVerTask != null && mCfgVerTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
			} else if (event == TaskEvent.Cancel) {
			} else if (event == TaskEvent.Work) {
				try {
					// 模拟保存在本地的配置信息版本号
					int local_cfgver = Setting.getCfgVersion();
					DvsCfgVer cfgver = new DvsCfgVer();
					cfgver.setCfgver(1);

					if (cfgver.getCfgver() > local_cfgver) {
						Setting.setCfgVersion(-1);
					}

					if (cfgver != null)
						mServerCfgver = cfgver.getCfgver();
				} catch (Exception e) {
					TToastUtils.makeText("获取服务配置失败！");
				}
			}
		} else if (mAppTask != null && mAppTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
			} else if (event == TaskEvent.Cancel) {
			} else if (event == TaskEvent.Work) {
				while (true) {
					if (task.isCancel())
						return;
					try {
						long result = DvsUtils.getSpinnerTime(Setting
								.getMonitorTimeIndex());

						if (result == 0) {
							Thread.sleep(5 * 1000);
							continue;
						}

						if (!TNetWorkUtil.isNetworkConnected())
							continue;

						long secondA = System.currentTimeMillis()
								- Setting.getInfoRefreshTime();
						long secondB = System.currentTimeMillis()
								- Setting.getMonitorRefreshTime();

						if (secondA >= result * 1000) {
							TBroadcastByInner
									.sentEvent(CoreEventUtils.Event_GetDvsTriggerSum);

						}

						if (secondB >= result * 1000) {
							TBroadcastByInner
									.sentEvent(CoreEventUtils.Event_GetDvsHost_Item_Value);
						}
						Thread.sleep(1 * 1000);
					} catch (Exception e) {

					}
				}
			}
		}
	}

	public boolean isRerequestByCfgVer() {
		if (Setting.getCfgVersion() == -1) {
			return true;
		}

		if (mServerCfgver == -1)
			return false;

		if (Setting.getCfgVersion() < mServerCfgver) {
			return true;
		}

		return false;
	}

	public int getServerCfgVer() {
		return mServerCfgver;
	}

	public void getCfgVer() {
		if (mCfgVerTask == null) {
			mCfgVerTask = new TTask();
		}

		mCfgVerTask.setIXTaskListener(this);
		mCfgVerTask.stopTask();
		mCfgVerTask.startTask("");
	}

	public void appTask() {
		if (mAppTask == null) {
			mAppTask = new TTask();
		}

		mAppTask.setIXTaskListener(this);
		mAppTask.stopTask();
		mAppTask.startTask("");
	}

}