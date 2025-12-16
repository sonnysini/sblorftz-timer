import 'package:flutter/material.dart';

class StructureCard extends StatelessWidget {
  final String icon;
  final String title;
  final String description;
  final bool isSelected;
  final FocusNode? focusNode;
  final bool autofocus;
  final VoidCallback onPressed;

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
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Focus(
      focusNode: focusNode,
      autofocus: autofocus,
      onFocusChange: (hasFocus) {
        // Trigger rebuild per aggiornare lo stile
        (context as Element).markNeedsBuild();
      },
      child: Builder(
        builder: (context) {
          final hasFocus = Focus.of(context).hasFocus;

          return GestureDetector(
            onTap: onPressed,
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 150),
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: hasFocus
                    ? theme.colorScheme.primary.withOpacity(0.15)
                    : theme.colorScheme.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isSelected
                      ? theme.colorScheme.primary
                      : hasFocus
                          ? theme.colorScheme.primary.withOpacity(0.5)
                          : theme.colorScheme.onSurface.withOpacity(0.2),
                  width: isSelected ? 3 : hasFocus ? 2 : 1,
                ),
              ),
              child: Row(
                children: [
                  // Icona
                  Container(
                    width: 50,
                    height: 50,
                    decoration: BoxDecoration(
                      color: isSelected
                          ? theme.colorScheme.primary.withOpacity(0.2)
                          : theme.colorScheme.onSurface.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Center(
                      child: Text(
                        icon,
                        style: const TextStyle(fontSize: 24),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  // Testo
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: isSelected
                                ? theme.colorScheme.primary
                                : theme.colorScheme.onSurface,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          description,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSurface.withOpacity(0.7),
                          ),
                        ),
                      ],
                    ),
                  ),
                  // Checkmark se selezionato
                  if (isSelected)
                    Icon(
                      Icons.check_circle,
                      color: theme.colorScheme.primary,
                      size: 28,
                    ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
