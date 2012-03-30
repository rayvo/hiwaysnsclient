
/**
 *  ���� ������� ������Ʈ ������ ������� Ŭ���̾�Ʈ ���� �ҽ��� �ƴ϶� 
 *  ������� Ŭ���̾�Ʈ (Ŀ��Ʈ �ϴ� ���� Ŭ���̾�Ʈ) �� ��ε彺�� �ϴ� �ٸ� ��� ����������� ����� �ϹǷ� Ŭ���̾�Ʈ�� �� ������Ʈ�� ���� �ϰ� �ִ�.
 *  ������� ������Ʈ�� ����Ҷ��� ���尡 �ƴϰ� ����Ʈ�θ� �ٲ��ָ� �ȴ�.
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
	    	    
/*Ŭ���̾�Ʈ ���� �� �� ���� ���� ������ */
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
		   * Ŭ���̾�Ʈ ���� �����ܿ� �����͸� ������ ���� write�Ұ��̹Ƿ� write�ϱ����� �����ܰ� Ŀ��Ʈ�� �ؾ��Ѵ�. 
		   * �������� accept�� ���� ���� �����Ƿ� �켱 ���� �� �۵��ǰ� �־�� �Ѵ�
		   * ������ �۵��ǰ� �־�� ���Ŀ� ���� Ŭ���̾�Ʈ���� �ѹ� ����Ǵ� Ŀ��Ʈ�� ������ �� �����Ƿ�
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
	 * ������ �ۼ����� ���� Ŭ���̾�Ʈ���� ���� ������(Ŭ���̾�Ʈ ��)
	 */
	class ConnectThread extends Thread{
	
		
		 BluetoothSocket mmSocket;
		 BluetoothDevice mmDevice;
		
		ConnectThread(BluetoothDevice device){
			
			
			  BluetoothSocket tmp = null;	
			  mmDevice = device;
			  	  
			  try{
				  
				 tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID); //����Ʈ ����̽��� uuid�� �°� �����ؾ� �۵��Ѵ�	
			//	 Toast.makeText(DisplayData.this,"connect ���� ����", Toast.LENGTH_LONG).show();
				 
			  }catch (IOException e){	
				  
			//	  Toast.makeText(DisplayData.this,"connect ���� ����", Toast.LENGTH_LONG).show();
			  }
			  mmSocket = tmp;			
		}
		
		/**
		 * ���� ���� �õ�
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
			
			manageConnectedSocket(mmSocket); //���� ������ �ۼ��� �۾��� �ϴ� ������ ȣ��			
		}
		
		public void cancel() {
			
			try {
				
				mmSocket.close();				
			} catch(IOException e) {}
		}
		
	}

	/**
	 * Ŭ���̾�Ʈ���� ����� ���� �����带 ������ ���� �޼���
	 */
	protected void manageConnectedSocket(BluetoothSocket socket){
		
		if(clientThread != null)
			return;
		
		clientThread = new ClientCommunicationThread(socket);
		clientThread.start();		
	}
	
	/**
	 * �������� Ŭ���̾�Ʈ Ŀ�´����̼� ������
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
	 * ������ �ۼ��� �۾� ������
	 */
	class ConnectedThread extends Thread{
	
		 BluetoothSocket mmSocket;
		 InputStream mmInStream;
		 OutputStream mmOutStream;
			
		ConnectedThread(BluetoothSocket socket){
						
			/** �ۼ����� ���� ��Ʈ�� ����*/
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
			
			// ������ ������ ������� ���� ����
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
		//			writeMessage = "����";
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
	 * ���� �̺�Ʈ
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
