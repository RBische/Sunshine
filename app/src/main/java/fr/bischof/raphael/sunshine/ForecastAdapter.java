package fr.bischof.raphael.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import fr.bischof.raphael.sunshine.data.WeatherContract;

/**
 * Created by biche on 17/06/2015.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private final View mEmptyView;
    private Context mContext;
    private boolean mShowTodayItem = false;
    private Cursor mCursor;
    final private ForecastAdapterOnClickHandler mClickHandler;


    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler, View emptyView) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
        this.mEmptyView = emptyView;
        if(mEmptyView!=null){
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId = -1;
        if(viewType==VIEW_TYPE_TODAY) layoutId = R.layout.list_item_forecast_today;
        else layoutId = R.layout.list_item_forecast;

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        // Read weather icon ID from cursor
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(mCursor.getPosition());
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(
                        weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(
                        weatherId);
                break;
            }
        }


        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);

        // Read date from cursor
        long dateString = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateString));

        viewHolder.descriptionView.setText(mCursor.getString(ForecastFragment.COL_WEATHER_DESC));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext,high, isMetric));

        // Read low temperature from cursor
        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && mShowTodayItem ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mShowTodayItem = useTodayLayout;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (mCursor!=null&&mCursor.getCount()>0&&mEmptyView!=null){
            mEmptyView.setVisibility(View.GONE);
        }else if(mEmptyView!=null){
            mEmptyView.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
        }
    }

    public interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder vh);
    }
}
