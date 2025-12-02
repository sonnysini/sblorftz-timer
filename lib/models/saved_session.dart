import 'blind_level.dart';

class SavedSession {
  int currentLevelIndex;
  int remainingSeconds;
  List<BlindLevel> structure;
  DateTime savedAt;
  int? previousLevelIndex;
  int? previousRemainingSeconds;

  SavedSession({
    required this.currentLevelIndex,
    required this.remainingSeconds,
    required this.structure,
    required this.savedAt,
    this.previousLevelIndex,
    this.previousRemainingSeconds,
  });

  Map<String, dynamic> toJson() => {
        'currentLevelIndex': currentLevelIndex,
        'remainingSeconds': remainingSeconds,
        'structure': structure.map((level) => level.toJson()).toList(),
        'savedAt': savedAt.toIso8601String(),
        'previousLevelIndex': previousLevelIndex,
        'previousRemainingSeconds': previousRemainingSeconds,
      };

  factory SavedSession.fromJson(Map<String, dynamic> json) => SavedSession(
        currentLevelIndex: json['currentLevelIndex'] as int,
        remainingSeconds: json['remainingSeconds'] as int,
        structure: (json['structure'] as List)
            .map((e) => BlindLevel.fromJson(e as Map<String, dynamic>))
            .toList(),
        savedAt: DateTime.parse(json['savedAt'] as String),
        previousLevelIndex: json['previousLevelIndex'] as int?,
        previousRemainingSeconds: json['previousRemainingSeconds'] as int?,
      );
}
