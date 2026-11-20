# Countdown

A tiny, ad-free Android home-screen widget that counts down the days to something you're looking forward to.

Each countdown is a 1×1 widget: a clean white circle with the number of days left, and a coloured ring that slowly empties as the date gets closer.

## Features

- **Days at a glance.** A big, bold day count with your label underneath, just like an app name.
- **A ring that means something.** It stays full while the date is a year or more away, then empties day by day until it's gone.
- **Your colours.** Choose from seven ring colours for each countdown.
- **Easy to change.** Tap a countdown to edit its label, date or colour.
- **Always current.** Updates just after midnight and keeps counting up once the day has passed.
- **Private and lightweight.** About 25 KB, with no ads, no tracking and no internet access.

Requires Android 8.0 or later.

## Install

1. Download the latest `Countdown-x.y.apk` from [Releases](../../releases) on your phone.
2. Open it. If Android asks, allow installs from your browser or file manager.
3. Long-press your home screen, tap **Widgets**, find **Countdown** and drag it out.
4. Pick a label, date and colour, then tap **Save**.

Add as many countdowns as you like.

## Building from source

Countdown is plain Java with no third-party libraries. It builds with the Android SDK tools directly; you don't need Gradle or Android Studio.

You'll need:

- JDK 17 or newer
- Python 3
- The Android SDK with `platforms;android-35` and `build-tools;35.0.0`. The build looks for it in `$ANDROID_HOME`, falling back to `%LOCALAPPDATA%\Android\Sdk`.

Then run:

```sh
VERSION_CODE=5 VERSION_NAME=1.4 bash build.sh
```

The signed APK is written to `Countdown.apk`.

## Project layout

```
app/
├── AndroidManifest.xml
├── res/                          layouts, widget metadata, icon
└── src/com/simple/countdown/
    ├── CountdownWidget.java      widget provider and midnight refresh
    ├── Renderer.java             draws the ring and day count
    ├── ConfigActivity.java       label, date and colour screen
    ├── Store.java                per-widget settings
    └── MainActivity.java         how-to screen shown from the launcher
build.sh                          builds and signs the APK
```

## License

[MIT](LICENSE)
