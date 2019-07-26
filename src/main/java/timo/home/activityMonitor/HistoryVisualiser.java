package timo.home.activityMonitor;
/*Debugging adb logcat Alku:D tsw:D imu:D  *:S*/
import android.app.Activity;
import android.os.Bundle;
/*import UI stuff*/
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Environment;
import android.widget.LinearLayout;
//import android.util.Log;	//Debugging
//Service calling
import android.content.Intent;
import android.content.Context;
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
//Timer pickers
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import java.text.DateFormat;

//File listing
import java.io.FilenameFilter;

//Constants for Broadcast
import timo.home.activityMonitor.utils.Constants;
import timo.home.activityMonitor.utils.ActivityDataReader;
import timo.home.activityMonitor.utils.ActivityInterval;

//Save settings from session to the next
import android.content.SharedPreferences;

//SurfaceView
import android.view.SurfaceHolder;	//Holder to obtain the canvas
import android.view.SurfaceView;

//Context
//import timo.home.activityMonitor.R;	//Import R from this project...

public class HistoryVisualiser extends FragmentActivity {
	private static final String TAG = "HistoryVisualiser";

	Button dayButton;
	Button dayStopButton;
	Button dayStartButton;
	Button historyButton;

	private LinearLayout madLayout;
	private LinearLayout barLayout;
	
	private GraphicsSurfaceHistoryView graphicsView;
	private GraphicsSurfaceBarHistoryView graphicsViewHistogram;
	ArrayList<ActivityDataReader> historyData = null;
	ArrayList<ActivityInterval> historyInterval = null;
	private boolean gViewReady = false;
	private boolean ghViewReady = false;
	
	int dayStartH = 8;
	int dayStartM = 0;
	int dayStopH = 17;
	int dayStopM = 0;
	int surfacesCreated = 0;
	
	     /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
                
        //Load settings
        loadSettings();
 
        /*ADD UI STUFF*/
        dayStartButton= (Button) findViewById(R.id.mDayStartTime);
        dayStartButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	DialogFragment newFragment = new TimePickerFragmentStartDay();
				 		newFragment.show(getSupportFragmentManager(), "Set Visualisation Start Time");
				  	}
        		}
        );
        
        dayStopButton= (Button) findViewById(R.id.mDayStopTime);
        dayStopButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	DialogFragment newFragment = new TimePickerFragmentStopDay();
				 		newFragment.show(getSupportFragmentManager(), "Set Visualisation Stop Time");
				  	}
        		}
        );
        
        dayButton = (Button) findViewById(R.id.mDaysButton);
        dayButton.setOnClickListener(dayListener);
        
			//Update texts
	
			dayStartButton.setText(new String(getString(R.string.mDayStartTime)+" "+String.format("%02d",dayStartH)+":"+String.format("%02d",dayStartM)));
        	dayStopButton.setText(new String(getString(R.string.mDayStopTime)+" "+String.format("%02d",dayStopH)+":"+String.format("%02d",dayStopM)));

			//Graphics SurfaceViews
			madLayout = (LinearLayout) findViewById(R.id.activityPlotDay);	//GEt the activityPlot
			barLayout = (LinearLayout) findViewById(R.id.activityHistogramDay);	//GEt the activityPlot
			
			graphicsView = new GraphicsSurfaceHistoryView(this);
			madLayout.addView(graphicsView);
			
			
			
			graphicsViewHistogram = new GraphicsSurfaceBarHistoryView(this);
			barLayout.addView(graphicsViewHistogram);
					
			//Use anonymous SurfaceHolder.Callback		
			graphicsView.getHolder().addCallback(new SurfaceHolder.Callback(){
					public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {}
        			public void surfaceDestroyed(SurfaceHolder holder) {gViewReady = false;}
        			public void surfaceCreated(SurfaceHolder holder) {
        				gViewReady = true;
        				//Log.d(TAG,"GraphicsView surfaceCreated");
        				
					 	//if(historyData != null){
						//	graphicsView.updateData(historyData);
						//}
						
						if (historyInterval != null){
							//graphicsView.updateData(historyData);
							graphicsView.updateData(historyInterval);
						}
						
        			}
				}
			);
					
			graphicsViewHistogram.getHolder().addCallback(new SurfaceHolder.Callback(){
					public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {}
        			public void surfaceDestroyed(SurfaceHolder holder) {ghViewReady = false;}
        			public void surfaceCreated(SurfaceHolder holder) {
        				ghViewReady = true;
        				//Log.d(TAG,"graphicsViewHistogram surfaceCreated");
        				
						if (historyInterval != null){
							graphicsViewHistogram.updateData(historyInterval);
						}        				
        				
						//float[] tempBar = new float[]{10000f,5000f,3000f,1500f,1000f};
						//graphicsViewHistogram.setActivity(new int[]{(int)(tempBar[0]*5f/60f),(int)(tempBar[1]*5f/60f),(int)((tempBar[2]+tempBar[3])*5f/60f)});
						//graphicsViewHistogram.updateData(tempBar);
        			}
				}
			
			
			);
		
						
						
						
						
						
			//BROADCASTSERVICE register the receiver
        if (dayReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(Constants.DAY_START);
				intentFilter.addAction(Constants.DAY_STOP);
            //Map the intent filter to the receiver
            registerReceiver(dayReceiver, intentFilter);
        }
			
		
    }
	
    
    /*TimePickers*/
    public static class TimePickerFragmentStartDay extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 		SharedPreferences sp = getActivity().getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
 		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, sp.getInt(Constants.DAY_START_H,8),sp.getInt(Constants.DAY_START_M,0),true);
		 }
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     //Broadcast to service and activity
		     Intent new_intent = new Intent();
			  new_intent.setAction(Constants.DAY_START);
			  new_intent.putExtra("H",hourOfDay);
				new_intent.putExtra("min",minute);
			  getActivity().sendBroadcast(new_intent);
		 }
	}
   
   public static class TimePickerFragmentStopDay extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
			 SharedPreferences sp = getActivity().getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, sp.getInt(Constants.DAY_STOP_H,17),sp.getInt(Constants.DAY_STOP_M,0),true);
		 }
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     //Broadcast to service and activity
		     Intent new_intent = new Intent();
			  new_intent.setAction(Constants.DAY_STOP);
			  new_intent.putExtra("H",hourOfDay);
				new_intent.putExtra("min",minute);
			  getActivity().sendBroadcast(new_intent);
		 }
	}

	private BroadcastReceiver dayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        		if (intent.getAction().equals(Constants.DAY_START)){
			 		//Update prefs
			 		dayStartH = intent.getIntExtra("H",8);
			 		dayStartM = intent.getIntExtra("min",0);
			 		updatePrefs(Constants.DAY_START_H,dayStartH);
			 		updatePrefs(Constants.DAY_START_M,dayStartM);
			 		updateInterval();
			 		if (gViewReady && historyInterval != null){
						//graphicsView.updateData(historyData);
						graphicsView.updateData(historyInterval);
					}
					if (ghViewReady && historyInterval != null){
						//graphicsView.updateData(historyData);
						graphicsViewHistogram.updateData(historyInterval);
					}
        			//Set the start time
					dayStartButton.setText(new String(getString(R.string.mDayStartTime)+" "+String.format("%02d",dayStartH)+":"+String.format("%02d",dayStartM)));				
				}
				if (intent.getAction().equals(Constants.DAY_STOP)){
					//Update prefs
			 		dayStopH = intent.getIntExtra("H",17);
			 		dayStopM = intent.getIntExtra("min",0);
			 		updatePrefs(Constants.DAY_STOP_H,dayStopH);
			 		updatePrefs(Constants.DAY_STOP_M,dayStopM);
			 		updateInterval();
			 		if (gViewReady && historyInterval != null){
						//graphicsView.updateData(historyData);
						graphicsView.updateData(historyInterval);
					}
					if (ghViewReady && historyInterval != null){
						//graphicsView.updateData(historyData);
						graphicsViewHistogram.updateData(historyInterval);
					}
					//Set the stop time
					dayStopButton.setText(new String(getString(R.string.mDayStopTime)+" "+String.format("%02d",dayStopH)+":"+String.format("%02d",dayStopM)));
				}
		}
	};


	
	private void updatePrefs(String key,int value){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
		SharedPreferences.Editor se = sp.edit();
		se.putInt(key,value);
		se.commit();
	}
	
	private void loadSettings(){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
        dayStartH = sp.getInt(Constants.DAY_START_H,7);
        dayStartM = sp.getInt(Constants.DAY_START_M,50);
        dayStopH = sp.getInt(Constants.DAY_STOP_H,16);
        dayStopM = sp.getInt(Constants.DAY_STOP_M,30);
	}
	
	
	//Listener for day picker button
	View.OnClickListener dayListener = new View.OnClickListener() {
		public void onClick(View v) {
				//Launch listview
				//////Log.d(TAG,"Try starting FileSelectorList");
				Intent intent = new Intent(HistoryVisualiser.this, FileSelectorList.class);
        		startActivityForResult(intent,Constants.FLISTSTARTINTENT);
        		//////Log.d(TAG,"Sent intent");
        		//startActivity(intent);
		}

	};
	
	public void updateInterval(){
		if (historyData != null){
			historyInterval = new ArrayList<ActivityInterval>();
			for (int i =0;i<historyData.size();++i){
				historyInterval.add(new ActivityInterval(historyData.get(i),dayStartH,dayStartM,dayStopH,dayStopM));
			}			
		}
	}
	
	//Listen to activity result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Filter for correct activity
		if (requestCode == Constants.FLISTSTARTINTENT){
			if (resultCode == RESULT_OK){
				////Log.d(TAG,"Got result");
				String[] fileNames = data.getStringArrayExtra(Constants.FNAMES);
				//dayButton.setText(fileNames[0]);
				//Read the data files here, and plot the data
				historyData = new ArrayList<ActivityDataReader>();
				for (int i =0;i<fileNames.length;++i){
					historyData.add(new ActivityDataReader(fileNames[i]));
				}
				//Log.d(TAG,"Got "+historyData.size()+" files");
				updateInterval();
				/*
				while (graphicsView.getHolder().isCreating()){
					try{
						Thread.sleep(1l);
						//Log.d(TAG,"Waiting for graphicsView");
					}catch(Exception err){}
				}
				*/		
				if (gViewReady){
					//graphicsView.updateData(historyData);
					graphicsView.updateData(historyInterval);
				}
				if (ghViewReady && historyInterval != null){
					//graphicsView.updateData(historyData);
					graphicsViewHistogram.updateData(historyInterval);
				}
				//dayButton.setText("OK");
			}else{
				dayButton.setText("Cancelled");
			}
		
		}
	}
	
	
	/*Power saving*/
     protected void onResume() {
     		super.onResume();
     }

     protected void onPause() {
     		super.onPause();
     }
	
	protected void onDestroy(){
		//BROADCAST SERVICE Unregister the receiver
      unregisterReceiver(dayReceiver);
      super.onDestroy();
	}
   


	

   
}
