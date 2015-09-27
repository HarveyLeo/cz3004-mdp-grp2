package com.example.mdpandroidapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class MainActivity extends Activity{

	//for connection to BluetoothService
	private boolean bound = false;
	private BluetoothService bluetoothService;
	
	/*Activity LifeCycle
	 * 
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		//Bind this activity to BluetoothService
		Intent intent = new Intent(this, BluetoothService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if (bluetoothService!= null)
			bluetoothService.startToast();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		//bluetoothService.stopToast();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		//bluetoothService.stopToast();
		if (bound){
			unbindService(connection);
			bound = false;
		}
	}
	
	/*Handle Button Click
	 * 
	 */
	
	public void onManageBluetoothConnectionClick(View view){
		Intent intent = new Intent(this, BluetoothConnection.class);
		startActivity(intent);
	}
	
	public void onManageBluetoothCommunicationClick(View view){
		Intent intent = new Intent(this, BluetoothCommunication.class);
		startActivity(intent);
	}
	
	public void onViewMapUIClick(View view){
		
	}
	
	public void onExitClick(View view){
		bluetoothService.stopToast();
		this.onStop();
		this.finish();
	}
	
	private ServiceConnection connection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder){
			BluetoothService.BluetoothBinder bluetoothBinder =
				(BluetoothService.BluetoothBinder) binder;
			bluetoothService = bluetoothBinder.getBluetooth();
			bound = true;
			bluetoothService.startToast();
		}
		@Override
		public void onServiceDisconnected(ComponentName name){
			bound = false;
		}
	};
	
}
