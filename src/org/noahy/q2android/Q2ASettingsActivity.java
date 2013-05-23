package org.noahy.q2android;


import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputType;
import com.actionbarsherlock.view.MenuItem;


public class Q2ASettingsActivity extends SherlockPreferenceActivity {
	
	private Context context;
	private static SharedPreferences prefs;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		this.context = getApplicationContext();
		addPreferencesFromResource(R.xml.preferences);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		final EditTextPreference streamPref = (EditTextPreference)findPreference("stream_max");
		final EditTextPreference contentPref = (EditTextPreference)findPreference("content_max");
		
		setupEditTextPreference(streamPref,"20", null);
		setupEditTextPreference(contentPref,null, getString(R.string.contentMaxDesc));

		streamPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		contentPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
	}
	protected String TAG = "Q2ASettingsActivity";


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            finish();
	            return true;

			default:
				return false;
	    }
	}	
	
	public void setupEditTextPreference(final EditTextPreference etp, final String def, final String desc) {
		if(def != null) {
			if(etp.getText() == null || etp.getText().equals(""))
				etp.setText(def);
			etp.setSummary(etp.getText());
		}
		else if(etp.getText() != null && !etp.getText().equals("")) {
			etp.setSummary(etp.getText());
		}
		
		
		etp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					final Object newValue) {
				if((newValue == null || ((String)newValue).length() == 0) && desc != null)
					etp.setSummary(desc);
				else
					etp.setSummary((String) newValue);

				return true;
			}
			
		});			
	}
}
