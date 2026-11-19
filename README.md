# Countdown

A tiny, ad-free Android home-screen widget: a 1×1 ring counting down the days to a date.

- White circle with a coloured arc for the time left. The arc is full at 365+ days and empties as the date approaches.
- Bold day count with "Days" underneath; your label sits under the circle like an app name.
- Tap the widget to change its label, date or colour.
- Refreshes just after midnight. No internet permission, no ads, no tracking.

## Build

Needs a JDK (17+), Python and the Android SDK with `platforms;android-35` and `build-tools;35.0.0`
(looked up at `$ANDROID_HOME`, or `%LOCALAPPDATA%\Android\Sdk` by default). No Gradle.

```sh
VERSION_CODE=5 VERSION_NAME=1.4 bash build.sh
```

This produces `Countdown.apk`. The first build creates `countdown.keystore` for signing; it is not committed.
Keep a backup of it, because Android only installs an update over the app if it's signed with the same key.
Raise `VERSION_CODE` for each new build you install.

## Layout

- `app/AndroidManifest.xml`
- `app/src/com/simple/countdown/`
  - `CountdownWidget.java`: widget provider and midnight refresh
  - `Renderer.java`: draws the ring
  - `ConfigActivity.java`: label, date and colour screen
  - `Store.java`: per-widget settings
- `app/res/`: layouts, widget metadata, icon
