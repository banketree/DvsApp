package com.dvsapp.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsItem;
import com.dvs.appjson.DvsValue;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.ui.fragment.RealTime;
import com.dvsapp.ui.fragment.RealTimeChart;
import com.dvsapp.ui.fragment.RealTimeData;
import com.dvsapp.utils.CoreEventUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.fragment.TFragment;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

//实时监控
public class RealTimeMonitor extends BaseFragmentActivity implements
		OnClickListener {
	private TFragment mMonitorFragment, mChartFragment, mDataFragment;

	private static HashMap<String, DvsValue> mDataValues = new HashMap<>();
	private static HashMap<String, DvsValue> mCtrlValues = new HashMap<>();

	private TTask mGetHostItemsTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		initView();
		goType(0);

		TBroadcastByInner.sentPostEvent(
				CoreEventUtils.Event_GetDvsHost_Item_Value, 1);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void processEventByInner(Intent intent) {
		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0);
		int event = intent.getIntExtra(TBroadcastByInner.EVENT, 0);

		if (mainEvent == CoreEventUtils.Event_Monitor_Back) {
			goType(0);
		} else if (mainEvent == CoreEventUtils.Event_Monitor_Chart) {
			goType(1);
		} else if (mainEvent == CoreEventUtils.Event_Monitor_Data) {
			goType(2);
		} else if (mainEvent == CoreEventUtils.Event_GetDvsHost_Item_Value) {
			onGetHostItemsTask();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);
		if (mGetHostItemsTask != null && mGetHostItemsTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				getDataValues().clear();
				getCtrlValues().clear();

				mMonitorFragment
						.sentEvent(CoreEventUtils.Event_GetDvsHost_Item_Value_Loading);
			} else if (event == TaskEvent.Cancel) {
				mMonitorFragment
						.sentEvent(CoreEventUtils.Event_GetDvsHost_Item_Value_Cancel);

				if (!TStringUtils.isEmpty(task.getError())) {
					makeText(task.getError());
				}

				if (task.getResultObject() != null
						&& (Boolean) task.getResultObject()) {
					Setting.setMonitorRefreshTime(System.currentTimeMillis());
					mMonitorFragment
							.sentEvent(CoreEventUtils.Event_GetDvsHost_Item_Value_Success);
				}
			} else if (event == TaskEvent.Work) {
				try {
					List<DvsItem> dvsItems = Main.getHostItemDatas(task
							.getParameter().get(0));

					// 数据项
					List<String> mStringList = new ArrayList<>();
					List<String> mFloatList = new ArrayList<>();
					List<String> mIntList = new ArrayList<>();

					for (DvsItem dvsItem : dvsItems) {
						if (dvsItem.getValue_type() == DvsAPI2.HISTORY_DATATYPE_STRING) {
							mStringList.add(dvsItem.getItemid());
						} else if (dvsItem.getValue_type() == DvsAPI2.HISTORY_DATATYPE_FLOAT) {
							mFloatList.add(dvsItem.getItemid());
						} else if (dvsItem.getValue_type() == DvsAPI2.HISTORY_DATATYPE_INTEGER) {
							mIntList.add(dvsItem.getItemid());
						}
					}

					Calendar calendar = Calendar.getInstance();
					Date curDate = calendar.getTime();
					calendar.add(Calendar.DATE, -3);
					Date startDate = calendar.getTime();

					if (!mStringList.isEmpty()) {
						DvsValue[] dvsValues = DvsAPI2
								.historyDataGetLast(Account.getInstance()
										.getSessionid(),
										(String[]) mStringList
												.toArray(new String[mStringList
														.size()]),
										DvsAPI2.HISTORY_DATATYPE_STRING,
										startDate, curDate, null, Setting
												.getDvsCfgVer());
						if (dvsValues != null) {
							for (int i = 0; i < dvsValues.length; i++) {
								mDataValues.put(dvsValues[i].getItemid(),
										dvsValues[i]);
							}
						}
					}

					if (!mFloatList.isEmpty()) {
						DvsValue[] dvsValues = DvsAPI2
								.historyDataGetLast(Account.getInstance()
										.getSessionid(),
										(String[]) mFloatList
												.toArray(new String[mFloatList
														.size()]),
										DvsAPI2.HISTORY_DATATYPE_FLOAT,
										startDate, curDate, null, Setting
												.getDvsCfgVer());
						if (dvsValues != null) {
							for (int i = 0; i < dvsValues.length; i++) {
								mDataValues.put(dvsValues[i].getItemid(),
										dvsValues[i]);
							}
						}
					}

					if (!mIntList.isEmpty()) {
						DvsValue[] dvsValues = DvsAPI2.historyDataGetLast(
								Account.getInstance().getSessionid(),
								(String[]) mIntList.toArray(new String[mIntList
										.size()]),
								DvsAPI2.HISTORY_DATATYPE_INTEGER, startDate,
								curDate, null, Setting.getDvsCfgVer());
						if (dvsValues != null) {
							for (int i = 0; i < dvsValues.length; i++) {
								mDataValues.put(dvsValues[i].getItemid(),
										dvsValues[i]);
							}
						}
					}

					// 获取主机的所有Application_CtrlParams数据项
					dvsItems = Main
							.getHostItemCtrls(task.getParameter().get(0));

					for (DvsItem dvsItem : dvsItems) {
						// getCtrlItems().add(dvsItems[vlaueIndex]);
					}
					task.setResultObject(true);
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
	}

	private void initView() {

	}

	private void goType(int type) {
		if (type == 0) {
			if (mMonitorFragment == null) {
				mMonitorFragment = new RealTime();
			}
			switchContent(R.id.main_container, mMonitorFragment);
			mChartFragment = null;
		} else if (type == 1) {
			mChartFragment = new RealTimeChart();
			switchContent(R.id.main_container, mChartFragment);
			mDataFragment = null;
		} else if (type == 2) {
			mDataFragment = new RealTimeData();
			switchContent(R.id.main_container, mDataFragment);
			mChartFragment = null;
		}
	}

	private void onGetHostItemsTask() {
		if (mGetHostItemsTask != null && mGetHostItemsTask.isTasking())
			return;

		if (get_status() != Status.RESUMED)
			return;

		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (RealTime.childExpandFlag == -1 || RealTime.expandFlag == -1) {
			return;
		}

		DvsHost host = null;
		try {
			host = Main.getHosts(
					Main.getHostGroups().get(RealTime.expandFlag).getGroupid())
					.get(RealTime.childExpandFlag);
		} catch (Exception e) {
		}

		if (host == null) {
			// TToastUtils.makeText("主机数据丢失");
			return;
		}

		if (mGetHostItemsTask == null) {
			mGetHostItemsTask = new TTask();
		}
		mGetHostItemsTask.setIXTaskListener(this);
		mGetHostItemsTask.stopTask();
		mGetHostItemsTask.startTask(host.getHostid());
	}

	public synchronized static HashMap<String, DvsValue> getDataValues() {
		return mDataValues;
	}

	public synchronized static HashMap<String, DvsValue> getCtrlValues() {
		return mCtrlValues;
	}

}