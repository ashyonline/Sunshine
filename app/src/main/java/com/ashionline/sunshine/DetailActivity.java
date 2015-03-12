package com.ashionline.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.ShareActionProvider;

import com.ashionline.sunshine.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String SUNSHINE_HASHTAG = "#Sunshine";
        private static final String LOG_TAG = DetailFragment.class.toString();
        private String forecastStr;
        private TextView detail;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment_menu, menu);
            MenuItem shareItem = menu.findItem(R.id.action_share);
            ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            if (provider != null) {
                provider.setShareIntent(prepareShareIntent());
            } else {
                Log.d(LOG_TAG, "Share action provider seems to be null");
            }
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(MainActivity.DETAIL_LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        private Intent prepareShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastStr + " " + SUNSHINE_HASHTAG);
            return shareIntent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                forecastStr = intent.getDataString();
            }

            detail = (TextView) rootView.findViewById(R.id.detail);

            return rootView;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), Uri.parse(forecastStr), ForecastFragment.FORECAST_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (detail != null)
                detail.setText(convertCursorRowToUXFormat(cursor));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            boolean isMetric = Utility.isMetric(this.getActivity());
            String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
            return highLowStr;
        }

        /*
            This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
            string.
         */
        private String convertCursorRowToUXFormat(Cursor cursor) {
            cursor.moveToFirst();
            String highAndLow = formatHighLows(
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

            return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                    " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                    " - " + highAndLow;
        }
    }
}
