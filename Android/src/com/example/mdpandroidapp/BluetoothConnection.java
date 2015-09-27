package com.example.mdpandroidapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothConnection extends Activity {

	//for logging purpose
	private String logMessage = "";
	private TextView debugView;

	//to change button text and color
	private Button listDeviceButton;
	private Button connnectAsClientButton;
	private TextView connectionStatusText;
	private Button connectAsServerButton;

	//for connection to BluetoothService
	private boolean bound = false;
	private BluetoothService bluetoothService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_communication);
		debugView = (TextView)findViewById(R.id.text_debug);
		
		listDeviceButton = (Button)findViewById(R.id.button_list_device);
		connnectAsClientButton = (Button)findViewById(R.id.button_connect_client);
		connectionStatusText = (TextView)findViewById(R.id.text_connection_status);
		connectAsServerButton = (Button)findViewById(R.id.button_connect_server);
		
		//Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothService.EVENT_DEVICE_LIST_UPDATED);
		filter.addAction(BluetoothService.EVENT_STATE_NONE);
		filter.addAction(BluetoothService.EVENT_STATE_LISTEN);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTING);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTED);
		filter.addAction(BluetoothService.EVENT_MESSAGE_RECEIVED);
		registerReceiver(mReceiver, filter);
		
		//Create listener for ListView
		AdapterView.OnItemClickListener itemClickListener = new 
			AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> ListView, View
				itemView, int position, long id){

				String deviceInfo = (String)ListView.getItemAtPosition(position);
				String[] deviceData = deviceInfo.split("\n");
				bluetoothService.setDeviceInfo(deviceData[0], deviceData[1]);
				logMsg(String.format("Select name: %s, MAC: %s", 
						deviceData[0], deviceData[1]));
				singleDeviceListView(deviceData[0], deviceData[1]);
			}
		};

		//Add the listener to the ListView
		ListView listView = (ListView)findViewById(R.id.list_devices);
		listView.setOnItemClickListener(itemClickListener);
	}
	
	private void singleDeviceListView(String deviceName, String MACAddress){
		ArrayList<String> singleDevice = new ArrayList<String>();
		singleDevice.add(deviceName + "\n" + MACAddress);
		setDeviceListView(singleDevice);
	}
	
	/*Activity LifeCycle
	 * 
	 */
	
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
		if (bluetoothService == null){
			return;
		}
		int mState = bluetoothService.getState();
		switch(mState){
			case BluetoothService.STATE_NONE:
				logMsg("Bluetooth State None");
				setConnectStatus("Bluetooth State None");
				break;
			case BluetoothService.STATE_LISTEN:
				logMsg("Bluetooth State Listening");
				setConnectStatus("Bluetooth State Listening");
				break;
			case BluetoothService.STATE_CONNECTING:
				logMsg("Bluetooth State Connecting");
				setConnectStatus("Bluetooth State Connecting");
				break;
			case BluetoothService.STATE_CONNECTED:
				logMsg("Bluetooth State Connected");
				setConnectStatus("Bluetooth State Connected");
				break;
			default:
				break;
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		try{
			unregisterReceiver(mReceiver);
		}catch (IllegalArgumentException e){
		}
		if (bound){
			unbindService(connection);
			bound = false;
		}
	}

	/*Handle button's onClick event
	 * 
	 */
	
	public void onTurnOnBtnClick(View view){
		Intent turnonBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.startActivity(turnonBTIntent);
	}
	
	public void onTurnOffBtnClick(View view){
		bluetoothService.turnOffBT();
	}
	
	public void onConnectServerBtnClick(View view){
		bluetoothService.connectAsServer();
	}
	
	public void onListDeviceBtnClick(View view){
		bluetoothService.performDiscovery();
	}
	
	public void onConnectClientBtnClick(View view){
		bluetoothService.connectAsClient();
	}
	
	public void onDeviceStringCommClick(View view){
		bluetoothService.write("apple");
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothService.EVENT_DEVICE_LIST_UPDATED.equals(action)){
				ArrayList<String> arrayList = bluetoothService.getDeviceList();
				setDeviceListView(arrayList);
			}else if(BluetoothService.EVENT_STATE_NONE.equals(action)){
				logMsg("Bluetooth State None");
				setConnectStatus("Bluetooth State None");
			}else if(BluetoothService.EVENT_STATE_LISTEN.equals(action)){
				logMsg("Bluetooth State Listening");
				setConnectStatus("Bluetooth State Listening");
			}else if(BluetoothService.EVENT_STATE_CONNECTING.equals(action)){
				logMsg("Bluetooth State Connecting");
				setConnectStatus("Bluetooth State Connecting");
			}else if(BluetoothService.EVENT_STATE_CONNECTED.equals(action)){
				logMsg("Bluetooth State Connected");
				setConnectStatus("Bluetooth State Connected");
			}else if(BluetoothService.EVENT_MESSAGE_RECEIVED.equals(action)){
				logMsg("Bluetooth received message: " + bluetoothService.getReceivedMsg());
			}
		}
	};
	
	private void setDeviceListView(ArrayList<String> arrayList){
		ArrayAdapter mArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, 
			arrayList);
		ListView deviceList = (ListView)findViewById(R.id.list_devices);
		deviceList.setAdapter(mArrayAdapter);
	}
	
	private void logMsg(String message){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		logMessage += dateFormat.format(date)+ ": " + message + "\n";
		debugView.setText(logMessage);
	}
	
	private void setConnectStatus(String message){
		connectionStatusText.setText("Connection Status: "+message);
	}
	
	private void showToast(String str){
		int duration = Toast.LENGTH_SHORT;
		CharSequence text = str;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}
	
	private ServiceConnection connection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder){
			BluetoothService.BluetoothBinder bluetoothBinder =
				(BluetoothService.BluetoothBinder) binder;
			bluetoothService = bluetoothBinder.getBluetooth();
			bound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName name){
			bound = false;
		}
	};
}
