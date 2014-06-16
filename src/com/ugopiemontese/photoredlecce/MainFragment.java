package com.ugopiemontese.photoredlecce;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.ugopiemontese.photoredlecce.utils.PhotoRed;
import com.ugopiemontese.photoredlecce.utils.PhotoRedSQLiteHelper;
import com.ugopiemontese.photoredlecce.utils.RilevazionePhotoRed;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class MainFragment extends Fragment implements OnCameraChangeListener {

	public MainFragment() {	}

	private GoogleMap map;
	private MapFragment mapFragment;
	
	private Double lecce_lat = 40.3541307;
	private Double lecce_lng = 18.1742945;
	
	private final String TAG_LUOGO = "Luogo";
	private final String TAG_COORDINATE = "coordinates";
	
	private final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private AsyncTask<Void, Void, Boolean> TaskAsincrono = null;
	
	private Intent serviceIntent;
	private SharedPreferences prefs;
	private final String PREF_NOTIFICA = "notifica";
	private final String PREF_PRIMO_AVVIO = "primo_avvio";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
		if ( status == ConnectionResult.SUCCESS ) {
			
			InitMap();
			TaskAsincrono = new CaricamentoAsincrono().execute();
			
		} else {
			
 			GooglePlayServicesUtil.getErrorDialog(status, getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
 			
 		}
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.toggleButton);
		toggleButton.setChecked(prefs.getBoolean(PREF_NOTIFICA, false));
		
		serviceIntent = new Intent(getActivity(), RilevazionePhotoRed.class);
		
		if (prefs.getBoolean(PREF_NOTIFICA, false)) {
			
			getActivity().startService(serviceIntent);
			
		}
		
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PREF_NOTIFICA, arg1);
				editor.commit();
				
				LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
				boolean loc_check = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				if (arg1 && loc_check) {
					
					getActivity().startService(serviceIntent);
					
				} else {
										
					if ( !loc_check ) {
						
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				        alertDialogBuilder.setMessage(R.string.status_inactive_gps).setCancelable(false);
				        
				        alertDialogBuilder.setPositiveButton(R.string.action_attiva_gps, new DialogInterface.OnClickListener() {
				        	
				        	public void onClick(DialogInterface dialog, int id) {
				        		
				                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				                startActivity(callGPSSettingIntent);
				                
				            }
				        	
				        });
				        
				        alertDialogBuilder.setNegativeButton(R.string.action_annulla_gps, new DialogInterface.OnClickListener() {
				        	
				        	public void onClick(DialogInterface dialog, int id) {
				        		
				        		dialog.cancel();
				        		
				        	}
				        	
				        });

				        AlertDialog alert = alertDialogBuilder.create();
				        alert.show();
						
					}
					
					getActivity().stopService(serviceIntent);
					
					arg0.setChecked(false);
					
					editor.putBoolean(PREF_NOTIFICA, false);
					editor.commit();
					
				}
				
			}
			
		});
		
		return rootView;
		
	}
	
	@Override
	public void onDestroyView() {
		
		super.onDestroyView();
		  
		if(TaskAsincrono != null && TaskAsincrono.getStatus() != AsyncTask.Status.FINISHED) {
			
			TaskAsincrono.cancel(true);
			
		}
		
	}
	
	private void InitMap() {	
		
	    mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView));
	    map = mapFragment.getMap();
	    map.clear();
	    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    map.setMyLocationEnabled(false);
	    map.getUiSettings().setAllGesturesEnabled(false);

	    LatLng lecce = new LatLng(Double.valueOf(lecce_lat), Double.valueOf(lecce_lng));
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lecce, 12);
		if ( map != null ) {
			
		    map.animateCamera(cameraUpdate);
		    
		}
		
		map.setOnCameraChangeListener(this);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setAllGesturesEnabled(true);
	    
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		
		float max_distance = 8000; // 8Km
		float[] distance = new float[1];
		LatLng lecce = new LatLng(Double.valueOf(lecce_lat), Double.valueOf(lecce_lng));
		Location.distanceBetween(lecce_lat, lecce_lng, position.target.latitude, position.target.longitude, distance);
		
		if(distance[0] >= max_distance) {
			
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lecce, 12);
			if ( map != null ) {
				
			    map.animateCamera(cameraUpdate);
			    
			}
			
		}
		
	}
	
	public String loadJSONFromAsset() {
		
	    String json = null;
	    try {
	    	
	        InputStream is = getActivity().getAssets().open("photored.json");
	        int size = is.available();
	        byte[] buffer = new byte[size];
	        is.read(buffer);
	        is.close();
	        json = new String(buffer, "UTF-8");
	        
	    } catch (IOException ex) {
	    	
	        ex.printStackTrace();
	        return null;
	        
	    }
	    
	    return json;

	}
	
	public class CaricamentoAsincrono extends AsyncTask<Void, Void, Boolean> {
		
		ArrayList<Object[]> photoredList = new ArrayList<Object[]>();
		PhotoRedSQLiteHelper db = new PhotoRedSQLiteHelper(getActivity());
		
		@Override
		protected void onPreExecute() {
			
			if ( map != null ) {
				
				map.clear();
				
			}
			
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
						
			try {
				
				JSONArray photoreds = new JSONArray(loadJSONFromAsset());
				
				for (int i = 0; i < photoreds.length(); i++) {
					
					JSONObject red = photoreds.getJSONObject(i);
		
					String address = red.getString(TAG_LUOGO); 
					JSONArray coordinates = red.getJSONArray(TAG_COORDINATE);	
					LatLng point = new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
					
					Object[] photored = new Object[] { address, point };
					photoredList.add(photored);
					
					if ( prefs.getBoolean(PREF_PRIMO_AVVIO, true) ) {
						
						db.addPhotoRed(new PhotoRed(
		                		(String) address, 
		                		(Double) point.latitude, 
		                		(Double) point.longitude
		                ));
						
					}
					
				}
				
			} catch (JSONException e) {
				
				e.printStackTrace();
				
			}
			
			return true;
			
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			
			if ( isCancelled() ) {
				
				return;
				
			}
			
			if ( prefs.getBoolean(PREF_PRIMO_AVVIO, true) ) {
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PREF_PRIMO_AVVIO, false);
				editor.commit();
				
			}
			
			for (int count = 0; count < photoredList.size(); count++) {	
				
				@SuppressWarnings("unused")
				Marker mrk = map.addMarker(new MarkerOptions()
					.title((String) photoredList.get(count)[0])
		    		.position((LatLng) photoredList.get(count)[1])
		    		.icon(BitmapDescriptorFactory.fromResource(R.drawable.photored))
		    		.anchor(0.5f, 0.5f)
		    	);
				
			}
			
		}
		
	}
	
}