package cn.just.alarmclock.activity;

import javax.security.auth.PrivateCredentialPermission;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import cn.just.alarmclock.R;

public class LocationBaiduMapActivity extends Activity implements
		OnClickListener {

	// 定位相关
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	private MyLocationConfiguration.LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;

	private LatLng clickPoint;

	private MapView mMapView;
	private BaiduMap mBaiduMap;

	private double longitudeStr;// 经度
	private double latitudeStr;// 纬度

	// UI相关
	private OnCheckedChangeListener radioButtonListener;
	private Button requestLocButton;
	private boolean isFirstLoc = true;// 是否首次定位

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_location_map);

		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
		requestLocButton.setText("普通");
		requestLocButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		});

		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.defaulticon) {
					// 传入null则，恢复默认图标
					mCurrentMarker = null;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
				if (checkedId == R.id.customicon) {
					// 修改为自定义marker
					mCurrentMarker = BitmapDescriptorFactory
							.fromResource(R.mipmap.icon_geo);
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
				}
			}
		});

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}

			@Override
			public void onMapClick(LatLng point) {
				clickPoint = point;
				int flag = 1;
				if (flag == 1) {
					showPointPosition();
					flag++;
				}
				if (flag != 1) {
					mBaiduMap.clear();
					showPointPosition();
				}
			}
		});

		Button saveBtn = (Button) findViewById(R.id.save);
		Button cancleBtn = (Button) findViewById(R.id.cancle);
		saveBtn.setOnClickListener(this);
		cancleBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			Intent intent = getIntent();
			intent.putExtra("longitude", longitudeStr);
			intent.putExtra("latitude", latitudeStr);
			setResult(1, intent);
			LocationBaiduMapActivity.this.finish();
			break;
		case R.id.cancle:
			LocationBaiduMapActivity.this.finish();
			break;

		default:
			break;
		}

	}

	private void showPointPosition() {
		// 构建Marker图标
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.mipmap.icon_geo);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(clickPoint).icon(
				bitmapDescriptor);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
		String str = null;
		if (clickPoint == null) {
			str = "点击地图可获取经纬度";
			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
		} else {

			longitudeStr = clickPoint.longitude;
			latitudeStr = clickPoint.latitude;
			str = String.format("当前经度：%f,当前纬度：%f", longitudeStr, latitudeStr);
			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = getIntent();
			intent.putExtra("longitude", longitudeStr);
			intent.putExtra("latitude", latitudeStr);
			setResult(1, intent);
			LocationBaiduMapActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

}
