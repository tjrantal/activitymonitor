package timo.home.activityMonitor.graphicsView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;		//Canvas for drawing the data on screen
import android.view.SurfaceHolder;	//Holder to obtain the canvas
import android.view.SurfaceView;
import android.util.AttributeSet;

//import android.util.Log;	//Debugging
import android.graphics.Paint;	//Debugging drawing
import android.graphics.Color;	//Debugging drawing
import android.graphics.Path;	//Plot trace
import android.graphics.Rect;
import android.graphics.RectF;
import android.annotation.SuppressLint;

//activity history from files
import java.util.ArrayList;
import timo.home.activityMonitor.utils.ActivityDataReader;
import timo.home.activityMonitor.utils.ActivityInterval;
import timo.home.activityMonitor.utils.Constants;

@SuppressLint("all")
public class GraphicsSurfaceBarHistoryView extends GraphicsSurfaceView {
	private static final String TAG = "GraphicsSurfaceBarHistoryView";
	int currentColor = 0;
	int[] activityMins = {0,0,0};
	float[] xPos = {1f/8f,3f/8f,6f/8f};
	String[] xTitle = {"Inactivity","Light","Moderate + Vigorous"};
	String xUnit = " min";
	private ArrayList<ActivityDataReader> activityReaders = null;
	private ArrayList<ActivityInterval> activityIntervals = null;
	protected Paint[] traceColours;
	
	public GraphicsSurfaceBarHistoryView(Context context) {
		super(context);
		initColours();
	}
	
	public GraphicsSurfaceBarHistoryView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initColours();
	}

	public GraphicsSurfaceBarHistoryView(Context context, AttributeSet attrs,int defstyle) {
		super(context,attrs,defstyle);
		initColours();
	}	
	
	public void setActivityReaders(ArrayList<ActivityDataReader> a){
		activityReaders  =a;
		//Log.d(TAG,"Activity readers updated "+activityReaders.size());
	}
	public void setActivityIntervals(ArrayList<ActivityInterval> a){
		activityIntervals  =a;
		//Log.d(TAG,"Activity intervals updated "+activityIntervals.size());
	}
	
	
	private void initColours(){
		Paint template = new Paint(mPaint);
		template.setStyle(Paint.Style.FILL);

		traceColours = new Paint[Constants.colourInts.length];
		for (int i =0;i<Constants.colourInts.length;++i){
			traceColours[i] = new Paint(template);
			traceColours[i].setColor(Constants.colourInts[i]);
			//Log.d(TAG,"Adding colour "+i+" int "+Constants.colourInts[i]);	
		}
	}
	
	
	
	
		/*call to plot the latest set of data*/
//	public void updateData(ArrayList<ActivityDataReader> a){
	//	setActivityReaders(a);
	public void updateData(ArrayList<ActivityInterval> a){
		setActivityIntervals(a);
	    canvas = null;
	    holder = getHolder();
	    try {
	        canvas = holder.lockCanvas(null);
	        synchronized(holder) {
	        		////Log.d(TAG,"onDraw "+activityReaders.size()+" canvas "+canvas.toString());
	        		//Log.d(TAG,"onDraw "+activityIntervals.size()+" canvas "+canvas.toString());
	        		
	        		
	        		
	            onDraw(canvas);
	        }

	    }catch (Exception err){ 
	    	//Log.d(TAG,"Canvas lock error "+err.toString());
	    	if (holder == null){
	    		//Log.d(TAG,"Holder null "+holder.toString());
	    	}else{
	    		//Log.d(TAG,"Holder not null "+holder.toString());
	    	}
	    }finally {

	        if(canvas != null) {
				////Log.d("tswt","unlockAndPost "+canvas.toString());
	            holder.unlockCanvasAndPost(canvas);
	        }

	    }
	}
	
	/*Draw plots here*/
	@Override
	protected void onDraw(Canvas canvas) {
		double y;
	    canvas.drawColor(Color.BLACK);	//Reset background color
	    if (activityIntervals != null){
	    	float width = (float) canvas.getWidth();
	    	float height = (float) canvas.getHeight();

			//Draw activity minutes of the day
			//PLOT TIMESTAMPS
			
			//Log.d(TAG,"Test font size");
			
			Paint paint = new Paint(mPaint);
			paint.setTextSize(testTextSize);
			Rect bounds = new Rect();
			String text = "0";
			paint.getTextBounds(text, 0, text.length(), bounds);

			// Calculate the desired size as a proportion of our testTextSize.
			paint.setTextSize(testTextSize * (0.04f*height) / bounds.height());
			paint.setStyle(Paint.Style.FILL);
			
			//Log.d(TAG,"Font size ready");
			
			//Plot header
			for (int i = 0; i<xPos.length;++i){
				
	    		canvas.drawText(xTitle[i],xPos[i]*width,0.10f*height,paint);
    		}
    		//Plot values
	    	currentColor = 0;
	    	for (int a =0; a<activityIntervals.size();++a){
	    		activityMins[0] = (int) (activityIntervals.get(a).tempBar[0]*5f/60f);
	    		activityMins[1] = (int) (activityIntervals.get(a).tempBar[1]*5f/60f);
	    		activityMins[2] = (int) ((activityIntervals.get(a).tempBar[2]+activityIntervals.get(a).tempBar[3])*5f/60f);
	    		traceColours[a % traceColours.length].setTextSize(testTextSize * (0.04f*height) / bounds.height());
	    		for (int i = 0; i<xPos.length;++i){
					canvas.drawText(activityMins[i]+xUnit,xPos[i]*width,(0.15f+((float)a)*0.05f)*height,traceColours[a % traceColours.length]);
				}

				
			}
			
    		
			
		}	
	}
}
