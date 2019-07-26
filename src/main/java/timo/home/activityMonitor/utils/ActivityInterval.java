/**A class to read activity data from a file*/
package timo.home.activityMonitor.utils;

import android.os.Environment;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
//import android.util.Log;	//Debugging

public class ActivityInterval{
	private static final String TAG = "ActivityInterval";
	public float[] tempData = new float[]{0f,0.25f,0.5f,0.25f,0f,-0.25f,-0.5f,-0.25f,0f};
	public float[] tempBar = {10000f,8000f,5000f,4000f,3000f,2000f,1500f,1000f,500f,250f,200f,100f,0f};
	public String[] times = {"08:00","10:00","12:00","14:00","16:00"};
	public ActivityInterval(ActivityDataReader adr, int dayStartH, int dayStartM,int dayStopH,int dayStopM){
		//Get calendar instances of start and stop set to the day of the adr, set start and stop -> get comparable timestamps. Timestamps are in UTC
		Calendar adrCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),Locale.ENGLISH);
		adrCalendar.setTimeInMillis(adr.tStamps[0]);
		Calendar startDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"),Locale.ENGLISH);
		startDate.set(adrCalendar.get(Calendar.YEAR),adrCalendar.get(Calendar.MONTH),adrCalendar.get(Calendar.DAY_OF_MONTH),dayStartH,dayStartM,0);
		Calendar stopDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"),Locale.ENGLISH);
		stopDate.set(adrCalendar.get(Calendar.YEAR),adrCalendar.get(Calendar.MONTH),adrCalendar.get(Calendar.DAY_OF_MONTH),dayStopH,dayStopM,0);
		 //Get the interval of interest
		 ArrayList<Integer> indices = new ArrayList<Integer>();
		 long startMilli = startDate.getTimeInMillis();
		 long stopMilli = stopDate.getTimeInMillis();
		 //Log.d(TAG,"Start Date "+startDate.getTime().toGMTString()+" Stop Date "+stopDate.getTime().toGMTString()+" Date(adr.tStamps[0]) "+adrCalendar.getTime().toGMTString());
		 //Log.d(TAG,"Start Milli "+startMilli+" Stop Milli "+stopMilli+" adr.tStamps[0] "+adr.tStamps[0]);
		 //Insert zeros if data isn't found, otherwise the recorded value
		 int i = 0;
		 long epochMilli = (long) (Constants.epochDuration*1000f);
		 for (long milli = startMilli;milli<(stopMilli-epochMilli+1);milli+=epochMilli){
		 		if (adr.tStamps[i] >= milli & adr.tStamps[i] <(milli+epochMilli-1l)){
		 			indices.add(i);
		 		}else{
		 			indices.add(-1);
		 		}
		 		while (i < (adr.tStamps.length -1) && adr.tStamps[i]<(milli+epochMilli-1l)){
		 			++i;
		 		}
		 		/*
				for (int i = 0; i<adr.tStamps.length; ++i){
				
					if (adr.tStamps[i] >= startMilli && adr.tStamps[i] <= stopMilli){
						indices.add(i);
					}
				}
				*/
			}
			//Log.d(TAG,"AInt length "+indices.size());
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			long range = stopMilli-startMilli;
			times = new String[]{	sdf.format(new Date(startMilli+range*0/4)),
										sdf.format(new Date(startMilli+range*1/4)),
										sdf.format(new Date(startMilli+range*2/4)),
										sdf.format(new Date(startMilli+range*3/4)),
										sdf.format(new Date(startMilli+range*4/4))
			};		
			//Extract the values
			tempData = new float[indices.size()];
			for (i = 0;i<indices.size();++i){
				if (indices.get(i) > -1){
					tempData[i]=adr.tempData[indices.get(i)];
				}else{
					tempData[i] = 0f;
				}
			}
																			
			//Calculate histogram
			tempBar = new float[Constants.madBins.length];
			
			int binInd = 0;
			float val;
			for (i = 0; i<tempData.length; ++i){
				//Find the correct bin
				val = tempData[i];
				binInd = 0;
				while (binInd < Constants.madBins.length && val > Constants.madBins[binInd]){
					++binInd;
				}
				tempBar[binInd] += 1f;
			}

	}
}
