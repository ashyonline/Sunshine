package com.ashionline.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ashionline.sunshine.data.WeatherContract;
import com.ashionline.sunshine.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements Callback {
    public static final int FORECAST_LOADER_ID = 1;
    public static final int DETAIL_LOADER_ID = 2;
    private static final String DETAIL_FRAGMENT_TAG = "detail_fragment_tag";
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "onCreate");
        super.onCreate(savedInstanceState);

        // initialize mLocation to whatever is stored was settings
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.main_activity);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
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
        if (mLocation != Utility.getPreferredLocation(this)) {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            forecastFragment.onLocationChanged();

            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onLocationChanged(mLocation);
            }
            mLocation = Utility.getPreferredLocation(this);
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

    protected DetailFragment newDetailFragment(Uri uri) {
        DetailFragment detailFragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putParcelable(DetailFragment.DETAIL_URI, uri);
        detailFragment.setArguments(args);
        return detailFragment;
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.weather_detail_container, newDetailFragment(dateUri));
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            String locationSetting = Utility.getPreferredLocation(this);
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, WeatherContract.WeatherEntry.getDateFromUri(dateUri)
                    ));
            startActivity(intent);
        }
    }
}