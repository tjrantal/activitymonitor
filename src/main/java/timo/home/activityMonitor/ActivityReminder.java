package timo.home.activityMonitor;
/*Debugging adb logcat Alku:D tsw:D imu:D  *:S*/
import android.app.Activity;
import android.os.Bundle;
/*import UI stuff*/
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;	//UI update from other threads
import android.os.Message;	//Message between threads
import android.os.Bundle;	//Data between threads
import android.os.Vibrator;
import timo.home.activityMonitor.imuCaptureService.*;	//imu capture service
import android.os.Environment;
import android.widget.LinearLayout;
//import android.util.Log;	//Debugging
//Service calling
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
//Plotting
import timo.home.activityMonitor.graphicsView.*;
//Reading a file
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
//Timer sliders
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.os.Build;
import android.app.Dialog;
import java.text.DateFormat;

//Constants for Broadcast
import timo.home.activityMonitor.utils.Constants;

//Reading activity file
import timo.home.activityMonitor.utils.ActivityDataReader;

//Save settings from session to the next
import android.content.SharedPreferences;

//Requesting file write permission
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;

//Save screenshot
import java.io.FileOutputStream;	//Output stream to write to a file
import android.graphics.Bitmap;
import android.graphics.Canvas;		//Canvas for drawing the data on screen

public class ActivityReminder extends FragmentActivity/*Activity*/{
	private static final String TAG = "ActivityReminder";
	
	// Requesting permission to WRITE a file
	 private static final int REQUEST_FILE_WRITING_PERMISSION = 201;	 
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};	

	Button remindStartButton;
	Button remindStopButton;
	Button stopButton;
	Button historyButton;
	//TextView startTime;
	//TextView stopTime;
	//TextView timerDuration;
	Button durationButton;
	Button buzzButton;
	ToggleButton timerToggle;
	Boolean started = false;
	
	private LinearLayout rawLayout;
	private LinearLayout madLayout;
	private LinearLayout barLayout;
	
	private GraphicsSurfaceView graphicsView;
	private GraphicsSurfaceBarView graphicsViewHistogram;
	private GraphicsSurfaceRawView graphicsViewRaw;
	
	//Settings
	boolean timerOn = false;
	int durationH = 0;
	int durationM = 30;
	int startH = 8;
	int startM = 0;
	int stopH = 17;
	int stopM = 0;
		
	
	     /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        
        //Load settings
        loadSettings();
 
        /*ADD UI STUFF*/
        remindStartButton= (Button) findViewById(R.id.mStartTime);
        remindStartButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	DialogFragment newFragment = new TimePickerFragmentStart();
				 		newFragment.show(getSupportFragmentManager(), "Set Reminder Start Time");
				  	}
        		}
        );
        remindStopButton= (Button) findViewById(R.id.mStopTime);
        remindStopButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	DialogFragment newFragment = new TimePickerFragmentStop();
				 		newFragment.show(getSupportFragmentManager(), "Set Reminder Stop Time");
				  	}
        		}
        );
        stopButton = (Button) findViewById(R.id.mStopButton);
        stopButton.setOnClickListener(stopListener);
        
        historyButton = (Button) findViewById(R.id.mHistory);
        historyButton.setOnClickListener(historyListener);
        
        //startTime =(TextView) findViewById(R.id.mStartTimeInfo);
			//stopTime	=(TextView) findViewById(R.id.mStopTimeInfo);
			//timerDuration	=(TextView) findViewById(R.id.mTimerDuration);

			durationButton=(Button) findViewById(R.id.mTimerButton);
			durationButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	DialogFragment newFragment = new TimePickerFragmentDuration();
				 		newFragment.show(getSupportFragmentManager(), "Set Inactivity Timer Duration");
				  	}
        		}
        );
        
        			
			//Update texts
			//startTime.setText(new String(String.format("%02d",startH)+":"+String.format("%02d",startM)));
			//stopTime.setText(new String(String.format("%02d",stopH)+":"+String.format("%02d",stopM)));
			//timerDuration.setText(new String("Inactivity timer "+(60*durationH+durationM)+" min"));
			
			remindStartButton.setText(new String(Constants.remindStartButton+String.format("%02d",startH)+":"+String.format("%02d",startM)));
        	remindStopButton.setText(new String(Constants.remindStopButton+String.format("%02d",stopH)+":"+String.format("%02d",stopM)));
        	durationButton.setText(new String(Constants.remindDuration+(60*durationH+durationM)+" min"));
        	
			timerToggle = (ToggleButton) findViewById(R.id.mRemindToggle);
			timerToggle.setChecked(timerOn);
			timerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				 		timerOn = isChecked;
					  if (isChecked) {
						   // The toggle is enabled
						   Intent new_intent = new Intent();
						  new_intent.setAction(Constants.TIMER_ON);
						  new_intent.putExtra(Constants.TIMER_ON,true);
						  sendBroadcast(new_intent);
					  } else {
						   // The toggle is disabled
						   Intent new_intent = new Intent();
						  new_intent.setAction(Constants.TIMER_OFF);
						  new_intent.putExtra(Constants.TIMER_ON,false);
						  sendBroadcast(new_intent);
					  }
				 }
			});

			rawLayout = (LinearLayout) findViewById(R.id.rawView);	//GEt the activityPlot
			madLayout = (LinearLayout) findViewById(R.id.activityPlot);	//GEt the activityPlot
			barLayout = (LinearLayout) findViewById(R.id.activityHistogram);	//GEt the activityPlot
			
			graphicsView = new GraphicsSurfaceView(this);
			madLayout.addView(graphicsView);
			
			
			graphicsViewHistogram = new GraphicsSurfaceBarView(this);
			barLayout.addView(graphicsViewHistogram);
			graphicsViewRaw =  new GraphicsSurfaceRawView(this);
			rawLayout.addView(graphicsViewRaw);
			//BROADCASTSERVICE register the receiver
        if (activityReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_COMMUNICATION_TO_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter(Constants.SERVICE_TO_ACTIVITY);
				intentFilter.addAction(Constants.TOAST);
				intentFilter.addAction(Constants.TIMER_ON);
				intentFilter.addAction(Constants.TIMER_OFF);
				intentFilter.addAction(Constants.TIMER_START);
				intentFilter.addAction(Constants.TIMER_STOP);
				intentFilter.addAction(Constants.TIMER_DURATION);
            //Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }
        
        
        	 //Check power savings white list
			 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName())){
				//Request ignoring battery optimizations
				 //timo.home.activityMonitor
				 startActivityForResult(
					 //new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + getPackageName()))
					 new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:timo.home.activityMonitor"))
					 ,Constants.REQUEST_DISABLE_BATTERY);//, Uri.parse("package:"+getPackageName())));
				 
				 
				 Toast.makeText(this, R.string.disable_battery_optimisation, Toast.LENGTH_SHORT).show();
				 finish();	//The user has to re-start the app with GPS enabled
			 }

			//Request file write permission, start service after requiring permission in the callback
     		ActivityCompat.requestPermissions(this, permissions, REQUEST_FILE_WRITING_PERMISSION);
    

			
		
    }
    
    //Request file writing permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.e(TAG,"oneRequestPermission result");
        switch (requestCode){
            case REQUEST_FILE_WRITING_PERMISSION:
                permissionToWriteAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!permissionToWriteAccepted ){
                	Toast.makeText(this, R.string.write_permission_required, Toast.LENGTH_SHORT).show();
                	finish();
             	}else{
             		startReminderService();
             	}
                break;

        }
    }
    
    
    //Start service once granted file writing permission
    private void startReminderService(){
    	///START AS FOREGROUND SERVICE
			Intent intent = new Intent(ActivityReminder.this, ImuCaptureService.class);
         intent = intent.setAction(Constants.START_SERVICE);
         ContextCompat.startForegroundService(this,intent);
    }
	
	
	
	//STEP1: Create a broadcast receiver
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        		if (intent.getAction().equals(Constants.TIMER_ON) || intent.getAction().equals(Constants.TIMER_OFF)){
			 		//Update prefs
			 		timerOn = intent.getBooleanExtra(Constants.TIMER_ON,false);
			 		//Log.d(TAG,"Got toggle intent "+Boolean.toString(timerOn));
			 		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
			 		//if (timerOn != sp.getBoolean(Constants.TIMER_ON,false)){
		 			updatePrefs(Constants.TIMER_ON,timerOn);
					//In case the broadcast came from ImuCaptureService
		 			timerToggle.setChecked(timerOn);
			 		//}
        		}
        		if (intent.getAction().equals(Constants.TIMER_START)){
			 		//Update prefs
			 		startH = intent.getIntExtra("H",8);
			 		startM = intent.getIntExtra("min",0);
			 		updatePrefs(Constants.TIMER_START_H,startH);
			 		updatePrefs(Constants.TIMER_START_M,startM);
        			//Set the start time
					//startTime.setText(new String(String.format("%02d",startH)+":"+String.format("%02d",startM)));
					remindStartButton.setText(new String(Constants.remindStartButton+String.format("%02d",startH)+":"+String.format("%02d",startM)));				
				}
				if (intent.getAction().equals(Constants.TIMER_STOP)){
					//Update prefs
			 		stopH = intent.getIntExtra("H",17);
			 		stopM = intent.getIntExtra("min",0);
			 		updatePrefs(Constants.TIMER_STOP_H,stopH);
			 		updatePrefs(Constants.TIMER_STOP_M,stopM);
					//Set the stop time
					//stopTime.setText(new String(String.format("%02d",stopH)+":"+String.format("%02d",stopM)));
					remindStopButton.setText(new String(Constants.remindStopButton+String.format("%02d",stopH)+":"+String.format("%02d",stopM)));
				}
				if (intent.getAction().equals(Constants.TIMER_DURATION)){
					//Update prefs
			 		durationH = intent.getIntExtra("H",0);
			 		durationM = intent.getIntExtra("min",30);
			 		updatePrefs(Constants.TIMER_DURATION_H,durationH);
			 		updatePrefs(Constants.TIMER_DURATION_M,durationM);
					//Set the stop time
					int tempDuration = 60*durationH+durationM;
					//timerDuration.setText(new String("Inactivity timer "+tempDuration+" min"));
					durationButton.setText(new String(Constants.remindDuration+(60*durationH+durationM)+" min"));
				}
				if (intent.getAction().equals(Constants.TOAST)){
        			//Toast.makeText(getApplicationContext(), intent.getStringExtra("Toast"), Toast.LENGTH_SHORT).show();
        		}
        
        		if (intent.getAction().equals(Constants.SERVICE_TO_ACTIVITY)){
					double mad = intent.getDoubleExtra("MAD",0d);
					String date = intent.getStringExtra("Date");
					float[] rawData = intent.getFloatArrayExtra("Raw");          
		         //Toast.makeText(getApplicationContext(), "MAD "+mad, Toast.LENGTH_SHORT).show();
		         ActivityDataReader adr = new ActivityDataReader("MaDs_"+date+".txt");
					//tempBar = new float[]{10000f,5000f,3000f,1500f,1000f};
					graphicsView.setTimes(adr.times);
					graphicsView.updateData(adr.tempData);
					graphicsViewHistogram.setActivity(new int[]{(int)(adr.tempBar[0]*5f/60f),(int)(adr.tempBar[1]*5f/60f),(int)((adr.tempBar[2]+adr.tempBar[3])*5f/60f)});
					graphicsViewHistogram.updateData(adr.tempBar);
					graphicsViewRaw.setMad((float) mad);
					graphicsViewRaw.setSRate((float) ((double)rawData.length)/5f);
					graphicsViewRaw.updateData(rawData);
		     }
        }
    };
    
    /*TimePickers*/
    public static class TimePickerFragmentStart extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 		SharedPreferences sp = getActivity().getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
 		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, sp.getInt(Constants.TIMER_START_H,8),sp.getInt(Constants.TIMER_START_M,0),true);
		 }
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     //Broadcast to service and activity
		     Intent new_intent = new Intent();
			  new_intent.setAction(Constants.TIMER_START);
			  new_intent.putExtra("H",hourOfDay);
				new_intent.putExtra("min",minute);
			  getActivity().sendBroadcast(new_intent);
		 }
	}
   
   public static class TimePickerFragmentStop extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
			 SharedPreferences sp = getActivity().getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, sp.getInt(Constants.TIMER_STOP_H,17),sp.getInt(Constants.TIMER_STOP_M,0),true);
		 }
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     //Broadcast to service and activity
		     Intent new_intent = new Intent();
			  new_intent.setAction(Constants.TIMER_STOP);
			  new_intent.putExtra("H",hourOfDay);
				new_intent.putExtra("min",minute);
			  getActivity().sendBroadcast(new_intent);
		 }
	}
	   public static class TimePickerFragmentDuration extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
			SharedPreferences sp = getActivity().getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, sp.getInt(Constants.TIMER_DURATION_H,0),sp.getInt(Constants.TIMER_DURATION_M,30),true);
		 }
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		 		//Broadcast to service and activity
		     Intent new_intent = new Intent();
			  new_intent.setAction(Constants.TIMER_DURATION);
			  new_intent.putExtra("H",hourOfDay);
				new_intent.putExtra("min",minute);
			  getActivity().sendBroadcast(new_intent);
		 }
	}
	
	private void updatePrefs(String key,boolean value){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		SharedPreferences.Editor se = sp.edit();
		se.putBoolean(key,value);
		se.commit();
	}
	
	private void updatePrefs(String key,int value){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		SharedPreferences.Editor se = sp.edit();
		se.putInt(key,value);
		se.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
        timerOn = sp.getBoolean(Constants.TIMER_ON,false);
        durationH = sp.getInt(Constants.TIMER_DURATION_H,0);
        durationM = sp.getInt(Constants.TIMER_DURATION_M,30);
        startH = sp.getInt(Constants.TIMER_START_H,8);
        startM = sp.getInt(Constants.TIMER_START_M,0);
        stopH = sp.getInt(Constants.TIMER_STOP_H,17);
        stopM = sp.getInt(Constants.TIMER_STOP_M,0);
	}
	

	
	
	View.OnClickListener stopListener = new View.OnClickListener() {
		public void onClick(View v) {
			//textView.setText(new String("ClickedStop"));
			//infoLabel.setText(new String("Stopped"));
		   //Try to stop the ImuCaptureService
        	Intent intent = new Intent(ActivityReminder.this, ImuCaptureService.class);
        	intent = intent.setAction(Constants.STOP_SERVICE);
 			startService(intent);
			finish();	//Shutdown the program
			//this.finishAffinity();
		}

	};
	
	private Bitmap appendBitmap(Bitmap top, Bitmap bottom){
		Bitmap combined = null; 
		int width = top.getWidth();
		int height = top.getHeight()+bottom.getHeight(); 
		combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 
		Canvas comboImage = new Canvas(combined); 
		comboImage.drawBitmap(top, 0f, 0f, null); 
		comboImage.drawBitmap(bottom, 0f, top.getHeight(), null); 
		return combined; 
	}
	
	//Take a screenshot
	public String takeScreenShot(){
		 try {
		 		File externalStorageDir = new File(Environment.getExternalStorageDirectory(), Constants.activityFileFolder);
		 		String fileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				if (!externalStorageDir.exists()) {
					 if (!externalStorageDir.mkdirs()) {
						  //Log.d(TAG, "failed to create directory");
					 }
				}
				
				/*
				graphicsView;
	private GraphicsSurfaceBarView graphicsViewHistogram;
	private GraphicsSurfaceRawView graphicsViewRaw;
				*/
		     // create bitmap screen capture
/*
		     View v1 = getWindow().getDecorView().getRootView();
		     v1.setDrawingCacheEnabled(true);
		     Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		     v1.setDrawingCacheEnabled(false);
*/

				
		     //graphicsViewHistogram.setDrawingCacheEnabled(true);
		     Bitmap bitmapTop = graphicsViewRaw.getScreenShot();
		     Bitmap bitmapMiddle = graphicsView.getScreenShot();
		     Bitmap bitmapBottom = graphicsViewHistogram.getScreenShot();
		     Bitmap screenShotCombined = appendBitmap(bitmapTop,bitmapMiddle);
		     screenShotCombined = appendBitmap(screenShotCombined,bitmapBottom);
		     //Stitch bitmaps together
		     
		     //graphicsViewHistogram.setDrawingCacheEnabled(false);

			
		     File imageFile = new File(externalStorageDir,fileName+".png");

		     FileOutputStream outputStream = new FileOutputStream(imageFile);
		     int quality = 100;
		     screenShotCombined.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
		     outputStream.flush();
		     outputStream.close();
		     return imageFile.toString();
		 } catch (Exception e){
		 	//Log.d(TAG,"Couldn't save screenshot "+e.toString());
		 	return new String("Could not save screenshot");
		 }
		 
	}
	
	//fire up the history browsing activity
	View.OnClickListener historyListener = new View.OnClickListener() {
		public void onClick(View v) {
			//historyButton.setText(new String("Has been clicked"));
//			String fName = takeScreenShot();
//			Toast.makeText(getApplicationContext(), fName, Toast.LENGTH_SHORT).show();

		   //Try to Launch HistoryVisualiser
        	Intent intent = new Intent(ActivityReminder.this, HistoryVisualiser.class);
        	startActivity(intent);
			
		}

	};
	/*Power saving*/
     protected void onResume() {
     		super.onResume();
     }

     protected void onPause() {
      		super.onPause();
     }
	
	protected void onDestroy(){
		//Log.d(TAG, " onDestroy");
		//BROADCAST SERVICE Unregister the receiver
      unregisterReceiver(activityReceiver);
      super.onDestroy();
	}
    
}
