package com.rainmama.other;

import com.rainmama.services.WeatherService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeatherAlarmReceiver extends BroadcastReceiver {
	public static final String ACTION_REFRESH_WEATHER_ALARM = "com.example.getweather.ACTION_REFRESH_WEATHER_ALARM";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent weatherIntent = new Intent(context, WeatherService.class);
		context.startService(weatherIntent);
	}
}
