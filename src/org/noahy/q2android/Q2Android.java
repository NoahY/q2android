
package org.noahy.q2android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.noahy.q2android.interfaces.*;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import android.app.NotificationManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Q2Android extends SherlockListActivity {
	
	protected String TAG = "Q2Android";

	public static String versionName = "0.1";
	
	private static SharedPreferences prefs;
	private static Q2Android activity;
	private StreamListAdapter adapter;
	private String website;

	private ListView listView;
	private ScrollView questionPane;
	private LinearLayout questionPad;

	private TextView questionTitle;
	private TextView questionMeta;
	private TextView questionContent;
	private TextView questionVotes;
	private TextView questionVotesLabel;
	private ImageView questionAvatar;

	private Button questionVoteUp;
	private Button questionVoteDown;
	private ImageView questionVoteSpacer;

	private Button questionEdit;
	private Button questionFlag;
	private Button questionClose;
	private Button questionHide;
	private Button questionAnswer;
	private Button questionComment;
	
	private LinearLayout answerContainer;

	protected static int currentScope;

	public static int NOTIFY_ID = 0;

	private AlarmManager mgr=null;
	private PendingIntent pi=null;

	private Intent intent;

	private MenuItem refreshItem;
	private boolean refreshing;

	protected ArrayList<CharSequence> notificationStrings;
	protected ArrayList<String> notificationLinks;

	protected HashMap<String,Boolean> adminRights = new HashMap<String,Boolean>();

	protected boolean submitting;

	private int lastScope;

	private ActionBar actionBar;

	private HashMap<?,?> currentQuestion;

	private LinearLayout commentContainer;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		actionBar = getSupportActionBar();
		
		actionBar.setHomeButtonEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		questionPane = (ScrollView)findViewById(R.id.q_pane);

		intent = this.getIntent();
		
    	listView = getListView();
		questionPad = (LinearLayout) findViewById(R.id.q_pad);

		questionTitle = (TextView) findViewById(R.id.qtitle);
		questionAvatar = (ImageView) findViewById(R.id.qavatar);
		questionMeta = (TextView) findViewById(R.id.qmeta);
		questionContent = (TextView) findViewById(R.id.qcontent);
		questionVotes = (TextView) findViewById(R.id.qnetvotes);
		questionVotesLabel = (TextView) findViewById(R.id.qvote_label);

		questionVoteUp = (Button) findViewById(R.id.qvote_up_button);
		questionVoteDown = (Button) findViewById(R.id.qvote_down_button);
		questionVoteSpacer = (ImageView) findViewById(R.id.qbutton_spacer);

		questionEdit = (Button) findViewById(R.id.qedit);
		questionFlag = (Button) findViewById(R.id.qflag);
		questionClose = (Button) findViewById(R.id.qclose);
		questionHide = (Button) findViewById(R.id.qhide);
		questionAnswer = (Button) findViewById(R.id.qanswer);
		questionComment = (Button) findViewById(R.id.qcomment);
	
		commentContainer = (LinearLayout) findViewById(R.id.qcomments);
		answerContainer = (LinearLayout) findViewById(R.id.acontainer);

    	currentScope = Q2AStrings.QUESTIONS;
    	lastScope = currentScope;
    	
    	registerForContextMenu(listView);
	    listView.setTextFilterEnabled(true);
	    
	    activity = this;
    	website = Q2AWebsite.getWebsite(this);

    	adjustLayout();
    	
    	if(prefs.getBoolean("auto_update", true) && !getIntent().hasExtra("notification"))
    		refreshStream(currentScope);
    	else if(Q2AWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Intent i = new Intent(this, Q2ALoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
		}
	}

	@Override
	public void onResume(){
		super.onResume();


    	// set up notification
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		
		mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
    	activity = this;
    	String newWebsite = Q2AWebsite.getWebsite(this);
    	adjustLayout();

    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Q2Android","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
    		intent.removeExtra(Intent.EXTRA_TEXT);
    	}
    	
    	if(prefs.getBoolean("interval_sync", false)) {
    		Long interval = Long.parseLong(prefs.getString("sync_interval", "60"))*60*1000;
			Log.i(TAG,interval+"");
    		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()+interval,
				interval,
				pi);
    	}
    	
    	// if website changed
    	
    	if(website != null && !website.equals(newWebsite)) {
    		website = newWebsite;
    	}

	}

	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_main, menu);
	    
	    refreshItem = menu.findItem(R.id.menuStream);
	    if(refreshing)
	    	showRefresh();
	    return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		Intent intent;
		switch (item.getItemId()) {
	        case android.R.id.home:
	        	if(!isQuestion)
	        		finish();
	        	else {
	        		isQuestion = false;
	        		adjustLayout();
	        	}
	            return true;

			case (int)R.id.menuStream:
		    	if(Q2AWebsite.getWebsite(this) == null) {
					Toast.makeText(this, getString(R.string.noWebsite),
							Toast.LENGTH_LONG).show();
					return true;
		    	}
				refreshStream(currentScope);
				break;
			case (int)R.id.menuNew:
				LayoutInflater inflater = activity.getLayoutInflater();
				LinearLayout questionLayout = (LinearLayout) inflater.inflate(R.layout.question_new, null);
				final EditText title = (EditText) questionLayout.findViewById(R.id.title);
				final EditText content = (EditText) questionLayout.findViewById(R.id.content);
				final EditText tags = (EditText) questionLayout.findViewById(R.id.tags);
				new AlertDialog.Builder(activity)
			    .setTitle(R.string.new_question)
			    .setView(questionLayout)
			    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
	
					public void onClick(DialogInterface dialog, int whichButton) {
						HashMap<String,Object> data = new HashMap<String,Object>();
						HashMap<String,Object> info = new HashMap<String,Object>();
						final HashMap<?,?> rawMap = (HashMap<?, ?>) currentQuestion.get("raw");

						info.put("type", "Q");
						info.put("title", title.getText().toString());
						info.put("content", content.getText().toString());
						info.put("tags", tags.getText().toString());

						data.put("action_data", info);
						data.put("action","post");
						data.put("action_id", (String)rawMap.get("postid"));
						getQuestions(data,currentScope);
						
			        }
			    }).setNegativeButton(android.R.string.no, null).show();	
				break;
			case (int)R.id.menuLogin:
				intent = new Intent(this, Q2ALoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, RESULT_LOGIN);
				break;
			case (int)R.id.menuPrefs:
				intent = new Intent(this, Q2ASettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case (int)R.id.menuHelp:
				intent = new Intent(this, Q2AHelpActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;

			default:
				return false;
	    }
		return true;
	}	

	public static final int RESULT_USER = 0;
	public static final int RESULT_LOGIN = 1;
	
	protected void  onActivityResult (int requestCode, int resultCode, Intent  data) {
		
		if(requestCode == RESULT_USER && resultCode != Activity.RESULT_OK)		
			refreshStream(currentScope);
		else if(requestCode == RESULT_LOGIN && resultCode == Activity.RESULT_OK) {
			String ws = data.getStringExtra("website");
			String un = data.getStringExtra("username");
			String pw = data.getStringExtra("password");

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("website", ws);
			editor.putString("username", un);
			editor.putString("password", pw);
			editor.commit();
			refreshStream(currentScope);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.i(TAG,"clicked stream item");
		Object obj = getListView().getItemAtPosition(position);
		if(!(obj instanceof HashMap))
			return;
		currentQuestion = (HashMap<?, ?>) obj;
		isQuestion = true;
		adjustLayout();
		questionPane.scrollTo(0, 0);
		loadQuestion();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getMenuInflater();
       	
		inflater.inflate(R.menu.stream_longclick, menu);
        menu.setHeaderTitle(getString(R.string.q_options));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int index = info.position;
	    final HashMap<?,?> entryMap = (HashMap<?, ?>) getListView().getItemAtPosition(index);
		Intent i;
	    
	    String link;
		link = (String)entryMap.get("url");
		link = link.replaceFirst("^\\./", Q2AWebsite.getWebsite(this));

	    Uri url;
		final EditText input;
		HashMap<String, Object> data;
		switch (item.getItemId()) {
			case R.id.view:
				url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case R.id.share_link:
				i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_TEXT, link);
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, getString(R.string.share_via)));
				return true;
			default:
				break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();
    }

	

	protected void refreshStream(int which) {
		if(refreshing)
			return;

		if(Q2AWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Intent i = new Intent(this, Q2ALoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
			return;
		}
		
		Log.d(TAG,"getting for currentScope of "+which);
		lastScope = which;
		
		getQuestions(new HashMap<String, Object>(), which);

	}
	
	public void getQuestions(HashMap<String, Object> data, int ascope) {
		
		if(refreshing)
			return;
		
		lastScope = ascope;

		Log.i(TAG ,"getting questions for "+currentScope);
		
		data.put("sort", Q2AStrings.getFilterRequestString(ascope));
		data.put("user_data", "true");
		data.put("full", "true");
		data.put("start", 0);
		data.put("size", Integer.parseInt(prefs.getString("stream_max", "20")));
		
		Q2ARequest stream = new Q2ARequest(activity, mHandler, "q2a.getQuestions", data, MSG_QUESTIONS);
		stream.execute();
		showRefresh();
	}

	public static final int MSG_QUESTIONS = 1;
	public static final int MSG_VOTE = 2;
	public static final int MSG_SCOPE = 3;

	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			completeRefresh();

			HashMap<?, ?> map;
			Object obj;
			Object[] list;
			
			String toast = null;
			
			Integer position;
			switch(msg.what) {
				case MSG_QUESTIONS:
					if(!(msg.obj instanceof HashMap)) {
						Log.w(TAG,"message not a map: "+msg.obj.getClass());
						if(msg.obj instanceof String)
							toast = (String) msg.obj;
						setEmptyList();
						break;
					}
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("data");
					
					if(!(obj instanceof Object[])) {
						Log.w(TAG,"data not an Object[]: "+obj.getClass());
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
						break;
					}
					
					list = (Object[]) obj;
					
					if(list.length == 0) {
						Log.w(TAG,"data empty");
						if(map.get("message") instanceof String)
							toast = (String) map.get("message");
						setEmptyList();
						questionPad.setVisibility(View.GONE);
						break;
					}
					
					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("level") instanceof String) {
							
						}
					}

					adapter = new StreamListAdapter(activity,list);
					setListAdapter(adapter);
					
					if(map.containsKey("acted"))
						position = (Integer)map.get("acted");
					else 
						position = 0;

					Object cobj = getListView().getItemAtPosition(position);
					if(!(cobj instanceof HashMap))
						return;
					currentQuestion = (HashMap<?, ?>) cobj;
					isQuestion = true;
					adjustLayout();
					loadQuestion();
						
					
					toast = getString(R.string.updated);
					
					currentScope = lastScope;
					actionBar.setTitle(getString(R.string.app_name)+" - "+getString(Q2AStrings.getFilterDisplayString(currentScope)));
					break;
				case MSG_SCOPE:
					if((msg.obj instanceof String)) { 
						if(((String) msg.obj).startsWith("http")) {
							Uri url = Uri.parse((String) msg.obj);
							Intent i = new Intent(Intent.ACTION_VIEW, url);
							activity.startActivity(i);							
						}
					}
					else
						refreshStream(msg.arg1);

					return;
				default: 
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			submitting = false;
			
			if(toast != null)
				Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();

				
		}
    };

	private boolean isQuestion;

	private void adjustLayout() {
		DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	int width = metrics.widthPixels; 	
    	boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && width > 600;

    	if(land) {
    		questionPane.setVisibility(View.VISIBLE);
    		listView.setVisibility(View.VISIBLE);
    	}
    	else if(isQuestion){
    		questionPane.setVisibility(View.VISIBLE);
    		listView.setVisibility(View.GONE);
    		actionBar.setDisplayHomeAsUpEnabled(false);
    	}
    	else {
    		questionPane.setVisibility(View.GONE);
    		listView.setVisibility(View.VISIBLE);
    		actionBar.setDisplayHomeAsUpEnabled(true);
    	}
	}

	protected void doSlideToggle(View view) {
		if(view.getVisibility() == View.GONE)
			doSlideDown(view);
		else
			doSlideUp(view);
	}

	private void setEmptyList() {
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
	}

	public void cancelAlarm(View v) {
		mgr.cancel(pi);
	}

	public void doSlideDown(View view){
		if(view.getVisibility() == View.VISIBLE || view.getAnimation() != null)
			return;
		view.setVisibility(View.VISIBLE);
		Animation slideDown = Q2AAnimations.slideDown(); 
		view.startAnimation(slideDown);
	}

	public void doSlideUp(View view){
		if(view.getVisibility() == View.GONE || view.getAnimation() != null)
			return;

		Animation slideUp = Q2AAnimations.slideUp(view); 
		view.startAnimation(slideUp);
	}

	public void showRefresh() {
		if(refreshItem == null) {
			Log.i(TAG,"not ready to show refresh");
			refreshing = true;
			return;
		}
		//Log.i(TAG,"showing refresh");
		
		/* Attach a rotating ImageView to the refresh item as an ActionView */
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView) inflater.inflate(R.layout.rotate, null);
		
		Animation rotation = AnimationUtils.loadAnimation(this, R.animator.rotate);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);
		refreshItem.setActionView(iv);
	}
	public void completeRefresh() {
    	refreshing = false;
		if(refreshItem == null || refreshItem.getActionView() == null)
			return;	
		
		refreshItem.getActionView().clearAnimation();
		refreshItem.setActionView(null);
	}

	
	protected void loadQuestion() {
		try {
			
			final HashMap<?,?> rawMap = (HashMap<?, ?>) currentQuestion.get("raw");
			final int questionId = Integer.parseInt((String) rawMap.get("postid")); 
			
    		String title = (String) rawMap.get("title");

    		String votes = (String) rawMap.get("netvotes");
        	if(!votes.startsWith("-") && !votes.equals("0"))
        		votes = "+"+votes;

    		questionVoteDown.setVisibility(View.VISIBLE);
    		questionVoteUp.setVisibility(View.VISIBLE);
    		questionVoteSpacer.setVisibility(View.GONE);
        	
        	Boolean voted = true;
			String uservote = (String) rawMap.get("uservote");
        	if(uservote.equals("1"))
        		questionVoteDown.setVisibility(View.GONE);
        	else if(uservote.equals("-1"))
        		questionVoteUp.setVisibility(View.GONE);
        	else if(currentQuestion.get("vote_state").equals("disabled")) {
        		questionVoteDown.setVisibility(View.GONE);
        		questionVoteUp.setVisibility(View.GONE);
        	}
        	else {
        		questionVoteSpacer.setVisibility(View.VISIBLE);
        		voted = false;
        	}
        	
        	final int upvote = voted?0:1;
        	final int downvote = voted?0:-1;
        	
			Spanned content = Q2AStrings.getEntryContent((String) currentQuestion.get("content"));

			String meta = Q2AStrings.getMetaString(activity, currentQuestion);
			Spanned metas = Html.fromHtml(meta);

			// buttons
			
			questionVoteUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();

					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("vote", upvote);
					info.put("type", "Q");
					
					data.put("action_data", info);
					data.put("action","vote");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestions(data,currentScope);
					
				}
				
			});

			questionVoteDown.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("vote", downvote);
					info.put("type", "Q");
					
					data.put("action_data", info);
					data.put("action","vote");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestions(data,currentScope);
					
				}
				
			});
			questionAnswer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					final EditText input = new EditText(activity);
					input.setHeight(200);
					input.setGravity(Gravity.TOP);
					new AlertDialog.Builder(activity)
				    .setTitle(R.string.post_answer)
				    .setView(input)
				    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("content", input.getText().toString());
							info.put("type", "A");
							
							data.put("action_data", info);
							data.put("action","post");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestions(data,currentScope);
				        }
				    }).setNegativeButton(android.R.string.no, null).show();	
				}
			});
			
			questionComment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					final EditText input = new EditText(activity);
					input.setHeight(200);
					input.setGravity(Gravity.TOP);
					new AlertDialog.Builder(activity)
				    .setTitle(R.string.post_comment)
				    .setView(input)
				    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("content", input.getText().toString());
							info.put("type", "C");
							info.put("parentid", questionId);
							
							data.put("action_data", info);
							data.put("action","post");
							data.put("action_id", questionId);
							getQuestions(data,currentScope);
				        }
				    }).setNegativeButton(android.R.string.no, null).show();	
				}
			});
			
        	String img = (String)currentQuestion.get("avatar");
			
			questionTitle.setText(title);
			questionContent.setText(content);
			questionMeta.setText(metas);
			questionVotes.setText(votes);
			
        	if(votes.equals("+1") || votes.equals("-1"))
        		questionVotesLabel.setText(activity.getString(R.string.one_vote));
			
        	UrlImageViewHelper.setUrlDrawable(questionAvatar, img);

        	answerContainer.removeAllViews();
        	commentContainer.removeAllViews();

        	if(currentQuestion.get("answers") instanceof Object[])
        		addAnswers((Object[])currentQuestion.get("answers"), questionId);

        	if(currentQuestion.get("comments") instanceof Object[]) {
				addComments((Object[]) currentQuestion.get("comments"), commentContainer);
        	}
        	
			questionPad.setVisibility(View.VISIBLE);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		isQuestion = true;
	}
	
	private void addAnswers(Object[] answers, final int questionId) {
		LayoutInflater inflater = getLayoutInflater();
		for(Object obj : answers) {
			if(obj instanceof HashMap) {
				HashMap<?,?> answer = (HashMap<?, ?>) obj;
				
				// Inflate the views from XML
				LinearLayout answerView = (LinearLayout) inflater.inflate(R.layout.answer, null);

				TextView metaView = (TextView) answerView.findViewById(R.id.meta);
				TextView contentView = (TextView) answerView.findViewById(R.id.content);
				TextView votesView = (TextView) answerView.findViewById(R.id.netvotes);
				TextView votesLabel = (TextView) answerView.findViewById(R.id.vote_label);
				Button voteUp = (Button) answerView.findViewById(R.id.vote_up_button);
				Button voteDown = (Button) answerView.findViewById(R.id.vote_down_button);

				Button commentButton = (Button) answerView.findViewById(R.id.comment);

				ImageView voteSpacer = (ImageView) answerView.findViewById(R.id.button_spacer);
				ImageView avatarView = (ImageView) answerView.findViewById(R.id.avatar);
				try {
					final HashMap<?,?> rawMap = (HashMap<?, ?>) answer.get("raw");
					
					final int answerId = Integer.parseInt((String) rawMap.get("postid")); 
					
					Integer vint = (Integer) answer.get("netvotes_raw");
		        	
					String votes = Integer.toString(vint);
					if(vint > 0)
		        		votes = "+"+votes;

		        	voteDown.setVisibility(View.VISIBLE);
		    		voteUp.setVisibility(View.VISIBLE);
		    		voteSpacer.setVisibility(View.GONE);
		        	
		        	Boolean voted = true;
					String uservote = (String) rawMap.get("uservote");
		        	if(uservote.equals("1"))
		        		voteDown.setVisibility(View.GONE);
		        	else if(uservote.equals("-1"))
		        		voteUp.setVisibility(View.GONE);
		        	else if(currentQuestion.get("vote_state").equals("disabled")) {
		        		voteDown.setVisibility(View.GONE);
		        		voteUp.setVisibility(View.GONE);
		        	}
		        	else {
		        		voteSpacer.setVisibility(View.VISIBLE);
		        		voted = false;
		        	}
		        	
		        	final int upvote = voted?0:1;
		        	final int downvote = voted?0:-1;
		        	
					Spanned content = Q2AStrings.getEntryContent((String) answer.get("content"));

					String meta = Q2AStrings.getMetaString(activity, answer);
					Spanned metas = Html.fromHtml(meta);

					voteUp.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("vote", upvote);
							info.put("type", "A");
							
							data.put("action_data", info);
							data.put("action","vote");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestions(data,currentScope);
							
						}
						
					});

					voteDown.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("vote", downvote);
							info.put("type", "A");
							
							data.put("action_data", info);
							data.put("action","vote");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestions(data,currentScope);
							
						}
						
					});
					
					commentButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							
							final EditText input = new EditText(activity);
							input.setHeight(200);
							input.setGravity(Gravity.TOP);
							new AlertDialog.Builder(activity)
						    .setTitle(R.string.post_comment)
						    .setView(input)
						    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int whichButton) {
									HashMap<String,Object> data = new HashMap<String,Object>();
									HashMap<String,Object> info = new HashMap<String,Object>();
									info.put("content", input.getText().toString());
									info.put("type", "C");
									info.put("parentid", answerId);
									
									data.put("action_data", info);
									data.put("action","post");
									data.put("action_id", questionId);
									getQuestions(data,currentScope);
						        }
						    }).setNegativeButton(android.R.string.no, null).show();	
						}
					});
					
		        	String img = (String)answer.get("avatar");
					
					contentView.setText(content);
					metaView.setText(metas);
					votesView.setText(votes);
					
		        	if(votes.equals("+1") || votes.equals("-1"))
		        		votesLabel.setText(getString(R.string.one_vote));
					
		        	UrlImageViewHelper.setUrlDrawable(avatarView, img);

		    		// get comments
		        	if(answer.get("comments") instanceof Object[]) {
						LinearLayout commentView = (LinearLayout) answerView.findViewById(R.id.comments);
						addComments((Object[]) answer.get("comments"), commentView);
		        	}
		        	answerContainer.addView(answerView);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addComments(Object[] comments, LinearLayout view) {
		
		LayoutInflater inflater = getLayoutInflater();
		for(Object obj : comments) {
			if(obj instanceof HashMap) {
				HashMap<?,?> comment = (HashMap<?, ?>) obj;
				LinearLayout commentView = (LinearLayout) inflater.inflate(R.layout.comment, null);
				TextView metaView = (TextView) commentView.findViewById(R.id.meta);
				TextView contentView = (TextView) commentView.findViewById(R.id.content);
				
				String meta = Q2AStrings.getMetaString(this, comment);
				Spanned metas = Html.fromHtml(meta);
				
				Spanned content = Q2AStrings.getEntryContent((String) comment.get("content"));

				
	        	metaView.setText(metas);
	        	contentView.setText(content);
	        	view.addView(commentView);
			}
		}
	}
	
}
