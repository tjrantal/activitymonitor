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

@SuppressLint("all")
public class GraphicsSurfaceRawView extends GraphicsSurfaceView {
	private static final String TAG = "GraphicsSurfaceRawView";
	float mad = -1f;
	float sRate = -1f;
	final String text ="Last 5 s "+String.format("%.2f",0.00f)+" g";
	public GraphicsSurfaceRawView(Context context) {
		super(context);
	}
	
	public GraphicsSurfaceRawView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public GraphicsSurfaceRawView(Context context, AttributeSet attrs,int defstyle) {
		super(context,attrs,defstyle);
	}
	
	public void setMad(float mad){
		this.mad = mad;
	}
	
	public void setSRate(float sRate){
		this.sRate = sRate;
	}
	/*Draw plots here*/
	@Override
	protected void onDraw(Canvas canvas) {
		double y;
		double normalise = 4.0;
	    canvas.drawColor(Color.BLACK);	//Reset background color
	    if (data != null){
	    	float width = (float) canvas.getWidth();
	    	float height = (float) canvas.getHeight();
	    	path.reset();
	    	y = height-(((double) (data[0]))/(normalise)*height);
	    	path.moveTo(0f,(float) y);
	    	////////Log.d(TAG,"Data != null "+y);
	    	//Plot all datapoints?
	    	for (int i = 1; i < (int) data.length;++i) {
				y = height-(((double) (data[i]))/(normalise)*height);
				path.lineTo((float)(i/((float)data.length-1)*width),(float) y);
			}
			canvas.drawPath(path,mPaint);
			//Write mad
			Paint paint = new Paint(mPaint);
			paint.setTextSize(testTextSize);
			Rect bounds = new Rect();
			String text = "0";
			paint.getTextBounds(text, 0, text.length(), bounds);

			// Calculate the desired size as a proportion of our testTextSize.
			paint.setTextSize(testTextSize * (0.08f*height) / bounds.height());
			paint.setStyle(Paint.Style.FILL);
		    canvas.drawText(new String("Last 5 s "+String.format("%.2f",mad)+" g sampling rate "+String.format("%.1f",sRate)),0.5f*width,0.125f*height,paint);
	    }
	    
	}
}
