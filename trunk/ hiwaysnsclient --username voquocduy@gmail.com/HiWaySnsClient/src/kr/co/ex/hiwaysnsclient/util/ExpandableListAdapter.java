package kr.co.ex.hiwaysnsclient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import kr.co.ex.hiwaysnsclient.db.Message;
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

	private ArrayList<ArrayList<Message>> children;

	public ExpandableListAdapter(Context context, ArrayList<String> groups,
			ArrayList<ArrayList<Message>> children) {
		this.context = context;
		this.groups = groups;
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
	public void addItem(Message message) {
		if (!groups.contains(message.getTitle())) {
			groups.add(message.getTitle());
		}
		int index = groups.indexOf(message.getTitle());
		if (children.size() < index + 1) {
			children.add(new ArrayList<Message>());
		}
		children.get(index).add(message);
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
		Message message = (Message) getChild(groupPosition, childPosition);
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
		/*
		if (message != null) {
			
			if (words != null) {
				for (int i = 0; i < words.size(); i++) {
					Word word = words.get(i);

					Type type = word.getType();
					if (type != null) { // in case of no type
						String curType = type.getType();
						if (!typeFlag.equals(curType)) {
							LinearLayout subLayout = new LinearLayout(context);

							curType = type.getType();
							TextView typeTextView = new TextView(context);
							typeTextView.setText(curType + "\t");
							typeTextView.setTextSize(20);
							typeTextView.setTextColor(Color.GREEN);
							subLayout.addView(typeTextView);

							Typeface myTypeface = Typeface.createFromAsset(
									context.getAssets(), "lsansuni.ttf");
							TextView pronounceTextView = new TextView(context);
							pronounceTextView.setTypeface(myTypeface);
							pronounceTextView.setText(word.getPronounce()
									.replace("''", "'"));
							pronounceTextView.setTextColor(Color.BLUE);
							pronounceTextView.setTextSize(20);
							if (proFlag) {
								subLayout.addView(pronounceTextView);

								
								boolean blnSoundExist = false;

								
									blnSoundExist = audioPlayer(false,
											message.getBaseWord());
								

								if (blnSoundExist) {
									ImageButton btnPronounce = new ImageButton(
											context);
									btnPronounce
											.setBackgroundResource(R.drawable.speaker);
									btnPronounce.setPadding(50, 20, 0, 0);
									btnPronounce.setTag(message.getBaseWord());
									btnPronounce
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													String word = (String) v
															.getTag();
													audioPlayer(true,
															word.trim());
												}
											});
									subLayout.addView(btnPronounce);
								}
								proFlag= false;
							}
							dataLayout.addView(subLayout);
						}
						typeFlag = curType;
					}

					List<Meaning> meanings = word.getMeanings();
					int count = i + 1;
					for (int j = 0; j < meanings.size(); j++) {
						Meaning meaning = meanings.get(j);
						TextView meaningView = new TextView(context);
						meaningView.setTextSize(20);
						meaningView.setTextColor(Color.CYAN);
						meaningView
								.setText(count + ") " + meaning.getMeaning());
						meaningView.setPadding(15, 0, 0, 0);
						dataLayout.addView(meaningView);

						List<Example> examples = meaning.getExamples();
						if (examples != null) {
							String tmp = "";
							for (int k = 0; k < examples.size(); k++) {
								Example example = examples.get(k);
								// Display example
								tmp = tmp + "-" + example.getExample() + "\n";
							}
							TextView examplesTextView = new TextView(context);
							examplesTextView.setText(tmp);
							examplesTextView.setTextColor(Color.LTGRAY);
							examplesTextView.setTypeface(null, Typeface.ITALIC);
							examplesTextView.setTextSize(20);
							examplesTextView.setPadding(20, 0, 0, 0);
							dataLayout.addView(examplesTextView);
						}
					}
				}
			}
		}*/
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
