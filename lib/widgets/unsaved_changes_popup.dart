import 'package:flutter/material.dart';
import 'focusable_button.dart';

/// Popup per gestire modifiche non salvate
/// Mostra 3 opzioni: ANNULLA (torna indietro), SALVA, ESCI (senza salvare)
class UnsavedChangesPopup extends StatelessWidget {
  final VoidCallback onCancel;
  final VoidCallback onSave;
  final VoidCallback onExit;

  const UnsavedChangesPopup({
    super.key,
    required this.onCancel,
    required this.onSave,
    required this.onExit,
  });

  @override
  Widget build(BuildContext context) {
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
                'Hai modifiche non salvate',
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  FocusableButton(
                    text: 'ANNULLA',
                    autofocus: true, // Scelta sicura come default
                    onPressed: onCancel,
                  ),
                  const SizedBox(width: 12),
                  FocusableButton(
                    text: 'SALVA',
                    onPressed: onSave,
                  ),
                  const SizedBox(width: 12),
                  FocusableButton(
                    text: 'ESCI',
                    customBackgroundColor: Colors.red,
                    customForegroundColor: Colors.white,
                    onPressed: onExit,
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
