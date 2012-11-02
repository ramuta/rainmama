package com.rainmama.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.rainmama.R;
import com.rainmama.activities.MainActivity;
import com.rainmama.data.UrlHolder;
import com.rainmama.data.WeatherDataHolder;
import com.rainmama.other.WeatherAlarmReceiver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class WeatherService extends Service {
	private static final String TAG = "Weather Service";
	
	// broadcast intent
	public static final String WEATHER_UPDATE = "weatherupdate";
	
	// location variables
	private LocationManager locationManager;
	
	// connection variables
	private InputStream is = null;
	private StringBuilder sb = null;
	
	// weather data
	private String weatherResponse;
	private WeatherDataHolder weatherHolder;
	
	// notification
	private String svcName = Context.NOTIFICATION_SERVICE;
	private NotificationManager notifManager;
	private int notifIcon = R.drawable.icon;
	private CharSequence notifTickerText = "RainMama says";
	private long when;
	private Notification notification;
	private static final int NOTIF_ID = 1;
	
	// shared preferences
	private static String TEMP_UNIT = "celsius";
	//public final static String PREF_AUTO_UPDATE = "autoupdate";
	
	// alarm for regular updates
	private AlarmManager alarms;
	private PendingIntent alarmIntent;
	private String alarmAction;
	
	// shared prefs
	private SharedPreferences prefs;
	private static final String SAVED_TEMP_CAT = "savedtempcat";
	private static final String SAVED_PRECIP = "savedprecip";
	private int lastTempCat;
	private int newTempCat;
	private float lastPrecip;
	private static float newPrecip;
	private String intervalString;
	
	@Override
	public void onCreate() {
		weatherHolder = new WeatherDataHolder(getApplicationContext());
		alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarmAction = WeatherAlarmReceiver.ACTION_REFRESH_WEATHER_ALARM;
		Intent intentToFire = new Intent(alarmAction);
		alarmIntent = PendingIntent.getBroadcast(WeatherService.this, 0, intentToFire, 0);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.i(TAG, "Service started");
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		// retrieve Shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	boolean autoUpdate = prefs.getBoolean("checkbox_notification_preference", true); // notifs on/off
    	TEMP_UNIT = prefs.getString("preference_temperature", "celsius");
    	intervalString = prefs.getString("preference_notification_interval", "1"); // selected interval
    	lastTempCat = prefs.getInt(SAVED_TEMP_CAT, 0);
    	lastPrecip = prefs.getFloat(SAVED_PRECIP, 0);
    	
    	int interval = Integer.parseInt(intervalString); // if interval == 1, potem objavi notif samo èe je sprememba vremena (ogled na 60 min)
    	
    	if (interval == 1) {
    		interval = 60;
    	}
    	
    	//interval = 1; // TODO

		// if autoUpdate is on, repeat on selected time period
		if (autoUpdate) {
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			long timeToRefresh = SystemClock.elapsedRealtime() + interval*60*1000;
			alarms.setRepeating(alarmType, timeToRefresh, interval*60*1000, alarmIntent);
		} else {
			alarms.cancel(alarmIntent);
		}
		
		// if there is a connection to the internet, do the weather task
		if (checkInternetConnection()) {
			new getWeatherTask().execute();
		} else {
			Log.e(TAG, "Problem with internet connection.");
		}		
		
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/** Get user's location: longitude and latitude. Then start getWeather method. */
	private void getUsersLocation() {
		Criteria criteria = new Criteria(); // criterias for locating, currently non is set
		
		String best = locationManager.getBestProvider(criteria, true); // get best location provider (it's network)
		
		// get last known location - probably good enough, but has to be further tested
		Location location = locationManager.getLastKnownLocation(best);
		//String last = location.toString();
		
		double uLat = location.getLatitude();
		double uLong = location.getLongitude();
		//Log.i(TAG, "lat: "+uLat+", long: "+uLong);
		
		getWeather(uLat, uLong);
	}
	
	/** Get weather information based on user's latitude and longitude.
	 * 
	 * @param latitude User's latitude
	 * @param longitude User's longitude */
	private void getWeather(double latitude, double longitude) {
		String weatherUrl = UrlHolder.getWeatherApiUrl(latitude, longitude);
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	   	
    	// http post 
    	try {
    	     HttpClient httpclient = new DefaultHttpClient();
    	     HttpPost httppost = new HttpPost(weatherUrl);
    	     httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    	     HttpResponse response = httpclient.execute(httppost);
    	     HttpEntity entity = response.getEntity();
    	     is = entity.getContent();
    	} catch(Exception e){
    	     Log.e(TAG, "Error in http connection"+e.toString());
    	}
    	
    	//convert response to string
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
    		sb = new StringBuilder();
    	    sb.append(reader.readLine() + "\n");
	        String line="0";	        
	        while ((line = reader.readLine()) != null) {
	        	sb.append(line + "\n");
	        }        
	        is.close();
	        weatherResponse = sb.toString(); // odgovor (rezultat) ki ga dobimo po poslanem zahtevku
	        Log.i(TAG, "WEATHER: " + weatherResponse);	        
	        weatherHolder.parseWeatherData(weatherResponse); // parse reponse
	        String sTemperature = weatherHolder.getTemperature(TEMP_UNIT);
	        newTempCat = weatherHolder.getTemperatureCategory(Integer.parseInt(sTemperature), TEMP_UNIT);	        
	        sendWeatherBroadcastIntent();
    	} catch(Exception e){
    		Log.e(TAG, "Error converting result "+e.toString());
    	}
	}
	
	/** Save temperature category to SharedPrefs */
	private void saveTempCat(int tempCat) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(SAVED_TEMP_CAT, tempCat);
		editor.commit();
	}
	
	/** Save last precip to SharedPrefs */
	private void saveLastPrecip(float precip) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(SAVED_PRECIP, precip);
		editor.commit();
	}
	
	/** Send weather broadcast intent to main activity. */
	private void sendWeatherBroadcastIntent() {
		Intent weatherBroadcastIntent = new Intent(WEATHER_UPDATE);
		sendBroadcast(weatherBroadcastIntent);
	}
	
	/** Task for getting user's location and weather info. */
	private class getWeatherTask extends AsyncTask<String, Void, Bitmap> {
	     protected Bitmap doInBackground(String... mark) {
	    	 getUsersLocation();
	         return null;
	     }
	     
	     protected void onPostExecute(Bitmap result) {
	    	 //Log.i(TAG, "onPostExecute");
	    	 if (!MainActivity.IS_IN_FRONT) { // if MainActivity is open/running/front, then don't set the notification!
	    		 try {
					newPrecip = Float.parseFloat(weatherHolder.getPrecipMM());
				} catch (NumberFormatException e) {
					newPrecip = (float) 0.0;
				}
	    		 
	    		 Log.i(TAG, "intervalString: "+intervalString);
	    		 
	    		 //newPrecip = (float) 0.1; // TODO
	    		 if (!intervalString.equals("1")) {
	    			 Log.i(TAG, "Interval is NOT set to Only if weather changes");
	    			 if (newPrecip >= 0.1) {
	    				 showNotif(true);
	    			 } else {
	    				 showNotif(false);
	    			 }
	    		 } else {
	    			 Log.i(TAG, "Interval is set to Only if weather changes");
		    		 if (newPrecip >= 0.1 && lastPrecip == 0) {
		    			 showNotif(true);
		    			 saveLastPrecip(newPrecip);
		    		 } else {
		    			 if (newTempCat != lastTempCat && lastTempCat != 0) {
				    		showNotif(false);
			    		 }
		    		 } 
	    		 }
	    	 }
	         stopSelf(); // stop service
	     }
	}
	
	/** Show notification. */
	private void showNotif(boolean precip) {
		setNotifications(precip);
        notifManager.notify(NOTIF_ID, notification);
        saveTempCat(newTempCat);
	}
	
	/** Set notification about current weather. If there's precipitation, set them true. */	
	private void setNotifications(boolean precip) {
		notifManager = (NotificationManager)getSystemService(svcName);
		when = System.currentTimeMillis();
		Context context = getApplicationContext();
		String contentText;
		if (precip == false) {
			contentText = getNotifText(weatherHolder.getTemperature(TEMP_UNIT));
		} else {
			contentText = context.getString(R.string.rainmama_rain);
		}			
		CharSequence contentTitle = "RainMama says:";		
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		//notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		if (Build.VERSION.SDK_INT < 16) {
			notification = new Notification(notifIcon, notifTickerText, when);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		} else {
			setNewNotifs(context, contentTitle, contentIntent, contentText);
		}
	}
	
	@TargetApi(16)
	public void setNewNotifs(Context context, CharSequence contentTitle, PendingIntent contentIntent, String contentText) {
		notification = new Notification.Builder(context)
        .setContentTitle(contentTitle)
        .setWhen(when)
        .setContentIntent(contentIntent)
        .setContentText(contentText)
        .setSmallIcon(R.drawable.icon)
        .setAutoCancel(true)
        .build();
	}
	
	/** Set notificiation text based on the current temperature. */
	private String getNotifText(String temperature) {
		switch (weatherHolder.getTemperatureCategory(Integer.parseInt(temperature), TEMP_UNIT)){
		case WeatherDataHolder.FREEZING:
			return weatherHolder.FREEZING_TEXT;
		case WeatherDataHolder.COLD:
			return weatherHolder.COLD_TEXT;
		case WeatherDataHolder.MEDIUM:
			return weatherHolder.MEDIUM_TEXT;
		case WeatherDataHolder.WARM:
			return weatherHolder.WARM_TEXT;
		case WeatherDataHolder.HOT:
			return weatherHolder.HOT_TEXT;
		}
		return null;
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
}