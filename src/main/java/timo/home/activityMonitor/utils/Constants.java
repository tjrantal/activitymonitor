/**Strings to be used to communicate/relay intent between ActivityMonitor and IMUCaptureService*/
package timo.home.activityMonitor.utils;
public final class Constants{
	public static final float epochDuration = 5f;	//MAD epoch duration in seconds
	public static final int[] colourInts = new int[]{0X77FF3232,0X77FF9632,0X77FFFF46,0X7732FF32,0X7700FFFF,0X77FF00FF,0X77FF8000};
	public static final float[] madBins = {0.0149f,0.091f,0.414f,Float.POSITIVE_INFINITY};	//Vaha-Ypya 2015a,b inactivity the cut-off between standing and slow walking <0.0149f , light <0.091f, moderate <0.414f, vigorous >= 0.414f
	public static final float[] rectangleYs = {0f, 0.0149f,0.091f,0.414f,1f}; //MAD visualisation rectangles
	public static final float maxY = 1f;	//MAD scale
	public static final String activityFileFolder = "activityReminder";
	public static final String START_SERVICE = "timo.home.activityMonitor.utils.Constants.START_SERVICE";
	public static final String STOP_SERVICE = "timo.home.activityMonitor.utils.Constants.STOP_SERVICE";
	public static final String TIMER_ON = "timo.home.activityMonitor.utils.Constants.TIMER_ON";
	public static final String TIMER_OFF = "timo.home.activityMonitor.utils.Constants.TIMER_OFF";
	public static final String TIMER_DURATION = "timo.home.activityMonitor.utils.Constants.TIMER_DURATION";
	public static final String TIMER_DURATION_H = "timo.home.activityMonitor.utils.Constants.TIMER_DURATION_H";
	public static final String TIMER_DURATION_M = "timo.home.activityMonitor.utils.Constants.TIMER_DURATION_M";
	public static final String TIMER_START = "timo.home.activityMonitor.utils.Constants.TIMER_START";
	public static final String TIMER_START_H = "timo.home.activityMonitor.utils.Constants.TIMER_START_H";
	public static final String TIMER_START_M = "timo.home.activityMonitor.utils.Constants.TIMER_START_M";
	public static final String TIMER_STOP = "timo.home.activityMonitor.utils.Constants.TIMER_STOP";
	public static final String TIMER_STOP_H = "timo.home.activityMonitor.utils.Constants.TIMER_STOP_H";
	public static final String TIMER_STOP_M = "timo.home.activityMonitor.utils.Constants.TIMER_STOP_M";
	public static final String SERVICE_TO_ACTIVITY = "timo.home.activityMonitor.utils.Constants.SERVICE_TO_ACTIVITY";
	public static final String APP_CLASS = "timo.home.activityMonitor.ActivityReminder";
	public static final String TOAST = "timo.home.activityMonitor.utils.Constants.TOAST";
	public static final String remindStartButton ="Remind start ";
	public static final String remindStopButton = "Remind stop ";
	public static final String remindDuration = "Every ";
	//Communication within the history visualisation activity
	public static final String DAY_START = "timo.home.activityMonitor.utils.Constants.DAY_START";
	public static final String DAY_START_H = "timo.home.activityMonitor.utils.Constants.DAY_START_H";
	public static final String DAY_START_M = "timo.home.activityMonitor.utils.Constants.DAY_START_M";
	public static final String DAY_STOP = "timo.home.activityMonitor.utils.Constants.DAY_STOP";
	public static final String DAY_STOP_H = "timo.home.activityMonitor.utils.Constants.DAY_STOP_H";
	public static final String DAY_STOP_M = "timo.home.activityMonitor.utils.Constants.DAY_STOP_M";
	public static final int FG_NOTIFICATION_INTENT = 102;
	public static final int FLISTSTARTINTENT = 103;
	public static final int REQUEST_DISABLE_BATTERY = 158;
	public static final String FNAMES = "timo.home.activityMonitor.utils.Constants.FNAMES";
	public static final String NOTIFICATION_CHANNEL_ID = "timo.home.activityMonitor.utils.Constants.NOTIFICATION_CHANNEL_ID";
	
}
