import 'package:flutter/material.dart';
import '../models/blind_level.dart';
import '../data/default_structure.dart';
import '../widgets/smart_input_popup.dart';
import '../widgets/confirm_popup.dart';
import '../widgets/add_level_popup.dart';
import 'timer_screen.dart';

class EditorScreen extends StatefulWidget {
  final List<BlindLevel>? initialStructure;

  const EditorScreen({super.key, this.initialStructure});

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  late List<BlindLevel> structure;

  @override
  void initState() {
    super.initState();
    structure = widget.initialStructure ?? getDefaultStructure();
  }

  void _editBlind(int index, bool isSmallBlind) {
    final level = structure[index];
    final currentValue = isSmallBlind ? level.smallBlind : level.bigBlind;

    showDialog(
      context: context,
      builder: (context) => SmartInputPopup(
        title: '${isSmallBlind ? "SMALL" : "BIG"} BLIND - LV.${level.levelNumber}',
        currentValue: currentValue,
        isBlind: true,
        structure: structure,
        currentIndex: index,
        onConfirm: (newValue) {
          setState(() {
            if (isSmallBlind) {
              structure[index] = level.copyWith(smallBlind: newValue);
            } else {
              structure[index] = level.copyWith(bigBlind: newValue);
            }
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _editDuration(int index) {
    final level = structure[index];

    showDialog(
      context: context,
      builder: (context) => SmartInputPopup(
        title: 'DURATA - LV.${level.levelNumber}',
        currentValue: level.durationMinutes,
        isBlind: false,
        structure: structure,
        currentIndex: index,
        onConfirm: (newValue) {
          setState(() {
            structure[index] = level.copyWith(durationMinutes: newValue);
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _deleteLevel(int index) {
    final level = structure[index];
    showDialog(
      context: context,
      builder: (context) => ConfirmPopup(
        title: 'Eliminare il livello ${level.levelNumber}?',
        subtitle: '(${level.smallBlind} / ${level.bigBlind})',
        onConfirm: () {
          setState(() {
            structure.removeAt(index);
            // Rinumera i livelli
            for (int i = 0; i < structure.length; i++) {
              structure[i] = structure[i].copyWith(levelNumber: i + 1);
            }
          });
          Navigator.of(context).pop();
        },
        onCancel: () {
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _addLevel() {
    final lastLevel = structure.isNotEmpty ? structure.last : null;
    final defaultSB = lastLevel != null ? lastLevel.smallBlind * 2 : 100;
    final defaultBB = lastLevel != null ? lastLevel.bigBlind * 2 : 200;
    final defaultDuration = lastLevel?.durationMinutes ?? 15;

    showDialog(
      context: context,
      builder: (context) => AddLevelPopup(
        levelNumber: structure.length + 1,
        defaultSmallBlind: defaultSB,
        defaultBigBlind: defaultBB,
        defaultDuration: defaultDuration,
        onAdd: (sb, bb, duration) {
          setState(() {
            structure.add(BlindLevel(
              levelNumber: structure.length + 1,
              smallBlind: sb,
              bigBlind: bb,
              durationMinutes: duration,
            ));
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _startTimer() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => TimerScreen(structure: structure),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: const Text('STRUTTURA TORNEO'),
      ),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Table(
                border: TableBorder.all(
                  color: Theme.of(context).colorScheme.primary.withOpacity(0.3),
                ),
                columnWidths: const {
                  0: FlexColumnWidth(1),
                  1: FlexColumnWidth(2),
                  2: FlexColumnWidth(2),
                  3: FlexColumnWidth(2),
                  4: FlexColumnWidth(1),
                },
                children: [
                  TableRow(
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.surface,
                    ),
                    children: [
                      _buildHeaderCell('LV'),
                      _buildHeaderCell('SB'),
                      _buildHeaderCell('BB'),
                      _buildHeaderCell('DURATA'),
                      _buildHeaderCell(''),
                    ],
                  ),
                  ...structure.asMap().entries.map((entry) {
                    final index = entry.key;
                    final level = entry.value;
                    return TableRow(
                      children: [
                        _buildCell(level.levelNumber.toString()),
                        _buildEditableCell(
                          level.smallBlind.toString(),
                          () => _editBlind(index, true),
                        ),
                        _buildEditableCell(
                          level.bigBlind.toString(),
                          () => _editBlind(index, false),
                        ),
                        _buildEditableCell(
                          '${level.durationMinutes} min',
                          () => _editDuration(index),
                        ),
                        _buildDeleteCell(() => _deleteLevel(index)),
                      ],
                    );
                  }),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _addLevel,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Theme.of(context).colorScheme.surface,
                    foregroundColor: Theme.of(context).colorScheme.primary,
                  ),
                  child: const Text('+ AGGIUNGI LIVELLO'),
                ),
                ElevatedButton(
                  onPressed: _startTimer,
                  child: const Text('▶ AVVIA TIMER'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderCell(String text) {
    return Padding(
      padding: const EdgeInsets.all(12),
      child: Text(
        text,
        style: Theme.of(context).textTheme.bodyLarge?.copyWith(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.primary,
            ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildCell(String text) {
    return Padding(
      padding: const EdgeInsets.all(12),
      child: Text(
        text,
        style: Theme.of(context).textTheme.bodyLarge,
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildEditableCell(String text, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        color: Theme.of(context).colorScheme.surface.withOpacity(0.3),
        child: Text(
          text,
          style: Theme.of(context).textTheme.bodyLarge,
          textAlign: TextAlign.center,
        ),
      ),
    );
  }

  Widget _buildDeleteCell(VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        child: Icon(
          Icons.delete,
          color: Theme.of(context).colorScheme.error,
        ),
      ),
    );
  }
}
