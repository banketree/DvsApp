package com.dvsapp.ui;

import com.dvsapp.wisdom.R;
import com.dvsapp.data.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class HostAgent extends BaseActivity implements OnClickListener {
	private EditText mHostAgentEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_agent);
		initView();
	}

	@Override
	public void processEventByInner(Intent intent) {

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.Button_reset) {
			mHostAgentEditText.setText(Setting.Host_Agent);
			Setting.setHostAgent(Setting.Host_Agent);
			makeText("恢复默认服务器");
		} else if (view.getId() == R.id.Button_submit) {
			String address = mHostAgentEditText.getText().toString();

			if (address.contains("http") || address.contains("https")) {
				Setting.setHostAgent(address);
				makeText("服务器设置为：" + Setting.getHostAgent());
				finish();
			} else {
				makeText("无效服务器地址，请输入有效地址，例如:http://***");
				return;
			}
		}
	}

	private void initView() {
		mHostAgentEditText = (EditText) findViewById(R.id.EditText_host_agent);
		mHostAgentEditText.setText(Setting.getHostAgent());
		findViewById(R.id.Button_reset).setOnClickListener(this);
		findViewById(R.id.Button_submit).setOnClickListener(this);
	}
}