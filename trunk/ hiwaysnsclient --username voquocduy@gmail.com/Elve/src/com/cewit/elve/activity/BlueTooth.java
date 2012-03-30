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
		  * 블루투스 활성화 메소드는 정의하지 않았다 . 그러므로 폰의 블루투스 켜기를 수동으로 켜줘야한다
		  * 바로 블루투스 기기 검색으로 들어간다.(수동으로 켰기 때문) 	
		  */	 
        super.onCreate(savedInstanceState);
        
        //블루투스의 디바이스의 액션을 잡아주기 위해 우선 리시버 핸들러를  등록시켜준다      
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
	 	setContentView(R.layout.bluetooth_main);
	 		
	 	arGeneral = new ArrayList<String>();
	
 		
 		Adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arGeneral);
 		
 		ListView list = (ListView)findViewById(R.id.list);
 		list.setAdapter(Adapter);    
	 		 		
	 	//리스트 뷰의 하나의 아이템을 터치했을때.
	 	list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	 		
	 		public void onItemClick(AdapterView<?> av,View v,int position,long id ){
	 				 				
					// 검색된 블루투스 디바이스의 맥 어드래스를 이용하여 리모트 블루투스 디바이스의 객체를 생성한다
	 				AppData.DEVICE_REMOTE = AppData.mBTAdapter.getRemoteDevice(macAddress[position]);
	 				
	 				//디바이스 한 아이템을 선택했기때문에 이제 디바이스 검색은 중지한다.	
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
    	
    	if(AppData.mBTAdapter.isDiscovering()) { //주변블루 투스 기기 를 검색한다.
    		
    		AppData.mBTAdapter.cancelDiscovery();
    	}
    	AppData.mBTAdapter.startDiscovery();
    }
 
    /**
     * 검색중에 주변기기를 찾았을때 응답받는 리시버 핸들러이다 핸들러가 응답받았을때 응답받은 디바이스를 리스트뷰에 추가해준다
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
   * 등록한 브로드캐스트리시버의 등록 취소
   */
  public void onDestroy(){
	  unregisterReceiver(mReceiver);
  }
  
}