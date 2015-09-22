package com.example.mdpandroidapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;

public final class BluetoothMgr{

	/*Variables and Constant
	 * 
	 * */
	
	private static final BluetoothMgr instance = new BluetoothMgr();

	public static Handler mHandler;
	
	//bluetoothmanager states
	public static final int STATE_NONE = 0; //doing nothing
	public static final int STATE_LISTEN = 1; //listening for incoming
	public static final int STATE_CONNECTING = 2; //initiating outgoing
	public static final int STATE_CONNECTED = 3; //connected to a device
	
	//for broadcast event
	public static final String EVENT_NO_BLUETOOTH_DEVICE_SELECTED = "com.event.NO_BLUETOOTH_DEVICE_SELECTED";
	public static final String EVENT_BLUETOOTH_NOT_ENABLED = "com.event.BLUETOOTH_NOT_ENABLED";
	public static final String EVENT_BLUETOOTH_NOT_SUPPORTED = "com.event.EVENT_BLUETOOTH_NOT_SUPPORTED";
	public static final String EVENT_DEVICE_LIST_UPDATED = "com.event.DEVICE_LIST_UPDATED";
	public static final String EVENT_BLUETOOTH_CONNECTION_FAILED = "com.event.BLUETOOTH_CONNECTION_FAILED";
	public static final String EVENT_DEVICE_NOT_CONNECTED = "com.event.DEVICE_NOT_CONNECTED";
	public static final String EVENT_STATE_NONE = "com.event.EVENT_STATE_NONE";
	public static final String EVENT_STATE_LISTEN = "com.event.EVENT_STATE_LISTEN";
	public static final String EVENT_STATE_CONNECTING = "com.event.EVENT_STATE_CONNECTING";
	public static final String EVENT_STATE_CONNECTED = "com.event.EVENT_STATE_CONNECTED";
	private static Context context;
	
	//for string communication
	public static final int MESSAGE_READ = 0020;
	public static final int MESSAGE_SENT = 0021;
	
	private ConnectThread mConnectThread;
	private AcceptThread mAcceptThread;
	private static ConnectedThread mConnectedThread;
	
	
	public static int mState = STATE_NONE;
	
	
	private static BluetoothAdapter mBluetoothAdapter;
	private static ArrayList arrayList;
	
	// Well known SPP UUID
	private static final UUID MY_UUID 
		= UUID.fromString("661dd0dc-e688-4945-9921-6b13ba67b07e");
	
	//for connection purpose
	private static String targetMACAddress = "";
	private static Device device = null;
	private static String targetDeviceName;
	private static String serverName = "Server 9001";
	private static String connectedDeviceName;
	private static String connectedMACAddress = "";
	
	/*constructor
	 * 
	 * */
	private BluetoothMgr(){
		super();
		arrayList = new ArrayList();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		//Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		if (context == null) return;
		context.registerReceiver(mReceiver, filter);
	}
	
	/*Singleton
	 * 
	 */
	public static BluetoothMgr getInstance(){
		return instance;
	}
	
	/*Setter and Getters
	 * 
	 */
	
	public void setDeviceInfo(String deviceName, String MACAddress){
		BluetoothMgr.targetDeviceName = deviceName;
		BluetoothMgr.targetMACAddress = MACAddress;

		if (mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		
		//clear listedDevice
		arrayList = new ArrayList();
		arrayList.add(deviceName + "\n" + MACAddress);
		pairDevice(mBluetoothAdapter.getRemoteDevice(MACAddress));
	}
	
	public void setHandler(Handler mHandler){
		BluetoothMgr.mHandler = mHandler;
	}
	
	public void setContext(Context context){
		BluetoothMgr.context = context;
	}
	
	public ArrayList getDeviceList(){
		return BluetoothMgr.arrayList;
	}
	
	public boolean isContextSet(){
		return (context==null)? false : true;
	}
	
	public int getState(){
		return BluetoothMgr.mState;
	}
	
	public void turnOffBT(){
		if (mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.disable();
		}
	}
	
	public void performDiscovery(){
		//clear deviceInfo
		targetDeviceName = "";
		targetMACAddress = "";
		connectedDeviceName = "";
		connectedMACAddress = "";
		
		//clear listedDevice
		arrayList = new ArrayList();
		
		if (mBluetoothAdapter == null){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_SUPPORTED);
			return;
		}
		
		if (!mBluetoothAdapter.isEnabled()){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_ENABLED);
			return;
		}
		
		if (mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		
		//List paired device in ListView, Bluetooth must be on
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0){
			for (BluetoothDevice device : pairedDevices){
				arrayList.add(device.getName()+"\n"+device.getAddress());
			}
			sendIntentBroadcast(EVENT_DEVICE_LIST_UPDATED);
		}
		//List non-paired devices
		mBluetoothAdapter.startDiscovery(); 
	}
	
	public void connectAsServer(){
		if (mBluetoothAdapter == null){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_SUPPORTED);
			return;
		}
		
		if (!mBluetoothAdapter.isEnabled()){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_ENABLED);
			return;
		}
		
		if (mState == STATE_LISTEN){
			if (mAcceptThread != null){
				mAcceptThread.cancel();
				mAcceptThread = null;
			}
		}
		
		if (mConnectThread != null){
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		//Cancel any thread currently running a connection
		if (mConnectedThread != null){
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mAcceptThread = new AcceptThread();
		mAcceptThread.start();
		setState(STATE_LISTEN);
	}
	
	public void connectAsClient(){
		if (mBluetoothAdapter == null){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_SUPPORTED);
			return;
		}
		
		if (!mBluetoothAdapter.isEnabled()){
			sendIntentBroadcast(EVENT_BLUETOOTH_NOT_ENABLED);
			return;
		}
		if (targetMACAddress.equals("")){
			sendIntentBroadcast(EVENT_NO_BLUETOOTH_DEVICE_SELECTED);
			return;
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(
			targetMACAddress);
		
		if (mState == STATE_CONNECTING){
			if (mConnectThread != null){
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}
		
		if (mAcceptThread != null){
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		
		//Cancel any thread currently running a connection
		if (mConnectedThread != null){
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}
	
	private void setState(int state){
		BluetoothMgr.mState = state;
		switch(state){
		case STATE_NONE:
			sendIntentBroadcast(EVENT_STATE_NONE);
			break;
		case STATE_LISTEN:
			sendIntentBroadcast(EVENT_STATE_LISTEN);
			break;
		case STATE_CONNECTING:
			sendIntentBroadcast(EVENT_STATE_CONNECTING);
			break;
		case STATE_CONNECTED:
			sendIntentBroadcast(EVENT_STATE_CONNECTED);
			break;
		default:
			break;
		}
	}
	
	public synchronized void stop(){
		setState(STATE_NONE);
		if (mConnectThread != null) {
	        mConnectThread.cancel();
	        mConnectThread = null;
	    }
		
		if (mAcceptThread != null){
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

	    if (mConnectedThread != null) {
	        mConnectedThread.cancel();
	        mConnectedThread = null;
	    }
	    if (mBluetoothAdapter != null) {
	        mBluetoothAdapter.cancelDiscovery();
	    }
	}
	
	private static Object obj = new Object();
	
	public static void write(String message){
		ConnectedThread r;
		synchronized(obj){
			if (mState != STATE_CONNECTED){
				sendIntentBroadcast(EVENT_DEVICE_NOT_CONNECTED);
				return;
			}
			r = mConnectedThread;
		}
		if (message.length() > 0){
			byte[] send = message.getBytes();
			r.write(send);
		}
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(
					BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED){
					arrayList.add(device.getName()+"\n"+device.getAddress());
				}
				sendIntentBroadcast(EVENT_DEVICE_LIST_UPDATED);
			}
		}
	};
	
	private synchronized void connected(BluetoothSocket mmSocket,
		BluetoothDevice mmDevice){
		// Cancel the thread that completed the connection
	    if (mConnectThread != null) {
	        mConnectThread.cancel();
	        mConnectThread = null;
	    }
	    
	    if (mAcceptThread != null){
	    	mAcceptThread.cancel();
	    	mAcceptThread = null;
	    }

	    // Cancel any thread currently running a connection
	    if (mConnectedThread != null) {
	        mConnectedThread.cancel();
	        mConnectedThread = null;
	    }
	    
	    if (mmDevice != null){
	    	connectedDeviceName = mmDevice.getName();
	    	connectedMACAddress = mmDevice.getAddress();
	    }

	    mConnectedThread = new ConnectedThread(mmSocket);
	    mConnectedThread.start();
	    
	    setState(STATE_CONNECTED);
	}
	
	private class AcceptThread extends Thread{
		private final BluetoothServerSocket mmServerSocket;
		
		public AcceptThread(){
			BluetoothServerSocket tmp = null;
			try{
				tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
					serverName, MY_UUID);
			}catch(IOException e){
				sendIntentBroadcast(EVENT_BLUETOOTH_CONNECTION_FAILED);
				e.printStackTrace();
			}
			mmServerSocket = tmp;
		}
		
		@Override
		public void run(){
			BluetoothSocket socket = null;
			while(mState != STATE_CONNECTED){
				try{
					socket = mmServerSocket.accept();
				}catch (IOException e){
					sendIntentBroadcast(EVENT_BLUETOOTH_CONNECTION_FAILED);
					e.printStackTrace();
					break;
				}
				//If a connection was accepted
				if (socket != null){
					connected(socket, socket.getRemoteDevice());
					try{
						mmServerSocket.close();
					}catch(IOException e){ }
					break;
				}
			}
		}
		
		public void cancel(){
			try{
				mmServerSocket.close();
			}catch (IOException e){	}
		}
	}//end of AcceptThread
	
	private class ConnectThread extends Thread{
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		
		public ConnectThread(BluetoothDevice device){
			this.mmDevice = device;
			BluetoothSocket tmp = null;
			try{
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			}catch(Exception e){
			}
			mmSocket = tmp;
		}
		
		@Override
		public void run(){
			setName("ConnectThread");
			mBluetoothAdapter.cancelDiscovery();
			try{
				mmSocket.connect();
			}catch(IOException connectException){
				try{
					mmSocket.close();
				}catch(IOException closeException){;
				}
				sendIntentBroadcast(EVENT_BLUETOOTH_CONNECTION_FAILED);
				return;
			}
			synchronized (BluetoothMgr.this){
				mConnectThread = null;
			}
			connected(mmSocket, mmDevice);
		}
		
		public void cancel(){
			try{
				mmSocket.close();
			}catch(IOException closeException){
			}
		}
	}//end of connectThread()
	
	private class ConnectedThread extends Thread{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		
		public ConnectedThread(BluetoothSocket socket){
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}catch(IOException e){
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		
		@SuppressLint("NewApi")
		@Override
		public void run(){

			byte[] buffer = new byte[1024];
			int bytes;

			while(true){
				try{
					//Read from the InputStream
					bytes = mmInStream.read(buffer);
					//Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
						.sendToTarget();	
				}catch(IOException e){
					sendIntentBroadcast(EVENT_BLUETOOTH_CONNECTION_FAILED);
					break;
				}
			}
		}
		@SuppressLint("NewApi")
		public void write(byte[] buffer){
			try{
				mmOutStream.write(buffer);
				mHandler.obtainMessage(MESSAGE_SENT, buffer.length, -1, buffer)
					.sendToTarget();	
			}catch(IOException e){
				sendIntentBroadcast(EVENT_BLUETOOTH_CONNECTION_FAILED);
			}
		}
		
		public void cancel(){
			try{
				mmSocket.close();
			}catch(IOException e){
			}
		}
		
	}//end of connectedThread
	
	private static void pairDevice(BluetoothDevice device){
		try{
			Method method = device.getClass().getMethod("createBond", (Class[])null);
			method.invoke(device, (Object[]) null);
		}catch(Exception e){
		}
	}
	
	//wont be using this
	private static void unpairDevice(BluetoothDevice device){
		try{
			Method method = device.getClass().getMethod("removeBond", (Class[])null);
			method.invoke(device, (Object[]) null);
		}catch(Exception e){
		}
	}
	
	private static void sendIntentBroadcast(String eventCode){
		Intent intent = new Intent();
		intent.setAction(eventCode);
		if (context == null){
			return;
		}
		context.sendBroadcast(intent);
	}
}
