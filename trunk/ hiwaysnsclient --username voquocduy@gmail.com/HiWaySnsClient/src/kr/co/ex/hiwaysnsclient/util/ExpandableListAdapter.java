package kr.co.ex.hiwaysnsclient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.main.R;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ExpandableListAdapter extends BaseExpandableListAdapter {

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private Context context;

	private ArrayList<String> groups;
	private ArrayList<String> dates;

	private ArrayList<ArrayList<TrOASISMessage>> children;

	public ExpandableListAdapter(Context context, ArrayList<String> groups, ArrayList<String> dates,
			ArrayList<ArrayList<TrOASISMessage>> children) {
		this.context = context;
		this.groups = groups;
		this.dates = dates;
		this.children = children;
	}

	/**
	 * A general add method, that allows you to add a Vehicle to this list
	 * 
	 * Depending on if the category opf the vehicle is present or not, the
	 * corresponding item will either be added to an existing group if it
	 * exists, else the group will be created and then the item will be added
	 * 
	 * @param vehicle
	 */
	public void addItem(TrOASISMessage message) {
		//if (!groups.contains(message.getTitle())) {
			groups.add(message.getTitle());
		//}
		int index = groups.indexOf(message.getTitle());
		if (children.size() < index + 1) {
			children.add(new ArrayList<TrOASISMessage>());
			
		}
		children.get(index).add(message);
		dates.add("[" + message.getCreatedDate().substring(0,10) + "]");
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return children.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// Return a child view. You can load your custom layout here.
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		TrOASISMessage message = (TrOASISMessage) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.child_layout, null);
		}

		// TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
		LinearLayout dataLayout = (LinearLayout) convertView
				.findViewById(R.id.dataLayout);
		dataLayout.removeAllViews();

		LinearLayout subLayout = new LinearLayout(context);
		
		TextView contentTV = new TextView(context);
		contentTV.setText(message.getContent());
		contentTV.setTextSize(13);
		contentTV.setTextColor(Color.BLACK);
		
		subLayout.addView(contentTV);
		
		dataLayout.addView(subLayout);
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// Return a group view. You can load your custom layout here.
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String group = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group_layout, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(group);
		
		
		TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
		tvDate.setText(dates.get(groupPosition));
		
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

	public boolean audioPlayer(boolean playFlag, String fileName) {

		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		String audioFilePath = Environment.getExternalStorageDirectory()
				.getPath() + "/cedic/sound/" + fileName + ".wav";
		;

		try {
			File file = new File(audioFilePath);
			FileInputStream fis = new FileInputStream(file);
			mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepare();
			if (playFlag) {
				mediaPlayer.start();
			}
			return true;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

}
