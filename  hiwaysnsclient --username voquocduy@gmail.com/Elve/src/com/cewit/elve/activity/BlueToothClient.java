
/**
 *  현재 블루투스 프로젝트 서버와 블루투스 클라이언트 간의 소스가 아니라 
 *  블루투스 클라이언트 (커낵트 하는 곳이 클라이언트) 와 브로드스팅 하는 다른 기반 블루투스와의 통신을 하므로 클라이언트인 이 프로젝트가 리드 하고 있다.
 *  블루투스 프로젝트와 통신할때는 리드가 아니고 롸이트로만 바꿔주면 된다.
 *  mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
 */

package com.cewit.elve.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.cewit.elve.lib.ElveUtil;

public class BlueToothClient extends Activity {
		
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
	private static final UUID MY_UUID  = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final byte[] BT_PCSC_ACK_CONNECTION = new byte[] {
		 0x13, 0x37, 0x30, (byte) 0xF8 };

	private static final String NAME = "BluetoothChat";
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main2);
	   	    
	    edit01=(TextView)findViewById(R.id.content);
	    	    
/*클라이언트 단일 때 의 소켓 생성 쓰레드 */
	//    connectThread = new ConnectThread(AppData.DEVICE_REMOTE);
	//    connectThread.setDaemon(true);
	//    connectThread.start();  
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
		   * 클라이언트 단은 서버단에 데이터를 보내기 위해 write할것이므로 write하기전에 서버단과 커낵트를 해야한다. 
		   * 서버단이 accept로 루프 돌고 있으므로 우선 서버 가 작동되고 있어야 한다
		   * 서버가 작동되고 있어야 그후에 켜진 클라이언트단의 한번 실행되는 커낵트를 수용할 수 있으므로
		   */
		  AppData.mBTAdapter.cancelDiscovery();
		  
		  try {
			  
			  mmSocket.connect();
	
			}catch (IOException connectException){
				try{
					mmSocket.close();
	
				} catch(IOException CloseException) {}				
				return;
			}
			manageConnectedSocket(mmSocket);
			
	    // TODO Auto-generated method stub
	}
	
	/**
	 * 데이터 송수신을 위한 클라이언트소켓 생성 쓰레드(클라이언트 단)
	 */
	class ConnectThread extends Thread{
	
		
		 BluetoothSocket mmSocket;
		 BluetoothDevice mmDevice;
		
		ConnectThread(BluetoothDevice device){
			
			
			  BluetoothSocket tmp = null;	
			  mmDevice = device;
			  	  
			  try{
				  
				 tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID); //리모트 디바이스의 uuid를 맞게 설정해야 작동한다	
			//	 Toast.makeText(DisplayData.this,"connect 생성 성공", Toast.LENGTH_LONG).show();
				 
			  }catch (IOException e){	
				  
			//	  Toast.makeText(DisplayData.this,"connect 생성 오류", Toast.LENGTH_LONG).show();
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
	//			Toast.makeText(DisplayData.this,"connected go", Toast.LENGTH_SHORT).show();
			} catch (IOException connectException) {
				
				try {
					
					mmSocket.close();	
				} catch(IOException CloseException) {}
				return;
			}
			
			manageConnectedSocket(mmSocket); //실제 데이터 송수신 작업을 하는 쓰레드 호출			
		}
		
		public void cancel() {
			
			try {
				
				mmSocket.close();				
			} catch(IOException e) {}
		}
		
	}

	/**
	 * 클라이언트와의 통신을 위한 스레드를 돌리기 위한 메서드
	 */
	protected void manageConnectedSocket(BluetoothSocket socket){
		
		if(clientThread != null)
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
	//			log("new client " + socket.getRemoteDevice().getAddress() + ".");
	//			Toast.makeText(DisplayData.this,"DD", Toast.LENGTH_SHORT).show();
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
				
	//			log("Lost connection to client "+ address + ".");
				try {
					this.socket.close();
				} catch(IOException e1){}
			} catch(Exception e) {
	//			log("error: " + e);
				try {
					this.socket.close();
				} catch(IOException e1) {}
			}
		}
	}
	

	/**
	 * 데이터 송수신 작업 쓰레드
	 */
	class ConnectedThread extends Thread{
	
		 BluetoothSocket mmSocket;
		 InputStream mmInStream;
		 OutputStream mmOutStream;
			
		ConnectedThread(BluetoothSocket socket){
						
			/** 송수신을 위한 스트림 생성*/
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch(IOException e) {}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			
		}
		

		public void run() {
			
			// 데이터 수신을 담기위한 버퍼 생성
			byte[] buffer = new byte[1024];
			int bytes;
			while(true) {
				
				try {
					
					bytes = mmInStream.read(buffer);		
				//	Toast.makeText(BlueToothClient.this,Integer.toString(bytes), Toast.LENGTH_SHORT).show();
				//	mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				}catch (IOException e){
					break;
				}
			}				
		}
		
		public void cancel(){
			
			try{
				
				mmSocket.close();
			} catch(IOException e) {}	
		}		
	}
	
	Handler mHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			
				case MESSAGE_WRITE:
					
					byte[] writeBuf = (byte[])msg.obj;
					writeMessage = new String(writeBuf);
		//			writeMessage = "전달";
//					mConversationArrayAdapter.add("Me: " + writeMessage);
	//				Toast.makeText(DisplayData.this,writeMessage, Toast.LENGTH_SHORT).show();
					break;
				
				case MESSAGE_READ:
					
					byte[] readBuf = (byte[])msg.obj;				
					readMessage = new String(readBuf, 0, msg.arg1);
					try {
						processMessage(readBuf);
						/*String encode = URLEncoder.encode(readMessage,"utf-8").replaceAll("%", " ");
						totalContent += encode;
						edit01.setText(totalContent);*/
					}catch(Exception e){}
				break;	
			}			
		}		
	};
	
	private void processMessage(byte[] buffer) {
		byte[] lngBytes = ElveUtil.getBytes(buffer, 4, 4);
		int lng = ElveUtil.byteArrayToInt(lngBytes);
		
		byte[] latBytes = ElveUtil.getBytes(buffer, 8, 4);
		int lat = ElveUtil.byteArrayToInt(latBytes);
		
		byte[] rpmBytes = ElveUtil.getBytes(buffer, 12, 2);
		int rpm = ElveUtil.byteArrayToInt(rpmBytes);
		
		byte[] speedBytes = ElveUtil.getBytes(buffer, 14, 1);
		int speed = ElveUtil.byteArrayToInt(speedBytes);
		
		byte[] rateBytes = ElveUtil.getBytes(buffer, 15, 1);
		int rate = ElveUtil.byteArrayToInt(rateBytes);
		
		byte[] tempBytes = ElveUtil.getBytes(buffer, 16, 1);
		int temp = ElveUtil.byteArrayToInt(tempBytes);
		
		byte[] volBytes = ElveUtil.getBytes(buffer, 17, 1);
		int vol = ElveUtil.byteArrayToInt(volBytes);
		
		byte[] accBytes = ElveUtil.getBytes(buffer, 18, 1);
		int acc = ElveUtil.byteArrayToInt(accBytes);
		
		byte[] braBytes = ElveUtil.getBytes(buffer, 19, 2);
		int bra = ElveUtil.byteArrayToInt(braBytes);
		
		byte[] aveBytes = ElveUtil.getBytes(buffer, 21, 2);
		int ave = ElveUtil.byteArrayToInt(aveBytes);
		

		
	}
	
	



	/**
	 * 이전 이벤트
	 */
	@Override
	public void onBackPressed() {
		
		super.onBackPressed();
		connectedThread.cancel();
		connectedThread = null;
		clientThread = null;

		finish();
	}
	
	public synchronized void stop() {
		
		if (connectedThread != null){
			
			connectedThread.cancel();
			connectedThread = null;					
		}
		if (clientThread != null){
			
			clientThread = null;					
		}
	}
}
