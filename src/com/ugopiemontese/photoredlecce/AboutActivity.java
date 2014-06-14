package com.ugopiemontese.photoredlecce;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView textLink = (TextView) findViewById(R.id.textLink);
		textLink.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.credits_data_link)));
				startActivity(browserIntent);
			}
			
		});
		
		TextView textDevelopedBy = (TextView) findViewById(R.id.textDevelopedBy);
		textDevelopedBy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developed_by_link)));
				startActivity(browserIntent);
			}
			
		});
		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    int itemId = item.getItemId();
	    switch (itemId) {
	    case android.R.id.home:
	    	finish();
	        break;
	    }
	    return true;
	}
	
}
