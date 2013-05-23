package org.noahy.q2android.interfaces;

import java.net.URI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Q2AWebsite {

	// set this if you are hardcoding a website into your app
	public final static String CUSTOM_WEBSITE = null;
	
	public static String getWebsite(Context context) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String website = CUSTOM_WEBSITE  != null ? CUSTOM_WEBSITE : prefs.getString("website", "");
		if(website.length() == 0)
			return null;
	
		website = sanitizeWebsite(website);
		
		return website;
	}

	public static String sanitizeWebsite(String website) {
		if(!website.startsWith("http"))
			website = "http://"+website;

		// potential problems
		
		website = website.replaceAll("\\?.*", "").replaceAll("index.php$","");
		
		if(!website.endsWith("/"))
			website = website+"/";

		return website;

	}

	public static boolean isValidWebsite(String link) {
		try {
			URI.create(link);
			
		} catch(Exception e) {
			return false;
		}

		return true;
	}

	
	
}
