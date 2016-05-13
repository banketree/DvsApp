package com.dvsapp.ui.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsValue;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.treecore.TApplication;
import com.treecore.TBroadcastByInner;
import com.treecore.activity.fragment.TFragment;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TTimeUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.task.TITaskListener;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

//实时电子表
public class RealTimeChart extends TFragment implements OnClickListener,
		OnChartGestureListener, OnChartValueSelectedListener, TITaskListener {
	private final static String Tag = RealTimeChart.class.getSimpleName();
	private View mContentView;
	private LineChart mLineChart;
	private FrameLayout mContanerFrameLayout;
	private TextView mDateTextView, mTypeTextView, mHostNameTextView;
	private TTask mGetHistoryDataTask;
	private List<DvsValue> mDvsValues = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mThis = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_realtime_monitor_chart, null);
		initView();
		onGetHistoryDataTask();
		return mThis;
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
		mContanerFrameLayout.removeView(mLineChart);
		mContanerFrameLayout.removeAllViews();

		if (mGetHistoryDataTask != null)
			mGetHistoryDataTask.stopTask();
		mGetHistoryDataTask = null;

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.Button_data) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Data);
		} else if (v.getId() == R.id.Button_chart) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Chart);
		} else if (v.getId() == R.id.ImageView_back) {
			TBroadcastByInner.sentEvent(CoreEventUtils.Event_Monitor_Back);
		} else if (v.getId() == R.id.ImageView_pre) {
			RealTime.curCalendar.add(Calendar.DATE, -1);
			mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
					.getTimeInMillis()));

			onGetHistoryDataTask();
		} else if (v.getId() == R.id.ImageView_next) {
			RealTime.curCalendar.add(Calendar.DATE, 1);
			mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
					.getTimeInMillis()));

			onGetHistoryDataTask();
		}
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
		Log.i("LongPress", "Chart longpressed.");
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		Log.i("DoubleTap", "Chart double-tapped.");
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		Log.i("SingleTap", "Chart single-tapped.");
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: "
				+ velocityY);
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
		Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		Log.i("Entry selected", e.toString());
		Log.i("", "low: " + mLineChart.getLowestVisibleXIndex() + ", high: "
				+ mLineChart.getHighestVisibleXIndex());
	}

	@Override
	public void onNothingSelected() {
		Log.i("Nothing selected", "Nothing selected.");
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		if (mGetHistoryDataTask != null && mGetHistoryDataTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(getActivity(), "数据",
						"获取数据中…", false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(getActivity());

				mDvsValues.clear();

				if (task.getResultObject() != null) {
					DvsValue[] dvsValues = (DvsValue[]) task.getResultObject();

					if (dvsValues != null) {
						for (int i = dvsValues.length - 1; i >= 0; i--) {
							mDvsValues.add(dvsValues[i]);
						}
					}
				}

				initData();
			} else if (event == TaskEvent.Work) {
				try {
					long[] minMax = TTimeUtils
							.getMinMaxByDay(RealTime.curCalendar
									.getTimeInMillis());

					DvsValue[] zv = DvsAPI2.historyDataGet(Account
							.getInstance().getSessionid(),
							new String[] { RealTime.clickDvsItem.getItemid() },
							RealTime.clickDvsItem.getValue_type(), new Date(
									minMax[0]), new Date(minMax[1]), 500,
							Setting.getDvsCfgVer());
					task.setResultObject(zv);
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	private void initView() {
		mThis.findViewById(R.id.Button_data).setOnClickListener(this);
		mThis.findViewById(R.id.Button_chart).setOnClickListener(this);
		mThis.findViewById(R.id.Button_chart).setSelected(true);
		mThis.findViewById(R.id.ImageView_back).setOnClickListener(this);

		mDateTextView = (TextView) mThis.findViewById(R.id.TextView_date);
		mDateTextView.setText(TTimeUtils.getYearMonDay(RealTime.curCalendar
				.getTimeInMillis()));

		mTypeTextView = (TextView) mThis.findViewById(R.id.TextView_data_name);
		mTypeTextView.setText(RealTime.clickDvsItem.getName());

		mHostNameTextView = (TextView) mThis
				.findViewById(R.id.TextView_host_name);
		mHostNameTextView.setText(RealTime.clickDvsHost.getName());

		mThis.findViewById(R.id.ImageView_pre).setOnClickListener(this);
		mThis.findViewById(R.id.ImageView_next).setOnClickListener(this);

		mContanerFrameLayout = (FrameLayout) mThis
				.findViewById(R.id.FrameLayout_container);

		if (mLineChart == null) {
			mLineChart = new LineChart(TApplication.getInstance());
			// mLineChart = (LineChart) mThis.findViewById(R.id.LineChart);
			mLineChart.setOnChartGestureListener(this);
			mLineChart.setOnChartValueSelectedListener(this);
			mLineChart.setDrawGridBackground(false);

			// no description text
			mLineChart.setDescription("");
			mLineChart
					.setNoDataTextDescription("You need to provide data for the chart.");

			// enable value highlighting
			mLineChart.setHighlightEnabled(true);

			// enable touch gestures
			mLineChart.setTouchEnabled(true);

			// enable scaling and dragging
			mLineChart.setDragEnabled(true);
			mLineChart.setScaleEnabled(true);
			// mChart.setScaleXEnabled(true);
			// mChart.setScaleYEnabled(true);

			// if disabled, scaling can be done on x- and y-axis separately
			mLineChart.setPinchZoom(true);

			// set an alternative background color
			// mChart.setBackgroundColor(Color.GRAY);

			// create a custom MarkerView (extend MarkerView) and specify the
			// layout
			// to use for it
			// MyMarkerView mv = new MyMarkerView(this,
			// R.layout.custom_marker_view);

			// set the marker to the chart
			// mLineChart.setMarkerView(mv);

			// x-axis limit line
			LimitLine llXAxis = new LimitLine(10f, "Index 10");
			llXAxis.setLineWidth(4f);
			llXAxis.enableDashedLine(10f, 10f, 0f);
			llXAxis.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
			llXAxis.setTextSize(10f);

			XAxis xAxis = mLineChart.getXAxis();
			// xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
			// xAxis.addLimitLine(llXAxis); // add x-axis limit line

			Typeface tf = Typeface.createFromAsset(TApplication.getInstance()
					.getAssets(), "OpenSans-Regular.ttf");

			mLineChart.getAxisRight().setEnabled(false);

			// mChart.getViewPortHandler().setMaximumScaleY(2f);
			// mChart.getViewPortHandler().setMaximumScaleX(2f);

			// mChart.setVisibleXRange(20);
			// mChart.setVisibleYRange(20f, AxisDependency.LEFT);
			// mChart.centerViewTo(20, 50, AxisDependency.LEFT);

			mLineChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
			// mChart.invalidate();

			// get the legend (only possible after setting data)
			Legend l = mLineChart.getLegend();

			// modify the legend ...
			// l.setPosition(LegendPosition.LEFT_OF_CHART);
			l.setForm(LegendForm.LINE);

			// // dont forget to refresh the drawing
			// mChart.invalidate();
		}

		mContanerFrameLayout.addView(mLineChart, new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	private void initData() {
		ArrayList<String> xVals = new ArrayList<String>();
		ArrayList<Entry> yVals = new ArrayList<Entry>();
		ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

		try {
			if (!mDvsValues.isEmpty()) {
				float maxValue = 0;

				for (int i = 0; i < mDvsValues.size(); i++) {
					String time = TTimeUtils.gethourTimeString(mDvsValues
							.get(i).getClock() * 1000);
					xVals.add(time);
					float value = TStringUtils.toFloat((String) mDvsValues.get(
							i).getValue());
					if (maxValue < value)
						maxValue = value;
					yVals.add(new Entry(value, i));
				}

				// YAxis
				YAxis leftAxis = mLineChart.getAxisLeft();
				leftAxis.removeAllLimitLines(); // reset all limit lines to
												// avoid overlapping lines
				maxValue = (float) (maxValue + maxValue * 0.2);
				leftAxis.setAxisMaxValue(maxValue);
				leftAxis.setAxisMinValue(0);
				leftAxis.setStartAtZero(true);
				// leftAxis.setYOffset(20f);
				leftAxis.enableGridDashedLine(10f, 10f, 0f);

				// limit lines are drawn behind data (and not on top)
				leftAxis.setDrawLimitLinesBehindData(true);

				// other
				// create a dataset and give it a type
				LineDataSet set1 = new LineDataSet(yVals, "时间");
				// set1.setFillAlpha(110);
				// set1.setFillColor(Color.RED);

				// LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");

				// set1.setDrawCircles(false);
				//
				// set1.setCircleSize(5f);

				//
				// // create a data object with the datasets
				// LineData data = new LineData(xVals, set1);
				// data.setValueTypeface(tf);
				// data.setValueTextSize(9f);
				// data.setDrawValues(false);

				// set the line to be drawn like this "- - - - - -"
				set1.setDrawFilled(true);
				set1.setDrawCubic(true);
				set1.setCubicIntensity(0.2f);
				set1.setDrawCircles(false);
				set1.setLineWidth(2f);
				// set1.setCircleSize(3f);
				set1.setDrawValues(false);
				set1.setDrawCircleHole(false);
				set1.setValueTextSize(9f);
				set1.setFillAlpha(65);
				set1.setHighLightColor(Color.rgb(244, 117, 117));
				set1.setColor(Color.rgb(104, 241, 175));
				set1.setFillColor(Color.rgb(168, 210, 147));
				set1.setDrawHorizontalHighlightIndicator(false);

				dataSets.add(set1); // add the datasets
				// with the datasets
			}
		} catch (Exception e) {
			TToastUtils.makeText("数据异常");
		}

		LineData data = new LineData(xVals, dataSets);// create a data object
		mLineChart.setData(data);// set data
		mLineChart.invalidate();
	}

	private void onGetHistoryDataTask() {
		if (mGetHistoryDataTask == null)
			mGetHistoryDataTask = new TTask();
		mGetHistoryDataTask.setIXTaskListener(this);
		mGetHistoryDataTask.stopTask();
		mGetHistoryDataTask.startTask("");
	}
}
