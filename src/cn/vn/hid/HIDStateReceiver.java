package cn.vn.hid;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HIDStateReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(HIDAutoConnectedService.TAG, "onReceive ... " + intent.getAction());
		if("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction())){
			
		}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String name = device.getName();
			String address = device.getAddress();
			Log.d(HIDAutoConnectedService.TAG, "ACTION_BOND_STATE_CHANGED ... " + name + "," + address + "," + device.getBondState());
			if(HIDAutoConnectedService.filterHIDDevice(device) && device.getBondState() == BluetoothDevice.BOND_NONE){
				//tall up we need connected it next reboot;
				boolean save = android.provider.Settings.System.putInt(context.getContentResolver(), HIDAutoConnectedService.HIDCONNECTED, 0);
			}
		}
		
	}

}
