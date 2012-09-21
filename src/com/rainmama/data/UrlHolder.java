package com.rainmama.data;

public class UrlHolder {
	private static final String API_KEY = "10484b52a8180242121409";
	//private static String weatherApiUrl = "http://free.worldweatheronline.com/feed/weather.ashx?q=Ljubljana,Slovenia&format=json&num_of_days=2&key="+API_KEY;
	
	/** Get WorldWeatherOnline.com API request
	 * @return the weatherApiUrl
	 */
	public static String getWeatherApiUrl(double latitude, double longitude) {
		return "http://free.worldweatheronline.com/feed/weather.ashx?q="+latitude+","+longitude+"&format=json&num_of_days=2&key="+API_KEY;
	}
}
