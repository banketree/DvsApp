package com.dvsapp.ui;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsCfgVer;
import com.dvs.appjson.DvsUser;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.utils.CoreEventUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TActivityUtils;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.config.TPreferenceConfig;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

//登录
public class Login extends BaseActivity implements OnClickListener {
	private TTask mLoginTask;
	private EditText mPhoneEditText, mPasswordEditText;
	private boolean mLoginSuccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mLoginTask != null)
			mLoginTask.stopTask();
		mLoginTask = null;
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mLoginTask != null && mLoginTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(mContext, "登陆", "正在登陆，请稍候……",
						false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(mContext);

				if (mLoginSuccess) {
					makeText("登陆成功");

					TActivityUtils.jumpToNewTopActivity(mContext, Main.class);

					finish();
				} else {
					makeText("登陆失败");
				}
			} else if (event == TaskEvent.Work) {
				try {
					DvsCfgVer cfgver = new DvsCfgVer();
					cfgver.setCfgver(Setting.getCfgVersion());

					String phone = mPhoneEditText.getText().toString();
					String password = mPasswordEditText.getText().toString();

					DvsUser user = DvsAPI2.userLogin(phone, password, cfgver);

					if (user != null
							&& !TStringUtils.isEmpty(user.getSessionid())) {
						Account.getInstance().release();
						Account.getInstance().setUser(user);
						Account.getInstance().saveUserInfo();

						mLoginSuccess = true;

					} else {
						mLoginSuccess = false;

					}
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	@Override
	public void processEventByInner(Intent intent) {
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.Button_submit) {
			login();
		} else if (view.getId() == R.id.Button_demo) {
			mPhoneEditText.setText("guest");
			mPasswordEditText.setText("");
			login();
		} else if (view.getId() == R.id.Button_host_agent) {
			TActivityUtils.jumpToActivity(mContext, HostAgent.class);
		}
	}

	private void initView() {
		mPhoneEditText = (EditText) findViewById(R.id.EditText_phone);
		mPasswordEditText = (EditText) findViewById(R.id.EditText_password);

		findViewById(R.id.Button_submit).setOnClickListener(this);
		findViewById(R.id.Button_demo).setOnClickListener(this);
		findViewById(R.id.Button_host_agent).setOnClickListener(this);

	}

	private void login() {
		if (!TNetWorkUtil.isNetworkConnected()) {
			makeText("无网络，请检查网络是否连接正常！");
			return;
		}

		String phone = mPhoneEditText.getText().toString();
		String password = mPasswordEditText.getText().toString();

		if (TStringUtils.isEmpty(phone)) {
			makeText("帐号不能为空");
			return;
		} else if (!phone.equalsIgnoreCase("guest")
				&& TStringUtils.isEmpty(password)) {
			makeText("密码不能为空");
			return;
		}

		if (mLoginTask == null) {
			mLoginTask = new TTask();
		}

		mLoginTask.setIXTaskListener(this);
		mLoginTask.stopTask();
		mLoginTask.startTask("");
	}

}
