package com.simple.countdown;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Draws the widget face: a white disc, a thin grey outer line, a thick coloured arc
 * for the share of time remaining (clockwise from 12 o'clock), and the day count in
 * a thin font with the label underneath.
 */
final class Renderer {
    private static final int TRACK = 0xFFC0C0C0;
    private static final int NUMBER = 0xFF000000;
    private static final int NUMBER_PAST = 0xFF9E9E9E;

    private Renderer() {}

    static long daysLeft(Store.Countdown cd) {
        return ChronoUnit.DAYS.between(LocalDate.now(), cd.target);
    }

    static Bitmap render(Store.Countdown cd, int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float c = size / 2f;
        float r = size / 2f - size * 0.03f;
        float arcW = r * 0.155f;
        float trackW = Math.max(1.5f, r * 0.034f);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFFFF);
        canvas.drawCircle(c, c, r, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(trackW);
        p.setColor(TRACK);
        canvas.drawCircle(c, c, r - trackW / 2f, p);

        String number;
        String unit;
        boolean past = false;
        if (cd == null) {
            number = "?";
            unit = "Tap to set";
        } else {
            long days = daysLeft(cd);
            past = days < 0;
            number = String.valueOf(Math.abs(days));
            unit = Math.abs(days) == 1 ? "Day" : "Days";
            if (past) unit += " ago";

            float remaining = remainingFraction(cd, days);
            if (remaining > 0f) {
                p.setStrokeWidth(arcW);
                p.setStrokeCap(Paint.Cap.BUTT);
                p.setColor(cd.color);
                float ar = r - arcW / 2f;
                canvas.drawArc(new RectF(c - ar, c - ar, c + ar, c + ar),
                        -90f, 360f * remaining, false, p);
            }
        }

        Typeface bold = Typeface.create("sans-serif", Typeface.BOLD);

        // Big bold number; cap height ~0.55 of the radius, shrunk only if it gets too wide.
        TextPaint num = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        num.setTypeface(bold);
        num.setColor(past ? NUMBER_PAST : NUMBER);
        num.setTextAlign(Paint.Align.CENTER);
        num.setTextSize(r * 0.77f);
        float maxNumW = r * 1.5f;
        float w = num.measureText(number);
        if (w > maxNumW) num.setTextSize(num.getTextSize() * maxNumW / w);
        Rect nb = new Rect();
        num.getTextBounds(number, 0, number.length(), nb);

        // "Days" underneath, small and bold.
        TextPaint sub = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        sub.setTypeface(bold);
        sub.setColor(past ? NUMBER_PAST : NUMBER);
        sub.setTextAlign(Paint.Align.CENTER);
        sub.setTextSize(r * 0.19f);
        String shown = TextUtils.ellipsize(unit, sub, r * 1.4f, TextUtils.TruncateAt.END).toString();
        Rect sb = new Rect();
        sub.getTextBounds("D", 0, 1, sb);

        float gap = r * 0.13f;
        float blockH = nb.height() + gap + sb.height();
        float numBaseline = c - blockH / 2f + nb.height() - nb.bottom;
        canvas.drawText(number, c, numBaseline, num);
        canvas.drawText(shown, c, numBaseline + nb.bottom + gap + sb.height(), sub);
        return bmp;
    }

    /** The ring covers a fixed year: full at 365+ days left, empty on the day itself. */
    static final int RING_DAYS = 365;

    static float remainingFraction(Store.Countdown cd, long daysLeft) {
        if (daysLeft <= 0) return 0f;
        return Math.min(daysLeft, RING_DAYS) / (float) RING_DAYS;
    }
}
