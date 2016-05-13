package com.dvsapp.utils;

import com.treecore.utils.TEventIdUtils;

public class CoreEventUtils extends TEventIdUtils {
	public static final int Activity_Self_Destory = getNextEvent();
	public static final int Event_GetDvsTriggerSum = getNextEvent();
	public static final int Event_Monitor_Back = getNextEvent();
	public static final int Event_Monitor_Chart = getNextEvent();
	public static final int Event_Monitor_Data = getNextEvent();

	public static final int Event_DvsHost_Map = getNextEvent();
	public static final int Event_DvsHost_Host_Map = getNextEvent();
	public static final int Event_Upload_Host_GPS = getNextEvent();

	public static final int Event_GetDvsHostGroup = getNextEvent();
	public static final int Event_GetDvsHostGroup_Success = getNextEvent();
	public static final int Event_GetDvsHost_Item_Value = getNextEvent();
	public static final int Event_GetDvsHost_Item_Value_Success = getNextEvent();
	public static final int Event_GetDvsHost_Item_Value_Loading = getNextEvent();
	public static final int Event_GetDvsHost_Item_Value_Cancel = getNextEvent();
}
