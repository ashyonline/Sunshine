package com.ashionline.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashionline.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /*
                This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
                string.
             */
    private String convertCursorRowToUXFormat(Cursor cursor) {
         String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int type = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (VIEW_TYPE_TODAY == type) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    /*
    This is where we fill-in the views with the contents of the cursor.
 */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder holder = (ViewHolder) view.getTag();

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int type = getItemViewType(cursor.getPosition());
        if (VIEW_TYPE_TODAY == type) {
            holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else {
            holder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        // TODO Read date from cursor
        long dateValue = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.dateTextView.setText(Utility.formatDate(dateValue));

        // TODO Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.forecastTextView.setText(description);
        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highTemperatureTextView.setText(Utility.formatTemperature(mContext, high, isMetric));

        // TODO Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowTemperatureTextView.setText(Utility.formatTemperature(mContext, low, isMetric));

    }

    private static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateTextView;
        public final TextView forecastTextView;
        public final TextView highTemperatureTextView;
        public final TextView lowTemperatureTextView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTemperatureTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTemperatureTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}