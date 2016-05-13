package com.dvsapp.data;

import com.treecore.TIGlobalInterface;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.config.TPreferenceConfig;

public class Account extends User implements TIGlobalInterface {
	private static final String Field_User_Info = "user_info";

	private static Account mThis;

	public Account() {
		initConfig();
	}

	public static Account getInstance() {
		if (mThis == null) {
			mThis = new Account();
		}

		return mThis;
	}

	@Override
	public void initConfig() {
		setJson(getUserInfo());
	}

	@Override
	public void release() {
		mThis = null;
	}

	public void layout() {
		setUserInfo("");
		release();
	}

	public boolean isValid() {
		return !TStringUtils.isEmpty(getSessionid());
	}

	public void saveUserInfo() {
		setUserInfo(getJson().toString());
	}

	public static void setUserInfo(String json) {
		TPreferenceConfig.getInstance().setString(Field_User_Info, json);
	}

	public static String getUserInfo() {
		return TPreferenceConfig.getInstance().getString(Field_User_Info, "");
	}
}
