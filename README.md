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

## Build

```bash
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

Serve un `local.properties` con `sdk.dir`. La release è firmata con la chiave debug della macchina che compila.

La versione Flutter (v1.3) è nel tag `v1.3-flutter`.
