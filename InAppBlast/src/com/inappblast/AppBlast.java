package com.inappblast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.inappblast.BlastConfig.OnMediaLoadedListener;


/**
 * Use this class to interact with the InAppBlast service.
 * TODO: Write more documentations for AppBlast class
 * @author Maksym Fedyay
 */
//TODO: Write more documentations for AppBlast class
public class AppBlast implements OnMediaLoadedListener{
	
	
	/**
	 * Disable all logs
	 */
	public static final int LL_NO = 12;
	
	
	/**
	 * Show all logs (ERROR and INFO log types)
	 */
	public static final int LL_ALL = 14;
	
	
	/**
	 * Show only ERROR logs
	 */
	public static final int LL_ERROR = 13;
	
	
	
	/**
	 * Logging tag
	 */
	private static final String  LOG_TAG = ">> AppBlast";
	
	
	
	/**
	 * Project key that must be set during <code>initSharedInstance()</code> 
	 */
	private static String projectKey;
	
	
	
	/**
	 * Shared preferences for storing the current userId
	 */
	private SharedPreferences pref;
	
	
	
	/**
	 * Name of shared preferences
	 */
	private static final String PREF_NAME = "pref";
	
	
	
	/**
	 * Shared preferences userId key
	 */
	private static final String SP_USER_ID = "user_id";
	
	
	
	/**
	 * Hidden constructor
	 */
	private AppBlast(){}

	
	
	/**
	 * Base server URL 
	 */
	private static final String URI_BASE = "http://api.pushpanel.io/v1";
	
	
	
	/**
	 * <b>&lt;server_address></b>/user/set </br>
	 * The address which should be used in order to set userId
	 * and user properties
	 */
	private static final String URI_SET = "/user/set";
	
	
	
	/**
	 * <b>&lt;server_address></b>/user/device/del </br>
	 * The address for deleting userId
	 */
	private static final String URI_UNSET = "/user/device/del";
	
	
		
	/**
	 * <b>&lt;server_address></b>/notification/get/<b>&lt;project_key></b>/<b>&lt;userId></b></br>
	 * The address of notification configuration data.
	 */
	private static final String URI_GET = "/notification/get/%1$s/%2$s";
	

	
	/**
	 * <b>&lt;server_address></b>/notification/mark</br>
	 * The address for reporting about notification performance and 
	 * relative statistic data.
	 */
	private static final String URI_MARK = "/notification/mark";
	
	
	
	/**
	 * Keeper used for the thread safe singleton creation.
	 * This is guarantees that during simultaneous call of 
	 * AppBlast.getSharedInstance() from different threads
	 * will produce one and only one instance.
	 * @author Maksym Fedyay
	 */
	private static final class Keeper{
		public static final AppBlast instance = new AppBlast();
	}
	
	
	
	/**
	 * Lock for blocking operations that should not be 
	 * started simultaneously.  
	 */
	// XXX: May not needed
	@SuppressWarnings("unused")
	private final Object lock = new Object();
	
	
	
	/**
	 * The reference to application object
	 * We need that object to gain access to SharedPreferences
	 */
	private static Application application;
	
	
	
	/**
	 * {@link AppBlast} will notify listeners thru this interface
	 */
	private OnBlastActionListener listener;
	
	
	
	/**
	 * Call this method to instantiate and initialize the AppBlas.
	 * You should pass valid projectKey, otherwise AppBlast
	 * will not work correctly. The call of this method valid only once.
	 * All subsequent call of this method will not affect the AppBlast state. 
	 * @param projectKey - String with valid project key
	 * @param application - reference to Application instance. Application must implement {@link OnBlastActionListener}
	 * @throws IllegalArgumentException
	 */
	public static void initSharedInstance(String projectKey, Application application){// throws IllegalArgumentException{
		
		if(TextUtils.isEmpty(projectKey) || application == null)
			throw new IllegalArgumentException(
					"The AppBlast.initSharedInstance() can't be called with empty or null parameters."
				);
		
		AppBlast.application = application;
		
		AppBlast.projectKey = projectKey;
		
		final AppBlast blast = Keeper.instance;
		
		blast.pref = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		
		if(application instanceof OnBlastActionListener)
			blast.listener = (OnBlastActionListener) application;
		
		String userId = blast.getUserId();
		
		/*  */
		if(DLog.i())
			DLog.i(LOG_TAG + ".initSharedInstance()",
					"[projectKey=" + projectKey +
					"] [userId=" + userId + "]");
		
		
		blast.timer = new Timer();
		
		blast.loadConfig();
		
	}
	
	
	
	/**
	 * Gets configuration data for notification
	 */
	private void loadConfig(){
		
		String userId = getUserId();
		
		if(TextUtils.isEmpty(userId)) return;
		
		String uriGet = String.format(URI_BASE + URI_GET, AppBlast.projectKey, userId);
		
		if(DLog.i())
			DLog.i(LOG_TAG + ".loadConfig()", "<< | URI=" + uriGet);
		
		HttpGet get = new HttpGet(uriGet); 
		NetTaskRequest request = new NetTaskRequest(NetTaskRequest.GET_CONFIG, get);
		NetTask task = new NetTask();
		task.execute(request);
		
		if(DLog.i())
			DLog.i(LOG_TAG + ".loadConfig()", ">>");
	}
	
	
	
	/**
	 * Returns the instance of AppBlast. Make sure to call
	 * {@link AppBlast#initSharedInstance(String)} 
	 * before this method call. If not to do so - {@link IllegalAccessError} will be generated during
	 * this method call.
	 * @return The instance of AppBlast
	 */
	public static AppBlast getSharedInstance(){// throws IllegalAccessException{
		if(Keeper.instance == null)
			throw new IllegalAccessError(
					"The call of AppBlast.getSharedInstance() before AppBlast.initSharedInstance() is illegal. "
					+ "Call AppBlast.getSharedInstance() first."
				); 
		return Keeper.instance;
	}
	
	
	
	/**
	 * {@link AppBlast InAppBlast} library provides interface for
	 * log leveling in order to provide as much as possible information
	 * about library internal processes
	 * or disable all logging.<br/>
	 * Pass one of the following constants according to the needs.<br/>
	 * {@link DLog#LL_ALL} - enable all logs<br/>
	 * {@link DLog#LL_ERROR} - show only error logs<br/>
	 * {@link DLog#LL_NO} - disable all logs<br/>
	 * Default logging level is {@link DLog#LL_NO}. 
	 * @param level - logging level
	 */
	public void setLogLevel(int level){
		DLog.setLogLevel(level);
	}
	
	
	
	/**
	 * User id that is about to be saved in SharedPreferences
	 * after being successfully saved on the server
	 */
	private String userId;
	
	
	
	/**
	 * Sets userId on remote server and locally.
	 * Locally saved only after success on server side.
	 * If pass empty string or null - no action will be taken.
	 * This operation is asynchronous and does not provide callback
	 * thru listener. After success of setting userId remotely and locally
	 * will be taken attempt to load and display notification.
	 * Notification will be displayed only when foreground {@link Activity}
	 * will be passed via {@link AppBlast#setBlastPoint(Activity)}
	 * method.
	 *   
	 * @param userId - String with the user id
	 */
	public void setUserId(String userId){
		
		if(TextUtils.isEmpty(userId)) return;
		
		this.userId = userId;
		
		HttpPost post = new HttpPost(URI_BASE + URI_SET);
		post.setHeader("key", AppBlast.projectKey);
		post.setHeader("Content-Type", "application/json");
		StringEntity sEntity = new JSONRequest(userId)
			.setDeviceProperties()
			.getEntityString();
		
		if(sEntity == null){
			if(DLog.e())
				DLog.e(LOG_TAG, "setUserId() interrapted because of error in json request creation");
			return;
		}
		
		post.setEntity(sEntity);
		NetTaskRequest request = new NetTaskRequest(NetTaskRequest.SET_USER_ID, post);
		NetTask task = new NetTask();
		task.execute(request);
	}
	
	
	
	/**
	 * Saves userId locally in shared preferences.
	 * This method should be called ONLY in the case of 
	 * {@link AppBlast#setUserId(String) setUserId()} 
	 * success.
	 * @param userId - String with the user id
	 */
	private void saveUserId(){
		pref.edit().putString(SP_USER_ID, this.userId).commit();
		AppBlast.this.loadConfig();
		this.userId = null;
		
	}
	
	
	
	/**
	 * Sets userId if userId is not set, or different from the 
	 * current userId. 
	 * 
	 * @param userId - String with the userId
	 * @return true if call of this method leads to set userId action,
	 * false otherwise.
	 */
	public boolean setUserIdIfNotSet(String userId){
		
		if(TextUtils.isEmpty(userId)) return false;
		
		String currentId = getUserId();
		
		if(userId.equals(currentId)) return false;
			
		setUserId(userId);
		
		return true;
	}
	
	
	
	/**
	 * Returns userId that was previously saved 
	 * or <code>null</code> if no userId found.
	 * @return String with userId
	 * @see AppBlast#setUserId(String)
	 * @see AppBlast#setUserIdIfNotSet(String)
	 */
	public String getUserId(){
		return pref.getString(SP_USER_ID, null);
	}
	
	
	
	/**
	 * Removes current userId. If userId previously not
	 * then operation ignored. 
	 */
	public void removeUserId(){
		
		HttpPost post = new HttpPost(URI_BASE + URI_UNSET);
		
		post.setHeader("Content-Type", "application/json");
		post.setHeader("key", AppBlast.projectKey);
		
		NetTaskRequest request = new NetTaskRequest(NetTaskRequest.UNSET_USER_ID, post);
		NetTask task = new NetTask();
		task.execute(request);
	}
	
	
	
	/**
	 * Sets user property. 
	 * @param property
	 */
	public void setUserProperty(Map<String, String> property){
	
		if(property == null || property.size() == 0) return;
		
		String userId = getUserId(); 
		
		if(TextUtils.isEmpty(userId)) return;
		
		HttpPost post = new HttpPost(URI_BASE + URI_SET);
		
		post.setHeader("key", AppBlast.projectKey);
		post.setHeader("Content-Type", "application/json");
		StringEntity entity = new JSONRequest(userId)
			.setUserProperties(property)
			.getEntityString();
		
		post.setEntity(entity);
		
		NetTaskRequest request = new NetTaskRequest(NetTaskRequest.SET_USER_PROPERTY, post);
		NetTask task = new NetTask();
		task.execute(request);
		
	}
	
	
	
	/**
	 * 
	 * @param delta2
	 */
	void postUsageInfo(long delta1, long delta2, int action){
		
		if(this.config == null) return;
		
		if(delta1 == 0 || delta2 == 0) return;
		
		String userId = this.pref.getString(SP_USER_ID, null);
		
		if(TextUtils.isEmpty(userId)) return;
		
		HttpPost post = new HttpPost(URI_BASE + URI_MARK);
		
		post.setHeader("key", AppBlast.projectKey);
		post.setHeader("Content-Type", "application/json");
		
		String notificationId = this.config.getId();
		
		StringEntity entity = new JSONRequest(userId)
			.setMarkData(delta1, delta2, action, notificationId)
			.getEntityString();
		
		post.setEntity(entity);
		
		NetTaskRequest request = new NetTaskRequest(NetTaskRequest.POST_MARK, post);
		
		NetTask task = new NetTask();
		task.execute(request);
	}
	
	
	
	
	/**
	 * 
	 * @author Maksym Fedyay
	 */
	static class NetTaskRequest {

		public static final int GET_CONFIG = 10;
		public static final int POST_MARK = 11;
		public static final int SET_USER_ID = 12;
		public static final int SET_USER_PROPERTY = 13;
		public static final int UNSET_USER_ID = 14;

		private HttpUriRequest request;

		private int action;

		public NetTaskRequest(int action, HttpUriRequest request) {

			if (action == 0 || request == null)
				throw new IllegalArgumentException(
						"Illegal arguments passed to NetTaskDesctiption constructor.");

			this.action = action;
			this.request = request;
		}

		public int getAction() {
			return this.action;
		}

		public HttpUriRequest getRequest() {
			return this.request;
		}

		public URI getURI() {
			return this.request.getURI();
		}

	}
	
	
	
	/**
	 * 
	 * @author Maksym Fedyay
	 */
	private class NetTask extends AsyncTask<NetTaskRequest, Void, JSONObject>{

		
		private int action;
		
		/* Log creation */
		public NetTask(){
			
			if(DLog.i())
				DLog.i(LOG_TAG + ".NetTask", "Initialized | " + this.toString());
		}
		
		/* Log death */
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			
			if(DLog.i())
				DLog.i(LOG_TAG + ".NetTask", "Destroyed | " + this.toString());
			
		}
		
		
		/*
		 * 
		 */
		@Override
		protected JSONObject doInBackground(NetTaskRequest... params) {
			
			JSONObject result = null;
			
			DefaultHttpClient client = new DefaultHttpClient();
			client.setRedirectHandler(new DefaultRedirectHandler());
			
			NetTaskRequest descriptor = params[0];
			HttpUriRequest request = descriptor.getRequest();
			
			this.action = descriptor.getAction();
			
			try {
				
				HttpResponse response = client.execute(request);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.doInBackground()", statusLine.toString());

				if(statusCode != 200) return null;
					
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				
				Scanner scanner = new Scanner(is);
				String json = scanner.useDelimiter("\\A").next();
				
				scanner.close();
				is.close();
				
				result = (JSONObject) new JSONTokener(json).nextValue();
				
			} catch (ClientProtocolException e) {
				if(DLog.e())
					DLog.e(LOG_TAG + ".NetTask.doInBackground()", e.getMessage());
			} catch (IOException e) {
				if(DLog.e())
					DLog.e(LOG_TAG + ".NetTask.doInBackground()", e.getMessage());
			} catch (JSONException e) {
				if(DLog.e())
					DLog.e(LOG_TAG + ".NetTask.doInBackground()", e.getMessage());
			}
			
			return result;
		}
		
		
		/*
		 * 
		 */
		@Override
		protected void onPostExecute(JSONObject result) {
			
			/* Check if result is not null */
			if(result == null){
				if(DLog.e())
					DLog.e(LOG_TAG + ".NetTask.onPostExecute()", 
							"Request with code " + this.action + " receive NULL response."
									+ "This may be caused by lack of network connection.");
				return;
			}
			
			
			/* Log the response */
			if(DLog.i())
				try {
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", result.toString(3));
				} catch (JSONException e) {
					if(DLog.e())
						DLog.e(LOG_TAG + ".NetTask.onPostExecute()", e.getMessage());
				}
			
			
			
			/* Check if error string is null */
			if(!result.isNull(BlastConfig.ERROR)){
				if(DLog.e())
					try {
						DLog.e(LOG_TAG + ".NetTask.onPostExecute()", 
								"Response come with error [" +result.getString(BlastConfig.ERROR)+"]");
					} catch (JSONException e) {
						if(DLog.e())
							DLog.e(LOG_TAG + ".NetTask.onPostExecute()", e.getMessage());
					}
				
				// TODO : Try to pass user id thru the NetTaskRequest 
				AppBlast.this.userId = null;
				
				return;
			}
			
			/* PostExecute actions */
			switch (this.action) {
			
			case NetTaskRequest.GET_CONFIG:
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", "Notification configuration loaded successfully.");
				
				new BlastConfig(result, AppBlast.this);
				
				break;

			case NetTaskRequest.POST_MARK:
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", "Notification statistics uploaded successfully.");
				
				// XXX: Nothing to do
				
				break;
			
			case NetTaskRequest.SET_USER_ID:
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", "User id set successfully.");
				
				AppBlast.this.saveUserId();
				
				break;
			
			case NetTaskRequest.UNSET_USER_ID:
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", "User id deleted successfully.");
				
				break;
			
			case NetTaskRequest.SET_USER_PROPERTY:
				
				if(DLog.i())
					DLog.i(LOG_TAG + ".NetTask.onPostExecute()", "User property set successfully.");
				
				break;
			}
			
		}
		
	}

	
	
	/**
	 * Activity that is in foreground
	 */
	private Activity activity;
	
	
	
	/**
	 * Use this method to set {@link Activity} that is in foreground,
	 * and delete activity when activity goes to background by passing null.
	 * {@link AppBlast} will keep weak reference to this activity in order to 
	 * provide extra action against memory leak. However this is important
	 * to always call {@link AppBlast#setBlastPoint(Activity) setBlastPoint(null)}
	 * when activity being paused. 
	 * Foreground activity needed for launching activity with notification.
	 * @param activity - foreground activity
	 */
	public void setBlastPoint(Activity activity){
		
		
		if (this.isMediaReady && activity != null){
			
			AppBlast.this.timer.cancel();
			this.importanceCode = 0;
			
			if(DLog.i())
				DLog.i(LOG_TAG + ".setBlastPoint()", "Activity " + activity.toString() + " and media data is ready.");
			
			startActivity();
			
		} else if (activity == null){
			
			if(this.importanceCode == 0) {
				if(DLog.i())
					DLog.i(LOG_TAG + ".setBlastPoint()", "Timer started");
				startTimer();
			}
			
		} else if (activity != null) {
			
			AppBlast.this.timer.cancel();
			
			if(!(activity instanceof ActBlast) && (this.importanceCode == 400 || this.importanceCode == 300)){
				if(DLog.i())
					DLog.i(LOG_TAG + ".setBlastPoint()", "!Notification start initialized after process being in background.");
				loadConfig();
			}
			
			this.importanceCode = 0;
			
		}
		
		this.activity = activity;
		
	}
	
	
	
	/**
	 * Starts notification activity
	 */
	private void startActivity(){
		Intent intent = new Intent(this.activity, ActBlast.class);
		this.activity.startActivity(intent);
		this.isMediaReady = false;
	}
	
	
	
	/**
	 * 
	 */
	private boolean isMediaReady = false;
	
	
	
	/**
	 * 
	 */
	private BlastConfig config;
	
	
	
	/*
	 * BlastConfig#OnMediaLoaderListener implementation
	 */
	@Override
	public void onMediaLoaded(int event, Object... args) {
		
		switch (event) {
		
		case OnMediaLoadedListener.DATA_READY:
			
			if(DLog.i())
				DLog.i(LOG_TAG + ".onMediaLoaded()@OnMediaLoadedListener", "Data ready.");
			
			this.config = (BlastConfig) args[0];
			
			if(this.activity != null){
				startActivity();
			} else {
				this.isMediaReady = true;
			}
			
			break;
			
		case OnMediaLoadedListener.ERROR_WHILE_LOADING:
			
			if(DLog.e())
				DLog.e(LOG_TAG, "Error while loading media data. Notification creation skipped.");
			
			break;
		}
		
	}
	
	
	
	/**
	 * Returns {@link BlastConfig} instance.
	 * The call of this method is invalid until
	 * {@link OnMediaLoadedListener#onMediaLoaded(int, Object...)}
	 * callback.
	 * @return
	 */
	BlastConfig getConfig(){
		return this.config;
	}
	
	
	
//	/**
//	 * Remove reference to BlastConfig 
//	 */
//	void dropConfig(){
//		this.config = null;
//	}
	
	
	
	/**
	 * Returns application {@link Context}
	 * This context needed in {@link JSONRequest}
	 * and in {@link BlastConfig} classes.
	 * This Context can't be used in layout 
	 * inflation process.
	 * @return application {@link Context}
	 */
	Context getApplicationContext(){
		return application.getApplicationContext();
	}
	
	
	
	/**
	 * This method notifies about notification Activity actions
	 * those actions indicates the way the activity closed, this means
	 * that notification Activity must be stopped (destroyed) immediately after 
	 * this method call. Also this method releases all resources that was kept for 
	 * notification activity, including blast point.
	 * @param code
	 * @param params
	 */
	void notifyApplication(int code, Object...params){
		// Release all resources
		this.config = null;
		this.activity = null;
		
		if(this.listener == null) return;
		// Notify user about notification action
		this.listener.onBlastAction(code, params);
		
	}
	
	
	/*
	 * Checking current process importance code in background  
	 */
	
	private Timer timer;
	
	private int importanceCode = 0;
	
	
	/**
	 * 
	 */
	private void startTimer(){
		
		if(this.timer.purge() == 0){
			this.timer = new Timer();
		}
		
		
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				AppBlast.this.importanceCode = checkProcessState();
				
				if(AppBlast.this.importanceCode == 400 
						|| AppBlast.this.importanceCode == 300 ){
//						|| AppBlast.this.importanceCode == 500){ // < 500 probably will never be the case
					
					if(DLog.i())
						DLog.i(LOG_TAG + " | Timer", "Process went to background");
					
					AppBlast.this.timer.cancel();
					
				}
			}
		};
		
		this.timer.scheduleAtFixedRate(tt, 2000, 5000);
	}
	
	
	/**
	 * Returns the importance code of current process.
	 * @return importance code
	 * @see {@link ActivityManager.RunningAppProcessInfo#importance} 
	 */
	public int checkProcessState() {
		ActivityManager am = (ActivityManager) AppBlast.application.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> list2 = am.getRunningAppProcesses();
		for (RunningAppProcessInfo ti : list2) {
			if(ti.processName.equals(AppBlast.application.getPackageName())){
				if(DLog.i())
					DLog.i(LOG_TAG + ".checkProcessState()", String.valueOf("PNAME=" + ti.processName + " IMPORTANCE=" + ti.importance ));
				return ti.importance;
			}
		}
		return 0;
	}
	
	
}
