# Sblorftz Timer v1.0

Timer per tornei di poker per Android TV sviluppato in Flutter.

## Caratteristiche

- Timer con countdown per livelli di blind
- Struttura torneo personalizzabile
- Salvataggio automatico della sessione
- Navigazione completa con telecomando Android TV
- Audio per avvisi (1 minuto rimasto, cambio livello)
- Tema scuro stile casinò

## Prerequisiti

Prima di compilare l'app, assicurati di avere installato:

1. **Flutter SDK** (versione 3.0 o superiore)
   - Scarica da: https://flutter.dev/docs/get-started/install
   - Aggiungi Flutter al PATH di sistema

2. **Android Studio** o **Visual Studio Code**
   - Con plugin Flutter e Dart installati

3. **Android SDK**
   - API Level 21 o superiore (configurato tramite Android Studio)

## Installazione

### 1. Verifica installazione Flutter

```bash
flutter doctor
```

Assicurati che tutti i check siano verdi (tranne quelli opzionali).

### 2. Installa le dipendenze

Dalla directory del progetto, esegui:

```bash
flutter pub get
```

### 3. Aggiungi i file audio

L'app richiede due file audio nella cartella `assets/sounds/`:

- `level_change.mp3` - Suono per il cambio livello (gong/campana)
- `one_minute.mp3` - Suono per l'avviso a 1 minuto

Puoi usare qualsiasi file MP3. Assicurati che siano presenti prima di compilare.

**Suggerimento:** Puoi trovare suoni gratuiti su siti come:
- https://freesound.org/
- https://mixkit.co/free-sound-effects/

### 4. Compila l'app

Per creare un APK di debug:

```bash
flutter build apk --debug
```

Per creare un APK di release:

```bash
flutter build apk --release
```

L'APK sarà generato in: `build/app/outputs/flutter-apk/`

### 5. Installa su Android TV

**Via USB:**
```bash
flutter install
```

**Manualmente:**
1. Copia l'APK su una chiavetta USB
2. Installa un file manager su Android TV (es. "X-plore File Manager")
3. Apri l'APK e installa

## Utilizzo

### Navigazione con Telecomando

#### Home Screen
- **OK** - Avvia torneo

#### Editor Struttura
- **Frecce** - Naviga tra i livelli e le colonne
- **OK** - Modifica valore o conferma azione
- **Back** - Torna alla home

#### Timer
- **Play/Pause** - Mette in pausa o riprende il timer
- **▶▶ (Fast Forward)** - Passa al livello successivo
- **◀◀ (Rewind)** - Torna al livello precedente
- **Back** - Mostra conferma uscita (salva automaticamente)

### Ripristino Sessione

L'app salva automaticamente lo stato del torneo quando esci dal timer. Al riavvio:
- Se esiste una sessione salvata, apparirà un popup
- **RIPRENDI** - Continua dal punto esatto dove hai lasciato
- **NUOVO** - Cancella la sessione e inizia un nuovo torneo

## Struttura Progetto

```
lib/
├── main.dart                    # Punto di ingresso
├── models/
│   ├── blind_level.dart         # Modello livello blind
│   └── saved_session.dart       # Modello sessione salvata
├── data/
│   └── default_structure.dart   # Template hardcoded (18 livelli)
├── screens/
│   ├── home_screen.dart         # Schermata home
│   ├── editor_screen.dart       # Editor struttura torneo
│   └── timer_screen.dart        # Timer principale
├── widgets/
│   ├── smart_input_popup.dart   # Popup modifica valori
│   ├── confirm_popup.dart       # Popup conferma
│   ├── add_level_popup.dart     # Popup aggiungi livello
│   └── resume_popup.dart        # Popup ripristino sessione
├── services/
│   ├── audio_service.dart       # Gestione audio
│   └── session_service.dart     # Salvataggio/ripristino stato
└── theme/
    └── app_theme.dart           # Tema scuro stile casinò
```

## Personalizzazione

### Modifica Template Default

Modifica il file `lib/data/default_structure.dart` per cambiare la struttura predefinita dei livelli.

### Cambia Colori

Modifica `lib/theme/app_theme.dart` per personalizzare la palette colori.

### Cambia Audio

Sostituisci i file MP3 in `assets/sounds/` con i tuoi suoni personalizzati.

## Troubleshooting

### L'app non si installa su Android TV

1. Abilita "Origini sconosciute" nelle impostazioni di Android TV
2. Abilita "Debug USB" nelle opzioni sviluppatore

### I suoni non funzionano

1. Verifica che i file MP3 siano presenti in `assets/sounds/`
2. Controlla che i nomi dei file corrispondano esattamente a:
   - `level_change.mp3`
   - `one_minute.mp3`
3. Verifica il volume di Android TV

### Il timer non risponde al telecomando

1. Assicurati che l'app abbia il focus
2. Prova a premere OK per dare il focus all'app
3. Verifica che il telecomando sia correttamente accoppiato

## Roadmap Futura (v2.0)

- Visualizzazione fiches colorate
- Salvataggio strutture multiple
- Impostazioni (volume, scelta suoni)
- Temi alternativi
- Statistiche torneo

## Licenza

Progetto personale - Uso libero

## Contatti

Per bug o suggerimenti, contatta lo sviluppatore.

---

**Sblorftz Timer v1.0 - Keep it simple, play poker!** ♠♥♣♦
