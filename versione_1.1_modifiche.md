## Contesto
Ho bisogno di implementare due modifiche all'app che stiamo sviluppando:

---

## MODIFICA 1: Ristrutturazione Home Screen

### Situazione attuale
La Home Screen ha un solo pulsante "AVVIA TORNEO" che porta all'Editor Struttura, e da lì si avvia il timer.

### Nuova struttura richiesta
La Home Screen deve avere DUE pulsanti:

1. **"AVVIA TORNEO"** (pulsante principale, focus di default)
   - Avvia DIRETTAMENTE il timer con la struttura corrente (template default o ultima modificata)
   - Salta completamente l'editor
   
2. **"PERSONALIZZA** (pulsante secondario, sotto il primo)
   - Apre l'Editor Struttura per modificare i livelli
   - Dall'editor, viene modificata dall'utente la struttura dei bui, quindi un bottone SALVA salverà la nuova struttura.

### Nuovo flusso
```
HOME
├── "AVVIA TORNEO" ────────────────────────▶ TIMER (diretto)
└── "PERSONALIZZA BUI" ──▶ EDITOR ──▶ SALVA E TORNA ALLA HOME
```

### Layout Home Screen desiderato
```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                    SBLORFTZ TIMER                       │
│                        ♠ ♥ ♣ ♦                          │
│                                                         │
│              ┌─────────────────────┐                    │
│              │   ▶ AVVIA TORNEO    │  ← Focus default   │
│              └─────────────────────┘                    │
│              ┌─────────────────────┐                    │
│              │     PERSONALIZZA    │                    │
│              └─────────────────────┘                    │
│                                                         │
│                      v1.0                               │
└─────────────────────────────────────────────────────────┘
```

### Note implementative
- Navigazione: frecce Su/Giù per spostarsi tra i due pulsanti
- Il popup "Riprendi torneo?" (se esiste sessione salvata) deve apparire SOLO quando si clicca "AVVIA TORNEO", non quando si va in "PERSONALIZZA BUI"
- Se l'utente sceglie "NUOVO" dal popup di ripristino, va comunque direttamente al timer (con struttura resettata al template)

---

## MODIFICA 2: Fix Bug Popup Conferma Uscita (CRITICO)

### Problema
Quando sono nel Timer e premo il tasto BACK del telecomando, appare il popup di conferma uscita con i pulsanti "CONTINUA" e "ESCI", ma NON RIESCO A SELEZIONARE i pulsanti con il telecomando. Il focus sembra non funzionare correttamente sui pulsanti del popup.

### Popup interessato
```
┌───────────────────────────────┐
│                               │
│    Vuoi uscire dal torneo?    │
│                               │
│   ┌──────────┐  ┌──────────┐  │
│   │ CONTINUA │  │   ESCI   │  │
│   └──────────┘  └──────────┘  │
│                               │
└───────────────────────────────┘
```

### Comportamento atteso
- Quando il popup appare, il focus deve essere su "CONTINUA" (default sicuro)
- Con freccia DESTRA → focus si sposta su "ESCI"
- Con freccia SINISTRA → focus torna su "CONTINUA"
- Con tasto OK → conferma il pulsante selezionato
- Con tasto BACK → chiude il popup e torna al timer (equivale a "CONTINUA")

### Possibili cause da investigare
1. Il popup potrebbe non richiedere correttamente il focus quando appare
2. I pulsanti potrebbero non essere avvolti in widget focusabili (Focus, FocusableActionDetector, ecc.)
3. Potrebbe mancare un FocusScope per isolare il focus nel popup
4. Su Android TV i Dialog/AlertDialog standard potrebbero avere problemi di focus

### Suggerimenti per il fix
Per Android TV, considera di:
- Usare `FocusScope` per contenere il popup
- Usare `autofocus: true` sul primo pulsante
- Assicurarsi che i pulsanti siano di tipo `ElevatedButton` o widget con `focusNode` esplicito
- Richiedere il focus programmaticamente con `FocusScope.of(context).requestFocus(focusNode)` quando il popup appare
- Verificare che il popup non stia bloccando gli eventi di input

### Esempio di struttura corretta per TV
```dart
showDialog(
  context: context,
  builder: (context) => FocusScope(
    autofocus: true,
    child: AlertDialog(
      title: Text('Vuoi uscire dal torneo?'),
      actions: [
        ElevatedButton(
          autofocus: true,  // Focus iniziale qui
          onPressed: () => Navigator.pop(context, false),
          child: Text('CONTINUA'),
        ),
        ElevatedButton(
          onPressed: () => Navigator.pop(context, true),
          child: Text('ESCI'),
        ),
      ],
    ),
  ),
);
```

---

## Riepilogo modifiche

| # | Tipo | Descrizione |
|---|------|-------------|
| 1 | Feature | Aggiungere pulsante "PERSONALIZZA" in Home, "AVVIA TORNEO" va diretto al timer |
| 2 | Bug Fix | Il popup conferma uscita non permette di selezionare i pulsanti con telecomando|

---

## File probabilmente coinvolti
- `lib/screens/home_screen.dart` (modifica 1)
- `lib/screens/timer_screen.dart` (modifica 2 - popup uscita)
- `lib/widgets/confirm_popup.dart` (modifica 2 - se il popup è un widget separato)
