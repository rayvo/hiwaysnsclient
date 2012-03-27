package com.cewit.elve.common;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cewit.elve.activity.R;

public class RowAdapter extends ArrayAdapter<RowItem> {

	private ArrayList<RowItem> items;
	private Context context;

	public RowAdapter(Context context, int textViewResourceId,
			ArrayList<RowItem> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
		RowItem i = items.get(position);
		if (i != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			if (tt != null) {
				tt.setText(i.getTitle());
			}
			if (bt != null) {
				bt.setText(i.getContent());
			}
		}
		return v;
	}

}