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

@SuppressLint("all")
public class GraphicsSurfaceBarView extends GraphicsSurfaceView {
	private static final String TAG = "GraphicsSurfaceBarView";
	int currentColor = 0;
	int[] activityMins = {0,0,0};
	float[] xPos = {1f/8f,3f/8f,6f/8f};
	String[] xTitle = {"Inactivity","Light","Moderate + Vigorous"};
	String xUnit = " min";
	public GraphicsSurfaceBarView(Context context) {
		super(context);
	}
	
	public GraphicsSurfaceBarView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public GraphicsSurfaceBarView(Context context, AttributeSet attrs,int defstyle) {
		super(context,attrs,defstyle);
	}	
	
	public void setActivity(int[] a){
		activityMins  =a;
	}
	
	/*Draw plots here*/
	@Override
	protected void onDraw(Canvas canvas) {
		double y;
		//double fullScale = Math.log(60d*1d/5d*60d*24d);	//Number of epochs in a day
	   double fullScale = 60d*1d/5d*60d*10d;//*24d;	//Show up to 10 h per day...
	    canvas.drawColor(Color.BLACK);	//Reset background color
	    if (data != null){
	    	float width = (float) canvas.getWidth();
	    	float height = (float) canvas.getHeight();
	    	float barWidth = width/((float) data.length);
	    	//Plot histogram
	    	currentColor = 0;
	    	for (int i = 0; i < (int) data.length;++i) {
				//y = height-(((double) (Math.log(data[i]+1d)))/(fullScale)*height);
				y = height-(((double) (data[i]))/(fullScale)*height);
				//path.lineTo((float)(i/((float)data.length)*width),(float) y);
				//drawRectangles here drawRoundRect?
				canvas.drawRoundRect(new RectF(barWidth*i,(float)y,barWidth*((float)(i+1)),height),barWidth/10f,barWidth/10f,colorInd[currentColor]);
				//canvas.drawRect(barWidth*i,(float)y,barWidth*((float)(i+1)),height,mPaint);
				++currentColor;
				if (currentColor >= colorInd.length){
					currentColor = 0;
				}
				
			}
			//Draw activity minutes of the day
			//PLOT TIMESTAMPS
			Paint paint = new Paint(mPaint);
			paint.setTextSize(testTextSize);
			Rect bounds = new Rect();
			String text = "0";
			paint.getTextBounds(text, 0, text.length(), bounds);

			// Calculate the desired size as a proportion of our testTextSize.
			paint.setTextSize(testTextSize * (0.08f*height) / bounds.height());
			paint.setStyle(Paint.Style.FILL);
			for (int i = 0; i<xPos.length;++i){
	    		canvas.drawText(xTitle[i],xPos[i]*width,0.15f*height,paint);
	    		canvas.drawText(activityMins[i]+xUnit,xPos[i]*width,0.25f*height,paint);
	    	}
			
	    }
	}
}
