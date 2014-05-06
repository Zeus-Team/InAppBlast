package com.inappblast;

import android.text.TextUtils;
import static com.inappblast.AppBlast.LL_ALL;
import static com.inappblast.AppBlast.LL_ERROR;
import static com.inappblast.AppBlast.LL_NO;;

/**
 * DLog implements ERROR and INFO logging and provides adjustable logging level.
 * Use {@link DLog#setLogLevel(int))} to change logging level.
 * Default log level is {@link DLog#LL_NO}.
 * DLog implements and translates only ERROR and INFO logs 
 * to a standard {@link android.util.Log logger}. 
 * @author Maksym Fedyay
 */
class DLog {
	
	
	/**
	 * Current log level
	 */
	private static int logLevel = LL_NO; 
	
	
	
	/**
	 * Indicates that current log level permits info logs
	 */
	private static boolean isInfoEnabled = false;
	
	
	
	/**
	 * Indicates that current log level permits error logs
	 */
	private static boolean isErrorEnabled = false;
	
	
	
	/**
	 * Sets logging level.
	 * Pass one of the following constants to set appropriate logging level <br/>
	 * {@link DLog#LL_ALL} - enable all logs<br/>
	 * {@link DLog#LL_ERROR} - only error logs<br/>
	 * {@link DLog#LL_NO} - disable all logs<br/>
	 * @param logLevel
	 */
	public static void setLogLevel(int logLevel){
		DLog.logLevel = logLevel;
		DLog.isErrorEnabled = logLevel == LL_ERROR || logLevel == LL_ALL;
		DLog.isInfoEnabled = logLevel == LL_ALL;
	}

	
	
	/**
	 * Returns current logging level.
	 * There total three  
	 * @return
	 */
	public static int getLogLevel(){
		return DLog.logLevel;
	}
	
	
	
	
	/**
	 * Sends ERROR log message. 
	 * @param tag - Used to identify source of message
	 * @param msg - The message that will be logged
	 */
	public static void e(String tag, String msg){
		
		if(TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) return;
		
		android.util.Log.e(tag, msg);
	}
	
	
	/**
	 * Sends INFO log message. 
	 * @param tag - Used to identify source of message
	 * @param msg - The message that will be logged
	 */
	public static void i(String tag, String msg){

		if(TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) return;
		
		android.util.Log.i(tag, msg);
	}
	
	
	
	/**
	 * Returns true if current log level permits informational logging,
	 * false otherwise.
	 * @return boolean
	 */
	public static boolean i(){
		return DLog.isInfoEnabled;
	}
	
	
	
	/**
	 * Returns true id current log level permits error logging,
	 * false otherwise.
	 * @return boolean
	 */
	public static boolean e(){
		return DLog.isErrorEnabled;
	}
	
	

	
}
