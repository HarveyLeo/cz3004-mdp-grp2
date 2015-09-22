package com.example.mdpandroidapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothCommunication extends Activity {

	//for logging purpose
	private String logMessage = "";
	private TextView debugView;

	//to change button text and color
	private Button listDeviceButton;
	private Button connnectAsClientButton;
	private TextView connectionStatusText;
	private Button connectAsServerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_communication);
		debugView = (TextView)findViewById(R.id.text_debug);
		
		listDeviceButton = (Button)findViewById(R.id.button_list_device);
		connnectAsClientButton = (Button)findViewById(R.id.button_connect_client);
		connectionStatusText = (TextView)findViewById(R.id.text_connection_status);
		connectAsServerButton = (Button)findViewById(R.id.button_connect_server);
		
		BluetoothMgr.getInstance().setContext(getApplicationContext());
		BluetoothMgr.getInstance().setHandler(new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch(msg.what){
				case BluetoothMgr.MESSAGE_READ:
					showToast("Message received");
					String receivedMessage = new String((byte[])msg.obj);
					logMsg("Message received: "+receivedMessage);
					break;
				case BluetoothMgr.MESSAGE_SENT:
					showToast("Message sent");
					String sentMessage = new String((byte[])msg.obj);
					logMsg("Message sent: "+sentMessage);
					break;
				default:
					break;
				}
			}
		});
		
		//Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothMgr.EVENT_NO_BLUETOOTH_DEVICE_SELECTED);
		filter.addAction(BluetoothMgr.EVENT_BLUETOOTH_NOT_ENABLED);
		filter.addAction(BluetoothMgr.EVENT_BLUETOOTH_NOT_SUPPORTED);
		filter.addAction(BluetoothMgr.EVENT_DEVICE_LIST_UPDATED);
		filter.addAction(BluetoothMgr.EVENT_BLUETOOTH_CONNECTION_FAILED);
		filter.addAction(BluetoothMgr.EVENT_DEVICE_NOT_CONNECTED);
		filter.addAction(BluetoothMgr.EVENT_STATE_NONE);
		filter.addAction(BluetoothMgr.EVENT_STATE_LISTEN);
		filter.addAction(BluetoothMgr.EVENT_STATE_CONNECTING);
		filter.addAction(BluetoothMgr.EVENT_STATE_CONNECTED);
		
		registerReceiver(mReceiver, filter);
		
		//Create listener for ListView
		AdapterView.OnItemClickListener itemClickListener = new 
			AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> ListView, View
				itemView, int position, long id){

				String deviceInfo = (String)ListView.getItemAtPosition(position);
				String[] deviceData = deviceInfo.split("\n");
				BluetoothMgr.getInstance().setDeviceInfo(deviceData[0], 
					deviceData[1]);
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
		ArrayList singleDevice = new ArrayList();
		singleDevice.add(deviceName + "\n" + MACAddress);
		setDeviceListView(singleDevice);
	}
	
	/*Handle button's onClick event
	 * 
	 */
	
	public void onTurnOnBtnClick(View view){
		Intent turnonBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.startActivity(turnonBTIntent);
	}
	
	public void onTurnOffBtnClick(View view){
		BluetoothMgr.getInstance().turnOffBT();
	}
	
	public void onConnectServerBtnClick(View view){
		BluetoothMgr.getInstance().connectAsServer();
	}
	
	public void onListDeviceBtnClick(View view){
		BluetoothMgr.getInstance().performDiscovery();
	}
	
	public void onConnectClientBtnClick(View view){
		BluetoothMgr.getInstance().connectAsClient();
	}
	
	public void onDeviceStringCommClick(View view){
		BluetoothMgr.getInstance().write("apple");
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothMgr.getInstance().EVENT_NO_BLUETOOTH_DEVICE_SELECTED.equals(action)){
				showToast("Select a Remote Device to connect");
				logMsg("No Bluetooth device selected");
			}else if(BluetoothMgr.getInstance().EVENT_BLUETOOTH_NOT_ENABLED.equals(action)){
				showToast("This application required Bluetooth to be enabled");
				logMsg("Bluetooth not enabled");
				setConnectStatus("Bluetooth not enabled");
			}else if(BluetoothMgr.getInstance().EVENT_BLUETOOTH_NOT_SUPPORTED.equals(action)){
				showToast("This device does not support Bluetooth");
				logMsg("Bluetooth not supported");
				setConnectStatus("Bluetooth is not supported");
			}else if(BluetoothMgr.getInstance().EVENT_DEVICE_LIST_UPDATED.equals(action)){
				ArrayList arrayList = BluetoothMgr.getInstance().getDeviceList();
				setDeviceListView(arrayList);
			}else if(BluetoothMgr.getInstance().EVENT_BLUETOOTH_CONNECTION_FAILED.equals(action)){
				showToast("Bluetooth Connection failed");
				logMsg("Bluetooth Connection failed");
				setConnectStatus("Bluetooth connection failed");
			}else if(BluetoothMgr.getInstance().EVENT_DEVICE_NOT_CONNECTED.equals(action)){
				showToast("No remote device connected");
				logMsg("No remote device connected");
			}else if(BluetoothMgr.getInstance().EVENT_STATE_NONE.equals(action)){
				logMsg("Bluetooth State None");
				showToast("Bluetooth State None");
				setConnectStatus("Bluetooth State None");
			}else if(BluetoothMgr.getInstance().EVENT_STATE_LISTEN.equals(action)){
				logMsg("Bluetooth State Listening");
				showToast("Bluetooth State Listening");
				setConnectStatus("Bluetooth state listening");
			}else if(BluetoothMgr.getInstance().EVENT_STATE_CONNECTING.equals(action)){
				showToast("Bluetooth State Connecting");
				logMsg("Bluetooth State Connecting");
				setConnectStatus("Bluetooth connection Connecting");
			}else if(BluetoothMgr.getInstance().EVENT_STATE_CONNECTED.equals(action)){
				showToast("Bluetooth State Connected");
				logMsg("Bluetooth State Connected");
				setConnectStatus("Bluetooth State Connected");
			}
		}
	};
	
	@Override
	public void finish(){
		unregisterReceiver(mReceiver);
		super.finish();
	}
	
	private void setDeviceListView(ArrayList arrayList){
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
}
