package com.kiwipedia.nzfauna;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class Splash extends Activity {

	public static ProgressDialog pDialog; 
	public static Handler pHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pDialog.dismiss();
		}
	}; 

	private boolean _active = true;
	private int _splashTime = 5000; // time to display the splash screen in ms
	private AssetFileDescriptor sound; 
	private AssetManager assets; 
	private MediaPlayer player; 
	private Context mContext; 
	private Activity mActivity; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.splash); 

		mContext = this;
		mActivity = this; 
		
		player = new MediaPlayer(); 

		ImageView imv = (ImageView) findViewById(R.id.splashImage); 
		assets = getAssets(); 
		try {
			InputStream is = assets.open("images/launch_iphone.png"); 
			Bitmap image = BitmapFactory.decodeStream(is); 
			imv.setImageBitmap(image); 
			is.close(); 
		} catch (Exception e) {
			Log.e("Splash.java", "image file error", e); 
		} 

		try {
			sound = assets.openFd("sounds/046.mp3");
		} catch (IOException e) {
			Log.e("Splash.java", "open FD error", e);   
		} 
	}

	@Override
	protected void onStart() {
		super.onStart();

		// thread for displaying the SplashScreen
		Thread splashTread = new Thread() {
			@Override
			public void run() {
				try {
					player.setDataSource(sound.getFileDescriptor(), 
							sound.getStartOffset(), sound.getLength());
				} catch (IllegalArgumentException e2) {
					Log.e("Splash.java", "set Data Source IAException", e2); 
				} catch (IllegalStateException e2) {
					Log.e("Splash.java", "set Data Source ISException", e2); 
				} catch (IOException e2) {
					Log.e("Splash.java", "set Data Source IOException", e2); 
				} 
				try {
					player.prepare();
				} catch (IllegalStateException e1) {
					Log.e("Splash.java", "player prepare ISException", e1); 
				} catch (IOException e1) {
					Log.e("Splash.java", "player prepare IOException", e1); 
				} 
				player.start(); 
				try {
					int waited = 0;
					while(_active && (waited < _splashTime)) {
						sleep(100);
						if(_active) {
							waited += 100;
						}
					}
				} catch(InterruptedException e) {
					// do nothing
				} finally {
					player.release(); 
					finish();
					mActivity.runOnUiThread(new Runnable() {
						
						public void run() {
							pDialog = ProgressDialog.show(mContext, "", "Loading..."); 
						}
					}); 
					startActivity(new Intent(getBaseContext(), 
							NZFaunaActivity.class));
				}
			}
		};
		splashTread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			_active = false;
		}
		return true;
	}
}
