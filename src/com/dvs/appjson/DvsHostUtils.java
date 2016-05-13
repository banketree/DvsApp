package com.dvs.appjson;

import org.json.JSONObject;

import com.treecore.utils.TJBsonUtils;
import com.treecore.utils.TStringUtils;

public class DvsHostUtils {
	private static String Field_Hostid = "_hostid";
	private static String Field_host = "_host";
	private static String Field_name = "_name";
	private static String Field_available = "_available";
	private static String Field_status = "_status";
	private static String Field_description = "_description";

	private static String Field_location_lat = "_location_lat";
	private static String Field_location_lon = "_location_lon";

	public static DvsHost setJson(String json) {
		try {
			return setJson(new JSONObject(json));
		} catch (Exception e) {
		}

		return null;
	}

	public static DvsHost setJson(JSONObject jsonObject) {
		DvsHost host = new DvsHost();
		host.setHostid(TJBsonUtils.getString(jsonObject, Field_Hostid));
		host.setHost(TJBsonUtils.getString(jsonObject, Field_host));
		host.setName(TJBsonUtils.getString(jsonObject, Field_name));
		host.setAvailable(TJBsonUtils.getInt(jsonObject, Field_available));
		host.setStatus(TJBsonUtils.getInt(jsonObject, Field_status));
		host.setDescription(TJBsonUtils
				.getString(jsonObject, Field_description));
		String lat = TJBsonUtils.getString(jsonObject, Field_location_lat);
		String lon = TJBsonUtils.getString(jsonObject, Field_location_lon);
		if (!TStringUtils.isEmpty(lat) && !TStringUtils.isEmpty(lon)) {
			DvsInventory inventory = new DvsInventory();
			inventory.setHostid(host.getHostid());
			inventory.setLocation_lat(lat);
			inventory.setLocation_lon(lon);
			host.setInventory(inventory);
		}
		return host;
	}

	public static JSONObject getJson(DvsHost host) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Field_Hostid, host.getHostid());
			jsonObject.put(Field_host, host.getHost());
			jsonObject.put(Field_name, host.getName());
			jsonObject.put(Field_available, host.getAvailable());
			jsonObject.put(Field_status, host.getStatus());
			jsonObject.put(Field_description, host.getDescription());

			DvsInventory inventory = host.getInventory();
			if (inventory != null
					&& !TStringUtils.isEmpty(inventory.getLocation_lat())
					&& !TStringUtils.isEmpty(inventory.getLocation_lon())) {
				jsonObject.put(Field_location_lat, inventory.getLocation_lat());
				jsonObject.put(Field_location_lon, inventory.getLocation_lon());
			}
		} catch (Exception e) {
		}

		return jsonObject;
	}
}
