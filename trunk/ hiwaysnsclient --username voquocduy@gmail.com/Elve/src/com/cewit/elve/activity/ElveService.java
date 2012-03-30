package com.cewit.elve.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.cewit.elve.common.Constants;
import com.cewit.elve.lib.ElveLocation;
import com.cewit.elve.lib.ElveUtil;
import com.google.android.maps.GeoPoint;

public class ElveService extends Service {

	ConnectThread connectThread;
	ConnectedThread connectedThread;

	ClientCommunicationThread clientThread;

	String readMessage;
	String writeMessage;
	String totalContent = "";

	TextView edit01;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final byte[] BT_PCSC_ACK_CONNECTION = new byte[] { 0x13,
			0x37, 0x30, (byte) 0xF8 };

	private static final String NAME1 = "ElveService";

	private static final String TAG = "ElveService";
	private static final int INTERVAL_SERVER = 500; // 5 seconds

	public static final String GET_BLUETOOTH_DATA_ACTION = "get.bluetooth.data";
	private final Handler handler = new Handler();
	Intent intent;
	int counter = 0;

	private byte[] buffer = null;
	
	protected	ElveLocation		mElveLocation	= null;

	@Override
	public void onCreate() {
		super.onCreate();
		intent = new Intent(GET_BLUETOOTH_DATA_ACTION);

		BluetoothSocket mmSocket;
		BluetoothDevice mmDevice;

		BluetoothSocket tmp = null;
		mmDevice = AppData.DEVICE_REMOTE;

		try {

			tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);

		} catch (IOException e) {

		}
		mmSocket = tmp;

		/*
		 * 클라이언트 단은 서버단에 데이터를 보내기 위해 write할것이므로 write하기전에 서버단과 커낵트를 해야한다. 서버단이
		 * accept로 루프 돌고 있으므로 우선 서버 가 작동되고 있어야 한다 서버가 작동되고 있어야 그후에 켜진 클라이언트단의 한번
		 * 실행되는 커낵트를 수용할 수 있으므로
		 */
		AppData.mBTAdapter.cancelDiscovery();

		try {

			mmSocket.connect();

		} catch (IOException connectException) {
			try {
				mmSocket.close();

			} catch (IOException CloseException) {
			}
			return;
		}
		manageConnectedSocket(mmSocket);
		
		mElveLocation	= new ElveLocation();
		

	}

	@Override
	public void onStart(Intent intent, int startId) {
		handler.removeCallbacks(sendUpdatesToUI);
		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

	}

	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
			DisplayLoggingInfo();
			handler.postDelayed(this, INTERVAL_SERVER); // 5 seconds
		}
	};

	private void DisplayLoggingInfo() {
		Log.d(TAG, "entered DisplayLoggingInfo");
		if (buffer != null && processBuffer1()) {
			GeoPoint	ptGeo	= mElveLocation.getCurrentGeoPoint();
			intent.putExtra(Constants.INTENT_LNG, lngValue);
			intent.putExtra(Constants.INTENT_LAT, latValue);
			intent.putExtra(Constants.INTENT_RATE, rateLevel);
			intent.putExtra(Constants.INTENT_APS, apsLevel);
			intent.putExtra(Constants.INTENT_BPS, bpsLevel);
			intent.putExtra(Constants.INTENT_SOC, socLevel);			
			sendBroadcast(intent);	
		}
		
	}

	private int lngValue = 0;
	private int latValue = 0;
	private int rateLevel = 0;
	private int apsLevel = 0;
	private int bpsLevel = 0;
	private int socLevel = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(sendUpdatesToUI);
		 super.onDestroy();
	}

	/**
	 * 데이터 송수신을 위한 클라이언트소켓 생성 쓰레드(클라이언트 단)
	 */
	class ConnectThread extends Thread {

		BluetoothSocket mmSocket;
		BluetoothDevice mmDevice;

		ConnectThread(BluetoothDevice device) {

			BluetoothSocket tmp = null;
			mmDevice = device;

			try {

				tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID); // 리모트
																			// 디바이스의
																			// uuid를
																			// 맞게
																			// 설정해야
																			// 작동한다
				// Toast.makeText(DisplayData.this,"connect 생성 성공",
				// Toast.LENGTH_LONG).show();

			} catch (IOException e) {

				// Toast.makeText(DisplayData.this,"connect 생성 오류",
				// Toast.LENGTH_LONG).show();
			}
			mmSocket = tmp;
		}

		/**
		 * 소켓 연결 시도
		 */
		public void run() {

			AppData.mBTAdapter.cancelDiscovery();

			try {

				mmSocket.connect();
				// Toast.makeText(DisplayData.this,"connected go",
				// Toast.LENGTH_SHORT).show();
			} catch (IOException connectException) {

				try {

					mmSocket.close();
				} catch (IOException CloseException) {
				}
				return;
			}

			manageConnectedSocket(mmSocket); // 실제 데이터 송수신 작업을 하는 쓰레드 호출
		}

		public void cancel() {

			try {

				mmSocket.close();
			} catch (IOException e) {
			}
		}

	}

	/**
	 * 클라이언트와의 통신을 위한 스레드를 돌리기 위한 메서드
	 */
	protected void manageConnectedSocket(BluetoothSocket socket) {

		if (clientThread != null)
			return;

		clientThread = new ClientCommunicationThread(socket);
		clientThread.start();
	}

	/**
	 * 실제적인 클라이언트 커뮤니케이션 스레드
	 */
	private class ClientCommunicationThread extends Thread {

		private BluetoothSocket socket;
		private InputStream is;
		private OutputStream os;
		private boolean cancel, active;

		public ClientCommunicationThread(BluetoothSocket socket) {

			this.socket = socket;
		}

		public void run() {

			String address = socket.getRemoteDevice().getAddress();

			try {
				// log("new client " + socket.getRemoteDevice().getAddress() +
				// ".");
				// Toast.makeText(DisplayData.this,"DD",
				// Toast.LENGTH_SHORT).show();
				BluetoothSocket socket = this.socket;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				this.is = is;
				this.os = os;
				active = true;

				os.write(BT_PCSC_ACK_CONNECTION);

				connectedThread = new ConnectedThread(socket);
				connectedThread.setDaemon(true);
				connectedThread.start();

			} catch (IOException e) {

				// log("Lost connection to client "+ address + ".");
				try {
					this.socket.close();
				} catch (IOException e1) {
				}
			} catch (Exception e) {
				// log("error: " + e);
				try {
					this.socket.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * 데이터 송수신 작업 쓰레드
	 */
	class ConnectedThread extends Thread {

		BluetoothSocket mmSocket;
		InputStream mmInStream;
		OutputStream mmOutStream;

		ConnectedThread(BluetoothSocket socket) {

			/** 송수신을 위한 스트림 생성 */
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;

		}

		public void run() {

			// 데이터 수신을 담기위한 버퍼 생성
			byte[] buffer = new byte[1024];
			int bytes;
			while (true) {

				try {

					bytes = mmInStream.read(buffer);
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
					
				} catch (IOException e) {
					break;
				} 
			}
		}

		public void cancel() {

			try {

				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case MESSAGE_WRITE:

				byte[] writeBuf = (byte[]) msg.obj;
				writeMessage = new String(writeBuf);
				// writeMessage = "전달";
				// mConversationArrayAdapter.add("Me: " + writeMessage);
				// Toast.makeText(DisplayData.this,writeMessage,
				// Toast.LENGTH_SHORT).show();
				break;

			case MESSAGE_READ:

				buffer = (byte[]) msg.obj;
				readMessage = new String(buffer, 0, msg.arg1);
				/*try {
					
					processBuffer();
					
					 * String encode =
					 * URLEncoder.encode(readMessage,"utf-8").replaceAll("%",
					 * " "); totalContent += encode;
					 * edit01.setText(totalContent);
					 
				} catch (Exception e) {
				}*/
				break;
			}
		}
	};

	private boolean processBuffer1() {
		rateLevel = ElveUtil.proLevelRandom(7);
		apsLevel = ElveUtil.proLevelRandom(8);
		bpsLevel = ElveUtil.proLevelRandom(8);
		socLevel = ElveUtil.proLevelRandom(10);		
		
		return true;
	}
	private boolean processBuffer() {
		byte[] currentBuffer;
		if (buffer[0] != 112 || buffer[1] != 114 || buffer[2] != 113) {
			return false;
		} else {
			currentBuffer = this.buffer;
		}
		byte[] lngBytes = ElveUtil.getBytes(currentBuffer, 4, 4);
		lngValue = ElveUtil.byteArrayToInt(lngBytes);

		byte[] latBytes = ElveUtil.getBytes(currentBuffer, 8, 4);
		latValue = ElveUtil.byteArrayToInt(latBytes);

		byte[] rpmBytes = ElveUtil.getBytes(currentBuffer, 12, 2);
		int rpm = ElveUtil.byteArrayToInt(rpmBytes);

		byte[] speedBytes = ElveUtil.getBytes(currentBuffer, 14, 1);
		int speed = ElveUtil.byteArrayToInt(speedBytes);

		byte[] rateBytes = ElveUtil.getBytes(currentBuffer, 15, 1);
		int rate = ElveUtil.byteArrayToInt(rateBytes);
		rateLevel = ElveUtil.getLevel(Constants.MAX_RATE,
				Constants.NUMBER_LEVEL_RATE, rate);

		byte[] tempBytes = ElveUtil.getBytes(currentBuffer, 16, 1);
		int temp = ElveUtil.byteArrayToInt(tempBytes);

		byte[] socBytes = ElveUtil.getBytes(currentBuffer, 17, 1);
		int soc = ElveUtil.byteArrayToInt(socBytes);
		socLevel = ElveUtil.getLevel(Constants.MAX_SOC,
				Constants.NUMBER_LEVEL_SOC, soc);

		byte[] apsBytes = ElveUtil.getBytes(currentBuffer, 18, 1);
		int aps = ElveUtil.byteArrayToInt(apsBytes);
		apsLevel = ElveUtil.getLevel(Constants.MAX_APS,
				Constants.NUMBER_LEVEL_APS, aps);

		byte[] bpsBytes = ElveUtil.getBytes(currentBuffer, 19, 2);
		int bps = ElveUtil.byteArrayToInt(bpsBytes);
		bpsLevel = ElveUtil.getLevel(Constants.MAX_BPS,
				Constants.NUMBER_LEVEL_BPS, bps);

		byte[] aveBytes = ElveUtil.getBytes(currentBuffer, 21, 2);
		int ave = ElveUtil.byteArrayToInt(aveBytes);
		
		return true;

	}

	public synchronized void stop() {

		if (connectedThread != null) {

			connectedThread.cancel();
			connectedThread = null;
		}
		if (clientThread != null) {

			clientThread = null;
		}
	}

}
