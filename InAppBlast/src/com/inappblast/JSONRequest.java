package com.inappblast;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


/**
 * 
 * @author Maksym Fedyay
 */
class JSONRequest{
	
	private static final String LOG_TAG = ">> JSONUserId";
	
	private JSONObject o;
	
	
	@SuppressWarnings("unused")
	private JSONRequest(){}
	
	
	/**
	 *  
	 * @param userId
	 */
	public JSONRequest(String userId){
		
		if(TextUtils.isEmpty(userId))
			throw new IllegalArgumentException("Invalid argument passed to JSONRequest constructor");
		
		this.o = new JSONObject();
		
		try {
			this.o.putOpt("_id", userId);
			this.o.putOpt("user_id", userId);
			// TODO: set the current time in 2012-04-23T18:25:43.511 format
		} catch (JSONException e) {
			DLog.e(LOG_TAG, e.getMessage());
		}
	}
	

	
	/**
	 * Adds device id at the top level with the 
	 *  
	 * @return
	 */
	public JSONRequest setDeviceId(){
		WifiManager wifiManager = (WifiManager) AppBlast.getSharedInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String mac_as_device_id = wifiManager.getConnectionInfo().getMacAddress();
		try {
			this.o.putOpt("device_id", mac_as_device_id);
		} catch (JSONException e) {
			DLog.e(LOG_TAG, e.getMessage());
		}
		return this;
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public JSONRequest setDeviceProperties(){
		WifiManager wifiManager = (WifiManager) AppBlast.getSharedInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String mac_as_device_id = wifiManager.getConnectionInfo().getMacAddress();
		String device_model = android.os.Build.MANUFACTURER;
		String os_version = android.os.Build.VERSION.RELEASE;
		String platform = "ANDROID";
		JSONObject device = new JSONObject();
		try {
			device.putOpt("device_id", mac_as_device_id);
			device.putOpt("os_version", os_version);
			device.putOpt("device_model", device_model);
			device.putOpt("platform", platform);
			this.o.putOpt("_device", device);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	
	
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public JSONRequest setUserProperties(Map<String, String> params){
		
		if(this.o == null) return null;
		
		for(Entry<String, String> entry : params.entrySet()){
			
			String key = entry.getKey();
			String value = entry.getValue();
			
			try {
				this.o.putOpt(key, value);
			} catch (JSONException e) {
				DLog.e(LOG_TAG, e.getMessage());
			}
		}
		
		return this;
	}
	
	
	
	/**
	 * 
	 * @param delta1
	 * @param delta2
	 * @param action
	 * @param notificationId
	 * @return
	 */
	public JSONRequest setMarkData(long delta1, long delta2, int action, String notificationId){
		
		try {
			o.putOpt("notification_id", notificationId);
			o.putOpt("delta1", delta1);
			o.putOpt("delta2", delta2);
			o.putOpt("action", action);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
		}
		
		return this;
	}
	
	
	
	
	/**
	 * Creates {@link StringEntity} that is represent JSONObject
	 * @return {@link StringEntity} or null if error was occurred
	 * during StringEntity creation.
	 */
	public StringEntity getEntityString(){
		
		if(this.o == null) return null;
		
		StringEntity result = null;
		
		
		if(DLog.i())
			try {
				DLog.i(LOG_TAG, o.toString(3));
			} catch (JSONException e1) {
				if(DLog.e())
					DLog.e(LOG_TAG, e1.getMessage());
			}
		
		try {
			result = new StringEntity(this.o.toString());
		} catch (UnsupportedEncodingException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
		} 
		
		return result;
		
	}
	
	
	
}
