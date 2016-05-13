package com.dvs.appjson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.dvsapp.data.Setting;

public class DvsAPI2 {
	private static boolean OPEN_LOG = false;

	private static int MAX_LIMIT = 1000;
	public static final int HISTORY_DATATYPE_FLOAT = 0;
	public static final int HISTORY_DATATYPE_STRING = 1;
	public static final int HISTORY_DATATYPE_LOG = 2;
	public static final int HISTORY_DATATYPE_INTEGER = 3;
	public static final int HISTORY_DATATYPE_TEXT = 4;

	public static void openLog(boolean openlog) {
		OPEN_LOG = openlog;
	}

	public static String urlPostMethod(String url, String params) {
		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(15 * 1000);
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(15 * 1000);

		PostMethod method = new PostMethod(url);
		try {
			if ((params != null) && (!params.trim().equals(""))) {
				StringRequestEntity requestEntity = new StringRequestEntity(
						params, "application/json-rpc", "UTF-8");
				method.setRequestEntity(requestEntity);
			}
			httpClient.executeMethod(method);
			String responses = method.getResponseBodyAsString();
			method.releaseConnection();
			return responses;
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static DvsUser userLogin(String username, String password,
			DvsCfgVer dcf) {
		if (dcf == null) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("user.login");
		DvsRequest.params_user_login pul = new DvsRequest.params_user_login();
		pul.setUser(username);
		pul.setPassword(password);
		zad.setParams(pul);
		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_User rsp = (DvsResult_User) JsonUtils.objectFromJson(s,
					DvsResult_User.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsUser user = rsp.getResult();
				if (user != null) {
					System.out.println(user.getUserid() + "@" + user.getName()
							+ "@" + user.getAlias() + "@" + user.getSurname()
							+ "@" + user.getSessionid() + "@"
							+ user.getRefresh() + "秒 @" + user.getUrl()
							+ "@cfgver" + rsp.getCfgver());

					return user;
				}
			}
		}

		return null;
	}

	public static DvsHostGroup[] hostGroupsGet(String sessonid, DvsCfgVer dcf) {
		if (dcf == null) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("hostgroup.get");
		zad.setAuth(sessonid);

		DvsRequest.params_hostgroup_get phg = new DvsRequest.params_hostgroup_get();
		String[] output = { "groupid", "name", "flags", "internal" };
		phg.setOutput(output);
		zad.setParams(phg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsHostGroupList rsp = (DvsResultList_DvsHostGroupList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsHostGroupList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static boolean hostGroupUpdate(String sessonid, String hostGroupID,
			String hostGroupName, DvsCfgVer dcf) {
		if ((hostGroupID == null) || (hostGroupName == null) || (dcf == null)) {
			return false;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("hostgroup.update");
		zad.setAuth(sessonid);

		DvsRequest.params_hostgroup_update phu = new DvsRequest.params_hostgroup_update();
		phu.setGroupid(hostGroupID);
		phu.setName(hostGroupName);
		zad.setParams(phu);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_GroupUpdate rsp = (DvsResult_GroupUpdate) JsonUtils
					.objectFromJson(s, DvsResult_GroupUpdate.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsResult_GroupUpdate.DvsHostGroupForUpdate dgfu = rsp
						.getResult();
				if ((dgfu != null) && (dgfu.getGroupids() != null)
						&& (dgfu.getGroupids().length > 0)
						&& (dgfu.getGroupids()[0].equals(hostGroupID))) {
					return true;
				}
			}
		}

		return false;
	}

	public static DvsEvent[] groupEventsGet(String sessonid,
			String[] hostGroupIDs, Date time_from, Date time_till, int limit,
			DvsCfgVer dcf) {
		if ((hostGroupIDs == null) || (time_from == null)
				|| (time_till == null) || (limit > MAX_LIMIT) || (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("event.get");
		zad.setAuth(sessonid);

		DvsRequest.params_event_get peg = new DvsRequest.params_event_get();
		String output = "extend";
		peg.setOutput(output);
		if (hostGroupIDs != null) {
			peg.setGroupids(hostGroupIDs);
		}
		if ((time_from != null) && (time_till != null)) {
			peg.setTime_from(String.valueOf(Math.round((float) time_from
					.getTime() / 1000.0F)));
			peg.setTime_till(String.valueOf(Math.round((float) time_till
					.getTime() / 1000.0F)));
		}
		peg.setLimit(limit);
		zad.setParams(peg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsEventList rsp = (DvsResultList_DvsEventList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsEventList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static DvsTriggerSum[] groupTriggersSumGet(String sessonid,
			DvsCfgVer dcf) {
		if (dcf == null) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("trigger.getsum");
		zad.setAuth(sessonid);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsTriggerSumList rsp = (DvsResultList_DvsTriggerSumList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsTriggerSumList.class);

			dcf.setCfgver(rsp.getCfgver());

			return rsp.getResult();
		}

		return null;
	}

	public static DvsTrigger[] groupTriggersGet(String sessonid,
			String[] hostGroupIDs, int limit, DvsCfgVer dcf) {
		if ((hostGroupIDs == null) || (limit > MAX_LIMIT) || (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("trigger.get");
		zad.setAuth(sessonid);

		DvsRequest.params_trigger_get ptg = new DvsRequest.params_trigger_get();
		String output = "extend";
		ptg.setOutput(output);
		if (hostGroupIDs != null) {
			ptg.setGroupids(hostGroupIDs);
		}

		ptg.setLimit(limit);
		zad.setParams(ptg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsTriggerList rsp = (DvsResultList_DvsTriggerList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsTriggerList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static DvsHost[] groupHostsGet(String sessonid,
			String[] hostGroupIDs, String[] triggerIDs,
			boolean containLocation, DvsCfgVer dcf) {
		if ((hostGroupIDs == null) || (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("host.get");
		zad.setAuth(sessonid);

		DvsRequest.params_host_get phg = new DvsRequest.params_host_get();
		String[] output = { "hostid", "host", "name", "available", "status",
				"description" };
		phg.setOutput(output);
		String[] selectInventory = { "location_lon", "location_lat" };
		if (containLocation) {
			phg.setSelectInventory(selectInventory);
		}
		if (hostGroupIDs != null) {
			phg.setGroupids(hostGroupIDs);
		}
		if (triggerIDs != null) {
			phg.setTriggerids(triggerIDs);
		}
		zad.setParams(phg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsHostList rsp = (DvsResultList_DvsHostList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsHostList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static boolean hostUpdate(String sessonid, String hostID,
			String hostName, DvsCfgVer dcf) {
		if ((hostID == null) || (hostName == null) || (dcf == null)) {
			return false;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("host.update");
		zad.setAuth(sessonid);

		DvsRequest.params_host_update phu = new DvsRequest.params_host_update();
		phu.setHostid(hostID);
		phu.setName(hostName);
		zad.setParams(phu);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_HostUpdate rsp = (DvsResult_HostUpdate) JsonUtils
					.objectFromJson(s, DvsResult_HostUpdate.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsResult_HostUpdate.DvsHostForUpdate dfu = rsp.getResult();
				if ((dfu != null) && (dfu.getHostids() != null)
						&& (dfu.getHostids().length > 0)
						&& (dfu.getHostids()[0].toString().equals(hostID))) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean hostLocation(String sessonid, String hostID,
			String location_lon, String location_lat, DvsCfgVer dcf) {
		if ((hostID == null) || (dcf == null)) {
			return false;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("host.update");
		zad.setAuth(sessonid);

		DvsRequest.params_host_inventory phl = new DvsRequest.params_host_inventory();
		phl.setHostid(hostID);
		DvsLocation dl = new DvsLocation();
		dl.setLocation_lon(location_lon);
		dl.setLocation_lat(location_lat);
		phl.setInventory(dl);
		zad.setParams(phl);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_HostUpdate rsp = (DvsResult_HostUpdate) JsonUtils
					.objectFromJson(s, DvsResult_HostUpdate.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsResult_HostUpdate.DvsHostForUpdate dfu = rsp.getResult();
				if ((dfu != null) && (dfu.getHostids() != null)
						&& (dfu.getHostids().length > 0)
						&& (dfu.getHostids()[0].toString().equals(hostID))) {
					return true;
				}
			}
		}

		return false;
	}

	public static DvsItem[] hostItemsGet(String sessonid, String[] hostIDs,
			String application, String key, DvsCfgVer dcf) {
		if ((hostIDs == null) || (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("item.get");
		zad.setAuth(sessonid);

		DvsRequest.params_host_item_get phig = new DvsRequest.params_host_item_get();
		phig.setHostids(hostIDs);
		if (application != null) {
			phig.setApplication(application);
		}
		DvsRequest.params_host_item_get.params_host_item_get_search phgs = new DvsRequest.params_host_item_get.params_host_item_get_search();
		if (key != null) {
			phgs.setKey_(key);
		}
		phig.setSearch(phgs);
		String[] output = { "itemid", "key_", "name", "value_type", "delay",
				"status" };
		phig.setOutput(output);
		zad.setParams(phig);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsItemList rsp = (DvsResultList_DvsItemList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsItemList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static DvsValue[] historyDataGet(String sessonid, String[] _itemids,
			int history, Date time_from, Date time_till, int limit,
			DvsCfgVer dcf) {
		if ((_itemids == null) || (time_from == null) || (time_till == null)
				|| (limit > MAX_LIMIT) || (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("history.get");
		zad.setAuth(sessonid);

		DvsRequest.params_history_get phg = new DvsRequest.params_history_get();
		if (_itemids != null) {
			phg.setItemids(_itemids);
		}
		phg.setHistory(history);

		if ((time_from != null) && (time_till != null)) {
			phg.setTime_from(String.valueOf(Math.round((float) time_from
					.getTime() / 1000.0F)));
			phg.setTime_till(String.valueOf(Math.round((float) time_till
					.getTime() / 1000.0F)));
		}
		phg.setLimit(limit);
		zad.setParams(phg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsHistoryDataList rsp = (DvsResultList_DvsHistoryDataList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsHistoryDataList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static DvsValue[] historyDataGetLast(String sessonid,
			String[] itemids, int history, Date time_from, Date time_till,
			String sortorder, DvsCfgVer dcf) {
		if ((itemids == null) || (time_from == null) || (time_till == null)
				|| (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("history.getlast");
		zad.setAuth(sessonid);

		DvsRequest.params_history_get phg = new DvsRequest.params_history_get();
		if (itemids != null) {
			phg.setItemids(itemids);
		}
		phg.setHistory(history);

		if ((time_from != null) && (time_till != null)) {
			phg.setTime_from(String.valueOf(Math.round((float) time_from
					.getTime() / 1000.0F)));
			phg.setTime_till(String.valueOf(Math.round((float) time_till
					.getTime() / 1000.0F)));
		}
		if (sortorder != null) {
			phg.setSortorder(sortorder);
		}
		zad.setParams(phg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsHistoryDataList rsp = (DvsResultList_DvsHistoryDataList) JsonUtils
					.objectFromJson(s, DvsResultList_DvsHistoryDataList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				return rsp.getResult();
			}
		}

		return null;
	}

	public static DvsValue[] historyDataGetPage(String sessonid,
			String[] itemids, int pageindex, int pagesize, int history,
			Date time_from, Date time_till, boolean distinct, String sortorder,
			DvsCfgVer dcf) {
		if ((itemids == null) || (time_from == null) || (time_till == null)
				|| (dcf == null)) {
			return null;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("history.getpage");
		zad.setAuth(sessonid);

		DvsRequest.params_history_get phg = new DvsRequest.params_history_get();
		if (itemids != null) {
			phg.setItemids(itemids);
		}
		phg.setPageindex(pageindex);
		phg.setPagesize(pagesize);
		phg.setDistinct(false);
		phg.setHistory(history);

		if ((time_from != null) && (time_till != null)) {
			phg.setTime_from(String.valueOf(Math.round((float) time_from
					.getTime() / 1000.0F)));
			phg.setTime_till(String.valueOf(Math.round((float) time_till
					.getTime() / 1000.0F)));
		}
		if (sortorder != null) {
			phg.setSortorder(sortorder);
		}
		zad.setParams(phg);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResultList_DvsHistroyDataPageList rsp = (DvsResultList_DvsHistroyDataPageList) JsonUtils
					.objectFromJson(s,
							DvsResultList_DvsHistroyDataPageList.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsValuePage dvp = rsp.getResult();
				if (dvp != null) {
					DvsValue[] dv = dvp.getData();
					if ((dv != null) && (dv.length > 0)) {
						for (int i = 0; i < dv.length; i++) {
							dv[i].setPagecount(dvp.getPagecount());
						}
					}
				}
				return rsp.getResult().getData();
			}
		}

		return null;
	}

	public static boolean userMessage(String sessonid, String name,
			String contact, String content, DvsCfgVer dcf) {
		if ((name == null) || (contact == null) || (contact == null)
				|| (dcf == null)) {
			return false;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("user.message");
		zad.setAuth(sessonid);

		DvsRequest.params_user_message pum = new DvsRequest.params_user_message();
		if (name != null) {
			pum.setNames(name);
		}
		if (contact != null) {
			pum.setContacts(contact);
		}
		if (content != null) {
			pum.setContents(content);
		}
		zad.setParams(pum);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_UserMessage rsp = (DvsResult_UserMessage) JsonUtils
					.objectFromJson(s, DvsResult_UserMessage.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsResult_UserMessage.DvsUserMessage dum = rsp.getResult();

				return (dum != null) && (dum.getMessageids()[0] != null);
			}

		}

		return false;
	}

	public static boolean userUpdate(String sessonid, String userid,
			String name, String passwd, DvsCfgVer dcf) {
		if ((userid == null) || ((name == null) && (passwd == null))
				|| (dcf == null)) {
			return false;
		}

		DvsRequest zad = new DvsRequest();
		zad.setJsonrpc("2.0");
		zad.setId(1);

		zad.setMethod("user.update");
		zad.setAuth(sessonid);

		DvsRequest.params_user_update puu = new DvsRequest.params_user_update();
		if (userid != null) {
			puu.setUserid(userid);
		}
		if (name != null) {
			puu.setName(name);
		}
		if (passwd != null) {
			puu.setPasswd(passwd);
		}
		zad.setParams(puu);

		String s = urlPostMethod(Setting.getHostAgent(),
				JsonUtils.jsonFromObject(zad));
		System.out.println(JsonUtils.jsonFromObject(zad));
		System.out.println(s);

		if (s != null) {
			DvsResult_UserUpdate rsp = (DvsResult_UserUpdate) JsonUtils
					.objectFromJson(s, DvsResult_UserUpdate.class);
			if (rsp != null) {
				dcf.setCfgver(rsp.getCfgver());

				DvsResult_UserUpdate.DvsUserUpdate duu = rsp.getResult();

				return (duu != null) && (duu.getUserids()[0] != null);
			}

		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		openLog(true);

		int local_cfgver = 1;
		DvsCfgVer cfgver = new DvsCfgVer();
		cfgver.setCfgver(local_cfgver);

		DvsUser user = userLogin("testapp", "testapp", cfgver);

		if (cfgver.getCfgver() > local_cfgver) {
			System.out.println("配置信息改变!");
		}

		if (user == null) {
			System.out.println("登录失败，请检查用户名和密码...");

			return;
		}
		String sessionID = user.getSessionid();
		if (sessionID != null) {
			System.out.println("登录成功...");

			DvsHostGroup[] groupList = hostGroupsGet(sessionID, cfgver);
			for (int i = 0; (groupList != null) && (i < groupList.length); i++) {
				System.out.println(groupList[i].getGroupid() + "@"
						+ groupList[i].getName() + "@"
						+ groupList[i].getFlags() + "@"
						+ groupList[i].getdInternal());
			}

			String[] hostGroupIDs = { "35" };
			DvsHost[] hostList = groupHostsGet(sessionID, hostGroupIDs, null,
					false, cfgver);
			for (int i = 0; (hostList != null) && (i < hostList.length); i++) {
				System.out.println(hostList[i].getHostid() + "@"
						+ hostList[i].getHost() + "@" + hostList[i].getName()
						+ "@" + hostList[i].getAvailable() + "@"
						+ hostList[i].getStatus() + "@"
						+ hostList[i].getDescription() + "@");
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			Date datetill = new Date();
			Calendar cl = Calendar.getInstance();
			cl.setTime(datetill);
			cl.add(6, -365);

			Date datefrom = cl.getTime();
			System.out.println("from " + sdf.format(datefrom) + " to "
					+ sdf.format(datetill));

			DvsEvent[] eventsList = groupEventsGet(sessionID, hostGroupIDs,
					datefrom, datetill, 100, cfgver);
			for (int i = 0; (eventsList != null) && (i < eventsList.length); i++) {
				System.out.println("events>>>" + eventsList[i].getEventid()
						+ "@" + eventsList[i].getAcknowledged() + "@"
						+ eventsList[i].getClock() + "@"
						+ eventsList[i].getNs() + "@"
						+ eventsList[i].getObject() + "@"
						+ eventsList[i].getSource());
			}

			DvsTriggerSum[] triggersSumList = groupTriggersSumGet(sessionID,
					cfgver);
			for (int i = 0; (triggersSumList != null)
					&& (i < triggersSumList.length); i++) {
				System.out.println("triggers_sum>>>"
						+ triggersSumList[i].getGroupid() + "@"
						+ triggersSumList[i].getPriority4() + "@"
						+ triggersSumList[i].getPriority2() + "@"
						+ triggersSumList[i].getPriority1());
			}

			DvsTrigger[] triggersList = groupTriggersGet(sessionID,
					hostGroupIDs, 100, cfgver);
			for (int i = 0; (triggersList != null) && (i < triggersList.length); i++) {
				System.out.println("triggers>>>"
						+ triggersList[i].getTriggerid()
						+ "@"
						+ triggersList[i].getDescription()
						+ "@"
						+ triggersList[i].getComments()
						+ "@"
						+ triggersList[i].getExpression()
						+ "@"
						+ sdf.format(new Date(
								triggersList[i].getLastchange() * 1000L)) + "@"
						+ triggersList[i].getPriority() + "@"
						+ triggersList[i].getUrl());

				String[] triggerIDs = { triggersList[i].getTriggerid() };
				DvsHost[] triggerHostList = groupHostsGet(sessionID, null,
						triggerIDs, false, cfgver);
				for (int thi = 0; (triggerHostList != null)
						&& (thi < triggerHostList.length);) {
					System.out.println("trigger_host>>>["
							+ triggersList[i].getTriggerid() + "]"
							+ triggerHostList[thi].getHostid() + "@"
							+ triggerHostList[thi].getHost() + "@"
							+ triggerHostList[thi].getAvailable() + "@"
							+ triggerHostList[thi].getStatus() + "@"
							+ triggerHostList[thi].getDescription());

					thi++;
				}

			}

			String[] hostIDs = { "10139" };
			DvsItem[] di = hostItemsGet(sessionID, hostIDs,
					"Application_RealtimeData", null, cfgver);
			for (int vlaueIndex = 0; (di != null) && (vlaueIndex < di.length); vlaueIndex++) {
				System.out.println("Application_RealtimeData>>>>>>"
						+ di[vlaueIndex].getItemid() + "@"
						+ di[vlaueIndex].getUnits() + "@"
						+ di[vlaueIndex].getValue_type());
			}

			di = hostItemsGet(sessionID, hostIDs, "Application_CtrlParams",
					null, cfgver);
			for (int vlaueIndex = 0; (di != null) && (vlaueIndex < di.length); vlaueIndex++) {
				System.out.println("Application_CtrlParams>>>>>>"
						+ di[vlaueIndex].getItemid() + "@"
						+ di[vlaueIndex].getUnits() + "@"
						+ di[vlaueIndex].getValue_type());
			}

			String[] itemIDs = { "23912", "23913", "23910" };
			DvsValue[] zv = historyDataGet(sessionID, itemIDs, 0, datefrom,
					datetill, 100, cfgver);

			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = historyDataGet(sessionID, itemIDs, 1, datefrom, datetill, 100,
					cfgver);

			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = historyDataGet(sessionID, itemIDs, 2, datefrom, datetill, 100,
					cfgver);
			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = historyDataGet(sessionID, itemIDs, 3, datefrom, datetill, 100,
					cfgver);

			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = historyDataGet(sessionID, itemIDs, 4, datefrom, datetill, 100,
					cfgver);

			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}

			}

			zv = historyDataGetLast(sessionID, itemIDs, 0, datefrom, datetill,
					null, cfgver);

			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}

			}

			int pageindex = 1;
			int pagesize = 10;
			itemIDs = new String[] { "23912" };
			zv = historyDataGetPage(sessionID, itemIDs, pageindex, pagesize, 0,
					datefrom, datetill, false, null, cfgver);
			if ((zv != null) && (zv.length > 0)) {
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000L
													+ Math.round((float) zv[valueIndex]
															.getNs() / 1000.0F / 1000.0F)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getPagecount() + "@"
									+ zv[valueIndex].getNs());
				}

			}

			if (userMessage(sessionID, "用户的名字", "18600000000", "用户上传的意见",
					cfgver))
				System.out.println("意见上传成功!");
			else {
				System.out.println("意见上传失败!");
			}

			if (userUpdate(sessionID, user.getUserid(), "改昵称不是用户名", null,
					cfgver))
				System.out.println("修改用户信息成功!");
			else {
				System.out.println("修改用户信息失败!");
			}
		} else {
			System.out.println("登录失败!");
		}
	}
}