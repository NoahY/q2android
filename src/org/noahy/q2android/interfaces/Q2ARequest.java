package org.noahy.q2android.interfaces;

import java.net.URI;
import java.util.HashMap;
import java.util.Vector;

import org.noahy.q2android.R;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class Q2ARequest {


	public static final int MSG_ERROR = 9999;
	public Vector<String> imageUrl = new Vector<String>();
	Vector<String> selectedCategories = new Vector<String>();
	private static SharedPreferences prefs;
	private static Context context;
	private static String protocol;
	private static int what;
	private static HashMap<String, Object> data;
	public static String error;
	public static String TAG = "Q2ARequest";
	private static Handler handler;



	public Q2ARequest(Context c, Handler h, String _protocol, HashMap<String, Object> _data, int _what) {
		context = c;
		handler = h;
		data = _data;
		protocol = _protocol;
		what = _what;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void execute() {

		new getStreamTask().execute(this);

	}
	
	private static class getStreamTask extends
			AsyncTask<Q2ARequest, Boolean, Boolean> {

		private boolean success = false;
		private Object obj;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = 0;
				msg.obj = obj;
				msg.what = what;
			}
			else {
				msg.arg1 = R.string.error;
				msg.obj = error;
				msg.what = MSG_ERROR;
			}
			handler.sendMessage(msg);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Q2ARequest... streams) {
			if(!isInternetOn()) {
				error = context.getString(R.string.checkSetupInternet);
				return false;
			}
			
			String password = prefs.getString("password", null);
			String username = prefs.getString("username", null);
			String website = Q2AWebsite.getWebsite(context);
			
			if(username == null || website == null || password == null)
				return false;
			
			Object[] params;
			
			params = new Object[] { 
				username,
				password,
				data 
			};
			
			String uris = website+"xml-rpc";
			
			URI uri;
			
			try {
				uri = URI.create(uris);
				
			} catch(Exception e) {
				error = context.getString(R.string.websiteSyntaxError);
				return false;
			}
			

			
			XMLRPCClient client = new XMLRPCClient(uri,
					username, password); // not used
			try {
				obj = client.call(protocol, params);
				success = true;
				return true;
			} catch (final XMLRPCException e) {
				error = e.toString();
				e.printStackTrace();
			}
			return false;
		}

	}

	public static boolean isInternetOn() {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
