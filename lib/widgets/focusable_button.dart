import 'package:flutter/material.dart';

class FocusableButton extends StatefulWidget {
  final String text;
  final VoidCallback onPressed;
  final FocusNode? focusNode;
  final bool autofocus;
  final Color? customBackgroundColor;
  final Color? customForegroundColor;

  const FocusableButton({
    super.key,
    required this.text,
    required this.onPressed,
    this.focusNode,
    this.autofocus = false,
    this.customBackgroundColor,
    this.customForegroundColor,
  });

  @override
  State<FocusableButton> createState() => _FocusableButtonState();
}

class _FocusableButtonState extends State<FocusableButton> {
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

    // Determina i colori in base al focus
    final backgroundColor = _hasFocus
        ? (widget.customBackgroundColor ?? theme.colorScheme.primary)
        : Colors.transparent;

    final foregroundColor = _hasFocus
        ? (widget.customForegroundColor ?? theme.scaffoldBackgroundColor)
        : theme.colorScheme.onSurface.withOpacity(0.6);

    final borderColor = _hasFocus
        ? (widget.customBackgroundColor ?? theme.colorScheme.primary)
        : theme.colorScheme.onSurface.withOpacity(0.3);

    return ElevatedButton(
      focusNode: _internalFocusNode,
      autofocus: widget.autofocus,
      onPressed: widget.onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: backgroundColor,
        foregroundColor: foregroundColor,
        side: BorderSide(color: borderColor, width: 2),
        textStyle: const TextStyle(
          fontSize: 22,
          fontWeight: FontWeight.bold,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 48, vertical: 24),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
      child: Text(widget.text),
    );
  }
}
