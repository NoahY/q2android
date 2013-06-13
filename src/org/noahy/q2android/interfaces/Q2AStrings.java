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
	public static final int UPDATED = 0;
	public static final int CREATED = 1;
	public static final int HOTNESS = 2;
	public static final int FAVORITES = 3;
	public static final int VIEWS = 4;
	public static final int NETVOTES = 5;
	public static final int ACOUNT = 6;
	public static final int FLAGCOUNT = 7;

	
	public static final String[] FILTERS = {"updated", "created", "hotness", "favorites", "views", "netvotes", "acount", "flagcount"};
	public static final Integer[] STREAMS = {UPDATED, CREATED, HOTNESS, FAVORITES, VIEWS, NETVOTES, ACOUNT, FLAGCOUNT};
	public static final Integer[] STRINGS = {R.string.updated, R.string.created, R.string.hotness, R.string.favorites, R.string.views, R.string.netvotes, R.string.acount, R.string.flagcount};
	
	public static ArrayList<String> getFilterDisplayStrings(Context context) {
		ArrayList<String> array = new ArrayList<String>();
		
		for(int string: STREAMS) {
			array.add(context.getString(STRINGS[string]));
		}
		return array;
	}

	
	public static String getMetaString(Activity activity, HashMap<?, ?> entryMap, boolean getOther) {
    	String metaorder = (String)entryMap.get("meta_order");
    	
    	String who = "who";
    	String what = "what";
    	String where = "where";
    	String when = "when";
    	
    	if(entryMap.containsKey("who_2") && getOther) { // updated
    		who += "_2";
    		what += "_2";
    		where += "_2";
    		when += "_2";
    	}
    	
    	String wheres = "";
    	if(entryMap.containsKey(where)) {
	    	HashMap<?, ?> whereMap = (HashMap<?, ?>)entryMap.get(where);
	    	if(whereMap.get("data") instanceof String)
	    		wheres = (String)whereMap.get("data");
	    	if(whereMap.get("prefix") instanceof String)
	    		wheres = whereMap.get("prefix") + wheres;
	    	if(whereMap.get("suffix") instanceof String)
	    		wheres += whereMap.get("suffix");
    	}

    	String whats = "";
    	if(entryMap.get(what) instanceof String){
        	whats = (String)entryMap.get(what);
    	}

    	String whens = "";
    	if(entryMap.containsKey(when)) {
	    	HashMap<?, ?> whenMap = (HashMap<?, ?>)entryMap.get(when);
	    	if(whenMap.get("data") instanceof String)
	    		whens = (String) whenMap.get("data");
	    	if(whenMap.get("prefix") instanceof String)
	    		whens = whenMap.get("prefix") + whens;
	    	if(whenMap.get("suffix") instanceof String)
	    		whens += whenMap.get("suffix");
    	}    	

    	String whos = "";
    	if(entryMap.containsKey(who)) {
			HashMap<?, ?> whoMap = (HashMap<?, ?>)entryMap.get(who);
	    	if(whoMap.get("data") instanceof String)
	    		whos = (String) whoMap.get("data");
	    	if(whoMap.get("prefix") instanceof String)
	    		whos = whoMap.get("prefix") + whos;
	    	if(whoMap.get("suffix") instanceof String)
	    		whos += whoMap.get("suffix");
    	}
    	
    	String meta = metaorder.replace("^", " ^")
    			.replace("^who", whos)
    			.replace("^when", whens)
    			.replace("^what", whats)
    			.replace("^where", wheres)
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
