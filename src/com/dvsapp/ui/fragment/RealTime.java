package com.dvsapp.ui.fragment;

import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsHostGroup;
import com.dvs.appjson.DvsItem;
import com.dvs.appjson.DvsValue;
import com.dvsapp.wisdom.R;
import com.dvsapp.ui.Main;
import com.dvsapp.ui.RealTimeMonitor;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.view.PinnedHeaderExpandableListView;
import com.dvsapp.view.PinnedHeaderExpandableListView.PinnedHeaderAdapter;
import com.treecore.TApplication;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.fragment.TFragment;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TTimeUtils;
import com.treecore.utils.TToastUtils;

//实时数据
public class RealTime extends TFragment implements OnClickListener {
	private final static String Tag = RealTimeChart.class.getSimpleName();
	private View mContentView;
	private PinnedHeaderExpandableListView mPinnedHeaderExpandableListView;
	private RealTimeExpandableAdapter mAdapter;
	private TextView mRefreshTextView;
	public static int expandFlag = -1;// 控制列表的展开
	public static int childExpandFlag = -1;
	public static DvsHost clickDvsHost;
	public static DvsItem clickDvsItem;
	private View mLoadView;
	public static Calendar curCalendar = Calendar.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mThis = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_realtime_monitor, null);
		initView();
		initData();
		return mThis;
	}

	@Override
	protected void handleEvent(int id, String... params) {
		super.handleEvent(id);

		if (id == CoreEventUtils.Event_GetDvsHost_Item_Value_Success) {
			if (mAdapter != null)
				mAdapter.notifyDataSetChanged();

			mRefreshTextView.setText("刷新数据时间："
					+ TTimeUtils.getFullTime(System.currentTimeMillis()));
		} else if (id == CoreEventUtils.Event_GetDvsHost_Item_Value_Loading) {
			if (mLoadView != null)
				mLoadView.setVisibility(View.VISIBLE);
		} else if (id == CoreEventUtils.Event_GetDvsHost_Item_Value_Cancel) {
			if (mLoadView != null)
				mLoadView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mLoadView = null;
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ImageView_refresh) {
			if (childExpandFlag == -1 || expandFlag == -1) {
				TToastUtils.makeText("请选定监听的主机");
				return;
			}

			DvsHost host = null;
			try {
				host = Main.getHosts(
						Main.getHostGroups().get(expandFlag).getGroupid()).get(
						childExpandFlag);
			} catch (Exception e) {
			}

			if (host == null) {
				TToastUtils.makeText("主机数据丢失");
				return;
			}

			TBroadcastByInner
					.sentEvent(CoreEventUtils.Event_GetDvsHost_Item_Value);
		}
	}

	private void initView() {
		mPinnedHeaderExpandableListView = (PinnedHeaderExpandableListView) mThis
				.findViewById(R.id.PinnedHeaderExpandableListView_host);
		mPinnedHeaderExpandableListView.setGroupIndicator(null);
		mPinnedHeaderExpandableListView.setDivider(null);
		mPinnedHeaderExpandableListView.setDividerHeight(0);
		mPinnedHeaderExpandableListView.setChildDivider(null);
		mPinnedHeaderExpandableListView.setChildIndicator(null);

		mRefreshTextView = (TextView) mThis
				.findViewById(R.id.TextText_refresh_time);

		mThis.findViewById(R.id.ImageView_refresh).setOnClickListener(this);

		mLoadView = mThis.findViewById(R.id.ProgressBar_loading);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// 设置悬浮头部VIEW
		// mPinnedHeaderExpandableListView.setHeaderView(getLayoutInflater()
		// .inflate(R.layout.view_information_host_group_view,
		// mPinnedHeaderExpandableListView, false));
		mAdapter = new RealTimeExpandableAdapter();
		mPinnedHeaderExpandableListView.setAdapter(mAdapter);

		// 设置单个分组展开
		mPinnedHeaderExpandableListView
				.setOnGroupClickListener(new GroupClickListener());
	}

	class GroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {

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

			// if (expandFlag != -1) {
			// TBroadcastByInner.sentEvent(
			// CoreEventUtils.Event_GetDvsHost_Item_Value, Main
			// .getHostGroups().get(expandFlag).getGroupid());
			// }
			childExpandFlag = -1;
			clickDvsHost = null;
			return true;
		}
	}

	private View getDataItem(final DvsItem item) {
		View dataView = LayoutInflater.from(TApplication.getInstance())
				.inflate(R.layout.view_dvshost_dataitem, null);

		((TextView) dataView.findViewById(R.id.TextView_type)).setText(item
				.getName() + ":");
		DvsValue value = RealTimeMonitor.getDataValues().get(item.getItemid());

		if (value != null) {
			if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_FLOAT) {
				((TextView) dataView.findViewById(R.id.TextView_value))
						.setText(TStringUtils.toFloat((String) value.getValue())
								+ "");
			} else if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_STRING) {
				((TextView) dataView.findViewById(R.id.TextView_value))
						.setText((String) value.getValue() + item.getUnits());
				dataView.findViewById(R.id.ImageView_chart).setVisibility(
						View.INVISIBLE);
			} else if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_INTEGER) {
				((TextView) dataView.findViewById(R.id.TextView_value))
						.setText(TStringUtils.toInt((String) value.getValue())
								+ item.getUnits());
			}
		}

		final View.OnClickListener onClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clickDvsItem = item;
				if (v.getId() == R.id.ImageView_chart) {
					curCalendar = Calendar.getInstance();
					TBroadcastByInner
							.sentEvent(CoreEventUtils.Event_Monitor_Chart);
				} else if (v.getId() == R.id.ImageView_history_data) {
					curCalendar = Calendar.getInstance();
					TBroadcastByInner
							.sentEvent(CoreEventUtils.Event_Monitor_Data);
				}
			}
		};

		dataView.findViewById(R.id.ImageView_chart).setOnClickListener(
				onClickListener);
		dataView.findViewById(R.id.ImageView_history_data).setOnClickListener(
				onClickListener);
		// dataView.findViewById(R.id.TextView_chart).setTag(item);
		// dataView.findViewById(R.id.TextView_history_data).setTag(item);

		return dataView;
	}

	private View getCtrlItem(DvsItem item) {
		View dataView = LayoutInflater.from(TApplication.getInstance())
				.inflate(R.layout.view_dvshost_ctrlitem, null);

		((TextView) dataView.findViewById(R.id.TextView_type)).setText(item
				.getName() + ":");
		// DvsValue value =
		// RealTimeMonitor.getDataValues().get(item.getItemid());
		//
		// if (value != null) {
		// if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_FLOAT) {
		// ((TextView) dataView.findViewById(R.id.TextView_value))
		// .setText(TStringUtils.toFloat((String) value.getValue())
		// + "");
		// } else if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_STRING) {
		// ((TextView) dataView.findViewById(R.id.TextView_value))
		// .setText((String) value.getValue() + item.getUnits());
		// dataView.findViewById(R.id.TextView_chart).setVisibility(
		// View.GONE);
		// } else if (item.getValue_type() == DvsAPI2.HISTORY_DATATYPE_INTEGER)
		// {
		// ((TextView) dataView.findViewById(R.id.TextView_value))
		// .setText(TStringUtils.toInt((String) value.getValue())
		// + item.getUnits());
		// }
		// }
		return dataView;
	}

	static class GroupHold {
		TextView nameTextView;
		ImageView arrowImageView;
	}

	static class ChildHold {
		View headView;
		TextView hostNameTextView;
		ImageView arrowImageView;
		TextView mapTextView;
		View realtimeDataView;
		LinearLayout dataContanterLayout;
		LinearLayout ctrlContanterLayout;
	}

	class RealTimeExpandableAdapter extends BaseExpandableListAdapter implements
			PinnedHeaderAdapter {
		private LayoutInflater inflater;

		public RealTimeExpandableAdapter() {
			inflater = LayoutInflater.from(TApplication.getInstance());
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			ChildHold childHold = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.view_monitor_host_item,
						null);
				childHold = new ChildHold();
				childHold.headView = convertView
						.findViewById(R.id.RelativeLayout_head);
				childHold.hostNameTextView = (TextView) convertView
						.findViewById(R.id.TextView_host_name);
				childHold.arrowImageView = (ImageView) convertView
						.findViewById(R.id.ImageView_arrow);
				childHold.mapTextView = (TextView) convertView
						.findViewById(R.id.TextView_map);
				childHold.realtimeDataView = convertView
						.findViewById(R.id.LinearLayout_realtime_data);
				childHold.dataContanterLayout = (LinearLayout) convertView
						.findViewById(R.id.LinearLayout_data_item);
				childHold.ctrlContanterLayout = (LinearLayout) convertView
						.findViewById(R.id.LinearLayout_ctrl_item);

				convertView.setTag(childHold);
			} else {
				childHold = (ChildHold) convertView.getTag();
			}

			DvsHost dvsHost = Main.getHosts(
					Main.getHostGroups().get(groupPosition).getGroupid()).get(
					childPosition);

			childHold.hostNameTextView.setText(dvsHost.getName());
			childHold.dataContanterLayout.removeAllViews();
			childHold.ctrlContanterLayout.removeAllViews();
			if (childExpandFlag == childPosition) {
				childHold.arrowImageView.setSelected(true);
				childHold.realtimeDataView.setVisibility(View.VISIBLE);

				List<DvsItem> dataItems = Main.getHostItemDatas(dvsHost
						.getHostid());

				for (int i = 0; i < dataItems.size(); i++) {
					DvsItem item = dataItems.get(i);
					childHold.dataContanterLayout.addView(getDataItem(item));
				}

				List<DvsItem> ctrlItems = Main.getHostItemCtrls(dvsHost
						.getHostid());
				for (int i = 0; i < ctrlItems.size(); i++) {
					DvsItem item = ctrlItems.get(i);
					childHold.ctrlContanterLayout.addView(getCtrlItem(item));
				}
			} else {
				childHold.arrowImageView.setSelected(false);
				childHold.realtimeDataView.setVisibility(View.GONE);
			}

			childHold.headView.setTag(dvsHost);
			childHold.headView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					DvsHost dvsHost = (DvsHost) view.getTag();

					RealTimeMonitor.getDataValues().clear();
					RealTimeMonitor.getCtrlValues().clear();

					if (childExpandFlag == childPosition) {
						childExpandFlag = -1;
						clickDvsHost = null;
					} else {
						childExpandFlag = childPosition;
						clickDvsHost = dvsHost;
					}

					mAdapter.notifyDataSetChanged();

					// 获取数据项+控制项
					if (childExpandFlag != -1) {
						TBroadcastByInner.sentEvent(
								CoreEventUtils.Event_GetDvsHost_Item_Value,
								dvsHost.getHostid());
					}
				}
			});

			childHold.mapTextView.setTag(dvsHost);
			childHold.mapTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					clickDvsHost = (DvsHost) view.getTag();
					TBroadcastByInner
							.sentEvent(CoreEventUtils.Event_DvsHost_Map);
					TBroadcastByInner.sentPostEvent(
							CoreEventUtils.Event_DvsHost_Host_Map, 1);
				}
			});
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int result = 0;
			try {
				result = Main.getHosts(
						Main.getHostGroups().get(groupPosition).getGroupid())
						.size();
			} catch (Exception e) {
			}

			return result;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupPosition;
		}

		@Override
		public int getGroupCount() {
			return Main.getHostGroups().size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			GroupHold groupHold = null;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.view_monitor_host_group, null);
				groupHold = new GroupHold();
				groupHold.nameTextView = (TextView) convertView
						.findViewById(R.id.TextView_name);
				groupHold.arrowImageView = (ImageView) convertView
						.findViewById(R.id.ImageView_arrow);
				convertView.setTag(groupHold);
			} else {
				groupHold = (GroupHold) convertView.getTag();
			}

			groupHold.arrowImageView.setSelected(isExpanded);

			DvsHostGroup group = Main.getHostGroups().get(groupPosition);
			groupHold.nameTextView.setText(group.getName());
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
}