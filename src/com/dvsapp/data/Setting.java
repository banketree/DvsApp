package com.dvsapp.data;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dvs.appjson.DvsCfgVer;
import com.dvs.appjson.DvsHostGroupUtils;
import com.treecore.utils.config.TPreferenceConfig;
import com.treecore.utils.config.TPropertiesConfig;

public class Setting {
	private static final String Field_Cfgver = "_cfgver";
	private static final String Field_Info_Refresh_Time = "_info_refresh_time";
	private static final String Field_Monitor_Refresh_Time = "_monitor_refresh_time";
	private static final String Field_Monitor_Time_Index = "_monitor_time_index";
	private static final String Field_Monitor_Host_Agent = "_monitor_Host_Agent";
	public static final String Host_Agent = "http://www.danvess.com//api_jsonrpc.php";// "http://123.57.141.84//api_jsonrpc.php";

	public static void setCfgVersion(int cfgver) {
		TPreferenceConfig.getInstance().setInt(Field_Cfgver, cfgver);
	}

	public static int getCfgVersion() {
		return TPreferenceConfig.getInstance().getInt(Field_Cfgver, -1);
	}

	public static DvsCfgVer getDvsCfgVer() {
		DvsCfgVer cfgver = new DvsCfgVer();
		cfgver.setCfgver(Setting.getCfgVersion());
		return cfgver;
	}

	public static Long getInfoRefreshTime() {
		return TPreferenceConfig.getInstance().getLong(Field_Info_Refresh_Time,
				(long) 0);
	}

	public static void setInfoRefreshTime(long time) {
		TPreferenceConfig.getInstance().setLong(Field_Info_Refresh_Time, time);
	}

	public static Long getMonitorRefreshTime() {
		return TPreferenceConfig.getInstance().getLong(
				Field_Monitor_Refresh_Time, (long) 0);
	}

	public static void setMonitorRefreshTime(long time) {
		TPreferenceConfig.getInstance().setLong(Field_Monitor_Refresh_Time,
				time);
	}

	public static void setMonitorTimeIndex(int index) {
		TPreferenceConfig.getInstance().setInt(Field_Monitor_Time_Index, index);
	}

	public static int getMonitorTimeIndex() {
		return TPreferenceConfig.getInstance().getInt(Field_Monitor_Time_Index,
				0);
	}

	public static void setHostAgent(String agent) {
		// TPropertiesConfig.getInstance().setString(Field_Monitor_Host_Agent,
		// agent);
	}

	public static String getHostAgent() {
		return Host_Agent;// TPropertiesConfig.getInstance().getString(Field_Monitor_Host_Agent,Host_Agent
							// );
	}
}
