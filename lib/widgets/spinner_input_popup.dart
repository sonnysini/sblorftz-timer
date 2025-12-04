import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Popup generico con spinner per modificare valori numerici
/// Supporta frecce su/giù per incrementare/decrementare
class SpinnerInputPopup extends StatefulWidget {
  final String title;
  final int initialValue;
  final String Function(int) formatValue;
  final int Function(int currentValue) getIncrement;
  final int Function(int currentValue) getDecrement;
  final bool Function(int currentValue) canIncrement;
  final bool Function(int currentValue) canDecrement;
  final String? subtitle;
  final String? hint;

  const SpinnerInputPopup({
    super.key,
    required this.title,
    required this.initialValue,
    required this.formatValue,
    required this.getIncrement,
    required this.getDecrement,
    required this.canIncrement,
    required this.canDecrement,
    this.subtitle,
    this.hint,
  });

  @override
  State<SpinnerInputPopup> createState() => _SpinnerInputPopupState();
}

class _SpinnerInputPopupState extends State<SpinnerInputPopup> {
  late int _currentValue;

  @override
  void initState() {
    super.initState();
    _currentValue = widget.initialValue;
  }

  void _increment() {
    if (widget.canIncrement(_currentValue)) {
      setState(() {
        _currentValue = widget.getIncrement(_currentValue);
      });
    }
  }

  void _decrement() {
    if (widget.canDecrement(_currentValue)) {
      setState(() {
        _currentValue = widget.getDecrement(_currentValue);
      });
    }
  }

  void _handleKeyEvent(KeyEvent event) {
    if (event is KeyDownEvent) {
      if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
        _increment();
      } else if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
        _decrement();
      } else if (event.logicalKey == LogicalKeyboardKey.select ||
          event.logicalKey == LogicalKeyboardKey.enter) {
        Navigator.of(context).pop(_currentValue);
      } else if (event.logicalKey == LogicalKeyboardKey.goBack ||
          event.logicalKey == LogicalKeyboardKey.escape) {
        Navigator.of(context).pop(null);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final canGoUp = widget.canIncrement(_currentValue);
    final canGoDown = widget.canDecrement(_currentValue);

    return KeyboardListener(
      focusNode: FocusNode()..requestFocus(),
      autofocus: true,
      onKeyEvent: _handleKeyEvent,
      child: Dialog(
        child: Container(
          padding: const EdgeInsets.all(32),
          constraints: const BoxConstraints(maxWidth: 400),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                widget.title,
                style: theme.textTheme.headlineSmall,
                textAlign: TextAlign.center,
              ),
              if (widget.subtitle != null) ...[
                const SizedBox(height: 12),
                Text(
                  widget.subtitle!,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    color: theme.colorScheme.primary,
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
              const SizedBox(height: 32),
              // Freccia SU
              Icon(
                Icons.arrow_drop_up,
                size: 48,
                color: canGoUp
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurface.withOpacity(0.3),
              ),
              const SizedBox(height: 8),
              // Valore corrente
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 16,
                ),
                decoration: BoxDecoration(
                  border: Border.all(
                    color: theme.colorScheme.primary,
                    width: 2,
                  ),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  widget.formatValue(_currentValue),
                  style: theme.textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              // Freccia GIÙ
              Icon(
                Icons.arrow_drop_down,
                size: 48,
                color: canGoDown
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurface.withOpacity(0.3),
              ),
              const SizedBox(height: 24),
              if (widget.hint != null)
                Text(
                  widget.hint!,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.onSurface.withOpacity(0.7),
                  ),
                  textAlign: TextAlign.center,
                ),
              const SizedBox(height: 16),
              Text(
                'Premi OK per confermare',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurface.withOpacity(0.7),
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
