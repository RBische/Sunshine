package fr.bischof.raphael.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import fr.bischof.raphael.sunshine.data.WeatherContract;
import fr.bischof.raphael.sunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int FORECAST_LOADER = 101;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private int mPosition = RecyclerView.NO_POSITION;
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
    private static final String SAVE_SCROLL_POSITION = "scrollPosition";
    private RecyclerView lvForecast;
    private ForecastAdapter mForecastAdapter;
    private Loader<Cursor> mCursorLoader;
    private String mLocation;
    private boolean mUseTodayLayout = false;
    private TextView mTvEmpty;
    private View mParallaxBar;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        onLocationChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_main, container, false);
        this.lvForecast = ((RecyclerView)v.findViewById(R.id.listview_forecast));
        this.mTvEmpty = (TextView)v.findViewById(R.id.tvEmpty);
        this.mParallaxBar = v.findViewById(R.id.parallax_bar);
        this.lvForecast.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.lvForecast.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mParallaxBar!=null){
                    mParallaxBar.setTranslationY(mParallaxBar.getTranslationY()+0.5f*-dy);
                }
            }
        });
        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_SCROLL_POSITION)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SAVE_SCROLL_POSITION);
        }
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.lvForecast.clearOnScrollListeners();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ViewHolder vh) {
                if (mForecastAdapter!=null){
                    if (getActivity() instanceof ForecastFragmentCallbacks){
                        ForecastFragmentCallbacks forecastFragmentCallbacks = (ForecastFragmentCallbacks)getActivity();
                        forecastFragmentCallbacks.onListItemClicked(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, date));
                    }
                }
            }
        },mTvEmpty);
        this.mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        this.mCursorLoader = getLoaderManager().initLoader(FORECAST_LOADER,null,this);
        this.lvForecast.setAdapter(mForecastAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SAVE_SCROLL_POSITION,mPosition);
        }
    }

    private void loadDatas() {
        //String location = Utility.getPreferredLocation(getActivity());
        //new FetchWeatherTask(getActivity()).execute(location);
        if (mForecastAdapter!=null&&mForecastAdapter.getItemCount()==0){
            SunshineSyncAdapter.syncImmediately(getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_map){
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mForecastAdapter ) {
            Cursor c = mForecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount()==0){
            ConnectivityManager cm =
                    (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (!isConnected){
                mTvEmpty.setText(getString(R.string.no_connection));
            }else{
                mTvEmpty.setText(getString(R.string.empty_string));
            }
        }
        mForecastAdapter.swapCursor(data);
        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            lvForecast.smoothScrollToPosition(mPosition);
        }
        loadDatas();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(){
        mLocation = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        loadDatas();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status))){
            int locationStatus = Utility.getLocationStatus(getActivity());
            if (locationStatus == SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN){
                mTvEmpty.setText(getString(R.string.empty_string));
            }else if (locationStatus==SunshineSyncAdapter.LOCATION_STATUS_INVALID){
                mTvEmpty.setText(getString(R.string.empty_forecast_list_invalid_location));
            }else{
                mTvEmpty.setText(getString(R.string.no_connection));
            }
        }
    }
}
