import 'package:audioplayers/audioplayers.dart';

class AudioService {
  final AudioPlayer _player = AudioPlayer();

  Future<void> playOneMinuteWarning() async {
    try {
      await _player.play(AssetSource('sounds/one_minute.mp3'));
    } catch (e) {
      // Gestione errori silenziosa
    }
  }

  Future<void> playLevelChange() async {
    try {
      await _player.play(AssetSource('sounds/level_change.mp3'));
    } catch (e) {
      // Gestione errori silenziosa
    }
  }

  void dispose() {
    _player.dispose();
  }
}
