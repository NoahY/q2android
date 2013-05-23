package org.noahy.q2android.interfaces;

import java.util.HashMap;

import org.noahy.q2android.R;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class Q2AStrings {
	
	protected static String TAG = "Q2AStrings";
	public static final int QUESTIONS = 1;

	public static final Integer[] STREAMS = {QUESTIONS};
	
	public static int getFilterDisplayString(int filter) {
		int string = 0;
		
		switch(filter) {
			
			case QUESTIONS:
				return R.string.questions;
		}
		
		return string;
	}
	
	public static String getFilterRequestString(int filter) {
		String string = "";
		
		switch(filter) {
			case QUESTIONS:
				return "created";
		}
		
		return string;
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
		string = string.replaceAll("\n", "").replaceAll("</p><p>", "<br/><br/>").replaceFirst(".*<DIV CLASS=\"entry-content\"><p>","").replaceFirst("</p></DIV>$", "");
		Spanned content = Html.fromHtml(string);
    	return content;
	}
}
