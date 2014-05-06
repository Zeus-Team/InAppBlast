package com.inappblast;


/**
 * Interface definition for a callback to be invoked when notification panel
 * actions will take place.
 * @author Maksym Fedyay
 */
public interface OnBlastActionListener {
	
	/**
	 * Notification activity closed explicitly 
	 */
	public static final int ACTION_NEGATIVE = 666;
	
	
	/**
	 * User clicked on CTA button. Additional parameter passed
	 * the string that contains CTA URL
	 */
	public static final int ACTION_POSITIVE = 777;
	
	
	/**
	 * Called when notification panel produces actions. <br/>
	 * 
	 * {@link OnBlastActionListener#ACTION_POSITIVE} - user clicked on CTA button.<br/>
	 * {@link OnBlastActionListener#ACTION_NEGATIVE} - user clicked on close button.<br/>
	 * 
	 * It is recommended to use <code>switch</code> statement to 
	 * catch the current action.
	 * 
	 * Do not perform too much work in cases of {@link OnBlastActionListener#ACTION_BACK} or 
	 * {@link OnBlastActionListener#ACTION_CLOSE}, because notification Activity is about to close,
	 * so any kind of heavy work should be performed asynchronously.
	 * 
	 * @param action - code of action
	 * @param params - additional parameters
	 */
	void onBlastAction(int action, Object...params);

	
//	 * @param activity - activity of current notification 
//	 * (use this activity in case when necessary to launch other activity)
//	 * but do not store reference to this activity outside of this method
//	 * to prevent memory leakage.
	
}
