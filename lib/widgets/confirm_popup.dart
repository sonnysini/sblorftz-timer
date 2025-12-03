import 'package:flutter/material.dart';
import 'focusable_button.dart';

class ConfirmPopup extends StatelessWidget {
  final String title;
  final String? subtitle;
  final VoidCallback onConfirm;
  final VoidCallback onCancel;

  const ConfirmPopup({
    super.key,
    required this.title,
    this.subtitle,
    required this.onConfirm,
    required this.onCancel,
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
                title,
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              if (subtitle != null) ...[
                const SizedBox(height: 8),
                Text(
                  subtitle!,
                  style: Theme.of(context).textTheme.bodyLarge,
                  textAlign: TextAlign.center,
                ),
              ],
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  FocusableButton(
                    text: 'NO',
                    autofocus: true,
                    onPressed: onCancel,
                  ),
                  const SizedBox(width: 16),
                  FocusableButton(
                    text: 'SÌ',
                    onPressed: onConfirm,
                    customBackgroundColor: Theme.of(context).colorScheme.error,
                    customForegroundColor: Theme.of(context).colorScheme.onError,
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
