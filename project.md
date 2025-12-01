# Sblorftz Timer v1.0 - Documento di Progetto

## 📋 Overview

**Nome:** Sblorftz Timer  
**Versione:** 1.0 (Entry Level)  
**Piattaforma:** Android TV  
**Tecnologia:** Flutter  
**Lingua:** Italiano  
**Tema visivo:** Scuro stile casinò  

---

## 🎯 Obiettivo v1.0

Un timer per poker funzionante e utilizzabile fin da subito, con il minimo indispensabile di funzionalità.

---

## 🖥️ Schermate (Solo 3)

### 1. Home Screen

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                                                         │
│                                                         │
│                    SBLORFTZ TIMER                       │
│                        ♠ ♥ ♣ ♦                          │
│                                                         │
│                                                         │
│              ┌─────────────────────┐                    │
│              │                     │                    │
│              │   ▶ AVVIA TORNEO    │                    │
│              │                     │                    │
│              └─────────────────────┘                    │
│                                                         │
│                                                         │
│                                                         │
│                                                         │
│                      v1.0                               │
└─────────────────────────────────────────────────────────┘
```

**Navigazione:** OK per procedere

---

### 1b. Popup Ripristino Torneo (se esiste sessione salvata)

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│          ┌───────────────────────────────┐              │
│          │                               │              │
│          │   Torneo in corso trovato!    │              │
│          │                               │              │
│          │   Livello 5 · 500/1.000       │              │
│          │   Tempo rimasto: 12:47        │              │
│          │                               │              │
│          │   ┌──────────┐  ┌──────────┐  │              │
│          │   │ RIPRENDI │  │  NUOVO   │  │              │
│          │   └──────────┘  └──────────┘  │              │
│          │                               │              │
│          └───────────────────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Comportamento:**
- Appare automaticamente all'avvio se esiste una sessione salvata
- "RIPRENDI" → Va direttamente al Timer, ripristina tutto
- "NUOVO" → Cancella la sessione salvata, va all'Editor con template pulito

---

### 2. Editor Struttura

```
┌─────────────────────────────────────────────────────────┐
│  ← INDIETRO                         STRUTTURA TORNEO    │
│─────────────────────────────────────────────────────────│
│                                                         │
│  ┌─────┬────────────┬────────────┬──────────┬───────┐   │
│  │ LV  │     SB     │     BB     │  DURATA  │       │   │
│  ├─────┼────────────┼────────────┼──────────┼───────┤   │
│  │  1  │     100    │     200    │  15 min  │ [DEL] │   │
│  │  2  │     200    │     400    │  15 min  │ [DEL] │   │
│  │  3  │     300    │     600    │  15 min  │ [DEL] │   │
│  │  4  │     400    │     800    │  15 min  │ [DEL] │   │
│  │  5  │     500    │   1.000    │  15 min  │ [DEL] │   │
│  │  6  │     600    │   1.200    │  10 min  │ [DEL] │   │
│  │  7  │     700    │   1.400    │  10 min  │ [DEL] │   │
│  │  8  │     800    │   1.600    │  10 min  │ [DEL] │   │
│  │ ... │    ...     │    ...     │   ...    │  ...  │   │
│  └─────┴────────────┴────────────┴──────────┴───────┘   │
│                                                         │
│  ┌─────────────────────┐       ┌─────────────────────┐  │
│  │  + AGGIUNGI LIVELLO │       │    ▶ AVVIA TIMER    │  │
│  └─────────────────────┘       └─────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Funzionalità:**
- Scroll verticale della lista livelli
- Selezione cella per modificarla (SB, BB, o Durata)
- Pulsante [DEL] per eliminare un livello
- Pulsante "+ AGGIUNGI LIVELLO" per aggiungere in coda
- Pulsante "AVVIA TIMER" per partire

**Navigazione:**
- Frecce Su/Giù: Scorre i livelli
- Frecce Sinistra/Destra: Muove tra le colonne (SB → BB → Durata → DEL)
- OK su cella: Apre popup modifica valore
- OK su DEL: Elimina il livello (con conferma)
- OK su AGGIUNGI: Aggiunge nuovo livello in coda
- OK su AVVIA: Parte il timer
- Back: Torna alla home

---

### 2b. Popup Modifica Valore (Smart Input)

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│          ┌───────────────────────────────┐              │
│          │       SMALL BLIND - LV.6      │              │
│          │───────────────────────────────│              │
│          │                               │              │
│          │   ┌───────┐  ┌───────┐        │              │
│          │   │  400  │  │  500  │        │              │
│          │   └───────┘  └───────┘        │              │
│          │   ┌───────┐  ┌───────┐        │              │
│          │   │  600  │  │  800  │ ←      │              │
│          │   └───────┘  └───────┘        │              │
│          │   ┌───────┐  ┌───────┐        │              │
│          │   │ 1000  │  │ 1500  │        │              │
│          │   └───────┘  └───────┘        │              │
│          │                               │              │
│          └───────────────────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Logica Smart Input per BLIND:**
Propone 6 valori basati sul contesto:
- Valore livello precedente
- Valore livello successivo (se esiste)
- Progressioni comuni: +100, +200, raddoppio, +50%
- Multipli dei tagli fiches (100, 500, 1000, 5000)

**Logica Smart Input per DURATA:**
Propone valori fissi: 5, 8, 10, 12, 15, 20 minuti

**Navigazione:** Frecce per muoversi, OK per confermare, Back per annullare

---

### 2c. Popup Conferma Eliminazione

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│          ┌───────────────────────────────┐              │
│          │                               │              │
│          │    Eliminare il livello 6?    │              │
│          │       (600 / 1.200)           │              │
│          │                               │              │
│          │   ┌─────────┐  ┌─────────┐    │              │
│          │   │   NO    │  │   SÌ    │    │              │
│          │   └─────────┘  └─────────┘    │              │
│          │                               │              │
│          └───────────────────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

### 2d. Popup Aggiungi Livello

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│          ┌───────────────────────────────┐              │
│          │       NUOVO LIVELLO (19)      │              │
│          │───────────────────────────────│              │
│          │                               │              │
│          │   Small Blind:   [30000]      │              │
│          │   Big Blind:     [60000]      │              │
│          │   Durata:        [10 min]     │              │
│          │                               │              │
│          │   ┌─────────────────────┐     │              │
│          │   │      AGGIUNGI       │     │              │
│          │   └─────────────────────┘     │              │
│          │                               │              │
│          └───────────────────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Valori default:** Raddoppio dell'ultimo livello esistente, stessa durata

---

### 3. Timer Principale

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                     LIVELLO 5                           │
│                                                         │
│                                                         │
│                                                         │
│                      12:47                              │
│                                                         │
│                                                         │
│                                                         │
│                   500 / 1.000                           │
│                                                         │
│                                                         │
│  ─────────────────────────────────────────────────────  │
│  PROSSIMO:  600 / 1.200                                 │
│                                                         │
│                                                         │
│         ◀◀           ⏸ PAUSA           ▶▶              │
└─────────────────────────────────────────────────────────┘
```

**Elementi:**
- LIVELLO: Numero livello corrente
- TIMER: Countdown grande e centrale (mm:ss)
- BLIND: Small / Big blind correnti
- PROSSIMO: Anteprima prossimo livello
- CONTROLLI: Indicazione visiva dei comandi disponibili

---

### 3b. Timer in Pausa

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                     LIVELLO 5                           │
│                                                         │
│                                                         │
│                       PAUSA                             │
│                      12:47                              │
│                                                         │
│                                                         │
│                                                         │
│                   500 / 1.000                           │
│                                                         │
│                                                         │
│  ─────────────────────────────────────────────────────  │
│  PROSSIMO:  600 / 1.200                                 │
│                                                         │
│                                                         │
│         ◀◀           ▶ PLAY            ▶▶              │
└─────────────────────────────────────────────────────────┘
```

**Effetto visivo:** Timer con effetto pulse/lampeggio lento, scritta "PAUSA" sopra il timer

---

### 3c. Popup Conferma Uscita

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│          ┌───────────────────────────────┐              │
│          │                               │              │
│          │    Vuoi uscire dal torneo?    │              │
│          │                               │              │
│          │   ┌──────────┐  ┌──────────┐  │              │
│          │   │ CONTINUA │  │   ESCI   │  │              │
│          │   └──────────┘  └──────────┘  │              │
│          │                               │              │
│          └───────────────────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Trigger:** Tasto Back durante il timer

---

## 🎮 Mappatura Telecomando

| Tasto              | Home       | Editor              | Timer                    |
|--------------------|------------|---------------------|--------------------------|
| OK                 | Avvia      | Modifica/Conferma   | -                        |
| Back               | Esci app   | Torna Home          | Popup conferma uscita    |
| Play/Pause         | -          | -                   | Pausa/Riprendi           |
| ▶▶ (Fast Forward) | -          | -                   | Livello successivo       |
| ◀◀ (Rewind)       | -          | -                   | Livello precedente       |
| Frecce             | -          | Naviga tabella      | -                        |

---

## 🔊 Audio

**2 soli suoni:**

| Evento                    | File                   | Descrizione                        |
|---------------------------|------------------------|------------------------------------|
| Manca 1 minuto            | `one_minute.mp3`       | Avviso discreto, breve             |
| Cambio livello            | `level_change.mp3`     | Gong/campana, più evidente         |

**Comportamento:**
- A 1:00 rimasto → suona `one_minute.mp3`
- A 0:00 → suona `level_change.mp3` e passa IMMEDIATAMENTE al livello successivo

---

## 🎨 Design System

### Palette Colori

| Elemento            | HEX       |
|---------------------|-----------|
| Sfondo principale   | #0D0D0D   |
| Sfondo card/popup   | #1A1A1A   |
| Accento oro         | #D4AF37   |
| Testo primario      | #FFFFFF   |
| Testo secondario    | #888888   |
| Focus/Selezione     | #FFD700   |
| Pericolo (elimina)  | #C41E3A   |

### Tipografia

| Elemento           | Dimensione | Peso     |
|--------------------|------------|----------|
| Timer countdown    | 160sp      | Bold     |
| Blind correnti     | 56sp       | Bold     |
| Livello            | 32sp       | Medium   |
| Prossimo livello   | 24sp       | Regular  |
| Testo tabella      | 22sp       | Regular  |
| Label/Caption      | 18sp       | Light    |

---

## 📊 Struttura Dati

### Template Hardcoded

```dart
final defaultStructure = [
  BlindLevel(1,  100,   200,   15),
  BlindLevel(2,  200,   400,   15),
  BlindLevel(3,  300,   600,   15),
  BlindLevel(4,  400,   800,   15),
  BlindLevel(5,  500,   1000,  15),
  BlindLevel(6,  600,   1200,  10),
  BlindLevel(7,  700,   1400,  10),
  BlindLevel(8,  800,   1600,  10),
  BlindLevel(9,  900,   1800,  10),
  BlindLevel(10, 1000,  2000,  10),
  BlindLevel(11, 2000,  4000,  10),
  BlindLevel(12, 3000,  6000,  10),
  BlindLevel(13, 4000,  8000,  10),
  BlindLevel(14, 5000,  10000, 10),
  BlindLevel(15, 10000, 20000, 10),
  BlindLevel(16, 15000, 30000, 10),
  BlindLevel(17, 20000, 40000, 10),
  BlindLevel(18, 25000, 50000, 10),
];
```

### Modello Dati

```dart
class BlindLevel {
  int levelNumber;
  int smallBlind;
  int bigBlind;
  int durationMinutes;
}

class SavedSession {
  int currentLevelIndex;      // Indice del livello corrente
  int remainingSeconds;       // Secondi rimasti nel countdown
  List<BlindLevel> structure; // Struttura completa (con eventuali modifiche)
  DateTime savedAt;           // Quando è stata salvata
}
```

**Persistenza:**
- `SavedSession` salvata in SharedPreferences come JSON
- Salvata automaticamente quando si esce dal timer
- Cancellata quando il torneo finisce (ultimo livello a 0:00) o si sceglie "NUOVO"

---

## 📱 Screen Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   HOME   │────▶│  POPUP   │────▶│  EDITOR  │────▶│  TIMER   │
│          │     │ Riprendi?│     │          │     │          │
└──────────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
                      │                │                │
                      │ (Riprendi)     │◀───── ESCI ────┘
                      │                │          │
                      └────────────────│──────────│───────┐
                                       │          │       │
                                  ┌────┴─────┐    │       ▼
                                  │  POPUP   │    │  [Salva stato]
                                  │ Modifica │    │
                                  │ Elimina  │    │
                                  │ Aggiungi │    │
                                  └──────────┘    │
                                                  │
                                            ┌─────┴──────┐
                                            │   POPUP    │
                                            │ Esci/Cont. │
                                            └────────────┘
```

**Flusso con sessione salvata:**
1. Avvio app → Home
2. Click "AVVIA TORNEO" → Popup "Riprendi?" (se esiste sessione)
3. "RIPRENDI" → Timer (stato ripristinato)
4. "NUOVO" → Editor (template pulito)

**Flusso senza sessione salvata:**
1. Avvio app → Home
2. Click "AVVIA TORNEO" → Editor (nessun popup)

---

## 📁 Struttura Progetto (Semplificata)

```
sblorftz_timer/
├── lib/
│   ├── main.dart
│   ├── models/
│   │   └── blind_level.dart
│   ├── data/
│   │   └── default_structure.dart
│   ├── screens/
│   │   ├── home_screen.dart
│   │   ├── editor_screen.dart
│   │   └── timer_screen.dart
│   ├── widgets/
│   │   ├── smart_input_popup.dart
│   │   ├── confirm_popup.dart
│   │   ├── add_level_popup.dart
│   │   └── resume_popup.dart
│   ├── services/
│   │   ├── audio_service.dart
│   │   └── session_service.dart
│   └── theme/
│       └── app_theme.dart
├── assets/
│   └── sounds/
│       ├── level_change.mp3
│       └── one_minute.mp3
└── pubspec.yaml (+ dipendenza: shared_preferences)
```

---

## 🚀 Roadmap v1.0

### Settimana 1
- [ ] Setup progetto Flutter per Android TV
- [ ] Configurazione AndroidManifest per Leanback
- [ ] Home screen
- [ ] Navigazione base con telecomando

### Settimana 2
- [ ] Editor struttura con tabella navigabile
- [ ] Smart input popup
- [ ] Funzioni aggiungi/elimina livello
- [ ] Template dati hardcoded

### Settimana 3
- [ ] Timer screen con countdown funzionante
- [ ] Controlli play/pausa/skip
- [ ] Integrazione suoni
- [ ] Test e fix su dispositivo reale

---

## 🧪 Checklist Test Finale

### Funzionalità
- [ ] Timer conta alla rovescia correttamente
- [ ] A 1:00 suona avviso
- [ ] A 0:00 suona cambio + passa al livello dopo (immediato)
- [ ] Pausa ferma il timer
- [ ] Skip avanti funziona
- [ ] Skip indietro funziona
- [ ] Modifica blind funziona
- [ ] Modifica durata funziona
- [ ] Elimina livello funziona
- [ ] Aggiungi livello in coda funziona
- [ ] Conferma uscita appare premendo Back

### Ripristino Sessione
- [ ] Uscendo dal timer, lo stato viene salvato
- [ ] Riavviando l'app, appare popup "Riprendi?"
- [ ] "Riprendi" ripristina livello e countdown esatti
- [ ] "Nuovo" cancella la sessione e apre editor pulito
- [ ] Fine torneo (ultimo livello finito) cancella la sessione
- [ ] La struttura modificata viene salvata insieme allo stato

### Navigazione
- [ ] Focus sempre visibile
- [ ] Tutti gli elementi raggiungibili
- [ ] Back comportamento corretto ovunque
- [ ] Tasti speciali telecomando mappati

### Display
- [ ] Timer leggibile da 3+ metri
- [ ] Niente tagliato ai bordi
- [ ] Popup centrati e leggibili

---

## 🔮 Future v2.0 (Per Dopo)

- Visualizzazione fiches colorate
- Salvataggio strutture multiple
- Impostazioni (volume, scelta suoni)
- Temi alternativi
- Statistiche torneo

---

*Sblorftz Timer v1.0 - Documento di Progetto*
*Keep it simple, play poker! ♠♥♣♦*
