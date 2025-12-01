import 'package:flutter/material.dart';
import '../models/saved_session.dart';

class ResumePopup extends StatelessWidget {
  final SavedSession session;
  final VoidCallback onResume;
  final VoidCallback onNew;

  const ResumePopup({
    super.key,
    required this.session,
    required this.onResume,
    required this.onNew,
  });

  @override
  Widget build(BuildContext context) {
    final currentLevel = session.structure[session.currentLevelIndex];
    final minutes = session.remainingSeconds ~/ 60;
    final seconds = session.remainingSeconds % 60;

    return FocusScope(
      autofocus: true,
      child: Dialog(
        child: Container(
          padding: const EdgeInsets.all(32),
          constraints: const BoxConstraints(maxWidth: 500),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'Torneo in corso trovato!',
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              Text(
                'Livello ${currentLevel.levelNumber} · ${currentLevel.smallBlind}/${currentLevel.bigBlind}',
                style: Theme.of(context).textTheme.bodyLarge,
              ),
              const SizedBox(height: 8),
              Text(
                'Tempo rimasto: ${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}',
                style: Theme.of(context).textTheme.bodyLarge,
              ),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ElevatedButton(
                    autofocus: true,
                    onPressed: onResume,
                    child: const Text('RIPRENDI'),
                  ),
                  const SizedBox(width: 16),
                  ElevatedButton(
                    onPressed: onNew,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Theme.of(context).colorScheme.surface,
                      foregroundColor: Theme.of(context).colorScheme.primary,
                    ),
                    child: const Text('NUOVO'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
