/**
 * This file is part of Jarvis for Android, an app for managing your server (files, talks...).
 *
 * Copyright (c) 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 *
 * LICENSE:
 *
 * Jarvis for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Jarvis for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 */
package mercandalli.com.jarvis.notificationpush;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import mercandalli.com.jarvis.ui.activity.Application;

public class NotificationPush {
	public static GoogleCloudMessaging gcm;
	public static String regId;

    public static  final String REG_ID = "regId";
    public static  final String APP_VERSION = "appVersion";    

    public static AsyncTask<Void, Void, String> shareRegidTask;

    Application app;
    
	public static void mainActNotif(final Application app) {

		final Context context = app;
		shareRegidTask = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
                app.getConfig().setUserRegId(regId);
				return "";
			}

			@Override
			protected void onPostExecute(String result) {
				shareRegidTask = null;
			}
		};
		shareRegidTask.execute(null, null, null);
	}	
	
	public static String registerGCM(Application app) {
		gcm = GoogleCloudMessaging.getInstance(app);
		regId = getRegistrationId(app);

		if (TextUtils.isEmpty(regId))
			registerInBackground(app);
		else
			mainActNotif(app);
		return regId;
	}

	public static String getRegistrationId(Activity activity) {
		final SharedPreferences prefs = activity.getSharedPreferences(Application.class.getSimpleName(), Context.MODE_PRIVATE);
		String registrationId = prefs.getString(REG_ID, "");
		if (registrationId.isEmpty())
			return "";
		int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(activity);
		if (registeredVersion != currentVersion)
			return "";
		return registrationId;
	}

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void registerInBackground(final Application app) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null)
						gcm = GoogleCloudMessaging.getInstance(app);
					regId = gcm.register(Config.GOOGLE_PROJECT_ID);
					msg = "Device registered, registration ID=" + regId;

					storeRegistrationId(app, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				} catch (Exception ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				mainActNotif(app);
			}
		}.execute(null, null, null);
	}

	public static void storeRegistrationId(Application app, String regId) {
		final SharedPreferences prefs = app.getSharedPreferences(Application.class.getSimpleName(), Context.MODE_PRIVATE);
		int appVersion = getAppVersion(app);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(REG_ID, regId);
		editor.putInt(APP_VERSION, appVersion);
		editor.commit();
	}
}
