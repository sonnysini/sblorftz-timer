# Sblorftz Timer v1.2 Beta - Piano Modifiche

## Obiettivo

Implementare un sistema di selezione strutture con 3 opzioni:
1. **STANDARD** - Struttura attuale 18 livelli, durate 15-10 min (sola lettura)
2. **VELOCE** - 18 livelli identici alla STANDARD, tutti a 10 min (sola lettura)
3. **CUSTOM** - Creata dall'utente da zero, modificabile e persistente

---

## Flusso Utente

```
HOME
├── "AVVIA TORNEO" ──────────────────────▶ Timer (usa struttura SELEZIONATA)
└── "PERSONALIZZA" ──▶ SELETTORE STRUTTURE
                            │
                            ├── [STANDARD] ──▶ Seleziona ✓
                            ├── [VELOCE] ──▶ Seleziona ✓
                            └── [CUSTOM] ──▶ Apre EDITOR CUSTOM
                                               │
                                               ├── Crea livelli da zero
                                               ├── Modifica SB e DURATA
                                               ├── Elimina livelli
                                               └── [SALVA] ──▶ Torna al selettore
```

---

## Schermata: Selettore Strutture

```
┌─────────────────────────────────────────────────────────┐
│  ← INDIETRO                      SCEGLI STRUTTURA       │
│─────────────────────────────────────────────────────────│
│                                                         │
│     ┌─────────────────────────────────────────────┐     │
│     │  ★ STANDARD                          [✓]    │     │
│     │     18 livelli · 15-10 min · 100/200 start  │     │
│     └─────────────────────────────────────────────┘     │
│                                                         │
│     ┌─────────────────────────────────────────────┐     │
│     │  ⚡ VELOCE                            [ ]    │     │
│     │     18 livelli · 10 min · 100/200 start     │     │
│     └─────────────────────────────────────────────┘     │
│                                                         │
│     ┌─────────────────────────────────────────────┐     │
│     │  ✎ CUSTOM                            [ ]    │     │
│     │     0 livelli · Premi OK per creare         │     │
│     └─────────────────────────────────────────────┘     │
│                                                         │
│            La struttura selezionata verrà               │
│            usata per il prossimo torneo                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Navigazione Selettore

| Tasto | Azione |
|-------|--------|
| **Frecce Su/Giù** | Naviga tra le 3 cards |
| **OK su STANDARD** | Seleziona quella struttura ✓ |
| **OK su VELOCE** | Seleziona quella struttura ✓ |
| **OK su CUSTOM** | Apre l'editor CUSTOM |
| **Back** | Torna alla Home |

### Comportamento Cards

- **STANDARD**: OK → seleziona come attiva
- **VELOCE**: OK → seleziona come attiva
- **CUSTOM**: OK → apre editor (se ha almeno 1 livello, può anche essere selezionata con freccia DX)

### Indicatore Selezione

La card selezionata mostra [✓] e bordo oro. L'altra mostra [ ] e bordo grigio.

### Card CUSTOM - Stati

| Stato | Descrizione mostrata |
|-------|---------------------|
| 0 livelli | "0 livelli · Premi OK per creare" |
| 1+ livelli | "X livelli · SB/BB start · Y min totali" |

---

## Schermata: Editor CUSTOM

### Layout - Lista Vuota (primo accesso)

```
┌─────────────────────────────────────────────────────────┐
│  ← INDIETRO                       STRUTTURA CUSTOM      │
│─────────────────────────────────────────────────────────│
│                                                         │
│                                                         │
│                                                         │
│                   Nessun livello                        │
│                                                         │
│              Premi OK per aggiungere                    │
│               il primo livello                          │
│                                                         │
│                                                         │
│                                                         │
│              ┌─────────────────────┐                    │
│              │   + AGGIUNGI LV.1   │  ← Focus           │
│              └─────────────────────┘                    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Layout - Con Livelli Esistenti (Lista Compatta Verticale)

```
┌─────────────────────────────────────────────────────────┐
│  ← INDIETRO                       STRUTTURA CUSTOM      │
│─────────────────────────────────────────────────────────│
│                                                         │
│     LV.1    100 / 200      10 min              [✕]      │
│  ┌──────────────────────────────────────────────────┐   │
│  │► LV.2    200 / 400      10 min              [✕]  │   │
│  └──────────────────────────────────────────────────┘   │
│     LV.3    300 / 600       8 min              [✕]      │
│     LV.4    500 / 1000      8 min              [✕]      │
│     LV.5   1000 / 2000      6 min              [✕]      │
│                                                         │
│              ┌─────────────────────┐                    │
│              │   + AGGIUNGI LV.6   │                    │
│              └─────────────────────┘                    │
│                                                         │
│  ┌─────────────┐                      ┌─────────────┐   │
│  │    SALVA    │                      │    RESET    │   │
│  └─────────────┘                      └─────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Struttura Riga Livello

```
     LV.X    [SB] / [BB]      [DURATA]              [✕]
             ▲                 ▲                     ▲
             │                 │                     │
         Modificabile     Modificabile          Elimina
         (OK apre popup)  (OK apre popup)
         
     BB = SB × 2 (automatico, non modificabile direttamente)
```

### Navigazione Editor

| Tasto | Azione |
|-------|--------|
| **Frecce Su/Giù** | Naviga tra righe livelli e pulsanti sotto |
| **Frecce Sx/Dx** | Naviga tra: SB → DURATA → [✕] nella riga selezionata |
| **OK su SB** | Apre popup modifica Small Blind |
| **OK su DURATA** | Apre popup modifica durata |
| **OK su [✕]** | Elimina livello (con popup conferma) |
| **OK su AGGIUNGI** | Apre popup nuovo livello |
| **OK su SALVA** | Salva struttura e torna al selettore |
| **OK su RESET** | Popup conferma → svuota tutti i livelli |
| **Back** | Se modifiche non salvate → popup / Altrimenti → torna indietro |

### Focus nella Riga

Quando una riga è selezionata, il focus può spostarsi tra 3 elementi:

```
┌──────────────────────────────────────────────────────┐
│► LV.2   [200] / 400     [10 min]             [✕]     │
│          ▲                 ▲                  ▲      │
│        focus 1          focus 2            focus 3   │
└──────────────────────────────────────────────────────┘
```

- **Focus 1 (SB)**: Evidenziato, OK apre popup modifica SB
- **Focus 2 (DURATA)**: Evidenziato, OK apre popup modifica durata
- **Focus 3 ([✕])**: Evidenziato in rosso, OK elimina con conferma

Il BB (400 nell'esempio) NON è selezionabile perché è sempre SB × 2.

---

## Popup: Aggiungi Nuovo Livello

### Step 1 - Scelta Small Blind

```
┌───────────────────────────────────────┐
│                                       │
│         NUOVO LIVELLO 5               │
│                                       │
│           SMALL BLIND                 │
│                                       │
│         ┌─────────────┐               │
│         │     500     │  ← Focus      │
│         └─────────────┘               │
│                                       │
│            ▲ +100                     │
│            ▼ (min)                    │
│                                       │
│         Premi OK per confermare       │
│                                       │
└───────────────────────────────────────┘
```

**Logica Step 1:**

| Condizione | Valore iniziale | Incremento | Minimo |
|------------|-----------------|------------|--------|
| Primo livello (LV.1) | 100 | ±100 | 100 |
| SB precedente < 900 | SB prec + 100 | ±100 | SB prec + 100 |
| SB precedente = 900 | 1000 | ±500 | 1000 |
| SB precedente ≥ 1000 | SB prec + 500 | ±500 | SB prec + 500 |

**Regola incrementi dinamica:**
- Se valore corrente < 1000 → incremento ±100
- Se valore corrente ≥ 1000 → incremento ±500

**Vincolo fondamentale:** Il nuovo SB deve essere SEMPRE > SB del livello precedente. Freccia GIÙ bloccata se già al minimo consentito.

**Navigazione:**
- Freccia SU: aumenta valore (con incremento appropriato)
- Freccia GIÙ: diminuisce valore (se > minimo)
- OK: conferma, calcola BB automaticamente, passa a Step 2
- Back: annulla e chiude popup

### Step 2 - Scelta Durata (BB mostrato automaticamente)

```
┌───────────────────────────────────────┐
│                                       │
│         NUOVO LIVELLO 5               │
│                                       │
│      SB: 500  →  BB: 1000 ✓           │
│                                       │
│             DURATA                    │
│                                       │
│         ┌─────────────┐               │
│         │   10 min    │  ← Focus      │
│         └─────────────┘               │
│                                       │
│            ▲ +1 min                   │
│            ▼ -1 min                   │
│                                       │
│         Premi OK per confermare       │
│                                       │
└───────────────────────────────────────┘
```

**Logica Step 2:**
- BB mostrato automaticamente: SB × 2 (informativo, non modificabile)
- Durata iniziale: 10 minuti
- Incremento durata: ±1 minuto
- Durata minima: 1 minuto
- Durata massima: 60 minuti

**Navigazione:**
- Freccia SU: +1 minuto
- Freccia GIÙ: -1 minuto (se > 1)
- OK: crea livello e chiude popup
- Back: torna a Step 1

---

## Popup: Modifica Small Blind Esistente

```
┌───────────────────────────────────────┐
│                                       │
│       MODIFICA LIVELLO 3              │
│                                       │
│           SMALL BLIND                 │
│                                       │
│         ┌─────────────┐               │
│         │     300     │  ← Focus      │
│         └─────────────┘               │
│                                       │
│            ▲ +100                     │
│            ▼ -100                     │
│                                       │
│         Premi OK per confermare       │
│                                       │
└───────────────────────────────────────┘
```

**Vincoli modifica SB:**
- Deve essere > SB del livello precedente (se esiste)
- Deve essere < SB del livello successivo (se esiste)
- Incremento ±100 se valore < 1000, ±500 se valore ≥ 1000
- BB viene aggiornato automaticamente (SB × 2) alla conferma

**Navigazione:**
- Freccia SU: aumenta valore
- Freccia GIÙ: diminuisce valore (rispettando i vincoli)
- OK: conferma modifica, aggiorna BB automaticamente
- Back: annulla

---

## Popup: Modifica Durata Esistente

```
┌───────────────────────────────────────┐
│                                       │
│       MODIFICA LIVELLO 3              │
│                                       │
│             DURATA                    │
│                                       │
│         ┌─────────────┐               │
│         │   10 min    │  ← Focus      │
│         └─────────────┘               │
│                                       │
│            ▲ +1 min                   │
│            ▼ -1 min                   │
│                                       │
│         Premi OK per confermare       │
│                                       │
└───────────────────────────────────────┘
```

**Vincoli modifica durata:**
- Minimo: 1 minuto
- Massimo: 60 minuti
- Incremento: ±1 minuto

**Navigazione:**
- Freccia SU: +1 minuto
- Freccia GIÙ: -1 minuto (se > 1)
- OK: conferma modifica
- Back: annulla

---

## Popup: Conferma Eliminazione Livello

```
┌───────────────────────────────────────┐
│                                       │
│      Eliminare il livello 3?          │
│         (300 / 600)                   │
│                                       │
│      ┌────────┐     ┌────────┐        │
│      │   NO   │     │   SÌ   │        │
│      └────────┘     └────────┘        │
│                                       │
└───────────────────────────────────────┘
```

- Focus iniziale su "NO" (scelta sicura)
- Dopo eliminazione, i livelli vengono rinumerati automaticamente

---

## Popup: Conferma Reset

```
┌───────────────────────────────────────┐
│                                       │
│      Eliminare tutti i livelli?       │
│                                       │
│      ┌────────┐     ┌────────┐        │
│      │   NO   │     │   SÌ   │        │
│      └────────┘     └────────┘        │
│                                       │
└───────────────────────────────────────┘
```

- Focus iniziale su "NO" (scelta sicura)

---

## Popup: Modifiche Non Salvate

Appare quando l'utente preme Back con modifiche pendenti:

```
┌───────────────────────────────────────┐
│                                       │
│       Hai modifiche non salvate       │
│                                       │
│   ┌─────────┐  ┌─────────┐  ┌─────┐   │
│   │ ANNULLA │  │  SALVA  │  │ESCI │   │
│   └─────────┘  └─────────┘  └─────┘   │
│                                       │
└───────────────────────────────────────┘
```

- **ANNULLA**: Torna all'editor (continua a modificare)
- **SALVA**: Salva e torna al selettore
- **ESCI**: Esce senza salvare (perde modifiche)
- Focus iniziale su "ANNULLA" (scelta sicura)

---

## Persistenza Dati

### StructureService

Nuovo service per gestire le strutture:

```dart
class StructureService {
  // Chiavi SharedPreferences
  static const _selectedKey = 'selected_structure';    // "standard" | "veloce" | "custom"
  static const _customKey = 'custom_structure';        // JSON della struttura custom

  // Lettura
  Future<String> getSelectedType();                    // Default: "standard"
  Future<List<BlindLevel>> getCustomStructure();       // Default: lista vuota []
  Future<List<BlindLevel>> getActiveStructure();       // Ritorna la struttura selezionata
  
  // Scrittura
  Future<void> setSelectedType(String type);
  Future<void> saveCustomStructure(List<BlindLevel> structure);
  Future<void> clearCustomStructure();
  
  // Utility
  Future<bool> hasCustomStructure();                   // true se custom ha almeno 1 livello
}
```

### Logica getActiveStructure()

```dart
Future<List<BlindLevel>> getActiveStructure() async {
  final type = await getSelectedType();
  
  if (type == "custom") {
    final custom = await getCustomStructure();
    if (custom.isNotEmpty) {
      return custom;
    }
    // Fallback a standard se custom è vuota
    return getDefaultStructure();
  }
  
  if (type == "veloce") {
    return getVeloceStructure();
  }
  
  return getDefaultStructure();
}
```

### Struttura VELOCE

Identica alla STANDARD ma con tutti i livelli a 10 minuti:

```dart
List<BlindLevel> getVeloceStructure() {
  return [
    BlindLevel(levelNumber: 1,  smallBlind: 100,   bigBlind: 200,   durationMinutes: 10),
    BlindLevel(levelNumber: 2,  smallBlind: 200,   bigBlind: 400,   durationMinutes: 10),
    BlindLevel(levelNumber: 3,  smallBlind: 300,   bigBlind: 600,   durationMinutes: 10),
    BlindLevel(levelNumber: 4,  smallBlind: 400,   bigBlind: 800,   durationMinutes: 10),
    BlindLevel(levelNumber: 5,  smallBlind: 500,   bigBlind: 1000,  durationMinutes: 10),
    BlindLevel(levelNumber: 6,  smallBlind: 600,   bigBlind: 1200,  durationMinutes: 10),
    BlindLevel(levelNumber: 7,  smallBlind: 700,   bigBlind: 1400,  durationMinutes: 10),
    BlindLevel(levelNumber: 8,  smallBlind: 800,   bigBlind: 1600,  durationMinutes: 10),
    BlindLevel(levelNumber: 9,  smallBlind: 900,   bigBlind: 1800,  durationMinutes: 10),
    BlindLevel(levelNumber: 10, smallBlind: 1000,  bigBlind: 2000,  durationMinutes: 10),
    BlindLevel(levelNumber: 11, smallBlind: 2000,  bigBlind: 4000,  durationMinutes: 10),
    BlindLevel(levelNumber: 12, smallBlind: 3000,  bigBlind: 6000,  durationMinutes: 10),
    BlindLevel(levelNumber: 13, smallBlind: 4000,  bigBlind: 8000,  durationMinutes: 10),
    BlindLevel(levelNumber: 14, smallBlind: 5000,  bigBlind: 10000, durationMinutes: 10),
    BlindLevel(levelNumber: 15, smallBlind: 10000, bigBlind: 20000, durationMinutes: 10),
    BlindLevel(levelNumber: 16, smallBlind: 15000, bigBlind: 30000, durationMinutes: 10),
    BlindLevel(levelNumber: 17, smallBlind: 20000, bigBlind: 40000, durationMinutes: 10),
    BlindLevel(levelNumber: 18, smallBlind: 25000, bigBlind: 50000, durationMinutes: 10),
  ];
}
// Durata totale: 180 minuti (3 ore)
```

---

## File da Creare/Modificare

| File | Azione | Descrizione |
|------|--------|-------------|
| `lib/services/structure_service.dart` | **NUOVO** | Gestione persistenza strutture |
| `lib/data/veloce_structure.dart` | **NUOVO** | Template struttura veloce (18 livelli, tutti 10 min) |
| `lib/screens/structure_selector_screen.dart` | **NUOVO** | Selettore STANDARD/VELOCE/CUSTOM |
| `lib/screens/custom_editor_screen.dart` | **NUOVO** | Editor dedicato per CUSTOM |
| `lib/widgets/spinner_input_popup.dart` | **NUOVO** | Popup con spinner su/giù |
| `lib/widgets/structure_card.dart` | **NUOVO** | Card per ogni struttura nel selettore |
| `lib/widgets/level_row.dart` | **NUOVO** | Riga livello con focus su SB/DURATA/[✕] |
| `lib/widgets/unsaved_changes_popup.dart` | **NUOVO** | Popup modifiche non salvate |
| `lib/screens/home_screen.dart` | **MODIFICA** | Collegare "PERSONALIZZA" al selettore |
| `lib/screens/editor_screen.dart` | **RIMUOVERE** | Sostituito da custom_editor_screen |
| `pubspec.yaml` | **MODIFICA** | Aggiornare versione a 1.2.0 |

---

## Ordine di Implementazione

### Fase 1: Infrastruttura
1. `structure_service.dart` - Service persistenza
2. `veloce_structure.dart` - Template struttura veloce

### Fase 2: Widget Riutilizzabili
3. `spinner_input_popup.dart` - Popup con frecce su/giù
4. `structure_card.dart` - Card per selettore
5. `level_row.dart` - Riga livello navigabile
6. `unsaved_changes_popup.dart` - Popup modifiche non salvate

### Fase 3: Schermate
7. `structure_selector_screen.dart` - Selettore strutture
8. `custom_editor_screen.dart` - Editor custom

### Fase 4: Integrazione
9. Modificare `home_screen.dart` - Collegare nuovo flusso
10. Rimuovere/deprecare `editor_screen.dart`
11. Aggiornare versione in `pubspec.yaml`

### Fase 5: Test
12. Test navigazione telecomando
13. Test persistenza dati
14. Test casi limite (lista vuota, valori minimi/massimi)

---

## Casi Limite da Gestire

1. **CUSTOM vuota selezionata**: Non permettere di selezionare CUSTOM se ha 0 livelli. Mostrare messaggio "Crea almeno un livello"
2. **Eliminazione ultimo livello**: Torna allo stato "lista vuota"
3. **Modifica SB che rompe la sequenza**: Validare che SB[n] > SB[n-1] e SB[n] < SB[n+1]
4. **Sessione salvata con struttura CUSTOM eliminata**: Se la sessione usa CUSTOM che è stata svuotata, fallback automatico a STANDARD

---

## Regole Business Fondamentali

### BB sempre automatico
```
BB = SB × 2
```
Il Big Blind NON è mai modificabile direttamente. Viene calcolato automaticamente.

### Incrementi SB
```
Se SB < 1000  → incremento ±100
Se SB ≥ 1000  → incremento ±500
```

### Vincoli SB
```
SB[livello N] > SB[livello N-1]
SB[livello N] < SB[livello N+1]
SB minimo assoluto: 100
```

### Vincoli Durata
```
Minimo: 1 minuto
Massimo: 60 minuti
Incremento: ±1 minuto
Default: 10 minuti
```

---

## Note Tecniche

### Focus Management Android TV
- Ogni popup deve usare `FocusScope` con `autofocus: true`
- I pulsanti devono usare `FocusableButton` esistente
- Le righe livello devono gestire focus interno (SB → DURATA → [✕])
- Testare navigazione con D-pad

### Scroll Lista
- Se i livelli superano lo spazio visibile, la lista deve scrollare
- Il livello selezionato deve sempre essere visibile
- Usare `ScrollController` per gestire lo scroll automatico

---

*Piano v1.2 Beta - Pronto per implementazione*
