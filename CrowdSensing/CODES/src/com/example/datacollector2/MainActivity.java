package com.example.datacollector2;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private boolean mIsLogStart = false;
	private boolean mIsPhoneStateStart = false;
	
	private TextView mTvInfo;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTvInfo = (TextView)findViewById(R.id.textView1);
		
//		if (isServiceRunning(LogService.class)) {
//			Log.d(TAG, "log service is already running.");
//			mIsLogStart = true;
//			mTvInfo.setText("log service is already running.\n");
//		}
		
		if (isServiceRunning(PhoneStateService.class)) {
			Log.d(TAG, "PhoneStateService service is already running.");
			mIsPhoneStateStart = true;
			mTvInfo.setText("PhoneStateService service is already running.\n");
		}
	}
	
	public void btnOnClickStartService(View view) {
		
//		if (mIsLogStart == false) {
//			Log.d(TAG, "start log service");
//			startService(new Intent(getBaseContext(), LogService.class));
//			mTvInfo.setText(mTvInfo.getText() + "start log service\n");
//			mIsLogStart = true;
//		}

		if (mIsPhoneStateStart == false) {
			Log.d(TAG, "start PhoneStateService");
			startService(new Intent(getBaseContext(), PhoneStateService.class));
			mTvInfo.setText(mTvInfo.getText() + "start PhoneStateService\n");
			mIsPhoneStateStart = true;
		}
	}

	// Stop the  service
	public void btnOnClinckStopService(View view) {
		
//		if (mIsLogStart == true) {
//			Log.d(TAG, "stop log service");
//			stopService(new Intent(getBaseContext(), LogService.class));		
//			mTvInfo.setText(mTvInfo.getText() + "stop service\n");
//			mIsLogStart = false;
//		}
		
		if (mIsPhoneStateStart == true) {
			Log.d(TAG, "stop PhoneStateService");
			stopService(new Intent(getBaseContext(), PhoneStateService.class));		
			mTvInfo.setText(mTvInfo.getText() + "stop PhoneStateService\n");
			mIsPhoneStateStart = false;
		}
	}
	
	private boolean isServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}