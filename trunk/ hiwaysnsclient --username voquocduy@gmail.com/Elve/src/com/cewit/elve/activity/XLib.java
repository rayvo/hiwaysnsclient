package com.cewit.elve.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.view.WindowManager;
/**
 * 공통 메서드
 * @author Administrator
 *
 */
public class XLib {
	/**
	 * 어플을 강제 종료 한다.
	 * 
	 * @param context
	 */
	static public void requestKillProcess(final Context context) {

		// #1. first check api level.
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < 8) {
			// #2. if we can use restartPackage method, just use it.
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(context.getPackageName());
		} else {
			// #3. else, we should use killBackgroundProcesses method.
			new Thread(new Runnable() {
				@Override
				public void run() {
					ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
					String name = context.getApplicationInfo().processName;
					// RunningServiceInfo si;

					// pooling the current application process importance
					// information.
					while (true) {
						List<RunningAppProcessInfo> list = am
								.getRunningAppProcesses();
						for (RunningAppProcessInfo i : list) {
							if (i.processName.equals(name) == true) {
								// #4. kill the process,
								// only if current application importance is
								// less than IMPORTANCE_BACKGROUND
								if (i.importance >= RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
									am.restartPackage(context.getPackageName()); // simple
																					// wrapper
																					// of
																					// killBackgrounProcess
								else
									Thread.yield();
								break;
							}
						}
					}
				}
			}, "Process Killer").start();
		}
	}

	/**
	 * network 연결 상태 확인
	 * 
	 * @return
	 */
	public static boolean isOnline(Context _context) { // network 연결 상태 확인

		/*
		 * ConnectivityManager connectivityManager = (ConnectivityManager)
		 * getApplicationContext
		 * ().getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo
		 * networkInfo = connectivityManager.getActiveNetworkInfo(); boolean
		 * connected = networkInfo != null && networkInfo.isAvailable() &&
		 * networkInfo.isConnected(); return connected;
		 */
		boolean connected = false;
		ConnectivityManager conMan = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(0).getState(); // mobile
		
		State wifi = conMan.getNetworkInfo(1).getState(); // wifi
		if (mobile == NetworkInfo.State.CONNECTED
				|| mobile == NetworkInfo.State.CONNECTING) {
			connected = true;
		} else if (wifi == NetworkInfo.State.CONNECTED
				|| wifi == NetworkInfo.State.CONNECTING) {
			connected = true;
		}

		
		return connected;
	}

	/**
	 * 액티비티를 시작한다.
	 * 
	 * @param _context
	 * @param _className
	 */
	public static void startActivity(Context _context, String _className) {
		String packageName = _context.getPackageName();
		Intent intent = new Intent();

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		intent.setComponent(new ComponentName(packageName, packageName + "."
				+ _className));
		_context.startActivity(intent);
	}

	/**
	 * 화면 흐리게 비활성
	 * 
	 * @param _context
	 */
	public static void setDimBehind(Activity _activity) {
		_activity.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	/**
	 * 메세지창을 출력한다.
	 * 
	 * @param _context
	 * @param _title
	 * @param _msg
	 */
	static public void setAlertDialog(Context _context, String _title,
			String _msg) {
		new AlertDialog.Builder(_context).setTitle(_title).setMessage(_msg)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}
	
	/**
	 * 메세지창을 출력 후 페이지 이동 한다.
	 * 
	 * @param _context
	 * @param _title
	 * @param _msg
	 */
	static public void setAlertDialog2(final Context _context, String _title,
			String _msg,DialogInterface.OnClickListener _onClickListener) {
		new AlertDialog.Builder(_context).setTitle(_title).setMessage(_msg)
				.setPositiveButton("확인",_onClickListener).show();
	}
	
//	XLib.setAlertDialog2(this, "확인", "신고내역이 삭제 되었습니다.",
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					onClick_back(null);
//				}
//			});

	/**
	 * 사용자 정보 읽기
	 * 
	 * @param _pref
	 * @param _key
	 * @param _defaultData
	 * @return
	 */
	static public String getSharedData(SharedPreferences _pref, String _key,
			String _defaultData) {
		return _pref.getString(_key, _defaultData);
	}

	/**
	 * 사용자 정보 저장
	 * 
	 * @param _pref
	 * @param _key
	 * @param _data
	 */
	static public void setSharedData(SharedPreferences _pref, String _key,
			String _data) {
		SharedPreferences.Editor editor = _pref.edit();
		editor.putString(_key, _data);
		editor.commit();

	}
}