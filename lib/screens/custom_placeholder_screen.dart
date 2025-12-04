import 'package:flutter/material.dart';

/// Schermata placeholder per CUSTOM - in sviluppo
class CustomPlaceholderScreen extends StatelessWidget {
  const CustomPlaceholderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Header
            Padding(
              padding: const EdgeInsets.all(24),
              child: Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.arrow_back),
                    onPressed: () => Navigator.of(context).pop(),
                  ),
                  const Spacer(),
                  Text(
                    'CUSTOM',
                    style: theme.textTheme.headlineSmall,
                  ),
                  const Spacer(),
                  const SizedBox(width: 48),
                ],
              ),
            ),
            // Contenuto centrale
            Expanded(
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      Icons.construction,
                      size: 64,
                      color: theme.colorScheme.primary,
                    ),
                    const SizedBox(height: 24),
                    Text(
                      'IN SVILUPPO',
                      style: theme.textTheme.headlineMedium?.copyWith(
                        color: theme.colorScheme.primary,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Questa funzionalità sarà\ndisponibile a breve',
                      style: theme.textTheme.bodyLarge,
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
