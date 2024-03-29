package com.rainmama.activities;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import com.rainmama.R;
import com.rainmama.data.WeatherDataHolder;
import com.rainmama.services.WeatherService;
//import com.actionbarsherlock.view.MenuInflater;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends SherlockActivity {
	private static final String TAG = "MainActivity";
	public static boolean IS_IN_FRONT;
	private WeatherReceiver receiver;
	private TextView mamaText;
	private ImageView image;
	private WeatherDataHolder holder;
	private static String GENDER = "male";
	private static String TEMP_UNIT = "celsius";
	private static String TEMPERATURE;
	private static String DESCRIPTION;
	private static String PRECIPITATION;
	
	// Flurry variables
	private static final String FSETTINGS = "Settings";
	
	// image links
	private final int NIKA = 777;
	private final int ROBERT = 776;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_IN_FRONT = true;
        setContentView(R.layout.activity_main);
        
        holder = new WeatherDataHolder(MainActivity.this);
        
        // get action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle("");
        
        image = (ImageView)findViewById(R.id.main_weather_image);
        mamaText = (TextView)findViewById(R.id.main_mama_advice);
        
        // check if there's an internet connection
        //if (checkInternetConnection()) {
        	//callWeatherService();
            
            IntentFilter filter = new IntentFilter(WeatherService.WEATHER_UPDATE);
        	filter.addCategory(Intent.ACTION_DEFAULT);
        	receiver = new WeatherReceiver();
        	registerReceiver(receiver, filter);
        /*
        } else {
        	openAlertDialogBox();
        }        
        */
    }
    
    public void callWeatherService() {
    	Intent intent = new Intent(MainActivity.this, WeatherService.class);
        startService(intent);
    }
    
    public class WeatherReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// get current temperature and description
			TEMPERATURE = WeatherDataHolder.getTemperature(TEMP_UNIT);			
			DESCRIPTION = WeatherDataHolder.getWeatherDescription();
			PRECIPITATION = WeatherDataHolder.getPrecipMM();
			double precip = Double.parseDouble(PRECIPITATION);
			
			// refresh menu
			invalidateOptionsMenu();
			
			// set image
			mamaText.setText(setImage());
			if (precip > 0.1) {
				mamaText.setText(setImage()+holder.RAIN_TEXT);
			}
			image.setOnClickListener(listener);
		}
    }
    
    /** Set image on MainActivity. */
    public String setImage() {    	
    	int tempCat;
		
		if (TEMPERATURE == null) {
			//Log.e(TAG, "temperature NULL!!!!");
			tempCat = 15;
		} else {
			tempCat = Integer.parseInt(TEMPERATURE);
		}
    	
    	// set image based on temperature category
		switch (holder.getTemperatureCategory(tempCat, TEMP_UNIT)){
		case WeatherDataHolder.FREEZING:
			if (GENDER.equals("female")) {
				image.setImageResource(R.drawable.freezing);
				image.setTag(NIKA);
			} else {
				image.setImageResource(R.drawable.mfreezing);
				image.setTag(ROBERT);
			}				
			mamaText.setText(holder.FREEZING_TEXT);
			return holder.FREEZING_TEXT;
		case WeatherDataHolder.COLD:
			if (GENDER.equals("female")) {
				image.setImageResource(R.drawable.cold);
				image.setTag(NIKA);
			} else {
				image.setImageResource(R.drawable.mcold);
				image.setTag(ROBERT);
			}
			mamaText.setText(holder.COLD_TEXT);
			return holder.COLD_TEXT;
		case WeatherDataHolder.MEDIUM:
			if (GENDER.equals("female")) {
				image.setImageResource(R.drawable.medium);
				image.setTag(NIKA);
			} else {
				image.setImageResource(R.drawable.mmedium);
				image.setTag(ROBERT);
			}
			mamaText.setText(holder.MEDIUM_TEXT);
			return holder.MEDIUM_TEXT;
		case WeatherDataHolder.WARM:
			if (GENDER.equals("female")) {
				image.setImageResource(R.drawable.warm);
				image.setTag(NIKA);
			} else {
				image.setImageResource(R.drawable.mwarm);
				image.setTag(ROBERT);
			}
			mamaText.setText(holder.WARM_TEXT);
			return holder.WARM_TEXT;
		case WeatherDataHolder.HOT:
			if (GENDER.equals("female")) {
				image.setImageResource(R.drawable.hot);
				image.setTag(NIKA);
			} else {
				image.setImageResource(R.drawable.mhot);
				image.setTag(ROBERT);
			}
			mamaText.setText(holder.HOT_TEXT);
			return holder.HOT_TEXT;
		}
		return null;
    }
    
    OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			int pogoj = (Integer) view.getTag();
			
			switch(pogoj) {
			case NIKA:
				//Log.i(TAG, "nika");
				visitBlogDialogBox("Nika", "http://nikadressed.blogspot.com/");
				break;
			case ROBERT:
				//Log.i(TAG, "robert");
				visitBlogDialogBox("Robert", "http://cargocollective.com/robertdragan");
				break;
			}
		}};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        
        String unitSign;
        if (TEMP_UNIT.equals("celsius")) {
        	unitSign = "�C";
        } else {
        	unitSign = "�F";
        }
        
        menu.add(WeatherDataHolder.getWeatherDescription()+", "+
        		WeatherDataHolder.getTemperature(TEMP_UNIT)+unitSign)
        		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //menu.add(WeatherDataHolder.getWeatherDescription()).setIcon(R.drawable.ic_launcher).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
        	//Log.i(TAG, "settings");
        	FlurryAgent.logEvent(FSETTINGS);
        	Intent intent = new Intent(this, Preference.class);
        	startActivity(intent);
        	return true;
        case R.id.menu_about:
        	Intent intent2 = new Intent(this, About.class);
        	startActivity(intent2);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.setLogEvents(true);
 	   FlurryAgent.onPageView();
	   FlurryAgent.onStartSession(this, "HCYDQK9CQZK6MF2P7FJS");
	   // additional code
	}
    
    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
       // additional code
    }
    
    private int weatherServiceConditions() {
    	if (checkInternetConnection()) {
    		LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    		boolean networkLocation = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    		if (networkLocation) {
    			return 1;
    		} else {
    			return 3;
    		}
    	} else {
    		return 2;
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	IS_IN_FRONT = true;
    	
    	switch (weatherServiceConditions()) {
    	case 1:
    		callWeatherService();
    		break;
    	case 2:
    		openAlertDialogBox();
    		break;
    	case 3:
    		openLocationAlertDialogBox();
    		break;
    	}
   	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	boolean check = prefs.getBoolean("checkbox_notification_preference", true);
    	GENDER = prefs.getString("preference_gender", "male");
    	TEMP_UNIT = prefs.getString("preference_temperature", "celsius");
    	
    	String intervalString = prefs.getString("preference_notification_interval", "122");
    	
    	if (GENDER.equals("male")) {
			FlurryAgent.setGender(Constants.MALE);
		} else if (GENDER.equals("female")) {
			FlurryAgent.setGender(Constants.FEMALE);
		}
    	
    	if (check) {
    		FlurryAgent.logEvent("Notifications on");
    	} else {
    		FlurryAgent.logEvent("Notifications off");
    	}
    	
    	if (TEMP_UNIT.equals("celsius")) {
    		FlurryAgent.logEvent("Celsius");
    	} else {
    		FlurryAgent.logEvent("Fahrenheit");
    	}
    	
    	FlurryAgent.logEvent("Interval "+intervalString);
    	
    	setImage();
    	//Log.i(TAG, "Gender: "+GENDER);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	IS_IN_FRONT = false;
    }

    @Override
    public void onDestroy() {  
    	try {
			this.unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.e(TAG, "Receiver not registered: "+e);
		}  
    	super.onDestroy();    	 	
    }
    
    /** test if there's internet connection */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.e(TAG, "no internet connection");
            return false;
        }
    }
    
    /** Opens alert dialog box for location settings. */
    private void openAlertDialogBox() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle(MainActivity.this.getString(R.string.connection_error_title));
		alert.setMessage(MainActivity.this.getString(R.string.connection_error_text));
		alert.setNegativeButton(MainActivity.this.getString(R.string.connection_exit), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainActivity.this.finish();
			}});
		alert.setPositiveButton(MainActivity.this.getString(R.string.connection_settings), new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				//Log.i("TAG", "Gremo v nastavitve!");
				startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				MainActivity.this.finish();
			}
		}).show();
    }
    
    /** Opens alert dialog box for location settings. */
    private void openLocationAlertDialogBox() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle(MainActivity.this.getString(R.string.error_location));
		alert.setMessage(MainActivity.this.getString(R.string.error_location_text));
		alert.setNegativeButton(MainActivity.this.getString(R.string.connection_exit), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainActivity.this.finish();
			}});
		alert.setPositiveButton(MainActivity.this.getString(R.string.connection_settings), new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				//Log.i("TAG", "Gremo v nastavitve!");
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				MainActivity.this.finish();
			}
		}).show();
    }
    
    /** AlertBox that asks user if he really wants to visit model's blog. */
    private void visitBlogDialogBox(String bloggerName, final String URL) {
    	AlertDialog.Builder alertBlog = new AlertDialog.Builder(MainActivity.this);
    	alertBlog.setTitle(bloggerName);
    	alertBlog.setMessage(MainActivity.this.getString(R.string.blogger_text1)+" "+bloggerName+MainActivity.this.getString(R.string.blogger_text2)+" "+bloggerName+MainActivity.this.getString(R.string.blogger_text3));
    	alertBlog.setNegativeButton(MainActivity.this.getString(R.string.blogger_back), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	alertBlog.setPositiveButton(MainActivity.this.getString(R.string.blogger_visit_blog), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				visitBlog(URL);
			}
		}).show();
    }
    
    /** Go to blog. */
    private void visitBlog(String URL) {
    	Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(URL));
        startActivity(intent);
    }
}