package com.example.mdpandroidapp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothCommunication extends Activity {
	
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter mArrayAdapter; 
	
	// Well known SPP UUID
	  private static final UUID MY_UUID =
	      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	//for logging purpose
	private String logMessage = "";
	private TextView debugView;
	
	//for connection purpose
	private String MACAddress;
	private String deviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_communication);
		debugView = (TextView)findViewById(R.id.text_debug);
		
		mArrayAdapter = new ArrayAdapter(
			this,android.R.layout.simple_list_item_1, new ArrayList());
		
		//Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_UUID);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		
		//Create listener for ListView
		AdapterView.OnItemClickListener itemClickListener = new 
			AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> ListView, View
				itemView, int position, long id){
				mBluetoothAdapter.cancelDiscovery();
				String deviceInfo = (String)ListView.getItemAtPosition(position);
				String[] deviceData = deviceInfo.split("\n");
				if (deviceData.length == 2){
					
					deviceName = deviceData[0];
					MACAddress = deviceData[1];
					logMsg(String.format("Select name: %s, MAC: %s", 
						deviceName, MACAddress));
					//ensure that selected device is paired
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MACAddress);
					pairDevice(device);
					singleDeviceListView();
				}else{
					logMsg("Invalid selection @ device ListView");
				}
			}

		};
		
		IntentFilter stateIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(mPairReceiver, stateIntent);
		
		//Add the listener to the ListView
		ListView listView = (ListView)findViewById(R.id.list_devices);
		listView.setOnItemClickListener(itemClickListener);
	}
	
	private void singleDeviceListView(){
		ArrayList singleDevice = new ArrayList();
		singleDevice.add(deviceName + "\n" + MACAddress);
		mArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, singleDevice);
		ListView listView = (ListView)findViewById(R.id.list_devices);
		listView.setAdapter(mArrayAdapter);
	}
	
	public void performDiscovery(View view){
		logMsg("Performing Discovery");
		
		//clear array list
		mArrayAdapter = new ArrayAdapter(
				this,android.R.layout.simple_list_item_1, new ArrayList());
		
		//Check if device support Bluetooth
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null){
			logMsg("Device does not support Bluetooth");
			return;
		}
		
		if (!mBluetoothAdapter.isEnabled()){
			Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBTIntent);
		}
		
		while(!mBluetoothAdapter.isEnabled()){
			//wait for Bluetooth to be enabled
		}
		
		//List paired device in ListView, Bluetooth must be on
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		//If there are paired device
		if (pairedDevices.size() > 0){
			logMsg(String.format("%d paired device found", pairedDevices.size()));
			//Loop through paired devices
			for (BluetoothDevice device : pairedDevices){
				//Add name and MAC address to an array adapter to show in LisrView
				mArrayAdapter.add(device.getName()+"\n"+device.getAddress());
			}
			//Bind ArrayAdapter to ListView
			ListView listView = (ListView)findViewById(R.id.list_devices);
			listView.setAdapter(mArrayAdapter);
		}else{
			logMsg("There is no paired device");
		}
		
		//List non-paired devices
		mBluetoothAdapter.startDiscovery(); 

	}
	
	public void connectAsClient(View view){
		logMsg("Connect As Client");
		
		if (MACAddress.equals("")){
			logMsg("Empty MACAddress");
			return;
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(
			MACAddress);
		
		final Handler handler = new Handler();
		handler.post(new ConnectThread(device));
	}
	
	public void transmitOutputStream(View view){
		logMsg("Transmitting Outstream");
	}
	
	private void logMsg(String message){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		logMessage += dateFormat.format(date)+ ": " + message + "\n";
		debugView.setText(logMessage);
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			//When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				//Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(
					BluetoothDevice.EXTRA_DEVICE);
				//Add the name and address to an array adapter
				mArrayAdapter.add(device.getName()+"\n"+device.getAddress());
			}else if (BluetoothDevice.ACTION_UUID.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(
					BluetoothDevice.EXTRA_DEVICE);
				Parcelable[] uuidExtra = intent.getParcelableArrayExtra(
					BluetoothDevice.EXTRA_UUID);
				for (int i=0; i<uuidExtra.length; i++){
					logMsg(String.format("Device:%s,Service:%s",
						device.getName(), uuidExtra[i].toString()));
				}
			}else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				logMsg("Discovey Started");
			}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				logMsg("Discovery Finished");
				for (int i=0; i <mArrayAdapter.getCount(); i++){
					logMsg(((String)mArrayAdapter.getItem(i)).split("\n")[0]);
				}
			}
		}
	};
	
	private class ConnectThread extends Thread{
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		
		public ConnectThread(BluetoothDevice device){
			BluetoothSocket tmp = null;
			mmDevice = device;
			
			
			try{
				//tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				
				Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
				tmp = (BluetoothSocket) m.invoke(device, 4);
				
			}catch(IllegalAccessException e){ 
				logMsg(e.getMessage());
			}catch(IllegalArgumentException e){
				logMsg(e.getMessage());
			}catch(InvocationTargetException e){
				logMsg(e.getMessage());
			}catch(NoSuchMethodException e){
				logMsg(e.getMessage());
			}
			mmSocket = tmp;
		}
		
		@Override
		public void run(){	
			//cancel discovery if it is running
			try{
				mmSocket.connect();
				logMsg("no connection error encountered");
			}catch(IOException connectException){
				logMsg("IOException occured when connecting socket");
				logMsg(connectException.getMessage());
				try{
					mmSocket.close();
				}catch(IOException closeException){
					logMsg("IOException occured when closing socket");
					logMsg(closeException.getMessage());
				}
			}
		}
		
		public void cancel(){
			try{
				mmSocket.close();
			}catch(IOException closeException){
				logMsg("IOException occured when closing socket");
				logMsg(closeException.getMessage());
			}
		}
	}//end of connectThread()
	
	private void pairDevice(BluetoothDevice device){
		try{
			Method method = device.getClass().getMethod("createBond", (Class[])null);
			method.invoke(device, (Object[]) null);
		}catch(Exception e){
			logMsg(e.getMessage());
		}
	}
	
	private void unpairDevice(BluetoothDevice device){
		try{
			Method method = device.getClass().getMethod("removeBond", (Class[])null);
			method.invoke(device, (Object[]) null);
		}catch(Exception e){
			logMsg(e.getMessage());
		}
	}
	
	private final BroadcastReceiver mPairReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			
			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
				logMsg("BluetoothDevice state changed");
				final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 
					BluetoothDevice.ERROR);
				final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
					BluetoothDevice.ERROR);
				
				if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING){
					showToast("Paired");
				}
				if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
					showToast("Unpaired");
				}
			}
		}
	};
	
	private void showToast(String str){
		int duration = Toast.LENGTH_SHORT;
		CharSequence text = str;
		Toast toast = Toast.makeText(this, text, duration);
	}


}
