package com.dvs.appjson;

import org.json.JSONObject;

import com.treecore.utils.TJBsonUtils;

public class DvsItemUtils {

	private static String Field_itemid = "_itemid";
	private static String Field_name = "_name";
	private static String Field_key_ = "_key_";
	private static String Field_units = "_units";
	private static String Field_value_type = "_value_type";
	private static String Field_delay = "_delay";
	private static String Field_status = "_status";

	public static DvsItem setJson(String json) {
		try {
			return setJson(new JSONObject(json));
		} catch (Exception e) {
		}

		return null;
	}

	public static DvsItem setJson(JSONObject jsonObject) {
		DvsItem item = new DvsItem();
		item.setItemid(TJBsonUtils.getString(jsonObject, Field_itemid));
		item.setName(TJBsonUtils.getString(jsonObject, Field_name));
		item.setKey_(TJBsonUtils.getString(jsonObject, Field_key_));
		item.setUnits(TJBsonUtils.getString(jsonObject, Field_units));
		item.setValue_type(TJBsonUtils.getInt(jsonObject, Field_value_type));
		item.setDelay(TJBsonUtils.getInt(jsonObject, Field_delay));
		item.setStatus(TJBsonUtils.getInt(jsonObject, Field_status));
		return item;
	}

	public static JSONObject getJson(DvsItem item) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Field_itemid, item.getItemid());
			jsonObject.put(Field_name, item.getName());
			jsonObject.put(Field_key_, item.getKey_());
			jsonObject.put(Field_units, item.getUnits());
			jsonObject.put(Field_value_type, item.getValue_type());
			jsonObject.put(Field_delay, item.getDelay());
			jsonObject.put(Field_status, item.getStatus());
		} catch (Exception e) {
		}

		return jsonObject;
	}
}
