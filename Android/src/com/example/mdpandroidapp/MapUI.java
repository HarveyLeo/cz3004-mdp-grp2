package com.example.mdpandroidapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;


public class MapUI extends Activity {
	
	//for logging purpose
	private String logMessage = "";
	private TextView debugView;
	
	//for connection to BluetoothService
	private boolean bound = false;
	private BluetoothService bluetoothService;
	
	//for status display
	private TextView robotStatusView;
	private TextView connectionStatusView;
	
	//for update demostration
	private boolean isAutoUpdate = false;
	private PixelGridView pixelGrid;

	/*Activity LifeCycle
	 * 
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_map_ui);
        
        robotStatusView = (TextView)findViewById(R.id.text_robot_status);
        connectionStatusView = (TextView)findViewById(R.id.text_connection_status);
        debugView = (TextView)findViewById(R.id.text_map_debug);
        
        
        pixelGrid = (PixelGridView)findViewById(R.id.arenaMap);
        pixelGrid.setNumColumns(15);
        pixelGrid.setNumRows(20);
        pixelGrid.invalidate();
        
        //Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothService.EVENT_STATE_NONE);
		filter.addAction(BluetoothService.EVENT_STATE_LISTEN);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTING);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTED);
		filter.addAction(BluetoothService.EVENT_MESSAGE_RECEIVED);
		registerReceiver(mReceiver, filter);
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
		if (bluetoothService == null){
			setConnectStatus("Bluetooth State None");
			return;
		}
		bluetoothService.startToast();
		int mState = bluetoothService.getState();
		switch(mState){
			case BluetoothService.STATE_NONE:
				setConnectStatus("Bluetooth State None");
				break;
			case BluetoothService.STATE_LISTEN:
				setConnectStatus("Bluetooth State Listening");
				break;
			case BluetoothService.STATE_CONNECTING:
				setConnectStatus("Bluetooth State Connecting");
				break;
			case BluetoothService.STATE_CONNECTED:
				setConnectStatus("Bluetooth State Connected");
				break;
			default:
				break;
		}
		
		//clear map
		pixelGrid.clearMap();
		pixelGrid.invalidate();
	}
    
    @Override
	protected void onStop(){
		//bluetoothService.stopToast();
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
    
    /*Handle Button Click here
	 * 
	 */
    
    //handle movement buttons
    public void onLeftBtnClick(View view){
    	bluetoothService.write("tl");
    }
    
	public void onRightBtnClick(View view){
	    bluetoothService.write("tr");	
	}
	
	public void onForwardBtnClick(View view){
		bluetoothService.write("f");	
	}
    
    //handle auto/ manual update switch
	public void onUpdateSwitchClick(View view){
		boolean on = ((Switch)view).isChecked();
		
		if (on){
			//set auto update
			isAutoUpdate = true;
			//check if connected
			
		}else{
			//set manual update
			isAutoUpdate = false;
		}
	}
	
    
    //handle start coord update
    public void onSendCoordBtnClick(View view){
    	EditText xText = (EditText)findViewById(R.id.edit_xcoord);
    	EditText yText = (EditText)findViewById(R.id.edit_ycoord);
    	String tempCoord = String.format("P3?,?",
			xText.getText().toString(),
			yText.getText().toString());
    	
    	//transmit tempCoord
    	bluetoothService.write(tempCoord);
    }
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothService.EVENT_STATE_NONE.equals(action)){
				setConnectStatus("Bluetooth State None");
			}else if(BluetoothService.EVENT_STATE_LISTEN.equals(action)){
				setConnectStatus("Bluetooth State Listening");
			}else if(BluetoothService.EVENT_STATE_CONNECTING.equals(action)){
				setConnectStatus("Bluetooth State Connecting");
			}else if(BluetoothService.EVENT_STATE_CONNECTED.equals(action)){
				setConnectStatus("Bluetooth State Connected");
			}else if(BluetoothService.EVENT_MESSAGE_RECEIVED.equals(action)){
				//substring(array index, array index + length)
				//check for actual update sub string
				String message = bluetoothService.getReceivedMsg();
				//logMsg("Received: "+message);
				String code = message.substring(0,2);
				if (code.equals("P0")){
				}else if(code.equals("P1")){
				}else if(code.equals("P2")){
				}
				
				code = message.substring(2, 6);
				//check for demo update sub string
				if (code.equals("grid")){
					String P2 = message.substring(11, message.length()-2);
				}
				//check for demo robot position
				code = message.substring(2,15);
				//logMsg("Code:" + code);
				if (code.equals("robotPosition")){
					String info = message.substring(20,message.length()-2);
					//logMsg("Info:" + info);
					pixelGrid.updateDemoRobotPos(info);
					pixelGrid.invalidate();
					
				}
				//check for demo robot status
				code = message.substring(2,8);
				if (code.equals("status")){
					setRobotStatus(message.substring(11,(message.length()-2)));
				}
			}
		}
	};
	
	private void logMsg(String message){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		logMessage += dateFormat.format(date)+ ": " + message + "\n";
		debugView.setText(logMessage);
	}
	
	private void setConnectStatus(String message){
		connectionStatusView.setText("Conn Status: "+message);
	}
	
	private void setRobotStatus(String message){
		robotStatusView.setText("Robot Status: "+message);
	}
	
	private ServiceConnection connection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder){
			BluetoothService.BluetoothBinder bluetoothBinder =
				(BluetoothService.BluetoothBinder) binder;
			bluetoothService = bluetoothBinder.getBluetooth();
			bound = true;
			bluetoothService.startToast();
			int mState = bluetoothService.getState();
			switch(mState){
				case BluetoothService.STATE_NONE:
					setConnectStatus("Bluetooth State None");
					break;
				case BluetoothService.STATE_LISTEN:
					setConnectStatus("Bluetooth State Listening");
					break;
				case BluetoothService.STATE_CONNECTING:
					setConnectStatus("Bluetooth State Connecting");
					break;
				case BluetoothService.STATE_CONNECTED:
					setConnectStatus("Bluetooth State Connected");
					break;
				default:
					break;
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName name){
			bound = false;
		}
	};
	
	
}
