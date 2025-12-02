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
  final int? initialPreviousLevelIndex;
  final int? initialPreviousRemainingSeconds;

  const TimerScreen({
    super.key,
    required this.structure,
    this.initialLevelIndex = 0,
    this.initialRemainingSeconds = 0,
    this.initialPreviousLevelIndex,
    this.initialPreviousRemainingSeconds,
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

  // Stato precedente (per undo)
  int? _previousLevelIndex;
  int? _previousRemainingSeconds;

  // Pressione prolungata
  DateTime? _forwardPressStartTime;
  DateTime? _backwardPressStartTime;
  bool _isLongPressingForward = false;
  bool _isLongPressingBackward = false;
  Timer? _longPressCheckTimer;
  double _longPressProgress = 0.0;

  @override
  void initState() {
    super.initState();
    currentLevelIndex = widget.initialLevelIndex;

    if (widget.initialRemainingSeconds > 0) {
      remainingSeconds = widget.initialRemainingSeconds;
    } else {
      remainingSeconds = widget.structure[currentLevelIndex].durationMinutes * 60;
    }

    // Carica stato precedente se disponibile
    _previousLevelIndex = widget.initialPreviousLevelIndex;
    _previousRemainingSeconds = widget.initialPreviousRemainingSeconds;

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
      previousLevelIndex: _previousLevelIndex,
      previousRemainingSeconds: _previousRemainingSeconds,
    );
    await _sessionService.saveSession(session);
  }

  void _startLongPressForward() {
    // Controlla se si può andare avanti
    if (currentLevelIndex >= widget.structure.length - 1) return;

    setState(() {
      _forwardPressStartTime = DateTime.now();
      _isLongPressingForward = true;
      _longPressProgress = 0.0;
    });
    _startLongPressCheckTimer();
  }

  void _startLongPressBackward() {
    // Controlla se si può tornare indietro
    if (currentLevelIndex <= 0) return;

    setState(() {
      _backwardPressStartTime = DateTime.now();
      _isLongPressingBackward = true;
      _longPressProgress = 0.0;
    });
    _startLongPressCheckTimer();
  }

  void _startLongPressCheckTimer() {
    _longPressCheckTimer?.cancel();
    _longPressCheckTimer = Timer.periodic(const Duration(milliseconds: 50), (timer) {
      if (_isLongPressingForward && _forwardPressStartTime != null) {
        final elapsed = DateTime.now().difference(_forwardPressStartTime!).inMilliseconds;
        setState(() {
          _longPressProgress = elapsed / 3000.0;
        });

        if (elapsed >= 3000) {
          _cancelLongPress();
          _manualNextLevel();
        }
      } else if (_isLongPressingBackward && _backwardPressStartTime != null) {
        final elapsed = DateTime.now().difference(_backwardPressStartTime!).inMilliseconds;
        setState(() {
          _longPressProgress = elapsed / 3000.0;
        });

        if (elapsed >= 3000) {
          _cancelLongPress();
          _manualPreviousLevel();
        }
      }
    });
  }

  void _cancelLongPress() {
    setState(() {
      _forwardPressStartTime = null;
      _backwardPressStartTime = null;
      _isLongPressingForward = false;
      _isLongPressingBackward = false;
      _longPressProgress = 0.0;
    });
    _longPressCheckTimer?.cancel();
  }

  void _manualNextLevel() {
    if (currentLevelIndex < widget.structure.length - 1) {
      // Salva lo stato corrente prima di cambiare
      setState(() {
        _previousLevelIndex = currentLevelIndex;
        _previousRemainingSeconds = remainingSeconds;

        currentLevelIndex++;
        remainingSeconds = widget.structure[currentLevelIndex].durationMinutes * 60;
        _oneMinuteWarningPlayed = false;
        _tenSecondsWarningPlayed = false;
      });
      _audioService.playLevelChange();
      _saveSession();
    }
  }

  void _manualPreviousLevel() {
    // Controlla se esiste uno stato precedente salvato
    if (_previousLevelIndex == null || _previousRemainingSeconds == null) return;

    setState(() {
      currentLevelIndex = _previousLevelIndex!;
      remainingSeconds = _previousRemainingSeconds!;
      _previousLevelIndex = null;
      _previousRemainingSeconds = null;
      _oneMinuteWarningPlayed = false;
      _tenSecondsWarningPlayed = false;
    });
    _audioService.playLevelChange();
    _saveSession();
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
              // Toggle pausa (comportamento immediato)
              if (!_isLongPressingForward && !_isLongPressingBackward) {
                _togglePause();
              }
            } else if (event.logicalKey == LogicalKeyboardKey.mediaFastForward ||
                event.logicalKey == LogicalKeyboardKey.arrowRight) {
              // Avvia pressione prolungata forward
              if (!_isLongPressingForward) {
                _startLongPressForward();
              }
            } else if (event.logicalKey == LogicalKeyboardKey.mediaRewind ||
                event.logicalKey == LogicalKeyboardKey.arrowLeft) {
              // Avvia pressione prolungata backward
              if (!_isLongPressingBackward) {
                _startLongPressBackward();
              }
            }
          } else if (event is KeyUpEvent) {
            // Cancella pressione prolungata se rilasciato prima dei 3 secondi
            if (event.logicalKey == LogicalKeyboardKey.mediaFastForward ||
                event.logicalKey == LogicalKeyboardKey.arrowRight) {
              if (_isLongPressingForward) {
                _cancelLongPress();
              }
            } else if (event.logicalKey == LogicalKeyboardKey.mediaRewind ||
                event.logicalKey == LogicalKeyboardKey.arrowLeft) {
              if (_isLongPressingBackward) {
                _cancelLongPress();
              }
            }
          }
        },
        child: Scaffold(
          body: Stack(
            children: [
              Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'LIVELLO ${currentLevel.levelNumber}',
                      style: Theme.of(context).textTheme.displaySmall,
                    ),
                    const SizedBox(height: 40),
                    if (isPaused)
                      TweenAnimationBuilder<double>(
                        duration: const Duration(milliseconds: 1000),
                        tween: Tween<double>(begin: 0.5, end: 1.0),
                        curve: Curves.easeInOut,
                        builder: (context, value, child) {
                          return Opacity(
                            opacity: value,
                            child: Transform.scale(
                              scale: 0.95 + (value * 0.05),
                              child: Text(
                                'PAUSA',
                                style: TextStyle(
                                  fontSize: 90,
                                  fontWeight: FontWeight.bold,
                                  color: Theme.of(context).colorScheme.primary,
                                ),
                              ),
                            ),
                          );
                        },
                        onEnd: () {
                          // Ripeti l'animazione se ancora in pausa
                          if (isPaused && mounted) {
                            setState(() {});
                          }
                        },
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
          // Overlay barra di progresso per pressione prolungata
          if (_longPressProgress > 0)
            Container(
              color: Colors.black.withOpacity(0.8),
              child: Center(
                child: Container(
                  padding: const EdgeInsets.all(40),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.surface,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  constraints: const BoxConstraints(maxWidth: 500),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        'CAMBIO LIVELLO IN CORSO...',
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                              color: Theme.of(context).colorScheme.primary,
                            ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 32),
                      LinearProgressIndicator(
                        value: _longPressProgress.clamp(0.0, 1.0),
                        minHeight: 8,
                        backgroundColor: Theme.of(context).colorScheme.surface,
                        valueColor: AlwaysStoppedAnimation<Color>(
                          Theme.of(context).colorScheme.primary,
                        ),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        '${(_longPressProgress * 100).toInt()}%',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    _longPressCheckTimer?.cancel();
    _audioService.dispose();
    // Disattiva wakelock quando si esce dalla schermata
    WakelockPlus.disable();
    super.dispose();
  }
}
