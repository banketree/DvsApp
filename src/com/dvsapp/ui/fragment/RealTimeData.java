package com.dvsapp.ui.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsValue;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.view.xlist.XListView;
import com.treecore.TApplication;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.fragment.TFragment;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TTimeUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TITaskListener;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

//实时数据
public class RealTimeData extends TFragment implements OnClickListener,
		XListView.IXListViewListener, TITaskListener {
	private final static String Tag = RealTimeChart.class.getSimpleName();
	private View mContentView;
	private XListView mDataListView;
	private MyAdapter mMyAdapter;
	private TextView mDateTextView, mTypeTextView, mHostNameTextView;;
	private List<DvsValue> mDvsValues = new ArrayList<>();
	private TTask mGetDvsValueTask;
	private int mPageindex = 0, mPageCount = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mThis = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_realtime_monitor_data, null);
		mDataListView = (XListView) mThis.findViewById(R.id.XListView_data);
		mDataListView.setPullRefreshEnable(false);
		mDataListView.setPullLoadEnable(false);
		mDataListView.setAutoLoadEnable(true);
		mDataListView.setXListViewListener(this);
		mDataListView.getHeaderView().getTimeView().setVisibility(View.GONE);

		mMyAdapter = new MyAdapter();
		mDataListView.setAdapter(mMyAdapter);

		mThis.findViewById(R.id.Button_data).setOnClickListener(this);
		mThis.findViewById(R.id.Button_chart).setOnClickListener(this);
		mThis.findViewById(R.id.ImageView_back).setOnClickListener(this);
		mThis.findViewById(R.id.Button_data).setSelected(true);

		mDateTextView = (TextView) mThis.findViewById(R.id.TextView_date);
		mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
				.getTimeInMillis()));

		mThis.findViewById(R.id.ImageView_pre).setOnClickListener(this);
		mThis.findViewById(R.id.ImageView_next).setOnClickListener(this);

		mTypeTextView = (TextView) mThis.findViewById(R.id.TextView_data_name);
		mTypeTextView.setText(RealTime.clickDvsItem.getName());

		mHostNameTextView = (TextView) mThis
				.findViewById(R.id.TextView_host_name);
		mHostNameTextView.setText(RealTime.clickDvsHost.getName());

		onGetDvsValueTask();
		return mThis;
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		onGetDvsValueTask();
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		if (mGetDvsValueTask != null && mGetDvsValueTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(getActivity(), "数据",
						"获取数据中…", false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(getActivity());
				//
				if (task.getResultObject() != null) {
					DvsValue[] dvsValues = (DvsValue[]) task.getResultObject();

					if (dvsValues != null) {
						for (int i = 0; i < dvsValues.length; i++) {
							mDvsValues.add(dvsValues[i]);
						}
					}
				}

				mMyAdapter.notifyDataSetChanged();
				if ((mPageindex + 1) >= mPageCount) {
					mDataListView.setPullLoadEnable(false);
				} else {
					mDataListView.setPullLoadEnable(true);
				}
			} else if (event == TaskEvent.Work) {
				try {
					long[] minMax = TTimeUtils
							.getMinMaxByDay(RealTime.curCalendar
									.getTimeInMillis());
					mPageindex = mPageindex + 1;

					DvsValue[] zv = DvsAPI2.historyDataGetPage(Account
							.getInstance().getSessionid(),
							new String[] { RealTime.clickDvsItem.getItemid() },
							mPageindex, 100, DvsAPI2.HISTORY_DATATYPE_FLOAT,
							new Date(minMax[0]), new Date(minMax[1]), false,
							null, Setting.getDvsCfgVer());

					if (zv != null && zv.length != 0) {
						mPageCount = (int) zv[0].getPagecount();
					} else {
						mPageCount = 0;
					}
					task.setResultObject(zv);
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	@Override
	protected void handleEvent(int id, String... params) {
		super.handleEvent(id);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.Button_chart) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Chart);
		} else if (v.getId() == R.id.Button_data) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Data);
		} else if (v.getId() == R.id.ImageView_back) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Back);
		} else if (v.getId() == R.id.ImageView_pre) {
			RealTime.curCalendar.add(Calendar.DATE, -1);
			mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
					.getTimeInMillis()));

			resetData();
			onGetDvsValueTask();
		} else if (v.getId() == R.id.ImageView_next) {
			RealTime.curCalendar.add(Calendar.DATE, 1);
			mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
					.getTimeInMillis()));
			resetData();
			onGetDvsValueTask();
		}
	}

	private void resetData() {
		mDvsValues.clear();
		mPageindex = 0;
		mPageCount = 1;
	}

	private void onGetDvsValueTask() {
		if (mGetDvsValueTask != null && mGetDvsValueTask.isTasking())
			return;
		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (mGetDvsValueTask == null)
			mGetDvsValueTask = new TTask();
		mGetDvsValueTask.setIXTaskListener(this);
		mGetDvsValueTask.stopTask();
		mGetDvsValueTask.startTask("");
	}

	public final class ViewHolder {
		public View headView;
		public TextView dateTextView;
		public TextView dataTextView;
	}

	public class MyAdapter extends BaseAdapter {

		public MyAdapter() {
		}

		@Override
		public int getCount() {
			return mDvsValues.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(TApplication.getInstance())
						.inflate(R.layout.view_realtime_monitor_data, null);
				viewHolder.headView = convertView.findViewById(R.id.View_head);
				viewHolder.dateTextView = (TextView) convertView
						.findViewById(R.id.TextView_date);
				viewHolder.dataTextView = (TextView) convertView
						.findViewById(R.id.TextView_data);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.headView.setVisibility(position == 0 ? View.VISIBLE
					: View.INVISIBLE);
			DvsValue dvsValue = mDvsValues.get(position);

			viewHolder.dateTextView.setText(TTimeUtils
					.gethourTimeString(dvsValue.getClock() * 1000));

			try {
				Float value = Float.parseFloat(String.valueOf(dvsValue
						.getValue()));
				DecimalFormat decimalFormat = new DecimalFormat("##0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
				viewHolder.dataTextView.setText(decimalFormat.format(value));
			} catch (Exception e) {
				viewHolder.dataTextView.setText(String.valueOf(dvsValue
						.getValue()));
			}

			return convertView;
		}
	}

}