package com.rainmama.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WeatherDataHolder {
	private static final String TAG = "WeatherDataHolder";
	
	// temperature categories
	public static final int FREEZING = 101;
	public static final int COLD = 202;
	public static final int WARM = 303;
	public static final int HOT = 404;
	
	public static final String FREEZING_TEXT = "Don't go anywhere without a coat.";
	public static final String COLD_TEXT = "Put something with long sleeves on.";
	public static final String WARM_TEXT = "T-shirt is OK!";
	public static final String HOT_TEXT = "Wear as little as you can. :)";
	
	// saved data
	private static String weatherDesc;
	private static String currCelsius;
	
	public WeatherDataHolder() {
		super();
	}
	
	/** Parse data string received from your weather info provider.
	 * @param weatherResponse String response from weather info provider */
	public void parseWeatherData(String weatherResponse) {
		JSONObject jObject;
		try {
			jObject = new JSONObject(weatherResponse);
			//Log.i(TAG, "jObject: "+jObject);
			
			JSONObject jObject2 = jObject.getJSONObject("data");
			//Log.i(TAG, "jObject2: "+jObject2);
			
			JSONArray currConditionArray = jObject2.getJSONArray("current_condition");
			Log.i(TAG, "currCondition: "+currConditionArray);
			
			JSONObject currConditionObject = currConditionArray.getJSONObject(0);
			
			// current temperature in celsius
			setCurrCelsius(currConditionObject.getString("temp_C"));			
			
			// weather description (sunny, rain etc.)
			JSONArray weatherDescArray = currConditionObject.getJSONArray("weatherDesc");
			JSONObject weatherDescObject = weatherDescArray.getJSONObject(0);
			setWeatherDesc(weatherDescObject.getString("value"));
		} catch (JSONException e) {
			Log.e(TAG, "JSON ex: "+e);
		}
	}
	
	/** Returns in which category certain temperature is in. */
	public int getTemperatureCategory(int temperature) {
		if (temperature <= 5) {
			return FREEZING;
		} else if (temperature > 5 && temperature <= 18) {
			return COLD;
		} else if (temperature > 18 && temperature <= 25) {
			return WARM;
		} else if (temperature > 25) {
			return HOT;
		}
		return 0;
	}

	/** Get weather description, like: sunny, rain, cloudy etc.
	 * 
	 * @return the weatherDesc
	 */
	public static String getWeatherDescription() {
		return weatherDesc;
	}

	/**
	 * @param weatherDesc the weatherDesc to set
	 */
	private static void setWeatherDesc(String weatherDesc) {
		WeatherDataHolder.weatherDesc = weatherDesc;
	}

	/** Get current temperature.
	 * 
	 * @return the currCelsius
	 */
	public static String getTemperature() {
		return currCelsius;
	}

	/**
	 * @param currCelsius the currCelsius to set
	 */
	private static void setCurrCelsius(String currCelsius) {
		WeatherDataHolder.currCelsius = currCelsius;
	}
}
