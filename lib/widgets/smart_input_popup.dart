import 'package:flutter/material.dart';
import '../models/blind_level.dart';
import 'focusable_button.dart';

class SmartInputPopup extends StatelessWidget {
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

  List<int> _getSuggestedValues() {
    if (!isBlind) {
      return [5, 8, 10, 12, 15, 20];
    }

    final suggestions = <int>{};

    if (currentIndex > 0) {
      final prevLevel = structure[currentIndex - 1];
      suggestions.add(prevLevel.smallBlind);
      suggestions.add(prevLevel.bigBlind);
      suggestions.add((prevLevel.smallBlind * 1.5).round());
      suggestions.add(prevLevel.smallBlind * 2);
    }

    if (currentIndex < structure.length - 1) {
      final nextLevel = structure[currentIndex + 1];
      suggestions.add(nextLevel.smallBlind);
      suggestions.add(nextLevel.bigBlind);
    }

    suggestions.addAll([100, 200, 500, 1000, 2000, 5000, 10000, 15000, 20000]);

    final sorted = suggestions.toList()..sort();
    return sorted.take(6).toList();
  }

  @override
  Widget build(BuildContext context) {
    final suggestions = _getSuggestedValues();

    return Dialog(
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
            const SizedBox(height: 24),
            GridView.builder(
              shrinkWrap: true,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 2.5,
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: suggestions.length,
              itemBuilder: (context, index) {
                final value = suggestions[index];

                return FocusableButton(
                  text: value.toString(),
                  autofocus: index == 0,
                  onPressed: () => onConfirm(value),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
