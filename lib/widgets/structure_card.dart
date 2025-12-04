import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Card per visualizzare una struttura nel selettore
class StructureCard extends StatefulWidget {
  final String icon;
  final String title;
  final String description;
  final bool isSelected;
  final VoidCallback onPressed;
  final FocusNode? focusNode;
  final bool autofocus;

  const StructureCard({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    required this.isSelected,
    required this.onPressed,
    this.focusNode,
    this.autofocus = false,
  });

  @override
  State<StructureCard> createState() => _StructureCardState();
}

class _StructureCardState extends State<StructureCard> {
  late FocusNode _internalFocusNode;
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
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // Colori in base a focus e selezione
    final borderColor = widget.isSelected
        ? const Color(0xFFFFD700) // Oro per selezionata
        : _hasFocus
            ? theme.colorScheme.primary
            : theme.colorScheme.onSurface.withOpacity(0.3);

    final backgroundColor = _hasFocus
        ? theme.colorScheme.primary.withOpacity(0.1)
        : Colors.transparent;

    return Focus(
      focusNode: _internalFocusNode,
      autofocus: widget.autofocus,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter ||
              event.logicalKey == LogicalKeyboardKey.space) {
            widget.onPressed();
            return KeyEventResult.handled;
          }
        }
        return KeyEventResult.ignored;
      },
      child: InkWell(
        onTap: widget.onPressed,
        child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: backgroundColor,
          border: Border.all(
            color: borderColor,
            width: widget.isSelected ? 3 : 2,
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          children: [
            // Icona
            Text(
              widget.icon,
              style: const TextStyle(fontSize: 32),
            ),
            const SizedBox(width: 16),
            // Testo
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.title,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: widget.isSelected
                          ? const Color(0xFFFFD700)
                          : null,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    widget.description,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurface.withOpacity(0.7),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 16),
            // Indicatore selezione
            Container(
              width: 32,
              height: 32,
              decoration: BoxDecoration(
                border: Border.all(
                  color: widget.isSelected
                      ? const Color(0xFFFFD700)
                      : theme.colorScheme.onSurface.withOpacity(0.5),
                  width: 2,
                ),
                borderRadius: BorderRadius.circular(4),
                color: widget.isSelected
                    ? const Color(0xFFFFD700)
                    : Colors.transparent,
              ),
              child: widget.isSelected
                  ? const Icon(
                      Icons.check,
                      color: Colors.black,
                      size: 24,
                    )
                  : null,
            ),
          ],
        ),
        ),
      ),
    );
  }
}
