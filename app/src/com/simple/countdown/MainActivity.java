package com.simple.countdown;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

/** Launcher screen: just explains how to add the widget. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        TextView t = new TextView(this);
        t.setText(R.string.main_text);
        t.setTextSize(17);
        t.setLineSpacing(0, 1.2f);
        t.setPadding(pad, pad, pad, pad);
        ScrollView s = new ScrollView(this);
        s.setFitsSystemWindows(true);
        s.addView(t);
        setContentView(s);
    }
}
