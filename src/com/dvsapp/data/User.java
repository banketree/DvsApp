package com.dvsapp.data;

import org.json.JSONObject;

import com.dvs.appjson.DvsUser;
import com.treecore.utils.TJBsonUtils;

class User extends DvsUser {
	private static final String Field_UserId = "_userid";
	private static final String Field_Alias = "_alias";
	private static final String Field_Name = "_name";
	private static final String Field_Surname = "_surname";
	private static final String Field_Url = "_url";
	private static final String Field_AutoLogin = "_autologin";
	private static final String Field_AutoLogout = "_autologout";
	private static final String Field_Lang = "_lang";
	private static final String Field_Refresh = "_refresh";
	private static final String Field_Type = "_type";
	private static final String Field_Theme = "_theme";
	private static final String Field_Attempt_Failed = "_attempt_failed";
	private static final String Field_Attempt_Ip = "_attempt_ip";
	private static final String Field_Attempt_Clock = "_attempt_clock";
	private static final String Field_Rows_Per_Page = "_rows_per_page";
	private static final String Field_Debug_Mode = "_debug_mode";
	private static final String Field_UserIp = "_userip";
	private static final String Field_SessionId = "_sessionid";
	private static final String Field_Gui_Access = "_gui_access";

	public void setUser(DvsUser user) {
		setUserid(user.getUserid());
		setAlias(user.getAlias());
		setName(user.getName());
		setSurname(user.getSurname());
		setUrl(user.getUrl());
		setAutologin(user.getAutologin());
		setAutologout(user.getAutologout());
		setLang(user.getLang());
		setRefresh(user.getRefresh());
		setType(user.getType());
		setTheme(user.getTheme());
		setAttempt_clock(user.getAttempt_clock());
		setAttempt_failed(user.getAttempt_failed());
		setAttempt_ip(user.getAttempt_ip());
		setRows_per_page(user.getRows_per_page());
		setDebug_mode(user.getDebug_mode());
		setUserip(user.getUserip());
		setSessionid(user.getSessionid());
		setGui_access(user.getGui_access());
	}

	public void setJson(String json) {
		try {
			setJson(new JSONObject(json));
		} catch (Exception e) {
		}
	}

	public void setJson(JSONObject jsonObject) {
		setUserid(TJBsonUtils.getString(jsonObject, Field_UserId));
		setAlias(TJBsonUtils.getString(jsonObject, Field_Alias));
		setName(TJBsonUtils.getString(jsonObject, Field_Name));
		setSurname(TJBsonUtils.getString(jsonObject, Field_Surname));
		setUrl(TJBsonUtils.getString(jsonObject, Field_Url));
		setAutologin(TJBsonUtils.getInt(jsonObject, Field_AutoLogin));
		setAutologout(TJBsonUtils.getInt(jsonObject, Field_AutoLogout));
		setLang(TJBsonUtils.getString(jsonObject, Field_Lang));
		setRefresh(TJBsonUtils.getInt(jsonObject, Field_Refresh));
		setType(TJBsonUtils.getInt(jsonObject, Field_Type));
		setTheme(TJBsonUtils.getString(jsonObject, Field_Theme));
		setAttempt_clock(TJBsonUtils.getLong(jsonObject, Field_Attempt_Failed));
		setAttempt_failed(TJBsonUtils.getInt(jsonObject, Field_Attempt_Ip));
		setAttempt_ip(TJBsonUtils.getString(jsonObject, Field_Attempt_Clock));
		setRows_per_page(TJBsonUtils.getInt(jsonObject, Field_Rows_Per_Page));
		setDebug_mode(TJBsonUtils.getInt(jsonObject, Field_Debug_Mode) == 0 ? false
				: true);
		setUserip(TJBsonUtils.getString(jsonObject, Field_UserIp));
		setSessionid(TJBsonUtils.getString(jsonObject, Field_SessionId));
		setGui_access(TJBsonUtils.getInt(jsonObject, Field_Gui_Access));
	}

	public JSONObject getJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Field_UserId, getUserid());
			jsonObject.put(Field_Alias, getAlias());
			jsonObject.put(Field_Name, getName());
			jsonObject.put(Field_Surname, getSurname());
			jsonObject.put(Field_Url, getUrl());
			jsonObject.put(Field_AutoLogin, getAutologin());
			jsonObject.put(Field_AutoLogout, getAutologout());
			jsonObject.put(Field_Lang, getLang());
			jsonObject.put(Field_Refresh, getRefresh());
			jsonObject.put(Field_Type, getType());
			jsonObject.put(Field_Theme, getTheme());
			jsonObject.put(Field_Attempt_Failed, getAttempt_failed());
			jsonObject.put(Field_Attempt_Ip, getAttempt_ip());
			jsonObject.put(Field_Attempt_Clock, getAttempt_clock());
			jsonObject.put(Field_Rows_Per_Page, getRows_per_page());
			jsonObject.put(Field_Debug_Mode, getDebug_mode() ? 1 : 0);
			jsonObject.put(Field_UserIp, getUserip());
			jsonObject.put(Field_SessionId, getSessionid());
			jsonObject.put(Field_Gui_Access, getGui_access());
		} catch (Exception e) {
		}

		return jsonObject;
	}
}
