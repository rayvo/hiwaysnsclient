package kr.co.ex.hiwaysnsclient.message;

import java.util.ArrayList;
import java.util.List;

import kr.co.ex.hiwaysnsclient.db.TrOASISDatabase;
import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.main.R;
import kr.co.ex.hiwaysnsclient.util.Constant;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ShowMessageActivity extends Activity implements Runnable {

	private ExpandableListAdapter adapter;
	private TrOASISDatabase db;
	List<TrOASISMessage> data;

	protected int alignedType;

	ExpandableListView listView;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.message_box);

		// Retrive the ExpandableListView from the layout
		listView = (ExpandableListView) findViewById(R.id.listView);
		listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1,
					int arg2, int arg3, long arg4) {
				/*
				 * Toast.makeText(getBaseContext(), "Child clicked",
				 * Toast.LENGTH_LONG).show();
				 */
				return false;
			}
		});

		listView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1,
					int arg2, long arg3) {
				/*
				 * Toast.makeText(getBaseContext(), "Group clicked",
				 * Toast.LENGTH_LONG).show();
				 */
				TextView tv = (TextView) arg1.findViewById(R.id.tvGroup);
				return false;
			}
		});

		Intent intent = this.getIntent();
		String messageId = "";
		if (intent.getExtras() == null) {
			alignedType = Constant.ALIGNED_BY_TIME;
		} else {
			if (intent.getExtras().get("ALIGNED_TYPE") != null) {
				alignedType = ((Integer) intent.getExtras().get("ALIGNED_TYPE"))
						.intValue();
			} else {
				alignedType = Constant.ALIGNED_BY_TIME;	
			}
			
			if (intent.getExtras().get(Constant.STR_MSG_ID) != null) {
				messageId = ((Integer) intent.getExtras().get(Constant.STR_MSG_ID)).toString();
			}
		}

		pd = ProgressDialog.show(this, "In process",
				"Loading word(s), please wait...", true, false);

		Thread thread = new Thread(this);
		thread.start();
	}

	protected ProgressDialog pd;

	@Override
	public void run() {
		loadData();
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			displayData();
		}
	};

	private void loadData() {
		db = new TrOASISDatabase(this);
		data = db.getAllMessages(alignedType);
	}

	private void displayData() {
		if (data == null) {
			// setContentView(R.layout.empty_list);
			Toast.makeText(getBaseContext(), "List is empty",
					Toast.LENGTH_SHORT).show();
		} else {
			// Initialize the adapter with blank groups and children
			// We will be adding children on a thread, and then update the
			// ListView
			adapter = new ExpandableListAdapter(this, new ArrayList<String>(),
					new ArrayList<ArrayList<TrOASISMessage>>());

			for (int i = 0; i < data.size(); i++) {
				adapter.addItem(data.get(i));
				adapter.notifyDataSetChanged();
			}

			// Set this blank adapter to the list view
			listView.setAdapter(adapter);
		}
	}
}
