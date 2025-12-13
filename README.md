# Sblorftz Timer v1.3

Timer professionale per tornei di poker su Android TV sviluppato in Flutter.

## ✨ Caratteristiche v1.3

### 🎮 Funzionalità Principali
- **Timer con countdown** per livelli di blind con animazioni
- **Struttura torneo personalizzabile** (aggiunta, modifica, eliminazione livelli)
- **Livello CAMBIO FICHES** dedicato (visualizzazione speciale con emoji 💯➡️💰)
- **Salvataggio automatico** della sessione ad ogni secondo
- **Ripristino sessione** esatto con popup "Riprendi/Nuovo"
- **Wakelock** - schermo sempre acceso durante il torneo
- **Navigazione completa** con telecomando Android TV

### 🎵 Sistema Audio
- **3 avvisi audio** configurati:
  - 🔔 1 minuto rimasto (`one_minute.mp3`)
  - ⏱️ 10 secondi finali (`ten_seconds.mp3`)
  - 🔊 Cambio livello (`level_change.mp3`)

### ⚡ Controlli Avanzati
- **Pressione prolungata (1.5s)** per cambio livello sicuro con barra progresso
- **Undo bidirezionale** con sistema checkpoint per tornare indietro
- **Play/Pause** istantaneo con animazione pulsante
- **Reset livello** con pressione prolungata su Play/Pause
- **Focus automatico migliorato** all'avvio

### 🎨 Interfaccia
- **Tema scuro stile casinò** (oro #D4AF37 su nero #0D0D0D)
- **Animazioni finali** quando il timer raggiunge 0
- **Countdown gigante** (160sp) per leggibilità a distanza
- **Interfaccia ottimizzata** per TV con focus visuale chiaro
- **19 livelli predefiniti** con progressione professionale

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

L'app richiede **3 file audio** nella cartella `assets/sounds/`:

- `level_change.mp3` - Suono per il cambio livello (gong/campana)
- `one_minute.mp3` - Suono per l'avviso a 1 minuto rimasto
- `ten_seconds.mp3` - Suono per l'avviso a 10 secondi finali

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

## 🎮 Utilizzo

### Navigazione con Telecomando

#### Home Screen
- **Frecce ↑↓** - Naviga tra "AVVIA TORNEO" e "PERSONALIZZA"
- **OK** - Conferma selezione
  - *AVVIA TORNEO*: Avvia direttamente il timer
  - *PERSONALIZZA*: Apre l'editor per modificare i livelli

#### Editor Struttura
- **Frecce ↑↓** - Naviga tra i livelli
- **OK** - Entra in modalità modifica riga
- **Frecce ←→** (in modifica) - Scegli colonna (SB / TEMPO / ELIMINA)
- **OK** (su colonna) - Modifica valore o elimina livello
- **Back** - Esce dalla modifica / Torna alla home
- **Pulsante "AGGIUNGI LIVELLO"** - Crea nuovo livello con valori intelligenti
- **Pulsante "AVVIA TIMER"** - Salva e avvia il torneo

#### Timer Screen - Controlli Base
- **Play/Pause (breve)** - Mette in pausa o riprende il timer
- **Play/Pause (lunga 1.5s)** - 🔄 Reset tempo livello corrente
- **◀◀ o ← (breve)** - Tap singolo ignorato
- **◀◀ o ← (lunga 1.5s)** - ⏪ Torna al livello precedente (con undo)
- **▶▶ o → (breve)** - Tap singolo ignorato
- **▶▶ o → (lunga 1.5s)** - ⏩ Salta al livello successivo (con undo)
- **Back** - Mostra popup conferma uscita (salva automaticamente)

#### Sistema Undo Bidirezionale
- Il timer salva un **checkpoint** ogni volta che cambi livello manualmente
- Quando torni indietro/avanti, **ripristina il tempo esatto** del checkpoint
- Puoi fare **undo/redo** illimitati tra livelli adiacenti

#### Barra Progresso Pressione Prolungata
Durante la pressione prolungata appare un overlay con:
- Titolo azione (es. "CAMBIO LIVELLO IN CORSO...")
- Barra progresso 0-100%
- Percentuale visibile
- Se rilasci prima di 1.5s, l'azione viene **annullata**

### Ripristino Sessione

L'app salva automaticamente lo stato del torneo quando esci dal timer. Al riavvio:
- Se esiste una sessione salvata, apparirà un popup
- **RIPRENDI** - Continua dal punto esatto dove hai lasciato
- **NUOVO** - Cancella la sessione e inizia un nuovo torneo

## 📁 Struttura Progetto

```
lib/
├── main.dart                    # Punto di ingresso
├── models/
│   ├── blind_level.dart         # Modello livello blind
│   └── saved_session.dart       # Modello sessione salvata con checkpoint
├── data/
│   └── default_structure.dart   # Template hardcoded (19 livelli + CAMBIO FICHES)
├── screens/
│   ├── home_screen.dart         # Schermata home con 2 pulsanti
│   ├── editor_screen.dart       # Editor struttura torneo completo
│   └── timer_screen.dart        # Timer con pressione prolungata e undo
├── widgets/
│   ├── focusable_button.dart    # Bottone ottimizzato per Android TV
│   ├── smart_input_popup.dart   # Popup modifica valori con suggerimenti
│   ├── confirm_popup.dart       # Popup conferma eliminazione
│   ├── add_level_popup.dart     # Popup aggiungi livello
│   └── resume_popup.dart        # Popup ripristino sessione
├── services/
│   ├── audio_service.dart       # Gestione 3 suoni (1min, 10sec, cambio)
│   └── session_service.dart     # Persistenza con SharedPreferences
└── theme/
    └── app_theme.dart           # Tema scuro casinò (oro su nero)
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

1. Verifica che **tutti e 3** i file MP3 siano presenti in `assets/sounds/`
2. Controlla che i nomi dei file corrispondano esattamente a:
   - `level_change.mp3`
   - `one_minute.mp3`
   - `ten_seconds.mp3`
3. Verifica il volume di Android TV
4. Testa con `flutter build apk --release` (non debug)

### Il timer non risponde al telecomando

1. Assicurati che l'app abbia il focus
2. Prova a premere OK per dare il focus all'app
3. Verifica che il telecomando sia correttamente accoppiato

## 📋 Changelog

### v1.3 (Corrente)
- ➕ Livello CAMBIO FICHES con visualizzazione dedicata
- 📊 19 livelli predefiniti (invece di 18)
- ✨ Emoji e animazioni per cambio fiches

### v1.2.1
- 🎯 Focus automatico migliorato all'avvio dell'app

### v1.2
- 🎨 Interfaccia editor migliorata per scelta blind
- 💡 Smart Input con suggerimenti intelligenti

### v1.1.2
- 👁️ Focus visuale potenziato per Android TV
- 🔧 Ottimizzazioni UX

### v1.1
- ⏱️ Pressione prolungata (1.5s) per cambio livello sicuro
- 🔄 Sistema undo bidirezionale con checkpoint
- 📊 Barra progresso visuale durante pressione

### v1.02
- 🎬 Animazioni timer finale (ultimi 10 secondi)
- 💥 Effetto pulsante quando timer raggiunge 0

### v1.01
- 🔒 Wakelock - schermo sempre acceso durante torneo
- 🔊 Miglioramenti sistema audio
- 🎵 Aggiunto suono 10 secondi finali

### v1.0
- 🎉 Release iniziale

## 🚀 Roadmap Futura (v2.0)

- 🎨 Visualizzazione fiches colorate per valore
- 💾 Salvataggio strutture multiple personalizzate
- ⚙️ Impostazioni (volume, scelta suoni custom)
- 🌈 Temi alternativi (casinò rosso, verde felt, etc.)
- 📊 Statistiche torneo (durata totale, livelli completati)
- 🏆 Modalità sit&go con blind più veloci

## Licenza

Progetto personale - Uso libero

## Contatti

Per bug o suggerimenti, contatta lo sviluppatore.

---

**Sblorftz Timer v1.3 - Timer professionale per i tuoi tornei di poker!** ♠♥♣♦
