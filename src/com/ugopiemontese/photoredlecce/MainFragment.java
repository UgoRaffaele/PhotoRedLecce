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

import android.app.Fragment;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
		
		return rootView;
		
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
		
		@Override
		protected void onPreExecute() {
			if ( map != null ) {
				map.clear();
			}
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONArray photoreds = null;
			
			try {
				
				photoreds = new JSONArray(loadJSONFromAsset());
				
				for (int i = 0; i < photoreds.length(); i++) {
					JSONObject red = photoreds.getJSONObject(i);
		
					String address = red.getString(TAG_LUOGO); 
					JSONArray coordinates = red.getJSONArray(TAG_COORDINATE);	
					LatLng point = new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
					
					Object[] photored = new Object[] { address, point };
					photoredList.add(photored);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
			
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			
			if ( isCancelled() ) {
				return;
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