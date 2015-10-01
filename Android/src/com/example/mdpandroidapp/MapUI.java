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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

/**************/
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
/**************/

/*
update actual map desc
1. must pass 3 strings, robot pos, explored map and obstacle map
2. keep the maparray
3. perform the algorithm
4. print the result

update actual robot pos
1. perform together with map update (must be passed together)

update demo map desc
1. must pass in one string, obstacle map.
2. robot pos is updated separately and explored map is assumed to be fully explored
3. clear the maparray (algorithm will ignore already explored cells)
4. perform the algorithm
5. print the result

update demo robot pos
1. must pass in one string, robot pos
2. robot pos must be transformed to suit the algorithm
	2a. input(x)(y)(orientation) will be switched to (row(y)))(col(x))(orientation)
	2b. input (x)(y)(orientation) will be changed to (x+1)(y+1)(orientation), taking reference
		from the center of the robot instead of the top left corner.
3. keep the maparray
4. perform the algorithm (clear the existing robot pos and insert new pos)
5. print the result.

misc
1. map array will be cleared when user back out the activity
2. switch will be reset to manual and disabled at the following conditions
	- not connected
	- disconnected
	
	
	<ScrollView 
        android:layout_width="match_parent"
    	android:layout_height="0dp"
    	android:layout_weight="1">
		
        <TextView
          android:id="@+id/text_map_debug"
          android:layout_width="match_parent"
		  android:layout_height="wrap_content"
		  android:text="Display debug message here"
          />
		        
	</ScrollView>
*/

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
		if (bluetoothService == null){
			setConnectStatus("Bluetooth State None");
			disableUpdateSwitch();
			return;
		}
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
		bluetoothService.write("P5StartExploration");
	}
	
	public void onShortestBtnClick(View view){
		bluetoothService.write("P6StartShortest");
	}
    
	public void onClearMapBtnClick(View view){
		pixelGrid.clearMap();
		pixelGrid.invalidate();
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
    	String tempCoord = String.format("P3%s,%s",
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
	
	private void setConnectStatus(String message){
		connectionStatusView.setText("Connection: "+message);
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
