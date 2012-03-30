package com.cewit.elve.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class BlueTooth extends Activity {
	
	
	private static final int REQUEST_CONNECT_DEVICE=1;
	private static final int REQUEST_ENABLE_BT=2;
		
	ArrayList<String> arGeneral;
	ArrayAdapter<String> Adapter;
	
	String tag;
	String macAddress[];
	int macAddressNum;
		
	BluetoothDevice device;
	BluetoothSocket mmSocket;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	   	
    	Log.d(tag,"##############.1onCreate");
    	macAddress = new String[20];
    	macAddressNum = 0;
    	  	
    	 AppData.mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    	 
		 /*
		  * ������� Ȱ��ȭ �޼ҵ�� �������� �ʾҴ� . �׷��Ƿ� ���� ������� �ѱ⸦ �������� ������Ѵ�
		  * �ٷ� ������� ��� �˻����� ����.(�������� �ױ� ����) 	
		  */	 
        super.onCreate(savedInstanceState);
        
        //��������� ����̽��� �׼��� ����ֱ� ���� �켱 ���ù� �ڵ鷯��  ��Ͻ����ش�      
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
	 	setContentView(R.layout.bluetooth_main);
	 		
	 	arGeneral = new ArrayList<String>();
	
 		
 		Adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arGeneral);
 		
 		ListView list = (ListView)findViewById(R.id.list);
 		list.setAdapter(Adapter);    
	 		 		
	 	//����Ʈ ���� �ϳ��� �������� ��ġ������.
	 	list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	 		
	 		public void onItemClick(AdapterView<?> av,View v,int position,long id ){
	 				 				
					// �˻��� ������� ����̽��� �� ��巡���� �̿��Ͽ� ����Ʈ ������� ����̽��� ��ü�� �����Ѵ�
	 				AppData.DEVICE_REMOTE = AppData.mBTAdapter.getRemoteDevice(macAddress[position]);
	 				
	 				//����̽� �� �������� �����߱⶧���� ���� ����̽� �˻��� �����Ѵ�.	
	 				AppData.mBTAdapter.cancelDiscovery();
	 				
	 				XLib.startActivity(BlueTooth.this, "ElveMainActivity");	
	 			}
	 		});
                       
         Button scanButton = (Button)findViewById(R.id.blue_search);
         
         scanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});
        
    }
    
    private void doDiscovery(){
    	
    	if(AppData.mBTAdapter.isDiscovering()) { //�ֺ���� ���� ��� �� �˻��Ѵ�.
    		
    		AppData.mBTAdapter.cancelDiscovery();
    	}
    	AppData.mBTAdapter.startDiscovery();
    }
 
    /**
     * �˻��߿� �ֺ���⸦ ã������ ����޴� ���ù� �ڵ鷯�̴� �ڵ鷯�� ����޾����� ������� ����̽��� ����Ʈ�信 �߰����ش�
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	
    	public void onReceive(Context context, Intent intent) {
    		
    		String action = intent.getAction();
    		
    		if(BluetoothDevice.ACTION_FOUND.equals(action)) {
    			
    			device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    			Adapter.add(device.getName()+'\n'+device.getAddress());
    			macAddress[macAddressNum++] = device.getAddress();			  
		  }
    		if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			  
    			setProgressBarIndeterminateVisibility(false);
    			setTitle(R.string.select_device);
    			
    			if(Adapter.getCount()==0){
				  
				  String noDevice = getResources().getText(R.string.none_found).toString();
				  Adapter.add(noDevice);
			  }
		  }
	  }
  };
    
  /*
  String DownloadHtml(String addr){
	  StringBuilder html=new StringBuilder();
	  try{
		  URL url=new URL(addr);
		  HttpURLConnection conn=(HttpURLConnection)url.openConnection();
		  if(conn != null){
			  conn.setConnectTimeout(1000);
			  conn.setUseCaches(false);
			  if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
				  BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
				  
				  for(;;){
					  String line=br.readLine();
					  if(line==null)break;
					  html.append(line+'\n');
				  }
				  br.close();
			  }
			  conn.disconnect();
		  }
	  }
	  catch(Exception ex){;}
	  return html.toString();
	  
  }
  */
  
  
  /*
    public void onStart(){
    	Log.d(tag,"##############.onstart");
    	if(!mBTAdapter.isEnabled()){
    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
    		
    	}
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent date){
    	Log.d(tag,"##############.onactivityresult");
    	
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:
    		
    	case REQUEST_ENABLE_BT:
    		if(resultCode == Activity.RESULT_OK){
    //			setupChat();
    		}
    		else{
    			Toast.makeText(this,R.string.bt_not_enable_leaving, Toast.LENGTH_SHORT).show();
    //			finish();
    		}
    	}
    }
   */
  
  /**
   * ����� ��ε�ĳ��Ʈ���ù��� ��� ���
   */
  public void onDestroy(){
	  unregisterReceiver(mReceiver);
  }
  
}