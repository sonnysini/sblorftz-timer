import 'package:flutter/material.dart';

class AddLevelPopup extends StatefulWidget {
  final int levelNumber;
  final int defaultSmallBlind;
  final int defaultBigBlind;
  final int defaultDuration;
  final Function(int, int, int) onAdd;

  const AddLevelPopup({
    super.key,
    required this.levelNumber,
    required this.defaultSmallBlind,
    required this.defaultBigBlind,
    required this.defaultDuration,
    required this.onAdd,
  });

  @override
  State<AddLevelPopup> createState() => _AddLevelPopupState();
}

class _AddLevelPopupState extends State<AddLevelPopup> {
  late int smallBlind;
  late int bigBlind;
  late int duration;

  @override
  void initState() {
    super.initState();
    smallBlind = widget.defaultSmallBlind;
    bigBlind = widget.defaultBigBlind;
    duration = widget.defaultDuration;
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        padding: const EdgeInsets.all(32),
        constraints: const BoxConstraints(maxWidth: 500),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              'NUOVO LIVELLO (${widget.levelNumber})',
              style: Theme.of(context).textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            _buildRow('Small Blind:', smallBlind),
            const SizedBox(height: 12),
            _buildRow('Big Blind:', bigBlind),
            const SizedBox(height: 12),
            _buildRow('Durata:', duration, isDuration: true),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: () => widget.onAdd(smallBlind, bigBlind, duration),
              child: const Text('AGGIUNGI'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRow(String label, int value, {bool isDuration = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: Theme.of(context).textTheme.bodyLarge,
        ),
        Text(
          isDuration ? '$value min' : value.toString(),
          style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                color: Theme.of(context).colorScheme.primary,
              ),
        ),
      ],
    );
  }
}
