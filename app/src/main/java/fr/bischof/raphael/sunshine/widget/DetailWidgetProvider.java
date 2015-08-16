package fr.bischof.raphael.sunshine.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import fr.bischof.raphael.sunshine.DetailActivity;
import fr.bischof.raphael.sunshine.MainActivity;
import fr.bischof.raphael.sunshine.R;
import fr.bischof.raphael.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by biche on 14/08/2015.
 */
public class DetailWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setRemoteViews(context, appWidgetIds, appWidgetManager);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        setRemoteViews(context,new int[]{appWidgetId},appWidgetManager);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteViews(Context context,int[] appWidgetIds,AppWidgetManager appWidgetManager){
        for(int appWidgetId:appWidgetIds){
            int layoutId = R.layout.widget_detail;
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);


            Intent svcIntent = new Intent(context, DetailWidgetRemoteViewsService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.widget_list,svcIntent);

            // Create an Intent to launch MainActivity
            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list,clickPendingIntentTemplate  );
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
