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

//Internationalisation
import java.text.NumberFormat;
import java.util.Locale;

public class ActivityDataReader{
	public float[] tempData = new float[]{0f,0.25f,0.5f,0.25f,0f,-0.25f,-0.5f,-0.25f,0f};
	public float[] tempBar = {10000f,8000f,5000f,4000f,3000f,2000f,1500f,1000f,500f,250f,200f,100f,0f};
	public String[] times = {"08:00","10:00","12:00","14:00","16:00"};
	public long[] tStamps = {0l,1l,2l,3l,4l,5l,6l,7l,8l};
	protected NumberFormat nf;
	
	//Constructor
	public ActivityDataReader(String activityFileName){
		nf = NumberFormat.getInstance(Locale.getDefault());	//Used to handle . and ,
		 //Try to read data from the text file, create histogram, and show data...
      File externalStorageDir = new File(Environment.getExternalStorageDirectory(), Constants.activityFileFolder);
		if (externalStorageDir.exists()) {
			File myFile = new File(externalStorageDir , activityFileName);
			if(myFile.exists()){
				try{
					BufferedReader br = new BufferedReader(new FileReader(myFile));
					String strLine = "";
					StringTokenizer st = null;
					String separator = "\t";
					int tokenNumber = 0;
					/*Read column headings from first row*/
					strLine = br.readLine(); /*Read the headings row*/
					st = new StringTokenizer(strLine, separator);
					ArrayList<ArrayList<String>> columns = new ArrayList<ArrayList<String>>();
					while(st.hasMoreTokens()){
						st.nextToken();
						columns.add(new ArrayList<String>());
					}
					/*Read data row by row*/
					while((strLine = br.readLine()) != null){
						st = new StringTokenizer(strLine, separator);
						tokenNumber = 0;
						while(st.hasMoreTokens()){
	                  String nextToken = st.nextToken().trim();
	                  columns.get(tokenNumber).add(new String(nextToken));
	                  ++tokenNumber;
						}
					}
					//Pop data into tempData;
					tempData = new float[columns.get(1).size()];
					tStamps= new long[columns.get(0).size()];
					for (int i = 0; i<columns.get(1).size(); ++i){
						tStamps[i] = nf.parse(columns.get(0).get(i)).longValue();
						tempData[i] = nf.parse(columns.get(1).get(i)).floatValue();
					}
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
					sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
					times = new String[]{	sdf.format(new Date(tStamps[(tStamps.length-1)*0/4])),
												sdf.format(new Date(tStamps[(tStamps.length-1)*1/4])),
												sdf.format(new Date(tStamps[(tStamps.length-1)*2/4])),
												sdf.format(new Date(tStamps[(tStamps.length-1)*3/4])),
												sdf.format(new Date(tStamps[(tStamps.length-1)*4/4]))
					};																		
					//Calculate histogram
					tempBar = new float[Constants.madBins.length];
					
					int binInd = 0;
					float val;
					for (int i = 0; i<tempData.length; ++i){
						//Find the correct bin
						val = tempData[i];
						binInd = 0;
						while (binInd < Constants.madBins.length && val > Constants.madBins[binInd]){
							++binInd;
						}
		
						tempBar[binInd] += 1f;
					}
				
				}catch(Exception e){
            ///don't do anything otherwise
            }
			}
		}
	}

	
}
