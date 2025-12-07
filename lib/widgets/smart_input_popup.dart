import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/blind_level.dart';
import 'focusable_button.dart';

class SmartInputPopup extends StatefulWidget {
  final String title;
  final int currentValue;
  final bool isBlind;
  final List<BlindLevel> structure;
  final int currentIndex;
  final Function(int) onConfirm;

  const SmartInputPopup({
    super.key,
    required this.title,
    required this.currentValue,
    required this.isBlind,
    required this.structure,
    required this.currentIndex,
    required this.onConfirm,
  });

  @override
  State<SmartInputPopup> createState() => _SmartInputPopupState();
}

class _SmartInputPopupState extends State<SmartInputPopup> {
  late int _currentValue;

  @override
  void initState() {
    super.initState();
    _currentValue = widget.currentValue;
  }

  void _incrementValue() {
    setState(() {
      if (widget.isBlind) {
        _currentValue += 100; // Incremento di 100 per i bui
      } else {
        _currentValue += 1; // Incremento di 1 per i minuti
      }
    });
  }

  void _decrementValue() {
    setState(() {
      if (widget.isBlind) {
        _currentValue = (_currentValue - 100).clamp(100, double.infinity).toInt();
      } else {
        _currentValue = (_currentValue - 1).clamp(1, double.infinity).toInt();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final increment = widget.isBlind ? 100 : 1;

    return Focus(
      autofocus: true,
      onKeyEvent: (node, event) {
        if (event is! KeyDownEvent) return KeyEventResult.ignored;

        if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
          _incrementValue();
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
          _decrementValue();
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.select ||
            event.logicalKey == LogicalKeyboardKey.enter) {
          widget.onConfirm(_currentValue);
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.goBack ||
            event.logicalKey == LogicalKeyboardKey.escape) {
          Navigator.of(context).pop();
          return KeyEventResult.handled;
        }

        return KeyEventResult.ignored;
      },
      child: Dialog(
        child: Container(
          padding: const EdgeInsets.all(48),
          constraints: const BoxConstraints(maxWidth: 600),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                widget.title,
                style: theme.textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 48),
              // Freccia su
              Icon(
                Icons.arrow_drop_up,
                size: 64,
                color: theme.colorScheme.primary.withOpacity(0.5),
              ),
              const SizedBox(height: 16),
              // Valore corrente
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 48, vertical: 24),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primary.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: theme.colorScheme.primary,
                    width: 3,
                  ),
                ),
                child: Text(
                  widget.isBlind ? _currentValue.toString() : '$_currentValue min',
                  style: TextStyle(
                    fontSize: 56,
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.primary,
                  ),
                ),
              ),
              const SizedBox(height: 16),
              // Freccia giù
              Icon(
                Icons.arrow_drop_down,
                size: 64,
                color: theme.colorScheme.primary.withOpacity(0.5),
              ),
              const SizedBox(height: 32),
              // Istruzioni
              Text(
                '↑ ↓ per cambiare (±$increment) • OK per confermare',
                style: TextStyle(
                  fontSize: 14,
                  color: theme.colorScheme.onSurface.withOpacity(0.6),
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
