package com.ugopiemontese.photoredlecce.utils;

import java.util.List;
import java.util.Locale;

import com.ugopiemontese.photoredlecce.MainActivity;
import com.ugopiemontese.photoredlecce.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;

public class RilevazionePhotoRed extends Service implements android.location.LocationListener {

	private NotificationManager notMan;
	private int NOTIFICATION = 10101;
	
	private LocationManager locationManager;
	private static final long MIN_TIME = 30000;
	private static final long MIN_DISTANCE = 500;
	private static final float MAX_DISTANCE = 5000;
	
	private TextToSpeech mTTS;
	
	List<PhotoRed> photoRedList;
	PhotoRedSQLiteHelper db;
			
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
    public void onCreate() {
		
        notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);     
        
        db = new PhotoRedSQLiteHelper(getBaseContext());
        photoRedList = db.getAllPhotoRed();
        
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
        return START_STICKY;
        
    }

    @Override
    public void onDestroy() {
    	
    	if ( mTTS != null ) {
    		
            mTTS.stop();
            mTTS.shutdown();
            
         }
    	
        notMan.cancel(NOTIFICATION);
        locationManager.removeUpdates(this);
        
    }
    
    private void showNotification() {
    	
    	CharSequence title = getText(R.string.app_name);
        CharSequence photored_found = getText(R.string.rilevazione_notification_photored);
        
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
	        .setContentTitle(title)
	        .setContentText(photored_found)
	        .setSmallIcon(R.drawable.ic_notification_photored)
	        .setWhen(System.currentTimeMillis());
        
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
    		this,
    		0,
    		resultIntent,
    		PendingIntent.FLAG_UPDATE_CURRENT
    	);
        notification.setContentIntent(resultPendingIntent);
        
        notMan.notify(NOTIFICATION, notification.build());
        
    }
    
    @Override
	public void onLocationChanged(Location arg0) {
		
    	boolean announced = false;
    	float[] distance = new float[1];
    	
    	for (int count = 0; count < photoRedList.size(); count++) {
    		
    		if ( !announced ) {
    		
	    		Location.distanceBetween(
					(Double) photoRedList.get(count).getLat(),
					photoRedList.get(count).getLng(),
					arg0.getLatitude(),
					arg0.getLongitude(),
					distance
	    		);

	        	if ( distance[0] <= MAX_DISTANCE ) {
	        		
	        		final String indirizzo = photoRedList.get(count).getAddress();
	        		mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
	        			
	                	@Override
	                    public void onInit(int arg0) {
	                    	
	                        if (arg0 == TextToSpeech.SUCCESS) {
	                        	
	                        	mTTS.setLanguage(Locale.ITALIAN);
	                        	mTTS.speak(getString(R.string.rilevazione_service_photored) + indirizzo, TextToSpeech.QUEUE_FLUSH, null);
	                        	
	                        }
	
	                    }
	                	
	                });
	        		
	        		showNotification();
	        		
	        		announced = true;

	        	}
    		
    		}
    		
    	}
    	
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
	}

}
