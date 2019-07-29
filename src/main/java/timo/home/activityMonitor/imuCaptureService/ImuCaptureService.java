package timo.home.activityMonitor.imuCaptureService;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.app.Service;
import android.os.PowerManager;
import android.content.Intent;
import android.content.Context;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.io.File;	//For saving results to a file
import java.io.FileOutputStream;	//Output stream to write to a file
import java.io.OutputStreamWriter;	//Buffered output stream
import android.os.Environment;

//import android.util.Log;	//Debugging
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.app.Notification;
//import android.app.Notification.Action;
//import android.app.Notification.Action.Builder;
import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationCompat.Builder;
//import android.support.v4.app.NotificationCompat.Action.Builder;
import timo.home.activityMonitor.ActivityReminder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.PendingIntent;
import timo.home.activityMonitor.R;	//Import R from this project...
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;

//Constants for Broadcast
import timo.home.activityMonitor.utils.Constants;

//Save settings from session to the next
import android.content.SharedPreferences;

//Vibrate reminder
import android.os.Vibrator;

//Internationalisation
import java.util.Locale;

public class ImuCaptureService extends Service{
	private static final String TAG = "ImuCaptureService";
	private SensorManager sensorManager;
	private Sensor accel;
	private PowerManager.WakeLock wl;
	private ImuCaptureListener imuCaptureListener;
	private Thread listenerThread;
	private int maxRate;
	private static final String[] descriptors = {"accelerometer"};
   private NotificationCompat.Builder nBuilder = null;
   //Settings
	boolean timerOn = false;
	int durationH = 0;
	int durationM = 30;
	int startH = 8;
	int startM = 0;
	int stopH = 17;
	int stopM = 0;
    
    	//STEP1: Create a broadcast receiver
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        		//////Log.d(TAG,"Received Intent "+intent.getAction());
				//Determine the correct action here
				if (intent.getAction().equals(Constants.TIMER_ON) || intent.getAction().equals(Constants.TIMER_OFF)){
			      timerOn = intent.getBooleanExtra(Constants.TIMER_ON,false);
			      imuCaptureListener.setTimerOn(timerOn);
			      //////Log.d(TAG,"Got toggle intent "+Boolean.toString(timerOn));
			      //Update preferences accordingly
					SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
					SharedPreferences.Editor se = sp.edit();
					se.putBoolean(Constants.TIMER_ON,timerOn);
					se.commit();
					
					//BROADCAST TOAST FOR DEBUGGIG
					//Intent new_intent = new Intent();
				  //new_intent.setAction(Constants.TOAST);
				  //sendBroadcast(new_intent);
					
				}

				if (intent.getAction().equals(Constants.TIMER_DURATION)){
					durationH = intent.getIntExtra("H",0);
			 		durationM = intent.getIntExtra("min",30);
			 		imuCaptureListener.setTimerMins(60*durationH+durationM);
				
				}
				if (intent.getAction().equals(Constants.TIMER_START)){
					startH = intent.getIntExtra("H",8);
			 		startM = intent.getIntExtra("min",0);
			 		imuCaptureListener.setStartH(startH);
			 		imuCaptureListener.setStartM(startM);
			 		imuCaptureListener.updateStartMillis();
				}
				if (intent.getAction().equals(Constants.TIMER_STOP)){
			 		stopH = intent.getIntExtra("H",17);
			 		stopM = intent.getIntExtra("min",0);
			 		imuCaptureListener.setStopH(stopH);
			 		imuCaptureListener.setStopM(stopM);
			 		imuCaptureListener.updateStopMillis();
				}
				
        }
    };
    
    
	
	/*Service to log imu (accelerometer because I don't have a gyro on my phone...)*/
	public void onCreate(){
		super.onCreate();
		getWakeLock();
		sensorManager=(SensorManager)this.getSystemService(this.SENSOR_SERVICE);
		accel =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		maxRate = (int) Math.ceil(1000000d/accel.getMinDelay());
		getSettings();
		imuCaptureListener = new ImuCaptureListener(maxRate,System.currentTimeMillis(),timerOn,startH,startM,stopH,stopM,durationH*60+durationM);
		//listenerThread = new Thread(imuCaptureListener);
		sensorManager.registerListener(imuCaptureListener, 
			accel,10000 /*10000 us per sample -> 100 Hz*/
			); //SensorManager.SENSOR_DELAY_FASTEST),SENSOR_DELAY_UI
			//BROADCASTSERVICE register the receiver
        if (serviceReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_COMMUNICATION_TO_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter(Constants.TIMER_ON);
				intentFilter.addAction(Constants.TIMER_OFF);
				intentFilter.addAction(Constants.TIMER_DURATION);
				intentFilter.addAction(Constants.TIMER_START);
				intentFilter.addAction(Constants.TIMER_STOP);
            //Map the intent filter to the receiver
            registerReceiver(serviceReceiver, intentFilter);
        }
			
			
	}

	//Settings need to be loaded only once, update when we get notifications...
	private void getSettings(){
		SharedPreferences sp = getSharedPreferences(Constants.APP_CLASS,Context.MODE_PRIVATE);
        timerOn = sp.getBoolean(Constants.TIMER_ON,false);
        durationH = sp.getInt(Constants.TIMER_DURATION_H,0);
        durationM = sp.getInt(Constants.TIMER_DURATION_M,30);
        startH = sp.getInt(Constants.TIMER_START_H,8);
        startM = sp.getInt(Constants.TIMER_START_M,0);
        stopH = sp.getInt(Constants.TIMER_STOP_H,17);
        stopM = sp.getInt(Constants.TIMER_STOP_M,0);
	}
		
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    		super.onStartCommand(intent, flags , startId);
    		    		
        if (intent.getAction().equals(Constants.STOP_SERVICE)){
		     	stopForeground(true);
		     	stopSelf();	//Call this to stop the service
        }
        if(intent.getAction().equals(Constants.START_SERVICE)){
        	showNotification();
        }
        return START_STICKY; // If we get killed, after returning from here, restart
    }
    
    
    //For notification
     private void showNotification() {
     		createNotificationChannel();	//Required for Android 9 and above
        Intent notificationIntent = new Intent(this, ActivityReminder.class);
        notificationIntent.setAction("ActivityReminderNotification");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         //Launch ActivityReminder as the default action
          Intent arIntent = new Intent(this, ActivityReminder.class);
        PendingIntent parIntent = PendingIntent.getActivity(this, 0,
                arIntent, 0);      
 			//Enable shutting down the monitor from taskbar
 			Intent closeIntent = new Intent(this, ImuCaptureService.class);
 			closeIntent.setAction(Constants.STOP_SERVICE);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
                
         
 			//Enable turning reminder on from taskbar
 			//Intent onIntent = new Intent(this, ImuCaptureService.class);
 			Intent onIntent = new Intent();
 			onIntent.setAction(Constants.TIMER_ON);
 			onIntent.putExtra(Constants.TIMER_ON,true);
        PendingIntent pOnIntent = PendingIntent.getBroadcast(this, 0,
                onIntent, 0);
         //Enable turning reminder off from taskbar
 			Intent offIntent = new Intent();
 			offIntent.setAction(Constants.TIMER_OFF);
 			offIntent.putExtra(Constants.TIMER_OFF,false);
        PendingIntent pOffIntent = PendingIntent.getBroadcast(this, 0,
                offIntent, 0);
           

  
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);
 		/*
        Notification notification = new Notification.Builder(this)
                .setContentTitle("ActivityMonitor")
                .setTicker("ActivityMonitor")
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true)
                .addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next, "Close Monitor",pcloseIntent).build())
                .build();
                */
                
                //.setContentIntent(parIntent);	//Launch activity monitor
         nBuilder = new NotificationCompat.Builder(this,Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Activity Reminder")
                .setContentIntent(parIntent)
                .setTicker("Activity Reminder")
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_close, "Close",pcloseIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_remindon, "On",pOnIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_remindoff, "Off",pOffIntent).build());
			//Start the service in foreground
        startForeground(Constants.FG_NOTIFICATION_INTENT,
                nBuilder.build());
 
    }

	//Create a nofitication channel. Required on SDK 9 or higher
	private void createNotificationChannel(){
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         NotificationChannel serviceChannel = new NotificationChannel(
                 Constants.NOTIFICATION_CHANNEL_ID,
                 getResources().getString(R.string.app_name),
                 NotificationManager.IMPORTANCE_DEFAULT
         );
			serviceChannel.setSound( null, null );	//Turn of notification sound
         NotificationManager manager = getSystemService(NotificationManager.class);
         manager.createNotificationChannel(serviceChannel);
     }
	}

    @Override
    public void onDestroy() {
        //////Log.d(TAG, "[SERVICE] onDestroy");
        //stop();
        sensorManager.unregisterListener(imuCaptureListener);	// Unregister listener.
        wl.release();	//Release wakelock
        //stopSelf(stopId);	//Call this just in case to stop the service
        //BROADCAST Unregister the receiver
        unregisterReceiver(serviceReceiver);
        super.onDestroy();
    }
	
	
	/*Listener class, listen to the service handle calculations in a separate thread*/
	public class ImuCaptureListener implements SensorEventListener{//, Runnable{
	
		int maxRate;			//Maximum sampling rate for accelerations given by the operating system
		int epochLength = 5; //Epoch lenght in s
		float[] resultant;
		long[] timeStamps;
		int pointer;
		long refMillis;
		long sensorRef = -1;
		long lastMilliRef = -1;
		public long lastTimerMilliRef = -1;	//Timestamp of when timer was last reset
		public int countAboveThresh = 0;
		public long timerDuration = -1;
		
		boolean timerOn = false;
		int startH = 8;
		int startM = 0;
		public long startMillis;
		int stopH = 17;
		int stopM = 0;
		public long stopMillis;
		double g = 9.81;
		boolean progressBarOn = false;
		
		int id = 102;
		public ImuCaptureListener(int maxRate, long refMillis, boolean timerOn, int startH, int startM, int stopH,int stopM, int duration){
			this.maxRate = maxRate;
			this.refMillis = refMillis;
			this.timerOn = timerOn;
			this.startH  = startH;
			this.startM  = startM;
			this.stopH  = stopH;
			this.stopM  = stopM;
			this.timerDuration = (long) duration;
			
			resultant = new float[(epochLength*2)*maxRate];	//Maximum number of samples to be expected in epoch duration
			timeStamps = new long[(epochLength*2)*maxRate];	//corresponding timestamps
			pointer = 0;
			lastTimerMilliRef = refMillis;
			lastMilliRef = refMillis;
			//Calculate the millisecond of day
			updateStartMillis();
			updateStopMillis();
 
		}
		
		//public void run(){/*Do nothing, just keep listening*/}
		
		public void setTimerMins(long d){
			timerDuration = d;
		}
		
		public void setTimerOn(boolean b){
			timerOn = b;
			lastTimerMilliRef = lastMilliRef; 
			countAboveThresh = 0;
			if (timerOn == false){
				//Remove the notification bar
				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         	nBuilder.setProgress(0,0,false);
         	nm.notify(id, nBuilder.build());
         	progressBarOn  = false;
			}else{
				//Add the notification bar
				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         	nBuilder.setProgress(100,0,false);
         	nm.notify(id, nBuilder.build());
         	progressBarOn = true;
			}
		}
		
		public void setStartH(int startH){
			this.startH = startH;
		}
		public void setStartM(int startM){
			this.startM = startM;
		}
		public void setStopH(int stopH){
			this.stopH = stopH;
		}
		public void setStopM(int stopM){
			this.stopM = stopM;
		}
		
		public void updateStartMillis(){
			startMillis = ((long) startH)*60l*60l*1000l+((long) startM)*60l*1000l;
		}
		
		public void updateStopMillis(){
			stopMillis = ((long) stopH)*60l*60l*1000l+((long) stopM)*60l*1000l;
		}
		
		public void onAccuracyChanged(Sensor sensor,int accuracy){/*Do nothing*/}
	
		public void onSensorChanged(SensorEvent event){
			// accelerometer
			if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
				//Init sensorRef
				if (sensorRef == -1){
					sensorRef = event.timestamp;
				}
			
				resultant[pointer] = (float) Math.sqrt(Math.pow(event.values[0]/g,2d)+Math.pow(event.values[1]/g,2d)+Math.pow(event.values[2]/g,2d));
				timeStamps[pointer] = event.timestamp;
				
				
				//If we've sampled for epoch length
				if (timeStamps[pointer] >=(sensorRef+((long)epochLength*(long)1000000000))){
					sensorRef +=((long)epochLength*(long)1000000000);
					//////Log.d(TAG," Got 5s of data "+(sensorRef/(long) 1000000000));
					//Process the data in a separate thread
					lastMilliRef = System.currentTimeMillis();
					Thread thread = new Thread(new DataProcessor(Arrays.copyOf(resultant,pointer),Arrays.copyOf(timeStamps,pointer),lastMilliRef,new SimpleDateFormat("yyyy-MM-dd").format(new Date()),nBuilder,this));
					thread.start();
					pointer = 0;
				}else{
					if (pointer < (resultant.length-1)){
						++pointer;
					}
				}
				//////Log.d(TAG," x "+event.values[0]+" y "+event.values[1]+" z "+event.values[2]);
				

			}
			
		}
		

		
		//Data processing thread
		public class DataProcessor implements Runnable{
			float[] data;
			long[] tStamps;
			long tStamp;
			long millisecondOfDay;
			String date;
			TimeZone tz;
			long tStampOffset;
			int id = 102;
			ImuCaptureListener parent;
			NotificationCompat.Builder nBuilder;
			Locale defaultLocale;
			public DataProcessor(float[] data,long[] tStamps, long tStamp,String date, NotificationCompat.Builder nBuilder,ImuCaptureListener parent){
				this.data = data;
				this.tStamps = tStamps;
				this.tStamp = tStamp;
				this.date = date;
				this.parent = parent;
				this.nBuilder = nBuilder;
				defaultLocale = Locale.getDefault();
				
				//include time in milliseconds + timezone
				tz = TimeZone.getDefault();
				tStampOffset = (long) tz.getOffset(tStamp);
				double timeInDays = ((double) tStamp+tStampOffset)/(1000d*60d*60d*24d);
				millisecondOfDay = (long) ((timeInDays % 1)*(1000d*60d*60d*24d));
			}
			
			private boolean checkTime(){
				//Debug
				/*
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				String start = sdf.format(new Date(parent.startMillis));
				String stop = sdf.format(new Date(parent.stopMillis));
				String current = sdf.format(new Date(millisecondOfDay));
					*/
															
															
				if (parent.startMillis < parent.stopMillis){
					//Start earlier than stop
					if (millisecondOfDay >= parent.startMillis && millisecondOfDay <= parent.stopMillis){
						//////Log.d(TAG,start+" "+stop+" currently "+current+" true");
						return true;
					}else{
						//////Log.d(TAG,start+" "+stop+" currently "+current+" false");
						return false;
					}
					
				}else{
					//Start later than stop, use XOR -> if one is true -> true, if both or neither -> false
					if (millisecondOfDay >= parent.startMillis ^ millisecondOfDay <= parent.stopMillis){
						//////Log.d(TAG,start+" "+stop+" currently "+current+" true");
						return true;
					}else{
						//////Log.d(TAG,start+" "+stop+" currently "+current+" false");
						return false;
					}
					
				}
			}
			
			//Do the analysis here
			public void run(){
				double mad = MaD(data);
				boolean timeIn = checkTime();
				//////Log.d(TAG,"TimeIn "+Boolean.toString(timeIn));
				//Timer reminder check
				if (parent.timerOn && timeIn == true && mad >= Constants.madBins[1]){
					++parent.countAboveThresh;
					//////Log.d(TAG,"Count above reset");
				}else{
					parent.countAboveThresh = 0;
				}
				//Reset reminder timer here
				if (parent.timerOn && timeIn == true && parent.countAboveThresh>=3){
					//////Log.d(TAG,"Reset lastTimerMilliRef");
					parent.lastTimerMilliRef =tStamp;
            	NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					nBuilder.setProgress(100, 0, false);
					nm.notify(id, nBuilder.build());
					parent.progressBarOn = true;
				}
				
				
				//Reset timer if not between alarm time or timer off
				if (parent.timerOn == false || timeIn == false){
					parent.lastTimerMilliRef =tStamp;
					//Remove the inactivity bar
					//Remove the notification bar if it's there
					if (parent.progressBarOn == true){
						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				   	nBuilder.setProgress(0,0,false);
				   	nm.notify(id, nBuilder.build());
				   	parent.progressBarOn = false;
		      	}
					
				}
				//////Log.d(TAG,"tSamp "+tStamp+" timer ref "+ parent.lastTimerMilliRef+" diff s "+((tStamp-parent.lastTimerMilliRef))+" timer d "+(parent.timerDuration*(long)60));
				//Play reminder if >= remind minutes have passed since last reset
				if (parent.timerOn && timeIn == true && tStamp >= (parent.lastTimerMilliRef+parent.timerDuration*(long)60*(long)1000)){
					//////Log.d(TAG,"VIBRATE");
					Thread thread = new Thread(new RemindVibrate());
					thread.start();
					parent.lastTimerMilliRef =tStamp;	//Reset the reminder last timestamp
					NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					nBuilder.setProgress(100, 0, false);
					nm.notify(id, nBuilder.build());
					parent.progressBarOn = true;
				}else{
					if (parent.timerOn && timeIn == true){
						//////Log.d(TAG,"Update progress bar");
						//Update notification bar
						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						nBuilder.setProgress(100, (int) (100d*((double)((tStamp-parent.lastTimerMilliRef)/1000))/((double)(parent.timerDuration*60))), false);
						nm.notify(id, nBuilder.build());
					}else{
						//////Log.d(TAG,"No progress timerOn "+Boolean.toString(parent.timerOn)+" timeIn "+Boolean.toString(timeIn));
					}
				}
				
				//Timer reminder check done
				
				
				//////Log.d(TAG," 5s MaD "+mad);
				//Save the MaDs into a file
				writeResult(mad,tStamp,date);
				//Broadcast the mad to Activity
			     Intent new_intent = new Intent();
				  new_intent.setAction(Constants.SERVICE_TO_ACTIVITY);
				  new_intent.putExtra("MAD",mad);
				  new_intent.putExtra("Date",date);
				  new_intent.putExtra("Raw",Arrays.copyOf(data,data.length));
				  sendBroadcast(new_intent);
			}
			
			private void writeResult(double mad, long tStamp,String date){
				
				File externalStorageDir = new File(Environment.getExternalStorageDirectory(), "activityReminder");
				if (!externalStorageDir.exists()) {
					 if (!externalStorageDir.mkdirs()) {
						  ////Log.d(TAG, "failed to create directory");
					 }
				}
			
				//File externalStorageDir = Environment.getExternalStorageDirectory()+"/activityMonitor";
				File myFile = new File(externalStorageDir , "MaDs_"+date+".txt");
				
				if(!myFile.exists()){
					try{
					myFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(myFile);
						
						 OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
						 myOutWriter.append("TStamp "+tz.getID()+"\tMaD [g]\n");
						 myOutWriter.close();
						 fOut.close();
					
					}catch(Exception e){
						////Log.d(TAG,"Couldn't create file "+e.toString());
					}
				}
				
				try{
						 FileOutputStream fOut = new FileOutputStream(myFile, true);
						 OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
						 myOutWriter.append(String.format(defaultLocale,"%d\t%f\n",tStamp+tStampOffset,mad));
						 myOutWriter.close();
						 fOut.close();
				}catch(Exception e){
					////Log.d(TAG,"Couldn't append to file "+e.toString());
				}
				
			}
			
			//Calculate mean
			private double mean(float[] a){
				double ret = 0d;
				for (int i = 0;i<a.length;++i){
					ret+=a[i];
				}
				return ret/=(double) a.length;
			}
			
			//Calculate MaD
			private double MaD(float[] a){
				float[] temp = new float[a.length];
				double meanVal = mean(a);
				for (int i = 0;i<a.length;++i){
					temp[i] = (float) Math.abs(a[i]-meanVal);
				}
				return mean(temp);			
			}
		}
		
		//Vibration for reminder
		public class RemindVibrate implements Runnable{
			public RemindVibrate(){}
			public void run(){
			// Get instance of Vibrator from current Context
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(new long[]{0, 150, 150, 150, 150, 150, 450,
													450, 150, 450, 150, 450, 150,
													150, 150, 150, 150, 150}, -1);
		  	}
  		}
	}

	
   private void getWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();	//Start wake lock, wl.release(); needs to be called to shut this down...
        //////Log.d(TAG,"Got WakeLock");
    }
    
    //Implement abstract onBind (requires the IBinder stuff...)
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        ImuCaptureService getService() {
            return ImuCaptureService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
}
