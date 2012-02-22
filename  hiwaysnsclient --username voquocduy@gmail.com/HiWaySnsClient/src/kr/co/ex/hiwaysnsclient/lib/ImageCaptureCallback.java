package kr.co.ex.hiwaysnsclient.lib;

import java.io.OutputStream;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

public class ImageCaptureCallback implements PictureCallback  {

	private OutputStream filoutputStream;
	public ImageCaptureCallback(OutputStream filoutputStream) {
		this.filoutputStream = filoutputStream;
	}
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			Log.v("[IMAGE CAPTURE CALLBACK]", "onPictureTaken=" + data + " length = " + data.length);
			///*
			filoutputStream.write(data);
			filoutputStream.flush();
			filoutputStream.close();
			//*/
		} catch(Exception ex) {
    		Log.e( "[EXCEPTION]", ex.toString() );
		}
	}
}