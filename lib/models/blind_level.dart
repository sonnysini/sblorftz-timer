class BlindLevel {
  int levelNumber;
  int smallBlind;
  int bigBlind;
  int durationMinutes;

  BlindLevel({
    required this.levelNumber,
    required this.smallBlind,
    required this.bigBlind,
    required this.durationMinutes,
  });

  Map<String, dynamic> toJson() => {
        'levelNumber': levelNumber,
        'smallBlind': smallBlind,
        'bigBlind': bigBlind,
        'durationMinutes': durationMinutes,
      };

  factory BlindLevel.fromJson(Map<String, dynamic> json) => BlindLevel(
        levelNumber: json['levelNumber'] as int,
        smallBlind: json['smallBlind'] as int,
        bigBlind: json['bigBlind'] as int,
        durationMinutes: json['durationMinutes'] as int,
      );

  BlindLevel copyWith({
    int? levelNumber,
    int? smallBlind,
    int? bigBlind,
    int? durationMinutes,
  }) {
    return BlindLevel(
      levelNumber: levelNumber ?? this.levelNumber,
      smallBlind: smallBlind ?? this.smallBlind,
      bigBlind: bigBlind ?? this.bigBlind,
      durationMinutes: durationMinutes ?? this.durationMinutes,
    );
  }
}
