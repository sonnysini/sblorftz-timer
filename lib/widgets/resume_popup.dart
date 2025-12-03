import 'package:flutter/material.dart';
import '../models/saved_session.dart';
import 'focusable_button.dart';

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
                  FocusableButton(
                    text: 'RIPRENDI',
                    autofocus: true,
                    onPressed: onResume,
                  ),
                  const SizedBox(width: 16),
                  FocusableButton(
                    text: 'NUOVO',
                    onPressed: onNew,
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
