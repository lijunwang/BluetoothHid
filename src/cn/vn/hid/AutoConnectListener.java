package cn.vn.hid;

import android.bluetooth.BluetoothDevice;


public interface AutoConnectListener {
	public void onStartConnected(BluetoothDevice device);
	public void onPairing(BluetoothDevice device);
	public void onPaired(BluetoothDevice device);
	public void onConnectedSuccess(BluetoothDevice device);
	public void onConnectedFailed(BluetoothDevice device);
}
