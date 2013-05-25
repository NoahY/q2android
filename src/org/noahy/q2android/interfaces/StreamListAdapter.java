package org.noahy.q2android.interfaces;

import org.noahy.q2android.*;

import java.util.HashMap;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StreamListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "StreamListAdapter";
	public SparseIntArray expanded = new SparseIntArray();
	private HashMap<String,Boolean> admin;
	
	public StreamListAdapter(Activity activity, Object[] rss) {
		super(activity, 0, rss);
		
	}


	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView;
		
		Object item = getItem(position);
		
		if(item instanceof String) {
			rowView =  inflater.inflate(R.layout.more_item, null);
			if(item.equals("<less>"))
				((TextView)rowView.findViewById(R.id.more)).setText(activity.getString(R.string.prev));
				
			else if(!item.equals("<more>"))
				((TextView)rowView.findViewById(R.id.more)).setText((String)item);
			return rowView;
		}
		
		rowView =  inflater.inflate(R.layout.stream_item, null);
		HashMap<?,?> entryMap = (HashMap<?, ?>) item;
		
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView metaView = (TextView) rowView.findViewById(R.id.meta);
		
		TextView viewsView = (TextView) rowView.findViewById(R.id.views);
		TextView answersView = (TextView) rowView.findViewById(R.id.answers);
		TextView votesView = (TextView) rowView.findViewById(R.id.netvotes);

		TextView viewsLabel = (TextView) rowView.findViewById(R.id.view_label);
		TextView answersLabel = (TextView) rowView.findViewById(R.id.answer_label);
		TextView votesLabel = (TextView) rowView.findViewById(R.id.vote_label);
		
		ImageView avatarView = (ImageView) rowView.findViewById(R.id.avatar);
		LinearLayout answerLayout = (LinearLayout) rowView.findViewById(R.id.answer_container);
        try {
    		HashMap<?,?> rawMap = (HashMap<?, ?>) entryMap.get("raw");
        	String title = (String)rawMap.get("title");
        	String votes = (String)rawMap.get("netvotes");
        	if(!votes.startsWith("-") && !votes.equals("0"))
        		votes = "+"+votes;
        	String answers = (String)rawMap.get("acount");
        	String views = (String)rawMap.get("views");
        	String selected = (String)rawMap.get("selchildid");
        	
        	String img = (String)entryMap.get("avatar");
        	
        	String meta = Q2AStrings.getMetaString(activity, entryMap);

        	Spanned metas = Html.fromHtml(meta);
        	
        	// add image
        	
        	UrlImageViewHelper.setUrlDrawable(avatarView, img);
        	
        	// add text
        	
        	titleView.setText(title);
        	
        	metaView.setText(metas);
        	
        	viewsView.setText(views);
        	
        	answersView.setText(answers);
        	
        	votesView.setText(votes);

        	// adjust views
        	
        	if(selected.length() > 0)
        		answerLayout.setBackgroundResource(R.color.selanswerback);
        	else if (!answers.equals("0"))
        		answerLayout.setBackgroundResource(R.color.someanswerback);
        		
        	metaView.setMovementMethod(LinkMovementMethod.getInstance());

        	// adjust labels
        	if(views.equals("1"))
        		viewsLabel.setText(activity.getString(R.string.one_view));
        	
        	if(votes.equals("+1") || votes.equals("-1"))
        		votesLabel.setText(activity.getString(R.string.one_vote));

        	if(answers.equals("1"))
        		answersLabel.setText(activity.getString(R.string.one_answer));
        	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        parent.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		return rowView;

	}

	private String sanitizeText(String string) {
		if(string != null)
			string = string.replace("\\\"", "\"").replace("\\'", "'");
		return string;
	} 

}