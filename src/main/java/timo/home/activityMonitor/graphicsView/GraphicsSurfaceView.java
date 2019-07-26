package timo.home.activityMonitor.graphicsView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;		//Canvas for drawing the data on screen
import android.view.SurfaceHolder;	//Holder to obtain the canvas
import android.view.SurfaceView;
import android.view.View;
import android.util.AttributeSet;

//import android.util.Log;	//Debugging
import android.graphics.Paint;	//Debugging drawing
import android.graphics.Color;	//Debugging drawing
import android.graphics.Path;	//Plot trace
import android.graphics.Rect;
import android.annotation.SuppressLint;
import timo.home.activityMonitor.utils.Constants;

@SuppressLint("all")
public class GraphicsSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "GraphicsSurfaceView";

	/*For plotting holder and canvas*/
	protected SurfaceHolder holder;
	protected Canvas canvas;
	protected Paint  mPaint;
	Path path;
	double radius = 30;
	final float testTextSize = 48f;
	protected float[] data = null;
	protected String[] times = null;

	protected Paint[] colorInd;
	protected Paint[] bgBars;
	public GraphicsSurfaceView(Context context) {
		super(context);
		////Log.d(TAG,"constructed1");
		init();

	}
	
	public GraphicsSurfaceView(Context context, AttributeSet attrs) {
		super(context,attrs);
		////Log.d(TAG,"constructed2");
		init();
	}

	public GraphicsSurfaceView(Context context, AttributeSet attrs,int defstyle) {
		super(context,attrs,defstyle);
		////Log.d(TAG,"constructed3");
		init();
	}	

	private void init(){
		getHolder().addCallback(this);
		setFocusable(true); // make sure we get key events
		mPaint = new Paint();
		mPaint.setDither(true);
		//mPaint.setColor(0xFF0000FF);
		mPaint.setColor(0xFFFFFFFF);
		mPaint.setStyle(Paint.Style.STROKE);//STROKE); //
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(3);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(12f);
		path = new Path();
		/*
		data = new short[500];
		*/
		Paint template = new Paint(mPaint);
		template.setStyle(Paint.Style.FILL_AND_STROKE);
		colorInd = new Paint[4];		
		colorInd[0] = new Paint(template);
		colorInd[0].setColor(0XFFFF3232);	//RED (255,50,50)
		//colorInd[0].setShadowLayer(20f,0f,0f,0X773232FF);
		colorInd[1] = new Paint(template);
		colorInd[1].setColor(0XFFFF9632);	//ORANGE (Orange 255,150,50)
		//colorInd[1].setShadowLayer(20f,0f,0f,0X773232FF);
		colorInd[2] = new Paint(template);
		colorInd[2].setColor(0XFFFFFF46);	//Yellow (Yellow 255,255,70)
		//colorInd[2].setShadowLayer(20f,0f,0f,0X773232FF);
		colorInd[3] = new Paint(template);
		colorInd[3].setColor(0XFF32FF32);	//Green (Blue 50,255,50)
		//colorInd[3].setShadowLayer(20f,0f,0f,0X773232FF);
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
		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}
	
	public void setTimes(String[] t){
		times = t;
	}
	
	/*call to plot the latest set of data*/
	public void updateData(float[] data){
		this.data = data;
	    canvas = null;
	    holder = getHolder();
	    try {
	        canvas = holder.lockCanvas(null);
	        synchronized(holder) {
	            onDraw(canvas);
	        }

	    }catch (Exception err){ 
	    	////Log.d(TAG,"Canvas lock error "+err.toString());
	    	if (holder == null){
	    		////Log.d(TAG,"Holder null "+holder.toString());
	    	}else{
	    		////Log.d(TAG,"Holder not null "+holder.toString());
	    	}
	    }finally {

	        if(canvas != null) {
				////Log.d("tswt","unlockAndPost "+canvas.toString());
	            holder.unlockCanvasAndPost(canvas);
	        }

	    }
	}
	
	/*Draw Canvas for saving a screen capture*/
	public Bitmap getScreenShot(){
		Bitmap bm = Bitmap.createBitmap(800,300,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		onDraw(canvas);
		return bm;
		
	}
	
	/*Draw plots here*/
	protected void onDraw(Canvas canvas) {
		////Log.d(TAG,"onDraw");
		int indexToPlot = 0;
		double y;
	    canvas.drawColor(Color.BLACK);	//Reset background color
  
	    if (data != null){

	    	float width = (float) canvas.getWidth();
	    	float maxY = Constants.maxY;
	    	float height = (float) canvas.getHeight();
	    	float plotHeight = 0.86f*height;	//Reserve 10% from the bottom for xtickmarks	
	    	path.reset();
	    	y = plotHeight-(((double) (data[indexToPlot]))/(maxY)*plotHeight);
	    	path.moveTo(0f,(float) y);
	    	float[] rectangleYs = Constants.rectangleYs;
	    	//Draw background rectangles
	    	for (int i =0;i<rectangleYs.length-1;++i){
	    		canvas.drawRect(0f,(1f-rectangleYs[i])*plotHeight,width,(1f-rectangleYs[i+1])*plotHeight,bgBars[i]);
	    	}
	    	
	    	////Log.d(TAG,"Data != null "+y);
	    	//Plot all datapoints?
	    	for (int i = 1; i < (int) data.length;++i) {
				y = plotHeight-(((double) (data[i]))/(maxY)*plotHeight);
				path.lineTo((float)(i/((float)data.length-1)*width),(float) y);
			}
	    	
	    	/*
			//Plot subsample of data points
			for (int i = 1; i < (int) width;++i) {
				indexToPlot = (int) (Math.floor(((double)i)*((double)data.length)/width));
				y = height-(((double) (data[indexToPlot]))/(1.5)*height);
				path.lineTo((float)i,(float) y);
			}
			*/
			canvas.drawPath(path,mPaint);
			
			//PLOT TIMESTAMPS
			Paint paint = new Paint(mPaint);
			  paint.setTextSize(testTextSize);
			 Rect bounds = new Rect();
			 String text = "08:00";
			 paint.getTextBounds(text, 0, text.length(), bounds);

			 // Calculate the desired size as a proportion of our testTextSize.
			 paint.setTextSize(testTextSize * (0.08f*height) / bounds.height());
				paint.setStyle(Paint.Style.FILL);
			 // Set the paint for that size.
			for (int i = 1; i<times.length-1;++i){
	    		canvas.drawText(times[i],((1f/(float)(times.length-1)))*((float)i)*width,0.99f*height,paint);
	    	}
			
			canvas.drawText(new String("Activity timing today"),0.5f*width,0.1f*height,paint);
	    }
	    //canvas.drawCircle(50.0f,50.0f,(float) (radius+Math.sin((double)reDraw)*10.0),mPaint);

	}
	
	/*SurfaceHolder.Callback*/
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
		// TODO Auto-generated method stub
		this.holder = holder; 
	}

	public void surfaceCreated(SurfaceHolder holder) {
		this.holder = holder; 
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

}
