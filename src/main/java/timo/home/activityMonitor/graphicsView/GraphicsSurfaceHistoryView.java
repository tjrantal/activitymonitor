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
import android.annotation.SuppressLint;

//activity history from files
import java.util.ArrayList;
import timo.home.activityMonitor.utils.ActivityDataReader;
import timo.home.activityMonitor.utils.ActivityInterval;
import timo.home.activityMonitor.utils.Constants;

@SuppressLint("all")
public class GraphicsSurfaceHistoryView extends GraphicsSurfaceView {
	private static final String TAG = "GraphicsSurfaceHistoryView";
	float mad = -1f;
	float sRate = -1f;
	final String text ="Last 5 s "+String.format("%.2f",0.00f)+" g";
	private ArrayList<ActivityDataReader> activityReaders;
	private ArrayList<ActivityInterval> activityIntervals;
	protected Paint[] traceColours;
	protected Paint[] bgBars;
	
	public GraphicsSurfaceHistoryView(Context context) {
		super(context);
		initColours();
	}
	
	public GraphicsSurfaceHistoryView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initColours();
	}

	public GraphicsSurfaceHistoryView(Context context, AttributeSet attrs,int defstyle) {
		super(context,attrs,defstyle);
		initColours();
	}
	
	private void initColours(){
		Paint template = new Paint(mPaint);
		template.setStyle(Paint.Style.STROKE);//FILL_AND_STROKE);

		traceColours = new Paint[Constants.colourInts.length];
		for (int i =0;i<Constants.colourInts.length;++i){
			traceColours[i] = new Paint(template);
			traceColours[i].setColor(Constants.colourInts[i]);
			//Log.d(TAG,"Adding colour "+i+" int "+Constants.colourInts[i]);	
		}
		
		bgBars = new Paint[4];
		template.setStyle(Paint.Style.FILL);
		bgBars[0] = new Paint(template);
		bgBars[0].setColor(0X77FF3232);	//RED (255,50,50)
		bgBars[1] = new Paint(template);
		bgBars[1].setColor(0X77FF9632);	//ORANGE (255,150,50)
		bgBars[2] = new Paint(template);
		bgBars[2].setColor(0X77FFFF46);	//Yellow (255,255,70)
		bgBars[3] = new Paint(template);
		bgBars[3].setColor(0X7732FF32);	//Green (50,255,50)

	}
	
	public void setActivityReaders(ArrayList<ActivityDataReader> a){
		activityReaders  =a;
		//Log.d(TAG,"Activity readers updated "+activityReaders.size());
	}
	public void setActivityIntervals(ArrayList<ActivityInterval> a){
		activityIntervals  =a;
		//Log.d(TAG,"Activity intervals updated "+activityIntervals.size());
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
		double normalise = Constants.maxY;
	    canvas.drawColor(Color.BLACK);	//Reset background color
	    
	   
	    	float width = (float) canvas.getWidth();
	    	float height = (float) canvas.getHeight();
	    	float plotHeight = 0.86f*height;	//Reserve 10% from the bottom for xtickmarks
	    	//float plotWidth = 0.9f*width;	//Reserve 10% from the bottom for xtickmarks
	    	float plotWidth = 1f*width;	//Reserve 10% from the bottom for xtickmarks
	    	
	    	//Draw background rectangles
	    	float[] rectangleYs = Constants.rectangleYs;
	    	for (int i =0;i<rectangleYs.length-1;++i){
	    		canvas.drawRect(width-plotWidth,(1f-rectangleYs[i])*plotHeight,width,(1f-rectangleYs[i+1])*plotHeight,bgBars[i]);
	    	}
	    	
	    	//Log.d(TAG,"onDraw w "+width+ " h "+height);	    
			 //for (int a =0; a<activityReaders.size();++a){
			 	//data = activityReaders.get(a).tempData;
			 	////Log.d(TAG,"activityReader "+a+" length "+data.length);
			 for (int a =0; a<activityIntervals.size();++a){
			 	data = activityIntervals.get(a).tempData;
			 	//Log.d(TAG,"activityIntervals "+a+" length "+data.length);
				 if (data != null && data.length > 2){

				 	//Draw all traces
				 	path.reset();
				 	//Log.d(TAG,"data[0] "+data[0]+" norm "+normalise+" length "+data.length);
				 	y = plotHeight-(((double) (data[0]))/(normalise)*plotHeight);
				 	path.moveTo(width-plotWidth,(float) y);
				 	//path.moveTo(0f,(float) y);
				 	////Log.d(TAG,"Data != null "+y);
				 	//Plot all datapoints?
				 	for (int i = 1; i < (int) data.length;++i) {
						y = plotHeight-(((double) (data[i]))/(normalise)*plotHeight);
						path.lineTo(width-plotWidth+(float)(i/((float)(data.length-1))*plotWidth),(float) y);
						//path.lineTo((float)(i/((float)(data.length-1))*plotWidth),(float) y);

					}
					//canvas.drawPath(path,traceColours[0]);
					////Log.d(TAG,"Past loop "+(traceColours.length % a));
					canvas.drawPath(path,traceColours[a % traceColours.length]);

				 }
			 }
			 
			 //PLOT TIMESTAMPS
			Paint paint = new Paint(mPaint);
		  	paint.setTextSize(testTextSize);
			Rect bounds = new Rect();
			String text = "08:00";
			paint.getTextBounds(text, 0, text.length(), bounds);

			// Calculate the desired size as a proportion of our testTextSize.
			paint.setTextSize(testTextSize * (0.05f*height) / bounds.height());
			paint.setStyle(Paint.Style.FILL);
		 	// Set the paint for that size.
		 	String[] times = activityIntervals.get(0).times;
			for (int i = 1; i<times.length-1;++i){
	    		canvas.drawText(times[i],width-plotWidth+((1f/(float)(times.length-1)))*((float)i)*plotWidth,0.99f*height,paint);
	    		//canvas.drawText(times[i],((1f/(float)(times.length-1)))*((float)i)*plotWidth,0.99f*height,paint);
	    	}
			/*
			//Plot Y-Axis
			for (int i = 1; i<Constants.rectangleYs.length-1;++i){
	    		canvas.drawText(String.format("%.3f",Constants.rectangleYs[i]),0.05f,plotHeight-Constants.rectangleYs[i]*plotHeight,paint);
	    	}
	    	*/
	    
	}
}
