package com.inappblast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

/**
 * BlastConfig provides "flat" access to notification configuration data.
 * And asynchronously loads media data that is mentioned in configuration data.
 * But loading will happens only in case if {@link OnMediaLoadedListener}
 * instance passed to the constructor (pass <code>null</code> if no loading needed). 
 * @author Maksym Fedyay
 */
class BlastConfig {
	
	
	
	/**
	 * Logging tag
	 */
	private static final String LOG_TAG = ">> BlastConfigReader";
	
	
	
	/*
	 * JSON fields
	 */
	public static final String ERROR = "err";
	public static final String ID = "_id";
	public static final String CLOSE_IMAGE_URL = "close_image_url";
	public static final String CTA = "cta";
	public static final String CTA_COLOR = "cta_color";
	public static final String CTA_COLOR_HIGHLIGHTED = "cta_color_highlighted";
	public static final String CTA_URL = "cta_url";
	public static final String DESCRIPTION = "description";
	public static final String GRADIENT_OVERLAY_URL = "gradient_overlay_url";
	public static final String IMAGE_URL = "image_url";
	public static final String TITLE = "title";
	public static final String TYPE = "type";
	public static final String NOTIFICATION = "notification";
	public static final String VARIATION = "variation";

	
	
	/**
	 * 
	 */
	private long timerStart;
	
	
	
	/**
	 * JSONObject that contains configuration data
	 */
	private JSONObject o;

	
	
	/**
	 * The {@link OnMediaLoadedListener} listener
	 */
	private OnMediaLoadedListener listener;
	
	
	
	/**
	 * BlastConfig event listener should be used in order 
	 * to be notified about media file downloading result.
	 * @author Maksym Fedyay
	 */
	public interface OnMediaLoadedListener{
		

			/**
			 * All data successfully loaded and ready to use.
			 * Additionally passed current BlastConfig instance
			 * with the links to the loaded files.
			 */
		public static final int DATA_READY = 200;
			
			/**
			 * While loading error occurred. Additional string parameters is 
			 * passed that is describes the source of error.
			 */
		public static final int ERROR_WHILE_LOADING = 500;
		
		/**
		 * Will be called in two cases. When all data is loaded successfully,
		 * or in the case of loading error.
		 * @param event - {@link LoadEvent} 
		 * @param args - optional parameters that can be supplied.
		 */
		void onMediaLoaded(int event, Object...args);
		
	}
	

	
	/**
	 * Constructs a new <code>BlastConfig</code>. BlastConfig is a wrapper for a
	 * {@link JSONObject}, and provides "flat" data access. Pass {@link OnMediaLoadedListener}
	 * instance in constructor to initiate media loading, or null if no media loading needed.
	 * @param {@link {@link JSONObject} - which contains configuration data. If
	 *        null passed then {@link IllegalArgumentException} will be thrown.
	 * @param listener - {@link OnMediaLoadedListener}
	 */
	public BlastConfig(JSONObject o, OnMediaLoadedListener listener) throws IllegalArgumentException {
		
		if (o == null){
			throw new IllegalArgumentException(
					"JSONObject that is passed to BlastConfig constructor can't be null."
				);
		}

		this.o = o;
		
		timerStart = new Date().getTime();
		
		if(listener != null){
			this.listener = listener;
			MediaLoader loader = new MediaLoader();
			loader.execute(getURIs());
		}
		
		if(DLog.i())
			DLog.i(LOG_TAG, "Instance created.");
		
	}

	
	
	/**
	 * Returns error description. If no error then empty string.
	 * @return Error description.
	 */
	// TODO : check what returns if no error
	public String getError() {
		try {
			return o.getString(ERROR);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns notification id
	 * @return String with notification id.
	 */
	public String getId() {
		try {
			return o.getJSONObject(NOTIFICATION).getString(ID);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns call to action string.
	 * @return String with call to action.
	 */
	public String getCta() {
		try {
			return o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(CTA);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns background color for action button
	 * @return String with color
	 */
	public int getCtaColor() {
		try {
			String sColor = "#" + o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(CTA_COLOR);
			int color = Color.parseColor(sColor);
			return color;
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return 0;
		}
	}

	
	
	/**
	 * Returns background color for action button pressed state
	 * @return
	 */
	public int getCtaColorHighlighted() {
		try {
			String sColor = "#" + o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(CTA_COLOR_HIGHLIGHTED);
			int color = Color.parseColor(sColor);
			return color;
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return 0;
		}
	}

	
	
	/**
	 * Returns URL to which user should be redirected after pressing action
	 * button.
	 * @return String with URL.
	 */
	public String getCtaUrl() {
		try {
			return o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(CTA_URL);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns string with detailed description.
	 * @return String with description
	 */
	public String getDescription() {
		try {
			return o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(DESCRIPTION);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns title
	 * @return String with title
	 */
	public String getTitle() {
		try {
			return o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(TITLE);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns type of
	 * @return
	 */
	public String getType() {
		try {
			return o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(TYPE);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Returns String array with the resources URLs for Bitmaps. <br/>
	 * The contract is: <br/>
	 * String[0] is CLOSE_IMAGE_URL <br/>
	 * String[1] is GRADIENT_OVERLAY_URL <br/>
	 * String[2] is IMAGE_URL <br/>
	 * @return String array
	 */
	private String[] getURIs() {
		String[] sUrls = new String[3];
		try {
			sUrls[0] = o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(CLOSE_IMAGE_URL);
			sUrls[1] = o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(GRADIENT_OVERLAY_URL);
			sUrls[2] = o.getJSONObject(NOTIFICATION).getJSONObject(VARIATION)
					.getString(IMAGE_URL);
		} catch (JSONException e) {
			if(DLog.e())
				DLog.e(LOG_TAG, e.getMessage());
		}
		return sUrls;
	}

	
	
	/**
	 * {@link BitmapDrawable} array, for containing 
	 * notification Drawable data 
	 */
	private BitmapDrawable[] bitmaps;
	
	
	
	/**
	 * 
	 * @author Maksym Fedyay
	 */
	private class MediaLoader extends AsyncTask<String[], Void, BitmapDrawable[]>{
		
		
		
		/* Log creation */
		public MediaLoader(){
			if(DLog.i())
				DLog.i(LOG_TAG + ".MediaLoader", "Initialized | " + this.toString());
		}
		
		
		
		/* Log death */
		@Override
		protected void finalize() throws Throwable {
			if(DLog.i())
				DLog.i(LOG_TAG + ".MediaLoader", "Destroyed | " + this.toString());
			super.finalize();
		}
		
		
		
		/*
		 * 
		 */
		@Override
		protected BitmapDrawable[] doInBackground(String[]... params) {
			
			DefaultHttpClient client = new DefaultHttpClient();
			client.setRedirectHandler(new DefaultRedirectHandler());
			
			BitmapDrawable[] bitmaps = new BitmapDrawable[params[0].length];
			
			String[] sUrls = params[0];
			
			for (int i = 0; i < params[0].length; i++) {
				
				/* Continue if URL is null */
				if(sUrls[i] == null){
					bitmaps[i] = null;
					continue;
				}
				
				try {
					if(DLog.i())
						DLog.i(LOG_TAG + ".MediaLoader.doInBackground()", "Start loading | " + sUrls[i]);
					
					HttpGet request = new HttpGet(sUrls[i]);
					HttpResponse response = client.execute(request);
					StatusLine statusLine = response.getStatusLine();
					
					int statusCode = statusLine.getStatusCode();
					
					if(DLog.i())
						DLog.i(LOG_TAG, "Loading finished | " + statusLine.toString());

					if(statusCode == 200){
						InputStream is = response.getEntity().getContent();
						
						BitmapDrawable drawable = new BitmapDrawable(AppBlast
								.getSharedInstance().getApplicationContext()
								.getResources(), is);
						
						bitmaps[i] = drawable;
					}
					
				} catch (ClientProtocolException e) {
					if(DLog.e())
						DLog.e(LOG_TAG, e.getMessage());
				} catch (IOException e) {
					if(DLog.e())
						DLog.e(LOG_TAG, e.getMessage());
				}
			}
			
			return bitmaps;
		}
		
		
		
		/*
		 * 
		 */
		@Override
		protected void onPostExecute(BitmapDrawable[] result) {
			
			if(result == null){
				BlastConfig.this.listener.onMediaLoaded(OnMediaLoadedListener.ERROR_WHILE_LOADING);
				return;
			}
			
			BlastConfig.this.bitmaps = result;
			BlastConfig.this.listener.onMediaLoaded(OnMediaLoadedListener.DATA_READY, BlastConfig.this);
			
		}
	}
	
	
	
	/**
	 * Returns Array of {@link BitmapDrawable}.
	 * The contract is: <br/>
	 * BitmapDrawable[0] is close image <br/>
	 * BitmapDrawable[1] is gradient overlay <br/>
	 * BitmapDrawable[2] is main background image <br/>
	 * Call of this method is invalid until {@link OnMediaLoadedListener#onMediaLoaded(LoadEvent, Object...)} 
	 * notification call about data loading success event.
	 * The all before that will return <code>null</code> 
	 * @return BitmapDrawable[]
	 * @see BlastConfig#BlastConfig(JSONObject, OnMediaLoadedListener)
	 */
	public BitmapDrawable[] getBitmaps(){
		return this.bitmaps;
	}
	
	
	
	/**
	 * Returns time delta in milliseconds.
	 * This delta indicated time elapsed from {@link BlastConfig}
	 * constructing moment to the moment of this method call.
	 * @return
	 */
	public long getDelta(){
		return new Date().getTime() - timerStart;
	}
	
}
