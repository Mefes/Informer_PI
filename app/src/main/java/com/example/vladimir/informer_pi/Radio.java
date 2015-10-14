package com.example.vladimir.informer_pi;

import java.io.IOException;


import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;



public class Radio extends Service{
    public MediaPlayer MPlayer;
    public static final String ACTION_PLAY = "ru.ktomsp.pi.sfukras.informer.action.PLAY";
    public static final String ACTION_PAUSE = "ru.ktomsp.pi.sfukras.informer.action.PAUSE";
//    public void onStartCommand(Intent intent) {
//        String action = intent.getAction();
//        if (action.equals(ACTION_PLAY)||action.equals(ACTION_PAUSE))
//        		{playpause();}
//        }
        
    @Override    
	public void onCreate() {
   	super.onCreate();

    }
		
       public void playpause(){ 
    
    	   try{	   createifyouneedit();
    	   if (MPlayer!=null && MPlayer.isPlaying()
    			   ) {
    		   MPlayer.stop();
               return;
    		   }

    	   else
	{
		MPlayer.prepareAsync();
		   MPlayer.setOnPreparedListener(new OnPreparedListener() {

	            public void onPrepared(MediaPlayer mp) {
	                MPlayer.start();
	                return;
		
	}
	        });   
	}}catch (Exception e){
		Log.d("onCreate",""+e);	
		MPlayer.release();
        return;

		};

	}

       @Override
       public void onDestroy() {
    	   super.onDestroy();
    	     stopForeground(true);
    	        if (MPlayer != null) {
    	            MPlayer.reset();
    	            MPlayer.release();
    	            MPlayer = null;
    	        }
       }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void createifyouneedit(){
		if (MPlayer==null){
	        MPlayer = new MediaPlayer();
	        try
	        {
	        	 MPlayer.setDataSource(getApplicationContext(),Uri.parse("http://193.218.136.87:8000/128kbit"));
	        	 MPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        }
	        catch (IllegalArgumentException except){
	        	except.printStackTrace();	
	        }	
	        
	        catch (IllegalStateException except){
	        	except.printStackTrace();	
	        }	
	        
	        catch (IOException except){
	        except.printStackTrace();	}
	}
	 	 
	}
	public int onStartCommand(Intent intent, int flags, int startId) {

			playpause();
	    return START_NOT_STICKY;
	  }
	
}

