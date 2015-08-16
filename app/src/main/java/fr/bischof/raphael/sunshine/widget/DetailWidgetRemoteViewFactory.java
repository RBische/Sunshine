package fr.bischof.raphael.sunshine.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import fr.bischof.raphael.sunshine.R;
import fr.bischof.raphael.sunshine.Utility;
import fr.bischof.raphael.sunshine.data.WeatherContract;

/**
 * Created by biche on 14/08/2015.
 */
public class DetailWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private Cursor mData;

    public DetailWidgetRemoteViewFactory(Context context, Intent intent) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        // Get today's data from the ContentProvider
        final long identityToken = Binder.clearCallingIdentity();
        String location = Utility.getPreferredLocation(mContext);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        mData = mContext.getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        mData.close();
    }

    @Override
    public int getCount() {
        int count = mData.getCount();
        return count;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                mData == null || !mData.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);
        // Read weather icon ID from cursor
        int weatherId = mData.getInt(COL_WEATHER_ID);
        // Get weather icon
        views.setImageViewResource(R.id.widget_icon, Utility.getIconResourceForWeatherCondition(
                mData.getInt(COL_WEATHER_CONDITION_ID)));

        // Read date from cursor
        long dateString = mData.getLong(COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        views.setTextViewText(R.id.widget_date,Utility.getFriendlyDayString(mContext, dateString));

        views.setTextViewText(R.id.widget_description,mData.getString(COL_WEATHER_DESC));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from cursor
        double high = mData.getDouble(COL_WEATHER_MAX_TEMP);
        views.setTextViewText(R.id.widget_high_temperature,Utility.formatTemperature(mContext,high, isMetric));

        // Read low temperature from cursor
        double low = mData.getDouble(COL_WEATHER_MIN_TEMP);
        views.setTextViewText(R.id.widget_low_temperature, Utility.formatTemperature(mContext, low, isMetric));

        final Intent fillInIntent = new Intent();
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(Utility.getPreferredLocation(mContext),dateString);
        fillInIntent.setData(weatherUri);
        views.setOnClickFillInIntent(R.id.widget_list_item,fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mData.moveToPosition(position))
            return mData.getLong(COL_WEATHER_ID);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
