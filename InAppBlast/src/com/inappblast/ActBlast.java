package com.inappblast;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity that is shows notification.
 * There are two actions that user can perform:
 * - Close notification (or back press)
 * - Press CTA Button
 * Those action will be notified if {@link OnBlastActionListener}
 * were implemented by {@link Application} class
 * @author Maksym Fedyay
 */
public class ActBlast extends Activity implements OnClickListener{
	
	
	/*
	 * Views
	 */
	private ImageView bg;
	private Button actionButton;
	private ImageButton closeButton;
	private TextView title;
	private TextView content;
	private LinearLayout overlay;
	
	
	/*
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.act_blast);
		
		bg = (ImageView) findViewById(R.id.blast_bg);
		
		actionButton = (Button) findViewById(R.id.blast_btn_action);
		actionButton.setOnClickListener(this);
		
		closeButton = (ImageButton) findViewById(R.id.blast_btn_close);
		closeButton.setOnClickListener(this);
		
		title = (TextView) findViewById(R.id.blast_title);
		content = (TextView) findViewById(R.id.blast_content);
		overlay = (LinearLayout) findViewById(R.id.blast_overlay);
		
		BlastConfig config = AppBlast.getSharedInstance().getConfig();
		
		if(config != null){
			
			BitmapDrawable[] bitmaps = config.getBitmaps();
			
			String title = config.getTitle();
			String content = config.getDescription();
			String cta = config.getCta();
			
			int ctaColor = config.getCtaColor();
			int ctaColorHighlight = config.getCtaColorHighlighted();
			
			if(bitmaps[0] != null)
				this.closeButton.setImageDrawable(bitmaps[0]);
			if(bitmaps[1] != null)
				setBackground(this.overlay, bitmaps[1]);
			if(bitmaps[2] != null)
				this.bg.setImageDrawable(bitmaps[2]);
			
			this.title.setText(title);
			
			this.content.setText(content);
			
			this.actionButton.setText(cta);
			
			ShapeDrawable rectPressed = new ShapeDrawable(new RectShape());
			rectPressed.getPaint().setColor(ctaColorHighlight);
			
			ShapeDrawable rectRegular = new ShapeDrawable(new RectShape());
			rectRegular.getPaint().setColor(ctaColor);
			
			StateListDrawable stateList = new StateListDrawable();
			stateList.addState(new int[] {android.R.attr.state_pressed}, rectPressed);
			stateList.addState(new int[] {}, rectRegular);
			
			setBackground(this.actionButton, stateList);
			
		}
		
	}
	
	
	
	/**
	 * This method different methods of setting
	 * background, depends on environment API level
	 * @param v - View where background need to be set
	 * @param d - {@link Drawable} as the background 
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackground(View v, Drawable d){
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			v.setBackground(d);
		} else {
			v.setBackgroundDrawable(d);
		}
	}
	
	
	
	/**
	 * Indicates time elapsed from notification configuration 
	 * being loaded to notification activity became visible to user.
	 */
	private long delta1;
	
	
	
	/**
	 * Indicates time elapsed from notification activity 
	 * being visible to user to became invisible to user  
	 */
	private long delta2;
	
	
	
	/*
	 * 
	 */
	@Override
	protected void onStart() {
		super.onResume();
		this.delta1 = AppBlast.getSharedInstance().getConfig().getDelta();
		this.delta2 = new Date().getTime();
	}
	
	
	
	/*
	 * =============================================
	 * Notification activity registration/unregistration 
	 * may be duplicated if application activity 
	 * registration will goes thru 
	 * Application.ActivityLifecycleCallbacks 
	 */
	
	
	/*
	 * We should register this activity as blast point 
	 * to preserve library consistency.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		AppBlast.getSharedInstance().setBlastPoint(this);
	}
	
	
	/*
	 * We should unregister this activity as blast point 
	 * to preserve library consistency 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		AppBlast.getSharedInstance().setBlastPoint(null);
	}
	/*=============================================*/
	

	
	/**
	 * Report notification basic statistic
	 * @param action - code of action 1-positive 0-negative
	 */
	private void reportStats(int action){
		this.delta2 = new Date().getTime() - this.delta2;
		AppBlast.getSharedInstance().postUsageInfo(delta1, delta2, action);
	}
	
	
	
	/*
	 * 
	 */
	@Override
	public void onClick(View v) {
		
		int id = v.getId();
		
		if(id == R.id.blast_btn_action){
			
			reportStats(1);
			
			AppBlast.getSharedInstance()
				.notifyApplication(
						OnBlastActionListener.ACTION_POSITIVE, 
						AppBlast.getSharedInstance().getConfig().getCtaUrl());
			
			this.finish();
			
		} else if(id == R.id.blast_btn_close){
			
			reportStats(0);
			AppBlast.getSharedInstance().notifyApplication(OnBlastActionListener.ACTION_NEGATIVE);
			this.finish();
			
		}
	}
	
	
	@Override
	public void onBackPressed() {
		reportStats(0);
		AppBlast.getSharedInstance().notifyApplication(OnBlastActionListener.ACTION_NEGATIVE);
		super.onBackPressed();
	}
	
	
}
