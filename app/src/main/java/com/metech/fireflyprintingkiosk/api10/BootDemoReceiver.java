package com.metech.fireflyprintingkiosk.api10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Simple receiver that will handle the boot completed intent and send the
 * intent to launch the BootDemoService.
 * 
 * @author BMB
 * 
 */
public class BootDemoReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent bootintent) {
		Intent i = new Intent();
	    i.setClassName("com.metech.fireflyprintingkiosk.api10", "com.metech.fireflyprintingkiosk.api10.MainActivity");
	    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(i);
	}
}