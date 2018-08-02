package cn.vn.hid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(HIDAutoConnectedService.TAG, "BootReceiver ... " + intent.getAction());
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			if(System.getInt(context.getContentResolver(), HIDAutoConnectedService.HIDCONNECTED, 0) != 1){
				Log.d(HIDAutoConnectedService.TAG, "BootReceiver 22 to start service... ");
				Intent service = new Intent(context, HIDAutoConnectedService.class);
				context.startService(service);
			}
			
			/*Intent ac = new Intent(context, MainActivity.class);
			ac.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(ac);*/
		}
	}

}
