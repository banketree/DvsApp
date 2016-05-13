package com.dvs.appjson;

import org.json.JSONObject;

import com.treecore.utils.TJBsonUtils;

public class DvsHostGroupUtils {
	public static String Field_Group_Id = "_groupid";
	public static String Field_Name = "_name";
	public static String Field_Flags = "_flags";
	public static String Field_Internal = "_internal";

	public static DvsHostGroup setJson(String json) {
		try {
			return setJson(new JSONObject(json));
		} catch (Exception e) {
		}

		return null;
	}

	public static DvsHostGroup setJson(JSONObject jsonObject) {
		DvsHostGroup hostGroup = new DvsHostGroup();
		hostGroup.setGroupid(TJBsonUtils.getString(jsonObject, Field_Group_Id));
		hostGroup.setName(TJBsonUtils.getString(jsonObject, Field_Name));
		hostGroup.setFlags(TJBsonUtils.getInt(jsonObject, Field_Flags));
		hostGroup.setInternal(TJBsonUtils.getInt(jsonObject, Field_Internal));
		return hostGroup;
	}

	public static JSONObject getJson(DvsHostGroup hostGroup) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Field_Group_Id, hostGroup.getGroupid());
			jsonObject.put(Field_Name, hostGroup.getName());
			jsonObject.put(Field_Flags, hostGroup.getFlags());
			jsonObject.put(Field_Internal, hostGroup.getdInternal());
		} catch (Exception e) {
		}

		return jsonObject;
	}
}
