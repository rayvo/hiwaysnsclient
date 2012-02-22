package kr.co.ex.hiwaysnsclient.facebook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.ex.hiwaysnsclient.facebook.SessionEvents.AuthListener;
import kr.co.ex.hiwaysnsclient.facebook.SessionEvents.LogoutListener;
import kr.co.ex.hiwaysnsclient.sns.HiWayImageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.R.id;
import com.facebook.android.Util;

public class TroasisFBActivity extends Activity {

	public static final String APP_ID = "294512100581933";

	String FILENAME = "TrOASIS_data";

	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

	private LoginButton mLoginButton;
	private TextView mText;
	private ImageButton mPostButton;
	private ImageButton mUploadButton;
	private ImageButton mTakePictureButton;
	private ImageButton mBackButton;
	private ImageButton mRotateLeftButton;
	private ImageButton mRotateRightButton;

	private LinearLayout mMainLayout;
	
	private String mDrivingStatus = "";

	private String mScreenshotFile = "";
	private String mTakenPicture = "";

	private Bitmap currentBitmap = null;
	private Bitmap originalBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (APP_ID == null) {
			Util.showAlert(this, "Warning", "Facebook Applicaton ID must be "
					+ "specified before running this example: see Example.java");
		}

		setContentView(R.layout.facebook_main);

		mHandler = new Handler();

		Intent intent = this.getIntent();
		if (intent.getExtras() != null) {
			mScreenshotFile = intent.getExtras().getString("SCREENSHOT_NAME");
			mDrivingStatus = "MY DRIVING STATUS  \n " + "ㅁ " + "주행시간 : "
					+ intent.getExtras().getString("TRAVEL_TIME") + "  -  "
					+ "주행거리 : "
					+ intent.getExtras().getString("TRAVEL_DISTANCE") + "\n"
					+ "ㅁ " + "평균속도 : "
					+ intent.getExtras().getString("AVERAGE_SPEED") + "  -  "
					+ "최고속도 : " + intent.getExtras().getString("HIGHEST_SPEED")
					+ "\n";

		} else {
			return;
		}
		// Display the screenshot onto the confirm page
		ImageView ivScreenshot = (ImageView) findViewById(id.imgScreenshot);
		currentBitmap = BitmapFactory.decodeFile(mScreenshotFile);
		originalBitmap = currentBitmap;
		ivScreenshot.setImageBitmap(currentBitmap);

		// For Facebook
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

		mLoginButton = (LoginButton) findViewById(R.id.login);
		mLoginButton = (LoginButton) findViewById(R.id.login);

		mText = (TextView) findViewById(R.id.txt);
		mUserPic = (ImageView) findViewById(R.id.user_pic);

		mPostButton = (ImageButton) findViewById(R.id.postButton);
		mUploadButton = (ImageButton) findViewById(R.id.uploadButton);
		mBackButton = (ImageButton) findViewById(R.id.id_btn_back);
		mRotateLeftButton = (ImageButton) findViewById(R.id.id_btn_rotate_left);
		mRotateRightButton = (ImageButton) findViewById(R.id.id_btn_rotate_right);
		mTakePictureButton = (ImageButton) findViewById(R.id.takePictureButton);

		mMainLayout = (LinearLayout) findViewById(R.id.linMainLayout);
		
		// mPostText.setText("\n" + caption);
		SessionStore.restore(mFacebook, this);
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());

		mLoginButton.init(this, mFacebook, mAsyncRunner);

		mUploadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				postPhoto();
				TroasisFBActivity.this.finish();
			}
		});
		mUploadButton.setVisibility(mFacebook.isSessionValid() ? View.VISIBLE
				: View.INVISIBLE);

		mPostButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mFacebook.dialog(TroasisFBActivity.this, "feed",
						new SampleDialogListener());
			}
		});
		mPostButton.setVisibility(mFacebook.isSessionValid() ? View.VISIBLE
				: View.INVISIBLE);

		mTakePictureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				takePhoto();
			}
		});

		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TroasisFBActivity.this.finish();
			}
		});
		
		mRotateLeftButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rotateLeft(true);

			}
		});
		mRotateRightButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rotateLeft(false);
			}
		});
		requestUserData();
	}

	private String buildComment() {
		CheckBox chkDrivingStatus = (CheckBox) findViewById(id.chkDrivingStatus);
		EditText mPostText = (EditText) findViewById(R.id.txtPost);
		String comment = mPostText.getText().toString();

		if (chkDrivingStatus.isChecked()) {
			comment = comment + "\n" + mDrivingStatus;
		}
		return comment;
	}

	private int rotateCount = 0;

	private void rotateLeft(boolean flag) {
		ImageView ivScreenshot = (ImageView) findViewById(id.imgScreenshot);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap,
				originalBitmap.getWidth(), originalBitmap.getHeight(), true);

		// // create a matrix object
		Matrix matrix = new Matrix();
		matrix.reset();
		if (flag) {
			rotateCount = rotateCount + 1;
		} else {
			rotateCount = rotateCount - 1;
		}

		int mod = rotateCount % 4;
		int rotateDegree = 0;
		switch (mod) {
		case 3:
			rotateDegree = -270;
			break;
		case 2:
			rotateDegree = -180;
			break;
		case 1:
			rotateDegree = -90;
			break;
		case -1:
			rotateDegree = 90;
			break;
		case -2:
			rotateDegree = 180;
			break;
		case -3:
			rotateDegree = 270;
			break;
		default:
			rotateDegree = 0;
		}

		if (rotateDegree != 0) {
			matrix.postRotate(rotateDegree, originalBitmap.getHeight() / 2,
					originalBitmap.getWidth() / 2);
			currentBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
					scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
					false);
		} else {
			currentBitmap = originalBitmap;
		}
		// display the rotated bitmap
		ivScreenshot.setImageBitmap(currentBitmap);

	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(String fileName) {
		try {
			File f = new File(fileName);
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 800;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	protected void takePhoto() {
		String parentPath = Environment.getExternalStorageDirectory()
				+ "/troasis";

		DateFormat iso8601Format = new SimpleDateFormat("yyyyMMdd");
		Date currentDate = new Date(System.currentTimeMillis());
		String folderName = iso8601Format.format(currentDate);
		String folderPath = parentPath + "/" + folderName;

		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		DateFormat fileFormat = new SimpleDateFormat("HHmmss");
		mTakenPicture = fileFormat.format(currentDate);
		mTakenPicture = folderPath + "/TAKEN_" + mTakenPicture + ".png";
		File file = new File(mTakenPicture);

		Uri outputFileUri = Uri.fromFile(file);

		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, HiWayImageActivity.CAMERA_PIC_REQUEST);
	}

	public boolean mMediaRecorded = false;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("MakeMachine", "resultCode: " + resultCode);
		if (requestCode != HiWayImageActivity.CAMERA_PIC_REQUEST) {
			mFacebook.authorizeCallback(requestCode, resultCode, data);
		} else {
			switch (resultCode) {
			case 0:
				Log.i("MakeMachine", "User cancelled");
				mMediaRecorded = false;
				break;

			case -1:
				onPhotoTaken();
				mMediaRecorded = true;
				break;
			}
		}
	}

	private void onPhotoTaken() {
		Log.i("MakeMachine", "onPhotoTaken");
		ImageView ivScreenshot = (ImageView) findViewById(id.imgScreenshot);
		currentBitmap = this.decodeFile(mTakenPicture);
		originalBitmap = currentBitmap;
		ivScreenshot.setImageBitmap(currentBitmap);

	}

	private void postPhoto() {
		Bundle params = new Bundle();
		params.putString("method", "photos.upload");
		params.putString("caption", buildComment());
		params.putString("name", getString(R.string.app_action));

		byte[] data = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			data = baos.toByteArray();
			params.putByteArray("picture", data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		mAsyncRunner.request(null, params, "POST", new SampleUploadListener(),
				null);
	}

	public class SampleAuthListener implements AuthListener {

		public void onAuthSucceed() {

			mText.setText("Fetching user name, profile pic...");
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			mAsyncRunner.request("me", params, new UserRequestListener());

			mUploadButton.setVisibility(View.VISIBLE);
			mPostButton.setVisibility(View.VISIBLE);
		}

		public void onAuthFail(String error) {
			mText.setText("Login Failed: " + error);
		}
	}

	public class SampleLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			mText.setText("Logging out...");
		}

		public void onLogoutFinish() {
			mText.setText("You have logged out! ");
			mUserPic.setImageBitmap(null);
			mUploadButton.setVisibility(View.INVISIBLE);
			mPostButton.setVisibility(View.INVISIBLE);
			mMainLayout.setVisibility(View.INVISIBLE);
		}
	}

	public class SampleRequestListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			try {
				// process the response here: executed in background thread
				Log.d("Facebook-MyGreatActivity",
						"Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String name = json.getString("name");

				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."
				TroasisFBActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						mText.setText("Hello there, " + name + "!");
					}
				});
			} catch (JSONException e) {
				Log.w("Facebook-MyGreatActivity", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-MyGreatActivity",
						"Facebook Error: " + e.getMessage());
			}
		}
	}

	public class SampleUploadListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			try {
				// process the response here: (executed in background thread)
				Log.d("Facebook-MyGreatActivity",
						"Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String src = json.getString("src");

				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."
				TroasisFBActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						mText.setText("Hello there, photo has been uploaded at \n"
								+ src);
					}
				});
			} catch (JSONException e) {
				Log.w("Facebook-MyGreatActivity", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-MyGreatActivity",
						"Facebook Error: " + e.getMessage());
			}
		}
	}

	public class WallPostRequestListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			Log.d("Facebook-MyGreatActivity", "Got response: " + response);
			String message = "<empty>";
			try {
				JSONObject json = Util.parseJson(response);
				message = json.getString("message");
			} catch (JSONException e) {
				Log.w("Facebook-MyGreatActivity", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-MyGreatActivity",
						"Facebook Error: " + e.getMessage());
			}
			final String text = "Your Wall Post: " + message;
			TroasisFBActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					mText.setText(text);
				}
			});
		}
	}

	public class WallPostDeleteListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			if (response.equals("true")) {
				Log.d("Facebook-MyGreatActivity",
						"Successfully deleted wall post");
				TroasisFBActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						mText.setText("Deleted Wall Post");
					}
				});
			} else {
				Log.d("Facebook-MyGreatActivity", "Could not delete wall post");
			}
		}
	}

	public class SampleDialogListener extends BaseDialogListener {

		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Log.d("Facebook-MyGreatActivity", "Dialog Success! post_id="
						+ postId);
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
				Log.d("Facebook-MyGreatActivity", "No wall post made");
			}
		}
	}

	/*
	 * Request user name, and picture to show on the main screen.
	 */
	private void requestUserData() {
		if (mFacebook != null && !mFacebook.isSessionValid()) {
			mText.setText("You are logged out! ");
			mUserPic.setImageBitmap(null);
			mMainLayout.setVisibility(View.INVISIBLE);
		} else {
			mMainLayout.setVisibility(View.VISIBLE);
			if (picURL != null & name != null) {
				mText.setText("Hi " + name + ", Welcome to Facebook!");
				mUserPic.setImageBitmap(Utility.getBitmap(picURL));
			} else {
				mText.setText("Fetching user name, profile pic...");
				Bundle params = new Bundle();
				params.putString("fields", "name, picture");
				mAsyncRunner.request("me", params, new UserRequestListener());
			}
		}
	}

	private Handler mHandler;
	private ImageView mUserPic;
	private String picURL = null;
	private String name = null;

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class UserRequestListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);

				picURL = jsonObject.getString("picture");
				name = jsonObject.getString("name");
				Utility.userUID = jsonObject.getString("id");

				mHandler.post(new Runnable() {
					public void run() {
						mText.setText("Hi " + name + ", Welcome to Facebook!");
						mUserPic.setImageBitmap(Utility.getBitmap(picURL));
						mMainLayout.setVisibility(View.VISIBLE);
					}
				});

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFacebook != null && !mFacebook.isSessionValid()) {
			mText.setText("You are logged out! ");
			mUserPic.setImageBitmap(null);
			mMainLayout.setVisibility(View.INVISIBLE);
		}
	}
}
