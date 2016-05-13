package com.dvsapp.utils;

import org.json.JSONObject;

import android.content.Context;

import com.dvs.appjson.DvsHostGroupUtils;
import com.treecore.utils.TToastUtils;

public class DvsUtils {
	public static String[] SpinnerTimeStrings = { "禁止", "5秒", "10秒", "15秒",
			"30秒", "1分钟", "2分钟", "5分钟", "15分钟", "30分钟", "1小时" };

	public static String[] TriggerStrings = { "一般", "警告", "严重" };

	private static long mExitTime = 0;

	public static void showSystemExitBySecond(Context context) {
		if ((System.currentTimeMillis() - mExitTime) > 2000) {
			TToastUtils.makeText("再按一次退出程序");
			mExitTime = System.currentTimeMillis();
		} else {
			System.exit(0);
		}
	}

	public static String getTriggerString(int priority) {
		int result = 0;
		if (priority == 1) {
		} else if (priority == 2) {
			result = 1;
		} else {
			result = 2;
		}
		return TriggerStrings[priority];
	}

	public static long getSpinnerTime(int index) {
		long result = 0;
		if (index == 0) {
		} else if (index == 1) {
			result = 5;
		} else if (index == 2) {
			result = 10;
		} else if (index == 3) {
			result = 15;
		} else if (index == 4) {
			result = 30;
		} else if (index == 5) {
			result = 60;
		} else if (index == 6) {
			result = 60 * 2;
		} else if (index == 7) {
			result = 60 * 5;
		} else if (index == 8) {
			result = 60 * 15;
		} else if (index == 9) {
			result = 60 * 30;
		} else if (index == 10) {
			result = 60 * 60;
		}
		return result;
	}
}
