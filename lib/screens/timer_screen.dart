import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../models/blind_level.dart';
import '../models/saved_session.dart';
import '../services/audio_service.dart';
import '../services/session_service.dart';

class TimerScreen extends StatefulWidget {
  final List<BlindLevel> structure;
  final int initialLevelIndex;
  final int initialRemainingSeconds;

  const TimerScreen({
    super.key,
    required this.structure,
    this.initialLevelIndex = 0,
    this.initialRemainingSeconds = 0,
  });

  @override
  State<TimerScreen> createState() => _TimerScreenState();
}

class _TimerScreenState extends State<TimerScreen> {
  late int currentLevelIndex;
  late int remainingSeconds;
  bool isPaused = true;
  Timer? _timer;
  final AudioService _audioService = AudioService();
  final SessionService _sessionService = SessionService();
  bool _oneMinuteWarningPlayed = false;
  bool _tenSecondsWarningPlayed = false;

  @override
  void initState() {
    super.initState();
    currentLevelIndex = widget.initialLevelIndex;

    if (widget.initialRemainingSeconds > 0) {
      remainingSeconds = widget.initialRemainingSeconds;
    } else {
      remainingSeconds = widget.structure[currentLevelIndex].durationMinutes * 60;
    }

    // Attiva wakelock per mantenere lo schermo sempre acceso
    WakelockPlus.enable();

    _startTimer();
  }

  void _startTimer() {
    setState(() => isPaused = false);
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (remainingSeconds > 0) {
        setState(() {
          remainingSeconds--;
        });

        if (remainingSeconds == 60 && !_oneMinuteWarningPlayed) {
          _audioService.playOneMinuteWarning();
          _oneMinuteWarningPlayed = true;
        }

        if (remainingSeconds == 10 && !_tenSecondsWarningPlayed) {
          _audioService.playTenSecondsWarning();
          _tenSecondsWarningPlayed = true;
        }

        if (remainingSeconds == 0) {
          _audioService.playLevelChange();
          _nextLevel();
        }

        _saveSession();
      }
    });
  }

  void _pauseTimer() {
    setState(() => isPaused = true);
    _timer?.cancel();
    _saveSession();
  }

  void _togglePause() {
    if (isPaused) {
      _startTimer();
    } else {
      _pauseTimer();
    }
  }

  void _nextLevel() {
    if (currentLevelIndex < widget.structure.length - 1) {
      setState(() {
        currentLevelIndex++;
        remainingSeconds = widget.structure[currentLevelIndex].durationMinutes * 60;
        _oneMinuteWarningPlayed = false;
        _tenSecondsWarningPlayed = false;
      });
      _saveSession();
    } else {
      // Torneo terminato: disattiva wakelock
      _sessionService.clearSession();
      _timer?.cancel();
      WakelockPlus.disable();
    }
  }

  void _previousLevel() {
    if (currentLevelIndex > 0) {
      setState(() {
        currentLevelIndex--;
        remainingSeconds = widget.structure[currentLevelIndex].durationMinutes * 60;
        _oneMinuteWarningPlayed = false;
        _tenSecondsWarningPlayed = false;
      });
      _saveSession();
    }
  }

  Future<void> _saveSession() async {
    final session = SavedSession(
      currentLevelIndex: currentLevelIndex,
      remainingSeconds: remainingSeconds,
      structure: widget.structure,
      savedAt: DateTime.now(),
    );
    await _sessionService.saveSession(session);
  }

  void _showExitDialog() {
    _pauseTimer();
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => PopScope(
        canPop: true,
        onPopInvokedWithResult: (didPop, result) {
          if (didPop) {
            _startTimer();
          }
        },
        child: FocusScope(
          autofocus: true,
          child: Dialog(
            child: Container(
              padding: const EdgeInsets.all(32),
              constraints: const BoxConstraints(maxWidth: 500),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Vuoi uscire dal torneo?',
                    style: Theme.of(context).textTheme.headlineMedium,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 32),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      ElevatedButton(
                        autofocus: true,
                        onPressed: () {
                          Navigator.of(context).pop();
                          _startTimer();
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Theme.of(context).colorScheme.surface,
                          foregroundColor: Theme.of(context).colorScheme.primary,
                        ),
                        child: const Text('CONTINUA'),
                      ),
                      const SizedBox(width: 16),
                      ElevatedButton(
                        onPressed: () async {
                          await _saveSession();
                          if (mounted) {
                            Navigator.of(context).pop();
                            Navigator.of(context).pop();
                          }
                        },
                        child: const Text('ESCI'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final currentLevel = widget.structure[currentLevelIndex];
    final nextLevel = currentLevelIndex < widget.structure.length - 1
        ? widget.structure[currentLevelIndex + 1]
        : null;

    final minutes = remainingSeconds ~/ 60;
    final seconds = remainingSeconds % 60;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) {
          _showExitDialog();
        }
      },
      child: KeyboardListener(
        focusNode: FocusNode()..requestFocus(),
        autofocus: true,
        onKeyEvent: (event) {
          if (event is KeyDownEvent) {
            if (event.logicalKey == LogicalKeyboardKey.mediaPlayPause ||
                event.logicalKey == LogicalKeyboardKey.select) {
              _togglePause();
            } else if (event.logicalKey == LogicalKeyboardKey.mediaFastForward ||
                event.logicalKey == LogicalKeyboardKey.arrowRight) {
              _nextLevel();
            } else if (event.logicalKey == LogicalKeyboardKey.mediaRewind ||
                event.logicalKey == LogicalKeyboardKey.arrowLeft) {
              _previousLevel();
            }
          }
        },
        child: Scaffold(
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'LIVELLO ${currentLevel.levelNumber}',
                  style: Theme.of(context).textTheme.displaySmall,
                ),
                const SizedBox(height: 40),
                if (isPaused)
                  Text(
                    'PAUSA',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                        ),
                  ),
                TweenAnimationBuilder<double>(
                  key: ValueKey(remainingSeconds),
                  duration: const Duration(milliseconds: 300),
                  tween: Tween<double>(
                    begin: remainingSeconds <= 10 && !isPaused ? 1.0 : 1.0,
                    end: remainingSeconds <= 10 && !isPaused ? 1.15 : 1.0,
                  ),
                  curve: Curves.easeInOut,
                  builder: (context, scale, child) {
                    return Transform.scale(
                      scale: scale,
                      child: Text(
                        '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}',
                        style: Theme.of(context).textTheme.displayLarge?.copyWith(
                              color: isPaused
                                  ? Theme.of(context).colorScheme.primary.withOpacity(0.6)
                                  : remainingSeconds <= 10
                                      ? Colors.red
                                      : null,
                              fontSize: remainingSeconds <= 10 && !isPaused
                                  ? (Theme.of(context).textTheme.displayLarge?.fontSize ?? 96) * 1.2
                                  : null,
                              fontWeight: remainingSeconds <= 10 && !isPaused
                                  ? FontWeight.bold
                                  : null,
                            ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 40),
                Text(
                  '${currentLevel.smallBlind} / ${currentLevel.bigBlind}',
                  style: Theme.of(context).textTheme.displayMedium,
                ),
                const SizedBox(height: 60),
                Container(
                  height: 1,
                  width: double.infinity,
                  margin: const EdgeInsets.symmetric(horizontal: 80),
                  color: Theme.of(context).colorScheme.primary.withOpacity(0.3),
                ),
                const SizedBox(height: 20),
                if (nextLevel != null)
                  Text(
                    'PROSSIMO:  ${nextLevel.smallBlind} / ${nextLevel.bigBlind}',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          color: Theme.of(context).colorScheme.primary.withOpacity(0.7),
                        ),
                  )
                else
                  Text(
                    'ULTIMO LIVELLO',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          color: Theme.of(context).colorScheme.primary.withOpacity(0.7),
                        ),
                  ),
                const Spacer(),
                Padding(
                  padding: const EdgeInsets.only(bottom: 40),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        '◀◀',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      const SizedBox(width: 80),
                      Text(
                        isPaused ? '▶ PLAY' : '⏸ PAUSA',
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                              color: Theme.of(context).colorScheme.primary,
                            ),
                      ),
                      const SizedBox(width: 80),
                      Text(
                        '▶▶',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    _audioService.dispose();
    // Disattiva wakelock quando si esce dalla schermata
    WakelockPlus.disable();
    super.dispose();
  }
}
