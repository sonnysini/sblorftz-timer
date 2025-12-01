import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/saved_session.dart';

class SessionService {
  static const String _sessionKey = 'saved_tournament_session';

  Future<void> saveSession(SavedSession session) async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = jsonEncode(session.toJson());
    await prefs.setString(_sessionKey, jsonString);
  }

  Future<SavedSession?> loadSession() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(_sessionKey);

    if (jsonString == null) {
      return null;
    }

    try {
      final jsonMap = jsonDecode(jsonString) as Map<String, dynamic>;
      return SavedSession.fromJson(jsonMap);
    } catch (e) {
      return null;
    }
  }

  Future<void> clearSession() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_sessionKey);
  }

  Future<bool> hasSession() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.containsKey(_sessionKey);
  }
}
