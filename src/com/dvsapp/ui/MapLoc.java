package com.dvsapp.ui;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.dvs.appjson.DvsAPI2;
import com.dvsapp.wisdom.R;
import com.dvsapp.data.Account;
import com.dvsapp.data.Setting;
import com.dvsapp.ui.fragment.RealTime;
import com.dvsapp.utils.CoreEventUtils;
import com.treecore.TBroadcastByInner;
import com.treecore.dialog.TDialogManager;
import com.treecore.utils.TActivityUtils;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.TToastUtils;
import com.treecore.utils.network.TNetWorkUtil;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

//地图定位
public class MapLoc extends BaseActivity {
	MapView mMapView;
	BaiduMap mBaiduMap;
	private Marker mMarkerA;
	BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.ac8);
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner mListener = new MyLocationListenner();
	double mLat = 39.897445;
	double mLang = 116.331398;

	private TTask mUploadHostGPSTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maploc);

		mMapView = (MapView) findViewById(R.id.MapView_baidu);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				LocationMode.FOLLOWING, true, null));

		mBaiduMap.getUiSettings();
		mBaiduMap.setMyLocationEnabled(true);

		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().zoom(5).build()));

		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(mListener);
	}

	@Override
	public void processEventByInner(Intent intent) {
		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, 0);
		int event = intent.getIntExtra(TBroadcastByInner.EVENT, 0);

		if (mainEvent == CoreEventUtils.Event_DvsHost_Host_Map) {
			if (RealTime.clickDvsHost == null)
				return;

			// 纬度+经度
			if (RealTime.clickDvsHost.getInventory() == null
					|| TStringUtils.isEmpty(RealTime.clickDvsHost
							.getInventory().getLocation_lat())
					|| TStringUtils.isEmpty(RealTime.clickDvsHost
							.getInventory().getLocation_lon())) {// 定位+上传位置信息
				mLocClient.start();
			} else {// 定位
				double lat = TStringUtils.toDouble(RealTime.clickDvsHost
						.getInventory().getLocation_lat());
				double lon = TStringUtils.toDouble(RealTime.clickDvsHost
						.getInventory().getLocation_lon());
				addOverlay(new LatLng(lat, lon));
			}
		} else if (mainEvent == CoreEventUtils.Event_Upload_Host_GPS) {
			showLoc();
		}
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mUploadHostGPSTask != null && mUploadHostGPSTask.equalTask(task)) {
			if (event == TaskEvent.Before) {
				TDialogManager.showProgressDialog(mContext, "坐标", "上传主机坐标中…",
						false);
			} else if (event == TaskEvent.Cancel) {
				TDialogManager.hideProgressDialog(mContext);

				if (!TStringUtils.isEmpty(task.getError())) {
					makeText(task.getError());
				}

				if (task.getResultObject() != null
						&& !TStringUtils.isEmpty((String) task
								.getResultObject())) {
					makeText((String) task.getResultObject());
				}
			} else if (event == TaskEvent.Work) {
				try {
					// 修改主机的经纬度
					if (DvsAPI2.hostLocation(Account.getInstance()
							.getSessionid(), RealTime.clickDvsHost.getHostid(),
							mLat + "", mLang + "", Setting.getDvsCfgVer())) {
						task.setResultObject("经纬度修改成功!");
					} else {
						task.setResultObject("经纬度修改失败!");
					}
				} catch (Exception e) {
					task.setError("提示：获取数据失败，请检查网络或重试");
				}
			}
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mLocClient != null)
			mLocClient.stop();
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();

		if (mUploadHostGPSTask != null)
			mUploadHostGPSTask.stopTask();
		mUploadHostGPSTask = null;
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null || mBaiduMap == null)
				return;
			// MyLocationData locData = new MyLocationData.Builder()
			// .accuracy(location.getRadius()).direction(100)
			// .latitude(location.getLatitude())
			// .longitude(location.getLongitude()).build();
			// mBaiduMap.setMyLocationData(locData);

			mLat = location.getLatitude();
			mLang = location.getLongitude();
			mLocClient.stop();

			addOverlay(new LatLng(mLat, mLang));
			// 上传位置信息
			if (RealTime.clickDvsHost != null)
				TBroadcastByInner
						.sentEvent(CoreEventUtils.Event_Upload_Host_GPS);
		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void addOverlay(LatLng point) {
		mBaiduMap.clear();

		OverlayOptions option = new MarkerOptions().position(point).icon(bdA);
		mMarkerA = (Marker) mBaiduMap.addOverlay(option);

		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.animateMapStatus(u);
	}

	private void uploadHostGPSTask() {
		if (!TNetWorkUtil.isNetworkConnected()) {
			TToastUtils.makeText("网络未连接，请检查网络是否连接正常！");
			return;
		}

		if (mUploadHostGPSTask == null) {
			mUploadHostGPSTask = new TTask();
		}
		mUploadHostGPSTask.setIXTaskListener(this);
		mUploadHostGPSTask.stopTask();
		mUploadHostGPSTask.startTask("");
	}

	private void showLoc() {
		if (RealTime.clickDvsHost == null)
			return;

		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage("为主机-" + RealTime.clickDvsHost.getName() + "添加地图定位？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				uploadHostGPSTask();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}