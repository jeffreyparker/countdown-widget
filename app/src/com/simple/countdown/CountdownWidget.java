package com.simple.countdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.ZoneId;

public class CountdownWidget extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.simple.countdown.REFRESH";
    private static final int BITMAP_SIZE = 400;

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) update(c, m, id);
        scheduleMidnight(c);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        update(c, m, id);
    }

    @Override
    public void onDeleted(Context c, int[] ids) {
        for (int id : ids) Store.delete(c, id);
    }

    @Override
    public void onEnabled(Context c) {
        scheduleMidnight(c);
    }

    @Override
    public void onDisabled(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am != null) am.cancel(refreshIntent(c));
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        super.onReceive(c, intent);
        String a = intent.getAction();
        if (ACTION_REFRESH.equals(a)
                || Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            updateAll(c);
            scheduleMidnight(c);
        }
    }

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, CountdownWidget.class))) {
            update(c, m, id);
        }
    }

    static void update(Context c, AppWidgetManager m, int id) {
        Store.Countdown cd = Store.load(c, id);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
        rv.setImageViewBitmap(R.id.ring, Renderer.render(cd, BITMAP_SIZE));
        rv.setContentDescription(R.id.ring, describe(cd));
        String label = cd == null ? "" : cd.label;
        rv.setTextViewText(R.id.label, label);
        rv.setViewVisibility(R.id.label, label.isEmpty() ? View.GONE : View.VISIBLE);

        Intent edit = new Intent(c, ConfigActivity.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .setData(Uri.parse("countdown://widget/" + id))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, id, edit,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.root, pi);
        m.updateAppWidget(id, rv);
    }

    private static String describe(Store.Countdown cd) {
        if (cd == null) return "Countdown, not set. Tap to choose a date.";
        long d = Renderer.daysLeft(cd);
        String what = cd.label.isEmpty() ? "" : " " + cd.label;
        if (d < 0) return -d + " days since" + what;
        return d + " days until" + what;
    }

    /** Wakes the widget just after local midnight so the day count ticks over. */
    static void scheduleMidnight(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am == null) return;
        long at = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .plusSeconds(5)
                .toInstant().toEpochMilli();
        am.set(AlarmManager.RTC, at, refreshIntent(c));
    }

    private static PendingIntent refreshIntent(Context c) {
        Intent i = new Intent(c, CountdownWidget.class).setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(c, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
