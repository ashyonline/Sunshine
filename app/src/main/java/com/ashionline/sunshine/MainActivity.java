package com.ashionline.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    public static final int FORECAST_LOADER_ID = 1;
    public static final int DETAIL_LOADER_ID = 2;
    private static final String FORECASTFRAGMENT_TAG = "forecast_fragment_tag";
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "onCreate");
        super.onCreate(savedInstanceState);

        // initialize location to whatever is stored was settings
        location = Utility.getPreferredLocation(this);

        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TAG", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TAG", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("TAG", "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (location != Utility.getPreferredLocation(this)) {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            forecastFragment.onLocationChanged();
            location = Utility.getPreferredLocation(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("TAG", "onStart");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivity(startSettings);
                return true;
            case R.id.action_location:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String zipCode = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
                Uri.Builder uriBuilder = Uri.parse("geo:0,0?").buildUpon()
                        .appendQueryParameter("q", zipCode);

                showMap(uriBuilder.build());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}