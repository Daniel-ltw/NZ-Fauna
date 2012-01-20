package com.kiwipedia.nzfauna;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Display extends Activity implements OnTouchListener, OnClickListener{

	private AssetManager assets; 
	private AssetFileDescriptor sound, speak; 
	private MediaPlayer player = null; 
	private Activity act; 
	private String id, 
	notfound = "<html><body><h1 style=\"text-align:center;\">The internet " +
			"connection appears to be offline</h1></body></html>", 
	ytPlayer = "<html><body><iframe class=\"youtube-player\" type=\"text/html\" " +
					"width=\"300\" height=\"200\" src=\"videoURL\" " +
					"frameborder=\"1\"></iframe></body></html>", 
	noPlayer = "<html><body><h1 style=\"text-align:center;\">You do not seem " +
			"to have Adobe Flash Player installed on your device. </h1></body></html>"; 

	SharedPreferences nzfauna; 
	private boolean imContainer = true; // true for open and false for collapse
	private NetworkInfo info;
	private TelephonyManager telMgr; 
	private ConnectivityManager connMgr; 
	private LayoutInflater layoutIn; 
	private float downXValue, downYValue;
	private ViewFlipper flipper; 
	private RelativeLayout displayFrame; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.display); 

		this.act = this; 

		connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); 
		telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); 
		info = connMgr.getActiveNetworkInfo(); 
		assets = getApplicationContext().getAssets(); 
		nzfauna = getApplicationContext().getSharedPreferences("nzfauna", 
				MODE_PRIVATE); 
		layoutIn = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE); 
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper); 

		//  prepare layout, views and handlers
		layoutIn.inflate(R.layout.displayframe, flipper); 
		displayFrame = (RelativeLayout) findViewById(R.id.displayFrame); 

		setup(); 
	}

	@Override
	protected void onResume() {
		super.onResume(); 
		textShow(); 
	}

	private void setup() {

		imContainer = true; 
		layoutIn.inflate(R.layout.displayoptionbar, displayFrame); 


		id = "000"; 
		if(nzfauna != null) {
			id = nzfauna.getString("id", "000"); 
		} else {
			Log.e("Display.java", "nzfauna = null"); 
		}

		if(id.equalsIgnoreCase("000")) {
			Log.e("Display.java", "id == 000"); 
		} else {
			//Log.w("Display.java", "id = " + id); 
		}

		try {
			sound = assets.openFd("sounds/" + id + ".mp3");
		} catch (IOException e1) {
			sound = null; 
		} 

		try {
			speak = assets.openFd("speak/s" + id + ".mp3");
		} catch (IOException e1) {
			speak = null; 
		}

		TextView animal = (TextView) findViewById(R.id.animal); 
		animal.setText(nzfauna.getString("name", "Can't find name detail. ")); 
		animal.setOnClickListener(this); 

		ImageView imv = (ImageView) findViewById(R.id.imView); 
		try {
			InputStream is = assets.open("animals/" + id + ".jpg"); 
			Bitmap image = BitmapFactory.decodeStream(is); 
			if(image == null) {
				Log.e("Display.java", "image = null"); 
			} else {
				imv.setImageBitmap(image); 
			}
			WebView wv = (WebView) findViewById(R.id.webView1); 
			wv.loadUrl("file:///android_asset/html/" + id + ".html"); 
			is.close(); 
		} catch (IOException e) {

		}
		InputStream is = null; 
		Bitmap image; 

		ImageView sourceBtn = (ImageView) findViewById(R.id.sourceBtn); 
		sourceBtn.setOnClickListener(this); 
		try {
			is = assets.open("images/info-icon.png");
			image = BitmapFactory.decodeStream(is); 
			sourceBtn.setImageBitmap(image); 
		} catch (IOException e1) {
			Log.e("Display.java", "Info icon problem", e1); 
		}

		Button sourceDetail = (Button) findViewById(R.id.sourceDetail); 
		sourceDetail.setOnClickListener(this); 


		// setup buttons for option bar
		LinearLayout optionBar = (LinearLayout) findViewById(R.id.optionBar); 
		optionBar.bringToFront(); 

		// prep video button
		try {
			ImageView videoBtn = (ImageView) findViewById(R.id.videoBtn); 
			optionBar.bringChildToFront(videoBtn); 
			videoBtn.setOnClickListener(this); 
			is = assets.open("images/video.png");
			image = BitmapFactory.decodeStream(is); 
			videoBtn.setImageBitmap(image); 
		} catch (IOException e) {

		}
		// prep sound button
		try {
			ImageView soundBtn = (ImageView) findViewById(R.id.soundBtn); 
			optionBar.bringChildToFront(soundBtn); 
			ImageView soundSelectedBtn = (ImageView) findViewById(R.id.soundSelectedBtn); 
			optionBar.bringChildToFront(soundSelectedBtn); 
			if(sound == null) {
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.soundLayout); 
				rl.removeAllViews(); 
				optionBar.removeView(rl); 
				optionBar.invalidate(); 
			} else {
				soundBtn.setOnClickListener(this); 
				is = assets.open("images/sound.png");
				image = BitmapFactory.decodeStream(is); 
				soundBtn.setImageBitmap(image); 

				soundSelectedBtn.setOnClickListener(this); 
				is = assets.open("images/sound_selected.png");
				image = BitmapFactory.decodeStream(is); 
				soundSelectedBtn.setImageBitmap(image); 
			}
		} catch (IOException e) {

		}
		// prep speak button
		try { 
			ImageView speakBtn = (ImageView) findViewById(R.id.speakBtn); 
			optionBar.bringChildToFront(speakBtn); 
			ImageView speakSelectedBtn = (ImageView) findViewById(R.id.speakSelectedBtn); 
			optionBar.bringChildToFront(speakSelectedBtn); 
			if(speak == null) {
				optionBar.removeView(speakBtn); 
				optionBar.removeView(speakSelectedBtn); 
				optionBar.invalidate(); 
			} else {
				speakBtn.setOnClickListener(this); 
				is = assets.open("images/speak.png");
				image = BitmapFactory.decodeStream(is); 
				speakBtn.setImageBitmap(image); 

				speakSelectedBtn.setOnClickListener(this); 
				is = assets.open("images/speak_selected.png");
				image = BitmapFactory.decodeStream(is); 
				speakSelectedBtn.setImageBitmap(image); 
			}
		} catch (IOException e) {

		}
		/*// prep record button
		try { 
			ImageView recordBtn = (ImageView) findViewById(R.id.recordBtn); 
			optionBar.bringChildToFront(recordBtn); 
			recordBtn.setOnClickListener(this); 
			is = assets.open("images/record.png");
			image = BitmapFactory.decodeStream(is); 
			recordBtn.setImageBitmap(image); 
		} catch (IOException e) {

		}*/
		// prep back button
		try { 
			ImageView backBtn = (ImageView) findViewById(R.id.backBtn); 
			optionBar.bringChildToFront(backBtn); 
			backBtn.setOnClickListener(this); 
			is = assets.open("images/arrowLeftGrey.png"); 
			image = BitmapFactory.decodeStream(is); 
			backBtn.setImageBitmap(image); 
		} catch (IOException e) {

		}
		try {
			is.close(); 
		} catch (IOException e) {
		} 

		ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper); 
		vf.setOnTouchListener(this); 

		displayFrame.invalidate(); 
	}

	private boolean checkConn() {
		int netType = -1, netSubtype = -1; 
		try{
			netType = info.getType(); 
			netSubtype = info.getSubtype(); 
		} catch (NullPointerException e) {}
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return info.isConnected(); 
		} else if (netType == ConnectivityManager.TYPE_MOBILE
				&& netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
				&& telMgr.getDataState() == TelephonyManager.DATA_CONNECTED) {
			return info.isConnected(); 
		} else {
			return false; 
		}
	}

	// Flip related methods below
	public boolean onTouch(View v, MotionEvent event) {

		long downTime = event.getDownTime(); 		
		long upTime = event.getEventTime(); 

		// Get the action that was done on this touch event
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// store the X value when the user's finger was pressed down
			downXValue = event.getRawX();
			// store the Y value when the user's finger was pressed down
			downYValue = event.getRawY();
			break;

		case MotionEvent.ACTION_UP:
			// Get the X value when the user released his/her finger
			float currentX = event.getRawX(); 
			// Get the Y value when the user released his/her finger
			float currentY = event.getRawY();  

			// time diff from down to up
			long timeDiff = ( upTime - downTime ); 

			// calculate absolute of X & Y and compare them
			float x = Math.abs(( currentX - downXValue )); 
			float y = Math.abs(( currentY - downYValue )); 

			if(timeDiff <= 500) {
				onClick(v); 
			}


			if(x >= y) {
				// going backwards: pushing stuff to the right
				if(downXValue < currentX) {
					// Get a reference to the ViewFlipper
					ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper); 
					// Set the animation
					flipper.setInAnimation(inFromLeftAnimation()); 
					flipper.setOutAnimation(outToRightAnimation()); 
					// Flip!
					int previous = nzfauna.getInt("previous", -1); 
					grabNext(previous); 
				}

				// going forwards: pushing stuff to the left
				if(downXValue > currentX) {
					// Get a reference to the ViewFlipper
					ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper); 
					// Set the animation
					flipper.setInAnimation(inFromRightAnimation()); 
					flipper.setOutAnimation(outToLeftAnimation()); 
					// Flip!
					int next = nzfauna.getInt("next", -1); 
					grabNext(next); 
				}
			} else if(y > x) {
				// swipe up or down detected
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.imageContainer); 
				int halfHeight = (rl.getMeasuredHeight() / 2) + 50; 
				RelativeLayout.LayoutParams rlLP = (LayoutParams) rl.getLayoutParams(); 
				WebView wv = (WebView) findViewById(R.id.webView1); 
				if((downYValue > currentY) && imContainer) {
					// adjust the offset for image container
					rlLP.topMargin = -halfHeight; 
					rl.requestLayout(); 

					// adjust the height for webview
					RelativeLayout.LayoutParams lp = new 
							RelativeLayout.LayoutParams(wv.getMeasuredWidth(), 
									(wv.getMeasuredHeight() + halfHeight)); 
					lp.addRule(RelativeLayout.ABOVE, R.id.optionBar); 
					lp.addRule(RelativeLayout.BELOW, R.id.imageContainer); 
					displayFrame.updateViewLayout(wv, lp); 
					imContainer = false; 
				} else if((downYValue < currentY) && !imContainer) {
					// adjust the offset for image container
					rlLP.topMargin = 0; 
					rl.requestLayout(); 

					// adjust the height for webview
					RelativeLayout.LayoutParams lp = new 
							RelativeLayout.LayoutParams(wv.getMeasuredWidth(), 
									(wv.getMeasuredHeight() - halfHeight)); 
					lp.addRule(RelativeLayout.ABOVE, R.id.optionBar); 
					lp.addRule(RelativeLayout.BELOW, R.id.imageContainer); 
					displayFrame.updateViewLayout(wv, lp); 
					imContainer = true; 
				}
				displayFrame.invalidate(); 
			}
			break;
		}

		// if you return false, these actions will not be recorded
		return true;
	}

	private void grabNext(int i) {
		if(i != -1) {
			String[] item = NZFaunaActivity.list.get(i); 
			nzfauna.edit().putString("id", item[0]).commit(); 
			nzfauna.edit().putString("name", item[1]).commit(); 
			nzfauna.edit().putString("source", item[2]).commit(); 
			nzfauna.edit().putString("video1", item[4]).commit(); 
			nzfauna.edit().putString("video2", item[5]).commit(); 
			nzfauna.edit().putString("video3", item[6]).commit(); 
			String key = nzfauna.getString("key", ""); 
			int minus = -1, plus = -1; 
			if(key.equalsIgnoreCase("name")) {
				try{
					minus = NZFaunaActivity.idNameMap.get(i - 1); 
				} catch(Exception e) {
					minus = -1; 
				}
				try{
					plus = NZFaunaActivity.idNameMap.get(i + 1); 
				} catch(Exception e) {
					plus = -1; 
				}
			} else if(key.equalsIgnoreCase("species")) {
				int row = NZFaunaActivity.speciesHashMap.get(item[0]); 
				try{
					minus = NZFaunaActivity.idSpeciesMap.get(row - 1); 
				} catch(Exception e) {
					minus = -1; 
				}
				try{
					plus = NZFaunaActivity.idSpeciesMap.get(row + 1); 
				} catch(Exception e) {
					plus = -1; 
				}
			}
			// If and else for previous
			if(minus != -1) {
				nzfauna.edit().putInt("previous", minus).commit(); 
			} else {
				nzfauna.edit().putInt("previous", 0).commit(); 
			}
			// If and else for next
			if(plus != -1) {
				nzfauna.edit().putInt("next", plus).commit(); 
			} else if(key.equalsIgnoreCase("name")){ 
				nzfauna.edit().putInt("next", 
						NZFaunaActivity.idNameMap.get((
								NZFaunaActivity.idNameMap.size()
								- 1))).commit(); 
			} else if(key.equalsIgnoreCase("species")){ 
				nzfauna.edit().putInt("next", 
						NZFaunaActivity.idSpeciesMap.get((
								NZFaunaActivity.idSpeciesMap.size() 
								- 1))).commit(); 
			}
			flipper.removeAllViews(); 
			flipper.invalidate(); 

			layoutIn.inflate(R.layout.displayframe, flipper); 
			displayFrame = (RelativeLayout) findViewById(R.id.displayFrame); 
			setup(); 
			flipper.showNext(); 
			textShow(); 
		}
	}

	private Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f); 
		inFromRight.setDuration(500); 
		inFromRight.setInterpolator(new AccelerateInterpolator()); 
		return inFromRight; 
	}

	private Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f); 
		outtoLeft.setDuration(500); 
		outtoLeft.setInterpolator(new AccelerateInterpolator()); 
		return outtoLeft; 
	}

	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f); 
		inFromLeft.setDuration(500); 
		inFromLeft.setInterpolator(new AccelerateInterpolator()); 
		return inFromLeft; 
	}

	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f); 
		outtoRight.setDuration(500); 
		outtoRight.setInterpolator(new AccelerateInterpolator()); 
		return outtoRight; 
	}

	// Implementation of onClickListener
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.videoBtn: 
			layoutIn.inflate(R.layout.displayvideo, displayFrame); 
			final RelativeLayout vidFrame = (RelativeLayout) findViewById(R.id.vidFrame); 
			vidFrame.bringToFront(); 
			final WebView vidView = (WebView) findViewById(R.id.vidView); 
			Button closeBtn = (Button) findViewById(R.id.closeBtn); 
			// play video
			vidView.getSettings().setJavaScriptEnabled(true); 
			vidView.getSettings().setPluginsEnabled(true); 
			vidView.getSettings().setPluginState(PluginState.ON); 
			vidView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL); 
			String url = nzfauna.getString("video1", null); 
			// TODO
			// Check for adobe flash
			boolean flashInstalled = false;
			try {
			  PackageManager pm = getPackageManager();
			  ApplicationInfo ai = pm.getApplicationInfo("com.adobe.flashplayer", 0);
			  if (ai != null)
			    flashInstalled = true;
			} catch (NameNotFoundException e) {
			  flashInstalled = false;
			}
			// Select the proper html code for the embedded video
			url = url.replace("watch?v=", "embed/"); 
			String video = ytPlayer.replace("videoURL", url + 
						"?fmt=6"); 
			if(!flashInstalled) vidView.loadData(noPlayer, "text/html", "UTF-8"); 
			else if(checkConn()) vidView.loadData(video, "text/html", "UTF-8"); 
			else {
				vidView.loadData(notfound, "text/html", "UTF-8"); 
			}
			closeBtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					displayFrame.removeView(vidFrame); 
					vidView.destroy(); 
				}
			}); 

			break; 
		case R.id.soundBtn: 
			if(player != null) { 
				if(player.isPlaying()) {
					player.seekTo(player.getDuration()); 
					player.stop(); 
					try {
						Thread.sleep(1000); 
					} catch (InterruptedException e) {
					} 
				}
				player.release(); 
			}
			player = new MediaPlayer(); 
			try {
				player.setDataSource(sound.getFileDescriptor(), 
						sound.getStartOffset(), sound.getLength());
				player.prepare(); 
			} catch (Exception e) {
				Toast t = Toast.makeText(getBaseContext(), 
						"Does not contain a sound file. ", Toast.LENGTH_SHORT); 
				t.show(); 
				Log.e("Display.java", e.getMessage()); 
			} 
			player.start(); 
			soundSpeak(R.id.soundSelectedBtn); 
			break; 
		case R.id.speakBtn: 
			if(player != null) {
				if(player.isPlaying()) {
					player.seekTo(player.getDuration()); 
					player.stop(); 
					try {
						Thread.sleep(1000); 
					} catch (InterruptedException e) {
					} 
				}
				player.release(); 
			}
			player = new MediaPlayer(); 
			try {
				player.setDataSource(speak.getFileDescriptor(), 
						speak.getStartOffset(), speak.getLength());
				player.prepare(); 
			} catch (Exception e) {
				Toast t = Toast.makeText(getBaseContext(), 
						"Does not contain a speak file. ", Toast.LENGTH_SHORT); 
				t.show(); 
				Log.e("Display.java", e.getMessage()); 
			} 
			player.start(); 
			textShow(); 
			soundSpeak(R.id.speakSelectedBtn); 
			break; 
			/*case R.id.recordBtn: 
			// AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
			// Implement recording functions for users
			// AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
			// TODO
			break; */
		case R.id.backBtn: 
			if(player != null) {
				if(player.isPlaying()) {
					player.seekTo(player.getDuration()); 
					player.stop(); 
					try {
						Thread.sleep(1000); 
					} catch (InterruptedException e) {
					} 
				}
				player.release(); 
			}
			this.finish(); 
			break; 
		case R.id.sourceBtn: 
			v.setVisibility(View.GONE); 
			Button sdbtn = (Button) findViewById(R.id.sourceDetail); 
			sdbtn.setText(nzfauna.getString("source", "Can't find source" +
					" detail. ")); 
			sdbtn.setVisibility(View.VISIBLE); 
			break; 
		case R.id.sourceDetail: 
			v.setVisibility(View.GONE); 
			ImageView sbtn = (ImageView) findViewById(R.id.sourceBtn); 
			sbtn.setVisibility(View.VISIBLE); 
			break; 
		}
	}

	// called to run sound or speak thread
	private void soundSpeak(final int viewID) {
		new Thread(new Runnable() {

			public void run() {
				act.runOnUiThread(new Runnable() {

					public void run() {
						ImageView imv = (ImageView) findViewById(viewID); 
						imv.setVisibility(View.VISIBLE); 
						imv.bringToFront(); 
						displayFrame.invalidate(); 
					}
				}); 
				while(player.isPlaying()) {
				}
				act.runOnUiThread(new Runnable() {

					public void run() {
						ImageView imv = (ImageView) findViewById(viewID); 
						imv.setVisibility(View.GONE); 
						displayFrame.invalidate(); 
					}
				}); 
			}
		}).start(); 
	}

	// called to display the name of the animal on display
	private void textShow() {
		new Thread(new Runnable() {

			public void run() {
				final TextView animal = (TextView) findViewById(R.id.animal); 
				act.runOnUiThread(new Runnable() {
					public void run() {
						animal.setVisibility(View.VISIBLE); 
					}
				}); 
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
				} 
				act.runOnUiThread(new Runnable() {
					public void run() {
						animal.setVisibility(View.GONE); 
					}
				}); 
			}
		}).start(); 
	}
}