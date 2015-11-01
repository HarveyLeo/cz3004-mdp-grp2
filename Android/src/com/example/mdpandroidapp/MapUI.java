package com.example.mdpandroidapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MapUI extends Activity implements SensorEventListener{
	
	//for logging purpose
	private String logMessage = "";
	private TextView debugView;
	
	//for toggle update mode
	private Switch updateSwitch;
	private ImageButton manualUpdateBtn;
	
	//for connection to BluetoothService
	private boolean bound = false;
	private BluetoothService bluetoothService;
	
	//for status display
	private TextView robotStatusView;
	private TextView connectionStatusView;
	private TextView RPIMACAddrView;
	private TextView RPIDeviceNameView;
	private String RPIMACAddr = "";
	private String RPIDeviceName = "";
	
	//for update demonstration
	private PixelGridView pixelGrid;
	private boolean isAutoUpdate = false;
	private boolean listenForUpdate = false;
	
	//for actual map update
	private boolean isP0Received;
	private boolean isP1Received;
	private boolean isP2Received;
	
	private String P0Str;
	private String P1Str;
	private String P2Str;
	
	/*********************************************************/
	private boolean tiltEnabled = false;
	private SensorManager sensorManager;
	/********************************************************/

	/*Activity LifeCycle
	 * 
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_map_ui);
        
        robotStatusView = (TextView)findViewById(R.id.textViewStatus);
        connectionStatusView = (TextView)findViewById(R.id.textViewConnect);
        RPIMACAddrView = (TextView)findViewById(R.id.textViewRPIMAC);
        RPIDeviceNameView = (TextView)findViewById(R.id.textViewRPIName);
        //debugView = (TextView)findViewById(R.id.text_map_debug);
        updateSwitch = (Switch)findViewById(R.id.updateSwitch);
        manualUpdateBtn = (ImageButton)findViewById(R.id.buttonRefresh);
        
        
        pixelGrid = (PixelGridView)findViewById(R.id.map);
        pixelGrid.setNumColumns(15);
        pixelGrid.setNumRows(20);
        //pixelGrid.setSampleMap();
        pixelGrid.invalidate();
        
        //Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothService.EVENT_STATE_NONE);
		filter.addAction(BluetoothService.EVENT_STATE_LISTEN);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTING);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTED);
		filter.addAction(BluetoothService.EVENT_MESSAGE_RECEIVED);
		registerReceiver(mReceiver, filter);
		
		/*******************************************************************************************************************************/
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
		/*******************************************************************************************************************************/
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
		
		//Read saved RPI MAC and name from DB.
		try{
			BluetoothDBHelper dbHelper = new BluetoothDBHelper(this);
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor cursor = db.query("STRING_MESS", 
				new String[]{"NAME", "DESCRIPTION"},
				null, null, null, null, null);
			if (cursor.moveToLast()){
				RPIMACAddr = cursor.getString(1);
				setRPIMacAddr(RPIMACAddr);
			}
			if (cursor.moveToPrevious()){
				RPIDeviceName = cursor.getString(1);
				setRPIDeviceName(RPIDeviceName);
			}
			cursor.close();
			db.close();
		}catch (SQLiteException e) { 
			Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
		}
		
		if (bluetoothService == null){
			setConnectStatus("Bluetooth State None");
			disableUpdateSwitch();
			return;
		}
		
		bluetoothService.setRPIDeviceInfo(RPIDeviceName, RPIMACAddr);
		bluetoothService.startToast();
		
		int mState = bluetoothService.getState();
		switch(mState){
			case BluetoothService.STATE_NONE:
				setConnectStatus("Bluetooth State None");
				disableUpdateSwitch();
				break;
			case BluetoothService.STATE_LISTEN:
				setConnectStatus("Bluetooth State Listening");
				disableUpdateSwitch();
				break;
			case BluetoothService.STATE_CONNECTING:
				setConnectStatus("Bluetooth State Connecting");
				disableUpdateSwitch();
				break;
			case BluetoothService.STATE_CONNECTED:
				setConnectStatus("Bluetooth State Connected");
				enableUpdateSwitch();
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
	
	public void onExploreBtnClick(View view){
		bluetoothService.write("explore\r\n");
	}
	
	public void onShortestBtnClick(View view){
		bluetoothService.write("fastest\r\n");
	}
    
	public void onClearMapBtnClick(View view){
		pixelGrid.clearMap();
		pixelGrid.invalidate();
	}
	
	public void enableBTBtnClick(View view){
		Intent turnonBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.startActivity(turnonBTIntent);
	}
	
	public void disableBTBtnClick(View view){
		bluetoothService.turnOffBT();
	}
	
    //handle auto/ manual update switch
	public void onUpdateSwitchClick(View view){
		boolean on = ((Switch)view).isChecked();
		
		if (on){
			bluetoothService.write("sendArena");
			isAutoUpdate = true;
			manualUpdateBtn.setEnabled(false);
		}else{
			manualUpdateBtn.setEnabled(true);
			isAutoUpdate = false;
		}
	}
	
	public void onManualUpdateBtnClick(View view){
		bluetoothService.write("sendArena");
		listenForUpdate = true;
	}
	
	public void autoConnect(View view){
		bluetoothService.autoConnectAsClient();
	}
	
	private void disableUpdateSwitch(){
		updateSwitch.setEnabled(false);
		updateSwitch.setChecked(false);
		manualUpdateBtn.setEnabled(false);
		isAutoUpdate = false;
	}
	
	private void enableUpdateSwitch(){
		updateSwitch.setEnabled(true);
		updateSwitch.setChecked(false);
		manualUpdateBtn.setEnabled(true);
		isAutoUpdate = false;
	}
    
    //handle start coord update
    public void onSendCoordBtnClick(View view){
    	EditText xText = (EditText)findViewById(R.id.editTextX);
    	EditText yText = (EditText)findViewById(R.id.editTextY);
    	String tempCoord = String.format("%s,%s",
			xText.getText().toString(),
			yText.getText().toString() + "\r\n");
    	
    	//transmit tempCoord
    	bluetoothService.write(tempCoord);
    }
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothService.EVENT_STATE_NONE.equals(action)){
				setConnectStatus("Bluetooth State None");
				disableUpdateSwitch();
			}else if(BluetoothService.EVENT_STATE_LISTEN.equals(action)){
				setConnectStatus("Bluetooth State Listening");
				disableUpdateSwitch();
			}else if(BluetoothService.EVENT_STATE_CONNECTING.equals(action)){
				setConnectStatus("Bluetooth State Connecting");
				disableUpdateSwitch();
			}else if(BluetoothService.EVENT_STATE_CONNECTED.equals(action)){
				setConnectStatus("Bluetooth State Connected");
				enableUpdateSwitch();
			}else if(BluetoothService.EVENT_MESSAGE_RECEIVED.equals(action)){
				//substring(array index, array index + length)
				
				//received message is assumed to be trimmed, with no leading
				//or trailing white space.
				
				//check for actual update sub string
				String message = bluetoothService.getReceivedMsg();
				String code = "";
				logMsg("Received: "+message);
				if (message.length()>2)
					code = message.substring(0,2);
				if (code.equals("P0")){
					//Remove P0 prefix
					P0Str = message.substring(2);
					isP0Received = true;
					checkCompleteMapDesc();
				}else if(code.equals("P1")){
					P1Str = message.substring(2);
					isP1Received = true;
					checkCompleteMapDesc();
				}else if(code.equals("P2")){
					P2Str = message.substring(2);
					isP2Received = true;
					checkCompleteMapDesc();
				}
				
				//check for demo update sub string
				if (message.length()>8)
					code = message.substring(2, 6);
				logMsg("Code:"+code);
				if (code.equals("grid")){
					
					//check if we should listen for update
					if (isAutoUpdate || (!isAutoUpdate && listenForUpdate)){
						String P2 = message.substring(12, message.length()-2);
						logMsg(String.format("P2=%s\nlength=%s", P2, P2.length()));
						pixelGrid.updateDemoArenaMap(P2);
						pixelGrid.invalidate();
						listenForUpdate = false;
					}
				}
				
				//check for demo robot status
				if (message.length()>10)
					code = message.substring(2,8);
				if (code.equals("status")){
					setRobotStatus(message.substring(11,(message.length()-2)));
				}
				
				//check for demo robot position
				if (message.length()>17)
					code = message.substring(2,15);
				if (code.equals("robotPosition")){
					String info = message.substring(20,message.length()-2);
					pixelGrid.updateDemoRobotPos(info);
					pixelGrid.invalidate();
					
				}
			}
		}
	};
	
	private void checkCompleteMapDesc(){
		if (isP0Received && isP1Received && isP2Received){
			isP0Received = false;
			isP1Received = false;
			isP2Received = false;
			pixelGrid.updateActualMap(P0Str, P1Str, P2Str);
			pixelGrid.invalidate();
		}
	}
	
	private void logMsg(String message){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		//logMessage += dateFormat.format(date)+ ": " + message + "\n";
		//debugView.setText(logMessage);
	}
	
	private void setRPIMacAddr(String message){
		RPIMACAddrView.setText("RPI MAC: "+message);
	}
	
	private void setRPIDeviceName(String message){
		RPIDeviceNameView.setText("RPI Name: "+message);
	}
	
	private void setConnectStatus(String message){
		connectionStatusView.setText("Conn: "+message);
	}
	
	private void setRobotStatus(String message){
		robotStatusView.setText("Status: "+message);
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
					disableUpdateSwitch();
					break;
				case BluetoothService.STATE_LISTEN:
					setConnectStatus("Bluetooth State Listening");
					disableUpdateSwitch();
					break;
				case BluetoothService.STATE_CONNECTING:
					setConnectStatus("Bluetooth State Connecting");
					disableUpdateSwitch();
					break;
				case BluetoothService.STATE_CONNECTED:
					setConnectStatus("Bluetooth State Connected");
					enableUpdateSwitch();
					break;
				default:
					break;
			}
			bluetoothService.setRPIDeviceInfo(RPIDeviceName, RPIMACAddr);
		}
		@Override
		public void onServiceDisconnected(ComponentName name){
			bound = false;
		}
	};
	
	
	
	/********************************************************************************************************************************/
    //handle tilt switch
	public void onTiltSwitchClick(View view){
		boolean on = ((Switch)view).isChecked();
		
		if (on){
			tiltEnabled = true;
		}else{
			tiltEnabled = false;
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		if (tiltEnabled) {
			if (y < -5) {
				bluetoothService.write("f");			
			} else if (x < -5) {
				bluetoothService.write("tr");
			} else if (x > 5) {
				bluetoothService.write("tl");
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	/********************************************************************************************************************************/
	
}
