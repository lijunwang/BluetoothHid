package cn.vn.hid;

import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

public class HIDAutoConnectedService extends Service implements AutoConnectListener{
	public static final String TAG = "HIDAutoConnectedService";
	private BluetoothAdapter mBluetoothAdapter;
	private BlueBroadcastReceiver mBroadcastReceiver = new BlueBroadcastReceiver();
	private IntentFilter mIntentFilter = new IntentFilter();
	private static String HID_NAME = "KMSWand"; // 连接的蓝牙设备名
	private static String HID_ADDR = "00:E0:4C:00:04:D6"; // 连接的蓝牙设备地址
	
	public static boolean filterHIDDevice(BluetoothDevice device){
		String name = device.getName();
		String address = device.getAddress();
		Log.d(TAG, "filterHIDDevice ... " + name + "," + address);
		if(device != null && HID_NAME.equals(name) && HID_ADDR.equals(address)){
			return true;
		}else{
			return false;
		}
	}
	private boolean mHasFoundHID = false;
	private boolean mReconneted = false;
	private final boolean fixBugForTwiceConnected = false;
	private HidUtil mHidUtil;
	protected static final String HIDCONNECTED = "HIDConnected";
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	private static final int MSG_REDISCOVERY = 0x10;
	private static long mStartDiscoveryTime = 0;
	//扫描时长
	private static final int mDiscoveryTotalTime = 5 * 60 * 1000;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			Log.d(TAG, "handleMessage ... " + msg.what);
			switch (msg.what) {
			case MSG_REDISCOVERY:
				mBluetoothAdapter.startDiscovery();
				break;
			default:
				break;
			}
		};
	};
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate ... ");
		mHidUtil = HidUtil.getInstance(this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			Log.e(TAG, "onCreate ... mBluetoothAdapter == null ");
			stopSelf();
		}
		// 如果没有打开蓝牙
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		mStartDiscoveryTime = System.currentTimeMillis();
		mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mIntentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
		registerReceiver(mBroadcastReceiver, mIntentFilter);
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand ... ");
		mStartDiscoveryTime = System.currentTimeMillis();
		mHasFoundHID = false;
		mHandler.sendEmptyMessage(MSG_REDISCOVERY);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy ... ");
		if(mBroadcastReceiver != null){
			unregisterReceiver(mBroadcastReceiver);
		}
		
	}
	
	private class BlueBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive scan and connect:" + action);
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// 通过广播接收到了BluetoothDevice
				BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null){
					return;
				}
				String name = device.getName();
				String address = device.getAddress();
				int state = device.getBondState();
				Log.i(TAG, "bluetooth ww name:" + name + ",address:" + address + "," + state);
				if(HID_NAME.equals(name) && HID_ADDR.equals(address)){
					Log.d(TAG, "found device and cancel discovery");
					//notify user that we are
					onStartConnected(device);
					Intent usb = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
					usb.putExtra("autoConnect", true);
					usb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            startActivity(usb);
					mHasFoundHID = true;
					if (!mHidUtil.isBonded(device)) {
						mHidUtil.pair(device);
					} else {
						mHidUtil.connect(device);
					}
				}
			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = device.getName();
				String address = device.getAddress();
				Log.i(TAG, "ACTION_BOND_STATE_CHANGED name:" + name + ",address:" + address + ",bondstate:" + device.getBondState());
				Log.i(TAG, "ACTION_BOND_STATE_CHANGED name ww:" + (device != null && HID_NAME.equals(name) && HID_ADDR.equals(address) && device.getBondState() == BluetoothDevice.BOND_BONDED));
				if(filterHIDDevice(device) && device.getBondState() == BluetoothDevice.BOND_BONDING){
					onPairing(device);
				}else if(filterHIDDevice(device) && device.getBondState() == BluetoothDevice.BOND_BONDED){
					onPaired(device);
				}
				if(filterHIDDevice(device) && device.getBondState() == BluetoothDevice.BOND_BONDED){
					boolean success = mHidUtil.connect(device);
//					wt02 failed at here
					Log.i(TAG, "ACTION_BOND_STATE_CHANGED name tt goto connect :" + success);
					/*Intent usb = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
					usb.putExtra("autoConnect", true);
					usb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            startActivity(usb);*/
				}
			} else if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
				int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i(TAG, "CONNECTION_STATE_CHANGED 22 state=" + state + ",device=" + device);
				if (filterHIDDevice(device) && state == BluetoothProfile.STATE_CONNECTED) {
					Log.d(TAG, "profile STATE_CONNECTED" + mReconneted);
//					stopSelf();
					//断开一次连接
					if(!mReconneted && fixBugForTwiceConnected){
						Log.d(TAG, "profile STATE_CONNECTED  and disconnect it ... ");
						mHidUtil.disConnect(device);
					}else{
						//第二次连接  一般可以成功，此时保存
						boolean save = android.provider.Settings.System.putInt(getContentResolver(), HIDCONNECTED, 1);
						Log.d(TAG, "profile STATE_CONNECTED really ok 22... ");
//						onConnectedSuccess(device);
						unregisterReceiver(mBroadcastReceiver);
						mBroadcastReceiver = null;
						stopSelf();
						Intent success = new Intent(HIDAutoConnectedService.this, HIDConnectedSuccessActivity.class);
						success.putExtra("name", device.getName());
						success.putExtra("address", device.getAddress());
						success.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(success);
					}
				} else if (filterHIDDevice(device) && state == BluetoothProfile.STATE_DISCONNECTED) {
					boolean reconncet = mHidUtil.connect(device);
					mReconneted = true;
					Log.d(TAG, "profile STATE_DISCONNECTED " + reconncet);
					/*if(reconncet){
						
					}*/
				}
//开始扫描后5分钟内没有扫描到就不再扫描				
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !mHasFoundHID && 
					(System.currentTimeMillis() - mStartDiscoveryTime <= mDiscoveryTotalTime)){
				Message.obtain(mHandler, MSG_REDISCOVERY).sendToTarget();
			}else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) && (System.currentTimeMillis() - mStartDiscoveryTime <= mDiscoveryTotalTime)){
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
				Log.d(TAG, "nani change ww.... " + state + "," + preState);
				if(state == BluetoothAdapter.STATE_OFF){
					Log.d(TAG, "nani change to enable.... ");
					mBluetoothAdapter.enable();
				}else if(state == BluetoothAdapter.STATE_ON){
					Log.d(TAG, "nani change to start MSG_REDISCOVERY.... ");
					mHandler.sendEmptyMessage(MSG_REDISCOVERY);
				}
			}
		}
	}

	//used to update UI
	@Override
	public void onStartConnected(BluetoothDevice device) {
		Log.d(TAG, "onStateConnected ...");
	}


	@Override
	public void onPairing(BluetoothDevice device) {
		Log.d(TAG, "onPairing ...");
	}


	@Override
	public void onPaired(BluetoothDevice device) {
		Log.d(TAG, "onPaired ...");
	}


	@Override
	public void onConnectedSuccess(BluetoothDevice device) {
		Log.d(TAG, "onConnectedSuccess ...");
	}


	@Override
	public void onConnectedFailed(BluetoothDevice device) {
		Log.d(TAG, "onConnectedFailed ...");
	}
	
}
