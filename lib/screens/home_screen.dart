import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../services/session_service.dart';
import '../services/structure_service.dart';
import '../widgets/resume_popup.dart';
import '../widgets/focusable_button.dart';
import 'structure_selector_screen.dart';
import 'timer_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final SessionService _sessionService = SessionService();
  final StructureService _structureService = StructureService();
  final FocusNode _startButtonFocus = FocusNode();
  final FocusNode _customizeButtonFocus = FocusNode();

  @override
  void dispose() {
    _startButtonFocus.dispose();
    _customizeButtonFocus.dispose();
    super.dispose();
  }

  Future<void> _checkForSavedSession() async {
    final hasSession = await _sessionService.hasSession();
    if (hasSession && mounted) {
      final session = await _sessionService.loadSession();
      if (session != null && mounted) {
        _showResumePopup(session);
      }
    } else {
      // No saved session, start with selected structure
      final activeStructure = await _structureService.getActiveStructure();
      if (mounted) {
        _startTimerWithStructure(activeStructure);
      }
    }
  }

  void _showResumePopup(session) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => ResumePopup(
        session: session,
        onResume: () {
          Navigator.of(context).pop();
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (context) => TimerScreen(
                structure: session.structure,
                initialLevelIndex: session.currentLevelIndex,
                initialRemainingSeconds: session.remainingSeconds,
                initialPreviousLevelIndex: session.previousLevelIndex,
                initialPreviousRemainingSeconds: session.previousRemainingSeconds,
              ),
            ),
          );
        },
        onNew: () async {
          await _sessionService.clearSession();
          if (mounted) {
            Navigator.of(context).pop();
            // Start timer with selected structure
            final activeStructure = await _structureService.getActiveStructure();
            if (mounted) {
              _startTimerWithStructure(activeStructure);
            }
          }
        },
      ),
    );
  }

  void _startTimerWithStructure(structure) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => TimerScreen(structure: structure),
      ),
    );
  }

  void _startTournament() {
    // Check for saved session and show resume popup or start directly
    _checkForSavedSession();
  }

  void _openStructureSelector() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const StructureSelectorScreen(),
      ),
    );
  }

  void _handleKeyEvent(KeyEvent event) {
    if (event is KeyDownEvent) {
      if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
        _customizeButtonFocus.requestFocus();
      } else if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
        _startButtonFocus.requestFocus();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: KeyboardListener(
        focusNode: FocusNode(),
        autofocus: false,
        onKeyEvent: _handleKeyEvent,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'SBLORFTZ TIMER',
                style: Theme.of(context).textTheme.displaySmall,
              ),
              const SizedBox(height: 16),
              Text(
                '♠ ♥ ♣ ♦',
                style: Theme.of(context).textTheme.displaySmall?.copyWith(
                      color: Theme.of(context).colorScheme.primary,
                    ),
              ),
              const SizedBox(height: 80),
              FocusableButton(
                text: '▶ AVVIA TORNEO',
                focusNode: _startButtonFocus,
                autofocus: true,
                onPressed: _startTournament,
              ),
              const SizedBox(height: 16),
              FocusableButton(
                text: 'PERSONALIZZA',
                focusNode: _customizeButtonFocus,
                onPressed: _openStructureSelector,
              ),
              const Spacer(),
              Padding(
                padding: const EdgeInsets.only(bottom: 32),
                child: Text(
                  'v1.3',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
