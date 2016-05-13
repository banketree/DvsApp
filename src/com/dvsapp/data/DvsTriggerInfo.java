package com.dvsapp.data;

import java.util.ArrayList;
import java.util.List;

import com.dvs.appjson.DvsHost;
import com.dvs.appjson.DvsTrigger;

public class DvsTriggerInfo {
	private DvsTrigger mDvsTrigger;
	private List<DvsHost> mDvsHosts = new ArrayList<>();

	public DvsTriggerInfo(DvsTrigger dvsTrigger, DvsHost[] dvsHosts) {
		mDvsTrigger = dvsTrigger;

		if (dvsHosts != null) {
			for (int i = 0; i < dvsHosts.length; i++) {
				mDvsHosts.add(dvsHosts[i]);
			}
		}
	}

	public DvsTrigger getDvsTrigger() {
		return mDvsTrigger;
	}

	public List<DvsHost> getDvsHosts() {
		return mDvsHosts;
	}
}
