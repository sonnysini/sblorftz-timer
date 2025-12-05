import '../models/blind_level.dart';

/// Struttura VELOCE: 18 livelli identici alla STANDARD ma tutti a 10 minuti
/// Durata totale: 180 minuti (3 ore)
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
