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

//Save settings from session to the next
import android.content.SharedPreferences;

import android.app.ListActivity;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import java.util.Arrays;
import java.util.Collections;

//Context
//import timo.home.activityMonitor.R;	//Import R from this project...

public class FileSelectorList extends Activity implements /*AdapterView.OnItemSelectedListener,*/AdapterView.OnItemClickListener{
	private static final String TAG = "FileSelectorList";
	ArrayAdapter<String> aa;
	ListView lw;
	Button acceptButton;
	Button cancelButton;
	String[] fNames = null;
	boolean[] selected = null;
	     /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
  			setContentView(R.layout.list);
  			
  			//Start the actual content
  			lw = (ListView) findViewById(R.id.mFileSelectorList);
  			acceptButton = (Button) findViewById(R.id.mVisualise);
  			cancelButton = (Button) findViewById(R.id.mCancel);
			//Implement button listeners  			
        	acceptButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
						int selectedNo = 0;
						for (int i = 0;i<selected.length;++i){
							if (selected[i] == true){
								++selectedNo;
							}
						}
						
						if (selectedNo > 0 && fNames != null ){
							String[] fileNames = new String[selectedNo];
							selectedNo  = 0;
							for (int i = 0; i<selected.length;++i){
								if (selected[i]  == true){
									////Log.d(TAG,"Adding "+selectedNo+" "+ fNames[i]);
									fileNames[selectedNo] = fNames[i];
									++selectedNo;
								}
							}
							Intent intent = new Intent();
		 					intent.putExtra(Constants.FNAMES,fileNames);
						  	setResult(RESULT_OK,intent);
					  	}else{
					  		setResult(RESULT_CANCELED);
					  	}
					  	finish();
				  	}
        		}
        	);
        	cancelButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					  	setResult(RESULT_CANCELED);
					  	finish();
				  	}
        		}
        	);
			//List files here
			File externalStorageDir = new File(Environment.getExternalStorageDirectory(), Constants.activityFileFolder);
			if (externalStorageDir.exists()) {
				//////Log.d(TAG,"storageDir exists");
				//List files
				File[] fList = externalStorageDir.listFiles(fnFilter);
				if (fList.length > 0){
					////Log.d(TAG,"fList > 0");
					fNames = new String[fList.length];
					selected = new boolean[fList.length];
					//Arrays.fill(selected,false);
					for (int i = 0;i<fList.length;++i){
						fNames[i] = fList[i].getName();
						selected[i] = false;
						//////Log.d(TAG,"fName "+i+" "+fNames[i]);
					}
					Arrays.sort(fNames, Collections.reverseOrder());
					//Add the files to ListView
					//////Log.d(TAG,"set ListAdapter");
					aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice /*R.layout.list*/,fNames);
					lw.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					lw.setAdapter(aa);

					//ListView listView = getListView();
					//listView.setTextFilterEnabled(true);
					/*
					listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							 // When clicked, show a toast with the TextView text
							 //Toast.makeText(getApplicationContext(),
							//((TextView) view).getText(), Toast.LENGTH_SHORT).show();
						}
					});
					*/
				}
			}
			//lw.setOnItemSelectedListener(this);
			lw.setOnItemClickListener(this);
		
    }
	
	FilenameFilter fnFilter = new FilenameFilter(){
		@Override
		public boolean accept(File dir, String name){
			return name.indexOf("MaDs_") > -1 ? true : false;
		}
	};
	
	//AdapterView.OnItemClickListener
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		selected[position] ^= true;	//Enable selecting and unselecting, flip at onItemClick
		////Log.d(TAG,aa.getItem(position).toString());
	}	
	
	/*
	//AdapterView.OnItemSelectedListener
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
		selected[position] = true;
		////Log.d(TAG,aa.getItem(position).toString());
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent){
		//Do nothing
	}
	*/
	/*
	  @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
    }
	*/
	
	/*Power saving*/
     protected void onResume() {
     		super.onResume();
     }

     protected void onPause() {
      		super.onPause();
     }
	
	protected void onDestroy(){
		//BROADCAST the selected files and/or set the return intent here
      super.onDestroy();
	}
    
}
