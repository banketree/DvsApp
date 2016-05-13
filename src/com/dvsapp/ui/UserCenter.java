package com.dvsapp.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.dvsapp.utils.DvsUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.utils.TActivityUtils;
import com.treecore.utils.config.TPreferenceConfig;

//用户中心
public class UserCenter extends BaseActivity implements OnClickListener {
	private Spinner mMonitorTimeSpinner, mLayoutTimeSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_center);

		initView();
	}

	@Override
	public void processEventByInner(Intent intent) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		mMonitorTimeSpinner = (Spinner) findViewById(R.id.Spinner_monitor_time);
		mLayoutTimeSpinner = (Spinner) findViewById(R.id.Spinner_layout_time);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				DvsUtils.SpinnerTimeStrings);

		mMonitorTimeSpinner.setAdapter(arrayAdapter);// 设置显示的数据
		mLayoutTimeSpinner.setAdapter(arrayAdapter);// 设置显示的数据

		mMonitorTimeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						TextView tv = (TextView) view;
						tv.setTextColor(getResources().getColor(R.color.white)); // 设置颜色
						tv.setTextSize(12F);
						Spinner spinner = (Spinner) parent;
						Setting.setMonitorTimeIndex(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
		mMonitorTimeSpinner.setSelection(Setting.getMonitorTimeIndex());

		mLayoutTimeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						TextView tv = (TextView) view;
						tv.setTextColor(getResources().getColor(R.color.white)); // 设置颜色
						tv.setTextSize(12F);
						Spinner spinner = (Spinner) parent;

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});

		findViewById(R.id.Button_layout).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.Button_layout) {
			showLayout();
		}
	}

	private void showLayout() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage("确认注销吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Account.getInstance().layout();
				TPreferenceConfig.getInstance().removeAll();
				TPreferenceConfig.getInstance().clear();
				Main.clearData();
				TActivityUtils.jumpToNewTopActivity(mContext, Login.class);
				TBroadcastByInner
						.sentEvent(CoreEventUtils.Activity_Self_Destory);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}