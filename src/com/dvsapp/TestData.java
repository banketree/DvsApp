package com.dvsapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.dvs.appjson.DvsAPI2;
import com.dvs.appjson.DvsCfgVer;
import com.dvs.appjson.DvsEvent;
import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsHostGroup;
import com.dvs.appjson.DvsItem;
import com.dvs.appjson.DvsTrigger;
import com.dvs.appjson.DvsTriggerSum;
import com.dvs.appjson.DvsUser;
import com.dvs.appjson.DvsValue;
import com.treecore.utils.TTimeUtils;

public class TestData {

	public static void main() throws Exception {
		// 打开日志输出
		DvsAPI2.openLog(true);

		// 模拟保存在本地的配置信息版本号
		int local_cfgver = 1;
		DvsCfgVer cfgver = new DvsCfgVer();
		cfgver.setCfgver(local_cfgver);

		DvsUser user = DvsAPI2.userLogin("testapp", "testapp", cfgver);

		// 测试配置信息版本号更改
		// 特别注意：
		// 1、这个配置信息号改变的判断需要在每个返回信息后进行并提示刷新配置
		// 2、配置信息号第一次读取或者本地刷新成功后需要存到本地，以便与每次返回时的配置号做比较
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

			// 获取该用户名下的所有主机组信息
			DvsHostGroup[] groupList = DvsAPI2.hostGroupsGet(sessionID, cfgver);
			for (int i = 0; groupList != null && i < groupList.length; i++) {

				System.out.println(groupList[i].getGroupid() + "@"
						+ groupList[i].getName()
						+ "@"// 主机组名称
						+ groupList[i].getFlags() + "@"
						+ groupList[i].getdInternal());
			}

			// 修改主机组名称
			if (DvsAPI2.hostGroupUpdate(sessionID, groupList[0].getGroupid(),
					"群组名称修改", cfgver)) {
				System.out.println("群组名称修改成功!");
			} else {
				System.out.println("群组名称修改失败!");
			}

			// 获取某个主机组下的所有主机信息
			String[] hostGroupIDs = new String[] { "31" };
			DvsHost[] hostList = DvsAPI2.groupHostsGet(sessionID, hostGroupIDs,
					null, true, cfgver);
			for (int i = 0; hostList != null && i < hostList.length; i++) {

				System.out.println(hostList[i].getHostid()
						+ "@"
						+ hostList[i].getHost()
						+ "@" // 主机名 关键字名称
						+ hostList[i].getName()// 主机组别名
						+ "@" + hostList[i].getAvailable() + "@"
						+ hostList[i].getStatus() + "@"
						+ hostList[i].getDescription() + "@"
						+ hostList[i].getInventory().getLocation_lat() + "@" // 纬度
						+ hostList[i].getInventory().getLocation_lon()); // 经度
			}

			// 修改主机名称(暂时没用)
			if (DvsAPI2.hostUpdate(sessionID, "10141", "主机名称修改", cfgver)) {
				System.out.println("主机名称修改 成功!");
			} else {
				System.out.println("主机名称修改 失败!");
			}

			// 修改主机的经纬度
			if (DvsAPI2.hostLocation(sessionID, "10141", "100.55", "55.21",
					cfgver)) {
				System.out.println("经纬度修改成功!");
			} else {
				System.out.println("经纬度修改失败!");
			}

			// 根据数据类型和时间区间获取某个主机的历史数据信息
			// 这里已经直接返回了itemid和对应的数据值

			// 计算时间区间
			// java.util.Date代表一个时间点，其值为距公元1970年1月1日
			// 00:00:00的毫秒数。所以它是没有时区和Locale概念的
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			Date datetill = new Date();
			Calendar cl = Calendar.getInstance();
			cl.setTime(datetill);
			cl.add(Calendar.DAY_OF_YEAR, -365); // 前一天
			// cl.add(Calendar.WEEK_OF_YEAR, -1); //前一周
			// cl.add(Calendar.MONTH, -1); //前一个月
			Date datefrom = cl.getTime();
			System.out.println("from " + sdf.format(datefrom) + " to "
					+ sdf.format(datetill));

			// 获取主机组下所有事件信息(暂时没用)
			DvsEvent[] eventsList = DvsAPI2.groupEventsGet(sessionID,
					hostGroupIDs, datefrom, datetill, 100, cfgver);
			for (int i = 0; eventsList != null && i < eventsList.length; i++) {

				System.out.println("events>>>" + eventsList[i].getEventid()
						+ "@" + eventsList[i].getAcknowledged() + "@"
						+ eventsList[i].getClock()
						+ "@"// 时间第一部分
						+ eventsList[i].getNs()
						+ "@"// 时间第二部分
						+ eventsList[i].getObject() + "@"
						+ eventsList[i].getSource());
			}

			// 获取所有主机组下所有触发器的数量
			DvsTriggerSum[] triggersSumList = DvsAPI2.groupTriggersSumGet(
					sessionID, cfgver);
			for (int i = 0; triggersSumList != null
					&& i < triggersSumList.length; i++) {
				System.out.println("triggers_sum>>>"
						+ triggersSumList[i].getGroupid() + "@"
						+ triggersSumList[i].getPriority4() + "@"
						+ triggersSumList[i].getPriority2() + "@"
						+ triggersSumList[i].getPriority1());
			}

			// 获取主机组下所有触发器信息：时间区间置null,null
			DvsTrigger[] triggersList = DvsAPI2.groupTriggersGet(sessionID,
					hostGroupIDs, 100, cfgver);
			for (int i = 0; triggersList != null && i < triggersList.length; i++) {

				System.out.println("triggers>>>"
						+ triggersList[i].getTriggerid()
						+ "@"
						+ triggersList[i].getDescription()// 事件名称
						+ "@"
						+ triggersList[i].getComments()// 事件附加描述
						+ "@"
						+ triggersList[i].getExpression()// 事件简要描述
						+ "@"
						+ sdf.format(new Date(
								triggersList[i].getLastchange() * 1000)) + "@"
						+ triggersList[i].getPriority() + "@"// 触发器严重级别：1，一般；2，警告；4，严重；
						+ triggersList[i].getUrl());

				// 获取触发器对应的主机ID
				String[] triggerIDs = new String[] { triggersList[i]
						.getTriggerid() };
				DvsHost[] triggerHostList = DvsAPI2.groupHostsGet(sessionID,
						null, triggerIDs, false, cfgver);
				for (int thi = 0; triggerHostList != null
						&& thi < triggerHostList.length; thi++) {

					System.out.println("trigger_host>>>["
							+ triggersList[i].getTriggerid() + "]"
							+ triggerHostList[thi].getHostid() + "@"
							+ triggerHostList[thi].getHost() + "@"
							+ triggerHostList[thi].getAvailable() + "@"
							+ triggerHostList[thi].getStatus() + "@"
							+ triggerHostList[thi].getDescription());
				}
			}

			// 获取主机的所有Application_RealtimeData数据项
			String[] hostIDs = new String[] { "10139" };
			DvsItem[] di = DvsAPI2.hostItemsGet(sessionID, hostIDs,
					"Application_RealtimeData", null, cfgver);
			for (int vlaueIndex = 0; di != null && vlaueIndex < di.length; vlaueIndex++) {

				System.out.println("Application_RealtimeData>>>>>>"
						+ di[vlaueIndex].getItemid() + "@"
						+ di[vlaueIndex].getUnits()// 数据项单位
						+ "@" + di[vlaueIndex].getValue_type());

				// 控制项
			}

			// 获取主机的所有Application_CtrlParams数据项
			di = DvsAPI2.hostItemsGet(sessionID, hostIDs,
					"Application_CtrlParams", null, cfgver);
			for (int vlaueIndex = 0; di != null && vlaueIndex < di.length; vlaueIndex++) {

				System.out.println("Application_CtrlParams>>>>>>"
						+ di[vlaueIndex].getItemid() + "@"
						+ di[vlaueIndex].getUnits()// 数据项单位
						+ "@" + di[vlaueIndex].getValue_type());
			}

			// 注意：这里的数据类型必须正确，否则异常
			String[] itemIDs = new String[] { "23912", "23913", "23910" };
			DvsValue[] zv = DvsAPI2.historyDataGet(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_FLOAT, datefrom, datetill, 100,
					cfgver);
			// System.out.println("host>>>HISTORY_DATATYPE_FLOAT" + hostIDs[0]);
			if (zv != null && zv.length > 0) {

				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = DvsAPI2.historyDataGet(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_STRING, datefrom, datetill, 100,
					cfgver);
			// System.out.println("host>>>HISTORY_DATATYPE_STRING" +
			// hostIDs[0]);
			if (zv != null && zv.length > 0) {

				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = DvsAPI2.historyDataGet(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_LOG, datefrom, datetill, 100,
					cfgver);
			if (zv != null && zv.length > 0) {
				// System.out.println("host>>>HISTORY_DATATYPE_LOG" +
				// hostIDs[0]);
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = DvsAPI2.historyDataGet(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_INTEGER, datefrom, datetill, 100,
					cfgver);
			// System.out.println("host>>>HISTORY_DATATYPE_INTEGER" +
			// hostIDs[0]);
			if (zv != null && zv.length > 0) {

				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			zv = DvsAPI2.historyDataGet(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_TEXT, datefrom, datetill, 100,
					cfgver);
			// System.out.println("host>>>HISTORY_DATATYPE_TEXT" + hostIDs[0]);
			if (zv != null && zv.length > 0) {

				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println(zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			// 获取最新数据(每个itemIDs)
			zv = DvsAPI2.historyDataGetLast(sessionID, itemIDs,
					DvsAPI2.HISTORY_DATATYPE_FLOAT, datefrom, datetill, null,
					cfgver);
			// System.out.println("host>>>HISTORY_DATATYPE_FLOAT" + hostIDs[0]);
			if (zv != null && zv.length > 0) {

				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()
									+ "@"
									+ sdf.format(new Date(
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() + "@"
									+ zv[valueIndex].getNs());
				}
			}

			// 获取页面数据(某个itemID)
			int pageindex = 1;// 必须大于1
			int pagesize = 10;
			itemIDs = new String[] { "23912" };
			zv = DvsAPI2.historyDataGetPage(sessionID, itemIDs, pageindex,
					pagesize, DvsAPI2.HISTORY_DATATYPE_FLOAT, datefrom,
					datetill, false, null, cfgver);
			if (zv != null && zv.length > 0) {
				// System.out.println("host>>>HISTORY_DATATYPE_FLOAT" +
				// hostIDs[0]);
				for (int valueIndex = 0; valueIndex < zv.length; valueIndex++) {

					// 注意：这里的时间精确到秒级：clock是秒级，NS是ns级
					System.out
							.println("historyDataGet>>>>>>"
									+ zv[valueIndex].getItemid()// itemID
									+ "@"
									+ sdf.format(new Date(
											// 时间
											zv[valueIndex].getClock()
													* 1000
													+ Math.round(zv[valueIndex]
															.getNs() / 1000f / 1000f)))
									+ "@" + zv[valueIndex].getValue() // 数据值
									+ "@" + zv[valueIndex].getPagecount() // 分页
									+ "@" + zv[valueIndex].getNs()); // 不必采用，上面时间已经包含
				}
			}

			// 用户上传意见和建议
			if (DvsAPI2.userMessage(sessionID, "用户的名字", "18600000000",
					"用户上传的意见", cfgver)) {
				System.out.println("意见上传成功!");
			} else {
				System.out.println("意见上传失败!");
			}

			// 用户更改自己的昵称和密码
			if (DvsAPI2.userUpdate(sessionID, user.getUserid(), "改昵称不是用户名",
					null, cfgver)) {
				System.out.println("修改用户信息成功!");
			} else {
				System.out.println("修改用户信息失败!");
			}

		} else {

			System.out.println("登录失败!");
		}
	}
}
