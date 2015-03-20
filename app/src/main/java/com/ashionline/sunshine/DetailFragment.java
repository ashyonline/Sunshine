package com.ashionline.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashionline.sunshine.data.WeatherContract;

/**
 * Created by ashi on 3/14/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_CONDITION_ID = 8;
    static final int COL_WEATHER_DEGREES = 9;


    private static final String SUNSHINE_HASHTAG = "#Sunshine";
    private static final String LOG_TAG = DetailFragment.class.toString();
    public static final String DETAIL_URI = "uri";
    private String forecastStr;

    private ShareActionProvider shareProvider;
    private TextView day;
    private TextView date;
    private TextView description;
    private TextView highTemp;
    private TextView lowTemp;
    private ImageView icon;
    private TextView humidity;
    private TextView wind;
    private TextView pressure;
    private Uri uri;
    private WindControl mWindControl;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if (forecastStr != null) {
            shareProvider.setShareIntent(prepareShareIntent());
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        day = (TextView) rootView.findViewById(R.id.day);
        date = (TextView) rootView.findViewById(R.id.date);
        description = (TextView) rootView.findViewById(R.id.description);
        highTemp = (TextView) rootView.findViewById(R.id.high_temp);
        lowTemp = (TextView) rootView.findViewById(R.id.low_temp);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        humidity = (TextView) rootView.findViewById(R.id.humidity);
        wind = (TextView) rootView.findViewById(R.id.wind);
        pressure = (TextView) rootView.findViewById(R.id.pressure);
        mWindControl = (WindControl) rootView.findViewById(R.id.wind_control);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != uri) {
// Now create and return a CursorLoader that will take care of
// creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return;
        }

        long dateInMillis = cursor.getLong(COL_WEATHER_DATE);
        String dateText = Utility.formatDate(dateInMillis);
        String dayName = Utility.getDayName(getActivity(), dateInMillis);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        if (shareProvider != null) {
            shareProvider.setShareIntent(prepareShareIntent());
        }

        int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);
        icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        day.setText(dayName);
        date.setText(dateText);
        String descriptionString = cursor.getString(COL_WEATHER_DESC);
        description.setText(descriptionString);
        highTemp.setText(high);
        lowTemp.setText(low);
        humidity.setText(cursor.getString(COL_WEATHER_HUMIDITY));
        pressure.setText(cursor.getString(COL_WEATHER_PRESSURE));
        wind.setText(cursor.getString(COL_WEATHER_WIND_SPEED));

        mWindControl.setDegrees(cursor.getFloat(COL_WEATHER_DEGREES));
        mWindControl.setSpeed(cursor.getFloat(COL_WEATHER_WIND_SPEED));

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            accessibilityManager.sendAccessibilityEvent(AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED));
        }

        // We still need this for the share intent
        forecastStr = String.format("%s - %s - %s/%s", dateText, descriptionString, high, low);
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (shareProvider != null) {
            shareProvider.setShareIntent(prepareShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    void onLocationChanged(String newLocation) {
// replace the uri, since the location has changed
        Uri oldUri = uri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(oldUri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            uri = updatedUri;
            getLoaderManager().restartLoader(MainActivity.DETAIL_LOADER_ID, null, this);
        }
    }
}