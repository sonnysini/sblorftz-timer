#!/usr/bin/env bash
# Pubblica un APK release per l'aggiornamento dall'app (vedi Updater.kt).
# Uso (sulla VM, dalla root della repo): tools/publish.sh <apk> "note della versione"
# Copia l'APK in ~/cloud/pubblico/sblorftz/ (= https://files.sonnysini.it/Pubblico/sblorftz/)
# e riscrive latest.json con versionCode/versionName presi da app/build.gradle.kts.
set -euo pipefail
apk=$1
notes=${2:-}
dest=${SBLORFTZ_PUBLISH_DIR:-$HOME/cloud/pubblico/sblorftz}

code=$(sed -n 's/^ *versionCode = \([0-9]*\).*/\1/p' app/build.gradle.kts)
name=$(sed -n 's/^ *versionName = "\(.*\)".*/\1/p' app/build.gradle.kts)
[ -n "$code" ] && [ -n "$name" ] || { echo "versione non trovata in app/build.gradle.kts" >&2; exit 1; }

mkdir -p "$dest"
file="sblorftz-timer-$name.apk"
cp "$apk" "$dest/$file.tmp" && mv "$dest/$file.tmp" "$dest/$file"
python3 -c 'import json,sys; print(json.dumps({"versionCode": int(sys.argv[1]), "versionName": sys.argv[2], "apk": sys.argv[3], "notes": sys.argv[4]}, ensure_ascii=False))' \
    "$code" "$name" "$file" "$notes" > "$dest/latest.json.tmp"
mv "$dest/latest.json.tmp" "$dest/latest.json"
# Link fisso per la prima installazione (Downloader): …/Pubblico/sblorftz/sblorftz-timer.apk
cp "$dest/$file" "$dest/sblorftz-timer.apk"
echo "pubblicato $name ($code) in $dest"
