package com.simple.countdown;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

/** Saves each widget's settings, keyed by its widget id. */
final class Store {
    static final int DEFAULT_COLOR = 0xFF03B4E8;

    static final class Countdown {
        String label = "";
        LocalDate target;
        /** The day the countdown was set; the ring shows the share of time left since then. */
        LocalDate start;
        int color = DEFAULT_COLOR;
    }

    private Store() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("countdowns", Context.MODE_PRIVATE);
    }

    static Countdown load(Context c, int id) {
        SharedPreferences p = prefs(c);
        if (!p.contains(id + "_target")) return null;
        Countdown cd = new Countdown();
        cd.label = p.getString(id + "_label", "");
        cd.target = LocalDate.ofEpochDay(p.getLong(id + "_target", 0));
        cd.start = LocalDate.ofEpochDay(p.getLong(id + "_start", LocalDate.now().toEpochDay()));
        cd.color = p.getInt(id + "_color", DEFAULT_COLOR);
        return cd;
    }

    static void save(Context c, int id, Countdown cd) {
        prefs(c).edit()
                .putString(id + "_label", cd.label)
                .putLong(id + "_target", cd.target.toEpochDay())
                .putLong(id + "_start", cd.start.toEpochDay())
                .putInt(id + "_color", cd.color)
                .apply();
    }

    static void delete(Context c, int id) {
        prefs(c).edit()
                .remove(id + "_label")
                .remove(id + "_target")
                .remove(id + "_start")
                .remove(id + "_color")
                .apply();
    }
}
