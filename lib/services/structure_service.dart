import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/blind_level.dart';
import '../data/default_structure.dart';
import '../data/veloce_structure.dart';

class StructureService {
  static const String _selectedKey = 'selected_structure';
  static const String _customKey = 'custom_structure';

  // Tipi di struttura
  static const String typeStandard = 'standard';
  static const String typeVeloce = 'veloce';
  static const String typeCustom = 'custom';

  /// Ottiene il tipo di struttura selezionata (default: "standard")
  Future<String> getSelectedType() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_selectedKey) ?? typeStandard;
  }

  /// Imposta il tipo di struttura selezionata
  Future<void> setSelectedType(String type) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_selectedKey, type);
  }

  /// Ottiene la struttura custom salvata (default: lista vuota)
  Future<List<BlindLevel>> getCustomStructure() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(_customKey);

    if (jsonString == null) {
      return [];
    }

    try {
      final jsonList = jsonDecode(jsonString) as List;
      return jsonList
          .map((item) => BlindLevel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Salva la struttura custom
  Future<void> saveCustomStructure(List<BlindLevel> structure) async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = jsonEncode(structure.map((level) => level.toJson()).toList());
    await prefs.setString(_customKey, jsonString);
  }

  /// Cancella la struttura custom
  Future<void> clearCustomStructure() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_customKey);
  }

  /// Verifica se esiste una struttura custom con almeno un livello
  Future<bool> hasCustomStructure() async {
    final custom = await getCustomStructure();
    return custom.isNotEmpty;
  }

  /// Ottiene la struttura attualmente attiva
  Future<List<BlindLevel>> getActiveStructure() async {
    final type = await getSelectedType();

    if (type == typeCustom) {
      final custom = await getCustomStructure();
      if (custom.isNotEmpty) {
        return custom;
      }
      // Fallback a standard se custom è vuota
      return getDefaultStructure();
    }

    if (type == typeVeloce) {
      return getVeloceStructure();
    }

    return getDefaultStructure();
  }

  /// Calcola la durata totale di una struttura in minuti
  int getTotalDuration(List<BlindLevel> structure) {
    return structure.fold(0, (sum, level) => sum + level.durationMinutes);
  }

  /// Ottiene i blinds iniziali di una struttura (primo livello)
  String getStartingBlinds(List<BlindLevel> structure) {
    if (structure.isEmpty) return '0/0';
    final first = structure.first;
    return '${first.smallBlind}/${first.bigBlind}';
  }
}
