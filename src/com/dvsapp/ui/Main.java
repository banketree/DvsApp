package com.dvsapp.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsHostUtils;
import com.dvs.appjson.DvsHostGroup;
import com.dvs.appjson.DvsHostGroupUtils;
import com.dvs.appjson.DvsItem;
import com.dvs.appjson.DvsItemUtils;
import com.dvsapp.MyApplication;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.utils.DvsUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.config.TPreferenceConfig;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

//主界面
public class Main extends BaseTabActivity implements OnClickListener {
	private static final String Field_Host_Group = Account.getInstance()
			.getName() + "DvsHostGroup";
	private static final String Field_Host = Account.getInstance().getName()
			+ "DvsHost";
	private static final String Field_Host_Item_Ctrl = Account.getInstance()
			.getName() + "DvsItem_ctrl";
	private static final String Field_Host_Item_Data = Account.getInstance()
			.getName() + "DvsItem_data";

	private TabHost mTabHost;

	private static List<DvsHostGroup> mDvsHostGroups = new ArrayList();
	private static HashMap<String, List<DvsHost>> mDvsHosts = new HashMap<>();// group->
	private static HashMap<String, List<DvsItem>> mCtrlItems = new HashMap<>();// host->
	private static HashMap<String, List<DvsItem>> mDataItems = new HashMap<>();// host->

	private TTask mGetDvsHostGroupTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();

		TBroadcastByInner.sentPostEvent(CoreEventUtils.Event_GetDvsTriggerSum,
				1);
		TBroadcastByInner
				.sentPostEvent(CoreEventUtils.Event_GetDvsHostGroup, 1);
	}

	@Override
	public void processEventByInner(Intent intent) {
		super.processEventByInner(intent);
		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0);
		int event = intent.getIntExtra(TBroadcastByInner.EVENT, 0);

		if (mainEvent == CoreEventUtils.Event_DvsHost_Map) {
			mTabHost.setCurrentTab(2);
		} else if (mainEvent == CoreEventUtils.Event_GetDvsHostGroup) {
			onGetDvsHostGroupTask();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			DvsUtils.showSystemExitBySecond(mContext);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View view) {
		TBroadcastByInner
				.sentPostEvent(CoreEventUtils.Event_GetDvsHostGroup, 1);

		if (view.getId() == R.id.Button_information) {
			mTabHost.setCurrentTab(0);
			goType(0);
		} else if (view.getId() == R.id.Button_realtimemonitor) {
			mTabHost.setCurrentTab(1);
			goType(1);
		} else if (view.getId() == R.id.Button_maploc) {
			mTabHost.setCurrentTab(2);
			goType(2);
		} else if (view.getId() == R.id.Button_usercenter) {
			mTabHost.setCurrentTab(3);
			goType(3);
		} else if (view.getId() == R.id.Button_help) {
			mTabHost.setCurrentTab(4);
			goType(4);
		}
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mGetDvsHostGroupTask != null
				&& mGetDvsHostGroupTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(mContext, "主机组", "获取主机组信息中…",
						false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(mContext);

				if (!TStringUtils.isEmpty(task.getError())) {
					makeText(task.getError());
				}
			} else if (event == TaskEvent.Work) {
				try {
					// 获取该用户名下的所有主机组信息
					DvsHostGroup[] groupList = DvsAPI2.hostGroupsGet(Account
							.getInstance().getSessionid(), Setting
							.getDvsCfgVer());

					setHostGroup(groupList);

					if (groupList != null && groupList.length != 0) {
						for (int i = 0; i < groupList.length; i++) {
							// 获取某个主机组下的所有主机信息
							DvsHost[] hostList = DvsAPI2.groupHostsGet(Account
									.getInstance().getSessionid(),
									new String[] { groupList[i].getGroupid() },
									null, false, Setting.getDvsCfgVer());
							setHost(groupList[i].getGroupid(), hostList);

							if (hostList != null && hostList.length != 0) {
								for (int j = 0; j < hostList.length; j++) {
									DvsItem[] dvsItems = DvsAPI2.hostItemsGet(
											Account.getInstance()
													.getSessionid(),
											new String[] { hostList[j]
													.getHostid() },
											"Application_RealtimeData", null,
											Setting.getDvsCfgVer());

									setHostItemData(hostList[j].getHostid(),
											dvsItems);

									// 获取主机的所有Application_CtrlParams数据项
									dvsItems = DvsAPI2.hostItemsGet(Account
											.getInstance().getSessionid(),
											new String[] { hostList[j]
													.getHostid() },
											"Application_CtrlParams", null,
											Setting.getDvsCfgVer());
									setHostItemCtrl(hostList[j].getHostid(),
											dvsItems);
								}
							}
						}

						Setting.setCfgVersion(MyApplication.getInstance()
								.getServerCfgVer());
						TBroadcastByInner
								.sentEvent(CoreEventUtils.Event_GetDvsHostGroup_Success);
					} else {

					}
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	private void initView() {
		mTabHost = getTabHost();
		mTabHost.setup();

		TabSpec tabSpec = mTabHost.newTabSpec("tab1");
		Intent intent = new Intent(mContext, Information.class);
		// intent.putExtra("table", AllChart.Table_Capacity);
		// intent.putExtra("company", mCompanyId);
		tabSpec.setContent(intent);
		tabSpec.setIndicator("tab1");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("tab2");
		intent = new Intent(mContext, RealTimeMonitor.class);
		tabSpec.setContent(intent);
		tabSpec.setIndicator("tab2");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("tab3");
		intent = new Intent(mContext, MapLoc.class);
		tabSpec.setContent(intent);
		tabSpec.setIndicator("tab3");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("tab4");
		intent = new Intent(mContext, UserCenter.class);
		tabSpec.setContent(intent);
		tabSpec.setIndicator("tab4");
		mTabHost.addTab(tabSpec);

		tabSpec = mTabHost.newTabSpec("tab5");
		intent = new Intent(mContext, Help.class);
		tabSpec.setContent(intent);
		tabSpec.setIndicator("tab5");
		mTabHost.addTab(tabSpec);

		findViewById(R.id.Button_information).setOnClickListener(this);
		findViewById(R.id.Button_realtimemonitor).setOnClickListener(this);
		findViewById(R.id.Button_maploc).setOnClickListener(this);
		findViewById(R.id.Button_usercenter).setOnClickListener(this);
		findViewById(R.id.Button_help).setOnClickListener(this);

		goType(0);
	}

	private void goType(int type) {
		if (type == 0) {
			findViewById(R.id.Button_information).setSelected(true);
			findViewById(R.id.Button_realtimemonitor).setSelected(false);
			findViewById(R.id.Button_maploc).setSelected(false);
			findViewById(R.id.Button_usercenter).setSelected(false);
			findViewById(R.id.Button_help).setSelected(false);
		} else if (type == 1) {
			findViewById(R.id.Button_information).setSelected(false);
			findViewById(R.id.Button_realtimemonitor).setSelected(true);
			findViewById(R.id.Button_maploc).setSelected(false);
			findViewById(R.id.Button_usercenter).setSelected(false);
			findViewById(R.id.Button_help).setSelected(false);
		} else if (type == 2) {
			findViewById(R.id.Button_information).setSelected(false);
			findViewById(R.id.Button_realtimemonitor).setSelected(false);
			findViewById(R.id.Button_maploc).setSelected(true);
			findViewById(R.id.Button_usercenter).setSelected(false);
			findViewById(R.id.Button_help).setSelected(false);
		} else if (type == 3) {
			findViewById(R.id.Button_information).setSelected(false);
			findViewById(R.id.Button_realtimemonitor).setSelected(false);
			findViewById(R.id.Button_maploc).setSelected(false);
			findViewById(R.id.Button_usercenter).setSelected(true);
			findViewById(R.id.Button_help).setSelected(false);
		} else if (type == 4) {
			findViewById(R.id.Button_information).setSelected(false);
			findViewById(R.id.Button_realtimemonitor).setSelected(false);
			findViewById(R.id.Button_maploc).setSelected(false);
			findViewById(R.id.Button_usercenter).setSelected(false);
			findViewById(R.id.Button_help).setSelected(true);
		}
	}

	private void onGetDvsHostGroupTask() {
		if (!MyApplication.getInstance().isRerequestByCfgVer()
				&& !Main.getHostGroups().isEmpty()) {
			return;
		}

		if (mGetDvsHostGroupTask != null && mGetDvsHostGroupTask.isTasking())
			return;

		if (get_status() != Status.RESUMED)
			return;

		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (mGetDvsHostGroupTask == null) {
			mGetDvsHostGroupTask = new TTask();
		}
		mGetDvsHostGroupTask.setIXTaskListener(this);
		mGetDvsHostGroupTask.stopTask();
		mGetDvsHostGroupTask.startTask("");
	}

	// ////////////////////////////////////////////////////////////////////////////////////

	// //host group
	public static void setHostGroup(DvsHostGroup[] groups) {
		JSONArray array = new JSONArray();

		if (groups != null && groups.length != 0) {
			for (int i = 0; i < groups.length; i++) {
				array.put(DvsHostGroupUtils.getJson(groups[i]));
			}
		}

		setHostGroupString(array.toString());
	}

	public synchronized static List<DvsHostGroup> getHostGroups() {
		if (!mDvsHostGroups.isEmpty())
			return mDvsHostGroups;

		mDvsHostGroups.clear();
		JSONArray array = null;
		try {
			array = new JSONArray(getHostGroupString());
			if (array != null && array.length() != 0) {
				for (int i = 0; i < array.length(); i++) {
					DvsHostGroup group = DvsHostGroupUtils.setJson(array
							.getJSONObject(i));
					mDvsHostGroups.add(group);
				}
			}
		} catch (Exception e) {
		}

		array = null;
		return mDvsHostGroups;
	}

	public static DvsHostGroup getHostGroupById(String groupId) {
		DvsHostGroup result = null;
		if (getHostGroups().isEmpty() || TStringUtils.isEmpty(groupId))
			return result;

		for (DvsHostGroup group2 : getHostGroups()) {
			if (groupId.equalsIgnoreCase(group2.getGroupid())) {
				result = group2;
				break;
			}
		}

		return result;
	}

	private static void setHostGroupString(String group) {
		TPreferenceConfig.getInstance().setString(Field_Host_Group, group);
	}

	private static String getHostGroupString() {
		return TPreferenceConfig.getInstance().getString(Field_Host_Group, "");
	}

	// /////host
	public static void setHost(String group, DvsHost[] hosts) {
		JSONArray array = new JSONArray();

		if (hosts != null && hosts.length != 0) {
			for (int i = 0; i < hosts.length; i++) {
				array.put(DvsHostUtils.getJson(hosts[i]));
			}
		}

		setHostString(group, array.toString());
	}

	public synchronized static List<DvsHost> getHosts(String group) {
		if (!mDvsHosts.containsKey(group)) {
			List<DvsHost> dvsHosts = new ArrayList<>();
			JSONArray array = null;
			try {
				array = new JSONArray(getHostString(group));
				if (array != null && array.length() != 0) {
					for (int i = 0; i < array.length(); i++) {
						DvsHost dvsHost = DvsHostUtils.setJson(array
								.getJSONObject(i));
						dvsHosts.add(dvsHost);
					}
				}
			} catch (Exception e) {
			}

			mDvsHosts.put(group, dvsHosts);
			array = null;
		}

		return mDvsHosts.get(group);
	}

	private static void setHostString(String groupId, String host) {
		TPreferenceConfig.getInstance().setString(Field_Host + "_" + groupId,
				host);
	}

	private static String getHostString(String groupId) {
		return TPreferenceConfig.getInstance().getString(
				Field_Host + "_" + groupId, "");
	}

	// // host item ctrl

	public static void setHostItemCtrl(String hostId, DvsItem[] items) {
		JSONArray array = new JSONArray();

		if (items != null && items.length != 0) {
			for (int i = 0; i < items.length; i++) {
				array.put(DvsItemUtils.getJson(items[i]));
			}
		}

		setHostItemCtrlString(hostId, array.toString());
	}

	public synchronized static List<DvsItem> getHostItemCtrls(String hostId) {
		if (!mCtrlItems.containsKey(hostId)) {
			List<DvsItem> items = new ArrayList<>();
			JSONArray array = null;
			try {
				array = new JSONArray(getHostItemCtrlString(hostId));
				if (array != null && array.length() != 0) {
					for (int i = 0; i < array.length(); i++) {
						DvsItem item = DvsItemUtils.setJson(array
								.getJSONObject(i));
						items.add(item);
					}
				}
			} catch (Exception e) {
			}

			mCtrlItems.put(hostId, items);
			array = null;
		}

		return mCtrlItems.get(hostId);
	}

	private static void setHostItemCtrlString(String hostId, String item) {
		TPreferenceConfig.getInstance().setString(
				Field_Host_Item_Ctrl + "_" + hostId, item);
	}

	private static String getHostItemCtrlString(String hostId) {
		return TPreferenceConfig.getInstance().getString(
				Field_Host_Item_Ctrl + "_" + hostId, "");
	}

	// // host item data

	public static void setHostItemData(String hostId, DvsItem[] items) {
		JSONArray array = new JSONArray();

		if (items != null && items.length != 0) {
			for (int i = 0; i < items.length; i++) {
				array.put(DvsItemUtils.getJson(items[i]));
			}
		}

		setHostItemDataString(hostId, array.toString());
	}

	public synchronized static List<DvsItem> getHostItemDatas(String hostId) {
		if (!mDataItems.containsKey(hostId)) {
			List<DvsItem> items = new ArrayList<>();
			JSONArray array = null;
			try {
				array = new JSONArray(getHostItemDataString(hostId));
				if (array != null && array.length() != 0) {
					for (int i = 0; i < array.length(); i++) {
						DvsItem item = DvsItemUtils.setJson(array
								.getJSONObject(i));
						items.add(item);
					}
				}
			} catch (Exception e) {
			}

			mDataItems.put(hostId, items);
			array = null;
		}

		return mDataItems.get(hostId);
	}

	private static void setHostItemDataString(String hostId, String item) {
		TPreferenceConfig.getInstance().setString(
				Field_Host_Item_Data + "_" + hostId, item);
	}

	private static String getHostItemDataString(String hostId) {
		return TPreferenceConfig.getInstance().getString(
				Field_Host_Item_Data + "_" + hostId, "");
	}

	public static void clearData() {
		mDvsHostGroups.clear();
		mDvsHosts.clear();
		mCtrlItems.clear();
		mDataItems.clear();
	}
}
