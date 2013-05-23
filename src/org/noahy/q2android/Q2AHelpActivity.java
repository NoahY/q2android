package org.noahy.q2android;

import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuItem;

public class Q2AHelpActivity extends SherlockActivity {
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		final Q2AHelpActivity context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
	    WebView view = new WebView(this);
	    view.setVerticalScrollBarEnabled(false);

	    ((LinearLayout)findViewById(R.id.webview_container)).addView(view);

	    view.setWebViewClient(new WebViewClient(){
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            if (url != null && url.startsWith("http")) {
	                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	                return true;
	            } else {
	                return false;
	            }
	        }
	    });
	    
	    view.loadData(getString(R.string.help_text), "text/html", "utf-8");
		
		int api = Build.VERSION.SDK_INT;
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		//SharedPreferences.Editor editor = prefs.edit();
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            finish();
	            return true;

			default:
				return false;
	    }
	}	

}
