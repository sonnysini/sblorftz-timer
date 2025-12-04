import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/blind_level.dart';

/// Riga livello nell'editor con focus interno su SB, DURATA, e [✕]
class LevelRow extends StatefulWidget {
  final BlindLevel level;
  final bool hasFocus;
  final VoidCallback onEditSmallBlind;
  final VoidCallback onEditDuration;
  final VoidCallback onDelete;
  final FocusNode? focusNode;

  const LevelRow({
    super.key,
    required this.level,
    required this.hasFocus,
    required this.onEditSmallBlind,
    required this.onEditDuration,
    required this.onDelete,
    this.focusNode,
  });

  @override
  State<LevelRow> createState() => _LevelRowState();
}

class _LevelRowState extends State<LevelRow> {
  late FocusNode _internalFocusNode;
  int _internalFocusIndex = 0; // 0=SB, 1=DURATA, 2=[✕]
  bool _hasFocus = false;

  @override
  void initState() {
    super.initState();
    _internalFocusNode = widget.focusNode ?? FocusNode();
    _internalFocusNode.addListener(_onFocusChange);
    _hasFocus = _internalFocusNode.hasFocus;
  }

  @override
  void dispose() {
    _internalFocusNode.removeListener(_onFocusChange);
    if (widget.focusNode == null) {
      _internalFocusNode.dispose();
    }
    super.dispose();
  }

  void _onFocusChange() {
    setState(() {
      _hasFocus = _internalFocusNode.hasFocus;
      if (!_hasFocus) {
        _internalFocusIndex = 0; // Reset quando perde focus
      }
    });
  }

  void _handleKeyEvent(KeyEvent event) {
    if (event is KeyDownEvent && _hasFocus) {
      if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
        setState(() {
          _internalFocusIndex = (_internalFocusIndex + 1).clamp(0, 2);
        });
      } else if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
        setState(() {
          _internalFocusIndex = (_internalFocusIndex - 1).clamp(0, 2);
        });
      } else if (event.logicalKey == LogicalKeyboardKey.select ||
          event.logicalKey == LogicalKeyboardKey.enter) {
        if (_internalFocusIndex == 0) {
          widget.onEditSmallBlind();
        } else if (_internalFocusIndex == 1) {
          widget.onEditDuration();
        } else if (_internalFocusIndex == 2) {
          widget.onDelete();
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return KeyboardListener(
      focusNode: _internalFocusNode,
      onKeyEvent: _handleKeyEvent,
      child: InkWell(
        focusNode: _internalFocusNode,
        onTap: () {}, // Required for focusNode
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: BoxDecoration(
            color: _hasFocus
                ? theme.colorScheme.primary.withOpacity(0.1)
                : Colors.transparent,
            border: Border.all(
              color: _hasFocus
                  ? theme.colorScheme.primary
                  : Colors.transparent,
              width: 2,
            ),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              // Numero livello
              SizedBox(
                width: 60,
                child: Text(
                  'LV.${widget.level.levelNumber}',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(width: 16),
              // Small Blind (modificabile)
              _buildFocusableValue(
                context: context,
                value: '${widget.level.smallBlind}',
                isFocused: _hasFocus && _internalFocusIndex == 0,
                width: 100,
              ),
              const SizedBox(width: 8),
              Text(
                '/',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: theme.colorScheme.onSurface.withOpacity(0.5),
                ),
              ),
              const SizedBox(width: 8),
              // Big Blind (non modificabile)
              SizedBox(
                width: 100,
                child: Text(
                  '${widget.level.bigBlind}',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: theme.colorScheme.onSurface.withOpacity(0.7),
                  ),
                ),
              ),
              const Spacer(),
              // Durata (modificabile)
              _buildFocusableValue(
                context: context,
                value: '${widget.level.durationMinutes} min',
                isFocused: _hasFocus && _internalFocusIndex == 1,
                width: 100,
              ),
              const SizedBox(width: 24),
              // Pulsante elimina [✕]
              _buildDeleteButton(
                context: context,
                isFocused: _hasFocus && _internalFocusIndex == 2,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFocusableValue({
    required BuildContext context,
    required String value,
    required bool isFocused,
    required double width,
  }) {
    final theme = Theme.of(context);
    return Container(
      width: width,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: isFocused
            ? theme.colorScheme.primary
            : theme.colorScheme.surface,
        border: Border.all(
          color: isFocused
              ? theme.colorScheme.primary
              : theme.colorScheme.onSurface.withOpacity(0.3),
          width: 2,
        ),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        value,
        style: theme.textTheme.titleMedium?.copyWith(
          color: isFocused
              ? theme.scaffoldBackgroundColor
              : theme.colorScheme.onSurface,
          fontWeight: FontWeight.bold,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildDeleteButton({
    required BuildContext context,
    required bool isFocused,
  }) {
    final theme = Theme.of(context);
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: isFocused ? Colors.red : Colors.transparent,
        border: Border.all(
          color: isFocused ? Colors.red : theme.colorScheme.onSurface.withOpacity(0.3),
          width: 2,
        ),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Icon(
        Icons.close,
        color: isFocused ? Colors.white : Colors.red.withOpacity(0.7),
        size: 24,
      ),
    );
  }
}
