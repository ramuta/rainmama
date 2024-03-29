package com.rainmama.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rainmama.R;

import android.content.Context;
import android.util.Log;

public class WeatherDataHolder {
	private static final String TAG = "WeatherDataHolder";
	
	// temperature categories
	public static final int FREEZING = 101;
	public static final int COLD = 202;
	public static final int MEDIUM = 250;
	public static final int WARM = 303;
	public static final int HOT = 404;
	
	public static String FREEZING_TEXT;
	public static String COLD_TEXT;
	public static String MEDIUM_TEXT;
	public static String WARM_TEXT;
	public static String HOT_TEXT;	
	public static String RAIN_TEXT;
	
	// saved data
	private static String weatherDesc;
	private static String currCelsius;
	private static String currFahrenheit;
	private static String precipMM;
	
	private Context c;
	
	public WeatherDataHolder(Context c) {
		super();
		
		this.c = c;
		
		FREEZING_TEXT = c.getString(R.string.rainmama_freezing);
		COLD_TEXT = c.getString(R.string.rainmama_cold);
		MEDIUM_TEXT = c.getString(R.string.rainmama_medium);
		WARM_TEXT = c.getString(R.string.rainmama_warm);
		HOT_TEXT = c.getString(R.string.rainmama_hot);
		RAIN_TEXT = c.getString(R.string.rainmama_rain);
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
			
			// current temperature in celsius, fahrenheit and precipitation
			setCurrCelsius(currConditionObject.getString("temp_C"));
			setCurrFahrenheit(currConditionObject.getString("temp_F"));
			setPrecipMM(currConditionObject.getString("precipMM"));
			
			// weather description (sunny, rain etc.)
			JSONArray weatherDescArray = currConditionObject.getJSONArray("weatherDesc");
			JSONObject weatherDescObject = weatherDescArray.getJSONObject(0);
			setWeatherDesc(weatherDescObject.getString("value"));
		} catch (JSONException e) {
			Log.e(TAG, "JSON ex: "+e);
		}
	}
	
	/** Returns in which category certain temperature is in. */
	public int getTemperatureCategory(int temperature, String unit) {
		if (unit.equals("celsius")) {
			if (temperature <= 5) {
				return FREEZING;
			} else if (temperature > 5 && temperature <= 12) {
				return COLD;
			} else if (temperature > 12 && temperature <= 18) {
				return MEDIUM;
			} else if (temperature > 18 && temperature <= 25) {
				return WARM;
			} else if (temperature > 25) {
				return HOT;
			}
		} else {
			if (temperature <= 41) {
				return FREEZING;
			} else if (temperature > 41 && temperature <= 54) {
				return COLD;
			} else if (temperature > 54 && temperature <= 64) {
				return MEDIUM;
			} else if (temperature > 64 && temperature <= 77) {
				return WARM;
			} else if (temperature > 77) {
				return HOT;
			}
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
	public static String getTemperature(String tempUnit) {
		if (tempUnit.equals("celsius")) {
			return currCelsius;
		}
		else if (tempUnit.equals("fahrenheit")) {
			return currFahrenheit;
		}
		return null;
	}

	/**
	 * @param currCelsius the currCelsius to set
	 */
	private static void setCurrCelsius(String currCelsius) {
		WeatherDataHolder.currCelsius = currCelsius;
	}

	/**
	 * @param currFahrenheit the currFahrenheit to set
	 */
	public static void setCurrFahrenheit(String currFahrenheit) {
		WeatherDataHolder.currFahrenheit = currFahrenheit;
	}

	/**
	 * @return the precipMM
	 */
	public static String getPrecipMM() {
		return precipMM;
	}

	/**
	 * @param precipMM the precipMM to set
	 */
	public static void setPrecipMM(String precipMM) {
		WeatherDataHolder.precipMM = precipMM;
	}
}
