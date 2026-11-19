#!/usr/bin/env bash
# Builds Countdown.apk using the Android SDK tools directly (no Gradle).
# Needs: JDK 17+, Android SDK with platforms;android-35 and build-tools;35.0.0, Python (uv).
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-$LOCALAPPDATA/Android/Sdk}"
BT="$SDK/build-tools/35.0.0"
JAR="$SDK/platforms/android-35/android.jar"
VERSION_CODE="${VERSION_CODE:-1}"
VERSION_NAME="${VERSION_NAME:-1.0}"
OUT=build

rm -rf "$OUT" && mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

echo "== resources"
"$BT/aapt2" compile --dir app/res -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$JAR" \
    --manifest app/AndroidManifest.xml "$OUT/res.zip" --java "$OUT/gen" \
    --min-sdk-version 26 --target-sdk-version 34 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME"

echo "== compile"
javac -nowarn -Xlint:-options --release 11 -encoding UTF-8 -cp "$JAR" -d "$OUT/classes" \
    $(find app/src "$OUT/gen" -name '*.java')

echo "== dex"
"$BT/d8.bat" --release --min-api 26 --lib "$JAR" --output "$OUT/dex" \
    $(find "$OUT/classes" -name '*.class')

echo "== package"
python -I -c "import sys,zipfile; z=zipfile.ZipFile(sys.argv[1],'a',zipfile.ZIP_DEFLATED); z.write(sys.argv[2],'classes.dex'); z.close()" \
    "$OUT/base.apk" "$OUT/dex/classes.dex"
"$BT/zipalign" -p -f 4 "$OUT/base.apk" "$OUT/aligned.apk"

# Signing key: kept next to this script. Reuse the same key for every build,
# otherwise Android won't install an update over the existing app.
KS=countdown.keystore
if [ ! -f "$KS" ]; then
    keytool -genkeypair -keystore "$KS" -storepass countdown -keypass countdown \
        -alias countdown -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Countdown" >/dev/null
fi
"$BT/apksigner.bat" sign --ks "$KS" --ks-pass pass:countdown --key-pass pass:countdown \
    --out Countdown.apk "$OUT/aligned.apk"
"$BT/apksigner.bat" verify Countdown.apk
echo "== done: $(pwd)/Countdown.apk"
