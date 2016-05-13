package com.dvsapp.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsHostGroup;
import com.dvs.appjson.DvsTrigger;
import com.dvs.appjson.DvsTriggerSum;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.DvsTriggerInfo;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.utils.DvsUtils;
import com.dvsapp.view.PinnedHeaderExpandableListView;
import com.dvsapp.view.PinnedHeaderExpandableListView.PinnedHeaderAdapter;
import com.treecore.TApplication;
import com.treecore.TBroadcastByInner;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TTimeUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.log.TLog;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

//信息总览
public class Information extends BaseActivity implements OnClickListener {
	private PinnedHeaderExpandableListView mPinnedHeaderExpandableListView;
	private int expandFlag = -1;// 控制列表的展开
	private PinnedHeaderExpandableAdapter mAdapter;
	private List<DvsTriggerSum> mDvsTriggerSums = new ArrayList();
	private List<DvsTriggerInfo> mDvsTriggerInfos = new ArrayList();

	private TTask mGetDvsTriggerSumTask, mGetDvsTriggerInfoTask;
	private TextView mRefreshTimeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		initView();
		initData();
		TBroadcastByInner.sentPostEvent(CoreEventUtils.Event_GetDvsTriggerSum,
				1);
	}

	@Override
	public void processEventByInner(Intent intent) {
		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0);
		int event = intent.getIntExtra(TBroadcastByInner.EVENT, 0);

		if (mainEvent == CoreEventUtils.Event_GetDvsTriggerSum
				| mainEvent == CoreEventUtils.Event_GetDvsHostGroup_Success) {
			getDvsTriggerSums();
		}
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
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		mPinnedHeaderExpandableListView = (PinnedHeaderExpandableListView) findViewById(R.id.PinnedHeaderExpandableListView_host);
		mPinnedHeaderExpandableListView.setGroupIndicator(null);
		mPinnedHeaderExpandableListView.setDivider(null);
		mPinnedHeaderExpandableListView.setDividerHeight(0);
		mPinnedHeaderExpandableListView.setChildDivider(null);
		mPinnedHeaderExpandableListView.setChildIndicator(null);

		mRefreshTimeTextView = (TextView) findViewById(R.id.TextText_refresh_time);
		findViewById(R.id.ImageView_refresh).setOnClickListener(this);
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mGetDvsTriggerSumTask != null
				&& mGetDvsTriggerSumTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				findViewById(R.id.ProgressBar_loading).setVisibility(
						View.VISIBLE);
			} else if (event == TaskEvent.Cancel) {
				findViewById(R.id.ProgressBar_loading).setVisibility(View.GONE);

				if (task.getResultObject() != null) {
					Setting.setInfoRefreshTime(System.currentTimeMillis());
					mRefreshTimeTextView
							.setText("刷新数据时间："
									+ TTimeUtils.getFullTime(System
											.currentTimeMillis()));

					mDvsTriggerSums.clear();
					DvsTriggerSum[] triggersSumList = (DvsTriggerSum[]) task
							.getResultObject();

					for (int i = 0; i < triggersSumList.length; i++) {
						if (triggersSumList[i].getPriority1() > 0
								|| triggersSumList[i].getPriority2() > 0
								|| triggersSumList[i].getPriority4() > 0)
							mDvsTriggerSums.add(triggersSumList[i]);
					}

					if (mAdapter != null)
						mAdapter.notifyDataSetChanged();

					getDvsTriggerInfoTask();
				}

				if (!TStringUtils.isEmpty(task.getError()))
					makeText(task.getError());
			} else if (event == TaskEvent.Work) {
				try {
					DvsTriggerSum[] triggersSumList = DvsAPI2
							.groupTriggersSumGet(Account.getInstance()
									.getSessionid(), Setting.getDvsCfgVer());

					task.setResultObject(triggersSumList);
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		} else if (mGetDvsTriggerInfoTask != null
				&& mGetDvsTriggerInfoTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				findViewById(R.id.ProgressBar_loading).setVisibility(
						View.VISIBLE);

			} else if (event == TaskEvent.Cancel) {
				findViewById(R.id.ProgressBar_loading).setVisibility(View.GONE);
				mDvsTriggerInfos.clear();

				if (task.getResultObject() != null) {
					List<DvsTriggerInfo> infos = (List<DvsTriggerInfo>) task
							.getResultObject();
					mDvsTriggerInfos.addAll(infos);

					if (mAdapter != null)
						mAdapter.notifyDataSetChanged();
				}

				if (!TStringUtils.isEmpty(task.getError()))
					makeText(task.getError());
			} else if (event == TaskEvent.Work) {
				List<DvsTriggerInfo> infos = new ArrayList<>();
				try {
					// 获取主机组下所有触发器信息：时间区间置null,null
					DvsTrigger[] triggersList = DvsAPI2.groupTriggersGet(
							Account.getInstance().getSessionid(),
							new String[] { task.getParameter().get(0) }, 100,
							Setting.getDvsCfgVer());

					for (int i = 0; triggersList != null
							&& i < triggersList.length; i++) {

						// 获取触发器对应的主机ID
						String[] triggerIDs = new String[] { triggersList[i]
								.getTriggerid() };
						DvsHost[] triggerHostList = DvsAPI2.groupHostsGet(
								Account.getInstance().getSessionid(), null,
								triggerIDs, false, Setting.getDvsCfgVer());

						DvsTriggerInfo dvsTriggerInfo = new DvsTriggerInfo(
								triggersList[i], triggerHostList);
						infos.add(dvsTriggerInfo);
					}

					task.setResultObject(infos);
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}

	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// 设置悬浮头部VIEW
		mAdapter = new PinnedHeaderExpandableAdapter();
		mPinnedHeaderExpandableListView.setAdapter(mAdapter);

		// 设置单个分组展开
		mPinnedHeaderExpandableListView
				.setOnGroupClickListener(new GroupClickListener());
	}

	private void getDvsTriggerSums() {
		if (mGetDvsTriggerSumTask != null && mGetDvsTriggerSumTask.isTasking())
			return;

		if (get_status() != Status.RESUMED)
			return;

		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (mGetDvsTriggerSumTask == null) {
			mGetDvsTriggerSumTask = new TTask();
		}

		mGetDvsTriggerSumTask.setIXTaskListener(this);
		mGetDvsTriggerSumTask.stopTask();
		mGetDvsTriggerSumTask.startTask("");
	}

	private void getDvsTriggerInfoTask() {
		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (expandFlag == -1) {
			return;
		}

		if (mGetDvsTriggerInfoTask == null) {
			mGetDvsTriggerInfoTask = new TTask();
		}
		mGetDvsTriggerInfoTask.setIXTaskListener(this);
		mGetDvsTriggerInfoTask.stopTask();
		mGetDvsTriggerInfoTask.startTask(mDvsTriggerSums.get(expandFlag)
				.getGroupid());
	}

	class GroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

			mDvsTriggerInfos.clear();
			if (expandFlag == -1) {
				// 展开被选的group
				mPinnedHeaderExpandableListView.expandGroup(groupPosition);
				// 设置被选中的group置于顶端
				mPinnedHeaderExpandableListView.setSelectedGroup(groupPosition);
				expandFlag = groupPosition;
			} else if (expandFlag == groupPosition) {
				mPinnedHeaderExpandableListView.collapseGroup(expandFlag);
				expandFlag = -1;
			} else {
				mPinnedHeaderExpandableListView.collapseGroup(expandFlag);
				// 展开被选的group
				mPinnedHeaderExpandableListView.expandGroup(groupPosition);
				// 设置被选中的group置于顶端
				mPinnedHeaderExpandableListView.setSelectedGroup(groupPosition);
				expandFlag = groupPosition;
			}

			getDvsTriggerInfoTask();
			return true;
		}
	}

	static class ChildView {
		LinearLayout mDataLayout;
	}

	static class GroupView {
		ImageView arrowImageView;
		TextView nameTextView;
		TextView infoTextView;
	}

	class PinnedHeaderExpandableAdapter extends BaseExpandableListAdapter
			implements PinnedHeaderAdapter {
		private LayoutInflater inflater;

		public PinnedHeaderExpandableAdapter() {
			inflater = LayoutInflater.from(TApplication.getInstance());
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ChildView childView = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.view_information_item,
						null);
				childView = new ChildView();
				childView.mDataLayout = (LinearLayout) convertView
						.findViewById(R.id.LinearLayout_infomation_data);
				convertView.setTag(childView);
			} else {
				childView = (ChildView) convertView.getTag();
			}

			childView.mDataLayout.removeAllViews();

			try {
				DvsTriggerInfo dvsTriggerInfo = mDvsTriggerInfos
						.get(childPosition);

				TextView textView = new TextView(mContext);
				textView.setText("事  件  名  称:"
						+ dvsTriggerInfo.getDvsTrigger().getDescription());
				textView.setTextColor(R.color.black);
				childView.mDataLayout.addView(textView);

				textView = new TextView(mContext);
				textView.setText("事件附加描述:"
						+ dvsTriggerInfo.getDvsTrigger().getComments());
				textView.setTextColor(R.color.black);
				childView.mDataLayout.addView(textView);

				textView = new TextView(mContext);
				textView.setText("事件简要描述:"
						+ dvsTriggerInfo.getDvsTrigger().getExpression());
				textView.setTextColor(R.color.black);
				childView.mDataLayout.addView(textView);

				textView = new TextView(mContext);
				textView.setText("严  重  级  别:"
						+ DvsUtils.getTriggerString(dvsTriggerInfo
								.getDvsTrigger().getPriority()));
				childView.mDataLayout.addView(textView);
				textView.setTextColor(R.color.black);
				// if (!dvsTriggerInfo.getDvsHosts().isEmpty()) {
				// textView = new TextView(mContext);
				// textView.setText("归  属  主  机:"
				// + dvsTriggerInfo.getDvsHosts().get(0).getName());
				// childView.mDataLayout.addView(textView);
				// textView.setTextColor(R.color.black);
				// }
			} catch (Exception e) {
			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mDvsTriggerInfos.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupPosition;
		}

		@Override
		public int getGroupCount() {
			return mDvsTriggerSums.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			GroupView groupView = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.view_infomation_group,
						null);
				groupView = new GroupView();
				groupView.arrowImageView = (ImageView) convertView
						.findViewById(R.id.ImageView_arrow);
				groupView.nameTextView = (TextView) convertView
						.findViewById(R.id.TextView_name);
				groupView.infoTextView = (TextView) convertView
						.findViewById(R.id.TextView_info);
				convertView.setTag(groupView);
			} else {
				groupView = (GroupView) convertView.getTag();
			}

			DvsTriggerSum dvsTriggerSum = mDvsTriggerSums.get(groupPosition);
			groupView.arrowImageView.setSelected(isExpanded);

			DvsHostGroup group = Main.getHostGroupById(dvsTriggerSum
					.getGroupid());
			if (group != null) {
				groupView.nameTextView.setText(group.getName());
			} else {
				groupView.nameTextView.setText("主机组"
						+ dvsTriggerSum.getGroupid());
			}

			// if (dvsTriggerSum.getPriority4() > 0
			// || dvsTriggerSum.getPriority2() > 0
			// || dvsTriggerSum.getPriority1() > 0) {
			groupView.infoTextView.setText("严重 ("
					+ dvsTriggerSum.getPriority4() + ")  警告 ("
					+ dvsTriggerSum.getPriority2() + ")  其它 ("
					+ dvsTriggerSum.getPriority1() + ")");
			// } else {
			// groupView.infoTextView.setText("");
			// }

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public int getHeaderState(int groupPosition, int childPosition) {
			final int childCount = getChildrenCount(groupPosition);
			if (childPosition == childCount - 1) {
				return PINNED_HEADER_PUSHED_UP;
			} else if (childPosition == -1
					&& !mPinnedHeaderExpandableListView
							.isGroupExpanded(groupPosition)) {
				return PINNED_HEADER_GONE;
			} else {
				return PINNED_HEADER_VISIBLE;
			}
		}

		@Override
		public void configureHeader(View header, int groupPosition,
				int childPosition, int alpha) {
			TLog.i("", "" + groupPosition);
			// String groupData = this.groupData[groupPosition];
			// ((TextView)
			// header.findViewById(R.id.groupto)).setText(groupData);

		}

		private SparseIntArray groupStatusMap = new SparseIntArray();

		@Override
		public void setGroupClickStatus(int groupPosition, int status) {
			groupStatusMap.put(groupPosition, status);
		}

		@Override
		public int getGroupClickStatus(int groupPosition) {
			if (groupStatusMap.keyAt(groupPosition) >= 0) {
				return groupStatusMap.get(groupPosition);
			} else {
				return 0;
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.ImageView_refresh) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_GetDvsTriggerSum);
		}
	}

}