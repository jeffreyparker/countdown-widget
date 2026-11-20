package io.parker.countdown;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.time.LocalDate;

/** Shown when the widget is added, and whenever it is tapped. */
public class ConfigActivity extends Activity {
    private static final int[] COLORS = {
            0xFF03B4E8, 0xFF43A047, 0xFFFB8C00, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF424242,
    };

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Store.Countdown existing;
    private int color = Store.DEFAULT_COLOR;
    private EditText label;
    private DatePicker date;
    private ImageView preview;
    private LinearLayout swatches;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setResult(RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.activity_config);
        label = findViewById(R.id.label);
        date = findViewById(R.id.date);
        preview = findViewById(R.id.preview);
        swatches = findViewById(R.id.swatches);

        existing = Store.load(this, widgetId);
        LocalDate initial = LocalDate.now().plusDays(30);
        if (existing != null) {
            label.setText(existing.label);
            initial = existing.target;
            color = existing.color;
        }
        date.init(initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth(),
                (v, y, m, d) -> refreshPreview());
        label.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int n) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int n) {}
            @Override public void afterTextChanged(Editable s) { refreshPreview(); }
        });
        buildSwatches();
        refreshPreview();

        findViewById(R.id.save).setOnClickListener(v -> save());
    }

    private Store.Countdown current() {
        Store.Countdown cd = new Store.Countdown();
        cd.label = label.getText().toString().trim();
        cd.target = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDayOfMonth());
        cd.color = color;
        // Start day is stored for reference only; the ring uses a fixed 365-day scale.
        cd.start = (existing != null && existing.target.equals(cd.target)) ? existing.start : LocalDate.now();
        return cd;
    }

    private void refreshPreview() {
        preview.setImageBitmap(Renderer.render(current(), 400));
    }

    private void buildSwatches() {
        swatches.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        int size = (int) (34 * dp);
        int margin = (int) (6 * dp);
        for (int col : COLORS) {
            View v = new View(this);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(col);
            if (col == color) g.setStroke((int) (3 * dp), 0xFF000000);
            v.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(0, margin, margin * 2, margin);
            v.setLayoutParams(lp);
            v.setOnClickListener(x -> {
                color = col;
                buildSwatches();
                refreshPreview();
            });
            swatches.addView(v);
        }
    }

    private void save() {
        Store.save(this, widgetId, current());
        CountdownWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        CountdownWidget.scheduleMidnight(this);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }
}
