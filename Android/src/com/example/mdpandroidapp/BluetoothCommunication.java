package com.example.mdpandroidapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothCommunication extends Activity{
	
	//for logging purpose
	private String logMessage = "";
	private TextView debugView;
	private TextView connectionStatusText;
	
	//for connection to BluetoothService
	private boolean bound = false;
	private BluetoothService bluetoothService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_communication);
		
		debugView = (TextView)findViewById(R.id.text_comm_debug);
		connectionStatusText = (TextView)findViewById(R.id.text_comm_status);
		
		//Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothService.EVENT_STATE_NONE);
		filter.addAction(BluetoothService.EVENT_STATE_LISTEN);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTING);
		filter.addAction(BluetoothService.EVENT_STATE_CONNECTED);
		filter.addAction(BluetoothService.EVENT_MESSAGE_RECEIVED);
		registerReceiver(mReceiver, filter);
		
		try{
			BluetoothDBHelper dbHelper = new BluetoothDBHelper(this);
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor cursor = db.query("STRING_MESS", 
				new String[]{"NAME", "DESCRIPTION"},
				null, null, null, null, null);
			
			if (cursor.moveToFirst()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text01);
				editText.setText(text);
			}
			if (cursor.moveToNext()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text02);
				editText.setText(text);
			}
			if (cursor.moveToNext()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text03);
				editText.setText(text);
			}
			if (cursor.moveToNext()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text04);
				editText.setText(text);
			}
			if (cursor.moveToNext()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text05);
				editText.setText(text);
			}
			if (cursor.moveToNext()){
				String text = cursor.getString(1);
				EditText editText = (EditText)findViewById(R.id.edit_text06);
				editText.setText(text);
			}
			cursor.close();
			db.close();
		}catch (SQLiteException e) { 
			Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
		}

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
			logMsg("Bluetooth State None");
			setConnectStatus("Bluetooth State None");
			return;
		}
		bluetoothService.startToast();
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
	protected void onPause(){
		//bluetoothService.stopToast();
		super.onPause();
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
	
	public void onSendClick(View view){
		//onSaveTextDBClick(null);
		EditText editText;
		Button sendBtn = (Button) view;
		switch(sendBtn.getId()){
		case R.id.button_send01:
			editText = (EditText)findViewById(R.id.edit_text01);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		case R.id.button_send02:
			editText = (EditText)findViewById(R.id.edit_text02);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		case R.id.button_send03:
			editText = (EditText)findViewById(R.id.edit_text03);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		case R.id.button_send04:
			editText = (EditText)findViewById(R.id.edit_text04);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		case R.id.button_send05:
			editText = (EditText)findViewById(R.id.edit_text05);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		case R.id.button_send06:
			editText = (EditText)findViewById(R.id.edit_text06);
			editText.setText(editText.getText().toString().trim()+"\r\n"); 
			bluetoothService.write(editText.getText().toString());
			break;
		default:
			break;
		}
	}
	
	public void onSaveTextDBClick(View view){
		EditText editText;
		
		editText = (EditText)findViewById(R.id.edit_text01);
		new UpdateStringTask().execute(
			new String[] {"edit_text01", editText.getText().toString().trim()+"\r\n"});
		
		editText = (EditText)findViewById(R.id.edit_text02);
		new UpdateStringTask().execute(
			new String[] {"edit_text02", editText.getText().toString().trim()+"\r\n"});
		editText = (EditText)findViewById(R.id.edit_text03);
		new UpdateStringTask().execute(
			new String[] {"edit_text03", editText.getText().toString().trim()+"\r\n"});
		
		editText = (EditText)findViewById(R.id.edit_text04);
		new UpdateStringTask().execute(
			new String[] {"edit_text04", editText.getText().toString().trim()+"\r\n"});
		editText = (EditText)findViewById(R.id.edit_text05);
		new UpdateStringTask().execute(
			new String[] {"edit_text05", editText.getText().toString().trim()+"\r\n"});
		
		editText = (EditText)findViewById(R.id.edit_text06);
		new UpdateStringTask().execute(
			new String[] {"edit_text06", editText.getText().toString().trim()+"\r\n"});
	}
	
	public void onCommBackClick(View view){
		this.onStop();
		this.finish();
	}
	
	private class UpdateStringTask extends AsyncTask<String, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(String... name) {
			String[] values = (String[]) name;
			ContentValues stringValues = new ContentValues();
			stringValues.put("DESCRIPTION", values[1]);
			BluetoothDBHelper dbHelper = 
				new BluetoothDBHelper(BluetoothCommunication.this);
			try{
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				db.update("STRING_MESS", stringValues, "NAME = ?", 
					new String[] {values[0]});
				db.close();
				return true;
			}catch(SQLiteException e){
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean success){ 
			if (success){
				logMsg("Text Saved");
			}
		}
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothService.EVENT_STATE_NONE.equals(action)){
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
	
	private void logMsg(String message){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		logMessage += dateFormat.format(date)+ ": " + message + "\n";
		debugView.setText(logMessage);
	}
	
	private void setConnectStatus(String message){
		connectionStatusText.setText("Status: "+message);
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
		public void onServiceDisconnected(ComponentName name){
			bound = false;
		}
	};
	
}
