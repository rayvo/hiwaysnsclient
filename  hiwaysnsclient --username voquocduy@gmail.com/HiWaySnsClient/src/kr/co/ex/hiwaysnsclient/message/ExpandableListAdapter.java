package kr.co.ex.hiwaysnsclient.message;

import java.util.ArrayList;

import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.main.R;
import android.content.Context;
import android.graphics.Color;
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

	private ArrayList<ArrayList<TrOASISMessage>> children;

	public ExpandableListAdapter(Context context, ArrayList<String> groups,
			ArrayList<ArrayList<TrOASISMessage>> children) {
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
	public void addItem(TrOASISMessage message) {
		if (!groups.contains(message.getTitle())) {
			groups.add(message.getTitle());
		}
		int index = groups.indexOf(message.getTitle());
		if (children.size() < index + 1) {
			children.add(new ArrayList<TrOASISMessage>());
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
		TrOASISMessage message = (TrOASISMessage) getChild(groupPosition,
				childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.child_layout, null);
		}

		// TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
		LinearLayout dataLayout = (LinearLayout) convertView
				.findViewById(R.id.dataLayout);
		dataLayout.removeAllViews();

		if (message != null) {

			TextView contentTextView = new TextView(context);
			contentTextView.setText(message.getContent());
			contentTextView.setTextColor(Color.BLACK);
			contentTextView.setPadding(20, 0, 0, 0);
			dataLayout.addView(contentTextView);
		}
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
}
