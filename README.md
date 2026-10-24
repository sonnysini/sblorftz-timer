# Sblorftz Timer

Timer per tornei di poker su Android TV / Fire TV. Kotlin + Jetpack Compose for TV.

## Telecomando

| Tasto | Cosa fa |
|---|---|
| OK / ⏯ | Pausa e ripresa |
| ◀ ▶ tenuti premuti (1 s) | Livello precedente o successivo. Tornando su un livello si ritrova il tempo che si era lasciato |
| ▲ ▼ | ±1 minuto sul livello in corso |
| ☰ | Menu: riprendi, ricomincia livello, struttura, nuovo torneo, esci |
| Indietro | Esci (con conferma). Il torneo resta salvato |

## Funzioni

- Struttura modificabile dal telecomando e salvata: piccolo buio (il grande è il doppio), durata, livelli e pause per il cambio fiches (100 → 500)
- Torneo salvato ogni secondo: se l'app si chiude, dalla home si riprende da dove si era (in pausa)
- Avvisi audio a 1 minuto, a 10 secondi e al cambio livello
- Schermo sempre acceso durante il torneo
- In basso: prossimo livello e tempo al prossimo cambio fiches
- Aggiornamenti dall'app (Release GitHub)

## Build

```bash
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

Serve un `local.properties` con `sdk.dir`. In locale la release è firmata con la chiave debug della macchina che compila; gli APK della CI con la chiave fissa del progetto (secret `KEYSTORE_*`).

## Installare e aggiornare

Prima installazione su Fire TV con Downloader:
`https://github.com/sonnysini/sblorftz-timer/releases/latest/download/sblorftz-timer.apk`

Poi l'app controlla da sola le nuove Release all'apertura (solo dalla home) e propone l'aggiornamento.

## Rilasciare

1. `versionCode` +1 e `versionName` nuovo in `app/build.gradle.kts`
2. Sezione nuova in `CHANGELOG.md` (diventa il testo del popup di aggiornamento)
3. Merge su `main`, poi `git tag vX.Y.Z && git push origin vX.Y.Z`: la CI compila, firma e pubblica la Release

La versione Flutter (v1.3) è nel tag `v1.3-flutter`.
