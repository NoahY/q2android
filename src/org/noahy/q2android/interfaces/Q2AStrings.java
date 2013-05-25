package org.noahy.q2android.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import org.noahy.q2android.R;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class Q2AStrings {
	
	protected static String TAG = "Q2AStrings";
	public static final int CREATED = 1;
	public static final int UPDATED = 2;
	public static final int ACOUNT = 3;
	public static final int FLAGCOUNT = 4;
	public static final int NETVOTES = 5;
	public static final int VIEWS = 6;
	public static final int HOTNESS = 7;

	public static final Integer[] STREAMS = {CREATED, UPDATED, HOTNESS, VIEWS, NETVOTES, ACOUNT, FLAGCOUNT};
	
	public static int getFilterDisplayString(int filter) {
		int string = 0;
		
		switch(filter) {
			case CREATED:
				return R.string.created;
			case UPDATED:
				return R.string.updated;
			case ACOUNT:
				return R.string.acount;
			case FLAGCOUNT:
				return R.string.flagcount;
			case NETVOTES:
				return R.string.netvotes;
			case VIEWS:
				return R.string.views;
			case HOTNESS:
				return R.string.hotness;
		}
		
		return string;
	}
	
	public static String getFilterRequestString(int filter) {
		String string = "";
		
		switch(filter) {
			case CREATED:
				return "created";
			case UPDATED:
				return "updated";
			case ACOUNT:
				return "acount";
			case FLAGCOUNT:
				return "flagcount";
			case NETVOTES:
				return "netvotes";
			case VIEWS:
				return "views";
			case HOTNESS:
				return "hotness";
		}
		
		return string;
	}

	public static ArrayList<String> getFilterDisplayStrings(Context context) {
		ArrayList<String> array = new ArrayList<String>();
		
		for(int string: STREAMS) {
			array.add(context.getString(getFilterDisplayString(string)));
		}
		return array;
	}

	
	public static String getMetaString(Activity activity, HashMap<?, ?> entryMap) {
    	String metaorder = (String)entryMap.get("meta_order");
    	
    	String where = "";
    	if(entryMap.containsKey("where")) {
	    	HashMap<?, ?> whereMap = (HashMap<?, ?>)entryMap.get("where");
	    	if(whereMap.get("data") instanceof String)
	    		where = (String)whereMap.get("data");
	    	if(whereMap.get("prefix") instanceof String)
	    		where = whereMap.get("prefix") + where;
	    	if(whereMap.get("suffix") instanceof String)
	    		where += whereMap.get("suffix");
    	}

    	String what = "";
    	if(entryMap.get("what") instanceof String)
        	what = (String)entryMap.get("what");

    	String when = "";
    	if(entryMap.containsKey("when")) {
	    	HashMap<?, ?> whenMap = (HashMap<?, ?>)entryMap.get("when");
	    	if(whenMap.get("data") instanceof String)
	    		when = (String) whenMap.get("data");
	    	if(whenMap.get("prefix") instanceof String)
	    		when = whenMap.get("prefix") + when;
	    	if(whenMap.get("suffix") instanceof String)
	    		when += whenMap.get("suffix");
    	}    	

    	String who = "";
    	if(entryMap.containsKey("who")) {
			HashMap<?, ?> whoMap = (HashMap<?, ?>)entryMap.get("who");
	    	if(whoMap.get("data") instanceof String)
	    		who = (String) whoMap.get("data");
	    	if(whoMap.get("prefix") instanceof String)
	    		who = whoMap.get("prefix") + who;
	    	if(whoMap.get("suffix") instanceof String)
	    		who += whoMap.get("suffix");
    	}
    	
    	String meta = metaorder.replace("^", " ^")
    			.replace("^who", who)
    			.replace("^when", when)
    			.replace("^what", what)
    			.replace("^where", where)
    			.replace("HREF=\"./", "HREF=\""+Q2AWebsite.getWebsite(activity));
    	return meta;
	}

	public static Spanned getEntryContent(String string) {
		if(string == null)
			string = "";
		string = string
				.replaceAll("\n", "")
				.replaceAll("</p><p>", "<br/><br/>")
				.replaceFirst(".*<DIV[^>]*>","")
				.replaceFirst("</DIV>[^<]*$", "")
				.replaceAll("</*p>", "");
		//Log.i(TAG,string);
		Spanned content = Html.fromHtml(string);
    	return content;
	}
}
