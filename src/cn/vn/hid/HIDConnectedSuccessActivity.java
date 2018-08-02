package cn.vn.hid;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HIDConnectedSuccessActivity extends Activity{
	
	String mDeviceName;
	String mDeviceAdresss;
	private static final int MSG_FINISH = 0x10;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_FINISH:
				finish();
				break;
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDeviceName = getIntent().getStringExtra("name");
		mDeviceAdresss = getIntent().getStringExtra("address");
		String msg = getResources().getString(R.string.hid_connected_msg, mDeviceName, mDeviceAdresss);
		setTitle(msg);
		Message m = Message.obtain(mHandler, MSG_FINISH);
		mHandler.sendMessageDelayed(m, 5000);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		AlertDialog.Builder buidler = new AlertDialog.Builder(this);
		buidler.setTitle(R.string.hid_connected_title);
		String msg = getResources().getString(R.string.hid_connected_msg, mDeviceName, mDeviceAdresss);
		buidler.setMessage(msg);
		buidler.setCancelable(true);
//		buidler.show();
	}
}
