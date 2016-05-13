package com.dvsapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsCfgVer;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TAppUtils;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

//帮助支持
public class Help extends BaseActivity implements OnClickListener {
	private EditText mNameEditText, mContactEditText, mContentEditText;
	private TTask mSubmitTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		initView();
	}

	@Override
	public void processEventByInner(Intent intent) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.Button_submit) {
			submit();
		} else if (view.getId() == R.id.TextView_web) {
			TextView content = (TextView) view;
			TAppUtils.clipToboard(content.getText().toString());
			makeText("复制成功");
		} else if (view.getId() == R.id.TextView_weixin) {
			TextView content = (TextView) view;
			TAppUtils.clipToboard(content.getText().toString());
			makeText("复制成功");
		} else if (view.getId() == R.id.TextView_qq) {
			TextView content = (TextView) view;
			TAppUtils.clipToboard(content.getText().toString());
			makeText("复制成功");
		} else if (view.getId() == R.id.TextView_email) {
			TextView content = (TextView) view;
			TAppUtils.clipToboard(content.getText().toString());
			makeText("复制成功");
		}
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mSubmitTask != null && mSubmitTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(mContext, "意见", "正在提交，请稍候……",
						false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(mContext);
				String result = task.getResultObject() != null ? (String) task
						.getResultObject() : "";
				if (!TStringUtils.isEmpty(result)) {
					makeText((String) task.getResultObject());

					if (result.contains("成功")) {
						mNameEditText.setText("");
						mContactEditText.setText("");
						mContentEditText.setText("");
					}
				} else if (!TStringUtils.isEmpty(task.getError())) {
					makeText(task.getError());
				}
			} else if (event == TaskEvent.Work) {
				try {
					DvsCfgVer cfgver = new DvsCfgVer();
					cfgver.setCfgver(Setting.getCfgVersion());

					String name = mNameEditText.getText().toString();
					String contact = mContactEditText.getText().toString();
					String content = mContentEditText.getText().toString();

					// 用户上传意见和建议
					if (DvsAPI2.userMessage(Account.getInstance()
							.getSessionid(), name, contact, content, cfgver)) {
						task.setResultObject("意见上传成功!");
					} else {
						task.setResultObject("意见上传失败!");
					}
				} catch (Exception e) {
					task.setError(e != null ? e.getMessage() : "");
				}
			}
		}
	}

	private void initView() {
		mNameEditText = (EditText) findViewById(R.id.EditText_name);
		mContactEditText = (EditText) findViewById(R.id.EditText_contact);
		mContentEditText = (EditText) findViewById(R.id.EditText_content);

		findViewById(R.id.Button_submit).setOnClickListener(this);

		findViewById(R.id.TextView_web).setOnClickListener(this);
		findViewById(R.id.TextView_weixin).setOnClickListener(this);
		findViewById(R.id.TextView_qq).setOnClickListener(this);
		findViewById(R.id.TextView_email).setOnClickListener(this);
	}

	private void submit() {
		if (!TNetWorkUtil.isNetworkConnected()) {
			makeText("无网络，请检查网络是否连接正常！");
			return;
		}

		String name = mNameEditText.getText().toString();
		String contact = mContactEditText.getText().toString();
		String content = mContentEditText.getText().toString();

		if (TStringUtils.isEmpty(name)) {
			makeText("名字不能为空");
			return;
		} else if (TStringUtils.isEmpty(contact)) {
			makeText("联系方式不能为空");
			return;
		} else if (TStringUtils.isEmpty(content)) {
			makeText("内容不能为空");
			return;
		}

		if (mSubmitTask == null) {
			mSubmitTask = new TTask();
		}

		mSubmitTask.setIXTaskListener(this);
		mSubmitTask.stopTask();
		mSubmitTask.startTask("");
	}
}